package Visualisation;

/**
 * Imports
 */
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import SudokuSolver.Sudoku;

//This class is made to visualize the solutions when we tried finding all of them
public class MultipleVisualizer extends Visualizer{

    /**
     * Extra instance variables
     */

    JButton prevPuzzle;
    JButton nextPuzzle;
    JLabel currentPuzzle;
    int currentPuzzleIndex = 0;

    /**
     * Extra methods
     */

    //Constructor that calls the parent constructor and adds the next and previous buttons to go through the puzzles
    //It also adds the total number of solutions as well as the time it took to solve for them at the top of the screen
    public MultipleVisualizer(Sudoku game, long timeToSolve) {

        super(game);

        int numOfSolutions = game.solutions.size();

        currentPuzzle = new JLabel("    Current solution: " + (currentPuzzleIndex+1));

        prevPuzzle = new JButton("Previous solution");
        prevPuzzle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(MultipleVisualizer.this.game.solutions.size() == 0){
                    JOptionPane.showMessageDialog(null, "The sudoku puzzle you entered has no solutions.", "No solutions", JOptionPane.INFORMATION_MESSAGE);
                }
                else{
                    currentPuzzleIndex--;
                    if(currentPuzzleIndex < 0) currentPuzzleIndex = numOfSolutions - 1;
                    currentPuzzle.setText("    Current solution: " + (currentPuzzleIndex+1));
                    MultipleVisualizer.this.game.grid = MultipleVisualizer.this.game.solutions.get(currentPuzzleIndex).grid;
                    boardPanel.refreshBoard();
                    gameFrame.validate();
                }
            }
        });

        nextPuzzle = new JButton(("Next solution"));
        nextPuzzle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(MultipleVisualizer.this.game.solutions.size() == 0){
                    JOptionPane.showMessageDialog(null, "The sudoku puzzle you entered has no solutions.", "No solutions", JOptionPane.INFORMATION_MESSAGE);
                }
                else{
                    currentPuzzleIndex = (currentPuzzleIndex+1) % MultipleVisualizer.this.game.solutions.size();
                    currentPuzzle.setText("    Current solution: " + (currentPuzzleIndex+1));
                    MultipleVisualizer.this.game.grid = MultipleVisualizer.this.game.solutions.get(currentPuzzleIndex).grid;
                    boardPanel.refreshBoard();
                    gameFrame.validate();
                }
            }
        });

        JPanel topPanel = new JPanel(new GridLayout());
        topPanel.add(new JLabel("   Time to solve: " + timeToSolve + " ms"));
        topPanel.add(new JLabel("   Total number of solutions: " + numOfSolutions));
        topPanel.add(currentPuzzle);

        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(prevPuzzle);
        bottomPanel.add(nextPuzzle);

        gameFrame.add(topPanel, BorderLayout.NORTH);
        gameFrame.add(bottomPanel, BorderLayout.SOUTH);
        gameFrame.validate();
    }
}
