package cn.havaachat.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 下载文件接口
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DownloadFileDTO {
    /**
     * 文件id
     */
    @NotEmpty
    private String fileId;
    /**
     * 是否展示缩略图
     */
    @NotNull
    private Boolean showCover;
}
