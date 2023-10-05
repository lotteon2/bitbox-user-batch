package attendance.batch.notification;

import attendance.batch.KafkaConsumerMock;
import attendance.batch.TestBatchConfig;
import attendance.batch.domain.Attendance;
import attendance.batch.domain.Member;
import attendance.batch.repository.AttendanceRepository;
import attendance.batch.repository.MemberRepositoryTest;
import io.github.bitbox.bitbox.util.DateTimeUtil;
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

import java.time.LocalDateTime;
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
        Member member1 = new Member("csh1",1L, "최성훈", "csh", "seonghun7304@naver.com", "path", 0L, "1", LocalDateTime.now(),LocalDateTime.now(), false);
        Member member2 = new Member("csh2",2L, "최성훈2", "csh2", "seonghun7305@naver.com", "path2", 1L, "2", LocalDateTime.now(),LocalDateTime.now(), false);
        Member member3 = new Member("csh3",3L, "최성훈3", "csh3", "seonghun7306@naver.com", "path3", 2L, "3", LocalDateTime.now(),LocalDateTime.now(), false);
        memberRepositoryTest.save(member1);
        memberRepositoryTest.save(member2);
        memberRepositoryTest.save(member3);

        Attendance attendance1 = new Attendance(member1, DateTimeUtil.convertToSqlDate("20230922"),"결석");
        Attendance attendance2 = new Attendance(member2, DateTimeUtil.convertToSqlDate("20230922"),"출석");
        Attendance attendance3 = new Attendance(member3, DateTimeUtil.convertToSqlDate("20230922"),"결석");
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