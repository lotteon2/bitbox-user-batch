package attendance.batch.notification;

import attendance.batch.domain.Attendance;
import attendance.util.DateUtil;
import io.github.bitbox.bitbox.dto.NotificationDto;
import io.github.bitbox.bitbox.enums.AttendanceStatus;
import io.github.bitbox.bitbox.enums.NotificationType;
import lombok.RequiredArgsConstructor;
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
@Configuration
public class NotificationBatch {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final KafkaTemplate<String, NotificationDto> kafkaTemplate;
    private final EntityManagerFactory emf;
    private final int chunkSize = 1000;
    private final AttendanceStatus DEFAULT_ATTENDANCE_STATE = AttendanceStatus.ABSENT;

    private final NotificationType messageType = NotificationType.ATTENDANCE;

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
        parameterValues.put("date", DateUtil.convertToLocalDate(date));
        parameterValues.put("state", DEFAULT_ATTENDANCE_STATE);

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
                .boardType(null)
                .senderNickname(null)
                .build();
    }

    @Bean
    public ItemWriter<NotificationDto> notificationWriter() {
        return items -> {
            for (NotificationDto item : items) {
                kafkaTemplate.send(topicName, item);
            }
        };
    }
}