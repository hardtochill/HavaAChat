package cn.havaachat.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 版本更新 修改或新增DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppUpdateDTO {
    /**
     * 自增id
     */
    private Integer id;
    /**
     * 版本号
     */
    @NotEmpty
    private String version;
    /**
     * 更新描述
     */
    @NotEmpty
    private String updateDesc;
    /**
     * 更新文件类型：0：本地文件，1：外链
     * 外链：将更新文件发到第三方平台上
     */
    @NotNull
    private Integer fileType;
    /**
     * 外链地址
     */
    private String outerLink;
    /**
     * 版本文件
     */
    MultipartFile file;
}
