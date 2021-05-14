/**
 * Imports
 */
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class Visualizer extends Thread {

    /**
     * Instance variables
     */
    final Dimension BOARD_PANEL_SIZE = new Dimension(800, 800);
    final Dimension GAME_FRAME_SIZE = new Dimension(900, 900);
    JFrame gameFrame;
    BoardPanel boardPanel;
    Sudoku game;

    /**
     * Methods and classes
     */

    public Visualizer(Sudoku game){
        this.gameFrame = new JFrame("Chess Sudoku Visualizer");
        this.game = game;
        this.boardPanel = new BoardPanel();
        this.gameFrame.add(boardPanel, BorderLayout.CENTER);
        this.gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.gameFrame.setSize(GAME_FRAME_SIZE);
        this.gameFrame.setVisible(true);
        this.gameFrame.validate();
    }

    public class BoardPanel extends JPanel {

        ArrayList<Square> boardSquares = new ArrayList<Square>();

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

        public Square getSquare(int x, int y) {
            for (Square square : boardSquares) {
                if (square.x == x && square.y == y) return square;
            }
            return null;
        }

        public void refreshBoard() {
            for (Square square : boardSquares) {
                square.refreshSquare();
                validate();
            }
        }
    }

    public class Square extends JPanel{

        int x;
        int y;
        int number;

        public Square(int x, int y, int number){
            super(new GridBagLayout());
            this.x = x;
            this.y = y;
            this.number = game.grid[x][y];
            if(number != 0) add(new JLabel("" + number));
            this.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
            validate();
        }

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
