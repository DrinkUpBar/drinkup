package cool.drinkup.drinkup.common.log.repository.impl;

import java.util.List;

/**
 * 日志记录存储接口
 * 用于操作ElasticSearch中的日志数据
 */
public interface LogRecordRepository {

    /**
     * 保存日志记录
     * @param logRecord 日志记录
     * @return 保存的日志记录
     */
    LogRecord save(LogRecord logRecord);

    /**
     * 根据业务编号和类型查询日志
     * @param bizNo 业务编号
     * @param type 日志类型
     * @return 日志记录列表
     */
    List<LogRecord> findByBizNoAndType(String bizNo, String type);

    /**
     * 根据业务编号、类型和子类型查询日志
     * @param bizNo 业务编号
     * @param type 日志类型
     * @param subType 日志子类型
     * @return 日志记录列表
     */
    List<LogRecord> findByBizNoAndTypeAndSubType(String bizNo, String type, String subType);

    /**
     * 根据租户查询日志
     * @param tenant 租户标识
     * @return 日志记录列表
     */
    List<LogRecord> findByTenant(String tenant);

    /**
     * 根据操作人查询日志
     * @param operator 操作人
     * @return 日志记录列表
     */
    List<LogRecord> findByOperator(String operator);
}
