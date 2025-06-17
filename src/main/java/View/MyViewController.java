package View;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import algorithms.search.AState;

import java.io.File;
import java.util.List;

public class MyViewController implements IView {

    @FXML
    private Label myLabel;

    @FXML
    private Button myButton;

    // Reference to the main application stage
    private Stage primaryStage;

    @FXML
    public void onButtonClicked(ActionEvent event) {
        System.out.println("Button was clicked!");
        showAlert("Button was clicked!");
    }

    /**
     * Injects the primary stage from outside after FXML loading.
     * Needed to show file dialogs from controller.
     * @param stage the application's primary stage
     */
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    @Override
    public void displayMaze(int[][] maze) {
        System.out.println("Maze displayed - rows: " + maze.length + ", cols: " + maze[0].length);
        // TODO: draw the maze visually on a Canvas or other JavaFX component
    }

    @Override
    public void updateCharacterPosition(int row, int col) {
        System.out.println("Character moved to: (" + row + ", " + col + ")");
        // TODO: update the character's position on the visual maze
    }

    @Override
    public void displaySolution(List<AState> solutionPath) {
        System.out.println("Displaying solution with " + solutionPath.size() + " steps.");
        // TODO: display the solution path visually on the maze
    }

    @Override
    public void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Message");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void showMazeSolved() {
        showAlert("Maze solved successfully!");
    }

    @Override
    public void setControlsEnabled(boolean enabled) {
        myButton.setDisable(!enabled);
        // You can add more UI elements here to enable/disable
    }

    @Override
    public int[] getMazeDimensions() {
        // TODO: ask the user for maze dimensions via dialog/input
        // For now, returns default dimensions
        return new int[]{10, 10};
    }

    @Override
    public String showSaveFileDialog() {
        if (primaryStage == null) return null;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Maze");
        File file = fileChooser.showSaveDialog(primaryStage);
        return file != null ? file.getAbsolutePath() : null;
    }

    @Override
    public String showLoadFileDialog() {
        if (primaryStage == null) return null;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Maze");
        File file = fileChooser.showOpenDialog(primaryStage);
        return file != null ? file.getAbsolutePath() : null;
    }
}
