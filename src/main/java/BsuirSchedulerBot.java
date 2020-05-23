import command.*;
import database.DatabaseManager;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import schedule.ScheduleHandler;
import schedule.SchedulePeriod;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BsuirSchedulerBot extends TelegramLongPollingCommandBot {

    private static final String PING_URL = "https://www.google.com";
    private static final int RATE = 120_000;
    private final ScheduleHandler scheduleHandler = new ScheduleHandler();
    public static Map<Long, Map<String, ScheduledExecutorService>> notifiedGroups = new HashMap<>();

    public String getBotToken() {
        return "1128392990:AAHS_fOam2gH7JiZPjNo0ELFN2kHXqprhmM";
    }

    public BsuirSchedulerBot() {
        register(new StartCommand());
        register(new AddCommand());
        register(new GroupsCommand());
        register(new DeleteCommand());
        register(new NotifyCommand());
        register(new HelpCommand());
        restoreNotifiedGroups();
    }

    public static void startAsync() {
        Runnable task = () -> {
            try {
                URL url = new URL(PING_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                System.out.println("Ping " + url.getHost() + ", OK: response code " + connection.getResponseCode());
                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
        ses.scheduleAtFixedRate(task, 0, RATE, TimeUnit.MILLISECONDS);
    }

    private void restoreNotifiedGroups() {
        Map<Long, List<String>> rawGroups = DatabaseManager.allGroupNotifications();
        rawGroups.forEach((key, value) -> {
            Map<String, ScheduledExecutorService> notifyServices = new HashMap<>();
            value.forEach(group -> notifyServices.put(group, getService(key, group)));
            notifiedGroups.put(key, notifyServices);
        });
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

    private void nonCommandCall(Update update) {
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
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void weekCallback(String callbackData, long messageId, long chatId) {
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
    }

    private void fullCallback(String callbackData, long messageId, long chatId) {
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

    private void groupsCallback(String callbackData, long messageId, long chatId) {
        String groupNumber = callbackData.split("_")[1];
        String answer = scheduleHandler.getScheduleString(groupNumber, null, SchedulePeriod.DAY);
        EditMessageText newMessage;
        if (answer == null) {
            newMessage = new EditMessageText()
                    .setChatId(chatId)
                    .setMessageId((int) messageId)
                    .setText("Такой группы не существует");
        } else {
            newMessage = new EditMessageText()
                    .setChatId(chatId)
                    .setMessageId((int) messageId)
                    .setText(answer);
            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            rowInline.add(new InlineKeyboardButton()
                    .setText("Расписание на неделю")
                    .setCallbackData("week_" + groupNumber));
            rowsInline.add(rowInline);
            markupInline.setKeyboard(rowsInline);
            newMessage.setReplyMarkup(markupInline);
        }
        try {
            execute(newMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void deleteCallback(String callbackData, long messageId, long chatId) {
        String groupNumber = callbackData.split("_")[1];
        var newMessage = new EditMessageText()
                .setChatId(chatId)
                .setMessageId((int) messageId);
        if (DatabaseManager.deleteUserGroup(chatId, groupNumber)) {
            newMessage.setText("Группа успешно удалена");
        } else {
            newMessage.setText("Не удалось удалить группу");
        }
        try {
            execute(newMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private Runnable getTask(long chatId, String groupNumber) {
        return () -> {
            String answer = scheduleHandler.getScheduleString(groupNumber, null, SchedulePeriod.DAY);
            var message = new SendMessage(chatId, answer);
            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            rowInline.add(new InlineKeyboardButton()
                    .setText("Расписание на неделю")
                    .setCallbackData("week_" + groupNumber));
            rowsInline.add(rowInline);
            markupInline.setKeyboard(rowsInline);
            message.setReplyMarkup(markupInline);
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        };
    }

    private ScheduledExecutorService getService(long chatId, String groupNumber) {
        ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
        var date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        int dayInSeconds = 24 * 60 * 60;
        int delay = dayInSeconds - (hours * 60 + minutes) * 60;
        ses.scheduleAtFixedRate(getTask(chatId, groupNumber), delay, dayInSeconds, TimeUnit.SECONDS);
        return ses;
    }

    private void notifyCallback(String callbackData, long messageId, long chatId) {
        String groupNumber = callbackData.split("_")[1];
        boolean isNotified = Boolean.parseBoolean(callbackData.split("_")[2]);
        if (isNotified) {
            DatabaseManager.deleteGroupNotification(chatId, groupNumber);
            notifiedGroups.get(chatId).get(groupNumber).shutdown();
            notifiedGroups.get(chatId).clear();
        } else {
            DatabaseManager.addGroupNotification(chatId, groupNumber);
            ScheduledExecutorService ses = getService(chatId, groupNumber);
            if (notifiedGroups.containsKey(chatId)) {
                notifiedGroups.get(chatId).put(groupNumber, ses);
            } else {
                Map<String, ScheduledExecutorService> notifyServices = new HashMap<>();
                notifyServices.put(groupNumber, ses);
                notifiedGroups.put(chatId, notifyServices);
            }
        }
        var newMessage = new EditMessageText()
                .setChatId(chatId)
                .setMessageId((int) messageId)
                .setText(String.format("Вы %s группы %s", isNotified ? "отписались от расписания" :
                                "подписались на расписание", groupNumber));
        try {
            execute(newMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void processNonCommandUpdate(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            nonCommandCall(update);
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            if (callbackData.startsWith("week")) {
                weekCallback(callbackData, messageId, chatId);
            } else if (callbackData.startsWith("full")) {
                fullCallback(callbackData, messageId, chatId);
            } else if (callbackData.startsWith("groups")) {
                groupsCallback(callbackData, messageId, chatId);
            } else if (callbackData.startsWith("delete")) {
                deleteCallback(callbackData, messageId, chatId);
            } else if (callbackData.startsWith("notify")) {
                notifyCallback(callbackData, messageId, chatId);
            }
        }
    }

    public String getBotUsername() {
        return "@bsuirschedulerbot";
    }
}
