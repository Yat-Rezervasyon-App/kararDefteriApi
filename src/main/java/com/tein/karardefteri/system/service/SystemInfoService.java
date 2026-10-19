package com.tein.karardefteri.system.service;

import com.tein.karardefteri.system.dto.SystemInfoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SystemInfoService {
    private final SystemInfoResponse info;

    public SystemInfoService(@Value("${spring.application.name}") String name,
                             @Value("${app.version}") String version) {
        this.info = new SystemInfoResponse(name, version);
    }

    public SystemInfoResponse getInfo() {
        return info;
    }
}
