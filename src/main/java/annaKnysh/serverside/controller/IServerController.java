package annaKnysh.serverside.controller;

import annaKnysh.serverside.chat.ChatDisplayData;
import annaKnysh.serverside.model.IServer;
import annaKnysh.serverside.xml.message.Message;
import jakarta.xml.bind.JAXBException;
import org.java_websocket.WebSocket;

import java.util.List;

@SuppressWarnings("unused")
public interface IServerController {
    void setServer(IServer server);
    void updateChatList(List<ChatDisplayData> chats);
    void onMessage(WebSocket conn, String input) throws JAXBException;
    void clearListeners();
    boolean isCurrentChat(int chatId);
    void displayMessage(Message message);
}
