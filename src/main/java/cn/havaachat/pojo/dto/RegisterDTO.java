package cn.havaachat.pojo.dto;

import cn.havaachat.constants.AccountConstants;
import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * 注册DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDTO {
    /**
     * 验证码唯一标识
     */
    @NotEmpty(message = "验证码错误")
    private String checkCodeKey;
    /**
     * 验证码
     */
    @NotEmpty(message = "验证码不能为空")
    private String checkCode;
    /**
     * 昵称
     */
    @NotEmpty(message = "名称不能为空")
    private String nickName;
    /**
     * 邮箱
     */
    @NotEmpty(message = "邮箱不能为空")
    @Email(message = "邮箱格式错误")
    private String email;
    /**
     * 密码
     */
    @NotEmpty(message = "密码不能为空")
    @Pattern(regexp = AccountConstants.REGEX_PASSWORD)
    private String password;

}
