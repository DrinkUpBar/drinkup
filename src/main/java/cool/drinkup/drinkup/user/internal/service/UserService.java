package cool.drinkup.drinkup.user.internal.service;

import cool.drinkup.drinkup.user.internal.controller.req.LoginRequest;
import cool.drinkup.drinkup.user.internal.mapper.UserMapper;
import cool.drinkup.drinkup.user.internal.model.OAuthTypeEnum;
import cool.drinkup.drinkup.user.internal.model.RoleEnum;
import cool.drinkup.drinkup.user.internal.model.User;
import cool.drinkup.drinkup.user.internal.repository.UserRepository;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * @deprecated 使用 UserOAuthService.findUserByOAuth 代替
     */
    @Deprecated
    @Transactional(readOnly = true)
    public Optional<User> findByOauthId(String oauthId, OAuthTypeEnum oauthType) {
        return userRepository.findByOauthIdAndOauthType(oauthId, oauthType);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public User registerUser(LoginRequest loginRequest) {
        User user = userMapper.toUser(loginRequest);
        user.setUsername(randomUserName());
        user.setNickname("品鉴师000000");
        user.setPassword(generateRandomPassword());
        // 设置默认角色（如果没有提供）
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            Set<String> roles = new HashSet<>();
            roles.add(RoleEnum.USER.getRole());
            user.setRoles(roles);
        }

        // 保存用户并获取ID
        user = userRepository.save(user);

        // 使用ID更新昵称并再次保存
        user.setNickname("品鉴师" + String.format("%06d", user.getId()));
        return userRepository.save(user);
    }

    private String randomUserName() {
        // 如果用户名为空，生成一个16位随机字母作为用户名
        StringBuilder randomUsername = new StringBuilder();
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 16; i++) {
            randomUsername.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return randomUsername.toString();
    }

    public String generateRandomPassword() {
        // 生成一个10位随机密码，包含大小写字母、数字和特殊字符
        StringBuilder password = new StringBuilder();
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+";
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 10; i++) {
            password.append(characters.charAt(random.nextInt(characters.length())));
        }
        String rawPassword = password.toString();
        return passwordEncoder.encode(rawPassword);
    }

    @Transactional
    public void addRoleToUser(String username, String roleName) {
        User user =
                userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("用户不存在: " + username));

        Set<String> roles = user.getRoles();
        if (roles == null) {
            roles = new HashSet<>();
            user.setRoles(roles);
        }

        roles.add(roleName);
        userRepository.save(user);
    }

    @Transactional
    public void removeRoleFromUser(String username, String roleName) {
        User user =
                userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("用户不存在: " + username));

        Set<String> roles = user.getRoles();
        if (roles != null) {
            roles.remove(roleName);
            userRepository.save(user);
        }
    }

    @Transactional(readOnly = true)
    public boolean hasRole(String username, String roleName) {
        return userRepository
                .findByUsername(username)
                .map(user -> user.getRoles() != null && user.getRoles().contains(roleName))
                .orElse(false);
    }

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }
}
