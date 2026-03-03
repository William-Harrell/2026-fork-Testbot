package frc.robot.auto;

// For UI in testing
import javax.swing.*;

import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
// End of UI/testing libs
import java.util.List;
import java.util.PriorityQueue;

import frc.robot.util.constants.FieldConstants;
import frc.robot.util.constants.RobotPhysicalConstants;

public class Auto {
    /*
     * AUTO MOVEMENT PLAN
     * 
     * 1.) Plot the field as a grid (scalar field)
     * 
     * 2.) Estimate position of robot on grid
     * 
     * 3.) Estimate position of target position on grid
     * 
     * 4.) Get obstacles position on grid and give their cells (and surrounding
     * cells) higher values
     * 
     * 4B.) Get velocity as well to determine values of surounding cells of
     * obstacles
     * 
     * 5.) Path by lowest sum. Weight chart with highest weight given to most
     * "obstructed" regions
     * 
     * 6.) Accomplish at maximum a set amount of "steps" before recalculating the
     * path
     * 
     * USING A* algo
     * 
     * Things we already have:
     * - Pose estimation
     * - Field object coordinates
     */

    // Instance vars
    private int s; // Scaling factor
    private int width; // Grid width
    private int length; // Grid height
    private int max; // Maximum heuristic value

    // Special cell states
    private boolean[][] buffer; // true = inflated padding, false = real obstacle or free
    private boolean[][] unreachable;

    // Fields
    private double[][] ConstantField; // Our grid of obstacles that never move
    private double[][] MutableField; // Our grid of obstacles that constantly updates

    // Static vars
    public static final String ANSI_RESET = "\u001B[0m"; // Apply the color white in Java terminal
    public static final String ANSI_RED = "\u001B[31m"; // Apply the color red in Java terminal
    public static final String ANSI_GREEN = "\u001B[32m"; // Apply the color green in Java terminal
    public static final String ANSI_YELLOW = "\u001B[33m"; // Apply the color yellow in Java terminal

    // The template for a field object (it's x, y, length, and width)
    // idk the origin yet
    // rn we're assuming everything's a rectangle so orientation isn't big right
    public static record FieldObject(double x, double y, double length, double width) {
    }

    // Constructor
    public Auto(int max_heuristic) {
        s = 10; // Scaling factor (cells per meter)

        // cols (x) cover FIELD_LENGTH, rows (y) cover FIELD_WIDTH — both at s cells/m
        width = 2 * s * (int) Math.floor(FieldConstants.FIELD_LENGTH / 2) + 1; // (make them odd so it's centered)
        length = 2 * s * (int) Math.floor(FieldConstants.FIELD_WIDTH / 2) + 1;

        max = max_heuristic;

        ConstantField = new double[length][width]; // Inches
        MutableField = new double[length][width]; // Inches
    }

    // For short-term testing in development
    public static void main(String[] args) {
        var myAuto = new Auto(50);
        double tolerance = RobotPhysicalConstants.ROBOT_WIDTH * 0.5; // Collision box for robot

        myAuto.initializeConstantField(tolerance);

        // myAuto.print(); // <-- (Not nearly as clean, especially w/ large scaling)
        // faster, tho
        myAuto.display(); // very nice indeed
    }

