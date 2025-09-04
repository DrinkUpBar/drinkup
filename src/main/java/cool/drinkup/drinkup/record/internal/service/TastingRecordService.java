package cool.drinkup.drinkup.record.internal.service;

import cool.drinkup.drinkup.record.internal.controller.req.AddTastingRecordRequest;
import cool.drinkup.drinkup.record.internal.controller.req.UpdateTastingRecordRequest;
import cool.drinkup.drinkup.record.internal.model.TastingRecord;
import cool.drinkup.drinkup.record.internal.model.TastingRecordImage;
import cool.drinkup.drinkup.record.internal.repository.TastingRecordImageRepository;
import cool.drinkup.drinkup.record.internal.repository.TastingRecordRepository;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TastingRecordService {

    private final TastingRecordRepository tastingRecordRepository;
    private final TastingRecordImageRepository tastingRecordImageRepository;

    /**
     * 创建品鉴记录
     */
    @Transactional
    public TastingRecord createTastingRecord(Long userId, AddTastingRecordRequest request) {
        log.info(
                "创建品鉴记录: userId={}, beverageId={}, beverageType={}",
                userId,
                request.getBeverageId(),
                request.getBeverageType());

        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

        // 创建品鉴记录
        TastingRecord tastingRecord = new TastingRecord();
        tastingRecord.setUserId(userId);
        tastingRecord.setBeverageId(request.getBeverageId());
        tastingRecord.setBeverageType(request.getBeverageType().name());
        tastingRecord.setContent(request.getContent());
        tastingRecord.setTastingDate(now);
        tastingRecord.setStatus(1);
        tastingRecord.setCreatedTime(now);
        tastingRecord.setUpdatedTime(now);

        TastingRecord savedRecord = tastingRecordRepository.save(tastingRecord);

        // 保存图片
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            List<TastingRecordImage> images = new ArrayList<>();
            for (int i = 0; i < request.getImages().size(); i++) {
                String imageUrl = request.getImages().get(i);
                TastingRecordImage image = new TastingRecordImage();
                image.setRecordId(savedRecord.getId());
                image.setImage(imageUrl);
                image.setIsCover(i == 0 ? 1 : 0); // 第一张图片设为封面
                image.setCreatedTime(now);
                images.add(image);
            }
            tastingRecordImageRepository.saveAll(images);
            savedRecord.setImages(images);
        }

        log.info("品鉴记录创建成功: id={}", savedRecord.getId());
        return savedRecord;
    }

    /**
     * 根据饮品ID和类型查询品鉴记录
     */
    @Transactional(readOnly = true)
    public List<TastingRecord> getTastingRecordsByBeverage(Long beverageId, String beverageType, Long userId) {
        log.info("查询品鉴记录: beverageId={}, beverageType={}", beverageId, beverageType);

        List<TastingRecord> records =
                tastingRecordRepository.findByBeverageIdAndBeverageTypeAndUserIdAndStatusOrderByCreatedTimeDesc(
                        beverageId, beverageType, userId, 1);

        // 为每个记录加载图片
        for (TastingRecord record : records) {
            List<TastingRecordImage> images =
                    tastingRecordImageRepository.findByRecordIdOrderByCreatedTimeAsc(record.getId());
            record.setImages(images);
        }

        log.info("查询到{}条品鉴记录", records.size());
        return records;
    }

    /**
     * 根据用户ID查询品鉴记录
     */
    @Transactional(readOnly = true)
    public List<TastingRecord> getTastingRecordsByUser(Long userId) {
        log.info("查询用户品鉴记录: userId={}", userId);

        List<TastingRecord> records = tastingRecordRepository.findByUserIdAndStatusOrderByCreatedTimeDesc(userId, 1);

        // 为每个记录加载图片
        for (TastingRecord record : records) {
            List<TastingRecordImage> images =
                    tastingRecordImageRepository.findByRecordIdOrderByCreatedTimeAsc(record.getId());
            record.setImages(images);
        }

        log.info("查询到{}条用户品鉴记录", records.size());
        return records;
    }

    /**
     * 更新品鉴记录内容
     */
    @Transactional
    public TastingRecord updateTastingRecord(Long recordId, Long userId, UpdateTastingRecordRequest request) {
        log.info("更新品鉴记录: recordId={}, userId={}", recordId, userId);

        // 查找记录并验证权限
        TastingRecord record = tastingRecordRepository
                .findByIdAndUserIdAndStatus(recordId, userId, 1)
                .orElseThrow(() -> new RuntimeException("品鉴记录不存在或无权限访问"));

        // 只更新内容字段
        record.setContent(request.getContent());
        record.setUpdatedTime(ZonedDateTime.now(ZoneOffset.UTC));

        TastingRecord updatedRecord = tastingRecordRepository.save(record);

        // 加载图片
        List<TastingRecordImage> images =
                tastingRecordImageRepository.findByRecordIdOrderByCreatedTimeAsc(record.getId());
        updatedRecord.setImages(images);

        log.info("品鉴记录更新成功: id={}", updatedRecord.getId());
        return updatedRecord;
    }

    /**
     * 软删除品鉴记录
     */
    @Transactional
    public void deleteTastingRecord(Long recordId, Long userId) {
        log.info("删除品鉴记录: recordId={}, userId={}", recordId, userId);

        // 查找记录并验证权限
        TastingRecord record = tastingRecordRepository
                .findByIdAndUserIdAndStatus(recordId, userId, 1)
                .orElseThrow(() -> new RuntimeException("品鉴记录不存在或无权限访问"));

        // 软删除：将状态设为0
        record.setStatus(0);
        record.setUpdatedTime(ZonedDateTime.now(ZoneOffset.UTC));

        tastingRecordRepository.save(record);

        log.info("品鉴记录删除成功: id={}", record.getId());
    }
}
