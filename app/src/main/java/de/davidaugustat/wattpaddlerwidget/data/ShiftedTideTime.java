package de.davidaugustat.wattpaddlerwidget.data;

import androidx.annotation.NonNull;

/**
 * Represents a tide that exists and happens on the current day in UTC+1 time but is shifted to the
 * next day in German time. (--> daylight savings time!)
 */
public class ShiftedTideTime extends TideTime{
    @Override
    public String getHumanReadableString() {
        return " ";
    }

    @NonNull
    @Override
    public String toString() {
        return "tide shifted to next day";
    }
}
