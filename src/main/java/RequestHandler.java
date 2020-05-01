import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class RequestHandler {

    private String scheduleUrlBase = "https://journal.bsuir.by/api/v1/studentGroup/schedule?studentGroup=";

    public String getScheduleResponse(String groupNumber) {
        try {
            JSONObject jsonObject = JsonReader.readJsonFromUrl(scheduleUrlBase + groupNumber);
            String currentWeekNumber = jsonObject.getNumber("currentWeekNumber").toString();
            StringBuffer response = new StringBuffer("Текущая неделя: " + currentWeekNumber);
            JSONArray schedules = jsonObject.getJSONArray("schedules");
            schedules.forEach(schedulesItem -> {
//                response.append("\n").append(jsonObject.getString("weekDay"));
                JSONArray schedule = ((JSONObject) schedulesItem).getJSONArray("schedule");
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
                    }
                });
                response.append("\n");
            });
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception";
        }
    }
}
