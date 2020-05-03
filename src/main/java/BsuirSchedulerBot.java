import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

public class BsuirSchedulerBot extends TelegramLongPollingBot {

    private final ScheduleHandler scheduleHandler = new ScheduleHandler();

    public String getBotToken() {
        return "1128392990:AAHS_fOam2gH7JiZPjNo0ELFN2kHXqprhmM";
    }

    public void onUpdateReceived(Update update) {
        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            User user = update.getMessage().getFrom();
            String text = update.getMessage().getText();
            System.out.println(user.getFirstName() + " " + user.getLastName() + ": " + text);
            String answer = scheduleHandler.getScheduleString(text, null, SchedulePeriod.DAY);
            SendMessage message;
            if (answer == null) {
                message = new SendMessage()
                        .setChatId(update.getMessage().getChatId())
                        .setText("Такой группы не существует");
            } else {
                message = new SendMessage()
                        .setChatId(update.getMessage().getChatId())
                        .setText(answer);
                InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                List<InlineKeyboardButton> rowInline = new ArrayList<>();
                rowInline.add(new InlineKeyboardButton()
                        .setText("Расписание на неделю")
                        .setCallbackData("week_" + text));
                rowsInline.add(rowInline);
                markupInline.setKeyboard(rowsInline);
                message.setReplyMarkup(markupInline);
            }
            try {
                execute(message); // Call method to send the message
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            String text = callbackData.split("_")[1];
            System.out.println(text);
            if (callbackData.startsWith("week")) {
                String answer = scheduleHandler.getScheduleString(text, null, SchedulePeriod.WEEK);
                EditMessageText newMessage = new EditMessageText()
                        .setChatId(chatId)
                        .setMessageId((int) messageId)
                        .setText(answer);
                try {
                    execute(newMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getBotUsername() {
        return "Расписание БГУИР";
    }
}
