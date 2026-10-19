package com.tein.karardefteri.system.controller;

import com.tein.karardefteri.system.dto.SystemInfoResponse;
import com.tein.karardefteri.system.service.SystemInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SystemInfoController.class)
class SystemInfoControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean SystemInfoService service;

    @Test void returnsSystemInfoAsJson() throws Exception {
        given(service.getInfo()).willReturn(new SystemInfoResponse("kararDefteriApi", "1.0.0"));
        mvc.perform(get("/api/system/info")).andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("kararDefteriApi"))
                .andExpect(jsonPath("$.version").value("1.0.0"));
    }
}
