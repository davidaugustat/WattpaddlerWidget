package de.davidaugustat.wattpaddlerwidget;

import java.time.LocalDateTime;

/**
 * Represents a tide that exists and happens on the current day.
 */
public class NormalTideTime  extends TideTime{

    private final LocalDateTime dateTime;

    public NormalTideTime(String dateString, String timeString){
        dateTime = DateTimeHelper.parseLocalDateTime(dateString, timeString);
    }

    @Override
    public String getHumanReadableString() {
        return DateTimeHelper.getFormattedTidesTime(dateTime);
    }

    @Override
    public String toString() {
        return "NormalTideTime{" +
                "dateTime=" + dateTime +
                '}';
    }
}
