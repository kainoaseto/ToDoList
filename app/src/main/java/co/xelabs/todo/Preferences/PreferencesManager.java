package co.xelabs.todo.Preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class PreferencesManager
{

    private Context _context;
    private Editor editor;
    private SharedPreferences sharedPref = null;

    public static final int PRIVATE_MODE     = 0;

    // Keys should always be defined here for reference
    public static final String KEY_LISTNAME              = "list_name_pref";
    public static final String KEY_THEME                 = "light_theme_pref";
    public static final String KEY_CALNAME               = "pref_calendar_name";
    public static final String KEY_GCAL_ENABLE           = "pref_enable_gcal";
    public static final String KEY_MAINPREFS             = "main_prefs";

    public PreferencesManager(Context context)
    {
        this._context = context;
    }

    public void createPref(String prefName, int mode)
    {
        sharedPref = _context.getSharedPreferences(prefName, mode);
        editor = sharedPref.edit();
        editor.commit();
    }

    public SharedPreferences getSharedPref() {
        return sharedPref;
    }

    public void storeValue(String prefName, int mode, String key, String var)
    {
        // prefName should be the preferences you are trying to store something in
        // the key is the keyvalue that will be referenced with the value being stored
        // Mode should almost always be 0 for private but the option is there to change
        // references the sharedPreferances based on the prefName Parameter and
        // stores a string.
        sharedPref = _context.getSharedPreferences(prefName, mode);
        editor = sharedPref.edit();
        editor.putString(key, var);
        editor.commit();
    }

    public void storeValue(String prefName, int mode, String key, int var)
    {
        // prefName should be the preferences you are trying to store something in
        // the key is the keyvalue that will be referenced with the value being stored
        // Mode should almost always be 0 for private but the option is there to change
        // references the sharedPreferances based on the prefName Parameter and
        // stores a integer.
        sharedPref = _context.getSharedPreferences(prefName, mode);
        editor = sharedPref.edit();
        editor.putInt(key, var);
        editor.commit();
    }

    public void storeValue(String prefName, int mode, String key, boolean var)
    {
        // prefName should be the preferences you are trying to store something in
        // the key is the keyvalue that will be referenced with the value being stored
        // Mode should almost always be 0 for private but the option is there to change
        // references the sharedPreferances based on the prefName Parameter and
        // stores a integer.
        sharedPref = _context.getSharedPreferences(prefName, mode);
        editor = sharedPref.edit();
        editor.putBoolean(key, var);
        editor.commit();
    }

    public String getStoredString(String prefName, int mode, String key)
    {
        sharedPref = _context.getSharedPreferences(prefName, mode);
        editor = sharedPref.edit();

        try
        {
            return  sharedPref.getString(key, null);
        }
        catch(NullPointerException e)
        {
            e.printStackTrace();
            return null;
        }

    }

    public int getStoredInt(String prefName, int mode, String key)
    {
        sharedPref = _context.getSharedPreferences(prefName, mode);
        editor = sharedPref.edit();

        try
        {
            return sharedPref.getInt(key, 0);
        }
        catch(NullPointerException e)
        {
            e.printStackTrace();
            return 0;
        }

    }

    public void clearStoredValue(String prefName, int mode, String key)
    {
        sharedPref = _context.getSharedPreferences(prefName, mode);
        editor = sharedPref.edit();

        // Clearing all data from sharedPreferences with name prefName
        editor.remove(key);
        editor.commit();
    }

    public void clearPrefs(String prefName, int mode)
    {
        sharedPref = _context.getSharedPreferences(prefName, mode);
        editor = sharedPref.edit();

        // Clearing all data from sharedPreferences with name prefName
        editor.clear();
        editor.commit();
    }

    public void storeArray(String prefName, int mode, String ID, String[] array)
    {
        sharedPref = _context.getSharedPreferences(prefName, mode);
        editor = sharedPref.edit();
        for(int i = 0; i < array.length; i ++)
        {
            editor.putString(ID + i, array[i]);
            Log.i("PreferancesManager", "Array values added" );
            editor.commit();
        }
    }

    public void storeArrayList(String prefName, int mode, String ID, List<Integer> arrayList)
    {
        sharedPref = _context.getSharedPreferences(prefName, mode);
        editor = sharedPref.edit();
        if(arrayList.isEmpty())
        {
            // Its null so nothing to store
            return;
        }
        for(int i = 0; i < arrayList.size(); i ++)
        {
            editor.putInt(ID + i, arrayList.get(i));
            Log.i("PreferancesManager", "Array values added" );
            editor.commit();
        }
    }

    public List<Integer> getArray(String prefName, int mode, String key)
    {
        sharedPref = _context.getSharedPreferences(prefName, mode);
        editor = sharedPref.edit();
        List<Integer> arrayList = new ArrayList<Integer>();

        for(int i = 0; i < arrayList.size(); i ++)
        {
            editor.putInt(key + i, arrayList.get(i));
            Log.i("PreferancesManager", "Array values added" );
            editor.commit();
        }
        return arrayList;
    }
}
