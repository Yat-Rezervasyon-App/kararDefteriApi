package com.tein.karardefteri.system.controller;

import com.tein.karardefteri.system.dto.SystemInfoResponse;
import com.tein.karardefteri.system.service.SystemInfoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system")
public class SystemInfoController {
    private final SystemInfoService service;

    public SystemInfoController(SystemInfoService service) {
        this.service = service;
    }

    @GetMapping("/info")
    public SystemInfoResponse getInfo() {
        return service.getInfo();
    }
}
