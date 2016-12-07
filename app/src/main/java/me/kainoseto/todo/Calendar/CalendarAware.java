package me.kainoseto.todo.Calendar;

import java.util.List;

/**
 * Created by TYLER on 12/6/2016.
 */

public interface CalendarAware {

    /**
     * Called when GetCalendarItemsTask finishes retreiving calendar events. Allows UI to receive results asynchronously.
     *
     * @param events list of events returned by the Google Calendar API
     */
    void onGetCalendarItemsResult(List<CalendarEvent> events);

    /**
     * Called when PostCalendarItemTask finishes inserting an item into a calendar
     */
    void onPostCalendarItemsResult();
}
