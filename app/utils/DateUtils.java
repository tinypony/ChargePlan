package utils;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateUtils {

	public static Calendar stringToCalendar(String timeString) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeString.substring(0, 2), 10));
		cal.set(Calendar.MINUTE, Integer.parseInt(timeString.substring(2, 4), 10));
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal;
	}
	
	public static Calendar stringToCalendar(Calendar cal, String timeString) {
		cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeString.substring(0, 2), 10));
		cal.set(Calendar.MINUTE, Integer.parseInt(timeString.substring(2, 4), 10));
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal;
	}
	
	public static Calendar rewindCalendar(Calendar cal) {
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal;
	}
	
	public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
	    long diffInMillies = date2.getTime() - date1.getTime();
	    return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
	}
	
	public static Calendar getCalendar(long timestamp) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date(timestamp));
		return cal;
	}
	
}
