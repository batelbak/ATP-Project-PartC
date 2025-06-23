package View;

import ViewModel.MyViewModel;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import Model.IModel;
import Model.MyModel;

import java.io.IOException;
import java.util.Objects;

public class GameIntroController {

    @FXML
    private TextField textField_mazeRows;

    @FXML
    private TextField textField_mazeColumns;

    @FXML
    private CheckBox solutionCheckBox;

    /**
     * Triggered when the user clicks the Start button
     */
    @FXML
    private void startGame(ActionEvent event) {
        try {
            int rows = Integer.parseInt(textField_mazeRows.getText());
            int cols = Integer.parseInt(textField_mazeColumns.getText());
            boolean showSolution = solutionCheckBox.isSelected();

            // Load the main game screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/MyView.fxml"));
            Parent root = loader.load();

            // Pass ViewModel if needed
            MyViewController controller = loader.getController();
            IModel model = new MyModel();
            MyViewModel viewModel = new MyViewModel(model);
            controller.setViewModel(viewModel);

            // Show main stage FIRST
            Stage stage = (Stage) textField_mazeRows.getScene().getWindow();
            controller.setPrimaryStage(stage);
            Scene gameScene = new Scene(root, 1000, 800);
            root.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/View/MainStyle.css")).toExternalForm());
            stage.setScene(gameScene);
            stage.setTitle("Maze Game - Play");
            stage.show();

            // Generate the maze after the stage is shown
            Platform.runLater(() -> {
                // Set maze generated state BEFORE generating
                controller.setMazeGenerated(true);

                // Generate the maze
                viewModel.generateMaze(rows, cols);

                // Apply selected option for showing solution
                if (showSolution) {
                    viewModel.solveMaze();
                }

                // Ensure focus is set properly
                Platform.runLater(() -> {
                    if (controller.getMazeDisplayer() != null) {
                        controller.getMazeDisplayer().setFocusTraversable(true);
                        controller.getMazeDisplayer().requestFocus();
                    }
                });
            });

        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter valid numbers.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}