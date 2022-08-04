package de.davidaugustat.wattpaddlerwidget;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;

import static org.junit.Assert.*;

public class TideTimesTest {

    Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

    @Test
    public void tidesTimeTest1(){
        testTidesFetchResult("631P", "2022-08-02", "05:01", "17:13", "11:30", " ");
    }

    @Test
    public void tidesTimeTest2(){
        testTidesFetchResult("631P", "2022-08-03", "05:38", "17:51", "00:01", "12:08");
    }

    @Test
    public void tidesTimeTest3(){
        testTidesFetchResult("631P", "2022-08-04", "06:17", "18:29", "00:39", "12:47");
    }

    @Test
    public void tidesTimeTest4(){
        testTidesFetchResult("750P", "2022-07-30", "05:03", "17:11", "11:42", " ");
    }

    @Test
    public void tidesTimeTest5(){
        testTidesFetchResult("750P", "2022-07-31", "05:38", "17:45", "00:09", "12:18");
    }

    @Test
    public void tidesTimeTest6(){
        testTidesFetchResult("675P", "2022-08-03", "05:29", "17:40", "*", "*");
    }

    public void testTidesFetchResult(String locationId, String dateString, String highTide1Expected,
                                     String highTide2Expected, String lowTide1Expected,
                                     String lowTide2Expected){

        Object lock = new Object();

        Location location = new Location(locationId, "Test");
        new DataFetcher(context).fetchTidesDataSingleDay(location, dateString, tidesInfo -> {
            assertEquals(highTide1Expected, tidesInfo.getHighTide1Formatted());
            assertEquals(highTide2Expected, tidesInfo.getHighTide2Formatted());
            assertEquals(lowTide1Expected, tidesInfo.getLowTide1Formatted());
            assertEquals(lowTide2Expected, tidesInfo.getLowTide2Formatted());

            synchronized (lock){
                lock.notifyAll();
            }
        }, errorMsg -> {
            fail("Network Error: " + errorMsg);
        });

        synchronized (lock){
            try {
                lock.wait();
            } catch (InterruptedException e) {
                fail(e.getMessage());
            }
        }
    }
}
