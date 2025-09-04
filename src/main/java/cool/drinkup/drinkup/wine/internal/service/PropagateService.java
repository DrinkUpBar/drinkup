package cool.drinkup.drinkup.wine.internal.service;

import cool.drinkup.drinkup.wine.internal.controller.req.PropagateRequest;
import cool.drinkup.drinkup.wine.internal.enums.PropagateTypeEnum;
import cool.drinkup.drinkup.wine.internal.model.ShareMapping;
import cool.drinkup.drinkup.wine.internal.model.UserWine;
import cool.drinkup.drinkup.wine.internal.model.Wine;
import cool.drinkup.drinkup.wine.internal.repository.ShareMappingRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PropagateService {
    private final ShareMappingRepository shareMappingRepository;
    private final WineService wineService;
    private final UserWineService userWineService;

    public String makeSharedId(PropagateRequest request) {
        if (request.getType() == PropagateTypeEnum.WINE) {
            Wine wine = wineService.getWineById(request.getRecordId());
            if (wine == null) {
                throw new RuntimeException("酒不存在，ID: " + request.getRecordId());
            }
        } else if (request.getType() == PropagateTypeEnum.USER_WINE) {
            UserWine userWine = userWineService.getUserWineById(request.getRecordId());
            if (userWine == null) {
                throw new RuntimeException("用户酒不存在，ID: " + request.getRecordId());
            }
        }
        String sharedId = UUID.randomUUID().toString().replace("-", "");
        ShareMapping shareMapping = new ShareMapping();
        shareMapping.setSharedId(sharedId);
        shareMapping.setType(request.getType());
        shareMapping.setRecordId(request.getRecordId());
        shareMapping.setUserId(request.getUserId());
        shareMappingRepository.save(shareMapping);
        return sharedId;
    }

    public Object getSharedInfoBySharedId(String sharedId) {
        ShareMapping shareMapping = shareMappingRepository.findBySharedId(sharedId);
        if (shareMapping == null) {
            throw new RuntimeException("酒卡分享ID不存在，ID: " + sharedId);
        }
        if (shareMapping.getType() == PropagateTypeEnum.WINE) {
            Wine wine = wineService.getWineById(shareMapping.getRecordId());
            if (wine == null) {
                throw new RuntimeException("酒不存在，ID: " + shareMapping.getRecordId());
            }
            return wineService.toWineVo(wine);
        } else if (shareMapping.getType() == PropagateTypeEnum.USER_WINE) {
            UserWine userWine = userWineService.getUserWineById(shareMapping.getRecordId());
            if (userWine == null) {
                throw new RuntimeException("用户酒不存在，ID: " + shareMapping.getRecordId());
            }
            return userWineService.toWorkflowUserWineVo(userWine);
        }
        throw new RuntimeException("酒卡分享ID不存在，ID: " + sharedId);
    }
}
