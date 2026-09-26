# Geliştirici rehberi

Bu dosya `README.md`'nin tamamlayıcısıdır. README **projeyi kullanmayı**
anlatır; bu dosya **kodu değiştirmeyi** anlatır: neresinde ne var, hangi karar
neden alındı ve hangi tuzaklara düşüldü.

Yazılma amacı: bu bilginin bir kısmı yalnızca geliştirme sırasındaki
konuşmalarda vardı. Buraya taşındı ki kod kendi kendine yeterli olsun.

**Son güncelleme:** 26 Eylül 2026 · `src/app.html` 1111 satır · sürüm 1.4
(versionCode 5)

---

## 1. Ortam — yeni bir bilgisayarda ilk iş

`build.ps1` iki yolu **sabit yazılmış** halde tutar (satır 6-7) ve bunlar
başka bir makinede kesinlikle farklı olur:

```powershell
$java   = 'C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot'
$gradle = "$env:USERPROFILE\.gradle\wrapper\dists\gradle-8.14.4-bin\92wwslzcyst3phie3o264zltu\gradle-8.14.4\bin\gradle.bat"
```

`92wwslzcyst3phie3o264zltu` Gradle'ın ürettiği rastgele bir klasör adıdır —
her kurulumda değişir. Yeni makinede derleme "dosya bulunamadı" ile
patlarsa **ilk bakılacak yer burasıdır.** Gerçek yolu bulmak için:

```powershell
Get-ChildItem "$env:USERPROFILE\.gradle\wrapper\dists" -Recurse -Filter gradle.bat | Select-Object FullName
```

Gerekenler: **JDK 21** (derleme JDK'sı), **Android SDK 36** (compileSdk 36),
Gradle 8.14.4 wrapper dağıtımı. Java kaynak uyumluluğu 17'ye sabitli
(`android/app/build.gradle`), yani JDK 21 ile derlenip 17 hedefiyle çıkar.

İmzalama için iki dosya gerekir, **ikisi de depoda yoktur**:
`android/tavla-release.jks` ve `android/keystore.properties`. Yedek:
`C:\Users\PC\Desktop\Tavla-imza-yedek.zip`. Yoksa derleme çalışır, sadece
release paketi imzasız çıkar (`build.gradle` bunu kendi halleder).

---

## 2. Neden bu mimari

**Neden HTML5, native Android değil.** Bu soru geliştirme sırasında ayrıca
değerlendirildi ve HTML5'te kalma kararı verildi. Gerekçeler:

- Android TV'nin zorladığı her şey (tam ekran, leanback launcher, D-pad
  girdisi, geri tuşu) zaten native katmanda ve doğru çalışıyor.
- Performans maliyeti bir mimari sorun değil: tek bir SVG `filter`
  özniteliği ve `innerHTML` ile yeniden çizim. Tavla 60 fps gerektiren bir
  oyun değil.
- Tek kod tabanı dört hedefe hizmet ediyor: tarayıcı, telefon, tablet, TV.
  Native'e geçmek bunu dörde bölerdi.

**Neden tek dosya, çerçeve yok, derleme adımı yok.** `src/app.html` HTML
iskeleti (`<html>/<head>/<body>`) olmadan yazılır; `build.ps1` onu sarar.
Böylece dosya hem doğrudan tarayıcıda açılabilir hem de bir Artifact olarak
yayınlanabilir. Bağımlılık yok, `npm install` yok, güvenlik güncellemesi
kovalanacak paket yok.

**Neden `innerHTML` ile yeniden çizim.** Durum değiştiğinde `render()`
tahtanın tamamını SVG dizesi olarak üretip `svg.innerHTML`'e basar. Diff
yok, sanal DOM yok. 1500×1000 viewBox'lık bir tahtada bu ölçülebilir bir
maliyet çıkarmadı ve kodu kökten sadeleştirdi. **İstisnası sürüklemedir**
(bkz. Tuzak 1).

**Neden DOM odağı kullanılmıyor.** TV'de D-pad gezintisi tamamen elle
yazıldı (`focusables`, `curFocus`, `focusPos`, `G.fi`, `G.kbActive`). Sebep:
tahta her çizimde `innerHTML` ile yeniden kurulduğu için DOM odağı sürekli
kaybolur. Odak bir durum değişkeni olarak tutulur, DOM'da değil.

