package View;

import Model.IModel;
import Model.MyModel;
import ViewModel.MyViewModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class MainApplication extends Application {

    public static Stage mainStage;

    @Override
    public void start(Stage stage) throws Exception {
        mainStage = stage;

        // Load FXML using class-based loader since file is inside package View
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(MyViewController.class.getResource("MyView.fxml")));
        Parent root = loader.load();

        // Connect controller
        MyViewController controller = loader.getController();

        // Connect Model and ViewModel
        IModel model = new MyModel();
        MyViewModel viewModel = new MyViewModel(model);
        controller.setViewModel(viewModel);
        controller.setPrimaryStage(stage); // in case of FileChoosers etc.

        // Load CSS
        root.getStylesheets().add(Objects.requireNonNull(MyViewController.class.getResource("MainStyle.css")).toExternalForm());

        // Show stage
        stage.setTitle("Maze Application - Part C");
        stage.setScene(new Scene(root, 800, 600));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
