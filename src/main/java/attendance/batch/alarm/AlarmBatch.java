package attendance.batch.alarm;

import attendance.batch.domain.Attendance;
import attendance.batch.util.KafkaProducer;
import io.github.bitbox.bitbox.dto.NotificationDto;
import io.github.bitbox.bitbox.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

import javax.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@Configuration
public class AlarmBatch {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final EntityManagerFactory emf;
    private final int chunkSize = 1000;
    private final String defaultAttendanceState = "결석";
    private final String messageType = "attendance";

    @Value("${topicName}")
    private String topicName;

    // 매일 08:45분쯤 도는 배치
    @Bean
    public Job notificationJob() {
        return jobBuilderFactory.get("notificationJob")
                .start(notificationStep()).build();
    }

    @Bean
    public Step notificationStep() {
        return stepBuilderFactory.get("notificationStep")
                .<Attendance, NotificationDto>chunk(chunkSize)
                .reader(attendanceReader(null))
                .processor(attendanceToNotificationDtoProcessor())
                .writer(notificationWriter())
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<Attendance> attendanceReader(@Value("#{jobParameters[date]}") String date) {
        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("date", DateTimeUtil.convertToSqlDate(date));
        parameterValues.put("state", defaultAttendanceState);

        return new JpaPagingItemReaderBuilder<Attendance>()
                .name("attendanceReader")
                .entityManagerFactory(emf)
                .pageSize(chunkSize)
                .queryString("SELECT a FROM Attendance a WHERE a.attendanceDate = :date AND a.attendanceState = :state ORDER BY a.attendanceId ASC")
                .parameterValues(parameterValues)
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<Attendance, NotificationDto> attendanceToNotificationDtoProcessor() {
        return attendance -> NotificationDto.builder()
                .notificationType(messageType)
                .receiverId(attendance.getMember().getMemberId())
                .boardId(null)
                .senderNickname(null)
                .build();
    }

    @Bean
    public ItemWriter<NotificationDto> notificationWriter() {
        return items -> {
            for (NotificationDto item : items) {
                KafkaProducer.send(kafkaTemplate, topicName, item);
            }
        };
    }
}