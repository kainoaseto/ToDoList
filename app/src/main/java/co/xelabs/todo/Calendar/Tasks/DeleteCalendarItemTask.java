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

import java.io.IOException;

import co.xelabs.todo.Calendar.CalendarAware;
import co.xelabs.todo.Calendar.GoogleCalendarManager;
import co.xelabs.todo.Calendar.PlayServicesUtil;

/**
 * Created by TYLER on 12/6/2016.
 */

public class DeleteCalendarItemTask extends AsyncTask {
    private com.google.api.services.calendar.Calendar mService;
    private Exception mLastError;
    private Activity mActivity;
    private CalendarAware mCalendarAware;

    private static final String APP_NAME = "TodoList";
    private static final String LOG_TAG = DeleteCalendarItemTask.class.getCanonicalName();
    private String CALENDAR_NAME;
    private String mEventId;

    public DeleteCalendarItemTask(GoogleAccountCredential credential, Activity activity, CalendarAware calendarAware, String calendarName, String eventId){
        super();
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.calendar.Calendar.Builder(transport, jsonFactory, credential).setApplicationName(APP_NAME).build();
        mActivity = activity;
        mCalendarAware = calendarAware;

        mEventId = eventId;
        CALENDAR_NAME = calendarName;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        try{
            deleteEventFromCalendar();
            mCalendarAware.onDeleteCalendarItemResult(); //Notifying activty that the item has been deleted
        }catch(Exception e){
            mLastError = e;
            cancel(true);
        }
        return null;
    }

    private void deleteEventFromCalendar() throws IOException {
        mService.events().delete(CALENDAR_NAME, mEventId).execute();
    }

    @Override
    protected void onPreExecute(){
        Log.d(LOG_TAG, "Preparing to run DeleteCalendarItemTask");
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        Log.d(LOG_TAG, "DeleteCalendarItemTask finished executing");
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
            Log.e(LOG_TAG, "DeleteCalendarItem Request Canceled");
        }
    }
}
