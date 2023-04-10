curl http://refresh.rola.info/refresh?user=skyescn_1&country=us&state=&city=
curl -v --socks5 gate2.rola.info:2042 -U skyescn_1:209209us http://lumtest.com/myip.json

nohup java -Xmx1536m -Xms1536m -jar /opt/LPM-2.2.0.jar --spring.profiles.active=prod >/dev/null 2>&1 &

nohup proxy sps -p :50001 -a hotkingda:209209us -P socks5://skyescn_1222-ip-73.31.39.101:209209us@gate2.rola.info:2042 &

proxy sps -p :5000 -a hotkingda:209209us -P socks5://skyescn_80225-ip-92.119.177.50:209209us@gate2.rola.info:2042


nohup java -Xmx1024m -jar /opt/mail-4.0.2.jar >/dev/null 2>&1 &
nohup java -Xmx1024m -Xms1024m -jar /opt/mail-ex-1.2.4.jar --server.port=80 >/dev/null 2>&1 &
nohup java -Xmx5120m -jar /opt/LPM-1.0.0.final.jar  --spring.profiles.active=prod >/dev/null 2>&1 &

nohup java -jar  xxl-job-admin-2.3.1.jar  >/dev/null 2>&1 &




多用户  
ip范围  200个
设备前缀
设备号录入 excel导入   设备名称、设备id、设备类型、用户id