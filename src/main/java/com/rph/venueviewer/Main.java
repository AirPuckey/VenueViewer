package com.rph.venueviewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


/**
 * Ticket service viewer. It takes the output from the ticket service implementation
 * as input, and displays it in a graphical user interface.
 */
public class Main {

    private static final int DEFAULT_SPEED = 200;

    private static Color AVAILABLE = new Color(100, 100, 100);
    private static Color HELD = new Color(150, 130, 130);
    private static Color EXPIRED = new Color(180, 205, 180);
    private static Color RESERVED = new Color(200, 200, 200);
    private static Color INVALID = new Color(200, 100, 100);

    private int numRows = 1;

    private int numSeatsPerRow = 1;

    private boolean viewerInitialized = false;

    private boolean buttonsInitialized = false;

    private JButton[][] buttonGrid;

    public static void main(String[] args) {
        new Main().runMain(args);
    }

    private void runMain(String[] args) {
        List<String> argsList = Collections.unmodifiableList(Arrays.asList(args));

        /*
         * TODO: make the argument parsing much more robust.
         */

        if (argsList.contains("-u") || argsList.contains("-usage") || argsList.contains("-Usage")) {
            System.err.println();
            System.err.println("usage: java -cp viewer.jar RunMe [ options ]");
            System.err.println("  where options include:");
            System.err.println("    -usage (this message)");
            System.err.println("    -inputFile fileName (default: standard input)");
            System.err.println("    -speed milliseconds (default: 200) (smaller numbers are faster");
            System.err.println();
            return;
        }

        int speed = DEFAULT_SPEED;
        int speedFlag = argsList.indexOf("-speed");
        if (speedFlag >= 0) {
            speed = Integer.parseInt(argsList.get(speedFlag + 1));
        }

        Queue<String> lineQueue = new LinkedList<>();

        ActionListener taskPerformer = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                processLine(lineQueue.poll());
            }
        };
        new Timer(speed, taskPerformer).start();

        try {
            BufferedReader in;
            int inputFilenameIndex = argsList.indexOf("-inputFile");
            if (inputFilenameIndex >= 0) {
                in = new BufferedReader(new FileReader(argsList.get(inputFilenameIndex + 1)));
            } else {
                in = new BufferedReader(new InputStreamReader(System.in));
            }
            String line;
            while ((line = in.readLine()) != null) {
                lineQueue.add(line);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private void processLine(String line) {
        if ((line == null) || "".equals(line)) {
            return;
        }
        System.out.println(line);
        String[] tokens = line.split(" ");
        if ((tokens.length <= 0) || (tokens.length == 1 && "".equals(tokens[0]))) {
            // empty line
            return;
        }

        /*
         * TODO: make input parsing more robust.
         */

        if ("Venue".equals(tokens[0])) {
            int[] pair = parsePair(tokens[1]);
            numRows = pair[0];
            numSeatsPerRow = pair[1];
        }
        if ("Seat".equals(tokens[0])) {
            if (!viewerInitialized) {
                initializeAllButtons(AVAILABLE);
                showViewer();
                viewerInitialized = true;
            }
            int[] pair = parsePair(tokens[1]);
            setSeatColor(buttonGrid[pair[0]][pair[1]], getGraduatedColor(numRows * numSeatsPerRow,
                                                                         Integer.parseInt(tokens[2])));
        }
        if ("SeatHold".equals(tokens[0])) {
            if (!viewerInitialized) {
                initializeAllButtons(AVAILABLE);
                showViewer();
                viewerInitialized = true;
            }
            String state = tokens[1];
            Color color
                    = "Available".equals(state) ? AVAILABLE
                    : "Held".equals(state) ? HELD
                    : "Reserved".equals(state) ? RESERVED
                    : "Expired".equals(state) ? EXPIRED
                    : INVALID;
            String[] seatStrings = Arrays.copyOfRange(tokens, 2, tokens.length);
            for (String seatString : seatStrings) {
                int[] pair = parsePair(seatString);
                setSeatColor(buttonGrid[pair[0]][pair[1]], color);
            }
        }
    }

    private Color getGraduatedColor(int range, int n) {
        double percent = (double) n / (double) range;
        n = 256 - ((int) (200 * percent) + 26);
        return new Color(n, n, n);
    }

    private void showViewer() {
        JFrame frame = new JFrame("Ticket Service");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel grid = new JPanel(new GridLayout(numRows, numSeatsPerRow, 5, 5));
        grid.setBorder(BorderFactory.createEmptyBorder(30, 30, 30 , 30));

        for (int rowNum = 0; rowNum < numRows; rowNum++) {
            for (int colNum = 0; colNum < numSeatsPerRow; colNum++) {
                grid.add(buttonGrid[rowNum][colNum]);
            }
        }

        frame.getContentPane().add(grid);
        frame.pack();
        frame.setVisible(true);
    }

    private void initializeAllButtons(Color color) {
        buttonGrid = new JButton[numRows][];
        for (int rowNum = 0; rowNum < numRows; rowNum++) {
            JButton[] row = new JButton[numSeatsPerRow];
            for (int colNum = 0; colNum < numSeatsPerRow; colNum++) {
                JButton button = newSeatButton();
                setSeatColor(button, color);
                row[colNum] = button;
            }
            buttonGrid[rowNum] = row;
        }
    }

    private static JButton newSeatButton() {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(30, 30));
        button.setBackground(new Color(200, 200, 200));
        return button;
    }

    private static void setSeatColor(JButton seat, Color color) {
        seat.setBackground(color);
        seat.setOpaque(true);
        seat.setBorderPainted(false);
    }

    private int[] parsePair(String token) {
        String[] tokens = token.split("x");
        int[] pair = new int[2];
        pair[0] = Integer.parseInt(tokens[0]);
        pair[1] = Integer.parseInt(tokens[1]);
        return pair;
    }
}
