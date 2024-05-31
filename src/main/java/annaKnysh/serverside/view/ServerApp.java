package annaKnysh.serverside.view;

import annaKnysh.serverside.controller.ConfigController;
import annaKnysh.serverside.controller.IServerController;
import annaKnysh.serverside.controller.IServerListener;
import annaKnysh.serverside.controller.ServerController;
import annaKnysh.serverside.model.IServer;
import annaKnysh.serverside.model.Server;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.util.Objects;

@SuppressWarnings("CallToPrintStackTrace")
public class ServerApp extends Application {
    private static IServerController serverController;
    private static IServer server;
    private static Stage primaryStage;

    public static void initializeServer(String portStr) {
        try {
            int port = Integer.parseInt(portStr);
            server = Server.getInstance(port);
            if (serverController == null) {
                serverController = new ServerController();
            }
            serverController.setServer(server);
            server.addListener((IServerListener) serverController);
            server.startserver();
        } catch (NumberFormatException e) {
            showAlert("Initialization Error", "Port must be a valid number.");
        } catch (Exception e) {
            showAlert("Initialization Error", "An error occurred while starting the server: " + e.getMessage());
        }
    }

    public void showConfigScreen() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/annaKnysh/serverside/config.fxml"));
        Parent root = loader.load();
        ConfigController configController = loader.getController();
        System.out.println("Config: " + configController);
        Scene scene = new Scene(root);
        primaryStage.setTitle("Server Configuration");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void showMainScreen() {
        Platform.runLater(() -> {
            try {
                serverController.clearListeners();
                FXMLLoader loader = new FXMLLoader(ServerApp.class.getResource("/annaKnysh/serverside/server.fxml"));
                Parent root = loader.load();
                IServerController mainController = loader.getController();
                mainController.setServer(server);
                serverController = loader.getController();
                serverController.setServer(server);
                Scene scene = new Scene(root);
                scene.getStylesheets().add(Objects.requireNonNull(ServerApp.class.getResource("/annaKnysh/serverside/server-styles.css")).toExternalForm());
                primaryStage.setTitle("Server");
                primaryStage.setScene(scene);
                System.out.println(server.getListeners());
                primaryStage.show();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "An error occurred while showing the main screen: " + e.getMessage());
            }
        });
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        ServerApp.primaryStage = primaryStage;
        showConfigScreen();
    }

    @Override
    public void stop() {
        if (server != null) {
            try {
                server.stopserver();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Server failed to stop cleanly: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
