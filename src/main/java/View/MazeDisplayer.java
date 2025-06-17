package View;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Custom control for displaying maze
 * This is a custom component you create yourself!
 */
public class MazeDisplayer extends Canvas {

    private int[][] maze;
    private int[] characterPosition = {0, 0};
    private double cellSize = 20.0;

    public MazeDisplayer() {
        super(400, 400); // Default size
        this.setFocusTraversable(true);
    }

    /**
     * Method to display a new maze
     */
    public void displayMaze(int[][] maze) {
        this.maze = maze;
        redraw();
    }

    /**
     * Update character position
     */
    public void updateCharacterPosition(int row, int col) {
        this.characterPosition[0] = row;
        this.characterPosition[1] = col;
        redraw();
    }

    /**
     * Draw the maze and character
     */
    private void redraw() {
        if (maze == null) return;

        GraphicsContext gc = getGraphicsContext2D();

        // Clear the canvas
        gc.clearRect(0, 0, getWidth(), getHeight());

        // Draw the maze
        for (int row = 0; row < maze.length; row++) {
            for (int col = 0; col < maze[row].length; col++) {
                double x = col * cellSize;
                double y = row * cellSize;

                if (maze[row][col] == 1) { // Wall
                    gc.setFill(Color.BLACK);
                } else { // Path
                    gc.setFill(Color.WHITE);
                }

                gc.fillRect(x, y, cellSize, cellSize);
                gc.setStroke(Color.GRAY);
                gc.strokeRect(x, y, cellSize, cellSize);
            }
        }

        // Draw the character
        double charX = characterPosition[1] * cellSize;
        double charY = characterPosition[0] * cellSize;
        gc.setFill(Color.RED);
        gc.fillOval(charX + 2, charY + 2, cellSize - 4, cellSize - 4);
    }
}