package cool.drinkup.drinkup.infrastructure.internal.image.impl;

// 使用简化的字节操作实现，不依赖metadata-extractor的写入功能
import cool.drinkup.drinkup.infrastructure.spi.image.ImageMetadataProcessor;
import cool.drinkup.drinkup.infrastructure.spi.image.dto.ImageMetadata;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.CRC32;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 默认图片元数据处理器实现
 * 使用metadata-extractor库为图片添加EXIF和XMP元数据
 */
@Slf4j
@Component
public class DefaultImageMetadataProcessor implements ImageMetadataProcessor {

    @Override
    public byte[] addMetadata(byte[] imageBytes, ImageMetadata metadata) throws IOException {
        try {
            // 检测图片格式
            String format = detectImageFormat(imageBytes);

            if ("JPEG".equals(format)) {
                return addMetadataToJpeg(imageBytes, metadata);
            } else if ("PNG".equals(format)) {
                return addMetadataToPng(imageBytes, metadata);
            } else {
                // 对于其他格式，目前只是记录日志并返回原图
                log.warn("不支持为{}格式的图片添加元数据，返回原图", format);
                return imageBytes;
            }
        } catch (Exception e) {
            log.error("添加图片元数据时发生错误", e);
            // 如果添加元数据失败，返回原图
            return imageBytes;
        }
    }

    @Override
    public String addMetadataToBase64(String imageBase64, ImageMetadata metadata) throws IOException {
        byte[] imageBytes = Base64.getDecoder().decode(imageBase64);
        byte[] processedBytes = addMetadata(imageBytes, metadata);
        return Base64.getEncoder().encodeToString(processedBytes);
    }

    /**
     * 为JPEG图片添加元数据
     * 使用简化的字节操作实现
     */
    private byte[] addMetadataToJpeg(byte[] imageBytes, ImageMetadata metadata) throws IOException {
        try {
            // 创建XMP数据
            String xmpData = createXmpData(metadata);

            // 使用简单的字节操作添加元数据
            return addMetadataToJpegBytes(imageBytes, metadata, xmpData);

        } catch (Exception e) {
            log.error("处理JPEG元数据时发生错误", e);
            return imageBytes;
        }
    }

    /**
     * 为PNG图片添加元数据
     * PNG通过tEXt chunks存储文本信息
     */
    private byte[] addMetadataToPng(byte[] imageBytes, ImageMetadata metadata) throws IOException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // PNG文件签名 (8字节): 89 50 4E 47 0D 0A 1A 0A
            outputStream.write(imageBytes, 0, 8);

            int offset = 8;
            boolean insertedMetadata = false;