    // Create field method
    public void initializeConstantField(double tolerance) {
        int padding = (int) Math.ceil(tolerance * s); // padding around obstacles based on robot width

        // Cell states
        buffer = new boolean[length][width];
        unreachable = new boolean[length][width];

        for (FieldObject obj : FieldConstants.nogos) {
            int x = (int) Math.round(map(obj.x(), 0, FieldConstants.FIELD_LENGTH, 0, width));
            int y = (int) Math.round(map(obj.y(), 0, FieldConstants.FIELD_WIDTH, 0, length));
            int w = (int) Math.round(map(obj.width(), 0, FieldConstants.FIELD_WIDTH, 0, length));
            int l = (int) Math.round(map(obj.length(), 0, FieldConstants.FIELD_LENGTH, 0, width));

            if (x >= width || y >= length || x + l <= 0 || y + w <= 0)
                continue;

            int pxStart = Math.max(x - padding, 0);
            int pyStart = Math.max(y - padding, 0);
            int pxEnd = Math.min(x + l + padding, width);
            int pyEnd = Math.min(y + w + padding, length);
            for (int i = pyStart; i < pyEnd; i++)
                Arrays.fill(ConstantField[i], pxStart, pxEnd, max);
        }

        // Pass 2: stamp all real footprints on top with sentinel — nothing can
        // overwrite these now
        for (FieldObject obj : FieldConstants.nogos) {
            int x = (int) Math.round(map(obj.x(), 0, FieldConstants.FIELD_LENGTH, 0, width));
            int y = (int) Math.round(map(obj.y(), 0, FieldConstants.FIELD_WIDTH, 0, length));
            int w = (int) Math.round(map(obj.width(), 0, FieldConstants.FIELD_WIDTH, 0, length));
            int l = (int) Math.round(map(obj.length(), 0, FieldConstants.FIELD_LENGTH, 0, width));

            if (x >= width || y >= length || x + l <= 0 || y + w <= 0) {
                System.out.println("Fully out of bounds, skipping: " + obj);
                continue;
            }

            int xStart = Math.max(x, 0);
            int yStart = Math.max(y, 0);
            int xEnd = Math.min(x + l, width);
            int yEnd = Math.min(y + w, length);
            for (int i = yStart; i < yEnd; i++)
                Arrays.fill(ConstantField[i], xStart, xEnd, max + 1);
        }

        // Pass 3: classify
        for (int y = 0; y < length; y++) {
            for (int x = 0; x < width; x++) {
                if (ConstantField[y][x] > max) {
                    ConstantField[y][x] = max; // real obstacle
                } else if (ConstantField[y][x] == max) {
                    buffer[y][x] = true; // padding only
                }
            }
        }

        applyDistanceHeuristic(ConstantField, FieldConstants.CENTER_X, FieldConstants.CENTER_Y);

        List<double[]> waypoints = findPath(0, 0, 0, 0);

        for (double[] wp : waypoints) {
            ConstantField[(int) Math.round(2 * s * wp[0])][(int) Math.round(2 * s * wp[1])] = 0;
        }
    }

