package co.xelabs.todo;

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

import co.xelabs.todo.Calendar.CalendarAware;
import co.xelabs.todo.Calendar.CalendarEvent;
import co.xelabs.todo.Calendar.GoogleCalendarManager;
import co.xelabs.todo.Callback.SimpleItemTouchHelperCallback;
import co.xelabs.todo.Content.TodoContentManager;
import co.xelabs.todo.Database.ContentManager;
import co.xelabs.todo.Preferences.PreferencesManager;
import co.xelabs.todo.UI.TodoListAdapter;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, CalendarAware
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
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        // Initialize the singleton contentManager
        if( contentManager == null )
        {
            TodoContentManager.initInstance(this, this);
            contentManager = TodoContentManager.getInstance();
        }
        contentManager.UpdateActivity(this);

        calendarManager = GoogleCalendarManager.getInstance(getApplicationContext());

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
    protected void onResume()
    {
        super.onResume();
        UpdateList();
        getSupportActionBar().setTitle(preferencesManager.getSharedPref().getString(PreferencesManager.KEY_LISTNAME, "ToDo"));
        contentManager.UpdateActivity(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        calendarManager.handleOnActivityResult(requestCode, resultCode, data, this, false);
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

    //Handlers for easy permissions
    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.d(LOG_TAG, "Permissions granted with code " + requestCode);
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.w(LOG_TAG, "Permissions denied with code " + requestCode);
    }

    //Callbacks from Google Calendar Sync
    @Override
    public void onGetCalendarItemsResult(List<CalendarEvent> events) {
        Log.d(LOG_TAG, "Received Calendar Items");
        contentManager.syncWithCalendarEvents(events);
    }

    @Override
    public void onPostCalendarItemsResult() {
        Log.d(LOG_TAG, "Completed adding new calendar item");
    }

    @Override
    public void onDeleteCalendarItemResult() {
        Log.d(LOG_TAG, "Completed deleting a calendar item");
    }

    @Override
    public void onUpdateCalendarItemResult() {
        Log.d(LOG_TAG, "Completed updating a calendar item");
    }

    public void UpdateList()
    {
        if(GoogleCalendarManager.isCalendarEnabled()){
            calendarManager.getCalendarItems(this, this, calendarManager.getCalendarName());
        }
        todoListView.setAdapter(listAdapter);
    }
}
