package attendance.batch.alarm;

import attendance.batch.TestBatchConfig;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBatchTest
@SpringBootTest(classes = {AlarmBatch.class, TestBatchConfig.class})
class AlarmBatchTest {
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
}