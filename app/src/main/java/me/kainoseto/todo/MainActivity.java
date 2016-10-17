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

import com.facebook.stetho.Stetho;

import java.util.ArrayList;

import me.kainoseto.todo.Database.TodoListDbHelper;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    RecyclerView todoListView;
    TodoListAdapter listAdapter;
    ArrayList<TodoItem> todoItems;

    public static TodoListDbHelper todoDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        if( todoDbHelper == null) {
            todoDbHelper = new TodoListDbHelper(getApplicationContext());
        }

        todoItems = new ArrayList<>();

        todoListView = (RecyclerView) findViewById(R.id.listRecycler);
        listAdapter = new TodoListAdapter(this, todoItems);
        todoListView.setAdapter(listAdapter);
        todoListView.setLayoutManager(new LinearLayoutManager(this));

        UpdateList();
        Log.d(LOG_TAG, "onCreate()");
    }

    @Override
    protected void onResume() {
        Log.d(LOG_TAG, "OnResume()");
        super.onResume();
        UpdateList();
    }

    public void UpdateList() {
        todoItems.clear();
        todoDbHelper.getAllItems(todoItems);
        todoListView.setAdapter(listAdapter);
    }
}
