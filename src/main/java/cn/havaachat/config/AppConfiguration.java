package cn.havaachat.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * 基本配置信息
 */
@Component
@Data
public class AppConfiguration {
    /**
     * webSocket请求路径
     */
    @Value("${ws.path}")
    private String wsPath;
    /**
     * webSocket端口
     */
    @Value("${ws.port}")
    private Integer wsPort;
    /**
     * 本地文件目录
     */
    @Value("${project.folder}")
    private String fileFolder;
    /**
     * 超级管理员邮箱组
     */
    @Value("${admin.emails}")
    private String adminEmails;
}
