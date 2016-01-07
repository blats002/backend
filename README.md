# Ubuntu setup (testing host parsing)

# set in /etc/hosts
# 127.0.0.2	demo.localhost.com
# add additional subdomain if necessary
# then

iptables -t nat -A OUTPUT -d 127.0.0.2 -p tcp --dport 80 -j REDIRECT --to-port 8080

or

# Windows setup

# set in \WINDOWS\system32\drivers\etc
# 127.0.0.2	demo.localhost.com
# add additional subdomain if necessary
# then

netsh interface portproxy add v4tov4 listenport=80 listenaddress=127.0.0.2 connectport=8080 connectaddress=127.0.0.1