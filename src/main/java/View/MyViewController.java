package View;

import ViewModel.MyViewModel;
import algorithms.search.AState;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * Controller for the main view - handles all UI interactions
 * Implements full MVVM pattern with proper separation of concerns
 */
public class MyViewController implements IView, Initializable {

    // FXML injected components
    @FXML private MazeDisplayer mazeDisplayer;
    @FXML private ScrollPane mazeScrollPane;
    @FXML private Button generateMazeButton;
    @FXML private Button solveMazeButton;
    @FXML private Label statusLabel;
    @FXML private CheckBox showSolutionCheckBox; // הוספתי את זה
    @FXML private MenuItem saveMazeMenuItem;
    @FXML private MenuItem loadMazeMenuItem;
    @FXML private MenuItem newMazeMenuItem;
    @FXML private MenuItem propertiesMenuItem;
    @FXML private MenuItem exitMenuItem;
    @FXML private MenuItem helpMenuItem;
    @FXML private MenuItem aboutMenuItem;

    // Reference to ViewModel (MVVM pattern)
    private MyViewModel viewModel;

    // Current maze state
    private int[][] currentMaze;
    private int[] characterPosition = {0, 0};
    private boolean mazeGenerated = false;
    private Stage primaryStage;
    private int goalRow;
    private int goalCol;
    // Media players
    private MediaPlayer backgroundMusicPlayer;
    private MediaPlayer winSoundPlayer;
    /**
     * Initialize the controller after FXML loading
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupInitialState();
        setupKeyboardHandling();
        setupZoomFunctionality();
        updateControlsState();
        playBackgroundMusic();
    }
    private void playBackgroundMusic() {
        try {
            Media media = new Media(getClass().getResource("/backgroundSound/backgroundsound1.mp3").toExternalForm());
            backgroundMusicPlayer = new MediaPlayer(media);
            backgroundMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            backgroundMusicPlayer.setVolume(0.4);
            backgroundMusicPlayer.play();
        } catch (Exception e) {
            System.out.println("Error playing background music: " + e.getMessage());
        }
    }
    private void playWinSound() {
        try {
            Media media = new Media(getClass().getResource("/backgroundSound/WinSound.wav").toExternalForm());
            winSoundPlayer = new MediaPlayer(media);
            winSoundPlayer.setVolume(1.0);
            winSoundPlayer.play();
        } catch (Exception e) {
            System.out.println("Error playing win sound: " + e.getMessage());
        }
    }
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    /**
     * Set maze generated state (called from GameIntroController)
     */
    public void setMazeGenerated(boolean generated) {
        this.mazeGenerated = generated;
        updateControlsState();
    }

    /**
     * Get maze generated state (for debugging)
     */
    public boolean isMazeGenerated() {
        return mazeGenerated;
    }

    /**
     * Get the MazeDisplayer component
     */
    public MazeDisplayer getMazeDisplayer() {
        return mazeDisplayer;
    }

    /**
     * Set up initial UI state
     */
    private void setupInitialState() {
        if (statusLabel != null) {
            statusLabel.setText("Ready to generate maze...");
        }

        // Make maze displayer focusable for key events
        if (mazeDisplayer != null) {
            mazeDisplayer.setFocusTraversable(true);
        }
    }

    /**
     * Set up keyboard navigation (NumPad keys)
     */
    private void setupKeyboardHandling() {
        if (mazeDisplayer != null) {
            mazeDisplayer.setOnKeyPressed(this::handleKeyPressed);

            // Request focus when clicked
            mazeDisplayer.setOnMouseClicked(e -> {
                mazeDisplayer.requestFocus();
                System.out.println("Maze clicked - focus requested");
            });
        }
    }

    /**
     * Set up zoom functionality with Ctrl+Scroll
     */
    private void setupZoomFunctionality() {
        if (mazeScrollPane != null) {
            mazeScrollPane.setOnScroll(this::handleZoomScroll);
        }
    }

