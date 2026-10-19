# KararDefteri geliştirme düzeni

## Sorumluluklar

| Geliştirici | Alan | Ön inceleme yapan |
|---|---|---|
| Çağrı @ccagrimiracozlu | Kullanıcı, giriş ve projeler | Buse @buse321 |
| Buse @buse321 | Toplantı, not ve kararlar | Kardelen @kardelensimsek |
| Kardelen @kardelensimsek | Görevler, filtreler ve özet API’leri | Çağrı @ccagrimiracozlu |

Mentor ve son incelemeyi yapan: **@ahmettkuurt**.

## Bir işe başlama

Her iş için güncel main üzerinden yeni branch aç:

```sh
git switch main
git pull --ff-only origin main
git switch -c feat/cagri-w1-user-project-model
```

Buse için `feat/buse-w1-meeting-model`, Kardelen için
`feat/kardelen-w1-task-model` gibi isimler kullan. Kendi branch'ine commit edip
push yap ve hedefi `main` olan PR aç. main'e doğrudan push yapma.

## İnceleme ve merge

1. Küçük ve tek bir işi kapsayan PR aç; PR şablonunu doldur.
2. Otomatik istenen reviewer’ın tablodaki kişi olduğunu kontrol et. Eksikse Reviewers alanından elle seç.
3. Ön incelemeci kodu ve testleri kontrol etsin. Düzeltme gerekiyorsa
   Request changes, uygunsa Approve versin. Yalnız yorum, onay yerine geçmez.
4. Ön inceleme sonrasında @ahmettkuurt son incelemeyi yapar.
5. İki onay, çözümlenmiş konuşmalar ve başarılı build-and-test kontrolü
   tamamlandıktan sonra mentor Squash and merge ile birleştirir.
6. Birleşen branch'i kapat. Sonraki işe güncel main üzerinden yeni branch aç.

Yeni kod gönderildiğinde ruleset eski onayları düşürür; yeniden inceleme iste.
Migration dosyasının sürümünü ekip ile koordine et; main'e alınmış migration'ı
sonradan değiştirme.

İki onay ve mentor onayı GitHub ruleset ile zorunlu kılınır. Belirli stajyerin
önce incelemesi ve merge işlemini mentorun yapması ekip çalışma kuralıdır;
hazırlanan ruleset bu sırayı veya yalnız mentorun merge yapmasını teknik olarak
zorlamaz. Şartlar tamamlanınca Write yetkili kişiler de merge edebilir.

## Test

Java 21 kullan. Proje kökünde:

```sh
./mvnw --batch-mode --no-transfer-progress verify
```

Windows PowerShell:

```powershell
.\mvnw.cmd --batch-mode --no-transfer-progress verify
```

Mevcut başlangıç projesinde yalnızca Spring uygulama context testi vardır.
Özellikler eklendikçe ilgili iş kurallarını doğrulayan testleri PR'a ekle.

GitHub tarafındaki ilk kurulum için [kurulum adımlarını](.github/github-setup.md) izle.
