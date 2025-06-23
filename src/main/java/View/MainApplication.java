package View;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class MainApplication extends Application {

    public static Stage mainStage;

    @Override
    public void start(Stage stage) throws Exception {
        mainStage = stage;


        FXMLLoader introLoader = new FXMLLoader(getClass().getResource("/View/maze_intro_screen.fxml"));
        Parent introRoot = introLoader.load();


        Scene introScene = new Scene(introRoot, 1000, 700);
        stage.setTitle("Maze Application - Welcome");
        stage.setScene(introScene);
        stage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
