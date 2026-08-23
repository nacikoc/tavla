# Google Play — dahili test yayını yol haritası

Bu dosya, Tavla'yı Play Console'da **dahili test (internal testing)** izine çıkarmak
için gereken her adımı içerir. Yüklemenin kendisi Google hesabınıza giriş
gerektirdiği için o kısmı sizin yapmanız gerekiyor; hazırlık tarafındaki her şey
bu klasörde hazır.

---

## Yüklenecek dosya

```
apk\Tavla.aab
```

**APK değil, AAB.** Play, 2021'den beri yeni uygulamalarda `.aab` istiyor;
`apk\Tavla.apk` yalnızca yan yükleme ve elde test içindir. `build.ps1` artık
her çalıştığında ikisini birden üretiyor.

Yüklemeden hemen önce mutlaka yeniden derleyin ki AAB, oyunun son hâlini içersin:

```bash
powershell -ExecutionPolicy Bypass -File build.ps1
```

---

## Bu klasördeki hazır dosyalar

| Dosya | Ne için | Durum |
|---|---|---|
| `icon-512.png` | Uygulama simgesi (512×512) | Hazır |
| `feature-1024x500.png` | Öne çıkan görsel (1024×500) | Hazır |
| `tv-banner-1280x720.png` | Android TV banner'ı | Hazır |
| `screens/01-menu.png` | Telefon ekran görüntüsü (1080×1920) | Hazır |
| `screens/02-oyun.png` | Telefon ekran görüntüsü (1080×1920) | Hazır |
| `magaza-metinleri.md` | Ad, kısa/tam açıklama, sürüm notları | Hazır |
| `gizlilik-politikasi.html` | Gizlilik politikası sayfası | Hazır, **yayımlanması gerekiyor** |

**Gizlilik politikası adresi:**
https://claude.ai/code/artifact/e1f14619-a452-4349-aa9d-c6227ca60ae5

⚠️ Bu bağlantı şu an **size özel**. Play'in erişebilmesi için sayfadaki **Share**
menüsünden herkese açık hâle getirin. İsterseniz aynı HTML'i kendi alan adınıza
koyup o adresi de kullanabilirsiniz — Play yalnızca "giriş gerektirmeden açılan,
uygulamaya ait bir sayfa" istiyor.

---

## Kalıcı kararlar — bunları yüklemeden ÖNCE netleştirin

### 1. Paket adı: `com.hilspot.tavla`

İlk yüklemede hesabınıza **kalıcı olarak** bağlanır. Bir daha değiştirilemez,
silinse bile o ad tekrar kullanılamaz. Şu an doğru görünüyorsa devam edin.

### 2. Play App Signing: Google'ın ürettiği anahtarı seçin

Play, yeni uygulamalarda uygulama imzalama anahtarını kendisi yönetir. Karşınıza
çıkan seçenekte **varsayılanı** (Google imzalama anahtarı üretsin) kabul edin.
`android\tavla-release.jks` böylece yalnızca *yükleme anahtarınız* olur; kaybolur
ya da sızarsa Google'dan sıfırlatabilirsiniz. Kendi anahtarınızı imzalama
anahtarı olarak yüklerseniz (PEPK) kaybettiğinizde uygulamayı bir daha
güncelleyemezsiniz.

### 3. Anahtar dosyasını yedekleyin

`android\tavla-release.jks` ve yanındaki `android\keystore.properties` imzanın
tamamıdır. İkisi de `.gitignore` ile depo dışında tutuluyor — yani depoyu
paylaşsanız bile anahtar sızmaz, ama **yedeklemek tamamen size kalmış.**

Masaüstündeki `Tavla-imza-yedek.zip` paketinde anahtar, alias, parola ve
parmak izleri bir arada duruyor. En az iki ayrı yere koyun (Drive/parola
yöneticisi + çevrimdışı disk) ve Drive'daki kopyanın paylaşımını kapalı tutun.

Anahtarı kaybederseniz Play'de uygulamayı bir daha güncelleyemezsiniz.
(Play App Signing kullandığınız için yükleme anahtarı sıfırlanabilir, ama
bu talep günler sürer.)

### 4. Varsayılan dil: Türkçe (tr-TR)

Uygulama oluşturma ekranında seçilir, sonradan değiştirmek zahmetli.
İngilizce listeleme eklemek zorunda değilsiniz.

### 5. Yan yüklenmiş sürümü kaldırın

Play'den gelen kopya **farklı bir sertifikayla** imzalı olacağı için, elle
kurduğunuz Tavla duruyorsa kurulum "uygulama yüklenemedi" diye reddedilir.
Test cihazlarının hepsinden önce kaldırın:

