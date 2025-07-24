package cool.drinkup.drinkup.wine.internal.service;

import cool.drinkup.drinkup.shared.dto.WorkflowBartenderChatDto;
import cool.drinkup.drinkup.user.spi.AuthenticatedUserDTO;
import cool.drinkup.drinkup.user.spi.AuthenticationServiceFacade;
import cool.drinkup.drinkup.wine.internal.controller.req.UpdateCardImageRequest;
import cool.drinkup.drinkup.wine.internal.controller.resp.WorkflowUserWineVo;
import cool.drinkup.drinkup.wine.internal.mapper.UserWineMapper;
import cool.drinkup.drinkup.wine.internal.model.UserWine;
import cool.drinkup.drinkup.wine.internal.repository.UserWineRepository;
import cool.drinkup.drinkup.wine.spi.UserWineServiceFacade;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserWineService implements UserWineServiceFacade {
    private final UserWineRepository userWineRepository;
    private final UserWineMapper userWineMapper;
    private final AuthenticationServiceFacade authenticationServiceFacade;

    @Override
    @Transactional
    public UserWine saveUserWine(WorkflowBartenderChatDto chatBotResponse) {
        Optional<AuthenticatedUserDTO> currentAuthenticatedUser =
                authenticationServiceFacade.getCurrentAuthenticatedUser();
        if (currentAuthenticatedUser.isEmpty()) {
            throw new IllegalStateException("Expected authenticated user but got none");
        }
        AuthenticatedUserDTO authenticatedUserDTO = currentAuthenticatedUser.get();
        Long userId = authenticatedUserDTO.userId();
        var userWine = userWineMapper.toUserWine(chatBotResponse, userId);
        return userWineRepository.save(userWine);
    }

    public Page<UserWine> getUserWine(PageRequest pageRequest) {
        Optional<AuthenticatedUserDTO> currentAuthenticatedUser =
                authenticationServiceFacade.getCurrentAuthenticatedUser();
        if (currentAuthenticatedUser.isEmpty()) {
            throw new IllegalStateException("Expected authenticated user but got none");
        }
        AuthenticatedUserDTO authenticatedUserDTO = currentAuthenticatedUser.get();
        Long userId = authenticatedUserDTO.userId();
        return userWineRepository.findByUserId(userId, pageRequest);
    }

    public @Nullable UserWine getRandomUserWine() {
        Optional<AuthenticatedUserDTO> currentAuthenticatedUser =
                authenticationServiceFacade.getCurrentAuthenticatedUser();
        if (currentAuthenticatedUser.isEmpty()) {
            throw new IllegalStateException("Expected authenticated user but got none");
        }
        AuthenticatedUserDTO authenticatedUserDTO = currentAuthenticatedUser.get();
        Long userId = authenticatedUserDTO.userId();
        return userWineRepository.findRandomUserWine(userId);
    }

    public List<UserWine> getRandomUserWines(int count) {
        Optional<AuthenticatedUserDTO> currentAuthenticatedUser =
                authenticationServiceFacade.getCurrentAuthenticatedUser();
        if (currentAuthenticatedUser.isEmpty()) {
            throw new IllegalStateException("Expected authenticated user but got none");
        }
        AuthenticatedUserDTO authenticatedUserDTO = currentAuthenticatedUser.get();
        Long userId = authenticatedUserDTO.userId();
        return userWineRepository.findRandomUserWines(userId, count);
    }

    public WorkflowUserWineVo toWorkflowUserWineVo(UserWine userWine) {
        return userWineMapper.toWorkflowUserWineVo(userWine);
    }

    @Transactional
    public UserWine updateUserWine(Long userWineId, UpdateCardImageRequest request) {
        Optional<AuthenticatedUserDTO> currentAuthenticatedUser =
                authenticationServiceFacade.getCurrentAuthenticatedUser();
        if (currentAuthenticatedUser.isEmpty()) {
            throw new IllegalStateException("Expected authenticated user but got none");
        }
        AuthenticatedUserDTO authenticatedUserDTO = currentAuthenticatedUser.get();
        Long userId = authenticatedUserDTO.userId();

        UserWine userWine = userWineRepository
                .findById(userWineId)
                .orElseThrow(() -> new RuntimeException("用户酒不存在，ID: " + userWineId));

        // 验证当前用户是否有权限修改这个用户酒
        if (!userWine.getUserId().equals(userId)) {
            throw new RuntimeException("无权限修改此用户酒");
        }

        if (request.getCardImage() != null) {
            userWine.setCardImage(request.getCardImage());
        }
        if (request.getTheme() != null) {
            userWine.setTheme(request.getTheme());
        }
        if (request.getCardStyle() != null) {
            userWine.setCardStyle(request.getCardStyle());
        }
        return userWineRepository.save(userWine);
    }

    @Cacheable(value = "userWine", key = "#userWineId", unless = "#result == null", cacheManager = "cacheManager")
    public @Nullable UserWine getUserWineById(Long userWineId) {
        return userWineRepository.findById(userWineId).orElse(null);
    }

    public void save(UserWine userWine) {
        userWineRepository.save(userWine);
    }
}
