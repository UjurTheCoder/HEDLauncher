package org.example.launcher.net;

import java.io.InputStreamReader;
import java.net.URL;

public class HttpUtil {

    public static String get(String url) {
        try (InputStreamReader reader =
                     new InputStreamReader(new URL(url).openStream())) {

            StringBuilder sb = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                sb.append((char) c);
            }
            return sb.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