---

## 3. Kod haritası — `src/app.html`

Satır numaraları 1.4 sürümüne aittir ve düzenledikçe kayar; bölüm
başlıkları (`/* ===== ... ===== */`) dosyada gerçekten vardır, aramak için
onları kullanın.

| Satır | Bölüm | İçerik |
|---|---|---|
| 3–130 | `<style>` | Tüm CSS. Eski WebView kısıtları burada geçerli (bkz. README → Tarayıcı uyumluluğu) |
| 132–176 | Biçimlendirme | `#app`, `#topbar`, `#chips`, `#board` (SVG, viewBox 1500×1000), menü, `#toast` |
| 178–313 | **Kural motoru** | Saf fonksiyonlar, arayüzden tamamen bağımsız |
| 315–355 | Ses | `snd(kind)` — WebAudio ile sentezlenir, ses dosyası yok |
| 356–382 | **Durum** | `IS_TV`, `G` nesnesi, `toast`, zamanlayıcı yardımcıları |
| 383–414 | **Geometri** | `setGeo`, `colOf`, `colX`, `slotPos`, `barPos` |
| 415–432 | Çizim ilkelleri | `die` (zar yüzü), `checker` (pul) |
| 433–598 | **`render()`** | Tek büyük çizim fonksiyonu — tahta, pullar, zar, butonlar, ipuçları |
| 599–719 | Odak sistemi | `focusRect`, `focusables`, `curFocus`, `focusPos`, `updateHtmlFocus` |
| 720–855 | Oyun akışı | `startGame`, `animateRoll`, `doOpeningRoll`, `rollForTurn`, `beginMove`, `flyMove`, `humanPlay` |
| 856–881 | **Çift dokunuş** | `DBL_MS`, `QUICK_END_MS`, `endDelay`, `quickCands`, `quickPlay`, `cycleQuick` |
| 883–931 | Sıra sonu | `endTurn`, `aiStep`, `gameOver` |
| 932–1031 | **`dispatch(act, dbl)`** | Tüm eylemlerin tek giriş kapısı |
| 1032–1105 | **Sürükleme** | `svgPt`, `targetAt`, `dragStart/Move/End` + olay dinleyicileri |
| 1106–1109 | Başlatma | `render()` ve `resize` dinleyicisi |

### Kural motoru (178–313)

Arayüzden bağımsızdır; test etmek için ekrana ihtiyaç duymaz.

- `newState()` — başlangıç dizilimi. `{pts:[24], bar:{}, off:{}}`
- `movesFor(s,p,d)` — tek bir zar için geçerli hamleler
- `canBear(s,p)` — pul toplamaya başlanabilir mi
- `applyMove(s,p,m)` — hamleyi uygular, kırma dahil
- **`maxPlay(s,p,dice,memo)`** — tavlanın en kritik kuralı: *mümkün olan en
  çok zarı oynama zorunluluğu.* Yalnızca bir zar oynanabiliyorsa büyük olan
  tercih edilir. `memo` ile ezberlenir, yoksa çift zarlarda ağaç patlar.
- **`allowedMoves(s,p,dice)`** — `maxPlay` sonucuna göre filtrelenmiş,
  oyuncuya *gerçekten* izin verilen hamleler. Arayüz her yerde bunu kullanır
  (`G.allowed`); kural kontrolü arayüzde tekrar edilmez.
- `evalB(s)` / `pip(s,p)` / `aiSequence(s,dice)` — yapay zeka. `dfs` ile
  hamle dizileri denenir, `evalB` konumu puanlar.

### Durum nesnesi `G` (360)

Tek gerçek kaynak. `G.phase` oyunun durum makinesidir:

```
menu → opening → await → rolling → move → (ai) → await → ... → over
```

Kritik alanlar:

- `G.allowed` — izin verilen hamleler; `beginMove` ve her `humanPlay`
  sonrasında yeniden hesaplanır
- `G.undoStack` — geri alma. Çift dokunuş zinciri de bunu kullanır
- `G.quick` — `{from, i, n}`: çift dokunuşla oynanan son hamle, kaçıncı
  olasılık ve kaç olasılık var
- `G.fi` / `G.kbActive` — D-pad odağı. `kbActive` TV'de `true` başlar
  (`IS_TV`), dokunma olunca `false` olur
