package me.kainoseto.todo.Database;

import java.util.List;

import me.kainoseto.todo.Content.Subtask;
import me.kainoseto.todo.Content.TodoItem;

/**
 * Created by TYLER on 11/21/2016.
 */

public interface ContentManager {
    void refreshContent();
    void resetContent();
    boolean addTodoItem(String name, String desc, List<Subtask> subtasks, boolean done);
    void removeTodoItem(int uiIdx);
    TodoItem getTodoItem(int uiIdx);
    int getSize();
    boolean setName(int uiIdx, String name);
    boolean setDesc(int uiIdx, String desc);
    boolean setDone(int uiIdx, boolean done);
    boolean setUiIdx(int uiIdx, int newUiIdx);
    boolean setSubtasks(int uiIdx, List<Subtask> subtasks);
    boolean setSubtaskForTodoItem(int uiIdx, int subtaskIdx, Subtask subtask);
    void swapUiIdx(int firstIdx, int secondIdx);
}
