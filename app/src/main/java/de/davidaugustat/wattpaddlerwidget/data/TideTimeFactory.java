package de.davidaugustat.wattpaddlerwidget.data;

/**
 * Constructs a TideTime object whose subtype is dependent on the properties of the input data.
 */
public class TideTimeFactory {
    public static TideTime getTideTime(String dateString, String timeString, String targetDateString){
        if (dateString == null || timeString == null) {
            return new NonExistentTideTime();
        }
        if(dateString.equals(targetDateString)){
            return new NormalTideTime(dateString, timeString);
        }
        return new ShiftedTideTime();
    }
}
