package de.davidaugustat.wattpaddlerwidget;

import androidx.annotation.NonNull;

/**
 * Represents a tide that does not happen (is skipped).
 * This can happen for example when a harbor runs dry.
 */
public class NonExistentTideTime extends TideTime{
    @Override
    public String getHumanReadableString() {
        return "*";
    }

    @NonNull
    @Override
    public String toString() {
        return "nonexistent tide time";
    }
}
