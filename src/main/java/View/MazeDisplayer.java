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
    }

    public void clearSolutionPath() {
        this.solutionPath = null;
        this.showSolution = false;  // חשוב מאוד!
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

    // Accept and display solution
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

    // Draw solution path as yellow dots or image
    // Draw solution path as yellow dots or image
    // Draw solution path as yellow dots or image
    private void drawSolutionPath(GraphicsContext gc, double cellWidth, double cellHeight) {
        gc.setFill(Color.YELLOW.deriveColor(0, 1, 1, 0.8));

        for (int i = 0; i < solutionPath.size(); i++) {
            AState state = solutionPath.get(i);
            if (state == null) continue;

            try {
                // נניח ש-AState הוא MazeState עם פונקציה getPosition()
                algorithms.mazeGenerators.Position pos =
                        (algorithms.mazeGenerators.Position) state.getClass().getMethod("getPosition").invoke(state);

                int row = pos.getRowIndex();
                int col = pos.getColumnIndex();

                // בדיקה שהמיקום חוקי ולא על קיר
                if (row >= 0 && row < maze.length && col >= 0 && col < maze[0].length && maze[row][col] == 0) {

                    // אל תציג כדור על נקודת ההתחלה (הדמות האדומה)
                    boolean isStartPosition = (row == characterPosition[0] && col == characterPosition[1]);

                    // אל תציג כדור על נקודת המטרה (המטרה הירוקה)
                    boolean isGoalPosition = (row == goalPosition[0] && col == goalPosition[1]);

                    // צייר רק אם זה לא נקודת התחלה ולא מטרה
                    if (!isStartPosition && !isGoalPosition) {
                        double x = col * cellWidth;
                        double y = row * cellHeight;

                        if (solutionImage != null) {
                            gc.drawImage(solutionImage, x, y, cellWidth, cellHeight);
                        } else {
                            // צייר כדור צהוב קטן יותר
                            gc.fillOval(x + cellWidth * 0.25, y + cellHeight * 0.25, cellWidth * 0.5, cellHeight * 0.5);
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

    // Public getters
    public double getCellWidth() { return cellWidth; }
    public double getCellHeight() { return cellHeight; }
    public double getZoomFactor() { return zoomFactor; }
    public boolean isShowingSolution() { return showSolution; }
    public int[] getCharacterPosition() { return characterPosition.clone(); }
    public int[] getGoalPosition() { return goalPosition.clone(); }
}