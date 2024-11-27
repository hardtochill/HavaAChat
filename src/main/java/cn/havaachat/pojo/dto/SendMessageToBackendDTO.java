package cn.havaachat.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 消息发送DTO，前端发给后端
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageToBackendDTO {
    /**
     * 联系人Id
     */
    @NotEmpty
    private String contactId;
    /**
     * 消息类型
     */
    @NotNull
    private Integer messageType;
    /**
     * 消息内容
     */
    @NotEmpty
    @Size(max = 500)
    private String messageContent;
    /**
     * 文件大小
     */
    private Long fileSize;
    /**
     * 文件名
     */
    private String fileName;
    /**
     * 文件类型
     */
    private Integer fileType;

    @Override
    public String toString() {
        return "SendMessageToBackendDTO{" +
                "contactId='" + contactId + '\'' +
                ", messageType=" + messageType +
                ", fileSize=" + fileSize +
                ", fileName='" + fileName + '\'' +
                ", fileType=" + fileType +
                '}';
    }
}
