package schedule;

import connection.UrlReader;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.function.Function;

public class ScheduleHandler {

    private static final String SCHEDULE_URL_BASE =
            "https://journal.bsuir.by/api/v1/studentGroup/schedule?studentGroup=";

    private String parseDaySchedule(JSONArray schedule, String currentWeekNumber) {
        StringBuffer response = new StringBuffer();
        schedule.forEach(scheduleItem -> {
            JSONArray weekNumbers = ((JSONObject) scheduleItem).getJSONArray("weekNumber");
            ArrayList<Integer> numbers = new ArrayList<>(weekNumbers.length());
            weekNumbers.forEach(value -> numbers.add((int) value));
            if (numbers.contains(Integer.valueOf(currentWeekNumber))) {
                response.append("\n").append(((JSONObject) scheduleItem).getString("lessonTime"));
                response.append(" ").append(((JSONObject) scheduleItem).getString("subject"));
                response.append(" ").append(((JSONObject) scheduleItem).getString("lessonType"));
                int numSubgroup = ((JSONObject) scheduleItem).getInt("numSubgroup");
                if (numSubgroup != 0) {
                    response.append(" ").append(numSubgroup).append("пг");
                }
                String auditory = ((JSONObject) scheduleItem).getJSONArray("auditory")
                        .join(" ");
                if (auditory.length() > 0) {
                    auditory = auditory.substring(1, auditory.length() - 1);
                }
                response.append(" ").append(auditory);
            }
        });
        String result = response.toString();
        return result.isEmpty() ? "\nНет занятий" : result;
    }

    private String getDay(JSONObject scheduleDay, String weekNumber) {
        var response = new StringBuilder();
        response.append(scheduleDay.getString("weekDay"));
        JSONArray schedule = scheduleDay.getJSONArray("schedule");
        response.append(" ").append(parseDaySchedule(schedule, weekNumber));
        return response.toString();
    }

    private Schedule getSchedule(String groupNumber, String weekNumber) {
        try {
            JSONObject jsonObject = UrlReader.readJsonFromUrl(SCHEDULE_URL_BASE + groupNumber);
            if (jsonObject != null) {
                if (weekNumber == null) {
                    weekNumber = jsonObject.getNumber("currentWeekNumber").toString();
                }
                var days = new ArrayList<String>();
                JSONArray schedules = jsonObject.getJSONArray("schedules");
                String finalWeekNumber = weekNumber;
                schedules.forEach(schedulesItem -> days.add(getDay((JSONObject) schedulesItem, finalWeekNumber)));
                return new Schedule(weekNumber, days);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isGroupExists(String groupNumber) {
        JSONObject jsonObject = UrlReader.readJsonFromUrl(SCHEDULE_URL_BASE + groupNumber);
        return jsonObject != null;
    }

    private String parseIntDay(int day) {
        return switch (day) {
            case Calendar.SUNDAY -> "Воскресенье";
            case Calendar.MONDAY -> "Понедельник";
            case Calendar.TUESDAY -> "Вторник";
            case Calendar.WEDNESDAY -> "Среда";
            case Calendar.THURSDAY -> "Четверг";
            case Calendar.FRIDAY -> "Пятница";
            case Calendar.SATURDAY -> "Cуббота";
            default -> "Неопределено";
        };
    }

    public String getScheduleString(String groupNumber, String weekNumber, SchedulePeriod period) {
        Function<Schedule, String> todaySchedule = schedule -> {
            var calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            String day = parseIntDay(calendar.get(Calendar.DAY_OF_WEEK));
            String result = "Текущая неделя: " + schedule.weekNumber() + "\n\n";
            result += schedule.days().stream()
                    .filter(item -> item.startsWith(day))
                    .findFirst()
                    .orElse(day + "\nНет занятий");
            return result;
        };
        Function<Schedule, String> weekSchedule = schedule -> {
            String result = "Выбранная неделя: " + schedule.weekNumber() + "\n\n";
            result += String.join("\n\n", schedule.days());
            return result;
        };
        Schedule scheduleResponse = getSchedule(groupNumber, weekNumber);
        return (scheduleResponse == null) ? null :
                switch (period) {
                    case DAY -> todaySchedule.apply(scheduleResponse);
                    case WEEK -> weekSchedule.apply(scheduleResponse);
                };
    }
}
