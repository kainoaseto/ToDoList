package me.kainoseto.todo.Calendar.Tasks;

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
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import java.io.IOException;

import me.kainoseto.todo.Calendar.CalendarAware;
import me.kainoseto.todo.Calendar.CalendarEvent;
import me.kainoseto.todo.Calendar.GoogleCalendarManager;
import me.kainoseto.todo.Calendar.PlayServicesUtil;

/**
 * Created by TYLER on 12/6/2016.
 */

public class PostCalendarItemTask extends AsyncTask {
    private com.google.api.services.calendar.Calendar mService;
    private Exception mLastError;
    private Activity mActivity;
    private CalendarAware mCalendarAware;
    private CalendarEvent mCalendarEvent;

    private static final String APP_NAME = "TodoList";
    private static final String LOG_TAG = GetCalendarItemsTask.class.getCanonicalName();
    private String CALENDAR_NAME;

    public PostCalendarItemTask(GoogleAccountCredential credential, Activity activity, CalendarAware calendarAware, String calendarName, CalendarEvent calendarEvent){
        super();
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.calendar.Calendar.Builder(transport, jsonFactory, credential).setApplicationName(APP_NAME).build();
        mActivity = activity;
        mCalendarAware = calendarAware;
        mCalendarEvent = calendarEvent;
        CALENDAR_NAME = calendarName;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        try{
            postEventToCalendar();
            mCalendarAware.onPostCalendarItemsResult(); //Notifying activty that the item has been created
        }catch(Exception e){
            mLastError = e;
            cancel(true);
        }
        return null;
    }

    private void postEventToCalendar() throws IOException {
        Event event = new Event()
                .setSummary(mCalendarEvent.getTitle())
                .setDescription(mCalendarEvent.getDescription());

        EventDateTime startDateTime = new EventDateTime().setDateTime(mCalendarEvent.getStartDate());
        event.setStart(startDateTime);

        EventDateTime endDateTime = new EventDateTime().setDateTime(mCalendarEvent.getendDate());
        event.setEnd(startDateTime);

        mService.events().insert(CALENDAR_NAME, event).execute();
    }

    @Override
    protected void onPreExecute(){
        Log.d(LOG_TAG, "Preparing to run PostCalendarItem");
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        Log.d(LOG_TAG, "PostCalendarItem finished executing");
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
            Log.e(LOG_TAG, "PostCalendarItem Request Canceled");
        }
    }
}
