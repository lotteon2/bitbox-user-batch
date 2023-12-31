package attendance.batch.attendance;

import attendance.batch.domain.Attendance;
import attendance.batch.domain.Member;
import attendance.util.DateUtil;
import io.github.bitbox.bitbox.enums.AttendanceStatus;
import io.github.bitbox.bitbox.enums.AuthorityType;
import lombok.RequiredArgsConstructor;
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
@Configuration
public class AttendanceBatch {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory emf;
    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
    private int chunkSize;
    private final AttendanceStatus DEFAULT_ATTENDANCE_STATE = AttendanceStatus.ABSENT;
    private final AuthorityType DEFAULT_MEMBER_AUTHORITY = AuthorityType.TRAINEE;

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
        params.put("memberAuthority", DEFAULT_MEMBER_AUTHORITY);

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
        return member -> Attendance.builder()
                .member(member)
                .attendanceDate(DateUtil.convertToLocalDate(date))
                .attendanceState(DEFAULT_ATTENDANCE_STATE)
                .build();
    }

    @Bean
    public JpaItemWriter<Attendance> attendanceDbItemWriter() {
        JpaItemWriter<Attendance> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(emf);
        return jpaItemWriter;
    }
}