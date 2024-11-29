package cn.havaachat.service;

import cn.havaachat.pojo.dto.DownloadFileDTO;
import cn.havaachat.pojo.dto.SendMessageToBackendDTO;
import cn.havaachat.pojo.dto.SendMessageToFrontDTO;
import cn.havaachat.pojo.dto.UploadFileDTO;

/**
 * 聊天
 */
public interface ChatService {
    /**
     * 发送消息
     * @param sendMessageToBackendDTO
     */
     SendMessageToFrontDTO sendMessage(SendMessageToBackendDTO sendMessageToBackendDTO);

    /**
     * 上传文件
     * @param uploadFileDTO
     */
    void uploadFile(UploadFileDTO uploadFileDTO);

    /**
     * 下载文件
     * @param downloadFileDTO
     */
    void downloadFile(DownloadFileDTO downloadFileDTO);
}
