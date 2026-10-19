## Yapılan değişiklik

<!-- Hangi haftanın hangi işini tamamladın? Sorunu ve sonucu kısaca açıkla. -->

## Nasıl test edildi

<!-- Çalıştırdığın komutu ve sonucunu yaz. API değiştiyse örnek istek/cevap ekle. -->

```sh
./mvnw --batch-mode --no-transfer-progress verify
```

## Ön inceleme

İncelemeye hazır PR açıldığında aşağıdaki kişi otomatik reviewer olarak istenir.
**Reviewers** alanından doğru kişinin eklendiğini kontrol et; otomasyon çalışmazsa elle seç:

| PR sahibi | Ön inceleme | Mentor |
|---|---|---|
| Çağrı @ccagrimiracozlu | Buse @buse321 | @ahmettkuurt |
| Buse @buse321 | Kardelen @kardelensimsek | @ahmettkuurt |
| Kardelen @kardelensimsek | Çağrı @ccagrimiracozlu | @ahmettkuurt |

Ön inceleme yapan kişi Files changed → Review changes üzerinden Approve veya
Request changes versin. Ön inceleme tamamlanınca mentor son incelemeyi yapar.

## Kontrol listesi

- [ ] PR tek bir tamamlanabilir işi kapsıyor.
- [ ] İlgili doğrulama ve testleri çalıştırdım; sonucu yukarıya yazdım.
- [ ] API değiştiyse istek/cevap örneğini veya dokümantasyonu güncelledim.
- [ ] Migration eklediysem benzersiz sürüm kullandım; önceki migration'ı değiştirmedim.
- [ ] Gizli anahtar, gerçek parola veya kişisel veri eklemedim.
- [ ] Ön inceleme yapacak stajyerin Reviewers alanında bulunduğunu kontrol ettim.

## Bağımlılıklar veya bilinen eksikler

<!-- Yoksa "Yok" yaz. Bağımlı PR varsa bağlantısını ekle. -->
