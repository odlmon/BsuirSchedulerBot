package command;

import database.DatabaseManager;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import schedule.ScheduleHandler;

public class AddCommand extends BotCommand {

    public AddCommand() {
        super("add", "Добавьте номер группы в список для быстрого доступа");
    }

    private final ScheduleHandler scheduleHandler = new ScheduleHandler();

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        var answer = new SendMessage().setChatId(chat.getId());
        if (strings.length != 1) {
            answer.setText("Формат вызова команды /add <номер_группы>");
        } else {
            String input = strings[0];
//          TODO: add check records for collisions and add limit ~10
            if (input.matches("\\d{6}") && scheduleHandler.isGroupExists(input)) {
                DatabaseManager.addUserGroup(chat.getId(), input);
                answer.setText("Группа успешно добавлена в Ваш список");
            } else {
                answer.setText("Такой группы не существует");
            }
        }
        try {
            absSender.execute(answer);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
