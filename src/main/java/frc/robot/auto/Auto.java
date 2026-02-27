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

    private int s;
    private int width;
    private int max;
    private int length;
    private double[][] ConstantField; // [y][x]
    private double[][] MutableField;

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";

    public Auto() {
        s = 10; // Scaling factor (1 : s meters) <-- (real : grid)

        width = s * (int) Math.ceil(FieldConstants.FIELD_WIDTH);
        length = s * (int) Math.ceil(FieldConstants.FIELD_LENGTH);

        ConstantField = new double[length][width]; // Inches
        MutableField = new double[length][width]; // Inches
    }

    // For short-term testing in development
    public static void main(String[] args) {
        var myAuto = new Auto();
        myAuto.initializeConstantField();

        // myAuto.print(); <-- (Not nearly as clean, especially w/ large scaling)
        myAuto.display(); // very nice indeed
    }

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

    private static void assignColor(double a, int max, JButton cell) { // For UI
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

    public void display() { // Swing GUI
        // Constant Field
        JFrame constantview = new JFrame("Field View (Constant)");
        constantview.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(length, width));

        for (int y = 0; y < length; y++) {
            for (int x = 0; x < width; x++) {
                double val = ConstantField[y][x];
                JButton cell = new JButton(String.valueOf(val));
                cell.setToolTipText(String.valueOf(val));
                assignColor(val, max, cell);
                gridPanel.add(cell);
            }
        }

        constantview.add(gridPanel);
        constantview.pack();
        constantview.setVisible(true);
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

    public void initializeConstantField() {
        // Get forever no-go locations
        /*
         * Walls
         * Hub walls
         */
        for (int y = 0; y < length; y++) { // (Rn it just makes a radial from middle origin)
            for (int x = 0; x < width; x++) {
                // Set origin to center
                int rel_y = y - length / 2;
                int rel_x = x - width / 2;

                // Radial
                ConstantField[y][x] = (int) Math.sqrt(rel_x * rel_x + rel_y * rel_y);
                max = (int) Math.max(max, ConstantField[y][x]);
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

        // int rel_length = mapTo(, width, s, 0, length)
        // maybe a job for AI idk we gotta write this logic out this is wild
    }

    // We're gonna be using this a bit to map the field positions to grid positions
    // Maybe search online to see if a scalar field like this already exists or if
    // we can generate one
    public static int mapTo(double value, double lb1, double ub1, double lb2, double ub2) {
        return (int) (lb2 + (ub2 - lb2) * ((value - lb1) / (ub1 - lb1)));
    }
}
