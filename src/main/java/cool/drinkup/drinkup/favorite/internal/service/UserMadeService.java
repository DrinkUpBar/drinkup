package cool.drinkup.drinkup.favorite.internal.service;

import cool.drinkup.drinkup.favorite.internal.controller.req.CheckFavoriteItemRequest;
import cool.drinkup.drinkup.favorite.internal.dto.UserMadeDTO;
import cool.drinkup.drinkup.favorite.internal.entity.UserMade;
import cool.drinkup.drinkup.favorite.internal.repository.UserMadeRepository;
import cool.drinkup.drinkup.favorite.spi.MadeObjectLoader;
import cool.drinkup.drinkup.favorite.spi.ObjectType;
import cool.drinkup.drinkup.shared.dto.UserWine;
import cool.drinkup.drinkup.shared.dto.Wine;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class UserMadeService {

    private final UserMadeRepository madeRepository;
    private final Map<ObjectType, MadeObjectLoader<?>> loaderMap;

    public UserMadeService(UserMadeRepository madeRepository, List<MadeObjectLoader<?>> loaders) {
        this.madeRepository = madeRepository;
        this.loaderMap = new HashMap<>();
        for (MadeObjectLoader<?> loader : loaders) {
            this.loaderMap.put(loader.getMadeType(), loader);
        }
    }

    // 添加已调制
    @Transactional
    public UserMade addMade(Long userId, ObjectType objectType, Long objectId) {
        // 1. 验证对象是否存在
        MadeObjectLoader<?> loader = loaderMap.get(objectType);
        if (loader == null || !loader.validateObject(objectId)) {
            throw new RuntimeException("调制对象不存在");
        }

        // 2. 检查是否已标记为调制
        if (madeRepository.existsByUserIdAndObjectTypeAndObjectId(userId, objectType, objectId)) {
            throw new RuntimeException("已经标记为调制过了");
        }

        // 3. 创建已调制记录
        UserMade made = new UserMade();
        made.setUserId(userId);
        made.setObjectType(objectType);
        made.setObjectId(objectId);
        made.setMadeTime(ZonedDateTime.now(ZoneOffset.UTC));

        UserMade saved = madeRepository.save(made);

        // 4. 触发后续操作
        loader.afterMade(objectId, true);

        return saved;
    }

    // 取消已调制标记
    @Transactional
    public void removeMade(Long userId, ObjectType objectType, Long objectId) {
        madeRepository.deleteByUserIdAndObjectTypeAndObjectId(userId, objectType, objectId);

        MadeObjectLoader<?> loader = loaderMap.get(objectType);
        if (loader != null) {
            loader.afterMade(objectId, false);
        }
    }

    // 获取用户已调制列表（带详情）
    public Page<UserMadeDTO> getUserMadesWithDetails(Long userId, ObjectType objectType, Pageable pageable) {
        // 1. 查询已调制记录
        Page<UserMade> madePage = (objectType == null)
                ? madeRepository.findByUserIdOrderByMadeTimeDesc(userId, pageable)
                : madeRepository.findByUserIdAndObjectTypeOrderByMadeTimeDesc(userId, objectType, pageable);

        // 2. 按类型分组
        Map<ObjectType, List<UserMade>> groupedMades =
                madePage.getContent().stream().collect(Collectors.groupingBy(UserMade::getObjectType));

        // 3. 批量加载关联对象
        List<UserMadeDTO> dtoList = new ArrayList<>();
        for (Map.Entry<ObjectType, List<UserMade>> entry : groupedMades.entrySet()) {
            ObjectType type = entry.getKey();
            List<UserMade> mades = entry.getValue();
            List<Long> objectIds = mades.stream().map(UserMade::getObjectId).collect(Collectors.toList());

            MadeObjectLoader<?> loader = loaderMap.get(type);
            if (loader != null) {
                Map<Long, ?> objectMap = loader.loadObjects(objectIds);

                // 4. 组装DTO
                for (UserMade made : mades) {
                    Object obj = objectMap.get(made.getObjectId());
                    if (obj != null) {
                        dtoList.add(convertToDTO(made, obj));
                    }
                }
            }
        }

        // 5. 按原始顺序排序
        List<Long> originalIds =
                madePage.getContent().stream().map(UserMade::getId).collect(Collectors.toList());

        dtoList.sort(Comparator.comparingInt(dto -> originalIds.indexOf(dto.getId())));

        return new PageImpl<>(dtoList, pageable, madePage.getTotalElements());
    }

    // 检查是否已调制
    public boolean isMade(Long userId, ObjectType objectType, Long objectId) {
        return madeRepository.existsByUserIdAndObjectTypeAndObjectId(userId, objectType, objectId);
    }

    // 批量检查已调制状态
    public Map<Long, Boolean> checkMadeStatus(Long userId, ObjectType objectType, List<Long> objectIds) {
        List<UserMade> mades = madeRepository.findByUserIdAndObjectTypeAndObjectIdIn(userId, objectType, objectIds);
        Set<Long> madeIds = mades.stream().map(UserMade::getObjectId).collect(Collectors.toSet());

        return objectIds.stream().collect(Collectors.toMap(Function.identity(), madeIds::contains));
    }

    // 批量检查多种类型的已调制状态
    public Map<ObjectType, Map<Long, Boolean>> checkMadeStatusMulti(
            Long userId, List<CheckFavoriteItemRequest> requests) {
        // 1. 按类型分组
        Map<ObjectType, List<Long>> typeToIds = requests.stream()
                .collect(Collectors.groupingBy(
                        CheckFavoriteItemRequest::getObjectType,
                        Collectors.mapping(CheckFavoriteItemRequest::getObjectId, Collectors.toList())));

        // 2. 对每种类型批量查询
        Map<ObjectType, Map<Long, Boolean>> result = new HashMap<>();
        for (Map.Entry<ObjectType, List<Long>> entry : typeToIds.entrySet()) {
            ObjectType type = entry.getKey();
            List<Long> objectIds = entry.getValue();
            result.put(type, checkMadeStatus(userId, type, objectIds));
        }

        return result;
    }

    // 转换为DTO
    private UserMadeDTO convertToDTO(UserMade made, Object object) {
        UserMadeDTO dto = new UserMadeDTO();
        dto.setId(made.getId());
        dto.setObjectType(made.getObjectType());
        dto.setObjectId(made.getObjectId());
        dto.setMadeTime(made.getMadeTime());

        // 根据类型设置不同的字段
        if (object instanceof Wine) {
            Wine wine = (Wine) object;
            dto.setObjectDetail(wine);
        } else if (object instanceof UserWine) {
            UserWine userWine = (UserWine) object;
            dto.setObjectDetail(userWine);
        } else {
            // 处理其他类型
            dto.setObjectDetail(object);
        }

        return dto;
    }
}
