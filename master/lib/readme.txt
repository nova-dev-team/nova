
ceil_conf.rb ---> configure ceil<-->nova
ceil/server/server_settings ---> ceil settings, eg, FTP/NFS location,...

common config is in CEIL/server/server_settings.rb

    SERVER_KEY_STORE_PATH <-----key storage folder

file structure of ceil by NFS

    on server exports 3 folder
    /scripts ro to all

    /config  ro to all 

    /share   rw to all

    SERVER_KEY_DISPATCH_PATH = '/share'
    and 
    SERVER_CONFIG_STORE_PATH = '/config'


file structure of ceil by FTP(Carrier)
    no safety gurantee..
    ftp is set on server
    for anonymous login
    ftproot
       |--packages
       |--keys
       
set SERVER_KEY_DISPATCH_PATH to ftproot/keys on server

    
    

