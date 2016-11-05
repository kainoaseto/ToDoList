package me.kainoseto.todo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.facebook.stetho.Stetho;

import java.util.ArrayList;

import me.kainoseto.todo.Database.TodoListDbHelper;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    RecyclerView todoListView;
    TodoListAdapter listAdapter;
    ArrayList<TodoItem> todoItems;
    public static final int SETTINGS_INTENT_RESULT_KEY = 4001;
    public static TodoListDbHelper todoDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
        UpdateList();
    }

    public void UpdateList() {
        todoItems.clear();
        todoDbHelper.getAllItems(todoItems);
        todoListView.setAdapter(listAdapter);
    }
}
