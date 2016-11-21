package me.kainoseto.todo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;

import com.facebook.stetho.Stetho;

import java.util.ArrayList;

import me.kainoseto.todo.Database.TodoListDbHelper;
import me.kainoseto.todo.Preferences.PreferencesManager;
import me.kainoseto.todo.UI.TodoItem;
import me.kainoseto.todo.UI.TodoListAdapter;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    RecyclerView todoListView;
    TodoListAdapter listAdapter;
    ArrayList<TodoItem> todoItems;
    CoordinatorLayout cl;
    public static final int SETTINGS_INTENT_RESULT_KEY = 4001;
    public static TodoListDbHelper todoDbHelper;
    public static PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (preferencesManager == null) {
            preferencesManager = new PreferencesManager(getApplicationContext());
            preferencesManager.createPref(PreferencesManager.KEY_MAINPREFS, PreferencesManager.PRIVATE_MODE);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        if(todoDbHelper == null) {
            Stetho.newInitializerBuilder(this)
                    .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                    .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                    .build();
            Stetho.initializeWithDefaults(this);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_additem);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent detailViewIntent = new Intent(getApplicationContext(), EditItemDetailActivity.class);
                detailViewIntent.putExtra("NEW", true);
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

        if( todoDbHelper == null) {
            todoDbHelper = new TodoListDbHelper(getApplicationContext());
        }

        todoItems = new ArrayList<>();

        todoListView = (RecyclerView) findViewById(R.id.listRecycler);
        listAdapter = new TodoListAdapter(this, todoItems);
        todoListView.setAdapter(listAdapter);
        todoListView.setLayoutManager(new LinearLayoutManager(this));

        UpdateList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(preferencesManager.getSharedPref().getBoolean(PreferencesManager.KEY_THEME, false)) {
            setTheme(R.style.LightTheme_NoActionBar);
            cl.setBackgroundColor(ContextCompat.getColor(this, R.color.colorLightBackground));
        } else {
            setTheme(R.style.DarkTheme_NoActionBar);
            cl.setBackgroundColor(ContextCompat.getColor(this, R.color.windowBackground));
        }
        UpdateList();
        getSupportActionBar().setTitle(preferencesManager.getSharedPref().getString(PreferencesManager.KEY_LISTNAME, "ToDo"));
    }

    public void UpdateList() {
        todoItems.clear();
        todoDbHelper.getAllItems(todoItems);
        todoListView.setAdapter(listAdapter);
    }

}
