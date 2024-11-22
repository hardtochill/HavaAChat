package cn.havaachat.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SaveGroupDTO {
    /**
     * 群组id
     */
    private String groupId;
    /**
     * 群组名称
     */
    @NotEmpty
    private String groupName;
    /**
     * 群组公告
     */
    private String groupNotice;
    /**
     * 加入类型
     */
    @NotNull
    private Integer joinType;
    /**
     * 群头像文件
     */
    private MultipartFile avatarFile;
    /**
     * 群头像文件缩略图
     */
    private MultipartFile avatarCover;
}
