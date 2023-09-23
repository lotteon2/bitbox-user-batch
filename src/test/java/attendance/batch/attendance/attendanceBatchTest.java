package attendance.batch.attendance;

import attendance.batch.TestBatchConfig;
import attendance.batch.domain.Attendance;
import attendance.batch.domain.Member;
import attendance.batch.repository.AttendanceRepository;
import attendance.batch.repository.MemberRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBatchTest
@SpringBootTest(classes = {AttendanceBatch.class, TestBatchConfig.class})
class attendanceBatchTest {
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private AttendanceRepository attendanceRepository;

    @BeforeEach
    public void insertData(){
        Member member1 = new Member(1L, "최성훈", "csh", "seonghun7304@naver.com", "path", 0L, "1", LocalDateTime.now(),LocalDateTime.now(), false);
        Member member2 = new Member(2L, "최성훈2", "csh2", "seonghun7305@naver.com", "path2", 1L, "2", LocalDateTime.now(),LocalDateTime.now(), false);
        memberRepository.save(member1);
        memberRepository.save(member2);
    }

    @Test
    public void member_테이블의_수만큼_attendance_테이블에_삽입된다() throws Exception {
        DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate date = LocalDate.of(2023, 9, 22);
        String formattedDate = date.format(FORMATTER);

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("date", formattedDate)
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        List<Attendance> attendances = (List<Attendance>) attendanceRepository.findAll();
        List<Member> members = (List<Member>) memberRepository.findAll();
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        Assertions.assertEquals(attendances.size(), members.size());
    }
}