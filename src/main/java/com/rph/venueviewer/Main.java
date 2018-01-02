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

public class Main {

    private boolean viewerShown = false;

    private JButton[][] buttonGrid;

    public static void main(String[] args) {
        new Main().runMain(args);
    }

    private void runMain(String[] args) {
        List<String> argsList = Collections.unmodifiableList(Arrays.asList(args));

        Queue<String> lineQueue = new LinkedList<>();

        int delay = 1000; //milliseconds
        ActionListener taskPerformer = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                processLine(lineQueue.poll());
            }
        };
        new Timer(delay, taskPerformer).start();

        try {
            BufferedReader in;
            int inputFilenameIndex = argsList.indexOf("-i");
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
            System.out.println("empty line");
            return;
        }
        if ("Venue".equals(tokens[0])) {
            int[] pair = parsePair(tokens[1]);
            if (!viewerShown) {
                showViewer(pair[0], pair[1]);
            }
        }
        if ("Seat".equals(tokens[0])) {
            int[] pair = parsePair(tokens[1]);
            setSeatColor(buttonGrid[pair[0]][pair[1]], 256 - 20 - Integer.parseInt(tokens[2]));
        }
        if ("SeatHold".equals(tokens[0])) {
            String state = tokens[1];
            int color = "Reserved".equals(state) ? 64 : "Expired".equals(state) ? 192 : 96;
            String[] seatStrings = Arrays.copyOfRange(tokens, 2, tokens.length);
            for (String seatString : seatStrings) {
                int[] pair = parsePair(seatString);
                setSeatColor(buttonGrid[pair[0]][pair[1]], color);
            }
        }
    }

    private void showViewer(int numRows, int numSeatsPerRow) {
        //Create and set up the window.
        JFrame frame = new JFrame("Ticket Service");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        buttonGrid = new JButton[numRows][];
        for (int rowNum = 0; rowNum < numRows; rowNum++) {
            JButton[] row = new JButton[numSeatsPerRow];
            for (int colNum = 0; colNum < numSeatsPerRow; colNum++) {
                row[colNum] = newSeatButton();
            }
            buttonGrid[rowNum] = row;
        }

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

    private static JButton newSeatButton() {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(30, 30));
        return button;
    }

    private static void setSeatColor(JButton seat, int n) {
        seat.setBackground(new Color(n, n, n));
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
