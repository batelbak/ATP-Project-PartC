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
 * Enhanced MazeDisplayer with images, zoom, and solution display
 */
public class MazeDisplayer extends Canvas {

    private int[][] maze;
    private int[] characterPosition = {0, 0};
    private int[] goalPosition = {0, 0};

    // Display properties
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

    // Images
    private Image heroImage;
    private Image wallImage;
    private Image goalImage;
    private Image solutionImage;

    // Parent container
    private AnchorPane parentPane;

    // Win callback
    private Runnable onWinCallback;

    public MazeDisplayer() {
        super();
        this.setFocusTraversable(true);
        initialize();
        loadImages();
    }

    private void initialize() {
        // Set up zoom functionality
        this.setOnScroll(this::handleZoomScroll);

        // Keyboard listener
        this.setOnKeyPressed(this::handleKeyPress);

        // Listen to parent size changes
        parentProperty().addListener((obs, oldParent, newParent) -> {
            if (newParent instanceof AnchorPane) {
                this.parentPane = (AnchorPane) newParent;
                setupParentListeners();
            }
        });
    }

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

    private Image loadImageSafely(String path) {
        try {
            return new Image(getClass().getResourceAsStream(path));
        } catch (Exception e) {
            return null;
        }
    }

    public void displayMaze(int[][] maze) {
        this.maze = maze;
        this.showSolution = false;

        if (maze != null && maze.length > 0) {
            setGoalPosition(maze.length - 1, maze[0].length - 1);
            updateCanvasSize();
            redraw();
        }
    }

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
    }

    public void updateCharacterPosition(int row, int col) {
        this.characterPosition[0] = row;
        this.characterPosition[1] = col;
        checkWinCondition();
        redraw();
    }

    private void checkWinCondition() {
        if (characterPosition[0] == goalPosition[0] && characterPosition[1] == goalPosition[1]) {
            if (onWinCallback != null) {
                onWinCallback.run();
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

    public void displaySolution(Solution solution) {
        this.solution = solution;
        if (solution != null) {
            this.solutionPath = solution.getSolutionPath();
            this.showSolution = true;
        } else {
            this.showSolution = false;
        }
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

    private void handleKeyPress(KeyEvent event) {
        if (maze == null) return;

        int row = characterPosition[0];
        int col = characterPosition[1];

        int newRow = row, newCol = col;

        KeyCode code = event.getCode();
        switch (code) {
            case NUMPAD8: newRow = row - 1; break; // Up
            case NUMPAD2: newRow = row + 1; break; // Down
            case NUMPAD4: newCol = col - 1; break; // Left
            case NUMPAD6: newCol = col + 1; break; // Right
            case NUMPAD7: newRow = row - 1; newCol = col - 1; break; // Up-Left
            case NUMPAD9: newRow = row - 1; newCol = col + 1; break; // Up-Right
            case NUMPAD1: newRow = row + 1; newCol = col - 1; break; // Down-Left
            case NUMPAD3: newRow = row + 1; newCol = col + 1; break; // Down-Right
            default: return;
        }

        if (newRow >= 0 && newRow < maze.length && newCol >= 0 && newCol < maze[0].length && maze[newRow][newCol] == 0) {
            updateCharacterPosition(newRow, newCol);
        }

        event.consume();
    }

    private void redraw() {
        if (maze == null) return;

        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        gc.save();
        gc.scale(zoomFactor, zoomFactor);

        double zoomedCellWidth = cellWidth / zoomFactor;
        double zoomedCellHeight = cellHeight / zoomFactor;

        for (int row = 0; row < maze.length; row++) {
            for (int col = 0; col < maze[row].length; col++) {
                double x = col * zoomedCellWidth;
                double y = row * zoomedCellHeight;
                drawCell(gc, row, col, x, y, zoomedCellWidth, zoomedCellHeight);
            }
        }

        if (showSolution && solutionPath != null) {
            drawSolutionPath(gc, zoomedCellWidth, zoomedCellHeight);
        }

        drawGoal(gc, zoomedCellWidth, zoomedCellHeight);
        drawCharacter(gc, zoomedCellWidth, zoomedCellHeight);

        gc.restore();
    }

    private void drawCell(GraphicsContext gc, int row, int col, double x, double y,
                          double cellWidth, double cellHeight) {
        if (maze[row][col] == 1) {
            if (wallImage != null) {
                gc.drawImage(wallImage, x, y, cellWidth, cellHeight);
            } else {
                gc.setFill(Color.BLACK);
                gc.fillRect(x, y, cellWidth, cellHeight);
            }
        } else {
            gc.setFill(Color.WHITE);
            gc.fillRect(x, y, cellWidth, cellHeight);
        }

        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(0.5);
        gc.strokeRect(x, y, cellWidth, cellHeight);
    }

    private void drawSolutionPath(GraphicsContext gc, double cellWidth, double cellHeight) {
        gc.setFill(Color.YELLOW.deriveColor(0, 1, 1, 0.6));

        for (AState state : solutionPath) {
            try {
                String[] parts = state.toString().replace("{", "").replace("}", "").split(",");
                int row = Integer.parseInt(parts[0].trim());
                int col = Integer.parseInt(parts[1].trim());

                if (row != characterPosition[0] || col != characterPosition[1]) {
                    double x = col * cellWidth;
                    double y = row * cellHeight;

                    if (solutionImage != null) {
                        gc.drawImage(solutionImage, x, y, cellWidth, cellHeight);
                    } else {
                        gc.fillOval(x + cellWidth * 0.2, y + cellHeight * 0.2,
                                cellWidth * 0.6, cellHeight * 0.6);
                    }
                }
            } catch (Exception e) {
                // skip
            }
        }
    }

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

    public double getCellWidth() { return cellWidth; }
    public double getCellHeight() { return cellHeight; }
    public double getZoomFactor() { return zoomFactor; }
    public boolean isShowingSolution() { return showSolution; }
    public int[] getCharacterPosition() { return characterPosition.clone(); }
    public int[] getGoalPosition() { return goalPosition.clone(); }
}
