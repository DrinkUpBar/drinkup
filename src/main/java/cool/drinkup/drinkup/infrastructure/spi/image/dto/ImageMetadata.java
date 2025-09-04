package cool.drinkup.drinkup.infrastructure.spi.image.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 图片元数据信息
 * 用于封装需要添加到图片中的EXIF和XMP元数据
 */
@Data
@Builder
public class ImageMetadata {

    /**
     * 软件信息
     */
    private String software;

    /**
     * 创建工具
     */
    private String creatorTool;

    /**
     * 艺术家/创作者
     */
    private String artist;

    /**
     * 描述信息
     */
    private String description;

    /**
     * 关键词
     */
    private String keywords;

    /**
     * 是否为AI生成
     */
    private Boolean aiGenerated;

    /**
     * AI模型名称
     */
    private String aiModel;

    /**
     * AI版本
     */
    private String aiVersion;

    /**
     * AI提供商
     */
    private String aiProvider;

    /**
     * 凯合心情
     */
    private String kaiHeMood;

    /**
     * 凯合口味
     */
    private String kaiHeFlavor;

    /**
     * 创建默认的DrinkUp应用元数据
     * @param mood 心情
     * @param flavor 口味
     * @param description 描述
     * @param keywords 关键词
     * @return ImageMetadata
     */
    public static ImageMetadata createDrinkUpMetadata(String mood, String flavor, String description, String keywords) {
        return ImageMetadata.builder()
                .software("KaiHe DrinkUp App v1.0")
                .creatorTool("Qwen (Alibaba Cloud) + KaiHe Mixology Engine")
                .artist("DrinkUp AI Bartender")
                .description(description)
                .keywords(keywords)
                .aiGenerated(true)
                .aiModel("Qwen-Max (Alibaba Cloud)")
                .aiVersion("2.2")
                .aiProvider("Alibaba Cloud")
                .kaiHeMood(mood)
                .kaiHeFlavor(flavor)
                .build();
    }
}
