package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DatabaseManager {

    private static final String url = "jdbc:mysql://192.168.99.100:3306/bot_users";
    private static final String userName = "root";
    private static final String password = "root";

    public static final byte userGroupLimit = 10;
    public static final byte cacheTtlInDays = 1;

    private static void deleteExpiredCache(String groupNumber) {
        try (Connection connection = DriverManager.getConnection(url, userName, password);
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM schedule_cache WHERE group_number=?")) {
            statement.setString(1, groupNumber);
            statement.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static String checkoutScheduleInCache(String groupNumber) {
        try (Connection connection = DriverManager.getConnection(url, userName, password);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT schedule, expires FROM schedule_cache WHERE group_number=?")) {
            statement.setString(1, groupNumber);
            ResultSet resultSet = statement.executeQuery();
            String schedule = "";
            while (resultSet.next()) {
                if (resultSet.getDate("expires").compareTo(new Date()) > 0) {
                    schedule = resultSet.getString("schedule");
                } else {
                    deleteExpiredCache(groupNumber);
                }
            }
            resultSet.close();
            return schedule.isEmpty() ? null : schedule;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

    public static void addScheduleInCache(String groupNumber, String schedule) {
        try (Connection connection = DriverManager.getConnection(url, userName, password);
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO schedule_cache (group_number, schedule, expires) VALUES (?, ?, ?)")) {
            statement.setString(1, groupNumber);
            statement.setString(2, schedule);
            var date = new Date();
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.add(Calendar.DATE, cacheTtlInDays);
            statement.setDate(3, new java.sql.Date(c.getTime().getTime()));
            statement.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static boolean isUserGroupPresentInList(long chatId, String groupNumber) {
        try (Connection connection = DriverManager.getConnection(url, userName, password);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT group_number FROM user_groups WHERE chat_id=? AND group_number=?")) {
            statement.setLong(1, chatId);
            statement.setString(2, groupNumber);
            return statement.executeQuery().next();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    public static boolean isUserGroupLimitOver(long chatId) {
        try (Connection connection = DriverManager.getConnection(url, userName, password);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT group_number FROM user_groups WHERE chat_id=?")) {
            statement.setLong(1, chatId);
            int count = 0;
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                count++;
            }
            resultSet.close();
            return count == userGroupLimit;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    public static void addUserGroup(long chatId, String groupNumber) {
        try (Connection connection = DriverManager.getConnection(url, userName, password);
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO user_groups (chat_id, group_number) VALUES (?, ?)")) {
            statement.setLong(1, chatId);
            statement.setString(2, groupNumber);
            statement.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static boolean deleteUserGroup(long chatId, String groupNumber) {
        try (Connection connection = DriverManager.getConnection(url, userName, password);
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM user_groups WHERE chat_id=? AND group_number=?")) {
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
        try (Connection connection = DriverManager.getConnection(url, userName, password);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT group_number FROM user_groups WHERE chat_id=?")) {
            statement.setLong(1, chatId);
            ResultSet resultSet = statement.executeQuery();
            var list = new ArrayList<String>();
            while (resultSet.next()) {
                list.add(resultSet.getString("group_number"));
            }
            resultSet.close();
            return list;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }
}
