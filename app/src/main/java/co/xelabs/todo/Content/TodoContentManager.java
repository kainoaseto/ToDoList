package co.xelabs.todo.Content;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.google.api.client.util.DateTime;

import java.math.BigDecimal;
import java.util.ArrayList;

import co.xelabs.todo.Calendar.CalendarAware;
import co.xelabs.todo.Calendar.CalendarEvent;
import co.xelabs.todo.Calendar.GoogleCalendarManager;
import co.xelabs.todo.Database.ContentManager;
import co.xelabs.todo.Database.TodoDatabaseHandler;

import java.util.List;

/**
 * Created by Kainoa on 11/20/2016.
 */

//TODO: Find a better way to store calenar event id
public class TodoContentManager implements ContentManager
{
    private static final String LOG_TAG = TodoContentManager.class.getSimpleName();

    private static ArrayList<TodoItem>         todoItems;
    private static TodoDatabaseHandler  databaseHandler;

    //BEGIN - Fields used when calendar enabled
    private static GoogleCalendarManager calendarManager;
    private static CalendarAware calendarAware;
    private static Activity activity;
    //END - Fields used when calendar enabled

    private static TodoContentManager singleton;

    private TodoContentManager(Context context)
    {
        todoItems = new ArrayList<>();
        if(databaseHandler == null)
            databaseHandler = new TodoDatabaseHandler(context);
    }

    /**
     * Initializes instance of TodoContentManager. If using GoogleCalendar, pass this an activity
     *
     * @param context
     */
    public static void initInstance(Context context, CalendarAware calAware) {
        if (singleton != null)
        {
            Log.e(LOG_TAG, "Tried to initialize content manager but it is already initialized!");
            return;
        }

        singleton = new TodoContentManager(context);
        todoItems.clear();
        databaseHandler.getAllItems(todoItems);

        if(GoogleCalendarManager.isCalendarEnabled()){
            activity = (Activity) context;
            calendarAware = calAware;
            calendarManager = GoogleCalendarManager.getInstance(activity);
        }
    }

    public void UpdateActivity(Context context)
    {
        activity = (Activity) context;
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
    public boolean addTodoItem(String name, String calId, String desc, List<Subtask> subtasks, boolean done, DateTime startDate, DateTime endDate, boolean updateGoogleCal)
    {
        int uiIdx = getSize();

        long rowId = databaseHandler.addToDoItem(uiIdx, calId, name, desc, done, subtasks, startDate, endDate, updateGoogleCal);
        if (rowId < 0)
            return false;

        todoItems.add(uiIdx, new TodoItem(new BigDecimal(rowId).intValueExact(), uiIdx, calId, name, desc, subtasks, startDate, endDate, done, updateGoogleCal));

        if(GoogleCalendarManager.isCalendarEnabled() && updateGoogleCal){
            if(null == calendarManager){
                calendarManager = GoogleCalendarManager.getInstance(activity);
            }
            calendarManager.createCalendarItem(activity, calendarAware, calendarManager.getCalendarName(), new CalendarEvent(name, calId, desc, startDate, endDate));
        }

        return true;
    }

    @Override
    public void removeTodoItem(int uiIdx)
    {
        if(GoogleCalendarManager.isCalendarEnabled()){
            if(null == calendarManager){
                calendarManager = GoogleCalendarManager.getInstance(activity);
            }
            calendarManager.deleteCalendarItem(activity, calendarAware, calendarManager.getCalendarName(), todoItems.get(uiIdx).getCalId());
        }

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
        if(GoogleCalendarManager.isCalendarEnabled()){
            if(null == calendarManager){
                calendarManager = GoogleCalendarManager.getInstance(activity);
            }
            calendarManager.updateCalendarItem(activity, calendarAware, calendarManager.getCalendarName(), item.getName(), new CalendarEvent(name, item.getCalId(), item.getDescription(), item.getStartDate(), item.getEndDate()));
        }
        item.setName(name);
        todoItems.set(uiIdx, item);
        return databaseHandler.updateName(uiIdx, name);
    }

    @Override
    public boolean setDesc(int uiIdx, String desc)
    {
        TodoItem item = todoItems.get(uiIdx);
        if(GoogleCalendarManager.isCalendarEnabled()){
            if(null == calendarManager){
                calendarManager = GoogleCalendarManager.getInstance(activity);
            }
            calendarManager.updateCalendarItem(activity, calendarAware, calendarManager.getCalendarName(), item.getName(), new CalendarEvent(item.getName(),item.getCalId(), desc, item.getStartDate(), item.getEndDate()));
        }
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

    @Override
    public boolean setSyncWithGCal(int uiIdx, boolean syncWithGCal)
    {
        TodoItem item = todoItems.get(uiIdx);
        item.setDone(syncWithGCal);
        todoItems.set(uiIdx, item);
        return databaseHandler.updateGCalSync(uiIdx, syncWithGCal);
    }

    @Override
    public boolean setEndDate(int uiIdx, DateTime endDate) {
        TodoItem item = todoItems.get(uiIdx);
        if(GoogleCalendarManager.isCalendarEnabled()){
            if(null == calendarManager){
                calendarManager = GoogleCalendarManager.getInstance(activity);
            }
            calendarManager.updateCalendarItem(activity, calendarAware, calendarManager.getCalendarName(), item.getName(), new CalendarEvent(item.getName(), item.getCalId(), item.getDescription(), item.getStartDate(), endDate));
        }
        item.setEndDate(endDate);
        todoItems.set(uiIdx, item);
        return databaseHandler.updateEndDate(uiIdx, endDate);
    }

    @Override
    public boolean setStartDate(int uiIdx, DateTime startDate) {
        TodoItem item = todoItems.get(uiIdx);
        if(GoogleCalendarManager.isCalendarEnabled()){
            if(null == calendarManager){
                calendarManager = GoogleCalendarManager.getInstance(activity);
            }
            calendarManager.updateCalendarItem(activity, calendarAware, calendarManager.getCalendarName(), item.getName(), new CalendarEvent(item.getName(), item.getCalId(), item.getDescription(), startDate, item.getEndDate()));
        }
        item.setStartDate(startDate);
        todoItems.set(uiIdx, item);
        return databaseHandler.updateStartDate(uiIdx, startDate);
    }

    @Override
    public void syncWithCalendarEvents(List<CalendarEvent> events) {
        for(CalendarEvent event : events){
            if(!todoListContainsTitle(event.getTitle())){
                addTodoItem(event.getTitle(), event.getId(), event.getDescription(), new ArrayList(), false, event.getStartDate(), event.getendDate(), false);
            }
        }
    }

    //This could make above method really inefficeint, should be revised
    private boolean todoListContainsTitle(String title){
        for(TodoItem item : todoItems){
            if(item.getName().equals(title)){
                return true;
            }
        }
        return false;
    }
}
