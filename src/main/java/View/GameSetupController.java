package View;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for Game Setup Screen - Maze Configuration
 */
public class GameSetupController implements Initializable {

    // Maze Dimensions
    @FXML private TextField rowsField;
    @FXML private TextField columnsField;

    // Difficulty Buttons
    @FXML private ToggleButton easyBtn;
    @FXML private ToggleButton mediumBtn;
    @FXML private ToggleButton hardBtn;
    @FXML private ToggleButton expertBtn;
    @FXML private Label difficultyDescription;

    // Algorithm Settings
    @FXML private ComboBox<String> mazeGeneratorCombo;
    @FXML private ComboBox<String> searchAlgorithmCombo;

    // Game Features
    @FXML private CheckBox soundEffectsCheck;
    @FXML private CheckBox showHintsCheck;
    @FXML private CheckBox showTimerCheck;
    @FXML private CheckBox animationsCheck;

    // Action Buttons
    @FXML private Button startGameBtn;
    @FXML private Button resetBtn;
    @FXML private Button backBtn;

    // Game Settings
    private String selectedDifficulty = "medium";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupDifficultyButtons();
        setupAlgorithmCombos();
        setupDifficultyDescriptions();
    }

    private void setupDifficultyButtons() {
        ToggleGroup difficultyGroup = new ToggleGroup();
        easyBtn.setToggleGroup(difficultyGroup);
        mediumBtn.setToggleGroup(difficultyGroup);
        hardBtn.setToggleGroup(difficultyGroup);
        expertBtn.setToggleGroup(difficultyGroup);

        // Set medium as default
        mediumBtn.setSelected(true);

        // Add listener for difficulty changes
        difficultyGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                ToggleButton selected = (ToggleButton) newToggle;
                updateDifficulty(selected);
            }
        });
    }

    private void setupAlgorithmCombos() {
        // Maze Generator options
        mazeGeneratorCombo.getItems().addAll(
                "MyMazeGenerator (Custom)",
                "SimpleMazeGenerator",
                "EmptyMazeGenerator"
        );
        mazeGeneratorCombo.setValue("MyMazeGenerator (Custom)");

        // Search Algorithm options
        searchAlgorithmCombo.getItems().addAll(
                "BestFirstSearch (Recommended)",
                "BreadthFirstSearch",
                "DepthFirstSearch"
        );
        searchAlgorithmCombo.setValue("BestFirstSearch (Recommended)");
    }

    private void setupDifficultyDescriptions() {
        updateDifficultyDescription("medium");
    }

    private void updateDifficulty(ToggleButton selected) {
        if (selected == easyBtn) {
            selectedDifficulty = "easy";
            updateDifficultyDescription("easy");
            // Set easier maze dimensions
            rowsField.setText("10");
            columnsField.setText("10");
        } else if (selected == mediumBtn) {
            selectedDifficulty = "medium";
            updateDifficultyDescription("medium");
            // Set medium maze dimensions
            rowsField.setText("15");
            columnsField.setText("15");
        } else if (selected == hardBtn) {
            selectedDifficulty = "hard";
            updateDifficultyDescription("hard");
            // Set harder maze dimensions
            rowsField.setText("25");
            columnsField.setText("25");
        } else if (selected == expertBtn) {
            selectedDifficulty = "expert";
            updateDifficultyDescription("expert");
            // Set expert maze dimensions
            rowsField.setText("50");
            columnsField.setText("50");
        }

        System.out.println("Difficulty changed to: " + selectedDifficulty);
    }

    private void updateDifficultyDescription(String difficulty) {
        switch (difficulty) {
            case "easy":
                difficultyDescription.setText("Perfect for beginners - smaller maze, more hints");
                break;
            case "medium":
                difficultyDescription.setText("Balanced challenge for most players");
                break;
            case "hard":
                difficultyDescription.setText("For experienced players - larger maze, fewer hints");
                break;
            case "expert":
                difficultyDescription.setText("Ultimate challenge - massive maze, no hints!");
                break;
        }
    }

    @FXML
    private void startGame(ActionEvent event) {
        // Validate input
        if (!validateInput()) {
            return;
        }

        // Get settings
        int rows = Integer.parseInt(rowsField.getText());
        int columns = Integer.parseInt(columnsField.getText());
        String mazeGenerator = mazeGeneratorCombo.getValue();
        String searchAlgorithm = searchAlgorithmCombo.getValue();

        System.out.println("Starting game with settings:");
        System.out.println("Maze Size: " + rows + "x" + columns);
        System.out.println("Difficulty: " + selectedDifficulty);
        System.out.println("Generator: " + mazeGenerator);
        System.out.println("Search: " + searchAlgorithm);
        System.out.println("Sound Effects: " + soundEffectsCheck.isSelected());
        System.out.println("Show Hints: " + showHintsCheck.isSelected());
        System.out.println("Show Timer: " + showTimerCheck.isSelected());
        System.out.println("Animations: " + animationsCheck.isSelected());

        // TODO: Start the actual game with these settings
        showAlert("Game Starting!",
                "Starting your maze adventure!\n\n" +
                        "Size: " + rows + " x " + columns + "\n" +
                        "Difficulty: " + selectedDifficulty.toUpperCase() + "\n" +
                        "Generator: " + mazeGenerator + "\n" +
                        "Search: " + searchAlgorithm);
    }

    @FXML
    private void resetToDefaults(ActionEvent event) {
        // Reset all settings to default
        rowsField.setText("15");
        columnsField.setText("15");
        mediumBtn.setSelected(true);
        mazeGeneratorCombo.setValue("MyMazeGenerator (Custom)");
        searchAlgorithmCombo.setValue("BestFirstSearch (Recommended)");
        soundEffectsCheck.setSelected(true);
        showHintsCheck.setSelected(false);
        showTimerCheck.setSelected(true);
        animationsCheck.setSelected(true);

        selectedDifficulty = "medium";
        updateDifficultyDescription("medium");

        showAlert("Settings Reset", "All settings have been reset to defaults!");
    }

    @FXML
    private void goBack(ActionEvent event) {
        Stage stage = (Stage) backBtn.getScene().getWindow();
        stage.close();
    }

    private boolean validateInput() {
        try {
            int rows = Integer.parseInt(rowsField.getText());
            int columns = Integer.parseInt(columnsField.getText());

            if (rows < 5 || rows > 100) {
                showAlert("Invalid Input", "Rows must be between 5 and 100!");
                return false;
            }

            if (columns < 5 || columns > 100) {
                showAlert("Invalid Input", "Columns must be between 5 and 100!");
                return false;
            }

        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter valid numbers for maze dimensions!");
            return false;
        }

        return true;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Getters for the settings
    public int getRows() {
        return Integer.parseInt(rowsField.getText());
    }

    public int getColumns() {
        return Integer.parseInt(columnsField.getText());
    }

    public String getSelectedDifficulty() {
        return selectedDifficulty;
    }

    public String getMazeGenerator() {
        return mazeGeneratorCombo.getValue();
    }

    public String getSearchAlgorithm() {
        return searchAlgorithmCombo.getValue();
    }

    public boolean isSoundEffectsEnabled() {
        return soundEffectsCheck.isSelected();
    }

    public boolean isShowHintsEnabled() {
        return showHintsCheck.isSelected();
    }

    public boolean isShowTimerEnabled() {
        return showTimerCheck.isSelected();
    }

    public boolean isAnimationsEnabled() {
        return animationsCheck.isSelected();
    }
}