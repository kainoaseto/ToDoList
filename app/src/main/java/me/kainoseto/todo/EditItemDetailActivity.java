package me.kainoseto.todo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.facebook.stetho.inspector.elements.ShadowDocument;

import me.kainoseto.todo.Preferences.PreferencesManager;


/**
 * Created by Kainoa.
 */

public class EditItemDetailActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private EditText nameEditText;
    private EditText descEditText;
    private Switch  doneSwitch;
    private String name;
    private String description;
    private boolean done;
    private int idx;
    private boolean newItem;

    CoordinatorLayout cl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(MainActivity.preferencesManager.getSharedPref().getBoolean(PreferencesManager.KEY_THEME, false)) {
            setTheme(R.style.LightTheme);
            //cl.setBackgroundColor(ContextCompat.getColor(this, R.color.colorLightBackground));
        } else {
            setTheme(R.style.DarkTheme);
            //cl.setBackgroundColor(ContextCompat.getColor(this, R.color.windowBackground));
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editdetails);

        nameEditText = (EditText) findViewById(R.id.edit_name);
        descEditText = (EditText) findViewById(R.id.edit_desc);
        doneSwitch = (Switch) findViewById(R.id.switch_done);

        Intent intent = getIntent();
        Bundle intentData = intent.getExtras();

        newItem = intentData.getBoolean("NEW");

        if (!newItem) {
            name = intentData.getString("NAME");
            description = intentData.getString("DESC");
            done = intentData.getBoolean("DONE");
            idx = intentData.getInt("POSITION");
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
        /*if (!UpdateDB()) {
            Toast.makeText(this, R.string.item_fail_to_save, Toast.LENGTH_LONG).show();
        }*/
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*if (!UpdateDB()) {
            Toast.makeText(this, R.string.item_fail_to_save, Toast.LENGTH_LONG).show();
        } else {
            Intent detailViewIntent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(detailViewIntent);
        }*/
        Intent detailViewIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(detailViewIntent);
    }

    private boolean UpdateDB() {
        if(!nameEditText.getText().toString().equals("")) {
            if(newItem) {
                MainActivity.todoDbHelper.addToDoItem(nameEditText.getText().toString(), descEditText.getText().toString(), doneSwitch.isChecked());
            } else {
                MainActivity.todoDbHelper.updateName(idx, nameEditText.getText().toString());
                MainActivity.todoDbHelper.updateDesc(idx, descEditText.getText().toString());
                MainActivity.todoDbHelper.updateDone(idx, doneSwitch.isChecked());
            }
        }
        else {
            return false;
        }
        return true;
    }

}
