package me.kainoseto.todo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import me.kainoseto.todo.Callback.SimpleItemTouchHelperCallback;
import me.kainoseto.todo.Content.Subtask;
import me.kainoseto.todo.Content.TodoContentManager;
import me.kainoseto.todo.Content.TodoItem;
import me.kainoseto.todo.Database.ContentManager;
import me.kainoseto.todo.Preferences.PreferencesManager;
import me.kainoseto.todo.UI.SubtaskListTmpAdapter;


/**
 * Created by Kainoa.
 */

public class EditItemDetailActivity extends AppCompatActivity
{
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private EditText nameEditText;
    private EditText descEditText;
    private EditText subtaskEditText;
    private Switch  doneSwitch;
    private Switch  subtaskDoneSwitch;
    private Button  addSubtaskBtn;
    private RecyclerView subtasksRecycler;
    private SubtaskListTmpAdapter subtaskListAdapter;

    // Current values of item
    private String name;
    private String description;
    private boolean done;
    private int uiIdx;
    private TodoItem currentItem;

    private boolean isNewItem;
    private ContentManager contentManager;

    CoordinatorLayout cl;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // More theme garbage to clean up....
        if(MainActivity.preferencesManager.getSharedPref().getBoolean(PreferencesManager.KEY_THEME, false)) {
            setTheme(R.style.LightTheme_NoActionBar);
        } else {
            setTheme(R.style.DarkTheme_NoActionBar);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editdetails);

        Toolbar toolbar = (Toolbar) findViewById(R.id.edit_toolbar);
        setSupportActionBar(toolbar);

        nameEditText      = (EditText) findViewById(R.id.edit_name);
        descEditText      = (EditText) findViewById(R.id.edit_desc);
        subtaskEditText   = (EditText) findViewById(R.id.edit_subtask_name);
        doneSwitch        = (Switch) findViewById(R.id.switch_done);
        subtaskDoneSwitch = (Switch) findViewById(R.id.switch_subtask_done);
        addSubtaskBtn     = (Button) findViewById(R.id.btn_add_subtask);

        // Intent that called this activity
        Intent callingIntent    = getIntent();
        Bundle intentData       = callingIntent.getExtras();

        contentManager = TodoContentManager.getInstance();

        isNewItem = intentData.getBoolean(getString(R.string.intent_create_item));

        if (!isNewItem)
        {
            uiIdx           = intentData.getInt(getString(R.string.intent_idx));
            currentItem     = contentManager.getTodoItem(uiIdx);
            name            = currentItem.getName();
            description     = currentItem.getDescription();
            done            = currentItem.isDone();
            nameEditText.setText(name);
            descEditText.setText(description);
            doneSwitch.setChecked(done);
            getSupportActionBar().setTitle(name);
        } else {
            getSupportActionBar().setTitle("New Item");
        }

        ImageButton settingsBtn = (ImageButton) findViewById(R.id.save_toolbar_button);
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!UpdateDB()) {
                    Toast.makeText(getApplicationContext(), R.string.item_fail_to_save, Toast.LENGTH_LONG).show();
                } else {
                    Intent detailViewIntent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(detailViewIntent);
                }
            }
        });

        //Setting up recycler view
        subtasksRecycler = (RecyclerView) findViewById(R.id.recycler_edit_subtasks);
        TodoItem item = contentManager.getTodoItem(uiIdx);
        List<Subtask> subtasks = (item != null) ? item.getSubtasks() : new ArrayList();
        subtaskListAdapter = new SubtaskListTmpAdapter(this, uiIdx, subtasks);
        subtasksRecycler.setLayoutManager(new LinearLayoutManager(this));
        subtasksRecycler.setAdapter(subtaskListAdapter);


        //Setting up swipe to remove
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(subtaskListAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(subtasksRecycler);

        UpdateList();

        addSubtaskBtn.setOnClickListener((View v) -> {
            Subtask subtask = new Subtask(subtaskDoneSwitch.isChecked(), subtaskEditText.getText().toString());
            List<Subtask> tmpSubtasks = subtaskListAdapter.getTmpSubtasks();
            tmpSubtasks.add(subtask);
            subtaskListAdapter.setTmpSubtasks(tmpSubtasks);
            UpdateList();
            Log.d(LOG_TAG, "Added Subtask");
            //clear stuff out
            subtaskEditText.setText("");
            subtaskDoneSwitch.setChecked(false);
        });

    }

    @Override
    public void onResume(){
        super.onResume();

        if(getIntent().getExtras().getBoolean(getString(R.string.intent_create_item))){
            subtaskListAdapter.setTmpSubtasks(new ArrayList<>());
            subtaskListAdapter.setUiIdx(uiIdx);
        }

        UpdateList();
    }

    @Override
    public void finish() {
        super.finish();

        /* The back button was pressed, maybe check if there were changes in any of the fields
            and if there are changes present then ask them here if they would like to save
        */

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        // We need to be returning to our last activity which might not be detailViewIntent...
        // Maybe convert this to a fragment to handle lifecycle better
        Intent detailViewIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(detailViewIntent);
    }

    private boolean UpdateDB() {
        if(!nameEditText.getText().toString().equals(""))
        {
            if(isNewItem)
            {
                contentManager.addTodoItem(nameEditText.getText().toString(), descEditText.getText().toString(), subtaskListAdapter.getTmpSubtasks(), doneSwitch.isChecked());
            }
            else
            {
                contentManager.setName(uiIdx, nameEditText.getText().toString());
                contentManager.setDesc(uiIdx, descEditText.getText().toString());
                contentManager.setSubtasks(uiIdx, subtaskListAdapter.getTmpSubtasks());
                contentManager.setDone(uiIdx, doneSwitch.isChecked());
            }
        }
        else {
            return false;
        }
        return true;
    }

    private void UpdateList(){ subtasksRecycler.setAdapter(subtaskListAdapter);}
}
