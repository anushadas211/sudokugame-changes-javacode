# sudokugame-changes-javacode
package myjava.sudokugui;
import java.util.Random;
import java.util.Arrays;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Timer;
import java.util.TimerTask;
public class SudokuGUI extends JFrame {
    private final JTextField[][] cells;
    private int[][] solution;
    private final SudokuGenerator generator;
    private JPanel gridPanel;
    private JComboBox<String> difficultyCombo;
    private JLabel scoreLabel;
    private int mistakes;
    private long startTime;
    private Timer timer;
    private JButton pauseButton;
    private boolean isPaused = false;
    private long pausedTime = 0;
    private long totalPausedDuration = 0;
    
    public SudokuGUI() {
        super("Sudoku Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 700);
        setLocationRelativeTo(null);

        generator = new SudokuGenerator();
        cells = new JTextField[9][9];
        initComponents();
        startNewGame();
        setVisible(true);
    }

    // Initialize the top controls and grid panel.
    private void initComponents() {
        
        pauseButton = new JButton("Pause");
        JPanel topPanel = new JPanel();
        String[] difficulties = {"Easy", "Medium", "Hard"};
        difficultyCombo = new JComboBox<>(difficulties);
        topPanel.add(new JLabel("Select Difficulty: "));
        topPanel.add(difficultyCombo);

        topPanel.add(pauseButton);
        JButton newGameButton = new JButton("New Game");
        pauseButton.addActionListener(e -> togglePause());
        newGameButton.addActionListener(e -> startNewGame());
        topPanel.add(newGameButton);

        JButton checkButton = new JButton("Check");
      
        checkButton.addActionListener(e -> checkBoard());
        topPanel.add(checkButton);

        scoreLabel = new JLabel("Score: 0");
        topPanel.add(scoreLabel);
        add(topPanel, BorderLayout.NORTH);

        gridPanel = new JPanel(new GridLayout(9, 9));
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                JTextField cell = new JTextField();
                cell.setHorizontalAlignment(JTextField.CENTER);
                cell.setFont(new Font("Arial", Font.BOLD, 20));
                // Allow only digits 1–9.
                cell.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyTyped(KeyEvent e) {
                        char c = e.getKeyChar();
                        if (!Character.isDigit(c) || c == '0') {
                            e.consume();
                        }
                    }
                });
                cells[i][j] = cell;
                gridPanel.add(cell);
            }
        }
        add(gridPanel, BorderLayout.CENTER);
    }

    // Starts a new game: generates a new puzzle, resets timer and mistakes.
   private void startNewGame() {
    mistakes = 0;
    isPaused = false;
    totalPausedDuration = 0;
    if (timer != null) timer.cancel();
    startTime = System.currentTimeMillis();
    pauseButton.setText("Pause");
    startTime = System.currentTimeMillis();

        String difficulty = (String) difficultyCombo.getSelectedItem();
        int[][] puzzle = generator.generatePuzzle(difficulty);
        solution = generator.getSolution();

        // Fill the grid cells with the puzzle values.
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (puzzle[i][j] != 0) {
                    cells[i][j].setText(String.valueOf(puzzle[i][j]));
                    cells[i][j].setEditable(false);
                    cells[i][j].setBackground(Color.LIGHT_GRAY);
                } else {
                    cells[i][j].setText("");
                    cells[i][j].setEditable(true);
                    cells[i][j].setBackground(Color.WHITE);
                }
            }
        }
        // Create a timer to update the score every second.
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateScore();
            }
        }, 0, 1000);
    }
    // Checks the current board against the solution.
    // Highlights mistakes in pink and increases the mistakes count.
    // If the board is complete and correct, calculates and shows the final score.
    private void checkBoard() {
        boolean errorFound = false;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (cells[i][j].isEditable()) {
                    String text = cells[i][j].getText();
                    int num = 0;
                    try {
                        num = Integer.parseInt(text);
                    } catch (NumberFormatException ex) {
                        errorFound = true;
                        cells[i][j].setBackground(Color.PINK);
                        continue;
                    }
                    if (num != solution[i][j]) {
                        errorFound = true;
                        cells[i][j].setBackground(Color.PINK);
                    } else {
                        cells[i][j].setBackground(Color.WHITE);
                    }
                }
            }
        }
        if (errorFound) {
            mistakes++;
            JOptionPane.showMessageDialog(this, "There are mistakes in your solution.");
        } else {
            // Check if every cell is filled.
            boolean complete = true;
            for (int i = 0; i < 9 && complete; i++) {
                for (int j = 0; j < 9 && complete; j++) {
                    if (cells[i][j].getText().isEmpty()) {
                        complete = false;
                    }
                }
            }
            if (complete) {
                timer.cancel();
                long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
                // Calculate score: subtract elapsed time and a penalty for each mistake.
                int score = Math.max(1000 - (int) elapsedTime - (mistakes * 50), 0);
                JOptionPane.showMessageDialog(this, "Congratulations, you solved the puzzle!\nYour score: " + score);
            } else {
                JOptionPane.showMessageDialog(this, "So far, so good! Keep going.");
            }
        }
    }

    // Recalculates the score based on elapsed time and mistakes.
    private void updateScore() {
    if (isPaused) return;
    long elapsedTime = (System.currentTimeMillis() - startTime - totalPausedDuration) / 1000;
    int score = Math.max(1000 - (int) elapsedTime - (mistakes * 50), 0);
    scoreLabel.setText("Score: " + score);

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SudokuGUI());
    }

    private void togglePause() {
    if (isPaused) {
        // Resume the game
        isPaused = false;
        totalPausedDuration += System.currentTimeMillis() - pausedTime;
        pauseButton.setText("Pause");
        setCellsEditable(true);

        // Resume score update
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateScore();
            }
        }, 0, 1000);

    } else {
        // Pause the game
        isPaused = true;
        pausedTime = System.currentTimeMillis();
        pauseButton.setText("Resume");
        setCellsEditable(false);

        if (timer != null) {
            timer.cancel();
        }
    }
}

   private void setCellsEditable(boolean editable) {
    for (int i = 0; i < 9; i++) {
        for (int j = 0; j < 9; j++) {
            if (cells[i][j].isEditable()) {
                cells[i][j].setEditable(editable);
                cells[i][j].setBackground(editable ? Color.WHITE : Color.GRAY);
            }
        }
    }
   }
}


