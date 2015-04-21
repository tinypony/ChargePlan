package utils;

import java.util.Calendar;

public class DateUtils {

	public static Calendar stringToCalendar(String timeString) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeString.substring(0, 2), 10));
		cal.set(Calendar.MINUTE, Integer.parseInt(timeString.substring(2, 4), 10));
		cal.set(Calendar.SECOND, 0);
		return cal;
	}
}