- `G.anim` — animasyon kilidi; açıkken girdi yok sayılır
- `G.ptr` — oyuncu dokunma/fare mi kullanıyor (odak halkasını gizlemek için)

### Geometri (383–414)

İki yerleşim vardır ve `setGeo()` pencere oranına göre seçer:

- **Yatay** (TV, tablet): 1500×1000, ortada dikey bar, kenarda toplama sütunu
- **Dikey** (telefon): 1000×1700, iki yarı üst üste, zar/buton/toplama
  ortadaki yatay banda taşınır

`MIR` toplama yönünü çevirir (varsayılan **solda**), `localStorage`
anahtarı `tavla-mir`. Kural olarak iki yön de geçerlidir; bu bir tercihtir.

Hane indeksi → ekran koordinatı zinciri:
`colOf(i)` (hangi sıra/sütun) → `colX(c)` (x, `MIR` burada uygulanır) →
`slotPos(i,idx,count)` (pulun tam yeri, kalabalıkta üst üste biner).

### Girdi — üç ayrı yol

Hepsi `dispatch` ya da `humanPlay` ile birleşir:

1. **Dokunma / tıklama** → `document` üzerindeki `click` dinleyicisi →
   `dispatch(act, dbl)`. **`dbl` argümanını yalnızca bu yol gönderir.**
2. **Klavye / D-pad** → `keydown` → `dispatch(act)` — tek argümanla.
   Bu yüzden çift dokunuş kısayolu kumandada yapısal olarak yoktur; hızlı
   iki OK basışı kazayla hamle oynayamaz.
3. **Sürükleme** → `dragStart/Move/End` → `humanPlay(mv, null, true)`.
   Üçüncü argüman "pul zaten elle taşındı, uçuş animasyonu yapma" demek.

TV'de `focusables()`, `G.kbActive` açıkken hane değil **hamle** listesi
döndürür (`mv-N`). Sağ/sol oklar oynanabilir hamleler arasında gezer, OK
oynar. Bu, "imleç menüye kaçıyor" şikayeti üzerine hane-hane gezinmenin
yerine getirildi.

---

## 4. Tuzaklar

Hepsi geliştirme sırasında gerçekten yaşandı. Sırası önem sırası değil,
hepsi tekrar düşülmeye müsait.

### 1. Sürükleme sırasında `render()` çağırmayın

Parmak hareket ederken tahtayı yeniden çizerseniz `innerHTML` dokunuşun
hedef öğesini DOM'dan söker, tarayıcı `touchmove`/`touchend` iletmeyi keser
ve **sürükleme ortada ölür.** Bunun yerine kalkan pul `data-chk` ile
bulunup `opacity` ile gizlenir, hayalet kopya ayrı bir `<g>` olarak taşınır.
`dragEnd` bittiğinde tek bir `render()` yapılır.

### 2. Gradle "UP-TO-DATE" der, eski paketi bırakır

`build.ps1` başarı yazar ama `apk/` içindeki dosyaların tarihi eskidir.
Yayınlamadan önce **tarihe bakın.** Şüphede temizleyin:

```powershell
& $gradle -p android clean
```

Bu tuzak bir kez gerçek bir yanlış paketi yayınlamaya çok yaklaştırdı.

### 3. PowerShell 5.1, Gradle'ın stderr notlarını hata sayar

Gradle "uses deprecated API" gibi zararsız notları stderr'e yazar.
PowerShell bunları `ErrorRecord`'a çevirir ve `$ErrorActionPreference='Stop'`
altında betiği durdurur — derleme başarılı olsa bile APK kopyalanmaz.
`build.ps1` bu çağrı sırasında durdurmayı kapatır ve başarıyı **yalnızca
`$LASTEXITCODE`** ile ölçer. Bu yapıyı bozmayın.

### 4. `viewport-fit=cover` `build.ps1` içindedir

Viewport etiketi `src/app.html`'de değil, `build.ps1`'in ürettiği `<head>`
bölümünde (satır 18). Çentikli ekranlarda `env(safe-area-inset-*)`
boşluklarının çalışması buna bağlıdır. Bir kez `src/app.html`'de düzeltildi
ve APK'da hiçbir etkisi olmadı — çünkü yanlış yerdeydi.

