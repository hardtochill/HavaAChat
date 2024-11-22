package cn.havaachat.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class BaseEntity {
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
