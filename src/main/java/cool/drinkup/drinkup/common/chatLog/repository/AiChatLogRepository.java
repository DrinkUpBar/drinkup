package cool.drinkup.drinkup.common.chatLog.repository;

import cool.drinkup.drinkup.common.chatLog.model.AiChatLog;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiChatLogRepository extends ElasticsearchRepository<AiChatLog, String> {}
