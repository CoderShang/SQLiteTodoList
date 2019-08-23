package com.shang.todolist;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class UiUtils {


    /**
     * dip转pix
     *
     * @param dpValue
     * @return
     */
    public static int dip2px(float dpValue) {
        final float scale = App.get().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static String getDateStr(long time) {
        String format = "MM月dd日 HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(time));
    }

    public static String getTodayAndWeekStr() {
        Calendar mCalendar = Calendar.getInstance();
        int mYear = mCalendar.get(Calendar.YEAR);
        int mMonth = mCalendar.get(Calendar.MONTH) + 1;
        int mDay = mCalendar.get(Calendar.DAY_OF_MONTH);
        int mDayOfWeek = mCalendar.get(Calendar.DAY_OF_WEEK);
        StringBuilder sbDate = new StringBuilder();
        Locale locale = App.get().getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("zh")) {
            sbDate.append(mMonth).append("月").append(mDay).append("日").append(" ").append(calculateDayOfWeek(mDayOfWeek));
        } else {
            sbDate.append(calculateDayOfWeek(mDayOfWeek)).append(", ").append(convertMonth(mMonth)).append(" ").append(mDay);
        }
        return sbDate.toString();
    }

    public static String getTodayStr() {
        Calendar mCalendar = Calendar.getInstance();
        int mYear = mCalendar.get(Calendar.YEAR);
        int mMonth = mCalendar.get(Calendar.MONTH) + 1;
        int mDay = mCalendar.get(Calendar.DAY_OF_MONTH);
        int mDayOfWeek = mCalendar.get(Calendar.DAY_OF_WEEK);
        StringBuilder sbDate = new StringBuilder();
        Locale locale = App.get().getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("zh")) {
            sbDate.append(mMonth).append("月").append(mDay).append("日").append(" ").append(calculateDayOfWeek(mDayOfWeek));
        } else {
            sbDate.append(calculateDayOfWeek(mDayOfWeek)).append(", ").append(convertMonth(mMonth)).append(" ").append(mDay);
        }
        return sbDate.toString();
    }

    private static String convertMonth(int month) {
        String monthStr = null;
        switch (month) {
            case 1:
                monthStr = "Jan";
                break;
            case 2:
                monthStr = "Feb";
                break;
            case 3:
                monthStr = "Mar";
                break;
            case 4:
                monthStr = "Apr";
                break;
            case 5:
                monthStr = "May";
                break;
            case 6:
                monthStr = "June";
                break;
            case 7:
                monthStr = "July";
                break;
            case 8:
                monthStr = "Aug";
                break;
            case 9:
                monthStr = "Sept";
                break;
            case 10:
                monthStr = "Oct";
                break;
            case 11:
                monthStr = "Nov";
                break;
            case 12:
                monthStr = "Dec";
                break;
            default:
        }
        return monthStr;
    }

    private static String calculateDayOfWeek(int day) {
        String dayStr = null;
        switch (day) {
            case 1:
                dayStr = App.get().getResources().getString(R.string.week_sunday);
                break;
            case 2:
                dayStr = App.get().getResources().getString(R.string.week_monday);
                break;
            case 3:
                dayStr = App.get().getResources().getString(R.string.week_tuesday);
                break;
            case 4:
                dayStr = App.get().getResources().getString(R.string.week_wednesday);
                break;
            case 5:
                dayStr = App.get().getResources().getString(R.string.week_thursday);
                break;
            case 6:
                dayStr = App.get().getResources().getString(R.string.week_friday);
                break;
            case 7:
                dayStr = App.get().getResources().getString(R.string.week_saturday);
                break;
            default:
        }
        return dayStr;
    }
}
