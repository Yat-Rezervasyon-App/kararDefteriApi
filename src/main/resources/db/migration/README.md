# Migration düzeni

## Çalıştırılmayan örnek

`V1__create_users.sql.example` yalnız referans içindir. `.sql.example` uzantısı
nedeniyle Flyway bu dosyayı çalıştırmaz; uygulama açılışında `users` tablosu
oluşturulmaz. Kullanıcı geliştirmesi sırasında örnekten yararlanıp modele uygun
gerçek `V1__create_users.sql` dosyasını ayrıca oluşturun. Kullanıcı CRUD'u ve
`User` entity'si henüz eklenmemiştir.

| SQL alanı | Java alanı | JPA eşlemesi |
|---|---|---|
| id UUID | UUID id | @Id ve @GeneratedValue(strategy = GenerationType.UUID) |
| full_name VARCHAR(120) | String fullName | @Column(name = "full_name", nullable = false, length = 120) |
| email VARCHAR(254) | String email | @Column(nullable = false, length = 254, unique = true) |
| password_hash VARCHAR(255) | String passwordHash | @Column(name = "password_hash", nullable = false, length = 255) |
| created_at TIMESTAMP WITH TIME ZONE | Instant createdAt | @Column(name = "created_at", nullable = false, updatable = false) |

Entity sınıfı `user/entity` paketinde `@Entity` ve `@Table(name = "users")` ile
tanımlanır. UUID Hibernate tarafından üretilir. `createdAt`, entity oluşturulurken
veya `@PrePersist` içinde `Instant.now()` ile atanmalıdır; normal JPA insert'i
kolonu gönderdiği için yalnız SQL default'una güvenmeyin. SQL default'u kolonu
atlayan doğrudan SQL insert'lerinde çalışır. Ham parola saklamayın; parola özeti
uygun bir PasswordEncoder ile üretilip `password_hash` alanına yazılır.

## Entity yazınca neden tablo oluşmuyor?

Hibernate `ddl-auto=update` veya `create` olduğunda entity'lerden tablo üretebilir.
Bu projede `ddl-auto=validate` kullanılır: Hibernate yalnız entity ile mevcut tablo
uyumunu kontrol eder; tablo oluşturmaz veya değiştirmez.

Uygulama açılırken önce Flyway yeni SQL dosyalarını sürüm sırasıyla çalıştırır,
sonra Hibernate entity eşlemelerini doğrular. Flyway çalıştırdığı dosyaları
`flyway_schema_history` tablosunda kaydeder; aynı migration her açılışta tekrar
uygulanmaz. Mevcut `.sql.example` dosyası bu sürece dahil değildir.

## Yeni değişiklik ekleme

1. Entity'yi oluştur veya değiştir.
2. Aynı PR içinde karşılık gelen yeni SQL migration'ını ekle.
3. Yerel PostgreSQL açıkken uygulamayı ve `-Ppostgres-it verify` testlerini çalıştır.

Dosya adında sürümden sonra **iki alt çizgi** bulunur:

- `V1__create_users.sql`: ilk gerçek kullanıcı tablosu migration'ı için örnek ad.
- `V2__create_projects.sql`: sıradaki proje tablosu için örnek ad.
- `V3__create_meetings.sql`: sonraki toplantı tablosu için örnek ad.

Henüz gerçek migration yoktur; V1 dahil numaralar rezervasyon değildir. Üç geliştirici yeni sürüm
numarasını PR açmadan önce koordine etmelidir. Yabancı anahtarın bağlandığı tablo
önce oluşturulmalıdır. Sıra users → projects → meetings → tasks/decisions olur.

Uygulanmış veya main'e alınmış migration değiştirilmez; yeni sürüm eklenir.
Örneğin users tablosuna yeni alan eklenecekse, sıradaki boş numarayla
`V4__add_phone_to_users.sql` gibi bir dosyada `ALTER TABLE` yazılır. Alan ve numara
bu açıklamada örnektir; böyle bir dosya henüz eklenmemiştir.

Mevcut PostgreSQL'de Hibernate'in daha önce oluşturduğu tablolar varsa V1'i
çalıştırmadan önce şema ve migration geçmişi birlikte değerlendirilmelidir.
Bu ilk kurulum iskeletini yeni ve boş bir geliştirme veritabanında çalıştırın.
