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
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import me.kainoseto.todo.Calendar.Tasks.GetCalendarItemsTask;
import me.kainoseto.todo.Calendar.Tasks.PostCalendarItemTask;
import me.kainoseto.todo.Content.TodoItem;
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

    public static final int REQUEST_ACCOUNT_PICKER = 1000;
    public static final int REQUEST_AUTHORIZATION = 1001;
    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    public static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

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

    /**
     *  Gets permissions and credentials for the Google Calendar API, then runs an async task.
     *
     * @param activity - activity to display google account related UI on.
     * @param calendarAware - class that will handle the result of the calendar api call. Can be the same as the activity.
     */
    public void makeApiCall(Activity activity, CalendarAware calendarAware) {
        Context context = activity.getApplicationContext();
        if (!PlayServicesUtil.isGooglePlayServicesAvailable(activity.getApplicationContext())) {
            PlayServicesUtil.acquireGooglePlayServices(activity);
        }else if(mCredential.getSelectedAccountName() == null){
            chooseAccount(activity, calendarAware);
        }else if(!isDeviceOnline(context)){
            Toast.makeText(context, "No network connection available", Toast.LENGTH_SHORT).show();
        }else{
            CalendarEvent calendarEvent = new CalendarEvent("Test","Test", new DateTime(System.currentTimeMillis()), new DateTime(System.currentTimeMillis()));
            new PostCalendarItemTask(mCredential, activity, calendarAware, CALENDAR_NAME, calendarEvent).execute();
        }
    }

    /**
     * If makeApiCall is called, then handleOnActivityResult needs to be called inside onActivityResult
     *
     * @param requestCode
     * @param resultCode
     * @param data
     * @param activity
     * @param calendarAware - needed to retry make api call after redirects complete
     */
    public void handleOnActivityResult(int requestCode, int resultCode, Intent data, Activity activity, CalendarAware calendarAware){
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != Activity.RESULT_OK) {
                    Toast.makeText(activity.getApplicationContext(),"This app requires Google Play Services. Please install Google Play Services on your device and relaunch this app.", Toast.LENGTH_SHORT);
                } else {
                    makeApiCall(activity, calendarAware);
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
                        makeApiCall(activity, calendarAware);
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == Activity.RESULT_OK) {
                    makeApiCall(activity, calendarAware);
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
    private void chooseAccount(Activity activity, CalendarAware calendarAware) {
        if (EasyPermissions.hasPermissions(activity, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = activity.getPreferences(Context.MODE_PRIVATE).getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                makeApiCall(activity, calendarAware);
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
     * An asynchronous task that handle making the Google Calendar API call that inserts a new event.
     */
    public class PostEventTask extends AsyncTask<Void, Void, Boolean>{
        private com.google.api.services.calendar.Calendar mService;
        private Exception mLastError;
        private Activity mActivity;
        private TodoItem mTodoItem;

        PostEventTask(GoogleAccountCredential credential, Activity activity, TodoItem todoItem){
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(transport, jsonFactory, credential).setApplicationName(APP_NAME).build();
            mActivity = activity;
            mTodoItem = todoItem;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try{
                addEventToCalendar();
                return true;
            }catch(Exception e){
                mLastError = e;
                cancel(true);
                return false;
            }
        }

        private void addEventToCalendar() throws IOException {
            Event content = new Event();
            EventDateTime dt = new EventDateTime();
            dt.setDateTime(new DateTime(System.currentTimeMillis()));
            content.setSummary(mTodoItem.getName());
            content.setDescription(generateDescription());
            content.setStart(dt);

            Event ret = mService.events().insert(CALENDAR_NAME, content).execute();
            System.out.println("");
        }

        private String generateDescription(){
            //TODO: Generate the description from subtasks
            StringBuilder desc = new StringBuilder();
            return "";
        }

        @Override
        protected void onPreExecute(){
            Log.d(LOG_TAG, "Preparing to run PostEventTask");
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            Log.d(LOG_TAG, "Finished running PostEventTask");
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
                Log.e(LOG_TAG, "PostEventTask Request Canceled");
            }
        }
    }
}