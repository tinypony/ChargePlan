import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.Calendar;

import org.junit.Test;

import utils.DateUtils;


public class DateUtilsTest {

	@Test
	public void test() {
		Calendar cal;
		try {
			cal = DateUtils.getCalendar("2015-6-6");
			assertEquals(5, cal.get(Calendar.MONTH));
			assertEquals(6, cal.get(Calendar.DAY_OF_MONTH));
		} catch (ParseException e) {
			fail("Unexpected exception");
		}
		
	}

}
