package de.davidaugustat.wattpaddlerwidget.data;

import androidx.core.util.Pair;

import java.time.LocalDate;
import java.time.LocalDateTime;

import de.davidaugustat.wattpaddlerwidget.logic.DateTimeHelper;

/**
 * Stores the information that is displayed on the widget.
 */
public class TidesInfo {
    private final String locationId;
    private final String locationName;
    private final LocalDate date;
    private final TideTime highTide1;
    private final TideTime highTide2;
    private final TideTime lowTide1;
    private final TideTime lowTide2;
    private final LocalDateTime updatedTime;

    public TidesInfo(Location location, String dateString, Pair<TideTime, TideTime> lowTides,
                     Pair<TideTime, TideTime> highTides) {
        this.locationId = location.getId();
        this.locationName = location.getName();
        this.highTide1 = highTides.first;
        this.highTide2 = highTides.second;
        this.lowTide1 = lowTides.first;
        this.lowTide2 = lowTides.second;

        this.date = DateTimeHelper.parseDate(dateString);
        this.updatedTime = LocalDateTime.now();
    }

    public String getLocationId() {
        return locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getHighTidesFormatted(String formatString){
        return String.format(formatString,
                highTide1.getHumanReadableString(),
                highTide2.getHumanReadableString());
    }

    public String getLowTidesFormatted(String formatString){
        return String.format(formatString,
                lowTide1.getHumanReadableString(),
                lowTide2.getHumanReadableString());
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public String getDateFormatted(){
        return DateTimeHelper.getDateInGermanFormatting(date);
    }

    public String getLastUpdatedTimeFormatted(){
        return DateTimeHelper.getFormattedPreciseDateTime(updatedTime);
    }

    @Override
    public String toString() {
        return "TidesInfo{" +
                "locationId='" + locationId + '\'' +
                ", locationName='" + locationName + '\'' +
                ", date=" + date +
                ", highTide1='" + highTide1 + '\'' +
                ", highTide2='" + highTide2 + '\'' +
                ", lowTide1='" + lowTide1 + '\'' +
                ", lowTide2='" + lowTide2 + '\'' +
                ", updatedTime=" + updatedTime +
                '}';
    }
}
