package attendance.batch.attendance;

import attendance.batch.domain.Attendance;
import attendance.batch.domain.Member;
import io.github.bitbox.bitbox.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@Configuration
public class AttendanceBatch {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory emf;
    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
    private int chunkSize;
    private final String defaultAttendanceState = "결석";
    private final String defaultMemberAuthority = "TRAINEE";

    // 매일 12시 정각에 도는 배치
    @Bean
    public Job attendanceInsertJob() {
        return jobBuilderFactory.get("attendanceInsertJob")
                .start(attendanceInsertStep()).build();
    }

    @Bean
    public Step attendanceInsertStep() {
        return stepBuilderFactory.get("attendanceDeleteJob_step")
                .<Member, Attendance>chunk(chunkSize)
                .reader(memberDbItemReader())
                .processor(memberToAttendanceProcessor(null))
                .writer(attendanceDbItemWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<Member> memberDbItemReader() {
        Map<String, Object> params = new HashMap<>();
        params.put("isDeleted", false);
        params.put("memberAuthority", defaultMemberAuthority);

        return new JpaPagingItemReaderBuilder<Member>()
                .name("member_dbItemReader")
                .entityManagerFactory(emf)
                .pageSize(chunkSize)
                .queryString("SELECT m FROM Member m WHERE m.isDeleted = :isDeleted AND m.memberAuthority = :memberAuthority ORDER BY m.memberId ASC")
                .parameterValues(params)
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<Member, Attendance> memberToAttendanceProcessor(@Value("#{jobParameters[date]}") String date) {
        return member -> new Attendance(member, DateTimeUtil.convertToSqlDate(date),defaultAttendanceState);
    }

    @Bean
    public JpaItemWriter<Attendance> attendanceDbItemWriter() {
        JpaItemWriter<Attendance> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(emf);
        return jpaItemWriter;
    }
}