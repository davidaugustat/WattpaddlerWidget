package de.davidaugustat.wattpaddlerwidget;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Stores the information that is displayed on the widget.
 */
public class TidesInfo {
    private String locationId;
    private String locationName;
    private LocalDate date;
    private String highTide1;
    private String highTide2;
    private String lowTide1;
    private String lowTide2;
    private LocalDateTime updatedTime;

    public TidesInfo(String locationId, String locationName, LocalDate date, String highTide1,
                     String highTide2, String lowTide1, String lowTide2, LocalDateTime updatedTime) {
        this.locationId = locationId;
        this.locationName = locationName;
        this.date = date;
        this.highTide1 = highTide1;
        this.highTide2 = highTide2;
        this.lowTide1 = lowTide1;
        this.lowTide2 = lowTide2;
        this.updatedTime = updatedTime;
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

    public String getHighTide1() {
        return highTide1;
    }

    public String getHighTide2() {
        return highTide2;
    }

    public String getLowTide1() {
        return lowTide1;
    }

    public String getLowTide2() {
        return lowTide2;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public String getDateFormatted(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return date.format(formatter);
    }

    public String getLastUpdatedTimeFormatted(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        return updatedTime.format(formatter);
    }
}
