package connection;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class UrlReader {

    public static JSONObject readJsonFromUrl(String url) {
        try (InputStream is = new URL(url).openStream()) {
            var rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String jsonText = rd.lines().collect(Collectors.joining());
            return jsonText.isEmpty() ? null : new JSONObject(jsonText);
        } catch (IOException | JSONException e) {
            System.err.println("Failed to get json");
            return null;
        }
    }
}
