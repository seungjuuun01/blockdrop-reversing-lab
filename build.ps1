$ErrorActionPreference = "Stop"
$dir = "D:\FridaTarget-BlockDrop"
$BT  = "C:\Users\ssj01\AppData\Local\Android\Sdk\build-tools\36.0.0"
$AJAR= "C:\Users\ssj01\AppData\Local\Android\Sdk\platforms\android-35\android.jar"
$JAVAC = "C:\Program Files\Android\Android Studio\jbr\bin\javac.exe"
$KS  = "$env:USERPROFILE\.android\debug.keystore"

Set-Location $dir
Remove-Item -Recurse -Force classes,classes.dex,base.apk,unsigned.apk,aligned.apk,BlockDrop.apk -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force classes | Out-Null

$srcs = (Get-ChildItem -Recurse src -Filter *.java | ForEach-Object { $_.FullName })
Write-Host "[1/6] javac ($($srcs.Count) files)"
& $JAVAC -source 8 -target 8 -nowarn -bootclasspath $AJAR -d classes $srcs 2>&1 | Where-Object { $_ -notmatch 'obsolete|warning' }
if (-not (Get-ChildItem -Recurse classes -Filter *.class -ErrorAction SilentlyContinue)) { Write-Host "ERR javac"; exit 1 }

$cls = (Get-ChildItem -Recurse classes -Filter *.class | ForEach-Object { $_.FullName })
Write-Host "[2/6] d8 -> classes.dex"
& "$BT\d8.bat" --min-api 21 --output . $cls
if (-not (Test-Path classes.dex)) { Write-Host "ERR d8"; exit 1 }

Write-Host "[3/6] aapt2 link -> base.apk"
& "$BT\aapt2.exe" link -o base.apk --manifest AndroidManifest.xml -I $AJAR
if (-not (Test-Path base.apk)) { Write-Host "ERR aapt2"; exit 1 }

Write-Host "[4/6] add classes.dex into apk"
Copy-Item base.apk unsigned.apk -Force
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [System.IO.Compression.ZipFile]::Open("$dir\unsigned.apk", 'Update')
[System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, "$dir\classes.dex", "classes.dex") | Out-Null
$zip.Dispose()

Write-Host "[5/6] zipalign"
& "$BT\zipalign.exe" -f 4 unsigned.apk aligned.apk

Write-Host "[6/6] apksigner sign"
& "$BT\apksigner.bat" sign --ks $KS --ks-pass pass:android --key-pass pass:android --out BlockDrop.apk aligned.apk
if (Test-Path BlockDrop.apk) {
  "OK  BlockDrop.apk  $([math]::Round((Get-Item BlockDrop.apk).Length/1KB,1)) KB signed"
} else { "ERR sign" }
