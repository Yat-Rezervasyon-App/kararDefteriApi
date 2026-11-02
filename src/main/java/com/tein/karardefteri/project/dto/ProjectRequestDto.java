
package com.tein.karardefteri.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectRequestDto(

        @NotBlank(message = "Proje adı boş olamaz")
        @Size(max = 200, message = "Proje adı en fazla 200 karakter olabilir")
        String name,

        String description

) {
}
