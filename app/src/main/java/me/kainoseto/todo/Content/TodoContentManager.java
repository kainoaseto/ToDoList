package me.kainoseto.todo.Content;

import android.content.Context;
import android.util.Log;

import java.math.BigDecimal;
import java.util.ArrayList;

import me.kainoseto.todo.Database.ContentManager;
import me.kainoseto.todo.Database.TodoDatabaseHandler;
import java.lang.Math;
import java.util.List;

/**
 * Created by Kainoa on 11/20/2016.
 */

public class TodoContentManager implements ContentManager
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
        todoItems.clear();
        databaseHandler.getAllItems(todoItems);
    }

    public static TodoContentManager getInstance()
    {
        if(singleton == null) {
            Log.e(LOG_TAG, "Tried to get instance without initializing content manager!");
        }
         return singleton;
    }

    @Override
    public void refreshContent()
    {
        todoItems.clear();
        databaseHandler.getAllItems(todoItems);
    }

    @Override
    public void resetContent()
    {
        databaseHandler.reset();
        todoItems.clear();
        databaseHandler.getAllItems(todoItems);
    }

    @Override
    public boolean addTodoItem(String name, String desc, List<Subtask> subtasks, boolean done)
    {
        int uiIdx = getSize();

        long rowId = databaseHandler.addToDoItem(uiIdx, name, desc, done, subtasks);
        if (rowId < 0)
            return false;

        todoItems.add(uiIdx, new TodoItem(new BigDecimal(rowId).intValueExact(), uiIdx, name, desc, subtasks, done));
        return true;
    }

    @Override
    public void removeTodoItem(int uiIdx)
    {
        databaseHandler.removeToDoItem(uiIdx);
        todoItems.remove(uiIdx);

        TodoItem item;
        for(int i = uiIdx; i < getSize(); i++)
        {
            item = todoItems.get(i);
            databaseHandler.updateUiIdx(item.getUiIdx(), item.getUiIdx()-1);
            item.setUidIdx(item.getUiIdx()-1);
            todoItems.set(item.getUiIdx(), item);
        }
        Log.d(LOG_TAG, "Removed todoitem: " + uiIdx);
    }

    @Override
    public TodoItem getTodoItem(int uiIdx)
    {
        if(uiIdx < todoItems.size()){
            return todoItems.get(uiIdx);
        }
        return null;
    }

    @Override
    public int getSize()
    {
        return todoItems.size();
    }

    @Override
    public boolean setName(int uiIdx, String name)
    {
        TodoItem item = todoItems.get(uiIdx);
        item.setName(name);
        todoItems.set(uiIdx, item);
        return databaseHandler.updateName(uiIdx, name);
    }

    @Override
    public boolean setDesc(int uiIdx, String desc)
    {
        TodoItem item = todoItems.get(uiIdx);
        item.setDescription(desc);
        todoItems.set(uiIdx, item);
        return databaseHandler.updateDesc(uiIdx, desc);
    }

    @Override
    public boolean setDone(int uiIdx, boolean done)
    {
        TodoItem item = todoItems.get(uiIdx);
        item.setDone(done);
        todoItems.set(uiIdx, item);
        return databaseHandler.updateDone(uiIdx, done);
    }

    @Override
    public boolean setUiIdx(int uiIdx, int newUiIdx)
    {
        TodoItem item = todoItems.get(uiIdx);
        item.setUidIdx(uiIdx);
        todoItems.set(uiIdx, item);
        return databaseHandler.updateUiIdx(uiIdx, newUiIdx);
    }

    @Override
    public boolean setSubtasks(int uiIdx, List<Subtask> subtasks){
        TodoItem item = todoItems.get(uiIdx);
        item.setSubtasks(subtasks);
        todoItems.set(uiIdx, item);
        return databaseHandler.updateSubtasks(uiIdx, subtasks);
    }

    @Override
    public boolean setSubtaskForTodoItem(int uiIdx, int subtaskIdx, Subtask subtask){
        List<Subtask> subtasks = todoItems.get(uiIdx).getSubtasks();
        subtasks.remove(subtaskIdx);
        subtasks.add(subtaskIdx, subtask);
        return databaseHandler.updateSubtasks(uiIdx, subtasks);
    }

    @Override
    public void swapUiIdx(int firstIdx, int secondIdx)
    {
        setUiIdx(firstIdx, 999999+firstIdx);
        setUiIdx(secondIdx, firstIdx);
        setUiIdx(firstIdx+999999, secondIdx);
    }
}
