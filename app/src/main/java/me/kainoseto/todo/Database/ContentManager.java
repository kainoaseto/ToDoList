package me.kainoseto.todo.Database;

import me.kainoseto.todo.Content.TodoItem;

/**
 * Created by TYLER on 11/21/2016.
 */

public interface ContentManager {
    void refreshContent();
    void resetContent();
    boolean addTodoItem(String name, String desc, boolean done);
    void removeTodoItem(int uiIdx);
    TodoItem getTodoItem(int uiIdx);
    int getSize();
    boolean setName(int uiIdx, String name);
    boolean setDesc(int uiIdx, String desc);
    boolean setDone(int uiIdx, boolean done);
    boolean setUiIdx(int uiIdx, int newUiIdx);
    void swapUiIdx(int firstIdx, int secondIdx);
}
