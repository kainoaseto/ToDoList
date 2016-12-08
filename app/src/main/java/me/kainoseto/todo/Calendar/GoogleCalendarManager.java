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
import android.support.v7.app.AppCompatActivity;
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

import me.kainoseto.todo.Calendar.Tasks.DeleteCalendarItemTask;
import me.kainoseto.todo.Calendar.Tasks.GetCalendarItemsTask;
import me.kainoseto.todo.Calendar.Tasks.PostCalendarItemTask;
import me.kainoseto.todo.Content.TodoItem;
import me.kainoseto.todo.MainActivity;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by TYLER on 12/3/2016.
 */

public class GoogleCalendarManager {
    private static GoogleCalendarManager sSingleton;
    private static SharedPreferences sharedPreferences;

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

    private static AsyncTask currOp;

    public static GoogleCalendarManager getInstance(Context context) {
        if (null == sSingleton) {
            sSingleton = new GoogleCalendarManager(context);
            sharedPreferences = MainActivity.preferencesManager.getSharedPref();
        }
        return sSingleton;
    }

    private GoogleCalendarManager(Context context) {
        mCredential = GoogleAccountCredential.usingOAuth2(context, Arrays.asList(SCOPES)).setBackOff(new ExponentialBackOff());
    }

    /**
     * Sets up the necessary permissions and credentials to use the Google Calendar api, then retrieves a weeks worth of events.
     * NOTE: The calling activity needs to call handleOnActivityResult inside onActivityResult in order to handle navigation to the google sign in activity
     *
     * @param activity - activity to display google account related UI on.
     * @param calendarAware - class that contains callbacks for when async processing concludes
     * @param calendarName - name of the calendar
     */
    public synchronized void getCalendarItems(Activity activity, CalendarAware calendarAware, String calendarName){
        currOp = new GetCalendarItemsTask(mCredential, activity, calendarAware, calendarName);
        makeApiCall(activity);
    }

    /**
     * Sets up the necessary permissions and credentials to use the Google Calendar api, then creates a new event within the specified calendar.
     * NOTE: The calling activity needs to call handleOnActivityResult inside onActivityResult in order to handle navigation to the google sign in activity
     *
     * @param activity - activity to display google account related UI on.
     * @param calendarAware - class that contains callbacks for when async processing concludes
     * @param calendarName - name of the calendar
     * @param calendarEvent - contains data about the new calendar event
     */
     public synchronized void createCalendarItem(Activity activity, CalendarAware calendarAware, String calendarName, CalendarEvent calendarEvent){
         currOp = new PostCalendarItemTask(mCredential, activity, calendarAware, calendarName, calendarEvent);
         makeApiCall(activity);
    }

    /**
     * Sets up the necessary permissions and credentials to use the Google Calendar api, then deletes an event with the specified title
     * NOTE: The calling activity needs to call handleOnActivityResult inside onActivityResult in order to handle navigation to the google sign in activity
     *
     * @param activity - activity to display google account related UI on.
     * @param calendarAware - class that contains callbacks for when async processing concludes
     * @param calendarName - name of the calendar
     * @param title - title of the event to delete
     */
    public synchronized void deleteCalendarItem(Activity activity, CalendarAware calendarAware, String calendarName, String title){
        currOp = new DeleteCalendarItemTask(mCredential, activity, calendarAware, calendarName, title);
        makeApiCall(activity);
    }

    /**
     * Ensures Google Play Services are installed, a google account is selected, and the device has internet access
     *
     * @param activity
     */
    public void checkGoogleCalendarRequirements(Activity activity){
        if (!PlayServicesUtil.isGooglePlayServicesAvailable(activity.getApplicationContext())) {
            PlayServicesUtil.acquireGooglePlayServices(activity);
        }else if(mCredential.getSelectedAccountName() == null){
            chooseAccount(activity);
        }else if(!isDeviceOnline(activity.getApplicationContext())){
            Toast.makeText(activity.getApplicationContext(), "No network connection available", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     *  Gets permissions and credentials for the Google Calendar API, then runs an async task.
     *  NOTE: The calling activity needs to call handleOnActivityResult inside onActivityResult in order to handle navigation to the google sign in activity
     *  synchronized so nobody else updates currOp while making an API call
     *
     * @param activity - activity to display google account related UI on.
     */
    private void makeApiCall(Activity activity) {
        Context context = activity.getApplicationContext();
        if (!PlayServicesUtil.isGooglePlayServicesAvailable(activity.getApplicationContext())) {
            PlayServicesUtil.acquireGooglePlayServices(activity);
        }else if(mCredential.getSelectedAccountName() == null){
            chooseAccount(activity);
        }else if(!isDeviceOnline(context)){
            Toast.makeText(context, "No network connection available", Toast.LENGTH_SHORT).show();
        }else{
            currOp.execute();
        }
    }

    /**
     * If makeApiCall is called, then handleOnActivityResult needs to be called inside onActivityResult
     *
     * @param requestCode
     * @param resultCode
     * @param data
     * @param activity
     * @param loginOnly - if true then redirect to check calendar requirements
     */
    public void handleOnActivityResult(int requestCode, int resultCode, Intent data, Activity activity, boolean loginOnly){
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != Activity.RESULT_OK) {
                    Toast.makeText(activity.getApplicationContext(),"This app requires Google Play Services. Please install Google Play Services on your device and relaunch this app.", Toast.LENGTH_SHORT);
                } else {
                    if(loginOnly){
                        checkGoogleCalendarRequirements(activity);
                    }else{
                        makeApiCall(activity);
                    }
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
                        if(loginOnly){
                            checkGoogleCalendarRequirements(activity);
                        }else{
                            makeApiCall(activity);
                        }
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == Activity.RESULT_OK) {
                    if(loginOnly){
                        checkGoogleCalendarRequirements(activity);
                    }else{
                        makeApiCall(activity);
                    }
                }
                break;
        }
    }

    public static boolean isCalendarEnabled(){
        //getting shared prefs to avoid NPE if we dont create a calendar manager
        return MainActivity.preferencesManager.getSharedPref().getBoolean("pref_enable_gcal", false);
    }

    public String getCalendarName(){
        return sharedPreferences.getString("pref_calendar_name", "primary");
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
}