    /**
     * Finds the optimal path from the robot's current position to a target,
     * routing around obstacles in both the constant and mutable fields.
     *
     * Uses A* with the grid cell value as traversal cost, so high-heuristic
     * cells (near obstacles) are naturally avoided in favor of lower-cost routes.
     *
     * The two fields are treated as layers — the effective cost of a cell is
     * the max of ConstantField and MutableField, so a mutable obstacle (e.g. a
     * moving robot) blocks just as hard as a wall, whichever is higher.
     *
     * @param robotX  robot's current X position in meters
     * @param robotY  robot's current Y position in meters
     * @param targetX destination X position in meters
     * @param targetY destination Y position in meters
     * @return list of [x, y] waypoints in meters from start to target,
     *         or an empty list if no path exists
     */
    public List<double[]> findPath(double robotX, double robotY, double targetX, double targetY) {

        // Convert real-world coords to grid cells
        int startX = (int) Math.round(map(robotX, 0, FieldConstants.FIELD_LENGTH, 0, width - 1));
        int startY = (int) Math.round(map(robotY, 0, FieldConstants.FIELD_WIDTH, 0, length - 1));
        int goalX = (int) Math.round(map(targetX, 0, FieldConstants.FIELD_LENGTH, 0, width - 1));
        int goalY = (int) Math.round(map(targetY, 0, FieldConstants.FIELD_WIDTH, 0, length - 1));

        // Clamp to grid bounds
        startX = Math.max(0, Math.min(startX, width - 1));
        startY = Math.max(0, Math.min(startY, length - 1));
        goalX = Math.max(0, Math.min(goalX, width - 1));
        goalY = Math.max(0, Math.min(goalY, length - 1));

        // Bail early if start or goal is inside an obstacle
        if (cellCost(startX, startY) >= max || cellCost(goalX, goalY) >= max) {
            System.out.println("A*: start or goal is inside an obstacle.");
            return Collections.emptyList();
        }

        // --- A* data structures ---

        // gScore[y][x] = best known cost from start to this cell
        double[][] gScore = new double[length][width];
        for (double[] row : gScore)
            Arrays.fill(row, Double.MAX_VALUE);
        gScore[startY][startX] = 0;

        // fScore[y][x] = gScore + heuristic
        double[][] fScore = new double[length][width];
        for (double[] row : fScore)
            Arrays.fill(row, Double.MAX_VALUE);
        fScore[startY][startX] = heuristic(startX, startY, goalX, goalY);

        // Parent tracking for path reconstruction — encoded as (parentY * width +
        // parentX)
        int[][] parent = new int[length][width];
        for (int[] row : parent)
            Arrays.fill(row, -1);

        // Open set: [fScore, x, y]
        PriorityQueue<double[]> open = new PriorityQueue<>(
                Comparator.comparingDouble(n -> n[0]));
        open.add(new double[] { fScore[startY][startX], startX, startY });

        boolean[][] visited = new boolean[length][width];

        int[] dx = { -1, 0, 1, -1, 1, -1, 0, 1 };
        int[] dy = { -1, -1, -1, 0, 0, 1, 1, 1 };
        // Diagonal moves cost slightly more (sqrt2) than cardinal moves (1.0)
        double[] moveCost = { 1.414, 1.0, 1.414, 1.0, 1.0, 1.414, 1.0, 1.414 };

        while (!open.isEmpty()) {
            double[] cur = open.poll();
            int cx = (int) cur[1];
            int cy = (int) cur[2];

            if (visited[cy][cx])
                continue;
            visited[cy][cx] = true;

            // Goal reached — reconstruct path
            if (cx == goalX && cy == goalY) {
                return reconstructPath(parent, goalX, goalY);
            }

            for (int d = 0; d < 8; d++) {
                int nx = cx + dx[d];
                int ny = cy + dy[d];

                if (nx < 0 || nx >= width || ny < 0 || ny >= length)
                    continue;
                if (visited[ny][nx])
                    continue;

                double cost = cellCost(nx, ny);
                if (cost >= max)
                    continue; // impassable

                // Step cost = movement distance × normalized cell cost as a weight
                // Cells near obstacles cost more to traverse, pushing paths toward open space
                double cellWeight = 1.0 + (cost / max) * 10.0; // tune the multiplier as needed
                double tentativeG = gScore[cy][cx] + moveCost[d] * cellWeight;

                if (tentativeG < gScore[ny][nx]) {
                    gScore[ny][nx] = tentativeG;
                    fScore[ny][nx] = tentativeG + heuristic(nx, ny, goalX, goalY);
                    parent[ny][nx] = cy * width + cx; // encode parent coords
                    open.add(new double[] { fScore[ny][nx], nx, ny });
                }
            }
        }

        System.out.println("A*: no path found.");
        return Collections.emptyList();
    }

    /**
     * Returns the effective traversal cost of a cell, combining both field layers.
     * MutableField slots in here — when populated, dynamic obstacles raise costs
     * exactly the same way constant obstacles do
     */
    private double cellCost(int x, int y) {
        return Math.max(ConstantField[y][x], MutableField[y][x]);
    }

    /**
     * Octile distance heuristic — matches the 8-directional movement model.
     * Admissible (never overestimates), so A* is guaranteed optimal.
     */
    private double heuristic(int x1, int y1, int x2, int y2) {
        int dx = Math.abs(x1 - x2);
        int dy = Math.abs(y1 - y2);
        return (dx + dy) + (1.414 - 2.0) * Math.min(dx, dy);
    }

    /**
     * Walks the parent map from goal back to start, then reverses and converts
     * each grid cell back to real-world meters.
     */
    private List<double[]> reconstructPath(int[][] parent, int goalX, int goalY) {
        List<double[]> path = new ArrayList<>();

        int cx = goalX, cy = goalY;
        while (parent[cy][cx] != -1) {
            double worldX = map(cx, 0, width - 1, 0, FieldConstants.FIELD_LENGTH);
            double worldY = map(cy, 0, length - 1, 0, FieldConstants.FIELD_WIDTH);
            path.add(new double[] { worldX, worldY });

            int encoded = parent[cy][cx];
            cx = encoded % width;
            cy = encoded / width;
        }

        // Add start cell
        path.add(new double[] {
                map(cx, 0, width - 1, 0, FieldConstants.FIELD_LENGTH),
                map(cy, 0, length - 1, 0, FieldConstants.FIELD_WIDTH)
        });

        Collections.reverse(path);
        return path;
    }

