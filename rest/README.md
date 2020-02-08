# Note: To make writing of certificates to root directories

```
sudo chmod -R 757 /etc/ssl/certs/keys/
sudo chmod -R 757 /etc/ssl/certs/
sudo chmod -R 757 /etc/nginx/conf.d/
```

# It is also needed to create a bash script to be able to restart nginx after adding neew certificates

```
#!/bin/bash
echo "checking nginx config..."
#sudo nginx -t | grep 'successful' &> /dev/null
if sudo nginx -t 2>&1| grep -q 'successful'; then
        echo "restaring nginx..."
        sudo systemctl restart nginx
        echo "restarted nginx"
fi
```