package me.kainoseto.todo.Content;

import com.google.api.client.util.DateTime;

import java.util.List;

import me.kainoseto.todo.Util.DateTimeUtil;
import me.kainoseto.todo.Util.StringUtil;

/**
 * Created by Kainoa on 10/14/16.
 */

public class TodoItem
{
    private int id;
    private int uiIdx;
    private String calId;
    private String name;
    private String description;
    private boolean done;
    private List<Subtask> subtasks;

    private DateTime startDate;
    private DateTime endDate;

    public TodoItem(int id, int uiIdx, String calId, String name, String description, List<Subtask> subtasks, DateTime startDate, DateTime endDate, boolean done)
    {
        this.id = id;
        this.uiIdx = uiIdx;
        this.calId = calId;

        this.name = StringUtil.catchNullString(name);
        this.description = StringUtil.catchNullString(description);
        this.subtasks = subtasks;
        this.startDate = startDate;
        this.endDate = endDate;
        this.done = done;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() { return id; }

    public int getUiIdx() { return uiIdx; }

    public void setUidIdx(int uiIdx) { this.uiIdx = uiIdx; }

    public List<Subtask> getSubtasks(){return this.subtasks;}

    public void setSubtasks(List<Subtask> subtasks){this.subtasks = subtasks;}

    public DateTime getEndDate() {return endDate;}

    public void setEndDate(DateTime endDate) {this.endDate = endDate;}

    public DateTime getStartDate() {return startDate;}

    public void setStartDate(DateTime startDate) {this.startDate = startDate;}

    public String getCalId() {return calId;}

    public void setCalId(String calId) {this.calId = calId;}

}
