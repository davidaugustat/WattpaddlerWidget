package de.davidaugustat.wattpaddlerwidget;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.time.LocalDateTime;
import java.time.Month;

public class DateTimeHelperTest {

    @Test
    public void testParseTidesTimeInCET1(){
        String date = "2022-07-23";
        String time = "14:30";
        LocalDateTime expected = LocalDateTime.of(2022, Month.JULY, 23, 15, 30);
        assertEquals(expected, DateTimeHelper.parseTidesTimeInCET(date, time));
    }

    @Test
    public void testParseTidesTimeInCET2(){
        String date = "2022-12-23";
        String time = "14:30";
        LocalDateTime expected = LocalDateTime.of(2022, Month.DECEMBER, 23, 14, 30);
        assertEquals(expected, DateTimeHelper.parseTidesTimeInCET(date, time));
    }

    @Test
    public void testConvertTime1(){
        String date = "2022-08-01";
        String time = "8:30";
        String result = DateTimeHelper.getFormattedTidesTime(DateTimeHelper.parseTidesTimeInCET(date, time));
        assertEquals("9:30", result);
    }

    @Test
    public void testConvertTime2(){
        String date = "2022-08-01";
        String time = "17:30";
        String result = DateTimeHelper.getFormattedTidesTime(DateTimeHelper.parseTidesTimeInCET(date, time));
        assertEquals("18:30", result);
    }

    @Test
    public void testConvertTime3(){
        String date = "2022-01-01";
        String time = "8:30";
        String result = DateTimeHelper.getFormattedTidesTime(DateTimeHelper.parseTidesTimeInCET(date, time));
        assertEquals("8:30", result);
    }

    @Test
    public void testConvertTime4(){
        String date = "2022-01-01";
        String time = "17:30";
        String result = DateTimeHelper.getFormattedTidesTime(DateTimeHelper.parseTidesTimeInCET(date, time));
        assertEquals("17:30", result);
    }

    @Test
    public void testConvertTime5(){
        String date = "2022-08-01";
        String time = "08:30";
        String result = DateTimeHelper.getFormattedTidesTime(DateTimeHelper.parseTidesTimeInCET(date, time));
        assertEquals("9:30", result);
    }
}
