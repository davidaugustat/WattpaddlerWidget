package de.davidaugustat.wattpaddlerwidget.data;

import androidx.core.util.Pair;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import de.davidaugustat.wattpaddlerwidget.logic.DateTimeHelper;

/**
 * Helps to build a TidesInfo object step by step.
 */
public class TidesInfoBuilder {

    private final Location location;
    private final String targetDateString;
    private final List<LocalDateTime> lowTideTimes;
    private final List<LocalDateTime> highTideTimes;

    public TidesInfoBuilder(Location location, String targetDateString){
        this.location = location;
        this.targetDateString = targetDateString;
        this.lowTideTimes = new ArrayList<>(2);
        this.highTideTimes = new ArrayList<>(2);
    }

    /**
     * Adds a tide time to the list of times for low or high times depending on the
     * tideCategoryString.
     *
     * Note that if the dateString is not equal to the targetDateString of this object, nothing
     * will happen.
     *
     * @param dateString Date of the tide in format YYYY-MM-DD
     * @param timeString Time of the tide in format HH:mm
     * @param tideCategoryString Category of the tide, either 'H' for high or 'N' for low.
     */
    public void addTideTime(String dateString, String timeString, String tideCategoryString){
        if(!dateString.equals(targetDateString)){
            return;
        }
        LocalDateTime dateTime = DateTimeHelper.parseLocalDateTime(dateString, timeString);
        switch(tideCategoryString){
            case "N":
                lowTideTimes.add(dateTime);
                break;
            case "H":
                highTideTimes.add(dateTime);
                break;
            default:
                throw new IllegalArgumentException("Invalid tide category. Must be 'H' or 'N'");
        }
    }

    /**
     * Builds a TidesInfo object from the data that has been provided to the builder object.
     */
    public TidesInfo build(){
        Pair<TideTime, TideTime> lowTides = generateTideTimes(lowTideTimes);
        Pair<TideTime, TideTime> highTides = generateTideTimes(highTideTimes);
        return new TidesInfo(location, targetDateString, lowTides, highTides);
    }

    /**
     * Generates the TideTime objects of the correct dynamic type:
     * <ul>
     * <li>If both tide times are missing, a NonExistingTideTime object is created for both times.
     * This is the case when tides are not possible, e.g. because a harbor has run dry.</li>
     * <li>If one tide time is present, one NormalTideTime object and one ShiftedTideTime object are
     * created because the second tide time has shifted to the next day.</li>
     * <li>If both tide times are present, two NormalTideTime objects are created.</li>
     * </ul>
     * @param tideTimes Dates and times when the tides occur. Either provide high or low tides.
     */
    private Pair<TideTime, TideTime> generateTideTimes(List<LocalDateTime> tideTimes){
        TideTime tideTime1, tideTime2;
        switch (tideTimes.size()){
            case 0:
                tideTime1 = new NonExistentTideTime();
                tideTime2 = new NonExistentTideTime();
                break;
            case 1:
                tideTime1 = new NormalTideTime(tideTimes.get(0));
                tideTime2 = new ShiftedTideTime();
                break;
            default:
                tideTime1 = new NormalTideTime(tideTimes.get(0));
                tideTime2 = new NormalTideTime(tideTimes.get(1));
        }
        return new Pair<>(tideTime1, tideTime2);
    }

}
