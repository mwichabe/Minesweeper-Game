import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Minesweeper extends JFrame{

    private JFrame frame;
    private JPanel panel;
    private JLabel timeLabel;
    private int rows, cols, mines;
    private Cell[][] cells;
    private int coveredCells;
    private long startTime;
    private int elapsedTime;

    private static final int BEGINNER_ROWS = 9;
    private static final int BEGINNER_COLS = 6;
    private static final int BEGINNER_MINES = 11;

    private static final int INTERMEDIATE_ROWS = 18;
    private static final int INTERMEDIATE_COLS = 12;
    private static final int INTERMEDIATE_MINES = 36;

    private static final int ADVANCED_ROWS = 26;
    private static final int ADVANCED_COLS = 21;
    private static final int ADVANCED_MINES = 92;

    public Minesweeper() {
        initialize();
        frame.add(new BoardPanel(this)); 
        timeLabel = new JLabel();
        frame.add(timeLabel, BorderLayout.NORTH);
        startTime = System.currentTimeMillis();
        updateTime();
    }

    public void initialize() {
        String[] options = {"Beginner", "Intermediate", "Advanced"};
        String difficulty = (String) JOptionPane.showInputDialog(frame,
                "Choose difficulty", "Difficulty Selection",
                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        setDifficulty(difficulty);

        frame = new JFrame("Minesweeper");
        new BoardPanel(this); 
        panel = new JPanel(new GridLayout(rows, cols));

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Cell cell = new Cell(i, j);
                cell.addMouseListener(new MyMouseAdapter());
                cells[i][j] = cell;
                panel.add(cell);
            }
        }

        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateTime();
            }
        }, 1000, 1000);

        frame.setSize(cols * 20, rows * 20);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        placeMines();
    }

    public void setDifficulty(String difficulty) {
        switch (difficulty) {
            case "Beginner":
                rows = BEGINNER_ROWS;
                cols = BEGINNER_COLS;
                mines = BEGINNER_MINES;
                break;
            case "Intermediate":
                rows = INTERMEDIATE_ROWS;
                cols = INTERMEDIATE_COLS;
                mines = INTERMEDIATE_MINES;
                break;
            case "Advanced":
                rows = ADVANCED_ROWS;
                cols = ADVANCED_COLS;
                mines = ADVANCED_MINES;
                break;
            default:
                throw new IllegalArgumentException("Invalid difficulty: " + difficulty);
        }

        cells = new Cell[rows][cols];
        coveredCells = rows * cols;
    }

    public void placeMines() {
        Random random = new Random();
        int placedMines = 0;

        while (placedMines < mines) {
            int x = random.nextInt(rows);
            int y = random.nextInt(cols);

            if (!cells[x][y].isMine()) {
                cells[x][y].setMine(true);
                placedMines++;
            }
        }

        calculateAdjacentMines();
    }

    public void calculateAdjacentMines() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (!cells[i][j].isMine()) {
                    int mines = countAdjacentMines(i, j);
                    cells[i][j].setAdjacentMines(mines);
                }
            }
        }
    }

    public int countAdjacentMines(int cellRow, int cellCol) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;

                int row = cellRow + i;
                int col = cellCol + j;

                if (isValidCell(row, col) && cells[row][col].isMine()) {
                    count++;
                }
            }
        }

        return count;
    }

    public boolean isValidCell(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    public void updateTime() {
        elapsedTime = (int) ((System.currentTimeMillis() - startTime) / 1000);
        timeLabel.setText("Time: " + elapsedTime);

        int maxTime;
        if (rows == BEGINNER_ROWS) maxTime = 60;
        else if (rows == INTERMEDIATE_ROWS) maxTime = 180;
        else maxTime = 660;

        if (elapsedTime >= maxTime) {
            endGame();
        }
    }

    public void endGame() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Cell cell = cells[i][j];
                if (cell.isMine() && !cell.getState().equals(CellState.MARKED_MINE)) {
                    cell.explode();
                }
            }
        }

        JOptionPane.showMessageDialog(frame, "Time's up! Game ended.");
        System.exit(0);
    }

    class Cell extends JPanel {

        int row;
        int col;

        boolean isMine;
        int adjacentMines;

        CellState state;

        Cell(int row, int col) {
            this.row = row;
            this.col = col;

            state = CellState.COVERED;
        }

        public void setMine(boolean yes) {
            isMine = yes;
        }

        public boolean isMine() {
            return isMine;
        }

        public void setAdjacentMines(int mines) {
            adjacentMines = mines;
        }

        public void draw() {
            setBorder(null);
            if (state == CellState.COVERED) {
                setBackground(Color.LIGHT_GRAY);
            } else if (isMine) {
                setBackground(Color.RED);
            } else {
                setBackground(Color.WHITE);
                switch (adjacentMines) {
                    case 1 -> setForeground(Color.BLUE);
                    case 2 -> setForeground(Color.GREEN);
                    default -> setForeground(Color.BLACK);
                }
                setToolTipText(adjacentMines > 0 ? String.valueOf(adjacentMines) : "");
}
        validate();
    }

    public CellState getState() {
        return state;
    }

    public void mouseClicked(MouseEvent e) {
        reveal();
    }

    public void reveal() {
        if (state == CellState.MARKED_MINE) return;

        if (state == CellState.COVERED) {
            state = CellState.REVEALED;
            coveredCells--;

            if (adjacentMines > 0) {
                return;
            }

            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i == 0 && j == 0) continue;

                    int row = this.row + i;
                    int col = this.col + j;

                    if (isValidCell(row, col)) {
                        cells[row][col].reveal();
                    }
                }
            }
        }

        if (isMine()) {
            explode();
            endGame();
        }

        if (coveredCells == mines) {
            endGame();
            JOptionPane.showMessageDialog(frame, "Congratulations! You won!");
        }

        draw();
        validate();
    }

    public void markMine() {
        state = state == CellState.MARKED_MINE ? CellState.COVERED : CellState.MARKED_MINE;
        draw();
        validate();
    }

    public void explode() {
        state = CellState.EXPLODED;

        JLabel mine = new JLabel(new ImageIcon("mine.png"));
        add(mine);

        playExplosionSound();

        draw();
        validate();
    }

    private void playExplosionSound() {
        try {
            File soundFile = new File("src/explosion.wav");
            if (!soundFile.exists()) {
                System.out.println("Sound file not found!");
                return;
            }

            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}

enum CellState {
    COVERED, REVEALED, MARKED_MINE, EXPLODED
}

class MyMouseAdapter extends MouseAdapter {

    @Override
    public void mouseClicked(MouseEvent e) {
        Cell cell = (Cell) e.getSource();

        if (e.getButton() == MouseEvent.BUTTON1) {
            cell.reveal();
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            cell.markMine();
        }
    }
}

public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> new Minesweeper());
}
}

