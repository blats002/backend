package com.divroll.bucket;

import java.io.IOException;
import java.util.Arrays;

//import com.joyent.manta.client.MantaClient;
//import com.joyent.manta.config.ConfigContext;
//import com.joyent.manta.config.StandardConfigContext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class SimpleClient {
    public static void main(String... args) throws IOException {
//        ConfigContext config = new StandardConfigContext()
//                .setMantaURL("https://us-east.manta.joyent.com")
//                // If there is no subuser, then just use the account name
//                .setMantaUser("divroll")
//                .setMantaKeyPath("src/test/java/data/id_rsa")
//                .setMantaKeyId("24:82:cf:ad:a7:a4:62:c4:c8:7e:1e:68:94:7a:13:1b");
//
//        try (MantaClient client = new MantaClient(config)) {
//            String mantaFile = "/divroll/stor/foo";
//
//            // Print out every line from file streamed real-time from Manta
//            try (InputStream is = client.getAsInputStream(mantaFile);
//                 Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name())) {
//
//                while (scanner.hasNextLine()) {
//                    System.out.println(scanner.nextLine());
//                }
//            }
//
//            // Load file into memory as a string directly from Manta
//            String data = client.getAsString(mantaFile);
//            System.out.println(data);
//        }
    }
}
