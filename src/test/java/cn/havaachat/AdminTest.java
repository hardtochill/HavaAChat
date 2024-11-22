package cn.havaachat;

import cn.havaachat.mapper.UserContactMapper;
import cn.havaachat.pojo.dto.TokenUserInfoDTO;
import cn.havaachat.redis.RedisUtils;
import cn.havaachat.utils.StringUtils;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest(classes = HavaAChatApplication.class)
@Transactional
@AutoConfigureMockMvc
public class AdminTest {
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
        tokenUserInfoDTO.setAdmin(true);
        String token = "too";
        Mockito.when(redisUtils.get(StringUtils.getRedisTokenUserInfoKey(token))).thenReturn(tokenUserInfoDTO);
        ResultActions resultActions = mockMvc.perform(post("/admin/loadUser")
                .header("token",token)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                );
        System.out.println(resultActions.andReturn().getResponse().getContentAsString(Charsets.UTF_8));
    }
}
