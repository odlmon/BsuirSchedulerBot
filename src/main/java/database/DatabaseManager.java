package database;

import org.postgresql.util.PGobject;

import java.sql.*;
import java.util.*;
import java.util.Date;

public class DatabaseManager {

    private static final String URL = System.getenv("JDBC_DATABASE_URL");
    public static final byte USER_GROUP_LIMIT = 10;
    public static final byte CACHE_TTL_IN_DAYS = 1;

    public static Map<Long, List<String>> allGroupNotifications() {
        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT chat_id, group_number FROM bot_users.notified_groups")) {
            ResultSet resultSet = statement.executeQuery();
            Map<Long, List<String>> notifiedGroups = new HashMap<>();
            while (resultSet.next()) {
                long chatId = resultSet.getLong("chat_id");
                if (notifiedGroups.containsKey(chatId)) {
                    notifiedGroups.get(chatId).add(resultSet.getString("group_number"));
                } else {
                    List<String> list = new ArrayList<>();
                    list.add(resultSet.getString("group_number"));
                    notifiedGroups.put(chatId, list);
                }
            }
            resultSet.close();
            return notifiedGroups;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return new HashMap<>();
        }
    }

    public static void addGroupNotification(long chatId, String groupNumber) {
        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO bot_users.notified_groups (chat_id, group_number) VALUES (?, ?)")) {
            statement.setLong(1, chatId);
            statement.setString(2, groupNumber);
            statement.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static void deleteGroupNotification(long chatId, String groupNumber) {
        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM bot_users.notified_groups WHERE chat_id=? AND group_number=?")) {
            statement.setLong(1, chatId);
            statement.setString(2, groupNumber);
            statement.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static boolean isGroupNotified(long chatId, String groupNumber) {
        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT group_number FROM bot_users.notified_groups WHERE chat_id=? AND group_number=?")) {
            statement.setLong(1, chatId);
            statement.setString(2, groupNumber);
            return statement.executeQuery().next();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    private static void deleteExpiredCache(String groupNumber) {
        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM bot_users.schedule_cache WHERE group_number=?")) {
            statement.setString(1, groupNumber);
            statement.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static String checkoutScheduleInCache(String groupNumber) {
        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT schedule, expires FROM bot_users.schedule_cache WHERE group_number=?")) {
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
        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO bot_users.schedule_cache (group_number, schedule, expires) VALUES (?, ?, ?)")) {
            statement.setString(1, groupNumber);
            var jsonObject = new PGobject();
            jsonObject.setType("json");
            jsonObject.setValue(schedule);
            statement.setObject(2, jsonObject);
            var date = new Date();
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.add(Calendar.DATE, CACHE_TTL_IN_DAYS);
            statement.setDate(3, new java.sql.Date(c.getTime().getTime()));
            statement.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static boolean isUserGroupPresentInList(long chatId, String groupNumber) {
        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT group_number FROM bot_users.user_groups WHERE chat_id=? AND group_number=?")) {
            statement.setLong(1, chatId);
            statement.setString(2, groupNumber);
            return statement.executeQuery().next();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    public static boolean isUserGroupLimitOver(long chatId) {
        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT group_number FROM bot_users.user_groups WHERE chat_id=?")) {
            statement.setLong(1, chatId);
            int count = 0;
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                count++;
            }
            resultSet.close();
            return count == USER_GROUP_LIMIT;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    public static void addUserGroup(long chatId, String groupNumber) {
        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO bot_users.user_groups (chat_id, group_number) VALUES (?, ?)")) {
            statement.setLong(1, chatId);
            statement.setString(2, groupNumber);
            statement.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static boolean deleteUserGroup(long chatId, String groupNumber) {
        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM bot_users.user_groups WHERE chat_id=? AND group_number=?")) {
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
        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT group_number FROM bot_users.user_groups WHERE chat_id=?")) {
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
