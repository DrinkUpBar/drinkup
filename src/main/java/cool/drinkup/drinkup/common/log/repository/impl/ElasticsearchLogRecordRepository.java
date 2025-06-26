package cool.drinkup.drinkup.common.log.repository.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * Elasticsearch日志记录存储实现
 * 使用Spring Data Elasticsearch将日志记录存储到Elasticsearch
 */
@Slf4j
@Repository("elasticsearchLogRecordRepository")
@RequiredArgsConstructor
public class ElasticsearchLogRecordRepository implements LogRecordRepository {

    private final LogRecordElasticsearchRepository elasticsearchRepository;

    @Override
    public LogRecord save(LogRecord logRecord) {
        try {
            LogRecord saved = elasticsearchRepository.save(logRecord);
            log.info(
                    "成功保存日志记录到Elasticsearch: id={}, bizNo={}, type={}",
                    saved.getId(),
                    saved.getBizNo(),
                    saved.getType());
            return saved;
        } catch (Exception e) {
            log.error(
                    "保存日志记录到Elasticsearch失败: id={}, bizNo={}, type={}",
                    logRecord.getId(),
                    logRecord.getBizNo(),
                    logRecord.getType(),
                    e);
            throw new RuntimeException("保存日志记录到Elasticsearch失败", e);
        }
    }

    @Override
    public List<LogRecord> findByBizNoAndType(String bizNo, String type) {
        try {
            List<LogRecord> records = elasticsearchRepository.findByBizNoAndTypeOrderByCreateTimeDesc(bizNo, type);
            log.info("从Elasticsearch查询到{}条日志记录: bizNo={}, type={}", records.size(), bizNo, type);
            return records;
        } catch (Exception e) {
            log.error("从Elasticsearch查询日志记录失败: bizNo={}, type={}", bizNo, type, e);
            throw new RuntimeException("查询日志记录失败", e);
        }
    }

    @Override
    public List<LogRecord> findByBizNoAndTypeAndSubType(String bizNo, String type, String subType) {
        try {
            List<LogRecord> records =
                    elasticsearchRepository.findByBizNoAndTypeAndSubTypeOrderByCreateTimeDesc(bizNo, type, subType);
            log.info("从Elasticsearch查询到{}条日志记录: bizNo={}, type={}, subType={}", records.size(), bizNo, type, subType);
            return records;
        } catch (Exception e) {
            log.error("从Elasticsearch查询日志记录失败: bizNo={}, type={}, subType={}", bizNo, type, subType, e);
            throw new RuntimeException("查询日志记录失败", e);
        }
    }

    @Override
    public List<LogRecord> findByTenant(String tenant) {
        try {
            List<LogRecord> records = elasticsearchRepository.findByTenantOrderByCreateTimeDesc(tenant);
            log.info("从Elasticsearch根据租户查询到{}条日志记录: tenant={}", records.size(), tenant);
            return records;
        } catch (Exception e) {
            log.error("从Elasticsearch根据租户查询日志记录失败: tenant={}", tenant, e);
            throw new RuntimeException("查询日志记录失败", e);
        }
    }

    @Override
    public List<LogRecord> findByOperator(String operator) {
        try {
            List<LogRecord> records = elasticsearchRepository.findByOperatorOrderByCreateTimeDesc(operator);
            log.info("从Elasticsearch根据操作人查询到{}条日志记录: operator={}", records.size(), operator);
            return records;
        } catch (Exception e) {
            log.error("从Elasticsearch根据操作人查询日志记录失败: operator={}", operator, e);
            throw new RuntimeException("查询日志记录失败", e);
        }
    }
}
