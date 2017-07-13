package com..bucket;

public class Template {
    private static String TEMPLATE = "#\n" +
            "# __DOMAIN__ HTTPS server configuration\n" +
            "#\n" +
            "\n" +
            "server {\n" +
            "    listen       443;\n" +
            "    server_name  __DOMAIN__;\n" +
            "\n" +
            "    ssl                  on;\n" +
            "    ssl_certificate      /var/lib/nginx/ssl/__DOMAIN__.crt;\n" +
            "    ssl_certificate_key  /var/lib/nginx/ssl/__DOMAIN__.key;\n" +
            "\n" +
            "    ssl_session_timeout  5m;\n" +
            "\n" +
            "    ssl_protocols TLSv1 TLSv1.1 TLSv1.2;\n" +
            "    ssl_ciphers ECDH+AESGCM:DH+AESGCM:ECDH+AES256:DH+AES256:ECDH+AES128:DH+AES:ECDH+3DES:DH+3DES:RSA+AESGCM:RSA+AES:RSA+3DES:!aNULL:!MD5:!DSS;\n" +
            "    ssl_prefer_server_ciphers   on;\n" +
            "    ssl_session_cache shared:SSL:10m;\n" +
            "                \n" +
            "    access_log /var/log/nginx/localhost.access_log main;\n" +
            "    error_log /var/log/nginx/localhost.error_log info;\n" +
            "    proxy_temp_path /var/nginx/tmp/;\n" +
            "    error_page   500 502 503 504  /50x.html;\n" +
            "\n" +
            "    location = /50x.html {\n" +
            "                        root   html;\n" +
            "               }\n" +
            "\n" +
            "    location / {\n" +
            "     set $upstream_name common;\n" +
            "#include conf.d/ssl.upstreams.inc;\n" +
            "\n" +
            "                        proxy_pass http://$upstream_name;\n" +
            "                        proxy_next_upstream error;\n" +
            "                        proxy_http_version 1.1;\n" +
            "                        proxy_set_header Upgrade $http_upgrade;\n" +
            "                        proxy_set_header Connection \"upgrade\";\n" +
            "                        proxy_set_header Host $host;\n" +
            "                        proxy_set_header X-Real-IP $remote_addr;\n" +
            "                        proxy_set_header X-Host $http_host;\n" +
            "                        proxy_set_header X-Forwarded-For $http_x_forwarded_for;\n" +
            "                        proxy_set_header X-URI $uri;\n" +
            "                        proxy_set_header X-ARGS $args;\n" +
            "                        proxy_set_header Refer $http_refer;\n" +
            "                        proxy_set_header X-Forwarded-Proto $scheme;\n" +
            "                }\n" +
            "}\n" +
            "\n" +
            "\n";
    public static String createFromTemplate(String domain) {
        String result = TEMPLATE.replaceAll("__DOMAIN__", domain);
        return result;
    }
}
