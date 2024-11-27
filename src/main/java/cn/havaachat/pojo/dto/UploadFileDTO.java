package cn.havaachat.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

/**
 * 文件上传
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadFileDTO {
    /**
     * 消息id
     */
    @NotNull
    private Long messageId;
    /**
     * 文件
     */
    @NotNull
    private MultipartFile file;
    /**
     * 文件缩略
     */
    @NotNull
    private MultipartFile cover;
}
