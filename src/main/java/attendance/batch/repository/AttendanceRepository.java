package attendance.batch.repository;

import attendance.batch.domain.Attendance;
import io.github.bitbox.bitbox.enums.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByAttendanceState(AttendanceStatus attendanceState);
}