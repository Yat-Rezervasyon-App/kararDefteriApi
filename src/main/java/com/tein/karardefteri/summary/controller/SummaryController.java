package com.tein.karardefteri.summary.controller;
import com.tein.karardefteri.summary.dto.ProjectSummaryResponseDto;
import com.tein.karardefteri.summary.dto.UserSummaryResponseDto;
import com.tein.karardefteri.summary.service.SummaryService;
import com.tein.karardefteri.user.dto.UserResponseDTO;
import com.tein.karardefteri.user.entity.UserRole;
import com.tein.karardefteri.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@Tag(
        name = "Özet Yönetimi",
        description = "Proje ve kullanıcı görev özetlerini sunan API uç noktaları."
)
public class SummaryController {

    private final SummaryService summaryService;
    private final UserService userService;

    public SummaryController(
            SummaryService summaryService,
            UserService userService
    ) {
        this.summaryService = summaryService;
        this.userService = userService;
    }

    @Operation(
            summary = "Proje özetini getir",
            description = "Projeye ait toplantı sayısını, aktif karar sayısını, açık görev sayısını ve gecikmiş görev sayısını getirir. Yalnızca proje sahibi veya ADMIN erişebilir."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Proje özeti başarıyla getirildi."),
            @ApiResponse(responseCode = "401", description = "Kullanıcı giriş yapmamış."),
            @ApiResponse(responseCode = "403", description = "Kullanıcının bu projeye erişim yetkisi yok."),
            @ApiResponse(responseCode = "404", description = "Proje bulunamadı.")
    })
    @GetMapping("/projects/{id}/summary")
    public ProjectSummaryResponseDto getProjectSummary(
            @Parameter(
                    description = "Özeti getirilecek projenin UUID değeri.",
                    example = "550e8400-e29b-41d4-a716-446655440000",
                    required = true
            )
            @PathVariable UUID id,
            Authentication authentication
    ) {
        UserResponseDTO currentUser =
                userService.getCurrentUser(authentication.getName());

        boolean isAdmin = currentUser.role() == UserRole.ADMIN;

        return summaryService.getProjectSummary(
                id,
                currentUser.id(),
                isAdmin
        );
    }

    @Operation(
            summary = "Giriş yapan kullanıcının görev özetini getir",
            description = "Giriş yapan kullanıcının açık ve gecikmiş görev sayılarını getirir. Açık görevler TODO ve IN_PROGRESS durumundaki görevlerdir. DONE durumundaki görevler açık görev sayısına dahil edilmez."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Kullanıcı görev özeti başarıyla getirildi."),
            @ApiResponse(responseCode = "401", description = "Kullanıcı giriş yapmamış.")
    })
    @GetMapping("/users/me/summary")
    public UserSummaryResponseDto getUserSummary(
            Authentication authentication
    ) {
        UserResponseDTO currentUser =
                userService.getCurrentUser(authentication.getName());

        return summaryService.getUserSummary(currentUser.id());
    }
}
