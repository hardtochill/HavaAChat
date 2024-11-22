package cn.havaachat.pojo.vo;

import cn.havaachat.utils.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 版本更新VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppUpdateVO {
    /**
     * 自增id
     */
    private Integer id;
    /**
     * 版本号
     */
    private String version;
    /**
     * 更新描述
     */
    private List<String> updateList;
    /**
     * 文件大小
     */
    private Long size;
    /**
     * 文件名
     */
    private String fileName;
    /**
     * 更新文件类型：0：本地文件，1：外链
     * 外链：将更新文件发到第三方平台上
     */
    private Integer fileType;
    /**
     * 外链地址
     */
    private String outerLink;
}
