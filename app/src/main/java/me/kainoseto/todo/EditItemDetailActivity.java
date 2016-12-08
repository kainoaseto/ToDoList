package me.kainoseto.todo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import com.google.api.client.util.DateTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import me.kainoseto.todo.Callback.SimpleItemTouchHelperCallback;
import me.kainoseto.todo.Content.Subtask;
import me.kainoseto.todo.Content.TodoContentManager;
import me.kainoseto.todo.Content.TodoItem;
import me.kainoseto.todo.Database.ContentManager;
import me.kainoseto.todo.DateTime.DateTimeListener;
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
    private CheckBox enableDateTimeCheckBox;
    private TableLayout datetimeTableLayout;
    private TextView startDateText;
    private TextView startTimeText;
    private TextView endDateText;
    private TextView endTimeText;
    private CheckBox enableGoogleCalSyncCheckBox;

    private View.OnClickListener datetimeListener;
    private DateTimeListener startListener;
    private DateTimeListener endListener;

    // Current values of item
    private String name;
    private String description;
    private boolean done;
    private int uiIdx;
    private TodoItem currentItem;

    private boolean isNewItem;
    private ContentManager contentManager;

    private AppCompatActivity activity;

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

        nameEditText            = (EditText) findViewById(R.id.edit_name);
        descEditText            = (EditText) findViewById(R.id.edit_desc);
        subtaskEditText         = (EditText) findViewById(R.id.edit_subtask_name);
        doneSwitch              = (Switch) findViewById(R.id.switch_done);
        subtaskDoneSwitch       = (Switch) findViewById(R.id.switch_subtask_done);
        addSubtaskBtn           = (Button) findViewById(R.id.btn_add_subtask);

        enableDateTimeCheckBox  = (CheckBox) findViewById(R.id.editview_setdatetimes);
        datetimeTableLayout     = (TableLayout) findViewById(R.id.editview_datetimes);
        startDateText           = (TextView) findViewById(R.id.editview_start_date);
        startTimeText           = (TextView) findViewById(R.id.editview_start_time);
        endDateText             = (TextView) findViewById(R.id.editview_end_date);
        endTimeText             = (TextView) findViewById(R.id.editview_end_time);

        enableGoogleCalSyncCheckBox = (CheckBox) findViewById(R.id.editview_enablegcalsync);

        if(MainActivity.preferencesManager.getSharedPref().getBoolean(PreferencesManager.KEY_GCAL_ENABLE, false)) {
            enableGoogleCalSyncCheckBox.setVisibility(View.VISIBLE);
        } else {
            enableGoogleCalSyncCheckBox.setVisibility(View.GONE);
        }

        enableGoogleCalSyncCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    enableDateTimeCheckBox.setChecked(true);
                    enableDateTimeCheckBox.setEnabled(false);
                } else {
                    enableDateTimeCheckBox.setEnabled(true);
                }
            }
        });


        // Intent that called this activity
        Intent callingIntent    = getIntent();
        Bundle intentData       = callingIntent.getExtras();

        contentManager = TodoContentManager.getInstance();

        isNewItem = intentData.getBoolean(getString(R.string.intent_create_item));

        activity = this;

        startListener = new DateTimeListener(startDateText, startTimeText);
        endListener = new DateTimeListener(endDateText, endTimeText);

        datetimeListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                switch (v.getId())
                {
                    case R.id.editview_start_date:
                        DatePickerDialog dpd = DatePickerDialog.newInstance(
                                startListener,
                                now.get(Calendar.YEAR),
                                now.get(Calendar.MONTH),
                                now.get(Calendar.DAY_OF_MONTH)
                        );
                        dpd.setThemeDark(true);
                        dpd.setTitle("Starting date");
                        dpd.show(getFragmentManager(), "Starting date");
                        break;
                    case R.id.editview_start_time:
                        TimePickerDialog tpd = TimePickerDialog.newInstance(
                                startListener,
                                now.get(Calendar.HOUR_OF_DAY),
                                now.get(Calendar.MINUTE),
                                false
                        );
                        tpd.setThemeDark(true);
                        tpd.setTitle("Starting Time");
                        tpd.show(getFragmentManager(), "Starting Time");
                        break;
                    case R.id.editview_end_date:
                        dpd = DatePickerDialog.newInstance(
                                endListener,
                                now.get(Calendar.YEAR),
                                now.get(Calendar.MONTH),
                                now.get(Calendar.DAY_OF_MONTH)
                        );
                        dpd.setThemeDark(true);
                        dpd.setTitle("Ending date");
                        dpd.show(getFragmentManager(), "Ending date");
                        break;
                    case R.id.editview_end_time:
                        tpd = TimePickerDialog.newInstance(
                                endListener,
                                now.get(Calendar.HOUR_OF_DAY),
                                now.get(Calendar.MINUTE),
                                false
                        );
                        tpd.setThemeDark(true);
                        tpd.setTitle("Ending Time");
                        tpd.show(getFragmentManager(), "Ending Time");
                        break;
                }
            }
        };

        enableDateTimeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    Calendar now = Calendar.getInstance();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("E MMM dd, yyyy", Locale.US);
                    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
                    String currentDate = dateFormat.format(now.getTime());
                    String currentTime = timeFormat.format(now.getTime());

                    datetimeTableLayout.setVisibility(View.VISIBLE);

                    startDateText.setText(currentDate);
                    startTimeText.setText(currentTime);
                    endDateText.setText(currentDate);
                    endTimeText.setText(currentTime);

                    startDateText.setOnClickListener(datetimeListener);
                    startTimeText.setOnClickListener(datetimeListener);
                    endDateText.setOnClickListener(datetimeListener);
                    endTimeText.setOnClickListener(datetimeListener);

                } else {
                    datetimeTableLayout.setVisibility(View.GONE);
                    startDateText.setText("NULL");
                    startTimeText.setText("NULL");
                    endDateText.setText("NULL");
                    endTimeText.setText("NULL");
                }
            }
        });

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

        ImageButton saveBtn = (ImageButton) findViewById(R.id.save_toolbar_button);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!UpdateDB()) {
                    Toast.makeText(getApplicationContext(), R.string.item_fail_to_save, Toast.LENGTH_LONG).show();
                } else {
                    Intent returnIntent;
                    if(isNewItem)
                        returnIntent = new Intent(getApplicationContext(), MainActivity.class);
                    else
                    {
                        returnIntent = new Intent(getApplicationContext(), ItemDetailActivity.class);
                        returnIntent.putExtra(getString(R.string.intent_idx), uiIdx);
                    }

                    returnIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    NavUtils.navigateUpTo(activity, returnIntent);
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
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
        Intent returnIntent;
        if(isNewItem)
            returnIntent = new Intent(getApplicationContext(), MainActivity.class);
        else
        {
            returnIntent = new Intent(getApplicationContext(), ItemDetailActivity.class);
            returnIntent.putExtra(getString(R.string.intent_idx), uiIdx);
        }

        returnIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        NavUtils.navigateUpTo(activity, returnIntent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        // We need to be returning to our last activity which might not be detailViewIntent...
        // Maybe convert this to a fragment to handle lifecycle better
        //Intent detailViewIntent = new Intent(getApplicationContext(), MainActivity.class);
        //startActivity(detailViewIntent);
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
