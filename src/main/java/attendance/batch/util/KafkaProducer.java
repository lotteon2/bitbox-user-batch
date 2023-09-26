package attendance.batch.util;

import attendance.batch.exception.KafkaSubmitException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bitbox.bitbox.dto.NotificationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
public class KafkaProducer {
    public static void send(KafkaTemplate<String,String> kafkaTemplate, String topic, NotificationDto notificationDto){
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString;

        try{
            jsonInString = mapper.writeValueAsString(notificationDto);
        }catch (JsonProcessingException e){
            throw new RuntimeException("JSON 파싱에 실패했습니다",e);
        }

        try {
            kafkaTemplate.send(topic, jsonInString);
        }catch(Exception e){
            log.error("스프링 배치 실패함(카프카 전송 에러)");
            throw new KafkaSubmitException("카프카 메시지 전송에 실패했습니다",e);
        }

    }
}