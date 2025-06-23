package View;

import algorithms.search.AState;
import algorithms.search.Solution;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.layout.AnchorPane;
import java.util.ArrayList;
import java.util.List;

/**
 * MazeDisplayer with full support for rendering maze, player, goal,
 * zooming, keyboard navigation, and optional solution display.
 */
public class MazeDisplayer extends Canvas {

    // Maze data and player/goal positions
    private int[][] maze;
    private int[] characterPosition = {0, 0};
    private int[] goalPosition = {0, 0};

    // Cell size and zoom configuration
    private double cellWidth = 20.0;
    private double cellHeight = 20.0;
    private double zoomFactor = 1.0;
    private final double zoomStep = 0.1;
    private final double maxZoom = 3.0;
    private final double minZoom = 0.5;

    // Solution display
    private Solution solution;
    private ArrayList<AState> solutionPath;
    private boolean showSolution = false;

    // Images for game elements
    private Image heroImage;
    private Image wallImage;
    private Image goalImage;
    private Image solutionImage;

    // Parent layout and callback for win
    private AnchorPane parentPane;
    private Runnable onWinCallback;

    public MazeDisplayer() {
        super();
        this.setFocusTraversable(true);
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.TRANSPARENT);
        initialize();
        loadImages();
    }

    // Set up listeners and controls
    private void initialize() {
        this.setOnScroll(this::handleZoomScroll);
        this.setOnKeyPressed(this::handleKeyPress);

        parentProperty().addListener((obs, oldParent, newParent) -> {
            if (newParent instanceof AnchorPane) {
                this.parentPane = (AnchorPane) newParent;
                setupParentListeners();
            }
        });
        enableMouseDragMovement();

    }

    public void clearSolutionPath() {
        this.solutionPath = null;
        this.showSolution = false;
        redraw();
    }
    // Rescale canvas when parent size changes
    private void setupParentListeners() {
        if (parentPane != null) {
            parentPane.widthProperty().addListener((obs, oldWidth, newWidth) -> {
                if (maze != null) {
                    updateCanvasSize();
                    redraw();
                }
            });

            parentPane.heightProperty().addListener((obs, oldHeight, newHeight) -> {
                if (maze != null) {
                    updateCanvasSize();
                    redraw();
                }
            });
        }
    }
    // Enables moving the character by clicking and dragging the mouse
    public void enableMouseDragMovement() {
        this.setOnMouseReleased(event -> {
            if (maze == null) return;

            double mouseX = event.getX();
            double mouseY = event.getY();

            // Calculate the cell coordinates based on mouse position and zoom
            int targetCol = (int)(mouseX / (cellWidth * zoomFactor));
            int targetRow = (int)(mouseY / (cellHeight * zoomFactor));

            // Boundary check: make sure the target is inside the maze
            if (targetRow < 0 || targetCol < 0 ||
                    targetRow >= maze.length || targetCol >= maze[0].length)
                return;

            // Allow movement only to an adjacent cell that is not a wall
            int currentRow = characterPosition[0];
            int currentCol = characterPosition[1];
            boolean isAdjacent = Math.abs(targetRow - currentRow) + Math.abs(targetCol - currentCol) == 1;

            if (isAdjacent && maze[targetRow][targetCol] == 0) {
                updateCharacterPosition(targetRow, targetCol);
            }
        });
    }


    // Load images from resources
    private void loadImages() {
        try {
            heroImage = loadImageSafely("/images/hero.png");
            wallImage = loadImageSafely("/images/wall.png");
            goalImage = loadImageSafely("/images/goal.png");
            solutionImage = loadImageSafely("/images/solution.png");
        } catch (Exception e) {
            System.out.println("Could not load images, using default colors");
        }
    }

    // Try to load image or return null
    private Image loadImageSafely(String path) {
        try {
            return new Image(getClass().getResourceAsStream(path));
        } catch (Exception e) {
            return null;
        }
    }

    // Set maze and prepare display
    public void displayMaze(int[][] maze) {
        this.maze = maze;
        this.showSolution = false;

        if (maze != null && maze.length > 0) {
            setGoalPosition(maze.length - 1, maze[0].length - 1);
            updateCanvasSize();
            redraw();
        }
    }

    // Recalculate canvas size based on parent
    private void updateCanvasSize() {
        if (maze == null) return;

        double availableWidth = parentPane != null ? parentPane.getWidth() * 0.9 : 800;
        double availableHeight = parentPane != null ? parentPane.getHeight() * 0.9 : 600;

        double maxCellWidth = availableWidth / maze[0].length;
        double maxCellHeight = availableHeight / maze.length;

        double cellSize = Math.min(maxCellWidth, maxCellHeight);
        this.cellWidth = Math.max(cellSize, 10);
        this.cellHeight = this.cellWidth;

        double canvasWidth = maze[0].length * cellWidth;
        double canvasHeight = maze.length * cellHeight;

        this.setWidth(canvasWidth);
        this.setHeight(canvasHeight);

        // Center canvas
        if (parentPane != null) {
            double centerX = (parentPane.getWidth() - canvasWidth) / 2;
            double centerY = (parentPane.getHeight() - canvasHeight) / 2;
            this.setLayoutX(centerX);
            this.setLayoutY(centerY);
        }
    }

    // Move player
    public void updateCharacterPosition(int row, int col) {
        this.characterPosition[0] = row;
        this.characterPosition[1] = col;
        checkWinCondition();
        redraw();
    }

    // Win condition check
    private void checkWinCondition() {
        System.out.println("Checking win: char=(" + characterPosition[0] + "," + characterPosition[1] +
                ") goal=(" + goalPosition[0] + "," + goalPosition[1] + ")");

        if (characterPosition[0] == goalPosition[0] && characterPosition[1] == goalPosition[1]) {
            System.out.println("===> WIN DETECTED!");
            if (onWinCallback != null) {
                onWinCallback.run();
            } else {
                System.out.println("onWinCallback is null!");
            }
        }
    }


    public void setOnWinCallback(Runnable callback) {
        this.onWinCallback = callback;
    }

    public void setGoalPosition(int row, int col) {
        this.goalPosition[0] = row;
        this.goalPosition[1] = col;
        redraw();
    }

    public void displaySolutionPath(List<AState> solutionPath) {
        this.solutionPath = new ArrayList<>(solutionPath);
        this.showSolution = true;
        redraw();
    }

    public void toggleSolutionDisplay() {
        this.showSolution = !this.showSolution;
        redraw();
    }

    // Handle zoom via Ctrl + mouse wheel
    private void handleZoomScroll(ScrollEvent event) {
        if (event.isControlDown() && maze != null) {
            double oldZoom = zoomFactor;

            if (event.getDeltaY() > 0 && zoomFactor < maxZoom) {
                zoomFactor = Math.min(zoomFactor + zoomStep, maxZoom);
            } else if (event.getDeltaY() < 0 && zoomFactor > minZoom) {
                zoomFactor = Math.max(zoomFactor - zoomStep, minZoom);
            }

            if (oldZoom != zoomFactor) {
                redraw();
            }

            event.consume();
        }
    }

    // Handle keyboard movement
    private void handleKeyPress(KeyEvent event) {
        if (maze == null) return;

        int row = characterPosition[0];
        int col = characterPosition[1];
        int newRow = row, newCol = col;

        KeyCode code = event.getCode();
        switch (code) {
            case NUMPAD8: newRow = row - 1; break;
            case NUMPAD2: newRow = row + 1; break;
            case NUMPAD4: newCol = col - 1; break;
            case NUMPAD6: newCol = col + 1; break;
            case NUMPAD7: newRow = row - 1; newCol = col - 1; break;
            case NUMPAD9: newRow = row - 1; newCol = col + 1; break;
            case NUMPAD1: newRow = row + 1; newCol = col - 1; break;
            case NUMPAD3: newRow = row + 1; newCol = col + 1; break;
            default: return;
        }

        // Check movement legality
        if (newRow >= 0 && newRow < maze.length && newCol >= 0 && newCol < maze[0].length && maze[newRow][newCol] == 0) {
            updateCharacterPosition(newRow, newCol);
        }

        event.consume();
    }

    // Main drawing logic
    private void redraw() {
        if (maze == null) return;

        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        gc.save();
        gc.scale(zoomFactor, zoomFactor);

        double zoomedCellWidth = cellWidth / zoomFactor;
        double zoomedCellHeight = cellHeight / zoomFactor;

        // Draw maze cells
        for (int row = 0; row < maze.length; row++) {
            for (int col = 0; col < maze[row].length; col++) {
                double x = col * zoomedCellWidth;
                double y = row * zoomedCellHeight;
                drawCell(gc, row, col, x, y, zoomedCellWidth, zoomedCellHeight);
            }
        }

        // Draw path if visible
        if (showSolution && solutionPath != null) {
            drawSolutionPath(gc, zoomedCellWidth, zoomedCellHeight);
        }

        drawGoal(gc, zoomedCellWidth, zoomedCellHeight);
        drawCharacter(gc, zoomedCellWidth, zoomedCellHeight);

        gc.restore();
    }

    // Draw single maze cell
    private void drawCell(GraphicsContext gc, int row, int col, double x, double y, double cellWidth, double cellHeight) {
        if (maze[row][col] == 1) {
            gc.setFill(Color.BLACK);
            gc.fillRect(x, y, cellWidth, cellHeight);
            gc.setStroke(Color.web("#FF4500"));
            gc.setLineWidth(1.5);
            gc.strokeRect(x, y, cellWidth, cellHeight);
        } else {
            gc.setFill(Color.web("#CC0000", 0.4));
            gc.fillRect(x, y, cellWidth, cellHeight);
            gc.setStroke(Color.web("#FF4500"));
            gc.setLineWidth(0.5);
            gc.strokeRect(x, y, cellWidth, cellHeight);
        }
    }

    // Draws the solution path on the maze using yellow dots or a custom image
    private void drawSolutionPath(GraphicsContext gc, double cellWidth, double cellHeight) {
        // Set the fill color to semi-transparent yellow
        gc.setFill(Color.YELLOW.deriveColor(0, 1, 1, 0.8));

        for (int i = 0; i < solutionPath.size(); i++) {
            AState state = solutionPath.get(i);
            if (state == null) continue;

            try {
                // Assume that AState is a MazeState and has a getPosition() method via reflection
                algorithms.mazeGenerators.Position pos =
                        (algorithms.mazeGenerators.Position) state.getClass().getMethod("getPosition").invoke(state);

                int row = pos.getRowIndex();
                int col = pos.getColumnIndex();

                // Ensure the position is within bounds and not a wall
                if (row >= 0 && row < maze.length &&
                        col >= 0 && col < maze[0].length &&
                        maze[row][col] == 0) {

                    // Skip drawing on the starting position (red character)
                    boolean isStartPosition = (row == characterPosition[0] && col == characterPosition[1]);

                    // Skip drawing on the goal position (green target)
                    boolean isGoalPosition = (row == goalPosition[0] && col == goalPosition[1]);

                    // Only draw if the cell is part of the path but not start or goal
                    if (!isStartPosition && !isGoalPosition) {
                        double x = col * cellWidth;
                        double y = row * cellHeight;

                        if (solutionImage != null) {
                            // Draw the provided solution image (e.g., yellow ball)
                            gc.drawImage(solutionImage, x, y, cellWidth, cellHeight);
                        } else {
                            // Draw a small yellow dot (circle) at the center of the cell
                            gc.fillOval(x + cellWidth * 0.25, y + cellHeight * 0.25,
                                    cellWidth * 0.5, cellHeight * 0.5);
                        }
                    }
                }

            } catch (Exception e) {
                System.err.println("Error in drawSolutionPath: " + e.getMessage());
            }
        }
    }


    // Draw goal cell
    private void drawGoal(GraphicsContext gc, double cellWidth, double cellHeight) {
        double x = goalPosition[1] * cellWidth;
        double y = goalPosition[0] * cellHeight;
        if (goalImage != null) {
            gc.drawImage(goalImage, x, y, cellWidth, cellHeight);
        } else {
            gc.setFill(Color.GREEN);
            gc.fillRect(x, y, cellWidth, cellHeight);
            gc.setFill(Color.DARKGREEN);
            gc.fillText("GOAL", x + 2, y + cellHeight / 2);
        }
    }

    // Draw character
    private void drawCharacter(GraphicsContext gc, double cellWidth, double cellHeight) {
        double x = characterPosition[1] * cellWidth;
        double y = characterPosition[0] * cellHeight;
        if (heroImage != null) {
            gc.drawImage(heroImage, x, y, cellWidth, cellHeight);
        } else {
            gc.setFill(Color.RED);
            gc.fillOval(x + 2, y + 2, cellWidth - 4, cellHeight - 4);
        }
    }

}