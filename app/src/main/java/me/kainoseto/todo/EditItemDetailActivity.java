package me.kainoseto.todo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import me.kainoseto.todo.Content.TodoContentManager;
import me.kainoseto.todo.Database.TodoDatabaseHandler;
import me.kainoseto.todo.Preferences.PreferencesManager;
import me.kainoseto.todo.Content.TodoItem;


/**
 * Created by Kainoa.
 */

public class EditItemDetailActivity extends AppCompatActivity
{
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private EditText nameEditText;
    private EditText descEditText;
    private Switch  doneSwitch;

    // Current values of item
    private String name;
    private String description;
    private boolean done;
    private int uiIdx;
    private TodoItem currentItem;

    private boolean isNewItem;
    private TodoContentManager contentManager;
    private TodoDatabaseHandler databaseHandler;

    CoordinatorLayout cl;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // More theme garbage to clean up....
        if(MainActivity.preferencesManager.getSharedPref().getBoolean(PreferencesManager.KEY_THEME, false)) {
            setTheme(R.style.LightTheme);
        } else {
            setTheme(R.style.DarkTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editdetails);

        nameEditText    = (EditText) findViewById(R.id.edit_name);
        descEditText    = (EditText) findViewById(R.id.edit_desc);
        doneSwitch      = (Switch) findViewById(R.id.switch_done);

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




        // TODO: Replace with save button in action bar
        FloatingActionButton fab_save = (FloatingActionButton) findViewById(R.id.fab_saveitem);
        fab_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!UpdateDB()) {
                    Toast.makeText(getApplicationContext(), R.string.item_fail_to_save, Toast.LENGTH_LONG).show();
                } else {
                    Intent detailViewIntent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(detailViewIntent);
                }
            }
        });
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
                contentManager.addTodoItem(nameEditText.getText().toString(), descEditText.getText().toString(), doneSwitch.isChecked());
            }
            else
            {
                contentManager.setName(uiIdx, nameEditText.getText().toString());
                contentManager.setDesc(uiIdx, descEditText.getText().toString());
                contentManager.setDone(uiIdx, doneSwitch.isChecked());
            }
        }
        else {
            return false;
        }
        return true;
    }

}
