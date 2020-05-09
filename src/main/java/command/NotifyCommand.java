package command;

import com.vdurmont.emoji.EmojiParser;
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

public class NotifyCommand extends BotCommand {

    private static final String positive = ":white_check_mark:";
    private static final String negative = ":negative_squared_cross_mark:";

    public NotifyCommand() {
        super("notify", "Установить оповещение для группы из списка");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        var answer = new SendMessage()
                .setChatId(chat.getId());
        List<String> groups = DatabaseManager.getAddedGroups(chat.getId());
        if (groups == null || groups.size() == 0) {
            answer.setText("Ваш список групп пуст, нечего помечать");
        } else {
            answer.setText("Ваш список групп со статусом уведомлений:");
            var markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
            groups.forEach(group -> {
                List<InlineKeyboardButton> rowInline = new ArrayList<>();
                boolean isNotified = DatabaseManager.isGroupNotified(chat.getId(), group);
                rowInline.add(new InlineKeyboardButton()
                        .setText(group + EmojiParser.parseToUnicode(isNotified ? positive : negative))
                        .setCallbackData("notify_%s_%s".formatted(group, isNotified)));
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
