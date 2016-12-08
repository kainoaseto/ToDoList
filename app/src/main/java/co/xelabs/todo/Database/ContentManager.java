package co.xelabs.todo.Database;

import com.google.api.client.util.DateTime;

import java.util.List;

import co.xelabs.todo.Calendar.CalendarEvent;
import co.xelabs.todo.Content.Subtask;
import co.xelabs.todo.Content.TodoItem;

/**
 * Created by TYLER on 11/21/2016.
 */

public interface ContentManager {
    void refreshContent();
    void resetContent();
    boolean addTodoItem(String name, String calId, String desc, List<Subtask> subtasks, boolean done, DateTime startDate, DateTime endDate, boolean updateGoogleCal);
    void removeTodoItem(int uiIdx);
    TodoItem getTodoItem(int uiIdx);
    int getSize();
    boolean setName(int uiIdx, String name);
    boolean setDesc(int uiIdx, String desc);
    boolean setDone(int uiIdx, boolean done);
    boolean setUiIdx(int uiIdx, int newUiIdx);
    boolean setSubtasks(int uiIdx, List<Subtask> subtasks);
    boolean setSubtaskForTodoItem(int uiIdx, int subtaskIdx, Subtask subtask);
    boolean setSyncWithGCal(int uiIdx, boolean syncWithGcal);
    void swapUiIdx(int firstIdx, int secondIdx);
    void syncWithCalendarEvents(List<CalendarEvent> events);
}
