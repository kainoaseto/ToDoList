package co.xelabs.todo.DateTime;

import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import com.google.api.client.util.DateTime;
import com.wdullaer.materialdatetimepicker.time.Timepoint;

import java.util.Locale;

/**
 * Created by Kainoa on 12/8/2016.
 */

public class DateTimeListener implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener
{
    private DateTime currentDateTime;
    private Calendar calendar;
    private TextView timeView;
    private TextView dateView;

    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;

    public DateTimeListener(TextView date, TextView time)
    {
        this.timeView = time;
        this.dateView = date;
        currentDateTime = null;
        calendar = Calendar.getInstance();

        dateFormat = new SimpleDateFormat("E MMM dd, yyyy", Locale.US);
        timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
    }

    public DateTime getDateTime()
    {
        currentDateTime = new DateTime(calendar.getTimeInMillis());
        return currentDateTime;
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        //String date = "You picked the following date: "+dayOfMonth+"/"+(++monthOfYear)+"/"+year;
        calendar.set(year, monthOfYear, dayOfMonth);

        Calendar newDate = Calendar.getInstance();
        newDate.set(year, monthOfYear, dayOfMonth);
        dateView.setText(dateFormat.format(newDate.getTime()));
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        Timepoint newTime = view.getSelectedTime();

        if(hourOfDay > 12)
            hourOfDay -= 12;

        String hourString = hourOfDay < 10 ? "0"+hourOfDay : ""+hourOfDay;
        String minuteString = minute < 10 ? "0"+minute : ""+minute;
        String postFix = newTime.isAM() ? "AM" : "PM";
        String time = hourString+ ":" +minuteString+" " + postFix;
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.AM_PM, newTime.isAM() ? Calendar.AM : Calendar.PM);

        timeView.setText(time);
    }
}
