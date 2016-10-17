package me.kainoseto.todo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

public class ItemDetailActivity extends AppCompatActivity {

    private TextView nameView;
    private TextView descriptionView;
    private Switch doneView;

    private String name;
    private String description;
    private boolean done;
    private int idx;

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        nameView = (TextView) findViewById(R.id.text_name);
        descriptionView = (TextView) findViewById(R.id.text_desc);
        doneView = (Switch) findViewById(R.id.switch_done_view);

        Intent intent = getIntent();
        Bundle intentData = intent.getExtras();

        name = intentData.getString("NAME");
        description = intentData.getString("DESC");
        done = intentData.getBoolean("DONE");
        idx = intentData.getInt("POSITION");
        nameView.setText(name);
        descriptionView.setText(description);
        doneView.setChecked(done);

        toolbar.setTitle(name);
        setSupportActionBar(toolbar);

        doneView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean updated = MainActivity.todoDbHelper.updateDone(idx, isChecked);
                Log.w(LOG_TAG, "Updated db with new done values: " + updated);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_edit) {
            Intent detailViewIntent = new Intent(getApplicationContext(), EditItemDetailActivity.class);
            detailViewIntent.putExtra("NEW", false);
            detailViewIntent.putExtra("NAME", name);
            detailViewIntent.putExtra("DESC", description);
            detailViewIntent.putExtra("DONE", done);
            detailViewIntent.putExtra("POSITION", idx);
            startActivity(detailViewIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
