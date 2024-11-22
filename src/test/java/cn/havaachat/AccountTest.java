package cn.havaachat;

import cn.havaachat.pojo.dto.LoginDTO;
import cn.havaachat.redis.RedisUtils;
import cn.havaachat.utils.StringUtils;
import com.alibaba.fastjson.JSON;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest(classes = HavaAChatApplication.class)
@Transactional
@AutoConfigureMockMvc
public class AccountTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private RedisUtils redisUtils;

    /**
     * 入参校验测试
     */
    @Test
    public void testLoginWithWrongParam() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        String checkCodeKey = "11";
        String checkCodeAnswer = "11";
        loginDTO.setCheckCodeKey(checkCodeKey);
        loginDTO.setCheckCode(checkCodeAnswer);
        loginDTO.setEmail("1235689");
        loginDTO.setPassword(null);
        String loginDTOJson = JSON.toJSONString(loginDTO);
        Mockito.when(redisUtils.get(StringUtils.getRedisCheckcodeKey(checkCodeKey))).thenReturn(loginDTO.getCheckCode());
        ResultActions resultActions = mockMvc.perform(post("/account/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginDTOJson));
        System.out.println(resultActions.andReturn().getResponse().getContentAsString());
    }

}
