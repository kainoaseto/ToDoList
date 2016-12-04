package me.kainoseto.todo.Calendar;

import com.google.api.client.util.DateTime;

import java.util.Date;

/**
 * Created by TYLER on 12/3/2016.
 */

public class CalendarEvent {
    private String title;
    private String description;
    private DateTime startDate;


    public CalendarEvent(String title, String description, DateTime startDate){
        this.title = title;
        this.description = description;
        this.startDate = startDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(DateTime startDate) {
        this.startDate = startDate;
    }
}
