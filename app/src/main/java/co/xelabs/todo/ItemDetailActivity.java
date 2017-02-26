package co.xelabs.todo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.api.client.util.DateTime;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import co.xelabs.todo.Content.TodoContentManager;
import co.xelabs.todo.Content.TodoItem;
import co.xelabs.todo.Database.ContentManager;
import co.xelabs.todo.UI.SubtaskListAdapter;

public class ItemDetailActivity extends AppCompatActivity {

    private TextView nameView;
    private TextView descriptionView;
    private Switch doneView;
    private RecyclerView subtaskRecyclerView;
    private TextView startDateText;
    private TextView startTimeText;
    private TextView endDateText;
    private TextView endTimeText;
    private TableLayout dateTimesLayout;

    private String name;
    private String description;
    private boolean done;
    private int uiIdx;
    private DateTime startDateTime;
    private DateTime endDateTime;

    private ContentManager contentManager;
    private TodoItem currentItem;

    private SubtaskListAdapter subtaskListAdapter;

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private Toolbar toolbar;

    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

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

        startDateText           = (TextView) findViewById(R.id.itemview_start_date);
        startTimeText           = (TextView) findViewById(R.id.itemview_start_time);
        endDateText             = (TextView) findViewById(R.id.itemview_end_date);
        endTimeText             = (TextView) findViewById(R.id.itemview_end_time);
        dateTimesLayout         = (TableLayout) findViewById(R.id.itemview_datetimes);


        startDateTime   = currentItem.getStartDate();
        endDateTime     = currentItem.getEndDate();

        dateFormat = new SimpleDateFormat("E MMM dd, yyyy", Locale.US);
        timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);

        if(startDateTime != null && endDateTime != null)
        {
            dateTimesLayout.setVisibility(View.VISIBLE);

            Date startDate  = new Date(startDateTime.getValue());
            Date endDate    = new Date(endDateTime.getValue());

            startDateText.setText(dateFormat.format(startDate));
            startTimeText.setText(timeFormat.format(startDate));

            endDateText.setText(dateFormat.format(endDate));
            endTimeText.setText(timeFormat.format(endDate));
        }
        else
        {
            dateTimesLayout.setVisibility(View.GONE);
        }

        nameView.setText(name);
        descriptionView.setText(description);
        doneView.setChecked(done);

        toolbar.setTitle(name);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

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
            case android.R.id.home:
                Intent returnIntent = new Intent(getApplicationContext(), MainActivity.class);
                returnIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                NavUtils.navigateUpTo(this, returnIntent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                return true;
            case R.id.action_edit:
                Intent detailViewIntent = new Intent(getApplicationContext(), EditItemDetailActivity.class);
                detailViewIntent.putExtra(getString(R.string.intent_idx), uiIdx);
                startActivity(detailViewIntent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;

            case R.id.action_remove:
                contentManager.removeTodoItem(uiIdx);
                Log.d(LOG_TAG, "REMOVED ITEM");
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void finish()
    {
        super.finish();

        Intent returnIntent = new Intent(getApplicationContext(), MainActivity.class);
        returnIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        NavUtils.navigateUpTo(this, returnIntent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        Intent callingIntent        = getIntent();
        Bundle callingIntentData    = callingIntent.getExtras();

        uiIdx           = callingIntentData.getInt(getString(R.string.intent_idx));
        currentItem     = contentManager.getTodoItem(uiIdx);

        name            = currentItem.getName();
        description     = currentItem.getDescription();
        done            = currentItem.isDone();

        startDateTime   = currentItem.getStartDate();
        endDateTime     = currentItem.getEndDate();

        if(startDateTime != null && endDateTime != null)
        {
            dateTimesLayout.setVisibility(View.VISIBLE);

            Date startDate  = new Date(startDateTime.getValue());
            Date endDate    =  new Date(endDateTime.getValue());

            startDateText.setText(dateFormat.format(startDate));
            startTimeText.setText(timeFormat.format(startDate));

            endDateText.setText(dateFormat.format(endDate));
            endTimeText.setText(timeFormat.format(endDate));
        }
        else
        {
            dateTimesLayout.setVisibility(View.GONE);
        }

        nameView.setText(name);
        descriptionView.setText(description);
        doneView.setChecked(done);

        toolbar.setTitle(name);
        setSupportActionBar(toolbar);

        subtaskListAdapter = new SubtaskListAdapter(this, uiIdx);
        subtaskRecyclerView.setAdapter(subtaskListAdapter);

    }

    private String generateDateString(Calendar cal){
        StringBuilder startDateStr = new StringBuilder();
        startDateStr.append(cal.get(Calendar.MONTH)).append("/").append(cal.get(Calendar.DAY_OF_MONTH)).append("/").append(Calendar.YEAR);
        return startDateStr.toString();
    }

    private String generateTimeString(Calendar cal){
        StringBuilder startTimeStr = new StringBuilder();
        startTimeStr.append(cal.get(Calendar.HOUR_OF_DAY)).append(":").append(cal.get(Calendar.MINUTE));
        return startTimeStr.toString();
    }
}
