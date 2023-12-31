package attendance.batch.notification;

import attendance.batch.KafkaConsumerMock;
import attendance.batch.TestBatchConfig;
import attendance.batch.domain.Attendance;
import attendance.batch.domain.Member;
import attendance.batch.repository.AttendanceRepository;
import attendance.batch.repository.MemberRepositoryTest;
import attendance.util.DateUtil;
import io.github.bitbox.bitbox.enums.AttendanceStatus;
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
import org.springframework.kafka.test.context.EmbeddedKafka;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBatchTest
@SpringBootTest(classes = {NotificationBatch.class, TestBatchConfig.class, KafkaConsumerMock.class})
@EmbeddedKafka( partitions = 1,
                brokerProperties = { "listeners=PLAINTEXT://localhost:7777"},
                ports = {7777})
class NotificationBatchTest {
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
    @Autowired
    private MemberRepositoryTest memberRepositoryTest;
    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    private KafkaConsumerMock kafkaConsumer;

    @BeforeEach
    public void insertData(){
        Member member1 = new Member(1L, "csh1", "seonghun7304@naver.com", "path", 0L, AuthorityType.MANAGER, false);
        Member member2 = new Member(2L, "csh2", "seonghun7305@naver.com", "path2", 1L, AuthorityType.ADMIN,  false);
        Member member3 = new Member(3L, "csh3", "seonghun7306@naver.com", "path3", 2L, AuthorityType.TRAINEE,  false);
        memberRepositoryTest.save(member1);
        memberRepositoryTest.save(member2);
        memberRepositoryTest.save(member3);


        Attendance attendance1 = new Attendance(member1,DateUtil.convertToLocalDate("20230922"),AttendanceStatus.ABSENT);
        Attendance attendance2 = new Attendance(member2,DateUtil.convertToLocalDate("20230922"),AttendanceStatus.ATTENDANCE);
        Attendance attendance3 = new Attendance(member3,DateUtil.convertToLocalDate("20230922"),AttendanceStatus.ABSENT);
        attendanceRepository.save(attendance1);
        attendanceRepository.save(attendance2);
        attendanceRepository.save(attendance3);
    }

    @Test
    public void attendance_테이블에서_결석인학생들의_row정보를_카프카에서_확인할수있다() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("date", "20230922")
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        kafkaConsumer.resetLatch(); // latch 초기화
        kafkaConsumer.getLatch().await(1, TimeUnit.SECONDS);

        Assertions.assertEquals(kafkaConsumer.getPayload().size(),2);
    }
}