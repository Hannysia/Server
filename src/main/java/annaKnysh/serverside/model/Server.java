package annaKnysh.serverside.model;

import annaKnysh.serverside.chat.ChatDisplayData;
import annaKnysh.serverside.controller.IServerController;
import annaKnysh.serverside.controller.IServerListener;
import annaKnysh.serverside.database.DatabaseManager;
import annaKnysh.serverside.xml.UserConnectionInfo;
import annaKnysh.serverside.xml.XMLUtility;
import annaKnysh.serverside.xml.chat.Chat;
import annaKnysh.serverside.xml.chat.ChatListResponse;
import annaKnysh.serverside.xml.chat.ChatRequest;
import annaKnysh.serverside.xml.message.Message;
import annaKnysh.serverside.xml.message.MessagesResponse;
import jakarta.xml.bind.*;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import java.net.InetSocketAddress;
import java.util.*;


@SuppressWarnings({"unused", "CallToPrintStackTrace"})
public class Server extends WebSocketServer implements IServer {
    private static IServer instance;
    final Set<WebSocket> connections;
    final Set<IServerListener> listeners;
    final DatabaseManager dbManager;

    public Server(int port) {
        super(new InetSocketAddress(port));
        connections = new HashSet<>();
        listeners = new HashSet<>();
        dbManager = DatabaseManager.getInstance();
    }

    public static synchronized IServer getInstance(int port) {
        if (instance == null) {
            instance = new Server(port);
        }
        return instance;
    }


    @Override
    public void onStart() {
        String logMessage = "Server started successfully on port: " + getPort();
        System.out.println(logMessage);
        updateChatList();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        connections.add(conn);
        String logMessage = "New connection: " + getPortConn(conn);
        System.out.println(logMessage);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        connections.remove(conn);
        String logMessage = "Closed connection: " + getPortConn(conn);
        System.out.println(logMessage);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        if (conn != null) {
            connections.remove(conn);
        }
        int port = conn != null ? conn.getRemoteSocketAddress().getPort() : 0;
        String logMessage = "Error from " + port + ": " + ex.getMessage();
        System.out.println(logMessage);
    }

    void updateChatList() {
        List<ChatDisplayData> chats = dbManager.getAllChats();
        notifyListenersAboutChats(chats);
    }

    void notifyListenersAboutChats(List<ChatDisplayData> chats) {
        for (IServerListener listener : listeners) {
            listener.updateChatList(chats);
        }
    }

    @Override
    public void addListener(IServerListener listener) {
        listeners.add(listener);
        System.out.println("Listener added: " + listener.getClass().getName());
    }

