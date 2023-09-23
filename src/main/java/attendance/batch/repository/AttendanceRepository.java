package attendance.batch.repository;

import attendance.batch.domain.Attendance;
import org.springframework.data.repository.CrudRepository;

public interface AttendanceRepository extends CrudRepository<Attendance, Long> {

}