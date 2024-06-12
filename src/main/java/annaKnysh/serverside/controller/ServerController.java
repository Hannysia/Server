package annaKnysh.serverside.controller;

import annaKnysh.serverside.chat.ChatDisplayData;
import annaKnysh.serverside.database.DatabaseManager;
import annaKnysh.serverside.chat.InterfaceFactory;
import annaKnysh.serverside.xml.UserConnectionInfo;
import annaKnysh.serverside.xml.auth.AuthenticationQuery;
import annaKnysh.serverside.xml.auth.AuthenticationAnswer;
import annaKnysh.serverside.xml.chat.ChatRequest;
import annaKnysh.serverside.xml.message.Message;
import annaKnysh.serverside.xml.XMLUtility;
import annaKnysh.serverside.xml.message.MessagesResponse;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Callback;
import org.java_websocket.WebSocket;
import annaKnysh.serverside.model.IServer;

import java.io.StringReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@SuppressWarnings({"CallToPrintStackTrace", "unused"})
public class ServerController implements IServerListener, IServerController {
    private static ServerController instance;

    @FXML
    private ListView<ChatDisplayData> chatListView;
    @FXML
    private VBox messagesArea;
    @FXML
    private ScrollPane messageScrollPane;

    private IServer server;
    private int currentChatId = -1;
    private LocalDate currentDisplayedDate = null;
    private String usernameFirst;
    private String usernameSecond;

    private ServerController(IServer server) {
        this.server = server;
    }

    public ServerController() {}
    public static synchronized ServerController getInstance(IServer server) {
        if (instance == null) {
            instance = new ServerController(server);
        }
        return instance;
    }

