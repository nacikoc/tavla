# Tavla — tek komutluk yapım betiği
# src/app.html -> index.html -> android/app/src/main/assets -> APK

$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot
$java = 'C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot'
$gradle = "$env:USERPROFILE\.gradle\wrapper\dists\gradle-8.14.4-bin\92wwslzcyst3phie3o264zltu\gradle-8.14.4\bin\gradle.bat"

# 1) src/app.html -> index.html (tam HTML iskeleti sararak)
Write-Host '[1/3] index.html uretiliyor...'
$src = Get-Content "$root\src\app.html" -Raw -Encoding UTF8
$body = (($src -split "`n") | Select-Object -Skip 2) -join "`n"   # <title> ve <meta viewport> satirlari head'e tasindi
$head = @"
<!doctype html>
<html lang="tr">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no, viewport-fit=cover">
<title>Tavla</title>
</head>
<body>
"@
$html = $head + "`n" + $body + "`n</body>`n</html>`n"
[System.IO.File]::WriteAllText("$root\index.html", $html, (New-Object System.Text.UTF8Encoding $false))

# 2) oyunu APK varliklarina kopyala
Write-Host '[2/3] assets guncelleniyor...'
$assets = "$root\android\app\src\main\assets"
if (-not (Test-Path $assets)) { New-Item -ItemType Directory -Force $assets | Out-Null }
Copy-Item "$root\index.html" "$assets\index.html" -Force

# 3) APK derle
Write-Host '[3/3] APK derleniyor...'
$env:JAVA_HOME = $java
# DIKKAT: gradle zararsiz notlari (ornegin "uses deprecated API") stderr'e yazar.
# PowerShell 5.1 bunlari ErrorRecord'a cevirip $ErrorActionPreference='Stop' ile
# betigi durdurur; o yuzden bu cagri sirasinda durdurmayi kapatiyoruz ve
# basari/basarisizligi YALNIZCA cikis koduna bakarak karar veriyoruz.
$eskiTercih = $ErrorActionPreference
$ErrorActionPreference = 'Continue'
& $gradle -p "$root\android" assembleRelease assembleDebug bundleRelease --console=plain -q 2>&1 |
    ForEach-Object { Write-Host $_ }
$kod = $LASTEXITCODE
$ErrorActionPreference = $eskiTercih
if ($kod -ne 0) { throw "Gradle derlemesi basarisiz (exit $kod)" }

$out = "$root\apk"
if (-not (Test-Path $out)) { New-Item -ItemType Directory -Force $out | Out-Null }
Copy-Item "$root\android\app\build\outputs\apk\release\app-release.apk" "$out\Tavla.apk" -Force
Copy-Item "$root\android\app\build\outputs\apk\debug\app-debug.apk" "$out\Tavla-debug.apk" -Force
# Play'e YUKLENEN dosya budur; APK yalnizca yan yukleme/test icindir.
Copy-Item "$root\android\app\build\outputs\bundle\release\app-release.aab" "$out\Tavla.aab" -Force

Write-Host ''
Write-Host 'Hazir:'
Get-ChildItem $out -File -Include *.apk, *.aab -Recurse |
    ForEach-Object { Write-Host ("  {0}  ({1:N0} KB)" -f $_.FullName, ($_.Length / 1KB)) }
