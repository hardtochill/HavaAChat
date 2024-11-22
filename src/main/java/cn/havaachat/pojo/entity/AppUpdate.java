package cn.havaachat.pojo.entity;

import cn.havaachat.exception.BaseException;
import cn.havaachat.utils.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 版本更新发布
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppUpdate extends BaseEntity {
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
    private String updateDesc;
    /**
     * 更新方式： 0：未发布，1：灰度发布，2：全网发布
     * 灰度发布：只向部分用户推送版本更新
     */
    private Integer status;
    /**
     * 灰度发布uid
     */
    private String grayscaleUid;
    /**
     * 更新文件类型：0：本地文件，1：外链
     * 外链：将更新文件发到第三方平台上
     */
    private Integer fileType;
    /**
     * 外链地址
     */
    private String outerLink;
    /**
     * 更新描述数组
     * 前端传更新描述是：描述1|描述2|描述3|...
     * 后端存入数据库就原样存，但是返回给前端展示时要将“|”切成String[]
     */
    private String[] updateDescArray;

    public String[] getUpdateDescArray() {
        if(!StringUtils.isEmpty(updateDesc)){
            return updateDesc.split("\\|");
        }
        return updateDescArray;
    }

    public void setUpdateDescArray(String[] updateDescArray) {
        this.updateDescArray = updateDescArray;
    }
}
