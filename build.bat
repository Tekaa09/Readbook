@echo off
rem Dong goi Redbook:
rem   app.jar                          ban admin: quan ly de, cau hoi trong MySQL tren may nay
rem   dist\RedbookUser\RedbookUser.jar ban nguoi dung: chay offline, de dong goi san ben trong
rem build.bat release: them release\Redbook-windows.zip (kem Java) va release\Redbook-jar.zip de dang len GitHub
setlocal
cd /d "%~dp0"
set "JDK=C:\Program Files\Java\jdk-17\bin"
set "CONNECTOR=%~dp0lib\mysql-connector-j-9.4.0.jar"
rem thu muc tam nam ngoai OneDrive de khong bi khoa file khi dong bo
set "B=%TEMP%\redbook_build"

if not exist config\admin.properties (
  echo Thieu config\admin.properties - hay copy tu config\admin.properties.example roi dien mat khau MySQL.
  goto :error
)

echo [1/5] Compile...
"%JDK%\javac" --release 8 -nowarn -encoding UTF-8 -cp "%CONNECTOR%" -d bin src\*.java || goto :error

echo [2/5] Prepare...
if exist "%B%" rmdir /s /q "%B%"
mkdir "%B%\lib" "%B%\admin" "%B%\userbin"
pushd "%B%\lib"
"%JDK%\jar" xf "%CONNECTOR%" || (popd & goto :error)
del META-INF\MANIFEST.MF
popd
copy /y config\admin.properties "%B%\admin\redbook-default.properties" >nul

echo [3/5] Export questions from MySQL...
if not exist dist mkdir dist
"%JDK%\java" -cp "bin;%B%\admin;%CONNECTOR%" DatasetExporter "%B%\redbook-data.dat"
if errorlevel 1 (
  if not exist dist\redbook-data.dat (
    echo Khong doc duoc MySQL. Hay bat MySQL roi chay lai.
    goto :error
  )
  echo CANH BAO: MySQL chua bat - dung lai bo de xuat lan truoc ^(dist\redbook-data.dat^).
  copy /y dist\redbook-data.dat "%B%\redbook-data.dat" >nul
) else (
  copy /y "%B%\redbook-data.dat" dist\redbook-data.dat >nul
)

echo [4/5] app.jar (admin)...
"%JDK%\jar" cfm app.jar manifest.txt -C bin . -C "%B%\lib" . -C "%B%\admin" redbook-default.properties || goto :error
if exist input copy /y app.jar input\app.jar >nul

echo [5/5] RedbookUser.jar (nguoi dung, offline)...
xcopy /q /y bin\*.class "%B%\userbin\" >nul
del "%B%\userbin\AdminUI*.class" "%B%\userbin\AdminTools*.class" "%B%\userbin\QuestionForm*.class" "%B%\userbin\DatasetExporter*.class"
copy /y "%B%\redbook-data.dat" "%B%\userbin\" >nul
if not exist dist\RedbookUser\Images mkdir dist\RedbookUser\Images
if exist dist\RedbookUser\redbook.properties del dist\RedbookUser\redbook.properties
"%JDK%\jar" cfm dist\RedbookUser\RedbookUser.jar manifest.txt -C "%B%\userbin" . || goto :error
xcopy /q /y Images\*.* dist\RedbookUser\Images\ >nul
copy /y config\Redbook.bat dist\RedbookUser\ >nul
copy /y config\HuongDan.txt dist\RedbookUser\ >nul
rmdir /s /q "%B%"
if /i not "%~1"=="release" goto :done

echo [+] Release...
if exist release rmdir /s /q release
mkdir release "%B%\input" "%B%\zip\Redbook"
copy /y dist\RedbookUser\RedbookUser.jar "%B%\input\" >nul
"%JDK%\jpackage" --type app-image --name Redbook --input "%B%\input" --main-jar RedbookUser.jar --main-class Main ^
  --dest "%B%\image" --icon config\redbook.ico --app-version 1.0 --vendor Redbook ^
  --add-modules java.base,java.desktop,java.sql ^
  --jlink-options "--strip-debug --no-man-pages --no-header-files --compress=2" || goto :error
xcopy /e /q /y Images "%B%\image\Redbook\Images\" >nul
copy /y config\HuongDan.txt "%B%\image\Redbook\" >nul
powershell -NoProfile -Command "Compress-Archive -Path '%B%\image\Redbook' -DestinationPath 'release\Redbook-windows.zip'" || goto :error
copy /y dist\RedbookUser\RedbookUser.jar "%B%\zip\Redbook\" >nul
copy /y config\Redbook.bat "%B%\zip\Redbook\" >nul
copy /y config\HuongDan.txt "%B%\zip\Redbook\" >nul
xcopy /e /q /y Images "%B%\zip\Redbook\Images\" >nul
powershell -NoProfile -Command "Compress-Archive -Path '%B%\zip\Redbook' -DestinationPath 'release\Redbook-jar.zip'" || goto :error
rmdir /s /q "%B%"

:done
echo Done.
exit /b 0

:error
echo BUILD FAILED
exit /b 1
