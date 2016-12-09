package co.xelabs.todo.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String LOG_TAG = "DatabaseHelper";

    public static String DB_NAME;
    private static String SQL_CREATE_ENTRIES;

    private SQLiteDatabase writeableDatabase;
    private ArrayList<ContentValues> contentCache = new ArrayList<>();
    private boolean contentCacheNeedsUpdate = true;


    public DatabaseHelper(Context context, String databaseName, String entries, int dbVersion) {
        super(context, databaseName, null, dbVersion);
        DB_NAME = databaseName;
        SQL_CREATE_ENTRIES = entries;
        getDatabase();
    }


    /*
        Get the current sql database to write data to
     */
    public SQLiteDatabase getDatabase() {
        if (writeableDatabase == null || !writeableDatabase.isOpen()) {
            writeableDatabase = this.getWritableDatabase();
        }
        return writeableDatabase;
    }

    public void InvalidateCache() {
        contentCacheNeedsUpdate = true;
    }

    /*
        Insert content into a table in the database
     */
    public long insertValues(String tableName, ContentValues values) {
        long newRowId = getDatabase().insert(tableName, null, values);
        contentCacheNeedsUpdate = true;
        return newRowId;
    }

    public boolean deleteObject(String tableName, String columnName, String[] selectionArgs) {
        String selection = columnName + " = ?";
        int rowsDeleted = getDatabase().delete(tableName, selection, selectionArgs);
        Log.d(LOG_TAG, "rowsDeleted: " + rowsDeleted);
        if (rowsDeleted > 0) {
            contentCacheNeedsUpdate = true;
        }
        return  (rowsDeleted > 0);
    }

    public boolean updateObject(String tableName, String columnName, String[] selectionArgs, ContentValues values) {
        String selection = columnName + " = ?";
        int rowsUpdated = getDatabase().update(tableName, values, selection, selectionArgs);
        Log.d(LOG_TAG, "Rowsupdated: " + rowsUpdated);
        if (rowsUpdated > 0) {
            contentCacheNeedsUpdate = true;
        }

        return (rowsUpdated > 0);
    }

    public long getRowCount(String tableName) {
        return DatabaseUtils.queryNumEntries(getDatabase(), tableName);
    }

    public boolean containsObject(String tableName, String columnName, String[] selectionArgs,
                                  String orderBy, String[] projection) {
        String selection = columnName + "= ?";
        Cursor cursor = getDatabase().query(
                tableName,      // The table to query
                projection,     // The columns to return
                selection,      // The columns for the WHERE statement
                selectionArgs,  // The values for the WHERE statement
                null,           // Group by rows
                null,           // Filter by row groups
                orderBy         // The sort order
        );

        int count = cursor.getCount();
        cursor.close();
        return count != 0;
    }

    public int getCount(String tableName, String columnName, String[] selectionArgs, String[] projection) {
        String selection = columnName + "= ?";
        Cursor cursor = getDatabase().query(
                tableName,      // The table to query
                projection,     // The columns to return
                selection,      // The columns for the WHERE statement
                selectionArgs,  // The values for the WHERE statement
                null,           // Group by rows
                null,           // Filter by row groups
                null            // The sort order
        );

        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public ContentValues getContent(String tableName, String columnname, String[] selectionArgs,
                                    String[] projection, String orderBy) {
        String selection = columnname + "= ?";
        Cursor cursor = getDatabase().query(
                tableName,      // The table to query
                projection,     // The columns to return
                selection,      // The columns for the WHERE statement
                selectionArgs,  // The values for the WHERE statement
                null,           // Group by rows
                null,           // Filter by row groups
                orderBy         // The sort order
        );

        // No content found
        if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }

        ContentValues content = getValuesFromCursor(cursor, projection);
        cursor.close();

        if (content == null) {
            return null;
        }

        return content;
    }

    public ArrayList<ContentValues> getAllContent(String tableName, String[] projection)
    {
        return getAllContent(tableName, projection, null);
    }

    /**
     *
     * @param tableName
     * @param projection
     * @param orderBy A string that contains the key and then the order ex. "_id ASC"
     * @return Array list of content values
     */
    public ArrayList<ContentValues> getAllContent(String tableName, String[] projection, String orderBy)
    {
        String queryString = "SELECT * FROM " + tableName;

        if (!contentCacheNeedsUpdate && contentCache.size() > 0)
            return contentCache;

        if(orderBy != null)
        {
            queryString = "SELECT * FROM " + tableName + " ORDER BY " + orderBy;
        }

        Cursor cursor = getDatabase().rawQuery(queryString, null);

        ArrayList<ContentValues> contentList = new ArrayList<>();

        if(!cursor.moveToFirst()) {
            return null;
        }

        while (!cursor.isAfterLast()) {
            contentList.add(this.getValuesFromCursor(cursor, projection));
            cursor.moveToNext();
        }

        cursor.close();
        contentCache = new ArrayList<>(contentList);

        contentCacheNeedsUpdate = false;
        return contentList;
    }

    /**
     * WARNING this is a dangerous operation and should ONLY be ran if you know all the ramifications
     * of the operation that you are doing. ZERO handling will be done for you.
     * @param query
     * @param resetCache
     */
    protected void sendRawQuery(String query, boolean resetCache)
    {
        if(resetCache)
            InvalidateCache();

        getDatabase().execSQL(query);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(SQL_CREATE_ENTRIES);
        } catch( SQLException e) {
            e.printStackTrace();
        }
        Log.d(LOG_TAG, "=========================");
        Log.d(LOG_TAG, "Created new DB");
        Log.d(LOG_TAG, "=========================");
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onDowngrade(db, oldVersion, newVersion);
    }

    public ContentValues getValuesFromCursor(Cursor cursor, String[] projections) {
        ContentValues content = new ContentValues();

        for (String proj : projections) {
            int index;
            try {
                index = cursor.getColumnIndexOrThrow(proj);
            } catch(IllegalArgumentException e) {
                Log.w(LOG_TAG, "Error getting object form cursor");
                continue;
            }

            int type = cursor.getType(index);
            switch (type) {
                case Cursor.FIELD_TYPE_INTEGER:
                    content.put(proj, cursor.getInt(index));
                    break;
                case Cursor.FIELD_TYPE_STRING:
                    content.put(proj, cursor.getString(index));
                    break;
                case Cursor.FIELD_TYPE_FLOAT:
                    content.put(proj, cursor.getFloat(index));
                    break;
                case Cursor.FIELD_TYPE_BLOB:
                    content.put(proj, cursor.getBlob(index));
                    break;
                case Cursor.FIELD_TYPE_NULL:
                    Log.w(LOG_TAG, "Attempted to get unknown type from cursor!");
                    break;
                default:
                    Log.w(LOG_TAG, "Unknown type in getting values from cursor!");
            }
        }
        return content;
    }



}
