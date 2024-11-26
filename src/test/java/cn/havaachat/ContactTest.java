package cn.havaachat;

import cn.havaachat.mapper.UserContactMapper;
import cn.havaachat.pojo.dto.TokenUserInfoDTO;
import cn.havaachat.redis.RedisUtils;
import cn.havaachat.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import org.apache.commons.codec.Charsets;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest(classes = HavaAChatApplication.class)
@Transactional
@AutoConfigureMockMvc
public class ContactTest {
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
    public void testSearch() throws Exception {
        TokenUserInfoDTO tokenUserInfoDTO = new TokenUserInfoDTO();
        tokenUserInfoDTO.setUserId("U61368327818");
        String token = "too";
        String contactId = "G10500646517";
        Map<String,String> jsonMap = new HashMap<>();
        jsonMap.put("contactId","G10500646517");
        System.out.println(JSON.toJSONString(jsonMap));
        Mockito.when(redisUtils.get(StringUtils.getRedisTokenUserInfoDTOKeyByToken(token))).thenReturn(tokenUserInfoDTO);
        ResultActions resultActions = mockMvc.perform(post("/contact/search")
                .header("token",token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(jsonMap)));
        System.out.println(resultActions.andReturn().getResponse().getContentAsString(Charsets.UTF_8));
    }

}