            // 读取并处理PNG chunks
            while (offset < imageBytes.length) {
                // 读取chunk长度 (4字节，大端序)
                if (offset + 8 > imageBytes.length) break;

                int chunkLength = ((imageBytes[offset] & 0xFF) << 24)
                        | ((imageBytes[offset + 1] & 0xFF) << 16)
                        | ((imageBytes[offset + 2] & 0xFF) << 8)
                        | (imageBytes[offset + 3] & 0xFF);

                // 读取chunk类型 (4字节)
                String chunkType = new String(imageBytes, offset + 4, 4, StandardCharsets.US_ASCII);

                // 在IHDR chunk之后插入元数据chunks
                if ("IHDR".equals(chunkType) && !insertedMetadata) {
                    // 先写入IHDR chunk
                    int ihdrTotalLength = 4 + 4 + chunkLength + 4; // length + type + data + crc
                    outputStream.write(imageBytes, offset, ihdrTotalLength);
                    offset += ihdrTotalLength;

                    // 插入元数据chunks
                    insertMetadataChunks(outputStream, metadata);
                    insertedMetadata = true;
                } else {
                    // 复制其他chunks
                    int totalChunkLength = 4 + 4 + chunkLength + 4; // length + type + data + crc
                    if (offset + totalChunkLength <= imageBytes.length) {
                        outputStream.write(imageBytes, offset, totalChunkLength);
                        offset += totalChunkLength;
                    } else {
                        // 如果数据不完整，复制剩余部分
                        outputStream.write(imageBytes, offset, imageBytes.length - offset);
                        break;
                    }
                }

                // 如果到达IEND chunk，结束处理
                if ("IEND".equals(chunkType)) {
                    break;
                }
            }

            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("处理PNG元数据时发生错误", e);
            return imageBytes;
        }
    }

    /**
     * 插入PNG元数据chunks
     */
    private void insertMetadataChunks(ByteArrayOutputStream outputStream, ImageMetadata metadata) throws IOException {
        // 添加各种元数据作为tEXt chunks
        if (metadata.getSoftware() != null) {
            writeTextChunk(outputStream, "Software", metadata.getSoftware());
        }
        if (metadata.getCreatorTool() != null) {
            writeTextChunk(outputStream, "CreatorTool", metadata.getCreatorTool());
        }
        if (metadata.getArtist() != null) {
            writeTextChunk(outputStream, "Artist", metadata.getArtist());
        }
        if (metadata.getDescription() != null) {
            writeTextChunk(outputStream, "Description", metadata.getDescription());
        }
        if (metadata.getKeywords() != null) {
            writeTextChunk(outputStream, "Keywords", metadata.getKeywords());
        }
        if (metadata.getAiGenerated() != null && metadata.getAiGenerated()) {
            writeTextChunk(outputStream, "AI-Generated", "True");
        }
        if (metadata.getAiModel() != null) {
            writeTextChunk(outputStream, "AI-Model", metadata.getAiModel());
        }
        if (metadata.getAiVersion() != null) {
            writeTextChunk(outputStream, "AI-Version", metadata.getAiVersion());
        }
        if (metadata.getAiProvider() != null) {
            writeTextChunk(outputStream, "AI-Provider", metadata.getAiProvider());
        }
        if (metadata.getKaiHeMood() != null) {
            writeTextChunk(outputStream, "KaiHe-Mood", metadata.getKaiHeMood());
        }
        if (metadata.getKaiHeFlavor() != null) {
            writeTextChunk(outputStream, "KaiHe-Flavor", metadata.getKaiHeFlavor());
        }
    }

    /**
     * 写入PNG tEXt chunk
     */
    private void writeTextChunk(ByteArrayOutputStream outputStream, String keyword, String text) throws IOException {
        // 构建chunk数据: keyword + null separator + text
        ByteArrayOutputStream chunkData = new ByteArrayOutputStream();
        chunkData.write(keyword.getBytes(StandardCharsets.US_ASCII));
        chunkData.write(0); // null separator
        chunkData.write(text.getBytes(StandardCharsets.UTF_8));

        byte[] data = chunkData.toByteArray();

        // 写入chunk长度 (大端序)
        writeInt32BigEndian(outputStream, data.length);

        // 写入chunk类型
        outputStream.write("tEXt".getBytes(StandardCharsets.US_ASCII));

        // 写入chunk数据
        outputStream.write(data);

        // 计算并写入CRC
        CRC32 crc = new CRC32();
        crc.update("tEXt".getBytes(StandardCharsets.US_ASCII));
        crc.update(data);
        writeInt32BigEndian(outputStream, (int) crc.getValue());
    }

    /**
     * 写入32位大端序整数
     */
    private void writeInt32BigEndian(ByteArrayOutputStream outputStream, int value) throws IOException {
        outputStream.write((value >> 24) & 0xFF);
        outputStream.write((value >> 16) & 0xFF);
        outputStream.write((value >> 8) & 0xFF);
        outputStream.write(value & 0xFF);
    }

    /**
     * 检测图片格式
     */
    private String detectImageFormat(byte[] imageBytes) {
        if (imageBytes.length < 8) {
            return "UNKNOWN";
        }

        // JPEG: FF D8 FF
        if (imageBytes[0] == (byte) 0xFF && imageBytes[1] == (byte) 0xD8 && imageBytes[2] == (byte) 0xFF) {
            return "JPEG";
        }

        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if (imageBytes[0] == (byte) 0x89 && imageBytes[1] == 0x50 && imageBytes[2] == 0x4E && imageBytes[3] == 0x47) {
            return "PNG";
        }

        return "UNKNOWN";
    }

    /**
     * 创建XMP数据
     */
    private String createXmpData(ImageMetadata metadata) {
        StringBuilder xmp = new StringBuilder();
        xmp.append("<?xpacket begin=\"﻿\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?>\n");
        xmp.append("<x:xmpmeta xmlns:x=\"adobe:ns:meta/\">\n");
        xmp.append("  <rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n");
        xmp.append("    <rdf:Description rdf:about=\"\">\n");

        // 添加基础元数据
        if (metadata.getSoftware() != null) {
            xmp.append("      <Software>")
                    .append(escapeXml(metadata.getSoftware()))
                    .append("</Software>\n");
        }
        if (metadata.getCreatorTool() != null) {
            xmp.append("      <Creator-Tool>")
                    .append(escapeXml(metadata.getCreatorTool()))
                    .append("</Creator-Tool>\n");
        }
        if (metadata.getArtist() != null) {
            xmp.append("      <Artist>").append(escapeXml(metadata.getArtist())).append("</Artist>\n");
        }
        if (metadata.getDescription() != null) {
            xmp.append("      <Description>")
                    .append(escapeXml(metadata.getDescription()))
                    .append("</Description>\n");
        }
        if (metadata.getKeywords() != null) {
            xmp.append("      <Keywords>")
                    .append(escapeXml(metadata.getKeywords()))
                    .append("</Keywords>\n");
        }

        // 添加AI相关元数据
        if (metadata.getAiGenerated() != null && metadata.getAiGenerated()) {
            xmp.append("      <AI-Generated>True</AI-Generated>\n");
        }
        if (metadata.getAiModel() != null) {
            xmp.append("      <AI-Model>")
                    .append(escapeXml(metadata.getAiModel()))
                    .append("</AI-Model>\n");
        }
        if (metadata.getAiVersion() != null) {
            xmp.append("      <AI-Version>")
                    .append(escapeXml(metadata.getAiVersion()))
                    .append("</AI-Version>\n");
        }
        if (metadata.getAiProvider() != null) {
            xmp.append("      <AI-Provider>")
                    .append(escapeXml(metadata.getAiProvider()))
                    .append("</AI-Provider>\n");
        }

        // 添加开喝定制元数据
        if (metadata.getKaiHeMood() != null) {
            xmp.append("      <KaiHe-Mood>")
                    .append(escapeXml(metadata.getKaiHeMood()))
                    .append("</KaiHe-Mood>\n");
        }
        if (metadata.getKaiHeFlavor() != null) {
            xmp.append("      <KaiHe-Flavor>")
                    .append(escapeXml(metadata.getKaiHeFlavor()))
                    .append("</KaiHe-Flavor>\n");
        }

        xmp.append("    </rdf:Description>\n");
        xmp.append("  </rdf:RDF>\n");
        xmp.append("</x:xmpmeta>\n");
        xmp.append("<?xpacket end=\"w\"?>");

        return xmp.toString();
    }

    /**
     * 转义XML特殊字符
     */
    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    /**
     * 向JPEG字节数组添加元数据
     * 这是一个简化的实现，实际生产环境可能需要更复杂的JPEG段处理
     */
    private byte[] addMetadataToJpegBytes(byte[] originalBytes, ImageMetadata metadata, String xmpData) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // JPEG文件开始标记 (FF D8)
            outputStream.write(0xFF);
            outputStream.write(0xD8);

            // 写入自定义APP1段用于存储元数据信息
            writeApp1Segment(outputStream, metadata);

            // 如果有XMP数据，写入XMP段
            if (xmpData != null && !xmpData.isEmpty()) {
                writeXmpSegment(outputStream, xmpData);
            }

            // 跳过原始文件的SOI标记，复制剩余内容
            int startIndex = 2; // 跳过 FF D8

            // 查找第一个APP段之后的位置
            while (startIndex < originalBytes.length - 1) {
                if (originalBytes[startIndex] == (byte) 0xFF) {
                    byte marker = originalBytes[startIndex + 1];
                    if (marker >= (byte) 0xE0 && marker <= (byte) 0xEF) {
                        // 这是一个APP段，跳过它
                        int segmentLength =
                                ((originalBytes[startIndex + 2] & 0xFF) << 8) | (originalBytes[startIndex + 3] & 0xFF);
                        startIndex += 2 + segmentLength;
                    } else {
                        // 不是APP段，从这里开始复制
                        break;
                    }
                } else {
                    startIndex++;
                }
            }

            // 复制剩余的JPEG数据
            outputStream.write(originalBytes, startIndex, originalBytes.length - startIndex);

            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("写入JPEG元数据时发生错误", e);
            return originalBytes;
        }
    }

    /**
     * 写入APP1段
     */
    private void writeApp1Segment(ByteArrayOutputStream outputStream, ImageMetadata metadata) throws IOException {
        ByteArrayOutputStream app1Content = new ByteArrayOutputStream();

        // APP1标识符 "Exif\0\0"
        app1Content.write("Exif\0\0".getBytes());

        // 简化的EXIF数据 - 实际实现可能需要更复杂的TIFF格式处理
        String exifData = String.format(
                "Software: %s; Artist: %s; Description: %s",
                metadata.getSoftware() != null ? metadata.getSoftware() : "",
                metadata.getArtist() != null ? metadata.getArtist() : "",
                metadata.getDescription() != null ? metadata.getDescription() : "");

        app1Content.write(exifData.getBytes("UTF-8"));

        byte[] content = app1Content.toByteArray();

        // 写入APP1段标记
        outputStream.write(0xFF);
        outputStream.write(0xE1);

        // 写入段长度 (大端序)
        int length = content.length + 2;
        outputStream.write((length >> 8) & 0xFF);
        outputStream.write(length & 0xFF);

        // 写入内容
        outputStream.write(content);
    }

    /**
     * 写入XMP段
     */
    private void writeXmpSegment(ByteArrayOutputStream outputStream, String xmpData) throws IOException {
        // XMP数据通常存储在APP1段中，标识符为"http://ns.adobe.com/xap/1.0/\0"
        ByteArrayOutputStream xmpContent = new ByteArrayOutputStream();

        // XMP标识符
        xmpContent.write("http://ns.adobe.com/xap/1.0/\0".getBytes());
        xmpContent.write(xmpData.getBytes("UTF-8"));

        byte[] content = xmpContent.toByteArray();

        // 写入APP1段标记
        outputStream.write(0xFF);
        outputStream.write(0xE1);

        // 写入段长度 (大端序)
        int length = content.length + 2;
        outputStream.write((length >> 8) & 0xFF);
        outputStream.write(length & 0xFF);

        // 写入内容
        outputStream.write(content);
    }
}
