package attendance.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateUtil {
    public static LocalDate convertToLocalDate(String date) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        try {
            LocalDate localDate = LocalDate.parse(date, inputFormatter);
            return localDate;
        } catch (DateTimeParseException e) {
            throw new RuntimeException(e);
        }
    }
}
