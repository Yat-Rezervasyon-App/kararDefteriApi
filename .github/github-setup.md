# GitHub PR korumasını etkinleştirme

Repository: https://github.com/Yat-Rezervasyon-App/kararDefteriApi
Mentor: @ahmettkuurt

Bu dosyaların repository'ye eklenmesi branch korumasını kendiliğinden açmaz.
Aşağıdaki kurulum repository yöneticisi tarafından GitHub'da uygulanmalıdır.

## İlk kurulum sırası

1. Önce bu dosyaları main branch'ine al. CODEOWNERS dosyası PR'ın hedef
   branch'inde bulunmalıdır. Henüz koruma yokken bu başlangıç kurulumu tamamlanır.
2. Settings → Collaborators and teams / Manage access bölümünden Çağrı, Buse ve
   Kardelen'e Write, @ahmettkuurt hesabına Admin yetkisi ver.
   Hesaplar: Çağrı @ccagrimiracozlu, Buse @buse321, Kardelen @kardelensimsek.
   CODEOWNERS hesabının repository'de yazma veya daha yüksek yetkisi olmalıdır.
3. Actions sekmesinden CI iş akışını Run workflow ile main üzerinde çalıştır.
   build-and-test kontrolü yeşil olmadan korumayı etkinleştirme.
4. Settings → Rules → Rulesets → New ruleset → Import a ruleset yolunu aç.
   Menü görünümü değişirse New ruleset menüsünde Import seçeneğini ara.
5. Bu klasördeki main-ruleset.json dosyasını seç. main hedefini, build-and-test
   kontrolünü ve iki onay şartını kontrol ederek kaydet. JSON Active durumundadır;
   import sonrasında kaydedildiğinde kurallar uygulanır. Önceden aynı isimde kural
   varsa kopya oluşturmak yerine mevcut kuralla karşılaştırıp güncelle.
6. Bir deneme branch'inden PR açarak mentor ve diğer bir geliştirici onayı ile
   test kontrolü olmadan merge yapılamadığını doğrula.

Public repository'lerde bu korumalar GitHub Free ile kullanılabilir. Private
organizasyon repository'sinde Team veya uygun Enterprise planı gerekir.

## İçe aktarılacak kurallar

- Hedef branch: main.
- Doğrudan push yerine PR zorunlu.
- En az iki onay; bunlardan biri CODEOWNERS nedeniyle @ahmettkuurt.
- Yeni kod gönderilince eski onaylar iptal edilir.
- İnceleme konuşmaları çözümlenmeden merge yapılamaz.
- build-and-test başarılı olmalı ve PR branch'i main ile güncel olmalı.
- main silinemez ve force push yapılamaz.
- Bypass listesi boş; yöneticiler de normal PR sürecini kullanır.

Mentorun kendi açtığı PR'larda kendi onayı geçerli sayılmaz. Tek CODEOWNER mentor
olduğu için bu PR'lar normal kuralla birleşemez; başlangıç kurulumundan sonraki
özellik PR'larını stajyerler açmalıdır. Mentorun düzenli PR açacağı durumda ikinci
bir yetkili mentor CODEOWNERS'a eklenmeli ve onay politikası tekrar belirlenmelidir.

## İnceleme eşleşmesi

Çağrı → Buse → mentor; Buse → Kardelen → mentor; Kardelen → Çağrı → mentor.
İncelemeye hazır PR açıldığında, yeniden açıldığında veya taslaktan hazır duruma
geçtiğinde auto-reviewers.yml aşağıdaki hesap eşleşmesini kullanır:

- @ccagrimiracozlu → @buse321
- @buse321 → @kardelensimsek
- @kardelensimsek → @ccagrimiracozlu

Taslaklar, başka yazarlardan gelen PR’lar ve fork PR’ları için atama yapılmaz.
Workflow main üzerinde bulunmalıdır. Üç stajyer repository davetini kabul etmiş
ve Write yetkisine sahip olmalıdır. Otomasyon yalnız inceleme isteği gönderir;
onay vermez veya merge yapmaz. İnceleme sırası ekip kuralı olmaya devam eder.
Kuralın gerektirdiği ikinci onayı farklı bir Write yetkili kişi de verebilir.
Actions çalışmazsa Reviewers alanından ilgili hesabı elle seç.

Workflow PR kodunu checkout etmez veya çalıştırmaz; yalnız PR metadata’sını
kullanır. pull-requests: write izniyle GitHub’ın GITHUB_TOKEN hesabını kullanır;
ayrıca PAT veya gizli anahtar gerekmez.
CODEOWNERS, PR incelemeye hazır olduğunda mentoru otomatik incelemeye dahil eder.
GitHub onayların sırasını zorlamaz; mentor ön inceleme bitince onay verir.

Bu ruleset mentor onayını zorunlu kılar fakat yalnız mentorun merge yapmasını
zorlamaz. Merge işlemini mentorun yapması ekip çalışma kuralıdır.

## PostgreSQL eklendiğinde

Başlangıç projesinde veritabanı bağımlılığı yoktur; bu nedenle CI'da şu anda
PostgreSQL servisi çalıştırılmaz. JPA/Flyway eklendiğinde aynı PR içinde test
profili ve PostgreSQL test altyapısı da eklenmelidir. Testleri kapatarak CI'ı
geçirmeyin; varsayılan context testi de veritabanı yapılandırmasından etkilenir.

## Kaynaklar

- https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-rulesets/creating-rulesets-for-a-repository
- https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/customizing-your-repository/about-code-owners
- https://docs.github.com/en/actions/tutorials/build-and-test-code/java-with-maven