    @FXML
    public void initialize() {
        chatListView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<ChatDisplayData> call(ListView<ChatDisplayData> listView) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(ChatDisplayData item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            setText(item.toString());
                        }
                    }
                };
            }
        });

        String stylesheet = Objects.requireNonNull(getClass().getResource("/annaKnysh/serverside/css/chat.css")).toExternalForm();

        chatListView.getStylesheets().add(stylesheet);
        messageScrollPane.getStylesheets().add(stylesheet);
        chatListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                currentChatId = newSelection.chatId();
                usernameFirst = newSelection.usernameFirst();
                usernameSecond = newSelection.usernameSecond();
                loadMessagesForChat(newSelection.chatId());
            } else {
                currentChatId = -1;
                usernameFirst = null;
                usernameSecond = null;
            }
        });

        messageScrollPane.setFitToWidth(true);
    }

    private void loadMessagesForChat(int chatId) {
        messagesArea.getChildren().clear();
        processMessagesResponse(server.processServerGetMessagesRequest(chatId));
    }
    private void processMessagesResponse(String xmlMessage) {
        try {
            JAXBContext context = JAXBContext.newInstance(MessagesResponse.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            StringReader reader = new StringReader(xmlMessage);
            MessagesResponse response = (MessagesResponse) unmarshaller.unmarshal(reader);
            updateMessagesArea(response.getMessages());
        } catch (Exception e) {
        }
    }

    private void updateMessagesArea(List<Message> messages) {
        Platform.runLater(() -> {
            messagesArea.getChildren().clear();
            currentDisplayedDate = null;

            if (messages != null) {
                for (Message message : messages) {
                    displayMessage(message);
                }
            }
        });
    }

    public void displayMessage(Message message) {
        Platform.runLater(() -> {
            if (isCurrentChat(message.getChatId())) {
                LocalDateTime timestamp = message.getTimestamp();
                LocalDate messageDate = timestamp.toLocalDate();
                addDateLabelIfNecessary(messageDate);
                addMessageBox(message, timestamp);
            }
        });
    }

    private void addDateLabelIfNecessary(LocalDate messageDate) {
        if (currentDisplayedDate == null || !currentDisplayedDate.equals(messageDate)) {
            currentDisplayedDate = messageDate;
            // Create the date label using the corrected method
            HBox dateLabel = InterfaceFactory.createDateLabel(messageDate);
            messagesArea.getChildren().add(dateLabel);
        }
    }

    private void addMessageBox(Message message, LocalDateTime timestamp) {
        // Assume usernameFirst is known and passed to addMessageBox
        VBox messageBox = InterfaceFactory.createMessageBox(message, timestamp, usernameFirst);
        messagesArea.getChildren().add(messageBox);
    }

    public void setServer(IServer server) {
        this.server = server;
        server.addListener(this);
    }

    @Override
    public void updateChatList(List<ChatDisplayData> chats) {
        Platform.runLater(() -> {
            chatListView.getItems().clear();
            chatListView.getItems().addAll(chats);
        });
    }

    @Override
    public void onMessage(WebSocket conn, String input) throws JAXBException {
        if (input.contains("<userConnectionInfo>")) {
            handleUserConnectionInfo(conn, input);
        } else if (input.contains("<message>")) {
            handleMessage(conn, input);
        } else if (input.contains("<chatRequest>")) {
            handleChatRequest(conn, input);
        } else if (input.contains("<authenticationQuery>")) {
            handleAuthRequest(conn, input);
        } else {
            conn.send("Unsupported input format");
        }
    }

    void handleAuthRequest(WebSocket conn, String input) {
        try {
            JAXBContext context = JAXBContext.newInstance(AuthenticationQuery.class, AuthenticationAnswer.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            StringReader reader = new StringReader(input);
            AuthenticationQuery authenticationQuery = (AuthenticationQuery) unmarshaller.unmarshal(reader);
            boolean authenticated = DatabaseManager.checkOrCreateUser(authenticationQuery.getUsername());
            System.out.println(authenticated);
            AuthenticationAnswer authenticationAnswer = new AuthenticationAnswer(authenticated, authenticationQuery.getUsername());
            String authResponseXml = XMLUtility.toXML(authenticationAnswer);
            System.out.println(authResponseXml);
            conn.send(authResponseXml);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    private void handleChatRequest(WebSocket conn, String input) throws JAXBException {
        ChatRequest chatRequest = XMLUtility.fromXML(input, ChatRequest.class);
        switch (chatRequest.getAction()) {
            case "createChat":
                processChatCreationRequest(conn, chatRequest);
                break;
            case "updateChat":
                processChatUpdateRequest(conn, chatRequest);
                break;
            case "deleteChat":
                processChatDeletionRequest(conn, chatRequest);
                break;
            case "getChats":
                processGetChatsRequest(conn, chatRequest);
                break;
            case "getMessages":
                processGetMessagesRequest(conn, chatRequest);
                break;
            default:
                conn.send("Unsupported chat request action: " + chatRequest.getAction());
                break;
        }
    }

    private void processGetMessagesRequest(WebSocket conn, ChatRequest cR) {
        server.processGetMessagesRequest(conn, cR);
    }

    private void processGetChatsRequest(WebSocket conn, ChatRequest cR) {
        server.processGetChatsRequest(conn, cR);
    }

    private void processChatDeletionRequest(WebSocket conn, ChatRequest cR) {
        server.processChatDeletionRequest(conn, cR);
    }

    private void processChatUpdateRequest(WebSocket conn, ChatRequest cR) {
        server.processChatUpdateRequest(conn, cR);
    }

    private void processChatCreationRequest(WebSocket conn, ChatRequest cR) {
        server.processChatCreationRequest(conn, cR);
    }

    private void handleMessage(WebSocket conn, String input) {
        try {
            Message msg = XMLUtility.fromXML(input, Message.class);

            server.notifyListenersWithMessage(msg);
            server.sendDirectMessage(msg);
            server.recordMessageInDatabase(msg);
        } catch (JAXBException e) {
            System.err.println("Error parsing XML: " + e.getMessage());
            conn.send("Error processing your XML input");
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            conn.send("An unexpected error occurred");
        }
    }

    public boolean isCurrentChat(int chatId) {
        return chatId == currentChatId;
    }

    private void handleUserConnectionInfo(WebSocket conn, String input) throws JAXBException {
        UserConnectionInfo info = XMLUtility.fromXML(input, UserConnectionInfo.class);
        server.updateDatabase(info.getUsername(), server.getPortConn(conn));
    }



    @Override
    public void clearListeners() {
        server.clearListeners();
    }
}
