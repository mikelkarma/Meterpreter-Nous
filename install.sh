echo "[x] Instalando apktool & java"
sudo apt install apktool openjdk-17-jdk
echo "[x] Realizando algumas tarefas"
apktool d templates/app.apk
mv app templates
echo "[x] Pronto!"
