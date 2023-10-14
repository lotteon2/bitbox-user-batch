package attendance.batch.absent;

import attendance.batch.TestBatchConfig;
import attendance.batch.domain.Attendance;
import attendance.batch.domain.Member;
import attendance.batch.repository.AttendanceRepository;
import attendance.batch.repository.MemberRepositoryTest;
import attendance.util.DateUtil;
import io.github.bitbox.bitbox.enums.AttendanceStatus;
import io.github.bitbox.bitbox.enums.AuthorityType;
import org.junit.jupiter.api.AfterEach;
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

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBatchTest
@SpringBootTest(classes = {AbsentBatch.class, TestBatchConfig.class})
class AbsentBatchTest {
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
    @Autowired
    private MemberRepositoryTest memberRepositoryTest;
    @Autowired
    private AttendanceRepository attendanceRepository;
    private Attendance attendance3;

    @BeforeEach
    public void insertData(){
        Member member1 = new Member(1L, "csh1", "seonghun7304@naver.com", "path", 0L, AuthorityType.MANAGER, false);
        Member member2 = new Member(2L, "csh2", "seonghun7305@naver.com", "path2", 1L, AuthorityType.ADMIN,  false);
        Member member3 = new Member(3L, "csh3", "seonghun7306@naver.com", "path3", 2L, AuthorityType.TRAINEE,  false);
        memberRepositoryTest.save(member1);
        memberRepositoryTest.save(member2);
        memberRepositoryTest.save(member3);


        Attendance attendance1 = new Attendance(member1,DateUtil.convertToLocalDate("20231014"),AttendanceStatus.ATTENDANCE);
        Attendance attendance2 = new Attendance(member2,DateUtil.convertToLocalDate("20231014"),AttendanceStatus.ATTENDANCE);
        attendance3 = new Attendance(member3,DateUtil.convertToLocalDate("20231014"),AttendanceStatus.ATTENDANCE);
        attendance3.setQuitTime(LocalTime.now());
        attendanceRepository.save(attendance1);
        attendanceRepository.save(attendance2);
        attendanceRepository.save(attendance3);
    }

    @AfterEach
    public void deleteData(){
        attendanceRepository.deleteAll();
        memberRepositoryTest.deleteAll();
    }

    @Test
    public void attendance_테이블에_QUIT_TIME이_2개의_ROW가_존재하지_않으므로_2개의_출석정보가_ABSENT이다() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("date", "20231014")
                .addString("version","0")
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        List<Attendance> attendances = attendanceRepository.findByAttendanceState(AttendanceStatus.ABSENT);
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        Assertions.assertEquals(attendances.size(), 2);
    }

    @Test
    public void attendance_테이블에_QUIT_TIME이_3개의_ROW가_존재하지_않으므로_3개의_출석정보가_ABSENT이다() throws Exception {
        attendance3.setQuitTime(null);
        attendanceRepository.save(attendance3);

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("date", "20231014")
                .addString("version","1")
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        List<Attendance> attendances = attendanceRepository.findByAttendanceState(AttendanceStatus.ABSENT);
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        Assertions.assertEquals(attendances.size(), 3);
    }
}