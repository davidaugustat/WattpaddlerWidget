package de.davidaugustat.wattpaddlerwidget;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateTimeHelper {
    public static String getCurrentDateInQueryNotation(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.now().format(formatter);
    }
}
