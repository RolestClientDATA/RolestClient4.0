package minecraft.rolest.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Paths;

public class ClientDataReader {

    private static JsonObject clientData = null;
    private static long lastModified = 0;

    public static void loadClientData() {
        try {
            String filePath = Paths.get("C:", "Rolest Launcher", "launcher_config.json").toString();
            java.io.File file = new java.io.File(filePath);

            if (file.exists() && file.lastModified() > lastModified) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                JsonParser parser = new JsonParser();
                clientData = parser.parse(reader).getAsJsonObject();
                reader.close();
                lastModified = file.lastModified();
            }
        } catch (Exception e) {
            System.out.println("Failed to load client data: " + e.getMessage());
        }
    }

    public static String getUsername() {
        loadClientData();
        if (clientData != null && clientData.has("username")) {
            return clientData.get("username").getAsString();
        }
        return "Rolest";
    }

    public static String getUserId() {
        loadClientData();
        if (clientData != null && clientData.has("user_id")) {
            return clientData.get("user_id").getAsString();
        }
        return "N/A";
    }

    public static String getTitle() {
        loadClientData();
        if (clientData != null && clientData.has("title")) {
            return clientData.get("title").getAsString();
        }
        return "Игрок";
    }
}