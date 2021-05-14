package Visualisation;

/**
 * Imports
 */

import javax.swing.*;
import SudokuSolver.Sudoku;

import java.awt.*;

//This class is made to visualize the solutions when we tried finding only one of them
public class SingleVisualizer extends Visualizer{

    /**
     * Extra methods
     */

    public SingleVisualizer(Sudoku game, long timeToSolve){

        super(game);
        gameFrame.add(new JLabel("    Time to solve: " + timeToSolve + " ms"), BorderLayout.NORTH);

        if(hasEmptySpots(game.grid)){
            JOptionPane.showMessageDialog(null, "The sudoku puzzle you entered has no solutions.", "No solutions", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    //Method that checks if there are empty spots in the grid (unsolved then)
    private static boolean hasEmptySpots(int[][] grid){

        for(int i = 0; i < grid.length; i++){
            for(int j = 0; j < grid[0].length; j++){
                if(grid[i][j] == 0){
                    return true;
                }
            }
        }

        return false;
    }
}
