package de.davidaugustat.wattpaddlerwidget.logic;

import android.content.Context;

import androidx.core.util.Consumer;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.davidaugustat.wattpaddlerwidget.BuildConfig;
import de.davidaugustat.wattpaddlerwidget.data.Location;
import de.davidaugustat.wattpaddlerwidget.R;
import de.davidaugustat.wattpaddlerwidget.data.TidesInfo;
import de.davidaugustat.wattpaddlerwidget.data.TidesInfoBuilder;

/**
 * This class fetches data from the HTTP API and converts it to object oriented datasets.
 */
public class DataFetcher {

    private final Context context;
    private final int REQUEST_TIMEOUT_MILLIS = 5000;

    public DataFetcher(Context context) {
        this.context = context;
    }

    /**
     * Fetches a list of all available locations and provides them as a list of Location objects.
     *
     * @param dataFetchedAction Called when the data has arrived.
     * @param errorAction       Called in case of a network error.
     */
    public void fetchLocations(Consumer<List<Location>> dataFetchedAction,
                               Consumer<String> errorAction) {
        String url = BuildConfig.LOCATIONS_API_URL + context.getString(R.string.locations_api_path);
        getTextFromUrl(url, response -> {
            List<Location> locations = locationsCsvToList(response);
            dataFetchedAction.accept(locations);
        }, error -> errorAction.accept(error.getMessage()));
    }

    /**
     * Fetches the tides data for a single day from the widget API. The API returns all high and
     * low tides of the specified day.
     *
     * This method converts the received data into a TidesInfo object which is then provided to
     * a callback.
     *
     * @param location Location of which the tides data should be fetched.
     * @param date Day for which the data should be fetched. Format: yyyy-mm-dd
     * @param dataFetchedAction Gets called as soon as data is available
     * @param errorAction Gets called in case of a network error.
     */
    public void fetchTidesDataSingleDay(Location location, String date, Consumer<TidesInfo> dataFetchedAction,
                                        Consumer<String> errorAction) {
        String url = BuildConfig.TIDES_WIDGET_API_URL
                + String.format(context.getString(R.string.tides_widget_api_path), location.getId(), date);
        getTextFromUrl(url, response -> {
            try {
                TidesInfo tidesInfo = tidesInfoStringToObject(location, date, response);
                dataFetchedAction.accept(tidesInfo);
            } catch (IllegalArgumentException e){
                errorAction.accept("Error: Malformed response from API");
            }
        }, error -> {
            errorAction.accept(error.toString());
        });
    }

    /**
     * Fetches the tides data for the current day from the widget API. The API returns all high and
     * low tides of the current day.
     *
     * This method converts the received data into a TidesInfo object which is then provided to
     * a callback.
     *
     * @param location Location of which the tides data should be fetched.
     * @param dataFetchedAction Gets called as soon as data is available
     * @param errorAction Gets called in case of a network error.
     */
    public void fetchTidesDataSingleDay(Location location, Consumer<TidesInfo> dataFetchedAction,
                                                    Consumer<String> errorAction){
        String currentDateString = DateTimeHelper.getCurrentDateInQueryNotation();
        fetchTidesDataSingleDay(location, currentDateString, dataFetchedAction, errorAction);
    }

    /**
     * Converts raw response from the widget API to a TidesInfo objects.
     *
     * The response string must have the following structure (example):
     *
     * STARTDATA+
     * 2022-07-07; 0:38;N
     * 2022-07-07; 6:55;H
     * 2022-07-07;12:39;N
     * 2022-07-07;19:04;H
     * ENDDATA+
     * Pegel/Date 510P at 2022-07-07
     *
     * It is allowed that only one high tide (H) or low tide (N) occurs instead of two. It is also
     * allowed that no low tides (N) are contained at all.
     *
     * @param location Location for which the data was queried.
     * @param response Raw response text from the API
     * @return TidesInfo object containing the data from the API
     */
    private TidesInfo tidesInfoStringToObject(Location location, String targetDate, String response) {
        String[] lines = response.split("\n");
        if (lines.length < 4) {
            throw new IllegalArgumentException("Malformed response data. Not enough lines.");
        }
        String[] dayInfos = Arrays.copyOfRange(lines, 1, lines.length - 2);
        TidesInfoBuilder tidesInfoFactory = new TidesInfoBuilder(location, targetDate);

        for (String dayInfo : dayInfos) {
            String[] components = dayInfo.split(";");
            if (components.length < 3) {
                throw new IllegalArgumentException("Malformed response data. Not enough columns.");
            }
            String dateString = components[0].trim();
            String timeString = components[1].trim();
            String tideCategoryString = components[2].trim();
            tidesInfoFactory.addTideTime(dateString, timeString, tideCategoryString);
        }
        return tidesInfoFactory.build();
    }

    /**
     * Fetches a string from an URL via HTTP.
     *
     * @param url           URL which should be used to access the data.
     * @param successAction Called after data has been retrieved.
     * @param errorAction   Called in case of a network error.
     */
    private void getTextFromUrl(String url, Response.Listener<String> successAction,
                                Response.ErrorListener errorAction) {
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, successAction, errorAction);
        stringRequest.setShouldCache(false);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(REQUEST_TIMEOUT_MILLIS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
    }

    /**
     * Parses the CSV string containing locations and their IDs and converts it to a list of
     * Location objects.
     *
     * @param locationsCsv String containing a CSV-formatted list of locations
     * @return Locations contained in the CSV file as objects
     */
    private List<Location> locationsCsvToList(String locationsCsv) {
        String[] lines = locationsCsv.split("\n");
        List<Location> locations = new ArrayList<>(lines.length);
        for (String line : lines) {
            String[] columns = line.split(";");
            if (columns.length < 2) {
                throw new IllegalArgumentException("Invalid CSV line: " + line);
            }
            String locationName = columns[0];
            String locationId = columns[1];
            Location location = new Location(locationId, locationName);
            locations.add(location);
        }
        return locations;
    }
}
