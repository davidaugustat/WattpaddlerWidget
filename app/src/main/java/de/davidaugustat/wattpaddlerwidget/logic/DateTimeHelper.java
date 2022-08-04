package de.davidaugustat.wattpaddlerwidget.logic;

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
        if(date == null || timeCET == null){
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm");
        LocalDateTime dateTimeCET = LocalDateTime.parse(date + " " + timeCET, formatter);
        return OffsetDateTime.of(dateTimeCET, CET_OFFSET)
                .atZoneSameInstant(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public static LocalDateTime parseLocalDateTime(String date, String time){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm");
        return LocalDateTime.parse(date + " " + time, formatter);
    }

    public static String getFormattedTidesTime(LocalDateTime time){
        if(time == null){
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EE dd.MM.yy");
        return date.format(formatter);
    }
}
