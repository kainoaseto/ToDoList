package me.kainoseto.todo.Database;

import android.provider.BaseColumns;

/**
 * Created by Kainoa on 11/20/2016.
 */

public class TODOLIST_DB_COLUMNS implements BaseColumns
{
    public static final String TABLE_NAME = "todo_data";

    public static final String COLUMN_NAME_ID         = "_id";
    public static final String COLUMN_NAME_UI_IDX     = "ui_idx";
    public static final String COLUMN_NAME_NAME       = "name";
    public static final String COLUMN_NAME_DESC       = "desc";
    public static final String COLUMN_NAME_DONE       = "done";
    public static final String COLUMN_NAME_SUBTASKS   = "subtasks";
    public static final String COLUMN_NAME_START_DATE = "start_date";
    public static final String COLUMN_NAME_END_DATE   = "end_date";
};