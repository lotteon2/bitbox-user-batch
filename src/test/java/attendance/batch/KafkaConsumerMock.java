package attendance.batch;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.concurrent.CountDownLatch;

public class KafkaConsumerMock {
    private CountDownLatch latch = new CountDownLatch(1);
    private Object payload;

    @KafkaListener(topics = "${topicName}", groupId = "testConsumerGroup")
    public void receive(ConsumerRecord<?, ?> consumerRecord) {
        payload = consumerRecord.value();
        latch.countDown();
    }

    public void resetLatch() {
        latch = new CountDownLatch(1);
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    public Object getPayload() {
        return payload;
    }
}