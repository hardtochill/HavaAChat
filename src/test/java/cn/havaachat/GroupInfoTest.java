package cn.havaachat;

import cn.havaachat.constants.AutoFillConstants;
import cn.havaachat.enums.UserContactStatusEnum;
import cn.havaachat.mapper.UserContactMapper;
import cn.havaachat.pojo.dto.LoginDTO;
import cn.havaachat.pojo.dto.SaveGroupDTO;
import cn.havaachat.pojo.entity.GroupInfo;
import cn.havaachat.pojo.entity.UserContact;
import cn.havaachat.redis.RedisUtils;
import cn.havaachat.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.security.acl.Group;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest(classes = HavaAChatApplication.class)
@Transactional
@AutoConfigureMockMvc
public class GroupInfoTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private RedisUtils redisUtils;
    @Autowired
    private UserContactMapper userContactMapper;

    /**
     * 入参校验测试
     */
    @Test
    public void testSaveGroup() throws Exception {
        ResultActions resultActions = mockMvc.perform(post("/group/saveGroup")
                .contentType(MediaType.APPLICATION_JSON));
        System.out.println(resultActions.andReturn().getResponse().getContentAsString());
    }
    @Test
    public void testSQL(){
        List<UserContact> userContactList = userContactMapper.findBatchWithContactNameAndSexByContactIdAndStatus("G10500646517", UserContactStatusEnum.FRIEND.getStatus());
        System.out.println(userContactList);
    }
}
