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
            .ceil(FieldConstants.FIELD_LENGTH / FieldConstants.FIELD_WIDTH); // The field's length:width ratio so we can
                                                                             // have "1x1" cells

    // The template for a field object (it's x, y, length, and width)
    // idk the origin yet
    // rn we're assuming everything's a rectangle so orientation isn't big right
    public static record FieldObject(double x, double y, double length, double width) {
    }

    // Maybe Rhys can help fill these out idk
    public static FieldObject[] nogos = {
            new FieldObject(0, 0, 0, 0),
    };

    // Constructor
    public Auto(int max_heuristic) {
        s = 5; // Scaling factor (1 : s meters) <-- (real : grid)

        width = 2 * s * (int) Math.floor(FieldConstants.FIELD_WIDTH / 2) + 1; // (make them odd so it's centered)
        length = 2 * s * (int) Math.floor(FieldConstants.FIELD_LENGTH / 2) + 1;

        width *= FIELD_SIZE_RATIO; // For 1 x ~1 cells

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
        int rel_max = (int) Math.sqrt(((width-1)/2) * ((width-1)/2) + ((length-1)/2) * ((length-1)/2));

        for (int y = 0; y < length; y++) {
            for (int x = 0; x < width; x++) {
                int rel_y = y - length / 2;
                int rel_x = x - width / 2;

                // Radial
                ConstantField[y][x] = (int) map(Math.sqrt(rel_x * rel_x + rel_y * rel_y), rel_max);
            }
        }

        // Walls
        int boundary = 2;

        for (int y = 0; y < length; y++) {
            for (int x = 0; x < width; x++) {
                if (x <= boundary || y <= boundary || y >= length - (1 + boundary) || x >= width - (1 + boundary)) {
                    ConstantField[y][x] = max;
                }
            }
        }

        // draw the nogos
        // for (FieldObject obj: nogos) {
        // // draw it
        // /*
        // * Multiply the values by scaling factor
        // * loop thru
        // */

        // int x = s * (int) obj.x(), y = s * (int) obj.y;

        // for (int i = x)
        // }

        /**
         * So, when drawing obstacles:
         * - real-world [x, y, height, width] times scaling factor for grid dimensions
         * 
         */
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
            cell.setBackground(Color.BLACK);
        } else if (a > 0.8 * max) {
            cell.setBackground(Color.RED);
        } else if (a > 0.65 * max) {
            cell.setBackground(Color.ORANGE);
        } else if (a > 0.5 * max) {
            cell.setBackground(Color.YELLOW);
        } else if (a > 0.35 * max) {
            cell.setBackground(Color.GREEN);
        } else if (a > 0.15 * max) {
            cell.setBackground(Color.GRAY);
        } else {
            cell.setBackground(Color.WHITE);
        }
    }

    public void display() {
        JFrame constantview = new JFrame("Field View (Constant)");
        constantview.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(length, width / FIELD_SIZE_RATIO));

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
}
