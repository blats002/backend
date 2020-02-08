package com.apiblast.customcode.example.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class ParseDate {
    public static String parseDate(String dateparse, String mode) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        Date date;
        if(mode.equals("startDate")){
            date = dt.parse(dateparse+"T00:00:00.000-05:00");
        } else {
            date = dt.parse(dateparse+"T24:00:00.999-05:00");
        }
        calendar.setTime(date);
        TimeZone fromTimeZone = calendar.getTimeZone();
        TimeZone toTimeZone = TimeZone.getTimeZone("UTC");

        calendar.setTimeZone(fromTimeZone);
        calendar.add(Calendar.MILLISECOND, fromTimeZone.getRawOffset() * -1);
        if (fromTimeZone.inDaylightTime(calendar.getTime())) {
            calendar.add(Calendar.MILLISECOND, calendar.getTimeZone().getDSTSavings() * -1);
        }

        calendar.add(Calendar.MILLISECOND, toTimeZone.getRawOffset());
        if (toTimeZone.inDaylightTime(calendar.getTime())) {
            calendar.add(Calendar.MILLISECOND, toTimeZone.getDSTSavings());
        }
		/*2017-05-16T16:37:49.849Z*/
        String year = String.valueOf(calendar.getWeekYear());
        String month = numToString(calendar.get(Calendar.MONTH) + 1);
        String dateDay = numToString(calendar.get(Calendar.DATE));
        String hour = numToString(calendar.get(Calendar.HOUR));
        String minute = numToString(calendar.get(Calendar.MINUTE));
        String seconds = numToString(calendar.get(Calendar.SECOND));
        String milliSeconds = numToString(calendar.get(Calendar.MILLISECOND));

        return year+"-"+month+"-"+dateDay+"T"+hour+":"+minute+":"+seconds+"."+milliSeconds+"Z";
		/*System.out.println(calendar.getWeekYear());
		System.out.println(numToString(calendar.get(Calendar.DAY_OF_MONTH)));*/
    }

    private static String numToString(int num){
        if(num < 10){
            return "0"+num;
        } else {
            return String.valueOf(num);
        }
    }
}
