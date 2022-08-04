package de.davidaugustat.wattpaddlerwidget.data;

import java.time.LocalDateTime;

import de.davidaugustat.wattpaddlerwidget.logic.DateTimeHelper;

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
