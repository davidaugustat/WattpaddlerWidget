package de.davidaugustat.wattpaddlerwidget;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.List;
import java.util.stream.Collectors;

/**
 * App widget configuration activity that lets the user select a location that should be used
 * for the widget that is being configured.
 */
public class WidgetConfigurationActivity extends AppCompatActivity {

    private int appWidgetId;
    private Location selectedLocation;

    private ListView locationsList;
    private ProgressBar progressBar;
    private LinearLayout errorLayout;
    private Button retryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_configuration);

        this.appWidgetId = getAppWidgetId();
        setResult(RESULT_CANCELED);

        locationsList = findViewById(R.id.locations_listview);
        progressBar = findViewById(R.id.config_progressBar);
        errorLayout = findViewById(R.id.config_error_layout);
        retryButton = findViewById(R.id.config_retry_button);

        retryButton.setOnClickListener(view -> loadLocations());
        loadLocations();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.preference_options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.done_menu_item){
            finishActivitySuccess();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Fetches the locations list from the API an displays it in the locationsList list view.
     * If an error occurs while fetching the data, an error message together with a retry button
     * is displayed.
     */
    private void loadLocations(){
        progressBar.setVisibility(View.VISIBLE);
        locationsList.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);

        new DataFetcher(this).fetchLocations(locations -> {
            locationsList.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            setupLocationsList(locations);
        }, errorMessage -> {
            Log.d("Error loading locations", errorMessage);
            progressBar.setVisibility(View.GONE);
            errorLayout.setVisibility(View.VISIBLE);
        });
    }

    /**
     * Saves the selected location to shared preferences and triggers an update of the widget that
     * is associated with this activity instance. Then terminates the activity with result code
     * RESULT_OK.
     *
     * This way, the system knows that the configuration of the app widget was successful.
     *
     * If selectedLocation is null (i.e. the user has not selected a location), a toast gets
     * displayed and nothing else happens.
     */
    private void finishActivitySuccess(){
        if(selectedLocation == null){
            Toast.makeText(this, R.string.select_location_text, Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferencesHelper.saveLocation(selectedLocation, appWidgetId, this);
        updateWidget();

        Intent resultValue = new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    /**
     * Triggers an update of the app widget that is associated with this activity instance.
     */
    private void updateWidget(){
        Intent intent = new Intent(this, MainWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int [] {appWidgetId});
        sendBroadcast(intent);
    }

    /**
     * Retrieves the ID of the app widget that is associated with this activity instance from the
     * intent.
     */
    private int getAppWidgetId(){
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        return appWidgetId;
    }

    /**
     * Displays the locations from the list in the locationsList list view.
     *
     * Additionally sets up an item click listener for this list: When an item is clicked, the
     * selectedLocation field is set to its associated location object.
     *
     * @param locations Locations to display in the list
     */
    private void setupLocationsList(List<Location> locations){
        locationsList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        List<String> locationNames = locations.stream()
                .map(Location::getName)
                .collect(Collectors.toList());

        ArrayAdapter<String> listAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_single_choice, locationNames);
        locationsList.setAdapter(listAdapter);
        locationsList.setOnItemClickListener((adapterView, view, index, id) -> {
            selectedLocation = locations.get(index);
        });

        setPresetLocationCheckedIfExists(locations);
    }

    /**
     * Checks if a location for the current widget has already been configured in a previous setup.
     * If this is the case, this location is preselected in the list of locations.
     *
     * @param locations List of all locations that are displayed in the list.
     */
    private void setPresetLocationCheckedIfExists(List<Location> locations) {
        try {
            Location presetLocation = SharedPreferencesHelper.getLocation(appWidgetId, this);
            int index = locations.indexOf(presetLocation);
            if(index != -1){
                locationsList.setItemChecked(index, true);
                selectedLocation = presetLocation;
            }
        } catch (IllegalArgumentException exception){
            // when no location for the widget has been stored yet (i.e. at initial setup), no
            // action is required.
        }
    }
}