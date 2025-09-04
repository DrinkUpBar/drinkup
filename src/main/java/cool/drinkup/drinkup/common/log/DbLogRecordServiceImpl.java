package cool.drinkup.drinkup.common.log;

import com.mzt.logapi.service.ILogRecordService;
import cool.drinkup.drinkup.common.log.repository.impl.LogRecord;
import cool.drinkup.drinkup.common.log.repository.impl.LogRecordRepository;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DbLogRecordServiceImpl implements ILogRecordService {

    @Qualifier("elasticsearchLogRecordRepository")
    private final LogRecordRepository logRecordRepository;

    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    @Override
    public void record(com.mzt.logapi.beans.LogRecord bizLogRecord) {
        executorService.submit(() -> {
            try {
                // 将框架的LogRecord转换为我们的LogRecord实体
                LogRecord logRecord = convertToLogRecord(bizLogRecord);

                // 保存到Elasticsearch
                logRecordRepository.save(logRecord);

                log.info(
                        "成功保存日志记录到Elasticsearch: bizNo={}, type={}, operator={}, action={}",
                        logRecord.getBizNo(),
                        logRecord.getType(),
                        logRecord.getOperator(),
                        logRecord.getAction());

            } catch (Exception e) {
                log.error("保存日志记录到Elasticsearch失败", e);
            }
        });
    }

    @Override
    public List<com.mzt.logapi.beans.LogRecord> queryLog(String bizNo, String type) {
        try {
            List<LogRecord> logRecords = logRecordRepository.findByBizNoAndType(bizNo, type);

            // 转换回框架的LogRecord格式
            List<com.mzt.logapi.beans.LogRecord> result = new ArrayList<>();
            for (LogRecord logRecord : logRecords) {
                result.add(convertToBizLogRecord(logRecord));
            }

            log.info("从Elasticsearch查询到{}条日志记录: bizNo={}, type={}", result.size(), bizNo, type);
            return result;

        } catch (Exception e) {
            log.error("从Elasticsearch查询日志记录失败: bizNo={}, type={}", bizNo, type, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<com.mzt.logapi.beans.LogRecord> queryLogByBizNo(String bizNo, String type, String subType) {
        try {
            List<LogRecord> logRecords = logRecordRepository.findByBizNoAndTypeAndSubType(bizNo, type, subType);

            // 转换回框架的LogRecord格式
            List<com.mzt.logapi.beans.LogRecord> result = new ArrayList<>();
            for (LogRecord logRecord : logRecords) {
                result.add(convertToBizLogRecord(logRecord));
            }

            log.info(
                    "从Elasticsearch根据bizNo查询到{}条日志记录: bizNo={}, type={}, subType={}",
                    result.size(),
                    bizNo,
                    type,
                    subType);
            return result;

        } catch (Exception e) {
            log.error("从Elasticsearch根据bizNo查询日志记录失败: bizNo={}, type={}, subType={}", bizNo, type, subType, e);
            return new ArrayList<>();
        }
    }

    /**
     * 将框架的LogRecord转换为我们的LogRecord实体
     */
    private LogRecord convertToLogRecord(com.mzt.logapi.beans.LogRecord bizLogRecord) {
        return LogRecord.builder()
                .id(UUID.randomUUID().toString())
                .tenant(bizLogRecord.getTenant())
                .type(bizLogRecord.getType())
                .subType(bizLogRecord.getSubType())
                .bizNo(bizLogRecord.getBizNo())
                .operator(bizLogRecord.getOperator())
                .action(bizLogRecord.getAction())
                .fail(bizLogRecord.isFail())
                .createTime(ZonedDateTime.now(ZoneOffset.UTC))
                .extra(bizLogRecord.getExtra())
                .codeVariable(LogRecord.CodeVariable.builder()
                        .className(
                                bizLogRecord.getCodeVariable() != null
                                                && bizLogRecord
                                                                .getCodeVariable()
                                                                .get(com.mzt.logapi.beans.CodeVariableType.ClassName)
                                                        != null
                                        ? bizLogRecord
                                                .getCodeVariable()
                                                .get(com.mzt.logapi.beans.CodeVariableType.ClassName)
                                                .toString()
                                        : null)
                        .methodName(
                                bizLogRecord.getCodeVariable() != null
                                                && bizLogRecord
                                                                .getCodeVariable()
                                                                .get(com.mzt.logapi.beans.CodeVariableType.MethodName)
                                                        != null
                                        ? bizLogRecord
                                                .getCodeVariable()
                                                .get(com.mzt.logapi.beans.CodeVariableType.MethodName)
                                                .toString()
                                        : null)
                        .build())
                .build();
    }

    /**
     * 将我们的LogRecord实体转换回框架的LogRecord
     */
    private com.mzt.logapi.beans.LogRecord convertToBizLogRecord(LogRecord logRecord) {
        com.mzt.logapi.beans.LogRecord bizLogRecord = new com.mzt.logapi.beans.LogRecord();
        bizLogRecord.setId(logRecord.getId());
        bizLogRecord.setTenant(logRecord.getTenant());
        bizLogRecord.setType(logRecord.getType());
        bizLogRecord.setSubType(logRecord.getSubType());
        bizLogRecord.setBizNo(logRecord.getBizNo());
        bizLogRecord.setOperator(logRecord.getOperator());
        bizLogRecord.setAction(logRecord.getAction());
        bizLogRecord.setFail(logRecord.getFail());
        bizLogRecord.setExtra(logRecord.getExtra());
        Instant instant = logRecord.getCreateTime().toInstant();
        bizLogRecord.setCreateTime(Date.from(instant));

        // 处理CodeVariable可能为null的情况
        if (logRecord.getCodeVariable() != null) {
            bizLogRecord.setCodeVariable(Map.of(
                    com.mzt.logapi.beans.CodeVariableType.ClassName,
                    logRecord.getCodeVariable().getClassName() != null
                            ? logRecord.getCodeVariable().getClassName()
                            : "",
                    com.mzt.logapi.beans.CodeVariableType.MethodName,
                    logRecord.getCodeVariable().getMethodName() != null
                            ? logRecord.getCodeVariable().getMethodName()
                            : ""));
        }

        return bizLogRecord;
    }
}
