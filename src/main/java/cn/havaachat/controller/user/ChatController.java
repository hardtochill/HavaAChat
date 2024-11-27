package cn.havaachat.controller.user;

import cn.havaachat.annotation.GlobalInterceptor;
import cn.havaachat.pojo.dto.SendMessageToBackendDTO;
import cn.havaachat.pojo.dto.SendMessageToFrontDTO;
import cn.havaachat.pojo.dto.UploadFileDTO;
import cn.havaachat.pojo.vo.ResponseVO;
import cn.havaachat.service.ChatService;
import cn.havaachat.utils.ResponseUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 聊天
 */
@RestController
@RequestMapping("/chat")
public class ChatController {
    private ChatService chatService;
    public ChatController(ChatService chatService){
        this.chatService = chatService;
    }

    /**
     * 发送消息
     * @param sendMessageToBackendDTO
     * @return
     */
    @PostMapping("/sendMessage")
    @GlobalInterceptor
    public ResponseVO<SendMessageToFrontDTO> sendMessage(@Valid SendMessageToBackendDTO sendMessageToBackendDTO){
        SendMessageToFrontDTO sendMessageToFrontDTO = chatService.sendMessage(sendMessageToBackendDTO);
        return ResponseUtils.success(sendMessageToFrontDTO);
    }
    @PostMapping("/uploadFile")
    @GlobalInterceptor
    public ResponseVO uploadFile(@Valid UploadFileDTO uploadFileDTO){
        chatService.uploadFile(uploadFileDTO);
        return ResponseUtils.success();
    }
}
