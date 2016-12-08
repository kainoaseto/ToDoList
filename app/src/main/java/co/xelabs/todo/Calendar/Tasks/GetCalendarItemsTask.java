package co.xelabs.todo.Calendar.Tasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import co.xelabs.todo.Calendar.CalendarAware;
import co.xelabs.todo.Calendar.CalendarEvent;
import co.xelabs.todo.Calendar.GoogleCalendarManager;
import co.xelabs.todo.Calendar.PlayServicesUtil;
import co.xelabs.todo.Util.DateTimeUtil;

/**
 * An asynchronous task that handle making the Google Calendar API call that retrieves upcomming events.
 */
public class GetCalendarItemsTask extends AsyncTask{
    private com.google.api.services.calendar.Calendar mService;
    private Exception mLastError;
    private Activity mActivity;
    private CalendarAware mCalendarAware;

    private static final String APP_NAME = "TodoList";
    private static final String LOG_TAG = GetCalendarItemsTask.class.getCanonicalName();
    private String CALENDAR_NAME;

    public GetCalendarItemsTask(GoogleAccountCredential credential, Activity activity, CalendarAware calendarAware, String calendarName){
        super();
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.calendar.Calendar.Builder(transport, jsonFactory, credential).setApplicationName(APP_NAME).build();
        mActivity = activity;
        mCalendarAware = calendarAware;
        CALENDAR_NAME = calendarName;
    }

    @Override
    protected List<CalendarEvent> doInBackground(Object[] params) {
        try{
            List<CalendarEvent> events = getEventsFromApi();
            mCalendarAware.onGetCalendarItemsResult(events); //Updating activity with synced results
            return events;
        }catch(Exception e){
            mLastError = e;
            cancel(true);
            return null;
        }
    }

    private List<CalendarEvent> getEventsFromApi() throws IOException {
        DateTime now = new DateTime(System.currentTimeMillis());
        List<CalendarEvent> calendarEvents = new ArrayList<>();

        Events events = mService.events().list(CALENDAR_NAME)
                .setTimeMin(now)
                .setTimeMax(DateTimeUtil.weekFromNow())
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();

        List<Event> items = events.getItems();

        for(Event event: items){
            calendarEvents.add(new CalendarEvent(event.getSummary(), event.getId(), event.getDescription(), event.getStart().getDateTime(), event.getEnd().getDateTime()));
        }

        return calendarEvents;
    }

    @Override
    protected void onPreExecute(){
        Log.d(LOG_TAG, "Preparing to run GetCalendarItemsTask");
    }

    @Override
    protected void onPostExecute(Object output){
        //Log.d(LOG_TAG, "GetCalendarItemsTask returned "+output.size()+" results");
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        if(mLastError != null){
            if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                PlayServicesUtil.showGooglePlayServicesAvailabilityErrorDialog( ((GooglePlayServicesAvailabilityIOException) mLastError).getConnectionStatusCode(), mActivity);
            } else if (mLastError instanceof UserRecoverableAuthIOException) {
                mActivity.startActivityForResult(
                        ((UserRecoverableAuthIOException) mLastError).getIntent(),
                        GoogleCalendarManager.REQUEST_AUTHORIZATION);
            } else {
                Log.e(LOG_TAG, "The following error occurred:\n" + mLastError.getMessage());
            }
        }else{
            Log.e(LOG_TAG, "GetCalendarItemsTask Request Canceled");
        }
    }
}
