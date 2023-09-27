package attendance.batch.repository;

import attendance.batch.domain.Member;
import org.springframework.data.repository.CrudRepository;

public interface MemberRepositoryTest extends CrudRepository<Member, Long> {
}
