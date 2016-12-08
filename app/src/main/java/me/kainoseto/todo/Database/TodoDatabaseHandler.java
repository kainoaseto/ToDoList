package me.kainoseto.todo.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.api.client.util.DateTime;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import me.kainoseto.todo.Content.Subtask;
import me.kainoseto.todo.Content.TodoItem;
import me.kainoseto.todo.Util.DateTimeUtil;
import me.kainoseto.todo.Util.StringUtil;

import static me.kainoseto.todo.Database.TODOLIST_DB_COLUMNS.COLUMN_NAME_CAL_ID;
import static me.kainoseto.todo.Database.TODOLIST_DB_COLUMNS.COLUMN_NAME_DESC;
import static me.kainoseto.todo.Database.TODOLIST_DB_COLUMNS.COLUMN_NAME_DONE;
import static me.kainoseto.todo.Database.TODOLIST_DB_COLUMNS.COLUMN_NAME_END_DATE;
import static me.kainoseto.todo.Database.TODOLIST_DB_COLUMNS.COLUMN_NAME_ID;
import static me.kainoseto.todo.Database.TODOLIST_DB_COLUMNS.COLUMN_NAME_NAME;
import static me.kainoseto.todo.Database.TODOLIST_DB_COLUMNS.COLUMN_NAME_START_DATE;
import static me.kainoseto.todo.Database.TODOLIST_DB_COLUMNS.COLUMN_NAME_SUBTASKS;
import static me.kainoseto.todo.Database.TODOLIST_DB_COLUMNS.COLUMN_NAME_UI_IDX;
import static me.kainoseto.todo.Database.TODOLIST_DB_COLUMNS.TABLE_NAME;

public class TodoDatabaseHandler extends DatabaseHelper
{
    private static final String LOG_TAG = TodoDatabaseHandler.class.getSimpleName();

    private static final int DB_VERSION = 2;
    private static final String DB_NAME = "todo_list.db";
    private Gson gson;

    //Storing type so gson wont get confused by type erasure
    private Type subtaskType = new TypeToken<List<Subtask>>(){}.getType();

    private static final String[] FULL_PROJECTION = {
            COLUMN_NAME_ID,
            COLUMN_NAME_UI_IDX,
            COLUMN_NAME_CAL_ID,
            COLUMN_NAME_NAME,
            COLUMN_NAME_DESC,
            COLUMN_NAME_SUBTASKS,
            COLUMN_NAME_START_DATE,
            COLUMN_NAME_END_DATE,
            COLUMN_NAME_DONE
    };

    private static final String DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;
    private static final String CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + "(" +
            COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_NAME_UI_IDX + " INTEGER UNIQUE," +
            COLUMN_NAME_CAL_ID + " TEXT, " +
            COLUMN_NAME_NAME + " TEXT, " +
            COLUMN_NAME_DESC + " TEXT, " +
            COLUMN_NAME_SUBTASKS + " TEXT, " +
            COLUMN_NAME_START_DATE + " TEXT, " +
            COLUMN_NAME_END_DATE + " TEXT, " +
            COLUMN_NAME_DONE + " INTEGER DEFAULT 0)";

    public TodoDatabaseHandler(Context context)
    {
        super(context, DB_NAME, CREATE_ENTRIES, DB_VERSION);
        this.gson = new Gson();
    }

    @Override
    public void onCreate(SQLiteDatabase db) 
    {
        super.onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL(DELETE_ENTRIES);
        onCreate(db);
        Log.d(LOG_TAG, "Deleted table");
    }

    public void reset()
    {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(DELETE_ENTRIES);
        onCreate(db);
        InvalidateCache();
        Log.d(LOG_TAG, "Reset DB");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
    {
        onDowngrade(db, oldVersion, newVersion);
    }

    public long addToDoItem(int uiIdx, String calId, String name, String desc, boolean done, List<Subtask> subtasks, DateTime startDate, DateTime endDate)
    {
        String subtasksJson = gson.toJson(subtasks, subtaskType);

        ContentValues item = new ContentValues();
        item.put(COLUMN_NAME_UI_IDX, uiIdx);
        item.put(COLUMN_NAME_CAL_ID, StringUtil.catchNullString(calId));
        item.put(COLUMN_NAME_NAME, StringUtil.catchNullString(name));
        item.put(COLUMN_NAME_DESC, StringUtil.catchNullString(desc));
        item.put(COLUMN_NAME_START_DATE, DateTimeUtil.safeToStringRfc3339(startDate));
        item.put(COLUMN_NAME_END_DATE, DateTimeUtil.safeToStringRfc3339(endDate));
        item.put(COLUMN_NAME_SUBTASKS, subtasksJson);
        item.put(COLUMN_NAME_DONE, done);

        return super.insertValues(TABLE_NAME, item);
    }

    public void removeToDoItem(int uiIdx)
    {
        // Which row to delete based on id
        String[] selectionArgs = {Integer.toString(uiIdx)};
        Log.d(LOG_TAG, "ID: "+String.valueOf(uiIdx));
        super.deleteObject(TABLE_NAME, COLUMN_NAME_UI_IDX, selectionArgs);
    }

    public boolean updateName(int uiIdx, String name)
    {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME_NAME, name);
        return updateToDoItem(uiIdx, cv);
    }

    public boolean updateDesc(int uiIdx, String desc)
    {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME_DESC, desc);
        return updateToDoItem(uiIdx, cv);
    }

    public boolean updateDone(int uiIdx, boolean done)
    {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME_DONE, done ? 1 : 0);
        return updateToDoItem(uiIdx, cv);
    }

    public boolean updateUiIdx(int uiIdx, int newUiIdx)
    {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME_UI_IDX, newUiIdx);
        return updateToDoItem(uiIdx, cv);
    }

    public boolean updateSubtasks(int uiIdx, List<Subtask> subtasks){
        String subtasksJson = gson.toJson(subtasks, subtaskType);
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME_SUBTASKS, subtasksJson);
        return updateToDoItem(uiIdx, cv);
    }

    public void getAllItems(ArrayList<TodoItem> items)
    {
        ArrayList<ContentValues> data = super.getAllContent(TABLE_NAME, FULL_PROJECTION, COLUMN_NAME_UI_IDX+" ASC");
        if(data == null || data.size() < 1)
        {
            return;
        }

        for (ContentValues item : data)
        {
            TodoItem newItem = new TodoItem(
                    item.getAsInteger(COLUMN_NAME_ID),
                    item.getAsInteger(COLUMN_NAME_UI_IDX),
                    item.getAsString(COLUMN_NAME_CAL_ID),
                    item.getAsString(COLUMN_NAME_NAME),
                    item.getAsString(COLUMN_NAME_DESC),
                    gson.fromJson(item.getAsString(COLUMN_NAME_SUBTASKS), subtaskType),
                    DateTimeUtil.safeParseRfc3339(item.getAsString(COLUMN_NAME_START_DATE)),
                    DateTimeUtil.safeParseRfc3339(item.getAsString(COLUMN_NAME_END_DATE)),
                    item.getAsBoolean(COLUMN_NAME_DONE)
            );
            items.add(newItem);
        }
    }

    public long getRowCount() {
        return super.getRowCount(TABLE_NAME);
    }

    private boolean updateToDoItem(int uiIdx, ContentValues values)
    {
        // Which row to update based on id
        String[] selectionArgs = {String.valueOf(uiIdx)};
        Log.d(LOG_TAG, "ID: "+String.valueOf(uiIdx));
        return super.updateObject(TABLE_NAME, COLUMN_NAME_UI_IDX, selectionArgs, values);
    }
}
