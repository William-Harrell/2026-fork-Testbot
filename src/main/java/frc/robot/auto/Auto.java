package frc.robot.auto;

// For UI in testing
import javax.swing.*;

import java.awt.Color;
import java.awt.GridLayout;
// End of UI/testing libs

import frc.robot.util.constants.FieldConstants;

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

    private double[][] ConstantField; // Our grid of obstacles that never move
    private double[][] MutableField; // Our grid of obstacles that constantly updates

    // Static vars
    public static final String ANSI_RESET = "\u001B[0m"; // Apply the color white in Java terminal
    public static final String ANSI_RED = "\u001B[31m"; // Apply the color red in Java terminal
    public static final String ANSI_GREEN = "\u001B[32m"; // Apply the color green in Java terminal
    public static final String ANSI_YELLOW = "\u001B[33m"; // Apply the color yellow in Java terminal

    public static final int FIELD_SIZE_RATIO = (int) Math
            .round(FieldConstants.FIELD_LENGTH / FieldConstants.FIELD_WIDTH); // The field's length:width ratio so we can
                                                                              // have "1x1" cells

    // The template for a field object (it's x, y, length, and width)
    // idk the origin yet
    // rn we're assuming everything's a rectangle so orientation isn't big right
    public static record FieldObject(double x, double y, double length, double width) {
    }

    // Constructor
    public Auto(int max_heuristic) {
        s = 5; // Scaling factor (1 : s meters) <-- (real : grid)

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
        myAuto.initializeConstantField();

        // myAuto.print(); <-- (Not nearly as clean, especially w/ large scaling)
        myAuto.display(); // very nice indeed
    }

    // Create field method
    public void initializeConstantField() {
        for (FieldObject obj : FieldConstants.nogos) {
            // (81, 123) | Origin is bottom left
            int x = (int) Math.round(map(obj.x(), 0, FieldConstants.FIELD_LENGTH, 0, width));
            int y = (int) Math.round(map(obj.y(), 0, FieldConstants.FIELD_WIDTH, 0, length));
            int w = (int) Math.round(map(obj.width(), 0, FieldConstants.FIELD_WIDTH, 0, length));
            int l = (int) Math.round(map(obj.length(), 0, FieldConstants.FIELD_LENGTH, 0, width));

            int xStart = Math.max(x, 0);
            int yStart = Math.max(y, 0);
            int xEnd = Math.min(x + l, width);
            int yEnd = Math.min(y + w, length);

            if (xStart >= width || yStart >= length || xEnd <= 0 || yEnd <= 0) {
                System.out.println("Fully out of bounds, skipping: " + obj);
                continue;
            }

            for (int i = yStart; i < yEnd; i++) {
                for (int j = xStart; j < xEnd; j++) {
                    ConstantField[i][j] = max;
                }
            }
        }

        // No more radial!
        applyDistanceHeuristic(ConstantField, FieldConstants.CENTER_X, FieldConstants.CENTER_Y);
    }

    /**
     * Fills the given field grid with heuristic values based on true navigable
     * distance to the target position (tx, ty) in meters, routing AROUND obstacles.
     *
     * Uses BFS (wave propagation) from the target cell outward. Cells already
     * set to max (obstacles) are treated as walls and block propagation.
     * Cells that are unreachable stay at max.
     *
     * @param field the grid to write into (must have obstacles already drawn)
     * @param tx    target X in meters (along field length)
     * @param ty    target Y in meters (along field width)
     */
    public void applyDistanceHeuristic(double[][] field, double tx, double ty) {
        int targetX = (int) Math.round(map(tx, 0, FieldConstants.FIELD_LENGTH, 0, width - 1));
        int targetY = (int) Math.round(map(ty, 0, FieldConstants.FIELD_WIDTH, 0, length - 1));

        // BFS hop count grid — -1 means unvisited
        int[][] hops = new int[length][width];
        for (int[] row : hops)
            java.util.Arrays.fill(row, -1);

        java.util.Queue<int[]> queue = new java.util.LinkedList<>();

        // Seed from target (only if target itself isn't an obstacle)
        if (field[targetY][targetX] < max) {
            hops[targetY][targetX] = 0;
            queue.add(new int[] { targetX, targetY });
        }

        // 8-directional neighbors (allows diagonal pathing like a real robot)
        int[] dx = { -1, 0, 1, -1, 1, -1, 0, 1 };
        int[] dy = { -1, -1, -1, 0, 0, 1, 1, 1 };

        int maxHops = 0;

        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            int cx = cur[0], cy = cur[1];

            for (int d = 0; d < 8; d++) {
                int nx = cx + dx[d];
                int ny = cy + dy[d];

                // Skip out-of-bounds
                if (nx < 0 || nx >= width || ny < 0 || ny >= length)
                    continue;

                // Skip obstacles and already-visited cells
                if (field[ny][nx] >= max || hops[ny][nx] != -1)
                    continue;

                hops[ny][nx] = hops[cy][cx] + 1;
                if (hops[ny][nx] > maxHops)
                    maxHops = hops[ny][nx];
                queue.add(new int[] { nx, ny });
            }
        }

        // Write normalized values back — obstacles and unreachable cells stay at max
        for (int y = 0; y < length; y++) {
            for (int x = 0; x < width; x++) {
                if (field[y][x] >= max)
                    continue; // preserve obstacles
                if (hops[y][x] == -1) {
                    field[y][x] = max; // unreachable
                } else {
                    field[y][x] = (maxHops > 0) ? map(hops[y][x], 0, maxHops, 0, max) : 0;
                }
            }
        }
    }

    // FIELD DISPLAY METHODS
    // Text methods //
    private static String assignColor(double a, int max) { // For text
        if (a > 0.9 * max) {
            return ANSI_RED + a + ANSI_RESET;
        } else if (a > 0.5 * max) {
            return ANSI_YELLOW + a + ANSI_RESET;
        } else if (a > 0.25 * max) {
            return ANSI_GREEN + a + ANSI_RESET;
        } else {
            return ANSI_RESET + a + ANSI_RESET;
        }
    }

    public void print() {
        // Print field
        System.out.println("\n");
        System.out.print(" | ");

        for (int i = 0; i < length; i++) {
            for (int j = 0; j < width; j++) {
                System.out.print(assignColor(ConstantField[i][j], max) + " | ");
            }

            if (i < length - 1) {
                System.out.print("\n | ");
            } else {
                System.out.print("\n");
            }
        }
    }
    // End of text methods

    // GUI Methods
    private static void assignColor(double a, int max, JButton cell) { // For GUI
        if (a == 1 * max) {
            cell.setBackground(new Color(Color.HSBtoRGB(0, 0, (float) ((a/max)))));
        } else if (a > 0.8 * max) {
            cell.setBackground(new Color(Color.HSBtoRGB(0, .99f, (float) ((a/max)))));
        } else if (a > 0.65 * max) {
            cell.setBackground(new Color(Color.HSBtoRGB(33f, .99f, (float) ((a/max)))));
        } else if (a > 0.5 * max) {
            cell.setBackground(new Color(Color.HSBtoRGB(55f, .99f, (float) ((a/max)))));
        } else if (a > 0.35 * max) {
            cell.setBackground(new Color(Color.HSBtoRGB(115f, .99f, (float) ((a/max)))));
        } else if (a > 0.15 * max) {
            cell.setBackground(new Color(Color.HSBtoRGB(115f, 0f, (float) ((a/max)))));
        } else {
            cell.setBackground(new Color(Color.HSBtoRGB(115f, 0f, (float) ((a/max)))));
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
                cell.setToolTipText(String.valueOf(val));
                assignColor(val, max, cell);
                gridPanel.add(cell);
            }
        }

        constantview.add(gridPanel);
        constantview.pack();
        constantview.setVisible(true);
    }
    // End of GUI Methods //
    // END OF FIELD DISPLAY METHODS

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
