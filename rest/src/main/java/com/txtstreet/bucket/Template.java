package com..bucket;

public class Template {
    private static String TEMPLATE = "#\n" +
            "# __DOMAIN__ HTTPS server configuration\n" +
            "#\n" +
            "\n" +
            "server {\n" +
            "\tlisten 443 ssl;\n" +
            "    server_name __DOMAIN__;\n" +
            "    ssl_certificate /var/lib/nginx/ssl/__DOMAIN__.crt;\n" +
            "    ssl_certificate_key /var/lib/nginx/ssl/__DOMAIN__.key;\n" +
            "\taccess_log /var/log/nginx/localhost.access_log main;\n" +
            "    error_log /var/log/nginx/localhost.error_log info;\n" +
            "    proxy_temp_path /var/nginx/tmp/;\n" +
            "    error_page   500 502 503 504  /50x.html;\n" +
            "\n" +
            "    location = /50x.html {\n" +
            "                        root   html;\n" +
            "               }\n" +
            "\n" +
            "    location / {\n" +
            "\t\tset $upstream_name common;\n" +
            "\t\tinclude conf.d/ssl.upstreams.inc;\n" +
            "        proxy_pass http://$upstream_name;\n" +
            "        proxy_next_upstream error;\n" +
            "        proxy_http_version 1.1;\n" +
            "        proxy_set_header Upgrade $http_upgrade;\n" +
            "        proxy_set_header Connection \"upgrade\";\n" +
            "        proxy_set_header Host $host;\n" +
            "        proxy_set_header X-Real-IP $remote_addr;\n" +
            "        proxy_set_header X-Host $http_host;\n" +
            "        proxy_set_header X-Forwarded-For $http_x_forwarded_for;\n" +
            "        proxy_set_header X-URI $uri;\n" +
            "        proxy_set_header X-ARGS $args;\n" +
            "        proxy_set_header Refer $http_refer;\n" +
            "        proxy_set_header X-Forwarded-Proto $scheme;\n" +
            "\t}\n" +
            "}\n" +
            "\n" +
            "\n";
    public static String createFromTemplate(String domain) {
        String result = TEMPLATE.replaceAll("__DOMAIN__", domain);
        return result;
    }
}
