package attendance.batch.util;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class StringToDateType {

    public static Date convertToSqlDate(String date) {
        SimpleDateFormat inputFormatter = new SimpleDateFormat("yyyyMMdd");
        java.util.Date utilDate;

        try {
            utilDate = inputFormatter.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(utilDate);

        utilDate = calendar.getTime();

        return new Date(utilDate.getTime());
    }
}