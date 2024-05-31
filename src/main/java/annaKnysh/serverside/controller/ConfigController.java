package annaKnysh.serverside.controller;

import annaKnysh.serverside.database.DatabaseConnection;
import annaKnysh.serverside.view.ServerApp;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ConfigController {

    @FXML
    private TextField portField;
    @FXML
    private TextField dbUrlField;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    private String serverPort;
    private String databaseUrl;
    private String username;
    private String password;

    @FXML
    public void handleSave() {
        try {
            // Set the variables with values from the text fields
            serverPort = portField.getText();
            databaseUrl = dbUrlField.getText();
            username = usernameField.getText();
            password = passwordField.getText();

            // Validate inputs
            if (serverPort.isEmpty() || databaseUrl.isEmpty() || username.isEmpty() || password.isEmpty()) {
                showAlert("Validation Error", "All fields must be filled out.");
                return;
            }
            int port;
            try {
                port = Integer.parseInt(serverPort);
            } catch (NumberFormatException e) {
                showAlert("Validation Error", "Port must be a valid number.");
                return;
            }

            // Initialize the database connection
            if (!DatabaseConnection.initialize(databaseUrl, username, password)) {
                showAlert("Database Connection Error", "Could not connect to the database. Please check your credentials and try again.");
                return;
            }

            // Proceed to start the server
            ServerApp.initializeServer(serverPort);
            ServerApp.showMainScreen();

            // Close the configuration window
            Stage stage = (Stage) portField.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            showAlert("Error", "An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
