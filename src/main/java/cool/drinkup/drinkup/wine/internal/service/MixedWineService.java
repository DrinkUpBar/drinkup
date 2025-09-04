package cool.drinkup.drinkup.wine.internal.service;

import cool.drinkup.drinkup.wine.internal.controller.req.RandomWineTypeEnum;
import cool.drinkup.drinkup.wine.internal.controller.resp.RandomWineResp;
import cool.drinkup.drinkup.wine.internal.controller.resp.WorkflowUserWineVo;
import cool.drinkup.drinkup.wine.internal.controller.resp.WorkflowWineVo;
import cool.drinkup.drinkup.wine.internal.mapper.UserWineMapper;
import cool.drinkup.drinkup.wine.internal.mapper.WineMapper;
import cool.drinkup.drinkup.wine.internal.model.UserWine;
import cool.drinkup.drinkup.wine.internal.model.Wine;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MixedWineService {

    private final WineService wineService;
    private final UserWineService userWineService;
    private final WineMapper wineMapper;
    private final UserWineMapper userWineMapper;

    private RandomWineResp getRandomMixedWine(int count) {
        // 先随机分配每种类型需要的数量
        int ibaCount = 0;
        int userCount = 0;

        for (int i = 0; i < count; i++) {
            if (Math.random() < 0.5) {
                ibaCount++;
            } else {
                userCount++;
            }
        }

        List<RandomWineResp.RandomWineContent> mixedWines = new ArrayList<>();

        // 批量获取IBA酒
        if (ibaCount > 0) {
            List<Wine> ibaWines = wineService.getRandomWines(ibaCount);
            for (Wine wine : ibaWines) {
                WorkflowWineVo wineVo = wineMapper.toWineVo(wine);
                mixedWines.add(RandomWineResp.RandomWineContent.builder()
                        .type(RandomWineTypeEnum.IBA.name())
                        .wine(wineVo)
                        .build());
            }

            // 如果IBA酒数量不够，从用户酒库补充
            int missingIbaCount = ibaCount - ibaWines.size();
            if (missingIbaCount > 0) {
                List<UserWine> supplementUserWines = userWineService.getRandomUserWines(missingIbaCount);
                for (UserWine userWine : supplementUserWines) {
                    WorkflowUserWineVo userWineVo = userWineMapper.toWorkflowUserWineVo(userWine);
                    mixedWines.add(RandomWineResp.RandomWineContent.builder()
                            .type(RandomWineTypeEnum.USER.name())
                            .wine(userWineVo)
                            .build());
                }
            }
        }

        // 批量获取用户酒
        if (userCount > 0) {
            List<UserWine> userWines = userWineService.getRandomUserWines(userCount);
            for (UserWine userWine : userWines) {
                WorkflowUserWineVo userWineVo = userWineMapper.toWorkflowUserWineVo(userWine);
                mixedWines.add(RandomWineResp.RandomWineContent.builder()
                        .type(RandomWineTypeEnum.USER.name())
                        .wine(userWineVo)
                        .build());
            }

            // 如果用户酒数量不够，从IBA酒库补充
            int missingUserCount = userCount - userWines.size();
            if (missingUserCount > 0) {
                List<Wine> supplementIbaWines = wineService.getRandomWines(missingUserCount);
                for (Wine wine : supplementIbaWines) {
                    WorkflowWineVo wineVo = wineMapper.toWineVo(wine);
                    mixedWines.add(RandomWineResp.RandomWineContent.builder()
                            .type(RandomWineTypeEnum.IBA.name())
                            .wine(wineVo)
                            .build());
                }
            }
        }

        if (mixedWines.isEmpty()) {
            return RandomWineResp.builder().build();
        }

        // 随机打乱结果顺序，保持混合效果
        Collections.shuffle(mixedWines);

        return RandomWineResp.builder().wines(mixedWines).build();
    }

    private RandomWineResp getRandomUserWines(int count) {
        List<UserWine> randomUserWines = userWineService.getRandomUserWines(count);
        if (randomUserWines.isEmpty()) {
            return RandomWineResp.builder().build();
        }

        List<RandomWineResp.RandomWineContent> userWineContents = randomUserWines.stream()
                .map(userWine -> RandomWineResp.RandomWineContent.builder()
                        .type(RandomWineTypeEnum.USER.name())
                        .wine(userWineMapper.toWorkflowUserWineVo(userWine))
                        .build())
                .collect(Collectors.toList());

        return RandomWineResp.builder().wines(userWineContents).build();
    }

    private RandomWineResp getRandomWines(int count) {
        List<Wine> randomWines = wineService.getRandomWines(count);
        if (randomWines.isEmpty()) {
            return RandomWineResp.builder().build();
        }

        List<RandomWineResp.RandomWineContent> wineContents = randomWines.stream()
                .map(wine -> RandomWineResp.RandomWineContent.builder()
                        .type(RandomWineTypeEnum.IBA.name())
                        .wine(wineMapper.toWineVo(wine))
                        .build())
                .collect(Collectors.toList());

        return RandomWineResp.builder().wines(wineContents).build();
    }

    public RandomWineResp getRandomWine(String type, int count) {
        switch (RandomWineTypeEnum.valueOf(type.toUpperCase(Locale.ROOT))) {
            case MIXED:
                return getRandomMixedWine(count);
            case USER:
                return getRandomUserWines(count);
            case IBA:
                return getRandomWines(count);
            default:
                return RandomWineResp.builder().build();
        }
    }
}
