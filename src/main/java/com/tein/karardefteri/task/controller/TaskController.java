
package com.tein.karardefteri.task.controller;

import com.tein.karardefteri.task.dto.TaskRequestDto;
import com.tein.karardefteri.task.dto.TaskResponseDto;
import com.tein.karardefteri.task.dto.TaskStatusUpdateRequestDto;
import com.tein.karardefteri.task.service.TaskService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@Tag(
        name = "Görev Yönetimi",
        description = "Görev oluşturma, listeleme, filtreleme, güncelleme ve durum değiştirme işlemleri"
)
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // Görev oluşturma.
    @Operation(
            summary = "Yeni görev oluştur",
            description = """
                    Belirtilen toplantıya yeni bir görev ekler.
                    Başlık zorunludur ve en fazla 200 karakter olabilir.
                    Atanan kullanıcı ve son tarih isteğe bağlıdır.
                    Durum belirtilmezse TODO, öncelik belirtilmezse MEDIUM olarak kaydedilir.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Görev başarıyla oluşturuldu."),
            @ApiResponse(responseCode = "400", description = "Görev bilgileri geçersiz."),
            @ApiResponse(responseCode = "401", description = "Kullanıcı giriş yapmamış."),
            @ApiResponse(responseCode = "404", description = "Toplantı bulunamadı."),
            @ApiResponse(responseCode = "409", description = "Toplantı arşivlenmiş veya atanan kullanıcı aktif değil.")
    })
    @PostMapping("/api/meetings/{meetingId}/tasks")
    public ResponseEntity<TaskResponseDto> createTask(
            @Parameter(
                    description = "Görevin ekleneceği toplantının UUID değeri",
                    required = true
            )
            @PathVariable UUID meetingId,

            @Valid @RequestBody TaskRequestDto requestDto,
            HttpSession session) {

        UUID userId = getUserId(session);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        TaskResponseDto createdTask =
                taskService.createTask(meetingId, requestDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createdTask);
    }

    // Toplantıya ait görevleri listeleme.
    @Operation(
            summary = "Toplantı görevlerini listele",
            description = "Belirtilen toplantıya bağlı tüm görevleri listeler. Görevlerin durum, öncelik ve atama bilgilerini döndürür."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Toplantı görevleri başarıyla listelendi."),
            @ApiResponse(responseCode = "401", description = "Kullanıcı giriş yapmamış.")
    })
    @GetMapping("/api/meetings/{meetingId}/tasks")
    public ResponseEntity<List<TaskResponseDto>> getTasksByMeetingId(
            @Parameter(
                    description = "Görevleri listelenecek toplantının UUID değeri",
                    required = true
            )
            @PathVariable UUID meetingId,
            HttpSession session) {

        if (getUserId(session) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(
                taskService.getTasksByMeetingId(meetingId)
        );
    }

    // Giriş yapan kullanıcının görevlerini filtreleyerek ve sayfalayarak getirir.
    @Operation(
            summary = "Kendi görevlerimi listele",
            description = """
                    Giriş yapan kullanıcının kendisine atanmış görevlerini listeler.
                    Proje, durum, öncelik ve gecikme durumuna göre filtreleme yapılabilir.
                    overdue=true gecikmiş görevleri, overdue=false gecikmemiş ve tamamlanmış görevleri getirir.
                    Son tarihi olmayan görevler de overdue=false sonucuna dahildir.
                    Sonuçlar sayfalı olarak döndürülür.
                    Varsayılan sayfa 0, sayfa boyutu 20'dir.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Kullanıcının görevleri başarıyla listelendi."),
            @ApiResponse(responseCode = "400", description = "Filtre veya sayfalama parametreleri geçersiz."),
            @ApiResponse(responseCode = "401", description = "Kullanıcı giriş yapmamış.")
    })
    @GetMapping("/api/users/me/tasks")
    public ResponseEntity<Page<TaskResponseDto>> getMyTasks(
            @Parameter(description = "Projeye göre filtreleme için proje UUID değeri")
            @RequestParam(required = false) UUID projectId,

            @Parameter(description = "Görev durumu: TODO, IN_PROGRESS veya DONE")
            @RequestParam(required = false) String status,

            @Parameter(description = "Görev önceliği: LOW, MEDIUM veya HIGH")
            @RequestParam(required = false) String priority,

            @Parameter(description = "Gecikme filtresi: true gecikmiş görevleri; false gecikmemiş ve tamamlanmış görevleri getirir. Son tarihi olmayan görevler de false sonucuna dahildir.")
            @RequestParam(required = false) Boolean overdue,

            @Parameter(
                    description = "Sayfa numarası. Minimum 0, varsayılan 0.",
                    schema = @Schema(type = "integer", minimum = "0", example = "0")
            )
            @RequestParam(defaultValue = "0") int page,

            @Parameter(
                    description = "Sayfa başına kayıt sayısı. 1 ile 100 arasında olmalıdır; varsayılan 20.",
                    schema = @Schema(type = "integer", minimum = "1", maximum = "100", example = "20")
            )
            @RequestParam(defaultValue = "20") int size,

            HttpSession session) {

        UUID userId = getUserId(session);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(
                taskService.getMyTasks(
                        userId,
                        projectId,
                        status,
                        priority,
                        overdue,
                        page,
                        size
                )
        );
    }

    // Tüm görevleri filtreleyerek ve sayfalayarak getirir.
    @Operation(
            summary = "Görevleri filtreleyerek listele",
            description = """
                    Sistemdeki görevleri çeşitli kriterlere göre listeler.
                    Proje, atanan kullanıcı, durum, öncelik ve son tarih aralığına göre filtreleme yapılabilir.
                    overdue=true gecikmiş görevleri, overdue=false gecikmemiş ve tamamlanmış görevleri getirir.
                    Son tarihi olmayan görevler de overdue=false sonucuna dahildir.
                    Sonuçlar sayfalı olarak döndürülür.
                    Varsayılan sayfa 0, sayfa boyutu 20'dir.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Görevler başarıyla listelendi."),
            @ApiResponse(responseCode = "400", description = "Filtre veya sayfalama parametreleri geçersiz."),
            @ApiResponse(responseCode = "401", description = "Kullanıcı giriş yapmamış.")
    })
    @GetMapping("/api/tasks")
    public ResponseEntity<Page<TaskResponseDto>> getTasks(
            @Parameter(description = "Projeye göre filtreleme için proje UUID değeri")
            @RequestParam(required = false) UUID projectId,

            @Parameter(description = "Atanan kullanıcıya göre filtreleme için kullanıcı UUID değeri")
            @RequestParam(required = false) UUID assigneeId,

            @Parameter(description = "Görev durumu: TODO, IN_PROGRESS veya DONE")
            @RequestParam(required = false) String status,

            @Parameter(description = "Görev önceliği: LOW, MEDIUM veya HIGH")
            @RequestParam(required = false) String priority,

            @Parameter(
                    description = "Filtrelenecek son tarih aralığının başlangıcı (ISO 8601).",
                    example = "2026-11-01T00:00:00+03:00"
            )
            @RequestParam(required = false) OffsetDateTime dueFrom,

            @Parameter(
                    description = "Filtrelenecek son tarih aralığının bitişi (ISO 8601).",
                    example = "2026-11-30T23:59:59+03:00"
            )
            @RequestParam(required = false) OffsetDateTime dueTo,

            @Parameter(description = "Gecikme filtresi: true gecikmiş görevleri; false gecikmemiş ve tamamlanmış görevleri getirir. Son tarihi olmayan görevler de false sonucuna dahildir.")
            @RequestParam(required = false) Boolean overdue,

            @Parameter(
                    description = "Sayfa numarası. Minimum 0, varsayılan 0.",
                    schema = @Schema(type = "integer", minimum = "0", example = "0")
            )
            @RequestParam(defaultValue = "0") int page,

            @Parameter(
                    description = "Sayfa başına kayıt sayısı. 1 ile 100 arasında olmalıdır; varsayılan 20.",
                    schema = @Schema(type = "integer", minimum = "1", maximum = "100", example = "20")
            )
            @RequestParam(defaultValue = "20") int size,

            HttpSession session) {

        if (getUserId(session) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(
                taskService.getTasks(
                        projectId,
                        assigneeId,
                        status,
                        priority,
                        dueFrom,
                        dueTo,
                        overdue,
                        page,
                        size
                )
        );
    }

    // ID ile görev getirme.
    @Operation(
            summary = "ID ile görev getir",
            description = "Verilen görev ID'sine ait görev bilgilerini getirir. Görev bulunamazsa 404 Not Found hatası döndürür."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Görev başarıyla getirildi."),
            @ApiResponse(responseCode = "401", description = "Kullanıcı giriş yapmamış."),
            @ApiResponse(responseCode = "404", description = "Görev bulunamadı.")
    })
    @GetMapping("/api/tasks/{id}")
    public ResponseEntity<TaskResponseDto> getTaskById(
            @Parameter(description = "Getirilecek görevin sayısal ID değeri", required = true)
            @PathVariable Long id,
            HttpSession session) {

        if (getUserId(session) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    // Görev içeriğini ve atamasını güncelleme.
    @Operation(
            summary = "Görev bilgilerini güncelle",
            description = """
                    Mevcut görevin başlık, açıklama, atanan kullanıcı,
                    son tarih ve öncelik bilgilerini günceller.
                    İşlemi toplantı sahibi veya ADMIN yapabilir.
                    Görev durumu bu endpoint üzerinden değiştirilmez.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Görev bilgileri başarıyla güncellendi."),
            @ApiResponse(responseCode = "400", description = "Gönderilen görev bilgileri geçersiz."),
            @ApiResponse(responseCode = "401", description = "Kullanıcı giriş yapmamış."),
            @ApiResponse(responseCode = "403", description = "Kullanıcının görevi güncelleme yetkisi yok."),
            @ApiResponse(responseCode = "404", description = "Görev bulunamadı."),
            @ApiResponse(responseCode = "409", description = "Toplantı veya proje arşivlenmiş ya da atanan kullanıcı aktif değil.")
    })
    @PutMapping("/api/tasks/{id}")
    public ResponseEntity<TaskResponseDto> updateTask(
            @Parameter(description = "Güncellenecek görevin sayısal ID değeri", required = true)
            @PathVariable Long id,

            @Valid @RequestBody TaskRequestDto requestDto,
            HttpSession session) {

        UUID userId = getUserId(session);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean isAdmin =
                "ADMIN".equals(session.getAttribute("role"));

        TaskResponseDto updatedTask = taskService.updateTask(
                id,
                requestDto,
                userId,
                isAdmin
        );

        return ResponseEntity.ok(updatedTask);
    }

    // Görev durumunu güncelleme.
    @Operation(
            summary = "Görev durumunu güncelle",
            description = """
                    Görevin durumunu günceller.
                    Bu işlemi yalnızca göreve atanmış kullanıcı yapabilir.
                    Geçerli durumlar TODO, IN_PROGRESS ve DONE değerleridir.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Görev durumu başarıyla güncellendi."),
            @ApiResponse(responseCode = "400", description = "Görev durumu geçersiz veya eksik."),
            @ApiResponse(responseCode = "401", description = "Kullanıcı giriş yapmamış."),
            @ApiResponse(responseCode = "403", description = "Kullanıcının görev durumunu değiştirme yetkisi yok."),
            @ApiResponse(responseCode = "404", description = "Görev bulunamadı."),
            @ApiResponse(responseCode = "409", description = "Toplantı veya proje arşivlenmiş.")
    })
    @PatchMapping("/api/tasks/{id}/status")
    public ResponseEntity<TaskResponseDto> updateTaskStatus(
            @Parameter(
                    description = "Durumu değiştirilecek görevin sayısal ID değeri",
                    required = true
            )
            @PathVariable Long id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Görev durumunu güncelleme isteği.",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TaskStatusUpdateRequestDto.class),
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    name = "Durum güncelleme",
                                    value = "{\"status\":\"IN_PROGRESS\"}"
                            )
                    )
            )
            @Valid @RequestBody TaskStatusUpdateRequestDto request,

            HttpSession session) {

        UUID userId = getUserId(session);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String status = request == null
                ? null
                : request.status();

        TaskResponseDto updatedTask = taskService.updateTaskStatus(
                id,
                status,
                userId
        );

        return ResponseEntity.ok(updatedTask);
    }

    // Session üzerinden kullanıcı ID'sini alır.
    private UUID getUserId(HttpSession session) {

        Object userId = session.getAttribute("userId");

        if (userId instanceof UUID) {
            return (UUID) userId;
        }

        if (userId instanceof String) {
            try {
                return UUID.fromString((String) userId);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        return null;
    }
}
