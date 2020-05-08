package command;

import database.DatabaseManager;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

public class DeleteCommand extends BotCommand {

    public DeleteCommand() {
        super("delete", "Удаляет группу из списка");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        var answer = new SendMessage()
                .setChatId(chat.getId());
        List<String> list = DatabaseManager.getAddedGroups(chat.getId());
        if (list == null || list.size() == 0) {
            answer.setText("Нечего удалять");
        } else {
            answer.setText("Список групп, доступных для удаления:");
            var markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
            list.forEach(item -> {
                List<InlineKeyboardButton> rowInline = new ArrayList<>();
                rowInline.add(new InlineKeyboardButton()
                        .setText(item)
                        .setCallbackData("delete_" + item));
                rowsInline.add(rowInline);
            });
            markupInline.setKeyboard(rowsInline);
            answer.setReplyMarkup(markupInline);
        }
        try {
            absSender.execute(answer);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
