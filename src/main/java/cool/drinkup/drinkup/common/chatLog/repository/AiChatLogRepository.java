package cool.drinkup.drinkup.common.chatLog.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import cool.drinkup.drinkup.common.chatLog.model.AiChatLog;

@Repository
public interface AiChatLogRepository extends ElasticsearchRepository<AiChatLog, String> {
}

