package cool.drinkup.drinkup.workflow.internal.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mzt.logapi.starter.annotation.LogRecord;

import cool.drinkup.drinkup.common.log.event.AIChatEvent;
import cool.drinkup.drinkup.shared.spi.CommonResp;
import cool.drinkup.drinkup.workflow.internal.controller.resp.ImageUploadResp;
import cool.drinkup.drinkup.workflow.internal.service.image.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Tag(name = "图片管理", description = "图片上传和获取API")
public class ImageController {
    
    private final ImageService imageService;
    
    @LogRecord(
        type = AIChatEvent.AI_CHAT,
        subType = AIChatEvent.BehaviorEvent.IMAGE_UPLOAD,
        bizNo = "{{#_ret.body.data.imageId}}",
        success = "用户上传图片成功，图片大小：{{#image.size}}字节"
    )
    @Operation(
        summary = "上传图片",
        description = "上传图片并返回图片ID，可以通过该ID获取图片"
    )
    @ApiResponse(responseCode = "200", description = "成功上传图片")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommonResp<ImageUploadResp>> uploadImage(
        @Parameter(description = "要上传的图片文件") 
        @RequestPart("image") MultipartFile image
    ) {
        try {
            String imageId = imageService.storeImage(image);
            
            ImageUploadResp response = new ImageUploadResp();
            response.setImageId(imageId);
            
            return ResponseEntity.ok(CommonResp.success(response));
        } catch (Exception e) {
            log.error("Failed to upload image", e);
            return ResponseEntity.ok(CommonResp.error("上传图片失败: " + e.getMessage()));
        }
    }

    
    
    @Operation(
        summary = "获取图片",
        description = "通过图片ID获取图片"
    )
    @ApiResponse(responseCode = "200", description = "成功获取图片")
    @GetMapping("/{imageId}")
    public ResponseEntity<Resource> getImage(
        @Parameter(description = "图片ID") 
        @PathVariable String imageId
    ) {
        try {
            Resource imageResource = imageService.loadImage(imageId);
            
            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG) // Assuming JPEG format, can be enhanced to detect actual type
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + imageId + "\"")
                .body(imageResource);
        } catch (Exception e) {
            log.error("Failed to retrieve image with ID: {}", imageId, e);
            return ResponseEntity.notFound().build();
        }
    }
}
