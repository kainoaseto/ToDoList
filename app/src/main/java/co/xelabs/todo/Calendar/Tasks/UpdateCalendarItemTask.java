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
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import java.io.IOException;

import co.xelabs.todo.Calendar.CalendarAware;
import co.xelabs.todo.Calendar.CalendarEvent;
import co.xelabs.todo.Calendar.GoogleCalendarManager;
import co.xelabs.todo.Calendar.PlayServicesUtil;

/**
 * Created by TYLER on 12/8/2016.
 */

public class UpdateCalendarItemTask extends AsyncTask{
    private com.google.api.services.calendar.Calendar mService;
    private Exception mLastError;
    private Activity mActivity;
    private CalendarAware mCalendarAware;
    private CalendarEvent mCalendarEvent;

    private static final String APP_NAME = "TodoList";
    private static final String LOG_TAG = UpdateCalendarItemTask.class.getCanonicalName();
    private String CALENDAR_NAME;

    public UpdateCalendarItemTask(GoogleAccountCredential credential, Activity activity, CalendarAware calendarAware, String calendarName, String oldTitle, CalendarEvent calendarEvent){
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
        try {
            updateCalendarItem();
            mCalendarAware.onUpdateCalendarItemResult();
        } catch (IOException e) {
            mLastError = e;
            cancel(true);
        }
        return null;
    }

    private void updateCalendarItem() throws IOException {
        if((null != mCalendarEvent.getStartDate()) && (null != mCalendarEvent.getendDate())){
            Event event = mService.events().get(CALENDAR_NAME, mCalendarEvent.getId()).execute();

            EventDateTime startDate = new EventDateTime().setDateTime(mCalendarEvent.getStartDate());
            EventDateTime endDate = new EventDateTime().setDateTime(mCalendarEvent.getendDate());

            event.setStart(startDate);
            event.setEnd(endDate);
            event.setSummary(mCalendarEvent.getTitle());
            event.setDescription(mCalendarEvent.getDescription());

            mService.events().update(CALENDAR_NAME, event.getId(), event).execute();
        }else{
            Log.w(LOG_TAG, "Not updating calendar event with title \"" + mCalendarEvent.getTitle()+"\" since it does not have a start and end time");
        }
    }

    @Override
    protected void onPreExecute(){
        Log.d(LOG_TAG, "Preparing to run UpdateCalendarItemTask");
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        Log.d(LOG_TAG, "UpdateCalendarItemTask finished executingTask");
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
            Log.e(LOG_TAG, "UpdateCalendarItemTask Request Canceled");
        }
    }
}
