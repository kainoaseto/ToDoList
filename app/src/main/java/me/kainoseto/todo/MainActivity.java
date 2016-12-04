package me.kainoseto.todo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.facebook.stetho.Stetho;

import java.util.List;

import me.kainoseto.todo.Calendar.GoogleCalendarManager;
import me.kainoseto.todo.Callback.SimpleItemTouchHelperCallback;
import me.kainoseto.todo.Content.TodoContentManager;
import me.kainoseto.todo.Database.ContentManager;
import me.kainoseto.todo.Preferences.PreferencesManager;
import me.kainoseto.todo.UI.TodoListAdapter;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks
{
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private RecyclerView todoListView;
    private TodoListAdapter listAdapter;
    Stetho.Initializer initializer;

    private ContentManager contentManager;
    private GoogleCalendarManager calendarManager;

    /* TODO: Setup a proper theme management system by having a base layout all go off it
       a theme manager class can set which theme and it will update for all activities based
       off that layout.
    */
    CoordinatorLayout cl;
    public static final int SETTINGS_INTENT_RESULT_KEY = 4001;
    public static PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Setup a better pref management system since this may not be neccessary anymore
        if (preferencesManager == null) {
            preferencesManager = new PreferencesManager(getApplicationContext());
            preferencesManager.createPref(PreferencesManager.KEY_MAINPREFS, PreferencesManager.PRIVATE_MODE);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Remove this since its really hacky and heed the above TODO statement
        cl = (CoordinatorLayout) findViewById(R.id.actvity_main);
        if(preferencesManager.getSharedPref().getBoolean(PreferencesManager.KEY_THEME, false)) {
            setTheme(R.style.LightTheme_NoActionBar);
            cl.setBackgroundColor(ContextCompat.getColor(this, R.color.colorLightBackground));
        } else {
            setTheme(R.style.DarkTheme_NoActionBar);
            cl.setBackgroundColor(ContextCompat.getColor(this, R.color.windowBackground));
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(preferencesManager.getSharedPref().getString(PreferencesManager.KEY_LISTNAME, "ToDo"));

        // This is populated only on the first run so initialize our online debugger for sqlite
        if(initializer == null)
        {
            initializer = Stetho.newInitializerBuilder(this)
                    .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                    .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                    .build();
            Stetho.initialize(initializer);
            //Stetho.initializeWithDefaults(this);
        }

        FloatingActionButton createItemButton = (FloatingActionButton) findViewById(R.id.fab_additem);
        createItemButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent detailViewIntent = new Intent(getApplicationContext(), EditItemDetailActivity.class);
                detailViewIntent.putExtra(getString(R.string.intent_create_item), true);
                startActivity(detailViewIntent);
            }
        });

        ImageButton settingsBtn = (ImageButton) findViewById(R.id.settings_toolbar_button);
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingsViewIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivityForResult(settingsViewIntent, SETTINGS_INTENT_RESULT_KEY);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        // Initialize the singleton contentManager
        if( contentManager == null )
        {
            TodoContentManager.initInstance(getApplicationContext());
            contentManager = TodoContentManager.getInstance();
        }

        //TODO: Check if google calendar functionality has been selected. If it has been selected and is enabled then initialize GoogleCalendarManager. Otherwise redirect to first time screen.
        calendarManager = GoogleCalendarManager.getInstance(getApplicationContext());
        //TODO REVISE ABOVE

        todoListView        = (RecyclerView) findViewById(R.id.listRecycler);
        listAdapter         = new TodoListAdapter(this);

        todoListView.setAdapter(listAdapter);
        todoListView.setLayoutManager(new LinearLayoutManager(this));

        //For swipe and drag of todo items
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(listAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(todoListView);

        UpdateList();
    }

    @Override
    protected void onStart(){
        super.onStart();
        //TODO: More calendar stuff
        calendarManager.makeApiCall(this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        //TODO: More calendar stuff
        calendarManager.makeApiCall(this);

        if(preferencesManager.getSharedPref().getBoolean(PreferencesManager.KEY_THEME, false))
        {
            setTheme(R.style.LightTheme_NoActionBar);
            cl.setBackgroundColor(ContextCompat.getColor(this, R.color.colorLightBackground));
        }
        else
        {
            setTheme(R.style.DarkTheme_NoActionBar);
            cl.setBackgroundColor(ContextCompat.getColor(this, R.color.windowBackground));
        }
        UpdateList();
        getSupportActionBar().setTitle(preferencesManager.getSharedPref().getString(PreferencesManager.KEY_LISTNAME, "ToDo"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        calendarManager.handleOnActivityResult(requestCode, resultCode, data, this);
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     * @param requestCode The request code passed in
     *     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.d(LOG_TAG, "Permissions granted with code " + requestCode);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.w(LOG_TAG, "Permissions denied with code " + requestCode);
    }

    public void UpdateList()
    {
        todoListView.setAdapter(listAdapter);
    }
}
