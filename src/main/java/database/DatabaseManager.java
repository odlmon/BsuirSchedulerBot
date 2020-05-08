package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private static final String url = "jdbc:mysql://192.168.99.100:3306/bot_users";
    private static final String userName = "root";
    private static final String password = "root";

    public static void addUserGroup(long chatId, String groupNumber) {
        try (Connection connection = DriverManager.getConnection(url, userName, password)) {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO user_groups (chat_id, group_number) VALUES (?, ?)");
            statement.setLong(1, chatId);
            statement.setString(2, groupNumber);
            statement.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static boolean deleteUserGroup(long chatId, String groupNumber) {
        try (Connection connection = DriverManager.getConnection(url, userName, password)) {
            PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM user_groups WHERE chat_id=? AND group_number=?");
            statement.setLong(1, chatId);
            statement.setString(2, groupNumber);
            statement.execute();
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    public static List<String> getAddedGroups(long chatId) {
        try (Connection connection = DriverManager.getConnection(url, userName, password)) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT group_number FROM user_groups WHERE chat_id = " +
                    chatId);
            var list = new ArrayList<String>();
            while (resultSet.next()) {
                list.add(resultSet.getString("group_number"));
            }
            return list;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }
}