### 5. Android 13+ geri tuşu `onBackPressed()` çağırmaz

`targetSdk 36` ile öngörülü geri (predictive back) devreye girer ve
`onBackPressed()` hiç çalışmaz. `MainActivity` bu yüzden ayrıca
`OnBackInvokedDispatcher`'a kayıt olur; geri mantığı `geriBas()` içinde
ortaktır.

### 6. Emülatör gerçek çift dokunuş üretemez

`adb shell input tap` iki dokunuş arasına ~1,4 saniye koyar — `DBL_MS`
(350 ms) penceresinin çok üstünde. İki komutu aynı anda göndermek de iki
dokunuşu tek dokunuşa birleştirir. Çift dokunuş mantığı bu yüzden
WebView'in **içinde** çalışan bir doğrulama sayfasıyla test edildi
(sentetik `click` olayları). Sürükleme için `input swipe` kullanılabilir.

### 7. `DBL_MS` ile `endTurn` gecikmesi birbirine bağlı

`DBL_MS` (350 ms), en kısa `endTurn` gecikmesinin (500 ms) **altında
kalmalıdır**; yoksa sıra, oyuncu fikrini değiştirme şansı bulamadan biter.
`endDelay()` çift dokunuşla oynanmış ve birden fazla olasılık varsa süreyi
`QUICK_END_MS` (1500 ms) kadar uzatır.

### 8. Play ekran görüntüsü en-boy sınırı

Telefondan ham yakalama 1080×2340'tır (2,167:1) ve Play'in **2:1**
sınırını aşar — yüklerken reddedilir. Yakalamadan önce:

```bash
adb shell wm size 1080x1920
```

### 9. `render()` içinde çizim sırası önemli

Vuruş dikdörtgenleri (hane tıklama alanları) "ZAR AT" butonunun üstüne
denk gelirse butonu yutar. Vuruş alanları butonlardan **önce** çizilir.

### 10. Play imzası ile yerel imza farklıdır

Play App Signing kullanıldığı için Play'den inen kopya Google'ın
anahtarıyla, yerel `Tavla.apk` ise yükleme anahtarıyla imzalıdır. İkisi
aynı cihaza sırayla kurulamaz ("uygulama yüklenemedi"). Her iki kanalı da
kullanacaksanız Play Console → App bundle explorer → Downloads →
*Signed, universal APK* indirip **onu** dağıtın.

---

## 5. Sürüm çıkarma

1. `android/app/build.gradle` içinde `versionCode`'u **artır** (Play aynı
   kodu ikinci kez kabul etmez) ve `versionName`'i güncelle
2. `.\build.ps1` — `index.html`, varlıklar ve üç paket üretilir
3. `apk/` içindeki dosyaların **tarihini doğrula** (Tuzak 2)
4. Play'e **`apk/Tavla.aab`** yüklenir; `Tavla.apk` yalnızca elden kurulum
5. GitHub Releases'e **yalnızca** release APK'sı eklenir — `.aab` Play'e
   özeldir, `Tavla-debug.apk` hata ayıklama içindir
6. Etiket: `git tag -a vX.Y -m '...'` + `git push origin vX.Y`

Play Console tarafındaki beyan formlarının bu uygulama için doğru cevapları
[../store/PLAY-YOL-HARITASI.md](../store/PLAY-YOL-HARITASI.md) dosyasında.

---

## 6. Bilinen eksikler

Bilinçli bırakılanlar (ret sebebi değil, cilalanma payı):

- **Uyarlanabilir simge** yok — Android 8+ cihazlarda simge sistem şeklinin
  içinde biraz küçük görünür
- **Tablet ekran görüntüleri** Play'e yüklenmedi — zorunlu değil, eklenirse
  tablet aramalarında görünürlük artar
- **`minifyEnabled false`** — Java tarafı ~200 satır, küçültmenin anlamı yok
- **Otomatik test yok** — kural motoru saf fonksiyonlardan oluştuğu için
  test edilebilir durumda, ama kalıcı bir test dosyası konmadı.
  `allowedMoves` ve `maxPlay` en çok test hak eden yerler.
- **Tablette v1.4 hiç kurulmadı** — cihaz bağlanamadı; telefon ve TV'de
  doğrulandı