    /**
     * Handle keyboard navigation (NumPad keys as specified in requirements)
     */
    private void handleKeyPressed(KeyEvent event) {
        System.out.println("Key pressed: " + event.getCode() + ", mazeGenerated: " + mazeGenerated);

        if (!mazeGenerated || viewModel == null) {
            System.out.println("Cannot move - maze not generated or viewModel null");
            return;
        }

        String direction = null;

        switch (event.getCode()) {
            // Standard directions
            case NUMPAD8: case DIGIT8: direction = "UP"; break;
            case NUMPAD2: case DIGIT2: direction = "DOWN"; break;
            case NUMPAD4: case DIGIT4: direction = "LEFT"; break;
            case NUMPAD6: case DIGIT6: direction = "RIGHT"; break;

            // Diagonal movements
            case NUMPAD7: case DIGIT7: direction = "UP-LEFT"; break;
            case NUMPAD9: case DIGIT9: direction = "UP-RIGHT"; break;
            case NUMPAD1: case DIGIT1: direction = "DOWN-LEFT"; break;
            case NUMPAD3: case DIGIT3: direction = "DOWN-RIGHT"; break;

            default:
                System.out.println("Key not mapped: " + event.getCode());
                return; // Ignore other keys
        }

        System.out.println("Moving in direction: " + direction);
        // Move character through ViewModel
        moveCharacterInDirection(direction);
        event.consume();
    }

    /**
     * Move character in specified direction (including diagonals)
     */
    private void moveCharacterInDirection(String direction) {
        if (viewModel == null) return;

        int currentRow = characterPosition[0];
        int currentCol = characterPosition[1];
        int newRow = currentRow;
        int newCol = currentCol;

        // Calculate new position based on direction
        switch (direction) {
            case "UP": newRow--; break;
            case "DOWN": newRow++; break;
            case "LEFT": newCol--; break;
            case "RIGHT": newCol++; break;
            case "UP-LEFT": newRow--; newCol--; break;
            case "UP-RIGHT": newRow--; newCol++; break;
            case "DOWN-LEFT": newRow++; newCol--; break;
            case "DOWN-RIGHT": newRow++; newCol++; break;
        }

        if (isValidMove(newRow, newCol)) {
            viewModel.moveCharacter(direction);
            characterPosition[0] = newRow;
            characterPosition[1] = newCol;
            updateCharacterPosition(newRow, newCol);
            checkIfMazeSolved();
            statusLabel.setText("Position: (" + newRow + ", " + newCol + ")");
            System.out.println("Moved to: (" + newRow + ", " + newCol + ")");
        } else {
            System.out.println("Invalid move to: (" + newRow + ", " + newCol + ")");
            showAlert("Invalid move - can't move there!");
        }
    }

    /**
     * Check if a move to the given position is valid
     */
    private boolean isValidMove(int row, int col) {
        if (currentMaze == null) return false;

        return row >= 0 && row < currentMaze.length &&
                col >= 0 && col < currentMaze[0].length &&
                (currentMaze[row][col] == 0 || (row == goalRow && col == goalCol));
    }

    /**
     * Check if player reached the goal position
     */
    private void checkIfMazeSolved() {
        if (characterPosition[0] == goalRow && characterPosition[1] == goalCol) {
            Platform.runLater(() -> showMazeSolved());
        }
    }

    /**
     * Handle zoom with Ctrl+Scroll
     */
    private void handleZoomScroll(ScrollEvent event) {
        if (event.isControlDown() && mazeDisplayer != null) {
            double zoomFactor = event.getDeltaY() > 0 ? 1.1 : 0.9;
            mazeDisplayer.setScaleX(mazeDisplayer.getScaleX() * zoomFactor);
            mazeDisplayer.setScaleY(mazeDisplayer.getScaleY() * zoomFactor);
            event.consume();
        }
    }

    // =================== FXML Event Handlers ===================

