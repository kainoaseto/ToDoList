package me.kainoseto.todo.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;

import me.kainoseto.todo.MainActivity;
import me.kainoseto.todo.TodoItem;

public class TodoListDbHelper extends DatabaseHandler {
    private static final String LOG_TAG = "TodoListDbHelper";
    public static class TodoList implements BaseColumns {
        public static final String TABLE_NAME = "todo_data";
        public static final String COLUMN_NAME_ID = "_id";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_DESC = "desc";
        public static final String COLUMN_NAME_DONE = "done";
        public static final String COLUMN_NMAE_SUBITEMS = "subitems";
    };

    public static final int DB_VERSION = 9;
    public static final String DB_NAME = "todo_list.db";
    public static final String[] fullProjection = {
            TodoList.COLUMN_NAME_ID,
            TodoList.COLUMN_NAME_NAME,
            TodoList.COLUMN_NAME_DESC,
            TodoList.COLUMN_NAME_DONE
    };

    private static final String TEXT_TYPE = "TEXT";
    private static final String COMMA_SEP = ",";
    private static final String DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TodoList.TABLE_NAME;
    private static final String CREATE_ENTRIES = "CREATE TABLE " + TodoList.TABLE_NAME + "(" +
            TodoList.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT " + COMMA_SEP +
            TodoList.COLUMN_NAME_NAME + " " + TEXT_TYPE + COMMA_SEP +
            TodoList.COLUMN_NAME_DESC + " " + TEXT_TYPE + COMMA_SEP +
            TodoList.COLUMN_NAME_DONE + " INTEGER DEFAULT 0)";/* + COMMA_SEP +
            TodoList.COLUMN_NMAE_SUBITEMS + " BLOB )";*/

    public TodoListDbHelper(Context context) {
        super(context, DB_NAME, CREATE_ENTRIES, DB_VERSION);
        Log.w(LOG_TAG, "TodoListDbHelper created");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        super.onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /*if (DB_VERSION < 1) {
            db.execSQL("ALTER TABLE" + TodoList.TABLE_NAME +
                            " ADD COLUMN  " + TodoList.COLUMN_NAME_DONE + " INTEGER DEFAULT 0");
        }*/

        db.execSQL(DELETE_ENTRIES);
        onCreate(db);
        Log.d(LOG_TAG, "Deleted table");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onDowngrade(db, oldVersion, newVersion);
    }

    public long getRowCount() {
        return super.getRowCount(TodoList.TABLE_NAME);
    }

    public void addToDoItem(String name, String desc, boolean done) {
        ContentValues item = new ContentValues();
        item.put(TodoList.COLUMN_NAME_NAME, name);
        item.put(TodoList.COLUMN_NAME_DESC, desc);
        item.put(TodoList.COLUMN_NAME_DONE, done);

        super.insertValues(TodoList.TABLE_NAME, item);
    }

    protected boolean updateToDoItem(int idx, ContentValues values) {
        // Which row to update based on id
        String[] selectionArgs = {String.valueOf(idx)};
        Log.d(LOG_TAG, "ID: "+String.valueOf(idx));
        return super.updateObject(TodoList.TABLE_NAME, "_id", selectionArgs, values);
    }

    public void removeToDoItem(int idx) {
        // Which row to delete based on id
        String[] selectionArgs = {Integer.toString(idx)};
        Log.d(LOG_TAG, "ID: "+String.valueOf(idx));
        super.deleteObject(TodoList.TABLE_NAME, "_id", selectionArgs);
    }

    public boolean updateName(int idx, String name) {
        ContentValues cv = new ContentValues();
        cv.put(TodoList.COLUMN_NAME_NAME, name);
        return updateToDoItem(idx, cv);
    }

    public boolean updateDesc(int idx, String desc) {
        ContentValues cv = new ContentValues();
        cv.put(TodoList.COLUMN_NAME_DESC, desc);
        return updateToDoItem(idx, cv);
    }

    public boolean updateDone(int idx, boolean done) {
        ContentValues cv = new ContentValues();
        cv.put(TodoList.COLUMN_NAME_DONE, done ? 1 : 0);
        return updateToDoItem(idx, cv);
    }

    public void getAllItems(ArrayList<TodoItem> items) {
        ArrayList<ContentValues> data = super.getAllContent(TodoList.TABLE_NAME, fullProjection);
        if(data == null || data.size() < 1) {
            return;
        }
        for (ContentValues item : data) {
            TodoItem newItem = new TodoItem(item.getAsInteger("_id"), item.getAsString("name"), item.getAsString("desc"), item.getAsBoolean("done"));
            items.add(newItem);
        }
    }
}