public class SudokuGenerator {
    private final int[][] board;
    private final int[][] solution;
    private final Random random = new Random();

    public SudokuGenerator() {
        board = new int[9][9];
        solution = new int[9][9];
    }

    // Returns the complete solution for verification.
    public int[][] getSolution() {
        return solution;
    }

    // Generates a puzzle based on difficulty level.
    // Difficulty string should be "easy", "medium", or "hard".
    public int[][] generatePuzzle(String difficulty) {
        // Generate a complete solution.
        fillBoard(0, 0, board);
        // Copy the full solution.
        for (int i = 0; i < 9; i++) {
            solution[i] = Arrays.copyOf(board[i], 9);
        }
        // Determine how many cells to remove.
        int removals = 0;
        removals = switch (difficulty.toLowerCase()) {
            case "easy" -> 30;
            case "medium" -> 40;
            case "hard" -> 50;
            default -> 30;
        };
        // Remove cells randomly by setting them to 0.
        while (removals > 0) {
            int row = random.nextInt(9);
            int col = random.nextInt(9);
            if (board[row][col] != 0) {
                board[row][col] = 0;
                removals--;
            }
        }
        return board;
    }

    // Backtracking method to fill the board.
    private boolean fillBoard(int row, int col, int[][] board) {
        if (row == 9) {
            return true; // Completed all rows.
        }
        int nextRow = (col == 8) ? row + 1 : row;
        int nextCol = (col + 1) % 9;
        // Create an array of numbers 1-9 and shuffle them.
        int[] numbers = new int[9];
        for (int i = 0; i < 9; i++) {
            numbers[i] = i + 1;
        }
        shuffleArray(numbers);
        for (int num : numbers) {
            if (isSafe(board, row, col, num)) {
                board[row][col] = num;
                if (fillBoard(nextRow, nextCol, board)) {
                    return true;
                }
                board[row][col] = 0; // Backtrack.
            }
        }
        return false;
    }

    // Helper method: shuffles the array.
    private void shuffleArray(int[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }

    // Checks if placing num in board[row][col] is valid.
    private boolean isSafe(int[][] board, int row, int col, int num) {
        // Check row and column.
        for (int i = 0; i < 9; i++) {
            if (board[row][i] == num || board[i][col] == num)
                return false;
        }
        // Check the 3x3 subgrid.
        int startRow = row - row % 3;
        int startCol = col - col % 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[startRow + i][startCol + j] == num)
                    return false;
            }
        }
        return true;
    }
}


    
