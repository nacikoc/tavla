# GitHub Release — v1.4

Bu metin GitHub Releases sayfasındaki açıklama alanına yapıştırılmak içindir.
Kendisi bir depo belgesi değildir; sürüm çıkarken güncellenir.

Yüklenecek dosya: **`apk/Tavla-1.4.apk`** — yalnızca bu.
`Tavla.aab` Play'e özeldir, `Tavla-debug.apk` hata ayıklama içindir; ikisi de
Release'e eklenmez.

---

## Tavla 1.4

Telefon, tablet ve Android TV'de çalışan tam kurallı tavla. Reklam yok,
uygulama içi satın alma yok, **hiçbir izin istemez**, internete hiç bağlanmaz.

### Kurulum

`Tavla-1.4.apk` dosyasını indirip cihazınızda açın. Android "bilinmeyen
kaynaktan kurulum" uyarısı verirse, indirmeyi yaptığınız uygulamaya kurulum
izni verin.

Android TV'de genelde dosya yöneticisi bulunmaz; bilgisayardan kurabilirsiniz
(TV'de *Geliştirici seçenekleri → USB hata ayıklama* açık olmalı):

    adb connect TV_IP_ADRESI:5555
    adb install Tavla-1.4.apk

### Öne çıkanlar

- Tam kurallı tavla: zar zorunluluğu, kırma ve bar'dan giriş, pul toplama,
  çift zar, mars
- Bilgisayara karşı ya da aynı cihazda iki kişilik
- Üç kontrol yolu: dokunma, sürükle-bırak ve TV kumandası
- Çift dokunuşla hızlı hamle — aynı pula tekrar dokununca hamle geri alınıp
  diğer olasılığa geçilir
- Android TV'de kumandanın sağ/sol okları doğrudan oynanabilir hamleler
  arasında gezer, imleç menüye takılmaz
- Toplama yönü menüden sağa/sola çevrilir, tercih hatırlanır

### İmza hakkında

Bu APK geliştiricinin **yükleme anahtarıyla** imzalanmıştır. Uygulama ileride
Google Play'de yayına girerse, Play'den inen kopya Google'ın uygulama imzalama
anahtarını taşır ve sertifikalar farklı olur — o durumda Play sürümüne geçmeden
önce bu sürümü kaldırmanız gerekir.

Android 5.0 ve üzeri (minSdk 21) · versionCode 5 · yaklaşık 62 KB
