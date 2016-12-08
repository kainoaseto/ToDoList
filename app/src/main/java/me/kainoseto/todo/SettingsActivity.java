package me.kainoseto.todo;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.SwitchPreference;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.widget.Toast;

import java.util.List;

import me.kainoseto.todo.Calendar.GoogleCalendarManager;
import me.kainoseto.todo.Content.TodoContentManager;
import me.kainoseto.todo.Database.ContentManager;
import me.kainoseto.todo.Preferences.PreferencesManager;


public class SettingsActivity extends AppCompatPreferenceActivity {

    static SharedPreferences sharedPreferences;
    private static ContentManager contentManager;
    private static GoogleCalendarManager calendarManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();

        contentManager = TodoContentManager.getInstance();

        sharedPreferences = MainActivity.preferencesManager.getSharedPref();

        //getting an instance of calendar manager to be used for handling google login
        calendarManager = GoogleCalendarManager.getInstance(getApplicationContext());

        getFragmentManager().beginTransaction().add(android.R.id.content,
                new GeneralPreferenceFragment()).commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Handling navigation back from google sign in activity
        calendarManager.handleOnActivityResult(requestCode, resultCode, data, this, true);

    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            if (preference.getKey().equals("light_theme_pref")) {
                MainActivity.preferencesManager.storeValue(
                        PreferencesManager.KEY_MAINPREFS,
                        PreferencesManager.PRIVATE_MODE,
                        preference.getKey(),
                        (Boolean)value
                );
                return true;
            }
            else if (preference.getKey().equals("pref_enable_gcal")) {
                MainActivity.preferencesManager.storeValue(
                        PreferencesManager.KEY_MAINPREFS,
                        PreferencesManager.PRIVATE_MODE,
                        preference.getKey(),
                        (Boolean)value
                );
                return true;
            }
            else {
                MainActivity.preferencesManager.storeValue(
                        PreferencesManager.KEY_MAINPREFS,
                        PreferencesManager.PRIVATE_MODE,
                        preference.getKey(),
                        value.toString()
                );
            }

            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    // TODO Make the defaultVal class ambiguous
    private static void bindPreferenceSummaryToValue(Preference preference, Class<?> type)
    {
        bindPreferenceSummaryToValue(preference, type, "");
    }


    private static void bindPreferenceSummaryToValue(Preference preference, Class<?> type, String defaultVal)
    {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        if(type == String.class) {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    sharedPreferences.getString(preference.getKey(), defaultVal));
        } else if (type == Boolean.class) {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    sharedPreferences.getBoolean(preference.getKey(), false));
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            PreferenceCategory generalCategory = (PreferenceCategory) findPreference("general_settings");
            generalCategory.removePreference(findPreference("light_theme_pref"));

            SwitchPreference calendarSync = (SwitchPreference) findPreference("pref_enable_gcal");
            calendarSync.setChecked(sharedPreferences.getBoolean("pref_enable_gcal", false));



            //bindPreferenceSummaryToValue(findPreference("light_theme_pref"), Boolean.class);
            bindPreferenceSummaryToValue(findPreference("list_name_pref"), String.class, "ToDo");
            bindPreferenceSummaryToValue(findPreference("pref_calendar_name"), String.class, getString(R.string.pref_default_calendar_name));
            bindPreferenceSummaryToValue(findPreference("pref_enable_gcal"), Boolean.class);

            Preference resetButton = findPreference("reset_pref");
            resetButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    contentManager.resetContent();
                    MainActivity.preferencesManager.clearPrefs(PreferencesManager.KEY_MAINPREFS, PreferencesManager.PRIVATE_MODE);
                    Toast.makeText(getContext(), R.string.reset_app_toast, Toast.LENGTH_SHORT).show();
                    Intent returnMain = new Intent(getActivity(), MainActivity.class);
                    startActivity(returnMain);

                    return false;
                }
            });

            Preference enableCal = findPreference("pref_enable_gcal");
            enableCal.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if(sharedPreferences.getBoolean(preference.getKey(), false)){
                        calendarManager.checkGoogleCalendarRequirements(getActivity());
                    }
                    return false;
                }
            });

        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), MainActivity.class));
                getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                Intent intent = NavUtils.getParentActivityIntent(this);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                NavUtils.navigateUpTo(this, intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            //bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    /*@TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DataSyncPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_data_sync);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("sync_frequency"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }*/
}
