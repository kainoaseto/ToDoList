package me.kainoseto.todo.Calendar;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by TYLER on 12/3/2016.
 */

public class GoogleCalendarManager {

    private static GoogleCalendarManager sSingleton;

    //Vars for Google Calendar API
    private GoogleAccountCredential mCredential;
    private static final String[] SCOPES = {CalendarScopes.CALENDAR};

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String APP_NAME = "TodoList";
    private static final String CALENDAR_NAME = "primary";
    private static final String LOG_TAG = GoogleCalendarManager.class.getCanonicalName();

    public static GoogleCalendarManager getInstance(Context context) {
        if (null == sSingleton) {
            //TODO See what else needs to be done here
            sSingleton = new GoogleCalendarManager(context);
        }
        return sSingleton;
    }

    private GoogleCalendarManager(Context context) {
        mCredential = GoogleAccountCredential.usingOAuth2(context, Arrays.asList(SCOPES)).setBackOff(new ExponentialBackOff());
    }

    public void getEvents(){
        //TODO call makeApiCall with get async task
    }

    public void makeApiCall(Activity activity) {
        Context context = activity.getApplicationContext();
        if (!PlayServicesUtil.isGooglePlayServicesAvailable(activity.getApplicationContext())) {
            PlayServicesUtil.acquireGooglePlayServices(activity);
        }else if(mCredential.getSelectedAccountName() == null){
            chooseAccount(activity);
        }else if(!isDeviceOnline(context)){
            Toast.makeText(context, "No network connection available", Toast.LENGTH_SHORT).show();
        }else{
            //TODO: execute either a get all task from the calendar of an add new task to calendar
            new GetEventsTask(mCredential, activity).execute();
        }

    }

    /**
     * If makeApiCall is called, then handleOnActivityResult needs to be called inside onActivityResult
     *
     * @param requestCode
     * @param resultCode
     * @param data
     * @param activity
     */
    public void handleOnActivityResult(int requestCode, int resultCode, Intent data, Activity activity){
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != Activity.RESULT_OK) {
                    Toast.makeText(activity.getApplicationContext(),"This app requires Google Play Services. Please install Google Play Services on your device and relaunch this app.", Toast.LENGTH_SHORT);
                } else {
                    makeApiCall(activity);
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings = activity.getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        makeApiCall(activity);
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == Activity.RESULT_OK) {
                    makeApiCall(activity);
                }
                break;
        }
    }


    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount(Activity activity) {
        if (EasyPermissions.hasPermissions(activity, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = activity.getPreferences(Context.MODE_PRIVATE).getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                makeApiCall(activity);
            } else {
                // Start a dialog from which the user can choose an account
                activity.startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(activity, "This app needs to access your Google account (via Contacts).", REQUEST_PERMISSION_GET_ACCOUNTS, Manifest.permission.GET_ACCOUNTS);
        }
    }


    /**
     * Checks whether the device currently has a network connection.
     *
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * An asynchronous task that handle making the Google Calendar API call that retrieves upcomming events.
     */
    public class GetEventsTask extends AsyncTask<Void, Void, List<CalendarEvent>>{
        private com.google.api.services.calendar.Calendar mService;
        private Exception mLastError;
        private Activity mActivity;

        GetEventsTask(GoogleAccountCredential credential, Activity activity){
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(transport, jsonFactory, credential).setApplicationName(APP_NAME).build();
            mActivity = activity;
        }

        @Override
        protected List<CalendarEvent> doInBackground(Void... params) {
            try{
                return getEventsFromApi();
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
                    .setMaxResults(10)
                    .setTimeMin(now)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();

            List<Event> items = events.getItems();

            for(Event event: items){
                calendarEvents.add(new CalendarEvent(event.getSummary(), event.getDescription(), event.getStart().getDateTime()));
            }

            return calendarEvents;
        }

        @Override
        protected void onPreExecute(){
            Log.d(LOG_TAG, "Preparing to run GetEventTask");
        }

        @Override
        protected void onPostExecute(List<CalendarEvent> output){
            Log.d(LOG_TAG, "GetEventTask returned "+output.size()+" results");
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
                Log.e(LOG_TAG, "GetEventTask Request Canceled");
            }
        }
    }
}