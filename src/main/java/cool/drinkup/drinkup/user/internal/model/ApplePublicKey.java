package cool.drinkup.drinkup.user.internal.model;

import java.util.List;
import lombok.Data;

/**
 * Apple 公钥响应模型
 */
@Data
public class ApplePublicKey {

    /**
     * 公钥列表
     */
    private List<Key> keys;

    /**
     * 单个公钥信息
     */
    @Data
    public static class Key {
        /**
         * 密钥类型
         */
        private String kty;

        /**
         * 密钥ID
         */
        private String kid;

        /**
         * 用途
         */
        private String use;

        /**
         * 算法
         */
        private String alg;

        /**
         * RSA 模数
         */
        private String n;

        /**
         * RSA 指数
         */
        private String e;
    }
}