    public void applyDistanceHeuristic(double[][] field, double tx, double ty) {
        int targetX = (int) Math.round(map(tx, 0, FieldConstants.FIELD_LENGTH, 0, width - 1));
        int targetY = (int) Math.round(map(ty, 0, FieldConstants.FIELD_WIDTH, 0, length - 1));

        int[][] hops = new int[length][width];
        for (int[] row : hops)
            Arrays.fill(row, -1);

        Deque<int[]> queue = new ArrayDeque<>();

        if (field[targetY][targetX] < max) {
            hops[targetY][targetX] = 0;
            queue.add(new int[] { targetX, targetY });
        }

        int[] dx = { -1, 0, 1, -1, 1, -1, 0, 1 };
        int[] dy = { -1, -1, -1, 0, 0, 1, 1, 1 };
        int maxHops = 0;

        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            int cx = cur[0], cy = cur[1];

            for (int d = 0; d < 8; d++) {
                int nx = cx + dx[d];
                int ny = cy + dy[d];

                if (nx < 0 || nx >= width || ny < 0 || ny >= length)
                    continue;
                if (field[ny][nx] >= max || hops[ny][nx] != -1)
                    continue;

                hops[ny][nx] = hops[cy][cx] + 1;
                if (hops[ny][nx] > maxHops)
                    maxHops = hops[ny][nx];
                queue.add(new int[] { nx, ny });
            }
        }

        for (int y = 0; y < length; y++) {
            for (int x = 0; x < width; x++) {
                if (field[y][x] >= max)
                    continue; // obstacle/buffer
                if (hops[y][x] == -1) {
                    field[y][x] = (maxHops > 0) ? map(hops[y][x], 0, maxHops, 0, max - 1) : 0;
                    unreachable[y][x] = true; // trapped free cell, not a wall
                } else {
                    field[y][x] = (maxHops > 0) ? map(hops[y][x], 0, maxHops, 0, max) : 0;
                }
            }
        }
    }

    // GUI Methods
    private static void assignColor(double a, int max, boolean isBuffer, boolean isUnreachable, JButton cell) {
        if (isUnreachable) {
            cell.setBackground(new Color(80, 80, 80)); // DARK GRAY — unreachable

        } else if (isBuffer) {
            cell.setBackground(new Color(255, 182, 193)); // PINK — buffer

        } else if (a >= max) {
            cell.setBackground(new Color(Color.HSBtoRGB(0f, 0f, 1f))); // WHITE — obstacle

        } else if (a > 0.9 * max) {
            cell.setBackground(new Color(Color.HSBtoRGB(0.0f, 0.99f, (float) (a / max)))); // RED

        } else if (a > 0.5 * max) {
            cell.setBackground(new Color(Color.HSBtoRGB(0.167f, 0.99f, (float) (a / max)))); // YELLOW

        } else if (a > 0.25 * max) {
            cell.setBackground(new Color(Color.HSBtoRGB(0.333f, 0.99f, (float) (a / max)))); // GREEN

        } else {
            cell.setBackground(new Color(Color.HSBtoRGB(0f, 0f, (float) (a / max)))); // WHITE/DEFAULT
        }
    }

    public void display() {
        JFrame constantview = new JFrame("Field View (Constant)");
        constantview.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(length, width));

        for (int y = 0; y < length; y++) {
            for (int x = 0; x < width; x++) {
                double val = ConstantField[y][x];
                JButton cell = new JButton();

                boolean isBuffer = buffer[y][x];
                boolean isUnreachable = unreachable[y][x];

                cell.setToolTipText(
                        (isUnreachable) ? "Unreachable"
                                : (isBuffer) ? "Buffer Zone"
                                        : (val >= max) ? "Obstacle"
                                                : String.valueOf(val));

                assignColor(val, max, isBuffer, isUnreachable, cell);
                gridPanel.add(cell);
            }
        }

        constantview.add(gridPanel);
        constantview.pack();
        constantview.setVisible(true);
    }
    // End of GUI Methods //

    // Helper methods
    public double map(double valueCoord1, double endCoord1) { // Modified for our use-case

        if (Math.abs(endCoord1) < 1e-12) {
            throw new ArithmeticException("/ 0");
        }

        double ratio = (max) / (endCoord1);
        return ratio * (valueCoord1);
    }

    public static double map(double value, double istart, double istop, double ostart, double ostop) {
        return ostart + (ostop - ostart) * ((value - istart) / (istop - istart));
    }
}