package de.davidaugustat.wattpaddlerwidget;

import android.content.Context;

import androidx.core.util.Consumer;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.List;

/**
 * This class fetches data from the HTTP API and converts it to object oriented datasets.
 */
public class DataFetcher {

    private final Context context;

    public DataFetcher(Context context){
        this.context = context;
    }

    /**
     * Fetches a list of all available locations and provides them as a list of Location objects.
     *
     * @param dataFetchedAction Called when the data has arrived.
     * @param errorAction Called in case of a network error.
     */
    public void fetchLocations(Consumer<List<Location>> dataFetchedAction,
                               Consumer<String> errorAction){
        String url = context.getString(R.string.locations_api_url);
        getTextFromUrl(url, response -> {
            List<Location> locations = locationsCsvToList(response);
            dataFetchedAction.accept(locations);
        }, error -> errorAction.accept(error.getMessage()));
    }

    /**
     * Fetches the tides data from the API. The API returns the data for the current day and the
     * next few days. This method builds a list of TidesInfo objects where each object contains
     * the data for a single day.
     *
     * @param locationId ID of the location as retrieved by fetchLocations.
     * @param date Date of the day of which the data should be retrieved.
     * @param dataFetchedAction Gets called as soon as the data is available.
     * @param errorAction Gets called in case of a network error.
     */
    public void fetchTidesData(String locationId, String date,
                               Consumer<List<TidesInfo>> dataFetchedAction,
                               Consumer<String> errorAction){
        String url = String.format(context.getString(R.string.tides_api_url), locationId, date);
        getTextFromUrl(url, response -> {
            List<TidesInfo> tidesInfos = tidesInfoStringToList(response);
            dataFetchedAction.accept(tidesInfos);
        }, error -> errorAction.accept(error.getMessage()));
    }

    /**
     * Fetches a string from an URL via HTTP.
     *
     * @param url URL which should be used to access the data.
     * @param successAction Called after data has been retrieved.
     * @param errorAction Called in case of a network error.
     */
    private void getTextFromUrl(String url, Response.Listener<String> successAction,
                                Response.ErrorListener errorAction){
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, successAction, errorAction);
        queue.add(stringRequest);
    }

    private List<TidesInfo> tidesInfoStringToList(String rawData){
        //TODO Implement this
        return null;
    }

    /**
     * Parses the CSV string containing locations and their IDs and converts it to a list of
     * Location objects.
     *
     * @param locationsCsv String containing a CSV-formatted list of locations
     * @return Locations contained in the CSV file as objects
     */
    private List<Location> locationsCsvToList(String locationsCsv){
        String[] lines = locationsCsv.split("\n");
        List<Location> locations = new ArrayList<>(lines.length);
        for(String line: lines){
            String[] columns = line.split(";");
            if(columns.length < 2){
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