    @FXML
    private void onGenerateMaze(ActionEvent event) {
        int[] dimensions = getMazeDimensions();
        if (dimensions != null && viewModel != null) {
            statusLabel.setText("Generating maze...");
            generateMazeButton.setDisable(true);

            try {
                viewModel.generateMaze(dimensions[0], dimensions[1]);
                mazeGenerated = true;
                statusLabel.setText("Maze generated! Use NumPad keys to navigate (2,4,6,8 + diagonals 1,3,7,9)");

                // Focus on maze for keyboard input
                Platform.runLater(() -> {
                    mazeDisplayer.setFocusTraversable(true);
                    mazeDisplayer.requestFocus();
                });

            } catch (Exception e) {
                showAlert("Error generating maze: " + e.getMessage());
                statusLabel.setText("Error generating maze");
            } finally {
                generateMazeButton.setDisable(false);
                updateControlsState();
            }
        }
    }

    @FXML
    private void onSolveMaze(ActionEvent event) {
        if (viewModel != null && mazeGenerated) {
            statusLabel.setText("Solving maze...");
            try {
                viewModel.solveMaze();
                List<AState> solution = viewModel.getSolution();
                if (solution != null && !solution.isEmpty()) {
                    // רק שומר את הפתרון, לא מציג אותו אוטומטית
                    statusLabel.setText("Solution found! " + solution.size() + " steps. Check the box to show it.");
                } else {
                    showAlert("No solution found for this maze!");
                }
            } catch (Exception e) {
                showAlert("Error solving maze: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onToggleSolutionDisplay(ActionEvent event) {
        CheckBox checkBox = (CheckBox) event.getSource();
        boolean show = checkBox.isSelected();

        if (viewModel != null && mazeGenerated) {
            List<AState> solution = viewModel.getSolution();
            if (solution != null && !solution.isEmpty()) {
                if (show) {
                    // הוסף הדפסה לבדיקה
                    System.out.println("Solution path size: " + solution.size());
                    for (int i = 0; i < solution.size(); i++) {
                        try {
                            algorithms.mazeGenerators.Position pos =
                                    (algorithms.mazeGenerators.Position) solution.get(i).getClass().getMethod("getPosition").invoke(solution.get(i));
                            System.out.println("Step " + i + ": (" + pos.getRowIndex() + ", " + pos.getColumnIndex() + ")");
                        } catch (Exception e) {
                            System.out.println("Error reading position for step " + i);
                        }
                    }

                    mazeDisplayer.displaySolutionPath(solution);
                    statusLabel.setText("Solution displayed (" + solution.size() + " steps)");
                } else {
                    mazeDisplayer.clearSolutionPath();
                    statusLabel.setText("Solution hidden");
                }
            } else {
                showAlert("No solution found yet. Press 'Solve' first.");
                checkBox.setSelected(false);
            }
        }
    }

    @FXML
    private void onNewMaze(ActionEvent event) {
        onGenerateMaze(event);
    }

    @FXML
    private void onSaveMaze(ActionEvent event) {
        if (!mazeGenerated) {
            showAlert("No maze to save! Generate a maze first.");
            return;
        }

        String filePath = showSaveFileDialog();
        if (filePath != null && viewModel != null) {
            try {
                viewModel.saveMaze(new File(filePath));
                statusLabel.setText("Maze saved successfully!");
                showAlert("Maze saved to: " + filePath);
            } catch (Exception e) {
                showAlert("Error saving maze: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onLoadMaze(ActionEvent event) {
        String filePath = showLoadFileDialog();
        if (filePath != null && viewModel != null) {
            try {
                viewModel.loadMaze(new File(filePath));
                mazeGenerated = true;
                statusLabel.setText("Maze loaded successfully!");
                updateControlsState();
                Platform.runLater(() -> {
                    mazeDisplayer.setFocusTraversable(true);
                    mazeDisplayer.requestFocus();
                });
            } catch (Exception e) {
                showAlert("Error loading maze: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onShowProperties(ActionEvent event) {
        showAlert("Maze Game Properties:\n\n" +
                "Navigation:\n" +
                "• NumPad 2,4,6,8 - Move up/down/left/right\n" +
                "• NumPad 1,3,7,9 - Diagonal movement\n" +
                "• Ctrl+Scroll - Zoom in/out\n\n" +
                "Features:\n" +
                "• Generate new mazes\n" +
                "• Solve mazes automatically\n" +
                "• Save/Load mazes\n" +
                "• MVVM Architecture");
    }

    @FXML
    private void onShowHelp(ActionEvent event) {
        showAlert("Maze Game Help:\n\n" +
                "How to Play:\n" +
                "1. Click 'Generate Maze' to create a new maze\n" +
                "2. Use NumPad keys to move your character:\n" +
                "   - 2,4,6,8 for basic directions\n" +
                "   - 1,3,7,9 for diagonal movement\n" +
                "3. Alternatively, click with the mouse on a nearby cell to move there\n" +
                "4. Navigate from start (green) to goal (red)\n" +
                "5. Use 'Solve Maze' to see the solution\n" +
                "6. Save/Load mazes using File menu\n\n" +
                "Tips:\n" +
                "• Diagonal moves require clear L-shaped path\n" +
                "• Use Ctrl+Scroll to zoom in/out\n" +
                "• Click on maze area to focus for keyboard input");
    }


    @FXML
    private void onShowAbout(ActionEvent event) {
        showAlert("Maze Game - Part C\n\n" +
                "Advanced Topics in Programming\n" +
                "Ben-Gurion University\n\n" +
                "This project was developed as part of the course \"Advanced Topics in Programming\".\n" +
                "Students: Sapir Aharoni and Batel Bakala\n" +
                "Supervised by the Department of Software and Information Systems Engineering, BGU.\n\n" +
                "Features:\n" +
                "• MVVM Architecture Pattern\n" +
                "• JavaFX GUI with event-driven programming\n" +
                "• Custom maze generation algorithms\n" +
                "• Search algorithms (BFS, DFS, Best-First)\n" +
                "• File I/O for maze persistence\n" +
                "• Keyboard navigation with NumPad\n" +
                "• Zoom functionality\n\n" +
                "Algorithms:\n" +
                "• Maze Generation: MyMazeGenerator\n" +
                "• Maze Solving: BFS, DFS, BestFirstSearch");
    }


    @FXML
    private void onExit(ActionEvent event) {
        Platform.exit();
    }

    // Simple button click for testing
    @FXML
    public void onButtonClicked(ActionEvent event) {
        if (viewModel != null) {
            onGenerateMaze(event);
        } else {
            showAlert("ViewModel not connected! Check your setup.");
        }
    }

    // =================== IView Implementation ===================

    @Override
    public void displayMaze(int[][] maze) {
        this.currentMaze = maze;
        this.mazeGenerated = true;

        if (mazeDisplayer != null) {
            mazeDisplayer.displayMaze(maze);

            // במקום להגדיר קשיח (0,0), קבל מה-ViewModel:
            characterPosition[0] = viewModel.playerRowProperty().get();
            characterPosition[1] = viewModel.playerColProperty().get();
            mazeDisplayer.updateCharacterPosition(characterPosition[0], characterPosition[1]);

            // המטרה - צריך להוסיף property ל-ViewModel
            // או לבדוק במבוך עצמו איפה המטרה
            goalRow = viewModel.getGoalRow();
            goalCol = viewModel.getGoalCol();
            mazeDisplayer.setGoalPosition(goalRow, goalCol);

            Platform.runLater(() -> {
                mazeDisplayer.setFocusTraversable(true);
                mazeDisplayer.requestFocus();
                setupKeyboardHandling();
            });
        }
        updateControlsState();
    }

    @Override
    public void updateCharacterPosition(int row, int col) {
        if (mazeDisplayer != null) {
            mazeDisplayer.updateCharacterPosition(row, col);
        }
        characterPosition[0] = row;
        characterPosition[1] = col;
    }

    @Override
    public void displaySolution(List<AState> solutionPath) {
        if (mazeDisplayer != null && solutionPath != null) {
            // Pass the solution directly to the displayer
            mazeDisplayer.displaySolutionPath(solutionPath);
        }
    }

    @Override
    public void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Maze Game");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void showMazeSolved() {
        playWinSound(); //
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Congratulations!");
        alert.setHeaderText(" Maze Solved! ");
        alert.setContentText("Excellent work! You've successfully navigated through the maze!");
        alert.showAndWait();
        statusLabel.setText("Maze solved! Generate a new one?");
    }

    @Override
    public void setControlsEnabled(boolean enabled) {
        if (generateMazeButton != null) generateMazeButton.setDisable(!enabled);
        if (solveMazeButton != null) solveMazeButton.setDisable(!enabled || !mazeGenerated);
        if (saveMazeMenuItem != null) saveMazeMenuItem.setDisable(!mazeGenerated);
    }

    @Override
    public int[] getMazeDimensions() {
        Dialog<int[]> dialog = new Dialog<>();
        dialog.setTitle("Generate New Maze");
        dialog.setHeaderText("Enter maze dimensions:");

        // Create form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField rowsField = new TextField("25");
        TextField colsField = new TextField("25");

        grid.add(new Label("Rows:"), 0, 0);
        grid.add(rowsField, 1, 0);
        grid.add(new Label("Columns:"), 0, 1);
        grid.add(colsField, 1, 1);
        grid.add(new Label("Tip: Larger mazes take longer to generate"), 0, 2, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Result converter
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    int rows = Integer.parseInt(rowsField.getText().trim());
                    int cols = Integer.parseInt(colsField.getText().trim());

                    if (rows > 0 && cols > 0) {
                        return new int[]{rows, cols};
                    } else {
                        showAlert("Invalid dimensions!\nPlease enter positive values.");
                    }
                } catch (NumberFormatException e) {
                    showAlert("Invalid input!\nPlease enter valid numbers.");
                }
            }
            return null;
        });

        // Focus on first field
        Platform.runLater(() -> rowsField.requestFocus());

        Optional<int[]> result = dialog.showAndWait();
        return result.orElse(null);
    }

    @Override
    public String showSaveFileDialog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Maze");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Maze Files", "*.maze"));
        fileChooser.setInitialFileName("my_maze.maze");

        Stage stage = (Stage) generateMazeButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        return file != null ? file.getAbsolutePath() : null;
    }

    @Override
    public String showLoadFileDialog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Maze");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Maze Files", "*.maze"));

