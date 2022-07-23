package de.davidaugustat.wattpaddlerwidget;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Stores the information that is displayed on the widget.
 */
public class TidesInfo {
    private final String locationId;
    private final String locationName;
    private final LocalDate date;
    private final LocalDateTime highTide1;
    private final LocalDateTime highTide2;
    private final LocalDateTime lowTide1;
    private final LocalDateTime lowTide2;
    private final LocalDateTime updatedTime;

    public TidesInfo(String locationId, String locationName, LocalDate date, LocalDateTime highTide1,
                     LocalDateTime highTide2, LocalDateTime lowTide1, LocalDateTime lowTide2, LocalDateTime updatedTime) {
        this.locationId = locationId;
        this.locationName = locationName;
        this.date = date;
        this.highTide1 = highTide1;
        this.highTide2 = highTide2;
        this.lowTide1 = lowTide1;
        this.lowTide2 = lowTide2;
        this.updatedTime = updatedTime;
    }

    public TidesInfo(Location location, String dateString, String highTide1,
                     String highTide2, String lowTide1, String lowTide2) {
        this.locationId = location.getId();
        this.locationName = location.getName();
        this.highTide1 = DateTimeHelper.parseTidesTimeInCET(dateString, highTide1);
        this.highTide2 = DateTimeHelper.parseTidesTimeInCET(dateString, highTide2);
        this.lowTide1 = DateTimeHelper.parseTidesTimeInCET(dateString, lowTide1);
        this.lowTide2 = DateTimeHelper.parseTidesTimeInCET(dateString, lowTide2);

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

    public String getHighTide1Formatted() {
        return DateTimeHelper.getFormattedTidesTime(highTide1);
    }

    public String getHighTide2Formatted() {
        return DateTimeHelper.getFormattedTidesTime(highTide2);
    }

    public String getLowTide1Formatted() {
        return DateTimeHelper.getFormattedTidesTime(lowTide1);
    }

    public String getLowTide2Formatted() {
        return DateTimeHelper.getFormattedTidesTime(lowTide2);
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

    public int getNumberOfHighTides(){
        if(highTide1 != null && highTide2 != null){
            return 2;
        } else if(highTide1 != null) {
            return 1;
        }
        return 0;
    }

    public int getNumberOfLowTides(){
        if(lowTide1 != null && lowTide2 != null){
            return 2;
        } else if(lowTide1 != null) {
            return 1;
        }
        return 0;
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
