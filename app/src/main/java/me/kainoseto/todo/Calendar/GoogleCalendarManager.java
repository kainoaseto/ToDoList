package me.kainoseto.todo.Calendar;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;

import java.util.Arrays;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by TYLER on 12/3/2016.
 */

public class GoogleCalendarManager {

    private static GoogleCalendarManager singleton;

    //Vars for Google Calendar API
    private static GoogleAccountCredential mCredential;
    private static final String[] SCOPES = {CalendarScopes.CALENDAR};

    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    private static final String PREF_ACCOUNT_NAME = "accountName";

    public static GoogleCalendarManager getInstance(Context context) {
        if (null == singleton) {
            //TODO init stuff here
        }
        return singleton;
    }

    private GoogleCalendarManager(Context context) {
        mCredential = GoogleAccountCredential.usingOAuth2(context, Arrays.asList(SCOPES)).setBackOff(new ExponentialBackOff());
    }

    public void makeApiCall(Activity activity) {
        Context context = activity.getApplicationContext();
        if (!PlayServicesUtil.isGooglePlayServicesAvailable(activity.getApplicationContext())) {
            PlayServicesUtil.acquireGooglePlayServices(activity);
        }else if(mCredential.getSelectedAccountName() == null){
            chooseAccount(activity);
        }else if(!isDeviceOnline(context)){
            Toast.makeText(context, "No network connection available", Toast.LENGTH_SHORT);
        }else{
            //execute either a get all task from the calendar of an add new task to calendar
        }

    }

    public void setSelectedAccountName(String accountName){
        mCredential.setSelectedAccountName(accountName);
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
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
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