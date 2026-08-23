# Tavla

Telefon, tablet ve Android TV'de oynanabilen tavla oyunu. Oyunun tamamı tek bir HTML
dosyası; Android uygulaması bu dosyayı tam ekran bir WebView içinde açar.

## Kurulum

APK dosyaları `apk/` klasöründedir. İkisi de aynı oyundur:

| Dosya | Kullanım |
|---|---|
| `apk/Tavla.apk` | Normal kurulum (imzalı sürüm) |
| `apk/Tavla-debug.apk` | Geliştirme/hata ayıklama |

**Telefon ve tablet:** APK'yı cihaza kopyalayın, dosya yöneticisinden dokunun.
Android "bilinmeyen kaynak" uyarısı verirse, o uygulama için kurulum iznini açın.

**Android TV / TV box:** TV'de dosya yöneticisi genelde bulunmaz. İki yol var:

- USB bellek ile: APK'yı belleğe atın, TV'ye takın, TV'nin dosya yöneticisi veya
  "Send Files to TV" benzeri bir uygulamayla açın.
- Bilgisayardan ADB ile (TV ve bilgisayar aynı ağda, TV'de *Geliştirici seçenekleri >
  USB hata ayıklama* açık):

```bash
adb connect TV_IP_ADRESI:5555
```

```bash
adb install apk/Tavla.apk
```

