package me.kainoseto.todo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import me.kainoseto.todo.Content.TodoContentManager;
import me.kainoseto.todo.Content.TodoItem;
import me.kainoseto.todo.Database.ContentManager;
import me.kainoseto.todo.Preferences.PreferencesManager;
import me.kainoseto.todo.UI.SubtaskListAdapter;

public class ItemDetailActivity extends AppCompatActivity {

    private TextView nameView;
    private TextView descriptionView;
    private Switch doneView;
    private RecyclerView subtaskRecyclerView;

    private String name;
    private String description;
    private boolean done;
    private int uiIdx;

    private ContentManager contentManager;
    private TodoItem currentItem;

    private SubtaskListAdapter subtaskListAdapter;

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    CoordinatorLayout cl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(MainActivity.preferencesManager.getSharedPref().getBoolean(PreferencesManager.KEY_THEME, false)) {
            setTheme(R.style.LightTheme_NoActionBar);
        } else {
            setTheme(R.style.DarkTheme_NoActionBar);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        nameView = (TextView) findViewById(R.id.text_name);
        descriptionView = (TextView) findViewById(R.id.text_desc);
        doneView = (Switch) findViewById(R.id.switch_done_view);

        contentManager = TodoContentManager.getInstance();

        Intent callingIntent        = getIntent();
        Bundle callingIntentData    = callingIntent.getExtras();

        uiIdx           = callingIntentData.getInt(getString(R.string.intent_idx));
        currentItem     = contentManager.getTodoItem(uiIdx);

        name            = currentItem.getName();
        description     = currentItem.getDescription();
        done            = currentItem.isDone();

        nameView.setText(name);
        descriptionView.setText(description);
        doneView.setChecked(done);

        toolbar.setTitle(name);
        setSupportActionBar(toolbar);

        doneView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean updated = contentManager.setDone(uiIdx, isChecked);
                Log.w(LOG_TAG, "Updated db with new done values: " + updated);
            }
        });

        subtaskRecyclerView = (RecyclerView) findViewById(R.id.detail_subtask_recycler);
        subtaskListAdapter = new SubtaskListAdapter(this, uiIdx);
        subtaskRecyclerView.setAdapter(subtaskListAdapter);
        subtaskRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                Intent detailViewIntent = new Intent(getApplicationContext(), EditItemDetailActivity.class);
                detailViewIntent.putExtra(getString(R.string.intent_idx), uiIdx);
                startActivity(detailViewIntent);
                return true;

            case R.id.action_remove:
                contentManager.removeTodoItem(uiIdx);
                Log.d(LOG_TAG, "REMOVED ITEM");
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
}
