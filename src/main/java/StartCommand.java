import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class StartCommand extends BotCommand {

    public StartCommand() {
        super("start", "С помощью этой комманды вы можете начать работу с ботом");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        var answer = new SendMessage()
                .setChatId(chat.getId())
                .setText("Это бот с расписанием. Для начала отправь номер своей группы.");
        try {
            absSender.execute(answer);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
