package schedule;

import java.util.ArrayList;

public class Schedule {

    private final String weekNumber;

    private final ArrayList<String> days;

    public Schedule(String weekNumber, ArrayList<String> days) {
        this.weekNumber = weekNumber;
        this.days = days;
    }

    public String weekNumber() {
        return weekNumber;
    }

    public ArrayList<String> days() {
        return days;
    }
}
