
package com.tein.karardefteri.project.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile("dev")
public class DevelopmentProjectCreator {

    private static final UUID DEVELOPMENT_USER_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000001");

    public UUID getUserId() {
        return DEVELOPMENT_USER_ID;
    }
}
