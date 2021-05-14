package Visualisation;

/**
 * Imports
 */
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import SudokuSolver.Sudoku;

public abstract class Visualizer {

    /**
     * Instance variables
     */
    final Dimension BOARD_PANEL_SIZE = new Dimension(800, 800); //Dimension of the board panel
    final Dimension GAME_FRAME_SIZE = new Dimension(900, 900); //Dimension of the gameframe
    JFrame gameFrame; //The gameframe (window in which we are placing the board panel)
    BoardPanel boardPanel; //The board panel (Sudoku grid visualisation)
    Sudoku game; //The game we are basing the visualisation on

    /**
     * Methods and classes
     */

    //Public constructor that initializes the gameFrame and boardPanel
    public Visualizer(Sudoku game){
        this.gameFrame = new JFrame("Sudoku Visualizer");
        this.game = game;
        this.boardPanel = new BoardPanel();
        this.gameFrame.add(boardPanel, BorderLayout.CENTER);
        this.gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.gameFrame.setSize(GAME_FRAME_SIZE);
        this.gameFrame.setVisible(true);
        this.gameFrame.validate();
    }

    //Inner class that represents the board panel of the sudoku puzzle
    public class BoardPanel extends JPanel {

        //To store all the squares of the board panel
        ArrayList<Square> boardSquares = new ArrayList<Square>();

        //Constructor that initializes the board panel with the numbers located in the grid of game
        public BoardPanel() {

            super(new GridLayout(game.N, game.N));

            for (int i = 0; i < game.N; i++) {
                for (int j = 0; j < game.N; j++) {
                    Square square = new Square(i, j, game.grid[i][j]);
                    boardSquares.add(square);
                    add(square);
                }
            }
            setPreferredSize(BOARD_PANEL_SIZE);
            validate();
        }

        //To refresh the board if we changed the grid of game
        public void refreshBoard() {
            for (Square square : boardSquares) {
                square.refreshSquare();
                validate();
            }
        }
    }

    //Second inner class that represents 1 of the squares of the board panel
    public class Square extends JPanel{

        //To store the coordinates and value of a square
        int x;
        int y;
        int number;

        //Constructor that initializes a square according to the passed parameters
        public Square(int x, int y, int number){
            super(new GridBagLayout());
            this.x = x;
            this.y = y;
            this.number = game.grid[x][y];
            if(number != 0) add(new JLabel("" + number));
            this.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
            validate();
        }

        //Method that refreshes a square based on changes in the intial grid
        public void refreshSquare(){
            if(number != game.grid[x][y]) {
                this.removeAll();
                this.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
                number = game.grid[x][y];
                add(new JLabel("" + number));
                validate();
            }
        }
    }

}