Kurulduktan sonra uygulama TV ana ekranındaki uygulamalar arasında görünür
(manifest'te `LEANBACK_LAUNCHER` tanımlı).

## Oynanış

- **Dokunmatik:** Oynatmak istediğiniz pula dokunun, sonra altın noktayla işaretlenen
  hedefe dokunun.
- **Çift dokunuş (kısayol):** Pula çift dokunursanız hamle hemen oynanır — büyük zar
  önce denenir. Aynı haneye tekrar çift dokunursanız hamle geri alınıp *diğer*
  olasılığa geçilir; hamleler üst üste binmez. Kaynak hanedeki kesik çizgili halka ve
  yanındaki `1/2` sayacı, kaç seçenek olduğunu ve hangisinde olduğunuzu gösterir.
  Başka bir yere dokununca zincir kapanır. "Geri Al" tek adımda çift dokunuş
  öncesine döner.
- **TV kumandası:** Ok tuşlarıyla seçim değiştirilir, OK ile onaylanır. Geri tuşu
  seçimi iptal eder; iki kez basınca uygulamadan çıkar. Çift dokunuş kısayolu
  kumandada **yoktur**: hızlı iki OK basışı kazayla hamle oynamasın diye, kısayol
  yalnızca dokunma/fare yolunda tanınır (`dispatch(act, dbl)` ikinci argümanını
  yalnızca click dinleyicisi gönderir).

Kurallar: zar zorunluluğu (mümkünse iki zar da oynanır, tek zar oynanabiliyorsa büyük
olan tercih edilir), pul kırma ve bar'dan giriş, pul toplama, mars (2 puan) ve normal
galibiyet (1 puan).

## Geliştirme

Kaynak oyun dosyası `src/app.html`. Bu dosya HTML iskeleti olmadan yazılır (Artifact
olarak yayınlanabilmesi için); `build.ps1` onu tam bir HTML sayfasına sarıp
`index.html` üretir, APK varlıklarına kopyalar ve iki APK'yı derler:

```powershell
.\build.ps1
```

Sadece tarayıcıda denemek için `index.html` dosyasını açmak yeterlidir.

### Tarayıcı uyumluluğu

`minSdk 21` olduğu için oyun eski WebView sürümlerinde de çalışmak zorunda — test
edilen bir Android 11 cihazda WebView 83 çıktı. Bu yüzden sayfada şunlar
**kullanılmaz**: `inset` kısayolu, `margin-inline`, flex `gap`, `dvh` birimi ve
`:focus-visible`'ın `.kbf` ile aynı seçici listesinde yer alması. Bunların yerine
sırasıyla `top/right/bottom/left`, `margin-left/right`, komşu-kardeş kenar boşluğu
(`> * + *`), `@supports` ile korunan `dvh` ve ayrı kurallar kullanılır.

Yeni CSS özelliği eklerken hedef, Chrome 80 seviyesinde çalışmasıdır.

`build.ps1` sayfanın `<head>` bölümünü kendisi yazar; `src/app.html` ilk iki satırı
(başlık ve viewport) atılır. Viewport etiketini değiştirirseniz **build.ps1 içindekini**
düzenleyin — `viewport-fit=cover` oradan gelir ve çentikli ekranlarda
`env(safe-area-inset-*)` boşluklarının çalışması ona bağlıdır.

### Test

`android/app/src/main/assets/` altına `test.html` (oyun + doğrulama betiği) koyup
MainActivity'ye geçici bir intent ekstrası eklenerek WebView'in kendi içinde
çalıştırılabilir; sonuçlar `adb logcat -s TavlaWV` çıktısına düşer. Çift dokunuş
akışı bu yolla WebView 83 üzerinde doğrulandı (kural motoru, döngü, kırma geri alma,
zar durumu, TV yolunun etkilenmediği). Emülatörün `input tap` komutu iki dokunuş
arasına ~1,4 sn koyduğu için gerçek çift dokunuşu üretemez; zamanlama testleri
sayfa içinden sentetik `click` olaylarıyla yapılır.

## Proje yapısı

```
src/app.html                 oyunun tamamı (motor, arayüz, yapay zeka)
index.html                   build.ps1 tarafından üretilir — elle düzenlemeyin
build.ps1                    tek komutluk yapım betiği
apk/                         Tavla.apk (yan yükleme) + Tavla.aab (Play'e yüklenen)
android/                     Android WebView sarmalayıcı projesi
  app/src/main/java/...      MainActivity — tam ekran WebView, geri tuşu, çentik
  app/src/main/assets/       index.html buraya kopyalanır
  tavla-release.jks          imzalama anahtarı — depoda YOK, .gitignore'da
  keystore.properties        anahtar yolu ve parolaları — depoda YOK
store/                       Play Console için hazırlıklar
  PLAY-YOL-HARITASI.md       yükleme adımları ve beyanların cevapları
  magaza-metinleri.md        uygulama adı, açıklamalar, sürüm notları
  gizlilik-politikasi.html   gizlilik politikası sayfası
  icon-512.png · feature-1024x500.png · tv-banner-1280x720.png
  screens/                   mağaza ekran görüntüleri (telefon 1080×1920, TV 1920×1080)
```

Uygulama kimliği **`com.hilspot.tavla`** — Play'e ilk yüklemeden sonra bir daha
değiştirilemez.

### İmzalama

Release imzası için iki dosya gerekir ve **ikisi de depoya girmez**:

- `android/tavla-release.jks` — imzalama anahtarı
- `android/keystore.properties` — anahtar yolu, alias ve parolalar

Bu iki dosya yoksa derleme yine çalışır, yalnızca release paketi imzasız çıkar.
Yeni bir bilgisayarda kurarken ikisini de yedeğinizden geri koyun; `build.gradle`
gerisini kendisi halleder. Anahtarı kaybederseniz Play'de uygulamayı
güncelleyemezsiniz — yedeğini ayrı bir yerde tutun.

`keystore.properties` şu dört satırdan oluşur:

```properties
storeFile=../tavla-release.jks
storePassword=...
keyAlias=tavla
keyPassword=...
```

## Play Console

Dahili teste çıkarma adımları, beyan formlarının cevapları ve kalıcı kararlar
[store/PLAY-YOL-HARITASI.md](store/PLAY-YOL-HARITASI.md) dosyasında.
Play'e yüklenen dosya `apk/Tavla.aab`'dir; `apk/Tavla.apk` yalnızca yan yükleme
ve elde test içindir.
