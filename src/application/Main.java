package application;

import java.net.URL;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    LayoutController controller;

    @Override
    public void start(Stage primaryStage) {
        try {

            Platform.setImplicitExit(true);

            // Connect Java with XML
            URL location = getClass().getResource("Layout.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(location);
            fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
            Parent root = (Parent) fxmlLoader.load(location.openStream());

            // Create a scene and style it with CSS
            Scene scene = new Scene(root, 650, 450);
            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

            CoinbaseClient client = new CoinbaseClient();

            // Get a handle on our application's layout controller.
            controller = ((LayoutController) fxmlLoader.getController());
            client.addListener(controller.getListener());

            // Add a new agent to the client.
            PeerPressureAgent agent = new PeerPressureAgent(500, 4);
            client.addListener(agent.getListener());

            client.start();

            // Reveal the scene to the user.
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
