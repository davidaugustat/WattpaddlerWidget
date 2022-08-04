package de.davidaugustat.wattpaddlerwidget;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Stores the information that is displayed on the widget.
 */
public class TidesInfo {
    private final String locationId;
    private final String locationName;
    private final LocalDate date;
    private TideTime highTide1;
    private TideTime highTide2;
    private TideTime lowTide1;
    private TideTime lowTide2;
    private final LocalDateTime updatedTime;


    public TidesInfo(Location location, String dateString) {
        this.locationId = location.getId();
        this.locationName = location.getName();
        this.highTide1 = new NonExistentTideTime();
        this.highTide2 = new NonExistentTideTime();
        this.lowTide1 = new NonExistentTideTime();
        this.lowTide2 = new NonExistentTideTime();

        this.date = DateTimeHelper.parseDate(dateString);
        this.updatedTime = LocalDateTime.now();
    }

    public void addTimeTime(String dateString, String timeString, String categoryString, String targetDateString){
        TideTime currentTideTime = TideTimeFactory.getTideTime(dateString, timeString, targetDateString);

        switch (categoryString) {
            case "H":
                if (highTide1 instanceof NonExistentTideTime) {
                    highTide1 = currentTideTime;
                } else if (highTide2 instanceof NonExistentTideTime) {
                    highTide2 = currentTideTime;
                }
                break;
            case "N":
                if (lowTide1 instanceof NonExistentTideTime) {
                    lowTide1 = currentTideTime;
                } else if (lowTide2 instanceof NonExistentTideTime){
                    lowTide2 = currentTideTime;
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid tide category. Must be 'H' or 'N'");
        }
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
        return highTide1.getHumanReadableString();
    }

    public String getHighTide2Formatted() {
        return highTide2.getHumanReadableString();
    }

    public String getLowTide1Formatted() {
        return lowTide1.getHumanReadableString();
    }

    public String getLowTide2Formatted() {
        return lowTide2.getHumanReadableString();
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
