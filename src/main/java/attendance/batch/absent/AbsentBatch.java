package attendance.batch.absent;

import attendance.batch.domain.Attendance;
import io.github.bitbox.bitbox.enums.AttendanceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Configuration
public class AbsentBatch {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private int chunkSize = 500;
    private final AttendanceStatus SELECT_ATTENDANCE_STATE = AttendanceStatus.ATTENDANCE;
    private final AttendanceStatus UPDATE_ATTENDANCE_STATE = AttendanceStatus.ABSENT;


    // 매일 10시35분에 도는 배치
    @Bean
    public Job absentPatchJob() throws Exception {
        return jobBuilderFactory.get("absentPatchJob")
                .start(absentPatchStep()).build();
    }

    @Bean
    public Step absentPatchStep() throws Exception {
        return stepBuilderFactory.get("absentPatchStep")
                .<Attendance, Long>chunk(chunkSize)
                .reader(absentPatchReader())
                .processor(absentPatchProcessor())
                .writer(absentPatchWriter())
                .build();
    }

    @Bean
    public JdbcPagingItemReader<Attendance> absentPatchReader() throws Exception {
        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("attendanceState", SELECT_ATTENDANCE_STATE.toString());

        JdbcPagingItemReader<Attendance> itemReader = new JdbcPagingItemReader<>();
        itemReader.setDataSource(dataSource);
        itemReader.setFetchSize(chunkSize);
        itemReader.setPageSize(chunkSize);
        itemReader.setRowMapper(new BeanPropertyRowMapper<>(Attendance.class));
        itemReader.setQueryProvider(createQueryProvider());
        itemReader.setParameterValues(parameterValues);
        itemReader.setName("absentPatchReader");

        return itemReader;
    }

    @Bean
    public ItemProcessor<Attendance, Long> absentPatchProcessor() {
        return Attendance::getAttendanceId;
    }

    @Bean
    public ItemWriter<Long> absentPatchWriter() {
        return items -> {
            String updateQuery = "UPDATE attendance SET attendance_state = :attendance_state WHERE attendance_id IN (:ids)";
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("attendance_state", UPDATE_ATTENDANCE_STATE.toString());
            parameters.addValue("ids", items);
            jdbcTemplate.update(updateQuery, parameters);
        };
    }

    @Bean
    public PagingQueryProvider createQueryProvider() throws Exception {
        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(dataSource);
        queryProvider.setSelectClause("attendance_id, attendance_date, attendance_modify_reason, attendance_state, entrace_time, quit_time, member_id");
        queryProvider.setFromClause("attendance");
        queryProvider.setWhereClause("attendance_state = :attendanceState and quit_time IS NULL");

        Map<String, Order> sortKeys = new HashMap<>(1);
        sortKeys.put("attendance_id", Order.ASCENDING);

        queryProvider.setSortKeys(sortKeys);
        return queryProvider.getObject();
    }

}
