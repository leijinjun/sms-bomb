package com.lei2j.sms.bomb;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * @author leijinjun
 * @date 2021/1/6
 **/
@SpringBootTest
@AutoConfigureMockMvc
public class AppTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void test1() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/smsBomb/send").param("phone", "13962653261").accept(MediaType.APPLICATION_JSON))
                .andExpect((mvcResult) -> {
                    assert mvcResult.getResponse().getStatus() == 200;
                });
    }
}
