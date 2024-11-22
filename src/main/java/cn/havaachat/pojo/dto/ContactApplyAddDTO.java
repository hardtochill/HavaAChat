package cn.havaachat.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

/**
 * 联系人添加申请DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactApplyAddDTO {
    /**
     * 要添加的联系人id
     */
    @NotEmpty
    private String contactId;
    /**
     * 申请信息
     */
    private String applyInfo;
}
