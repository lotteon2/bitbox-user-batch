package attendance.batch.util;

import attendance.batch.dto.NotificationDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;

public class KafkaProducer {
    public static void send(KafkaTemplate<String,String> kafkaTemplate, String topic, List<? extends NotificationDto> notificationDtoList){
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString;
        try{
            jsonInString = mapper.writeValueAsString(notificationDtoList);
        }catch (JsonProcessingException e){
            throw new RuntimeException("JSON 파싱에 실패했습니다");
        }

        kafkaTemplate.send(topic, jsonInString);
    }
}