# 构建可双击启动的 Windows 启动器（XiangQi.exe）
#
# 用法（在仓库根目录）：
#   powershell -ExecutionPolicy Bypass -File build-launcher.ps1
#
# 结果：
#   XiangQi\dist\XiangQi\XiangQi.exe   ← 双击即可运行（自带精简 JRE，无需安装 Java）
#
# 说明：只用 JDK 自带的 javac / jar / jpackage，不依赖 Maven 或任何第三方工具。

$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot

function Find-Jdk {
    if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME 'bin\jpackage.exe'))) {
        return $env:JAVA_HOME
    }
    $candidates = @()
    $candidates += (Get-ItemProperty 'HKLM:\SOFTWARE\JavaSoft\JDK\*' -ErrorAction SilentlyContinue).JavaHome
    $candidates += (Get-ChildItem 'C:\Program Files\Java' -Directory -ErrorAction SilentlyContinue | ForEach-Object { $_.FullName })
    $candidates += (Get-ChildItem 'F:\Java' -Directory -ErrorAction SilentlyContinue | ForEach-Object { $_.FullName })
    foreach ($c in $candidates) {
        if ($c -and (Test-Path (Join-Path $c 'bin\jpackage.exe'))) { return $c }
    }
    throw '找不到带 jpackage 的 JDK，请设置 JAVA_HOME 指向 JDK 25+ 的安装目录。'
}

$jdk = Find-Jdk
Write-Host "使用 JDK: $jdk"

$classes = 'XiangQi\target\classes'
$jar = 'XiangQi\target\xiangqi.jar'
$dist = 'XiangQi\dist'

# 1) 编译
if (Test-Path $classes) { Remove-Item -Recurse -Force $classes }
$sources = Get-ChildItem -Recurse 'XiangQi\src\main\java' -Filter *.java | ForEach-Object { $_.FullName }
& (Join-Path $jdk 'bin\javac.exe') -encoding UTF-8 -d $classes $sources
if ($LASTEXITCODE -ne 0) { throw '编译失败' }

# 2) 打包成 jar
& (Join-Path $jdk 'bin\jar.exe') --create --file $jar --main-class edu.sustech.xiangqi.Main -C $classes .
if ($LASTEXITCODE -ne 0) { throw 'jar 打包失败' }

# 3) 生成自带运行时的应用镜像（含 XiangQi.exe）
if (Test-Path $dist) { Remove-Item -Recurse -Force $dist }
& (Join-Path $jdk 'bin\jpackage.exe') --type app-image --name XiangQi --app-version 1.0 `
    --input 'XiangQi\target' --main-jar 'xiangqi.jar' --main-class edu.sustech.xiangqi.Main `
    --dest $dist --icon 'XiangQi\launcher\app.ico' --java-options '-Dfile.encoding=UTF-8'
if ($LASTEXITCODE -ne 0) { throw 'jpackage 打包失败（可用 --type app-image 需要 JDK 自带 jpackage）' }

Write-Host ''
Write-Host "完成：$dist\XiangQi\XiangQi.exe （双击启动，无需另装 Java）"
