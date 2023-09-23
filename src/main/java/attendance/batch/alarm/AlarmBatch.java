package attendance.batch.alarm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;

@RequiredArgsConstructor
@Slf4j
@Configuration
public class AlarmBatch {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory emf;
    private int chunkSize = 1000;


}

/*
    8시 45분에 배치를 돌아서 출결 테이블을 조회하고 해당 날짜에 출석상태가 결석인 학생들에게 알림을 쏜다

    (유저아이디, 메시지타입(attendance), 메시지명(?))
 */