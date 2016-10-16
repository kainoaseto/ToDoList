package me.kainoseto.todo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    RecyclerView todoListView;
    TodoListAdapter listAdapter;
    ArrayList<TodoItem> todoItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

       FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_additem);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                Intent detailViewIntent = new Intent(getApplicationContext(), ItemDetailActivity.class);
                detailViewIntent.putExtra("POSITION", "NEW");
                startActivity(detailViewIntent);
            }
        });

        todoItems = new ArrayList<>();

        todoListView = (RecyclerView) findViewById(R.id.listRecycler);
        listAdapter = new TodoListAdapter(this, todoItems);
        todoListView.setAdapter(listAdapter);
        todoListView.setLayoutManager(new LinearLayoutManager(this));

    }
}
