package attendance.batch.attendance;

import attendance.batch.TestBatchConfig;
import attendance.batch.domain.Attendance;
import attendance.batch.domain.Member;
import attendance.batch.repository.AttendanceRepository;
import attendance.batch.repository.MemberRepositoryTest;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBatchTest
@SpringBootTest(classes = {AttendanceBatch.class, TestBatchConfig.class})
class attendanceBatchTest {
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
    @Autowired
    private MemberRepositoryTest memberRepositoryTest;
    @Autowired
    private AttendanceRepository attendanceRepository;

    @BeforeEach
    public void insertData(){
        Member member1 = new Member("csh1", 1L, "최성훈", "csh", "seonghun7304@naver.com", "path", 0L, "ADMIN", LocalDateTime.now(),LocalDateTime.now(), false);
        Member member2 = new Member("csh2", 2L, "최성훈2", "csh2", "seonghun7305@naver.com", "path2", 1L, "TRAINEE", LocalDateTime.now(),LocalDateTime.now(), true);
        Member member3 = new Member("csh3", 3L, "최성훈3", "csh3", "seonghun7306@naver.com", "path3", 2L, "TRAINEE", LocalDateTime.now(),LocalDateTime.now(), false);
        Member member4 = new Member("csh4", 4L, "최성훈4", "csh4", "seonghun7307@naver.com", "path4", 3L, "TRAINEE", LocalDateTime.now(),LocalDateTime.now(), true);
        Member member5 = new Member("csh5", 5L, "최성훈5", "csh5", "seonghun7308@naver.com", "path5", 4L, "TRAINEE", LocalDateTime.now(),LocalDateTime.now(), false);

        memberRepositoryTest.save(member1);
        memberRepositoryTest.save(member2);
        memberRepositoryTest.save(member3);
        memberRepositoryTest.save(member4);
        memberRepositoryTest.save(member5);
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