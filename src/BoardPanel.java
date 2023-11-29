import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BoardPanel extends JPanel {

    private int rows;
    private int cols;
    private int mines;

    private boolean[][] mineLocations;
    private boolean[][] revealed;
    private boolean[][] markedAsMine;
    private Minesweeper minesweeper;

    public BoardPanel(Minesweeper minesweeper) {
        this.minesweeper = minesweeper;
        addMouseListener(new MinesweeperMouseListener());
        setPreferredSize(new Dimension(400, 400));  // Set default window size
        initializeBoard(10, 10, 10);
    }

    public void initializeBoard(int rows, int cols, int mines) {
        this.rows = rows;
        this.cols = cols;
        this.mines = mines;

        mineLocations = new boolean[rows][cols];
        revealed = new boolean[rows][cols];
        markedAsMine = new boolean[rows][cols];

        // Initialize board with mines
        initializeMines();

    }

    private void initializeMines() {
        for (int i = 0; i < mines; i++) {
            int row, col;
            do {
                row = (int) (Math.random() * rows);
                col = (int) (Math.random() * cols);
            } while (mineLocations[row][col]);
            mineLocations[row][col] = true;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw the board
        int cellSize = 20;  // Adjust as needed
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (revealed[i][j]) {
                    // Draw revealed cell
                    g.setColor(Color.LIGHT_GRAY);
                    g.fillRect(j * cellSize, i * cellSize, cellSize, cellSize);
                    g.setColor(Color.BLACK);
                    g.drawRect(j * cellSize, i * cellSize, cellSize, cellSize);

                    // Draw mine if present
                    if (mineLocations[i][j]) {
                        g.setColor(Color.RED);
                        g.fillOval(j * cellSize, i * cellSize, cellSize, cellSize);
                    }
                } else {
                    // Draw unrevealed cell
                    g.setColor(Color.GRAY);
                    g.fillRect(j * cellSize, i * cellSize, cellSize, cellSize);
                    g.setColor(Color.BLACK);
                    g.drawRect(j * cellSize, i * cellSize, cellSize, cellSize);

                    // Draw flag if marked as mine
                    if (markedAsMine[i][j]) {
                        g.setColor(Color.BLUE);
                        g.fillRect(j * cellSize + cellSize / 4, i * cellSize + cellSize / 4, cellSize / 2, cellSize / 2);
                    }
                }
            }
        }
    }

   // Modify the MinesweeperMouseListener class inside the BoardPanel class
private class MinesweeperMouseListener extends MouseAdapter {
    @Override
    public void mousePressed(MouseEvent e) {
        int row = e.getY() / 20;  // Adjust based on cell size
        int col = e.getX() / 20;  // Adjust based on cell size

        // Handle left mouse click
        if (SwingUtilities.isLeftMouseButton(e)) {
            revealCell(row, col);
        }
    }
}

// Modify the revealCell method
private void revealCell(int row, int col) {
    if (!revealed[row][col]) {
        revealed[row][col] = true;

        if (mineLocations[row][col]) {
            // Game Over logic
            System.out.println("Game Over!");
            minesweeper.endGame(); // Call endGame from Minesweeper
        } else {
            // Check if the game is won
            if (checkGameWon()) {
                minesweeper.endGame();
                // JOptionPane.showMessageDialog(minesweeper.this.frame, "Congratulations! You won!");
            }
        }

        repaint();
    }
}

// Add the following method to check if the game is won
private boolean checkGameWon() {
    int revealedCount = 0;
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            if (revealed[i][j] && !mineLocations[i][j]) {
                revealedCount++;
            }
        }
    }
    return revealedCount == (rows * cols - mines);
}
}
