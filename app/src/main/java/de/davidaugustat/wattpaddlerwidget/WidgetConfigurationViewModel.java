package de.davidaugustat.wattpaddlerwidget;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import java.util.List;

/**
 * View model which holds all data for the WidgetConfigurationActivity.
 */
public class WidgetConfigurationViewModel extends AndroidViewModel {

    private int appWidgetId;
    private Location selectedLocation;
    private int selectedLocationIndex;
    private List<Location> locations;

    public WidgetConfigurationViewModel(@NonNull Application application) {
        super(application);
    }

    public int getAppWidgetId() {
        return appWidgetId;
    }

    public void setAppWidgetId(int appWidgetId) {
        this.appWidgetId = appWidgetId;
    }

    public Location getSelectedLocation() {
        return selectedLocation;
    }

    public void setSelectedLocation(Location selectedLocation, int index) {
        this.selectedLocation = selectedLocation;
        this.selectedLocationIndex = index;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    public int getSelectedLocationIndex() {
        return selectedLocationIndex;
    }
}
