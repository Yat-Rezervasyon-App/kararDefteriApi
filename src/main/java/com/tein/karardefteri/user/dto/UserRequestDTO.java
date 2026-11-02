package com.tein.karardefteri.user.dto;

import com.tein.karardefteri.user.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRequestDTO (
    @NotBlank(message = "Ad soyad boş olamaz")
    @Size(max =120 , message = "Ad soyad en fazla 120 karakter olabilir")
    String fullName,

    @NotBlank(message = "E-posta boş olamaz")
    @Email(message = "Geçerli bir e-potsa adresi girilmelidir")
    @Size(max = 254 , message = "E-posta en fazla 254 karakter olabilir")
    String email,

    @NotBlank(message = "Parola boş olamaz")
    @Size(min = 8, max = 72 , message = "Parola 8-72 karakter arasında olmalıdır")
    String password,

    @NotNull(message ="Kullanıcı rolü belirtmelidir")
    UserRole role
){
}
