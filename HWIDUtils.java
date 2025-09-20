package minecraft.rolest.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
public class HWIDUtils {

    public static String getHWID() {
        StringBuilder hwid = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec("wmic csproduct get uuid");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.length() > 0 && !line.startsWith("UUID")) {
                    hwid.append(line.trim());
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return hwid.toString();
    }
}