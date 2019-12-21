import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.Optional;

public class BMMain extends Application {
    public static Stage stage;
    public static Scene scene;
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("MainScreen.fxml"));

        Scene s = new Scene(root);
        scene = s;
        primaryStage.setScene(s);
        primaryStage.setTitle("BibTex Manager");

        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            if (BMMainScreen.aChangeIsMade) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Changed Library");
                alert.setHeaderText("Currently open library is not saved. Do you want to save?");
                alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO, ButtonType.CLOSE);

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent()) {
                    if (result.get() == ButtonType.NO) {
                        primaryStage.close();
                    }
                }
            } else {
                primaryStage.close();
            }
        });

        stage = primaryStage;
        primaryStage.setMaximized(true);
        primaryStage.show();
    }
}
