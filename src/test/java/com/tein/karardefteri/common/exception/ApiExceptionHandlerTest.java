package com.tein.karardefteri.common.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ApiExceptionHandlerTest {
    MockMvc mvc;
    @BeforeEach void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new FailureController())
                .setControllerAdvice(new ApiExceptionHandler()).build();
    }
    @Test void mapsInvalidRequestTo400() throws Exception {
        mvc.perform(get("/failure/validation")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_NAME"));
    }
    @Test void mapsBusinessConflictTo409() throws Exception {
        mvc.perform(get("/failure/conflict")).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PROJECT_ARCHIVED"));
    }
    @Test void mapsMissingResourceTo404() throws Exception {
        mvc.perform(get("/failure/missing")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROJECT_NOT_FOUND"));
    }
    @Test void includesValidationFields() throws Exception {
        mvc.perform(post("/failure/body").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.name").exists());
    }
    @Test void malformedJsonUsesCommonErrorFormat() throws Exception {
        mvc.perform(post("/failure/body").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("HTTP_400"));
    }
    @Test void unexpectedErrorsDoNotExposeInternalMessage() throws Exception {
        mvc.perform(get("/failure/unexpected")).andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(content().string(not(containsString("private-database-password"))));
    }
    @RestController
    static class FailureController {
        @GetMapping("/failure/validation") void validation() {
            throw new InvalidRequestException("INVALID_NAME", "Geçersiz ad");
        }
        @GetMapping("/failure/conflict") void conflict() {
            throw new BusinessRuleViolationException("PROJECT_ARCHIVED", "Proje arşivli");
        }
        @GetMapping("/failure/missing") void missing() {
            throw new ResourceNotFoundException("PROJECT_NOT_FOUND", "Proje bulunamadı");
        }
        @GetMapping("/failure/unexpected") void unexpected() {
            throw new IllegalStateException("private-database-password");
        }
        @PostMapping("/failure/body") void body(@Valid @RequestBody Body body) { }
    }
    record Body(@NotBlank String name) { }
}