        Stage stage = (Stage) generateMazeButton.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        return file != null ? file.getAbsolutePath() : null;
    }

    // =================== Helper Methods ===================

    /**
     * Update the enabled/disabled state of controls based on current state
     */
    private void updateControlsState() {
        if (solveMazeButton != null) {
            solveMazeButton.setDisable(!mazeGenerated);
        }
        if (saveMazeMenuItem != null) {
            saveMazeMenuItem.setDisable(!mazeGenerated);
        }
    }

    /**
     * Set the ViewModel (called from MainApplication)
     */
    public void setViewModel(MyViewModel viewModel) {
        this.viewModel = viewModel;

        // Set up property bindings for MVVM
        if (viewModel != null) {
            setupPropertyBindings();
        }
    }

    /**
     * Set up data bindings between ViewModel and View (MVVM pattern)
     */
    private void setupPropertyBindings() {
        // Bind maze display to ViewModel maze property
        viewModel.mazeProperty().addListener((obs, oldMaze, newMaze) -> {
            if (newMaze != null) {
                displayMaze(newMaze);
            }
        });

        // Bind character position to ViewModel properties
        viewModel.playerRowProperty().addListener((obs, oldRow, newRow) -> {
            updateCharacterPosition(newRow.intValue(), viewModel.playerColProperty().get());
        });

        viewModel.playerColProperty().addListener((obs, oldCol, newCol) -> {
            updateCharacterPosition(viewModel.playerRowProperty().get(), newCol.intValue());
        });

        if (mazeDisplayer != null) {
            mazeDisplayer.setOnWinCallback(() -> {
                Platform.runLater(() -> {
                    showMazeSolved();
                });
            });
        }
    }
}