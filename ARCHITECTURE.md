# KararDefteri paket yapısı

Tek Spring Boot uygulaması ve tek Maven modülü kullanılır. Kod, iş alanlarına göre
`com.tein.karardefteri` altında ayrılır.

```text
com.tein.karardefteri
├── user          Çağrı — kullanıcı ve giriş
├── project       Çağrı — projeler
├── meeting       Buse — toplantılar ve notlar
├── decision      Buse — kararlar
├── task          Kardelen — görevler
├── summary       Kardelen — özet sayıları
├── system        Çalışan controller ve service örneği
└── common
    ├── configuration
    ├── dto
    └── exception
```

İş modüllerinin yapısı:

```text
project
├── controller
├── service
├── repository
├── entity
└── dto
```

İhtiyaç olmayan katman için sınıf eklenmez. summary şu an controller, service ve
dto paketlerine sahiptir; kendi tablosu olmadığı için entity ve repository yoktur.
Boş paketler yalnız Git'te tutulabilmeleri için kısa package-info.java içerir.

## Bir özellik geliştirirken

1. Veritabanı modelini entity paketine JPA anotasyonlarıyla yaz.
2. Tabloyu oluşturan Flyway migration'ını ekle; Hibernate ddl-auto=validate kullanır.
3. repository paketinde JpaRepository'yi genişleten interface oluştur.
4. service paketinde @Service sınıfına iş kurallarını ve gerekli @Transactional
   sınırını yaz. Repository'yi constructor üzerinden al.
5. dto paketinde request ve response modellerini tanımla. Request alanlarında
   @NotBlank, @Size gibi doğrulamaları kullan.
6. controller paketinde @RestController sınıfı yaz. Controller yalnız Service'i
   çağırsın; iş kuralı veya repository erişimi içermesin.
7. İlgili iş kuralı, endpoint ve PostgreSQL entegrasyon testlerini ekle.

Akış: Controller → Service → Repository → PostgreSQL.

Her service için interface yazılması gerekmez. Gerekli olduğunda diğer modülün
service'ini çağırabilirsin; başka modülün repository'sini doğrudan kullanma.
Servisler birbirine döngüyle bağlanmamalı. summary diğer modüllerin okuma/sayım
servislerini çağırır; kaynak modüller summary'ye bağımlı olmaz.

Entity'yi doğrudan HTTP cevabı olarak döndürme; DTO kullan. Başlangıçta dönüşümü
elle yapabilirsin; mapper framework'ü veya ek soyutlama gerekli değil.

## Çalışan örnek

GET /api/system/info → SystemInfoController → SystemInfoService → SystemInfoResponse.
Bu işlem yalnız uygulama adı ve sürümünü döndürdüğü için repository kullanmaz.
Uygulama adı ve sürümü Spring ayarlarından constructor üzerinden alınır.

## Ortak hatalar

common/exception/ApiExceptionHandler ortak cevap biçimini üretir:
code, message, fieldErrors. Alan hatası yoksa fieldErrors boş nesnedir.

- HTTP alan doğrulaması veya InvalidRequestException: 400.
- ResourceNotFoundException: 404.
- BusinessRuleViolationException: 409.
- Beklenmeyen hata: 500; iç hata mesajı istemciye gönderilmez.

Giriş ve yetkilendirme geliştirilince Spring Security 401/403 cevaplarını aynı
DTO biçimine bağlayın. common/configuration ortak Spring ayarları içindir.

## Veritabanı ve testler

Migration sırası users → projects → meetings → tasks/decisions olarak koordine
edilir. main'e alınmış migration değiştirilmez; yeni sürüm eklenir.

Normal Maven verify HTTP ve ortak hata testlerini çalıştırır. PostgreSQL açıkken
-Ppostgres-it verify uygulama context'i, JDBC ve Flyway entegrasyonunu da doğrular.
CI, Ubuntu runner üzerinde PostgreSQL servisini doğrudan başlatır ve bu profili çalıştırır.

V1__create_users.sql.example yalnız referanstır; otomatik çalıştırılmaz ve tablo
oluşturmaz. Gerçek migration'lar, kullanıcı entity'si, iş CRUD'ları,
giriş/yetkilendirme ve diğer tablolar stajyerlerin haftalık görevleriyle eklenecektir. Mevcut örnek yalnız sistem bilgisi API'sidir.
