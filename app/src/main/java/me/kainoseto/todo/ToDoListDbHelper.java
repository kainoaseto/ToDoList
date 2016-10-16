package me.kainoseto.todo;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

public class TodoListDbHelper extends DatabaseHandler {

    public static class ToDoList implements BaseColumns {
        public static final String TABLE_NAME = "todo_data";
        public static final String COLUMN_NAME_ID = "ID";
        public static final String COLUMN_NAME_NAME = "NAME";
        public static final String COLUMN_NAME_DESC = "DESC";
        public static final String COLUMN_NAME_DONE = "DONE";
        public static final String COLUMN_NMAE_SUBITEMS = "SUBITEMS";
    };

    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "todo_list.db";
    public static final String[] fullProjection = {
            ToDoList.COLUMN_NAME_ID,
            ToDoList.COLUMN_NAME_NAME,
            ToDoList.COLUMN_NAME_DESC,
            ToDoList.COLUMN_NAME_DONE,
            ToDoList.COLUMN_NMAE_SUBITEMS
    };

    private static final String TEXT_TYPE = "TEXT";
    private static final String COMMA_SEP = ",";
    private static final String DELETE_ENTRIES = "DROP TABLE IF EXISTS " + ToDoList.TABLE_NAME;
    private static final String CREATE_ENTRIES = "CREATE TABLE " + ToDoList.TABLE_NAME + "(" +
            ToDoList.COLUMN_NAME_ID + " INTEGER PRIMARY KEY" +
            ToDoList.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
            ToDoList.COLUMN_NAME_DESC + TEXT_TYPE + COMMA_SEP +
            ToDoList.COLUMN_NAME_DONE + " INTEGER DEFAULT 0" + COMMA_SEP +
            ToDoList.COLUMN_NMAE_SUBITEMS + " BLOB )";

    public TodoListDbHelper(Context context) {
        super(context, DB_NAME, CREATE_ENTRIES, DB_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        super.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (DB_VERSION < 1) {
            db.execSQL("ALTER TABLE" + ToDoList.TABLE_NAME +
                            " ADD COLUMN  " + ToDoList.COLUMN_NAME_DONE + " INTEGER DEFAULT 0");
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public long getRowCount() {
        return super.getRowCount(ToDoList.TABLE_NAME);
    }

    public void addToDoItem(int idx, String name, String desc, boolean done) {
        ContentValues item = new ContentValues();
        item.put(ToDoList.COLUMN_NAME_ID, idx);
        item.put(ToDoList.COLUMN_NAME_NAME, name);
        item.put(ToDoList.COLUMN_NAME_DESC, desc);
        item.put(ToDoList.COLUMN_NAME_DONE, done);

        super.insertValues(ToDoList.TABLE_NAME, item);
    }

    public void updateToDoItem(String todoListCol, String idx, ContentValues values) {
        // Which row to udpate based on id
        String[] selectionArgs = {idx};
        super.updateObject(ToDoList.TABLE_NAME, todoListCol, selectionArgs, values);
    }

    public void removeToDoItem(String todoListCol, String idx) {
        // Which row to delete based on id
        String[] selectionArgs = {idx};
        super.deleteObject(ToDoList.TABLE_NAME, todoListCol, selectionArgs);
    }

    public void updateDone(String idx, boolean done) {

    }

}
