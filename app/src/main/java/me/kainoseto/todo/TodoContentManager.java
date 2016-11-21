package me.kainoseto.todo;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import me.kainoseto.todo.Database.TodoDatabaseHandler;
import me.kainoseto.todo.UI.TodoItem;

/**
 * Created by Kainoa on 11/20/2016.
 */

public class TodoContentManager
{
    private static final String LOG_TAG = TodoContentManager.class.getSimpleName();

    private static ArrayList<TodoItem>         todoItems;
    private static TodoDatabaseHandler  databaseHandler;

    private static TodoContentManager singleton;

    private TodoContentManager(Context context)
    {
        todoItems = new ArrayList<>();
        if(databaseHandler == null)
            databaseHandler = new TodoDatabaseHandler(context);
    }

    public static void initInstance(Context context) {
        if (singleton != null)
        {
            Log.e(LOG_TAG, "Tried to initialize content manager but it is already initialized!");
            return;
        }

        singleton = new TodoContentManager(context);
    }

    public static TodoContentManager getInstance()
    {
        if(singleton == null) {
            Log.e(LOG_TAG, "Tried to get instance without initializing content manager!");
        }
         return singleton;
    }

    public void refreshContent()
    {
        todoItems.clear();
        databaseHandler.getAllItems(todoItems);
    }

    public TodoDatabaseHandler getDatabaseHandler()
    {
        return databaseHandler;
    }

    public TodoItem getTodoItem(int index)
    {
        return todoItems.get(index);
    }

    public int getSize()
    {
        return todoItems.size();
    }
}
