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
import com.google.api.services.calendar.model.EventReminder;

import org.apache.commons.codec.binary.Base32;

import java.io.IOException;
import java.util.Arrays;

import me.kainoseto.todo.Calendar.CalendarAware;
import me.kainoseto.todo.Calendar.CalendarEvent;
import me.kainoseto.todo.Calendar.GoogleCalendarManager;
import me.kainoseto.todo.Calendar.PlayServicesUtil;
import me.kainoseto.todo.Util.StringUtil;

/**
 * Created by TYLER on 12/6/2016.
 */

public class PostCalendarItemTask extends AsyncTask {
    private com.google.api.services.calendar.Calendar mService;
    private Exception mLastError;
    private Activity mActivity;
    private Base32 base32 = new Base32(true);
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
        if((null != mCalendarEvent.getStartDate()) && (null != mCalendarEvent.getendDate())){
            Event event = new Event()
                    .setSummary(mCalendarEvent.getTitle())
                    .setDescription(mCalendarEvent.getDescription());

            EventDateTime startDateTime = new EventDateTime().setDateTime(mCalendarEvent.getStartDate());
            event.setStart(startDateTime);

            EventDateTime endDateTime = new EventDateTime().setDateTime(mCalendarEvent.getendDate());
            event.setEnd(endDateTime);
            event.setId(StringUtil.formatCalendarItemId(mCalendarEvent.getTitle()));

            //TODO: Seting one hour warning, eventually needs configurable time
            EventReminder[] remindersOverides = new EventReminder[]{
                    new EventReminder().setMethod("popup").setMinutes(60)
            };
            Event.Reminders reminders = new Event.Reminders().setUseDefault(false).setOverrides(Arrays.asList(remindersOverides));
            event.setReminders(reminders);

            mService.events().insert(CALENDAR_NAME, event).execute();
        }else{
            Log.w(LOG_TAG, "Not inserting calendar event with title \"" + mCalendarEvent.getTitle()+"\" since it does not have a start and end time");
        }
    }

    @Override
    protected void onPreExecute(){
        Log.d(LOG_TAG, "Preparing to run PostCalendarItemTask");
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        Log.d(LOG_TAG, "PostCalendarItem finished executingTask");
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
