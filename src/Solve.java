import SudokuSolver.Sudoku;
import Visualisation.MultipleVisualizer;
import Visualisation.SingleVisualizer;

//Main class that contains the main method run to solve puzzles
public class Solve {

    /**
     * Modify the following two fields as necessary before runtime
     */

    //The grid must be either 9x9, 16x16, 25x25, etc... (Have an integer square root)
    //Modify this grid to the sudoku puzzle of your choice (0 in place of empty spaces)
    final static int[][] gridToSolve = {
                                    {0, 0, 9, 8, 0, 0, 0, 0, 2},
                                    {0, 0, 0, 3, 1, 2, 8, 0, 0},
                                    {2, 0, 8, 0, 6, 0, 0, 0, 0},
                                    {0, 5, 0, 6, 2, 0, 0, 0, 1},
                                    {0, 0, 1, 4, 0, 8, 0, 2, 0},
                                    {6, 0, 0, 0, 9, 1, 0, 4, 0},
                                    {0, 0, 0, 0, 7, 6, 4, 0, 3},
                                    {0, 0, 7, 0, 8, 5, 0, 0, 0},
                                    {1, 0, 0, 9, 0, 0, 2, 0, 0}    };

    //Modify this boolean to indicate whether you want to find 1 or all solutions
    final static boolean allSolutions = true;

    /**
     * Run the following main method after modifying the above fields
     */

    //Main method that runs the solving algorithm and visualizes the solutions
    public static void main(String[] args){

        //Initializing a sudoku object with the required parameters
        int SIZE = (int) Math.sqrt(gridToSolve.length);
        Sudoku sudoku = new Sudoku(SIZE);
        sudoku.grid = gridToSolve;

        //Solving and recording the time elapsed
        long start = System.currentTimeMillis();
        sudoku.solve(allSolutions);
        long end = System.currentTimeMillis();

        //Visualizing the result
        if(allSolutions){
            MultipleVisualizer visualizer = new MultipleVisualizer(sudoku, end - start);
        }
        else{
            SingleVisualizer visualizer = new SingleVisualizer(sudoku, end - start);
        }
    }
}
