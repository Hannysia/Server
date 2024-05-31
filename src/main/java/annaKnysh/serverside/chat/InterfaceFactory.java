package annaKnysh.serverside.chat;

import annaKnysh.serverside.xml.message.Message;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@SuppressWarnings("unused")
public class InterfaceFactory {

    public static String formatDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM", Locale.getDefault());
        return date.format(formatter);
    }

    public static Label createDateLabel(String formattedDate) {
        Label dateLabel = new Label(formattedDate);
        dateLabel.setAlignment(Pos.CENTER);
        dateLabel.getStyleClass().add("date-label");
        return dateLabel;
    }

    public static HBox createDateBox(Label dateLabel) {
        HBox dateBox = new HBox();
        dateBox.setAlignment(Pos.CENTER);
        dateBox.getChildren().add(dateLabel);
        return dateBox;
    }

    public static VBox createMessageBox(Message message, LocalDateTime timestamp, String usernameFirst) {
        VBox messageBox = new VBox();
        Label senderLabel = new Label(message.getFrom());
        senderLabel.getStyleClass().add("sender-label");

        Text messageText = new Text(message.getContent());
        messageText.setWrappingWidth(300);
        messageText.getStyleClass().add("message-text");

        TextFlow messageFlow = new TextFlow(messageText);
        messageFlow.setMaxWidth(300);
        messageFlow.getStyleClass().add("message-text-flow");

        Label timeLabel = new Label(timestamp.format(DateTimeFormatter.ofPattern("HH:mm")));
        timeLabel.getStyleClass().add("time-label");

        HBox messageContainer = new HBox();
        messageContainer.setMaxWidth(300);

        StackPane textContainer = new StackPane(messageFlow);
        textContainer.setMaxWidth(300);

        messageContainer.getChildren().add(textContainer);

        if (message.getFrom().equals(usernameFirst)) {
            messageBox.setAlignment(Pos.CENTER_LEFT);
            messageContainer.setAlignment(Pos.CENTER_LEFT);
            textContainer.getStyleClass().add("text-container-left");
        } else {
            messageBox.setAlignment(Pos.CENTER_RIGHT);
            messageContainer.setAlignment(Pos.CENTER_RIGHT);
            textContainer.getStyleClass().add("text-container-right");
        }

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
}
