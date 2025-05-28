package myjava.sudokugui;
import java.util.Random;
import java.util.Arrays;

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
