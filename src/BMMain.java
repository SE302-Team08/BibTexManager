import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class BMMain extends Application {
    public static Stage stage;
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("MainScreen.fxml"));

        Scene scene = new Scene(root);
        scene.getStylesheets().add("./stylesheet.css");
        primaryStage.setScene(scene);
        primaryStage.setTitle("BibTex Manager");
        stage = primaryStage;
        primaryStage.show();
    }

    @Override
    public void stop() {
        BMConfig config = new BMConfig();
        config.setProps(new BMParser().getFile());
    }
}
