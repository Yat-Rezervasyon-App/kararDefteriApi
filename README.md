# KararDefteri API

Projeleri, toplantı notlarını, kararları, öncelikli görevleri ve özet sayılarını
yönetmek için geliştirilecek Spring Boot
backend projesi. Mevcut başlangıç iskeleti Java 21 ve Maven kullanır; iş modülleri
ve PostgreSQL entegrasyonu stajyer görevleriyle eklenecektir.

## Yerelde çalıştırma

Java 21 kurulu olmalıdır. Maven Wrapper gerekli Maven sürümünü ilk çalışmada indirir.

```sh
./mvnw spring-boot:run
```

## Derleme ve test

```sh
./mvnw --batch-mode --no-transfer-progress verify
```

Windows'ta `./mvnw` yerine `.\mvnw.cmd` kullan.

## Ekip ve inceleme düzeni

| Geliştirici | Sorumluluk | Ön inceleme |
|---|---|---|
| Çağrı @ccagrimiracozlu | Kullanıcı, giriş ve projeler | Buse @buse321 |
| Buse @buse321 | Toplantı, not ve kararlar | Kardelen @kardelensimsek |
| Kardelen @kardelensimsek | Görevler, filtreler ve özet API’leri | Çağrı @ccagrimiracozlu |

Mentor: **@ahmettkuurt**. Her iş ayrı branch'te geliştirilir, main'e PR açılır.
Bir stajyerin ön incelemesinden sonra mentor son incelemeyi yapar.

- [Branch ve PR çalışma düzeni](CONTRIBUTING.md)

CI, main'e açılan PR'larda ve main push'larında Java 21 ile Maven verify çalıştırır.
Kontrol adı `build-and-test` olarak sabittir. GitHub branch koruması dosya eklemekle
etkinleşmez; kurulum adımlarındaki ruleset içe aktarma işlemi de tamamlanmalıdır.