```bash
adb uninstall com.hilspot.tavla
```

Eski paket adıyla kurulmuş sürüm de varsa o da gitsin:

```bash
adb uninstall com.tavla.app
```

---

## Play Console'da adım adım

### 1. Uygulamayı oluştur
**Tüm uygulamalar → Uygulama oluştur**
- Uygulama adı: `Tavla — Çevrimdışı Klasik`
- Varsayılan dil: **Türkçe (tr-TR)**
- Uygulama mı oyun mu: **Oyun**
- Ücretsiz mi ücretli mi: **Ücretsiz**

### 2. Uygulama içeriği beyanları
**Politika ve programlar → Uygulama içeriği.** Bu uygulama için doğru cevaplar:

| Bölüm | Cevap |
|---|---|
| Gizlilik politikası | Yukarıdaki adres |
| Uygulama erişimi | **Tüm işlevler kısıtlama olmadan kullanılabilir** (giriş yok) |
| Reklamlar | **Hayır**, uygulamada reklam yok |
| İçerik derecelendirmesi | Kategori: **Oyun**. Şiddet, cinsellik, küfür, uyuşturucu: hepsine **hayır**. **Kumar sorularına da hayır** — zar var ama bahis, para, jeton, kazanç yok; klasik bir tahta oyunu. Beklenen sonuç: 3+ / herkes |
| Hedef kitle | Yaş grupları: **13+** (çocuklara özel tasarlanmadığı için "çocuklar" seçmeyin; çocuk seçmek Aile Politikası'nın ek şartlarını devreye sokar) |
| Veri güvenliği | **Veri toplanmıyor, veri paylaşılmıyor.** Uygulamanın INTERNET izni bile yok; cihazda saklanan tek şey "toplama yönü" tercihi ve o da cihazdan çıkmıyor |
| Devlet / finans / sağlık uygulaması | Hepsi **hayır** |
| Reklam kimliği | Kullanılmıyor — **hayır** |

### 3. Mağaza kaydı
**Kullanıcı kazanma → Mağaza varlığı → Ana mağaza kaydı**
`magaza-metinleri.md` içindeki metinleri yapıştırın, `icon-512.png` ve
`feature-1024x500.png` ile `screens/` altındaki telefon görüntülerini yükleyin.

### 4. Dahili test izi
**Test ve yayınlama → Test → Dahili test**
1. **Test kullanıcıları** sekmesinde bir e-posta listesi oluşturun (en fazla 100 Gmail adresi) — kendinizi de ekleyin
2. **Yeni sürüm oluştur** → `apk\Tavla.aab` yükleyin
3. Sürüm notlarını `magaza-metinleri.md` içinden yapıştırın
4. **İncele → Kullanıma sun**
5. Sayfadaki **katılım bağlantısını** kopyalayıp testçilere gönderin; her testçi
   bağlantıyı açıp katılımı kabul etmeden uygulamayı Play'de göremez

### 5. Android TV (isteğe bağlı, paralel yürütün)
**Test ve yayınlama → Kurulum → Gelişmiş ayarlar → Form faktörleri → Android TV ekle**

TV banner'ı (`tv-banner-1280x720.png`) ve en az bir 1920×1080 TV ekran görüntüsü
ister. TV başvurusu **elle inceleniyor** ve günler sürebilir; telefon/tablet
testini bunu beklemeden başlatın. TV tarafını o süre boyunca yan yükleyerek
test etmeye devam edebilirsiniz.

---

## Bilinçli olarak yapılmayanlar

- **Tablet ekran görüntüleri** — zorunlu değil. Eklenirse tablet aramalarında
  görünürlük artar. 7 inç için 1200×1920, 10 inç için 2560×1600 önerilir.
- **Uyarlanabilir simge (adaptive icon)** — Android 8+ cihazlarda simge sistem
  şeklinin içinde biraz küçük görünüyor. Ret sebebi değil, sadece cilalanma payı.
- **`minifyEnabled`** — kapalı; kod zaten 200 satır Java, küçültmenin anlamı yok.

---

## Yükleme öncesi son kontrol

```bash
powershell -ExecutionPolicy Bypass -File build.ps1
```

Sonra `apk\Tavla.aab` dosyasının **zaman damgasının az önceki çalıştırmadan**
olduğunu doğrulayın. Gradle bazen "UP-TO-DATE" deyip eski dosyayı bırakabiliyor;
şüphelenirseniz önce temizleyin:

```bash
cd android && gradle clean bundleRelease
```
