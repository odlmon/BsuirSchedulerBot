package command;

import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class HelpCommand extends BotCommand {

    public HelpCommand() {
        super("help", "Выводит основные команды и их назначение");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        String help = "Получить <b>раcписание</b> можно просто отправкой <b>номера группы</b>.\n\n" +
                "<b>Основные команды:</b>\n" +
                "/add &lt;<i>номер_группы</i>&gt; - Добавьте номер группы в список для быстрого доступа\n" +
                "/groups - Выводит список добавленных групп\n" +
                "/delete - Удаляет группу из списка\n" +
                "/notify - Установить оповещение для группы из списка\n" +
                "/help - Выводит основные команды и их назначение";
        var answer = new SendMessage()
                .setChatId(chat.getId())
                .setText(help)
                .enableHtml(true);
        try {
            absSender.execute(answer);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
