echo "[x] Instalando apktool & java"
sudo apt install apktool openjdk-17-jdk
echo "[x] Realizando algumas tarefas"
apktool d templates/app.apk
apktool d templates/app-tor.apk
rm -rf templates/app
rm -rf templates/app-tor
mv app templates
mv app-tor templates
echo "[x] Pronto!"
