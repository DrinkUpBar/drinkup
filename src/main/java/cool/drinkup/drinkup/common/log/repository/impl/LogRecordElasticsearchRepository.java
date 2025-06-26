package cool.drinkup.drinkup.common.log.repository.impl;

import java.time.ZonedDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * 日志记录ES Repository
 * 提供对log_record索引的Elasticsearch操作
 */
@Repository
public interface LogRecordElasticsearchRepository extends ElasticsearchRepository<LogRecord, String> {

    /**
     * 根据业务编号和类型查询日志
     * @param bizNo 业务编号
     * @param type 日志类型
     * @return 日志记录列表
     */
    List<LogRecord> findByBizNoAndTypeOrderByCreateTimeDesc(String bizNo, String type);

    /**
     * 根据业务编号、类型和子类型查询日志
     * @param bizNo 业务编号
     * @param type 日志类型
     * @param subType 日志子类型
     * @return 日志记录列表
     */
    List<LogRecord> findByBizNoAndTypeAndSubTypeOrderByCreateTimeDesc(String bizNo, String type, String subType);

    /**
     * 根据租户查询日志
     * @param tenant 租户标识
     * @return 日志记录列表
     */
    List<LogRecord> findByTenantOrderByCreateTimeDesc(String tenant);

    /**
     * 根据操作人查询日志
     * @param operator 操作人
     * @return 日志记录列表
     */
    List<LogRecord> findByOperatorOrderByCreateTimeDesc(String operator);

    /**
     * 根据时间范围查询日志
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日志记录列表
     */
    List<LogRecord> findByCreateTimeBetweenOrderByCreateTimeDesc(ZonedDateTime startTime, ZonedDateTime endTime);

    /**
     * 根据业务编号查询日志（分页）
     * @param bizNo 业务编号
     * @param pageable 分页参数
     * @return 日志记录分页结果
     */
    Page<LogRecord> findByBizNoOrderByCreateTimeDesc(String bizNo, Pageable pageable);

    /**
     * 根据操作失败状态查询日志
     * @param fail 是否失败
     * @return 日志记录列表
     */
    List<LogRecord> findByFailOrderByCreateTimeDesc(Boolean fail);

    /**
     * 统计某个业务编号的日志数量
     * @param bizNo 业务编号
     * @return 日志数量
     */
    long countByBizNo(String bizNo);

    /**
     * 根据类型查询日志（分页）
     * @param type 日志类型
     * @param pageable 分页参数
     * @return 日志记录分页结果
     */
    Page<LogRecord> findByTypeOrderByCreateTimeDesc(String type, Pageable pageable);

    /**
     * 根据操作人和时间范围查询日志
     * @param operator 操作人
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日志记录列表
     */
    List<LogRecord> findByOperatorAndCreateTimeBetweenOrderByCreateTimeDesc(
            String operator, ZonedDateTime startTime, ZonedDateTime endTime);
}
