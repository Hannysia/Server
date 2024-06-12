package annaKnysh.serverside.chat;

import annaKnysh.serverside.xml.message.Message;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class InterfaceFactory {
    //region DateLabel and DateBox
    public static HBox createDateLabel(LocalDate date) {
        String formattedDate = formatDate(date);
        return createDateLabel(formattedDate);
    }

    public static HBox createDateLabel(String formattedDate) {
        Label dateLabel = new Label(formattedDate);
        dateLabel.setAlignment(Pos.CENTER);
        dateLabel.getStyleClass().add("date-label");  // CSS class for date labels
        HBox dateBox = new HBox();
        dateBox.setAlignment(Pos.CENTER);
        dateBox.getChildren().add(dateLabel);
        return dateBox;
    }
    //endregion

    //region Message Input Field
    public static TextArea createMessageField() {
        TextArea messageField = new TextArea();
        messageField.setWrapText(true);
        messageField.getStyleClass().add("messageField");  // CSS class for message fields
        return messageField;
    }
    //endregion

    //region Message
    public static VBox createMessageBox(Message message, LocalDateTime timestamp, String usernameFirst) {
        VBox messageBox = new VBox();
        Label senderLabel = new Label(message.getFrom());
        senderLabel.getStyleClass().add("userName");  // CSS class for username labels

        Text messageText = new Text(message.getContent());
        messageText.setWrappingWidth(300);
        messageText.getStyleClass().add("textBodyMessage");  // CSS class for message text

        TextFlow messageFlow = new TextFlow(messageText);
        messageFlow.setMaxWidth(300);
        messageFlow.getStyleClass().add("message-text-flow");  // CSS class for message text flow

        Label timeLabel = new Label(timestamp.format(DateTimeFormatter.ofPattern("HH:mm")));
        timeLabel.getStyleClass().add("messageTime");  // CSS class for time labels

        HBox messageContainer = new HBox();
        messageContainer.setMaxWidth(300);

        StackPane textContainer = new StackPane(messageFlow);
        textContainer.setMaxWidth(300);

        if (message.getFrom().equals(usernameFirst)) {
            messageBox.setAlignment(Pos.CENTER_LEFT);
            messageContainer.setAlignment(Pos.CENTER_LEFT);
            textContainer.getStyleClass().add("bodyMessageLeft");  // CSS class for left-aligned messages
            messageContainer.getChildren().add(textContainer);
        } else {
            messageBox.setAlignment(Pos.CENTER_RIGHT);
            messageContainer.setAlignment(Pos.CENTER_RIGHT);
            textContainer.getStyleClass().add("bodyMessageRight");  // CSS class for right-aligned messages
            messageContainer.getChildren().add(textContainer);
        }

        messageContainer.setStyle("-fx-spacing: 10");
        messageBox.getChildren().addAll(senderLabel, messageContainer, timeLabel);
        return messageBox;
    }

    public static void addGeneralBox(VBox messagesArea, VBox messageBox, boolean isLeft) {
        HBox generalBox = new HBox();
        if (isLeft) {
            generalBox.setAlignment(Pos.CENTER_LEFT);
        } else {
            generalBox.setAlignment(Pos.CENTER_RIGHT);
        }
        generalBox.getChildren().add(messageBox);
        messagesArea.getChildren().add(generalBox);
    }
    //endregion

    //region Helpers
    private static String formatDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM", Locale.getDefault());
        return date.format(formatter);
    }
    //endregion
}
