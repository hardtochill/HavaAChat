package cn.havaachat.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

/**
 * 登录DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginDTO {
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
     * 邮箱
     */
    @NotEmpty(message = "邮箱不能为空")
    @Email(message = "邮箱格式错误")
    private String email;
    @NotEmpty(message = "密码不能为空")
    private String password;
}
