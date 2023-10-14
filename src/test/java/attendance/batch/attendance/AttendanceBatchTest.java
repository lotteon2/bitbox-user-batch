package attendance.batch.attendance;

import attendance.batch.TestBatchConfig;
import attendance.batch.domain.Attendance;
import attendance.batch.domain.Member;
import attendance.batch.repository.AttendanceRepository;
import attendance.batch.repository.MemberRepositoryTest;
import io.github.bitbox.bitbox.enums.AuthorityType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBatchTest
@SpringBootTest(classes = {AttendanceBatch.class, TestBatchConfig.class})
class AttendanceBatchTest {
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
    @Autowired
    private MemberRepositoryTest memberRepositoryTest;
    @Autowired
    private AttendanceRepository attendanceRepository;

    @BeforeEach
    public void insertData(){
        List<Member> members = new ArrayList<>();

        members.add(new Member("csh1", 1L, "csh", "seonghun7304@naver.com", "path", 0L, AuthorityType.ADMIN,  false));
        members.add(new Member("csh2", 2L, "csh2", "seonghun7305@naver.com", "path2", 1L,AuthorityType.TRAINEE, true));
        members.add(new Member("csh3", 3L, "csh3", "seonghun7306@naver.com", "path3", 2L, AuthorityType.TRAINEE, false));
        members.add(new Member("csh4", 4L, "csh4", "seonghun7307@naver.com", "path4", 3L, AuthorityType.TRAINEE,  true));
        members.add(new Member("csh5", 5L, "csh5", "seonghun7308@naver.com", "path5", 4L, AuthorityType.TRAINEE,  false));

        memberRepositoryTest.saveAll(members);
    }

    @Test
    public void member_테이블에서_삭제가안되었고_TRAINEE인학생의_수만큼_attendance_테이블에_삽입된다() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("date", "20230922")
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        List<Attendance> attendances = (List<Attendance>) attendanceRepository.findAll();
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        Assertions.assertEquals(attendances.size(), 2);
    }
}