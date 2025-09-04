package cool.drinkup.drinkup.workflow.internal.config;

import cool.drinkup.drinkup.user.spi.AuthenticatedUserDTO;
import cool.drinkup.drinkup.user.spi.AuthenticationServiceFacade;
import java.util.Optional;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepositoryDialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class ChatMemoryConfig {

    @Bean
    public JdbcChatMemoryRepositoryDialect myJdbcChatMemoryRepositoryDialect(
            AuthenticationServiceFacade authenticationServiceFacade) {
        return new MyChatMemoryRepositoryDialect(authenticationServiceFacade);
    }

    @Bean
    public JdbcChatMemoryRepository jdbcChatMemoryRepository(
            JdbcTemplate jdbcTemplate, JdbcChatMemoryRepositoryDialect dialect) {
        return JdbcChatMemoryRepository.builder()
                .jdbcTemplate(jdbcTemplate)
                .dialect(dialect)
                .build();
    }

    public static class MyChatMemoryRepositoryDialect implements JdbcChatMemoryRepositoryDialect {

        private AuthenticationServiceFacade authenticationServiceFacade;

        public MyChatMemoryRepositoryDialect(AuthenticationServiceFacade authenticationServiceFacade) {
            this.authenticationServiceFacade = authenticationServiceFacade;
        }

        private Long getUserId() {
            Optional<AuthenticatedUserDTO> currentAuthenticatedUser =
                    authenticationServiceFacade.getCurrentAuthenticatedUser();
            if (currentAuthenticatedUser.isPresent()) {
                return currentAuthenticatedUser.get().userId();
            }
            return null;
        }

        @Override
        public String getSelectMessagesSql() {
            return "SELECT content, type FROM SPRING_AI_CHAT_MEMORY WHERE conversation_id = ? ORDER"
                    + " BY `timestamp`";
        }

        @Override
        public String getInsertMessageSql() {
            Long userId = getUserId();
            if (userId == null) {
                return "INSERT INTO SPRING_AI_CHAT_MEMORY (conversation_id, content, type,"
                        + " `timestamp`) VALUES (?, ?, ?, ?)";
            } else {
                return "INSERT INTO SPRING_AI_CHAT_MEMORY (conversation_id, content, type,"
                        + " `timestamp`, user_id) VALUES (?, ?, ?, ?, "
                        + userId
                        + ")";
            }
        }

        @Override
        public String getSelectConversationIdsSql() {
            return "SELECT DISTINCT conversation_id FROM SPRING_AI_CHAT_MEMORY";
        }

        @Override
        public String getDeleteMessagesSql() {
            return "DELETE FROM SPRING_AI_CHAT_MEMORY WHERE conversation_id = ?";
        }
    }
}
