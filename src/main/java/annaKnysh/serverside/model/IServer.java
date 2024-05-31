package annaKnysh.serverside.model;

import annaKnysh.serverside.controller.IServerListener;
import annaKnysh.serverside.xml.chat.ChatRequest;
import annaKnysh.serverside.xml.message.Message;
import java.util.Set;
import org.java_websocket.WebSocket;

@SuppressWarnings("unused")
public interface IServer {
   void addListener(IServerListener listener);

   void processGetMessagesRequest(WebSocket conn, ChatRequest chatRequest);

   void processGetChatsRequest(WebSocket conn, ChatRequest chatRequest);

   void processChatDeletionRequest(WebSocket conn, ChatRequest chatRequest);

   void processChatUpdateRequest(WebSocket conn, ChatRequest chatRequest);

   void processChatCreationRequest(WebSocket conn, ChatRequest chatRequest);

   String processServerGetMessagesRequest(int chatId);

   void notifyListenersWithMessage(Message msg);

   void sendDirectMessage(Message msg);

   void recordMessageInDatabase(Message msg);

   int getPortConn(WebSocket conn);

   void updateDatabase(String username, int portConn);

    void clearListeners();

   Set<IServerListener> getListeners();

   void startserver();

   void stopserver() throws InterruptedException;
}
