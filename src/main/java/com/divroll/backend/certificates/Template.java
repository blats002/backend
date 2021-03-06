/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright (C) 2019  Kerby Martino
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Other licenses:
 * -----------------------------------------------------------------------------
 * Commercial licenses for this work are available. These replace the above
 * AGPL 3.0 and offer limited warranties, support, maintenance, and commercial
 * deployments.
 *
 * For more information, please email: support@divroll.com
 *
 */

package com.divroll.backend.certificates;

public class Template {
    private static String TEMPLATE1 = "#\n" +
            "# __DOMAIN__ HTTPS server configuration\n" +
            "#\n" +
            "\n" +
            "server {\n" +
            "    listen       443;\n" +
            "    server_name  __DOMAIN__;\n" +
            "\n" +
            "    ssl                  on;\n" +
            "    ssl_certificate      /etc/ssl/certs/__DOMAIN__.crt;\n" +
            "    ssl_certificate_key  /etc/ssl/certs/__DOMAIN__.key;\n" +
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
            "                        proxy_pass http://localhost:8080/;" +
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
    private static String TEMPLATE = "#\n" +
            "# __DOMAIN__ HTTPS server configuration\n" +
            "#\n" +
            "\n" +
            "server {\n" +
            "    listen       443;\n" +
            "    server_name  __DOMAIN__;\n" +
            "\n" +
            "    ssl                  on;\n" +
            "    ssl_certificate      /etc/ssl/certs/__DOMAIN__.crt;\n" +
            "    ssl_certificate_key  /etc/ssl/certs/__DOMAIN__.key;\n" +
            "\n" +
            "    ssl_session_timeout  5m;\n" +
            "\n" +
            "    ssl_protocols TLSv1 TLSv1.1 TLSv1.2;\n" +
            "    ssl_ciphers ECDH+AESGCM:DH+AESGCM:ECDH+AES256:DH+AES256:ECDH+AES128:DH+AES:ECDH+3DES:DH+3DES:RSA+AESGCM:RSA+AES:RSA+3DES:!aNULL:!MD5:!DSS;\n" +
            "    ssl_prefer_server_ciphers   on;\n" +
            "    ssl_session_cache shared:SSL:10m;\n" +
            "                \n" +
            "    access_log /var/log/nginx/localhost.access_log combined;\n" +
            "    error_log /var/log/nginx/localhost.error_log info;\n" +
            "    proxy_temp_path /etc/nginx/tmp/;\n" +
            "    error_page   500 502 503 504  /50x.html;\n" +
            "\n" +
            "    location = /50x.html {\n" +
            "                        root   html;\n" +
            "               }\n" +
            "\n" +
            "    location / {\n" +
            "     set $upstream_name common;\n" +
            "#include conf.d/ssl.upstreams.inc;\n" +
            "                        #proxy_pass http://$upstream_name;\n" +
            "                        proxy_pass http://localhost:8080/;\n" +
            "\t\t\tproxy_next_upstream error;\n" +
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
            "}";

    public static String createFromTemplate(String domain) {
        String result = TEMPLATE.replaceAll("__DOMAIN__", domain);
        return result;
    }
}