    void notifyListeners(WebSocket conn, String message) {
        listeners.forEach(listener -> {
            try {
                listener.onMessage(conn, message);
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void notifyListenersWithMessage(Message message) {
        listeners.forEach(listener -> {
            if (listener instanceof IServerController controller) {
                if (controller.isCurrentChat(message.getChatId())) {
                    controller.displayMessage(message);
                }
            }
        });
    }

    public int getPortConn(WebSocket conn) {
        return conn.getRemoteSocketAddress().getPort();
    }

    @Override
    public void onMessage(WebSocket conn, String input) {
        notifyListeners(conn, input);
    }

    void handleUserConnectionInfo(WebSocket conn, String input) throws JAXBException {
        UserConnectionInfo info = XMLUtility.fromXML(input, UserConnectionInfo.class);
        dbManager.updateConnectionInfo(info.getUsername(), getPortConn(conn));
    }

    public void recordMessageInDatabase(Message msg) {
        dbManager.recordMessage(msg);
    }

    public WebSocket getConnection() {
        for (WebSocket conn : this.connections) {
            if (conn != null && conn.isOpen()) {
                return conn;
            }
        }
        return null;
    }

    @Override
    public void processGetMessagesRequest(WebSocket conn, ChatRequest chatRequest) {
        List<Message> messages = dbManager.getMessagesFromDatabase(chatRequest.getChatId());
        try {
            String messagesXml = XMLUtility.toXML(new MessagesResponse(messages));
            conn.send(messagesXml);
        } catch (JAXBException e) {
            conn.send("Error serializing messages");
        }
    }

    @Override
    public String processServerGetMessagesRequest(int chatId) {
        try{
        return XMLUtility.toXML(new MessagesResponse(dbManager.getMessagesFromDatabase(chatId)));}
        catch (JAXBException e){
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void processChatUpdateRequest(WebSocket conn, ChatRequest chatRequest) {
        boolean success = dbManager.updateChat(chatRequest.getChatId(), chatRequest.getParameters());
        if (success) {
            conn.send("Chat updated successfully.");
        } else {
            conn.send("Failed to update chat.");
        }
        updateChatList();
    }

    @Override
    public void processChatDeletionRequest(WebSocket conn, ChatRequest chatRequest) {
        boolean successDeletingChat = dbManager.deleteChat(chatRequest.getChatId());
        boolean successDeletingMessages = dbManager.deleteMessagesByChatId(chatRequest.getChatId());
        if (successDeletingChat && successDeletingMessages) {
            conn.send("Chat deleted successfully.");
        } else {
            conn.send("Failed to delete chat.");
        }
        updateChatList();
    }

    @Override
    public void processGetChatsRequest(WebSocket conn, ChatRequest chatRequest) {
        List<Chat> chats = dbManager.getUserChats(chatRequest.getUsername1());
        try {
            String responseXml = XMLUtility.toXML(new ChatListResponse(chats));
            conn.send(responseXml);
        } catch (JAXBException e) {
            String logmessage = "Error serializing chat list: " + e.getMessage();
            conn.send("Error processing your request for chat list");
        }
        updateChatList();
    }

    @Override
    public void processChatCreationRequest(WebSocket conn, ChatRequest chatRequest) {
        if(!Objects.equals(chatRequest.getUsername1(), chatRequest.getUsername2())){
            if (dbManager.userExists(chatRequest.getUsername1()) && dbManager.userExists(chatRequest.getUsername2())) {
                boolean chatExists = dbManager.chatExists(chatRequest.getUsername1(), chatRequest.getUsername2());
                if (chatExists) {
                    conn.send("Chat already exists between " + chatRequest.getUsername1() + " and " + chatRequest.getUsername2() + ". Please find it in your chat list.");
                } else {
                    boolean chatCreated = dbManager.createChat(chatRequest.getUsername1(), chatRequest.getUsername2());
                    if (chatCreated) {
                        conn.send("Chat created successfully between " + chatRequest.getUsername1() + " and " + chatRequest.getUsername2());
                    } else {
                        conn.send("Failed to create chat or chat already exists.");
                    }
                }
                updateChatList();
            } else {
                conn.send("Problems with usernames");
            }}
    }

    public boolean userExists(String username) {
        return dbManager.userExists(username);
    }

    public void sendDirectMessage(Message msg) {
        String recipientUsername = msg.getTo();
        int recipientPort = dbManager.findPortByUsername(recipientUsername);

        connections.stream()
                .peek(ws -> System.out.println("Checking port: " + ws.getRemoteSocketAddress().getPort()))
                .filter(ws -> ws.getRemoteSocketAddress().getPort() == recipientPort)
                .findFirst()
                .ifPresent(ws -> {
                    try {
                        ws.send(XMLUtility.toXML(msg));
                    } catch (JAXBException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public List<Chat> getUserChats(String username) {
        return dbManager.getUserChats(username);
    }

    public int findPortByUsername(String username) {
        return dbManager.findPortByUsername(username);
    }

    public void updateDatabase(String username, int port) {
        dbManager.updateConnectionInfo(username, port);
    }

    @Override
    public void clearListeners() {
        listeners.clear();
    }

    public Set<IServerListener> getListeners() {
        return listeners;
    }

    public void stopserver() throws InterruptedException {
        this.stop();
    }
    public void startserver() {
        this.start();
    }
}
