# KararDefteri API

Proje bazlı toplantı, karar ve görev takibi için modül bazlı katmanlı
başlangıç iskeleti. Java 21, Spring Boot 4.1.1, Spring MVC, JPA, PostgreSQL ve
Flyway kullanılır. İş modüllerinin CRUD ve giriş kuralları stajyerlerce eklenecek;
çalışan referans akış `system` modülündedir.

## Yerelde çalıştırma

Java 21 ve çalışan bir PostgreSQL 16 veya daha yeni sürüm gereklidir. Maven Wrapper
Maven'ı ilk çalışmada indirir. PostgreSQL doğrudan makinede veya erişilebilir bir
sunucuda çalışır.

İlk kurulumda PostgreSQL yönetici hesabıyla bağlanıp veritabanlarını oluştur:

```sql
CREATE USER karardefteri WITH PASSWORD 'karardefteri_local_only';
CREATE DATABASE karardefteri OWNER karardefteri;
CREATE DATABASE karardefteri_test OWNER karardefteri;
```

Proje kökünde yerel ayarları kopyala ve uygulamayı başlat:

```sh
cp .env.example .env
./mvnw spring-boot:run
```

`.env` yerel ayarlardır ve Git'e dahil edilmez. Örnek parola yalnız yerel geliştirme
içindir. Spring Boot `.env` dosyasını properties olarak okur. PostgreSQL başka
sunucudaysa DB_URL, DB_USERNAME ve DB_PASSWORD ortam değişkenlerini kullan.

macOS'ta PostgreSQL kurulu değilse Homebrew ile kurulum yapılabilir:

```sh
brew install postgresql@17
brew services start postgresql@17
export PATH="$(brew --prefix postgresql@17)/bin:$PATH"
psql -d postgres
```

Son komut SQL oturumunu açar; yukarıdaki ilk kurulum SQL'lerini burada çalıştır.

```sh
curl http://localhost:8080/api/system/info
curl http://localhost:8080/actuator/health
```

System info örnek cevabı:

```json
{"name":"kararDefteriApi","version":"0.0.1-SNAPSHOT"}
```

Örnek `V1__create_users.sql.example` dosyası otomatik çalıştırılmaz ve tablo
oluşturmaz. Gerçek migration, kullanıcı entity'si ve CRUD'u stajyerlerce eklenecek. [Migration rehberi](src/main/resources/db/migration/README.md)
entity eşlemelerini ve yeni SQL dosyası ekleme adımlarını açıklar.

Windows PowerShell'de `cp` yerine `Copy-Item`, `./mvnw` yerine `.\mvnw.cmd` kullan.

## Testler

Veritabanı gerektirmeyen HTTP ve ortak hata testleri:

```sh
./mvnw --batch-mode --no-transfer-progress verify
```

PostgreSQL açıkken uygulama context'i, JDBC ve Flyway entegrasyonunu da doğrula:

```sh
DB_NAME=karardefteri_test ./mvnw --batch-mode --no-transfer-progress -Ppostgres-it verify
```

CI, Ubuntu runner üzerindeki PostgreSQL servisini doğrudan başlatır ve test
veritabanıyla entegrasyon profilini çalıştırır. Gerekli kontrolün adı
`build-and-test` olarak korunmuştur. Normal verify PostgreSQL entegrasyon testini
çalıştırmaz; bu test `postgres-it` Maven profiliyle Failsafe üzerinden zorunlu olur.

## Paket yapısı ve ekip

Paket kökü `com.tein.karardefteri`. İş modüllerinde controller, service,
repository, entity ve dto paketleri bulunur. İhtiyaç olmayan katmanda sınıf yazılmaz.

| Geliştirici | Modüller | Ön inceleme |
|---|---|---|
| Çağrı @ccagrimiracozlu | user, project | Buse @buse321 |
| Buse @buse321 | meeting, decision | Kardelen @kardelensimsek |
| Kardelen @kardelensimsek | task, summary | Çağrı @ccagrimiracozlu |

Mentor: **@ahmettkuurt**. Her iş güncel main'den açılan ayrı branch'te geliştirilir.
Ön inceleme sonrası mentor son incelemeyi ve merge işlemini yapar.

- [Mimari ve yeni özellik geliştirme sırası](ARCHITECTURE.md)
- [Branch ve PR çalışma düzeni](CONTRIBUTING.md)

Yeni migration sürümlerini ekip içinde koordine edin. Controller yalnız service'i,
service repository'yi çağırır. Entity'leri HTTP cevabında doğrudan döndürmeyin; DTO
kullanın. Giriş ve iş CRUD API'leri haftalık görevlerle eklenecek. `system`
modülünde çalışan controller/service örneği bulunur.
