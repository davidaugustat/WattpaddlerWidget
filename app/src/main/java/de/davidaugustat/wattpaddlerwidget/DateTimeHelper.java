package de.davidaugustat.wattpaddlerwidget;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class DateTimeHelper {

    private final static ZoneOffset CET_OFFSET = ZoneOffset.ofHours(+1);

    public static String getCurrentDateInQueryNotation(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.now().format(formatter);
    }

    public static LocalDateTime parseTidesTimeInCET(String date, String timeCET){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm");
        LocalDateTime dateTimeCET = LocalDateTime.parse(date + " " + timeCET, formatter);
        return OffsetDateTime.of(dateTimeCET, CET_OFFSET)
                .atZoneSameInstant(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public static String getFormattedTidesTime(LocalDateTime time){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:mm");
        return time.format(formatter);
    }

    public static String getFormattedPreciseDateTime(LocalDateTime dateTime){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        return dateTime.format(formatter);
    }

    public static LocalDate parseDate(String dateString){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(dateString, formatter);
    }

    public static String getDateInGermanFormatting(LocalDate date){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EE dd.MM.yyyy");
        return date.format(formatter);
    }
}
