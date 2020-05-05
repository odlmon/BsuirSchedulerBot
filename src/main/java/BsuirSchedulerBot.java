import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BsuirSchedulerBot extends TelegramLongPollingBot {

    private final ScheduleHandler scheduleHandler = new ScheduleHandler();

    public String getBotToken() {
        return "1128392990:AAHS_fOam2gH7JiZPjNo0ELFN2kHXqprhmM";
    }

    private InlineKeyboardMarkup generateInlineKeyBoard(String answer, String groupNumber) {
        String week = answer
                .lines()
                .filter(str -> str.startsWith("Выбранная неделя:"))
                .findFirst().get().split(" ")[2];
        var weeks = new ArrayList<>(Arrays.asList("1", "2", "3", "4"));
        weeks.remove(week);
        System.out.println(week);
        var markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        weeks.forEach(item -> rowInline.add(new InlineKeyboardButton()
                    .setText(item)
                    .setCallbackData("full_" + item + "_" + groupNumber)));
        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    public void onUpdateReceived(Update update) {
        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            User user = update.getMessage().getFrom();
            Long chatId = update.getMessage().getChatId();
            String text = update.getMessage().getText();
            System.out.println(user.getFirstName() + " " + user.getLastName() + ": " + text);
            String answer = scheduleHandler.getScheduleString(text, null, SchedulePeriod.DAY);
            SendMessage message;
            if (answer == null) {
                message = new SendMessage(chatId, "Такой группы не существует");
            } else {
                message = new SendMessage(chatId, answer);
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
            if (callbackData.startsWith("week")) {
                String groupNumber = callbackData.split("_")[1];
                System.out.println(groupNumber);
                String answer = scheduleHandler.getScheduleString(groupNumber,
                        null, SchedulePeriod.WEEK);
                var newMessage = new EditMessageText()
                        .setChatId(chatId)
                        .setMessageId((int) messageId)
                        .setText(answer);
                InlineKeyboardMarkup markupInline = generateInlineKeyBoard(answer, groupNumber);
                newMessage.setReplyMarkup(markupInline);
                try {
                    execute(newMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (callbackData.startsWith("full")) {
                String week = callbackData.split("_")[1];
                String groupNumber = callbackData.split("_")[2];
                System.out.println(week + " " + groupNumber);
                String answer = scheduleHandler.getScheduleString(groupNumber,
                        week, SchedulePeriod.WEEK);
                var newMessage = new EditMessageText()
                        .setChatId(chatId)
                        .setMessageId((int) messageId)
                        .setText(answer);
                InlineKeyboardMarkup markupInline = generateInlineKeyBoard(answer, groupNumber);
                newMessage.setReplyMarkup(markupInline);
                try {
                    execute(newMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }

//            if (update.getMessage().getText().equals("repeat")) {
//                Runnable task = () -> {
//                    SendMessage message = new SendMessage()
//                            .setChatId(update.getMessage().getChatId())
//                            .setText("repeat every 5 sec");
//                    try {
//                        execute(message); // Call method to send the message
//                    } catch (TelegramApiException e) {
//                        e.printStackTrace();
//                    }
//                };
//                ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
//                ses.scheduleAtFixedRate(task, 0, 5, TimeUnit.SECONDS);
//            } else {
//                SendMessage message = new SendMessage() // Create a SendMessage object with mandatory fields
//                        .setChatId(update.getMessage().getChatId())
//                        .setText();
//                try {
//                    execute(message); // Call method to send the message
//                } catch (TelegramApiException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
    }

    public String getBotUsername() {
        return "Расписание БГУИР";
    }
}
