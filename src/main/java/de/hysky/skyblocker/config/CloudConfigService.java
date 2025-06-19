package de.hysky.skyblocker.config;

import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.utils.ApiAuthentication;
import de.hysky.skyblocker.utils.Http;
import org.slf4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

/**
 * Handles uploading and downloading configuration data from the cloud.
 *
 * <p>This implementation uses HTTP calls to a remote service. The actual
 * service URL should be customised by server owners. Authentication is done
 * using the same token used for other Skyblocker API calls.</p>
 */
public class CloudConfigService {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String BASE_URL = "https://example.com/skyblocker/config";
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(Redirect.NORMAL)
            .version(Version.HTTP_2)
            .build();

    public static String downloadConfig(UUID uuid) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(BASE_URL + "/" + uuid))
                    .header("Accept", "application/json")
                    .header("User-Agent", Http.USER_AGENT);
            String token = ApiAuthentication.getToken();
            if (token != null) builder.header("Authorization", "Bearer " + token);

            HttpResponse<String> response = CLIENT.send(builder.build(), BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() == 200) {
                return response.body();
            }
        } catch (Exception e) {
            LOGGER.error("[Skyblocker] Failed to download cloud config", e);
        }
        return null;
    }

    public static void uploadConfig(UUID uuid, String json) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .PUT(BodyPublishers.ofString(json))
                    .uri(URI.create(BASE_URL + "/" + uuid))
                    .header("Content-Type", "application/json")
                    .header("User-Agent", Http.USER_AGENT);
            String token = ApiAuthentication.getToken();
            if (token != null) builder.header("Authorization", "Bearer " + token);

            CLIENT.send(builder.build(), BodyHandlers.discarding());
        } catch (Exception e) {
            LOGGER.error("[Skyblocker] Failed to upload cloud config", e);
        }
    }
}
