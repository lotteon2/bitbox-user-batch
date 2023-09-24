package attendance.batch.attendance;

import attendance.batch.domain.Attendance;
import attendance.batch.domain.Member;
import attendance.batch.util.StringToDateType;
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

@RequiredArgsConstructor
@Slf4j
@Configuration
public class AttendanceBatch {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory emf;
    private final int chunkSize = 500;
    private final String defaultAttendanceState = "결석";

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
        return new JpaPagingItemReaderBuilder<Member>()
                .name("member_dbItemReader")
                .entityManagerFactory(emf)
                .pageSize(chunkSize)
                .queryString("SELECT m FROM Member m ORDER BY m.memberId ASC")
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<Member, Attendance> memberToAttendanceProcessor(@Value("#{jobParameters[date]}") String date) {
        return member -> new Attendance(member, StringToDateType.convertToSqlDate(date),defaultAttendanceState);
    }

    @Bean
    public JpaItemWriter<Attendance> attendanceDbItemWriter() {
        JpaItemWriter<Attendance> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(emf);
        return jpaItemWriter;
    }
}