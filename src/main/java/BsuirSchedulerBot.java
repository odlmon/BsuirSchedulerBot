import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.Random;

public class BsuirSchedulerBot extends TelegramLongPollingBot {

    private RequestHandler requestHandler = new RequestHandler();

    private void sendMessage(Update update, String text) {
        SendMessage message = new SendMessage() // Create a SendMessage object with mandatory fields
                .setChatId(update.getMessage().getChatId())
                .setText(text);
        try {
            execute(message); // Call method to send the message
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendPicture(Update update, String id) {
        SendPhoto message = new SendPhoto()
                .setChatId(update.getMessage().getChatId())
                .setPhoto(new File("C:\\Users\\Mi\\Pictures\\Memes\\" + id + ".jfif"));
        try {
            execute(message); // Call method to send the message
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public String getBotToken() {
        return "1128392990:AAHS_fOam2gH7JiZPjNo0ELFN2kHXqprhmM";
    }

    public void onUpdateReceived(Update update) {
        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            User user = update.getMessage().getFrom();
            System.out.println(user.getFirstName() + " " + user.getLastName() + ": " +
                    update.getMessage().getText());
            String text = update.getMessage().getText();
            if (text.toLowerCase().matches("аче\\)*")) {
                sendMessage(update, "Ниче");
            } else if (text.toLowerCase().matches("оло\\)*")) {
                sendMessage(update, "Дроу)))");
            } else if (text.toLowerCase().matches("ты че\\)*")) {
                sendMessage(update, "Аче");
            } else if (text.matches("/start")) {
                sendMessage(update, "Стартуем!");
                SendSticker message = new SendSticker()
                        .setChatId(update.getMessage().getChatId())
                        .setSticker("CAACAgQAAxkBAAI22l55EcRaBMQT8gJgR6LUZKzTqB_CAAJkAAOodxIAAQT5BNcd-V0GGAQ");
                try {
                    execute(message); // Call method to send the message
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                sendMessage(update, "Заплати номером группы...");
            } else if (!text.toLowerCase().matches("\\d\\d\\d\\d\\d\\d")) {
                int random = new Random().nextInt(100);
                sendPicture(update, Integer.toString(random));
            } else {
                SendMessage message = new SendMessage() // Create a SendMessage object with mandatory fields
                        .setChatId(update.getMessage().getChatId())
                        .setText(requestHandler.getScheduleResponse(update.getMessage().getText()));
                try {
                    execute(message); // Call method to send the message
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        } else if (update.hasMessage() && update.getMessage().hasSticker()) {
            SendSticker message = new SendSticker() // Create a SendMessage object with mandatory fields
                    .setChatId(update.getMessage().getChatId())
                    .setSticker(String.valueOf(update.getMessage().getSticker().getFileId()));
            try {
                execute(message); // Call method to send the message
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    public String getBotUsername() {
        return "BsuirSchedulerBot";
    }
}
