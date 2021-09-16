package pl.fratik.chinczyk.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
            File file = new File("./config.json");
            if (!file.exists()) {
                Files.createFile(file.toPath());
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(gson.toJson(new Config()).getBytes(StandardCharsets.UTF_8));
                }
                log.info("Plik konfiguracyjny utworzony, skonfiguruj serwer!");
                System.exit(2);
                return;
            }
            Config.instance = gson.fromJson(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8), Config.class);
        } catch (Exception e) {
            log.error("Nie udało się odczytać/utworzyć konfiguracji!", e);
            System.exit(1);
            return;
        }
    }
}
