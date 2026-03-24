package dev.lukebemish.jvmfinder;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import groovy.json.JsonException;
import groovy.json.JsonSlurper;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

class Utils {
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    static URL url(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    static URI uri(URL url) {
        try {
            return url.toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    static Optional<URL> available(URL uri) {
        try {
            HttpURLConnection connection = (HttpURLConnection) uri.openConnection();
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            return (200 <= responseCode && responseCode < 300) ? Optional.of(uri) : Optional.empty();
        } catch (IOException e) {
            LOGGER.info("Failed to connect to URL: {}", uri, e);
            return Optional.empty();
        }
    }

    enum DecompressionMode {
        NONE,
        XZ
    }

    static <T> Optional<T> json(URL url, TypeToken<T> type, DecompressionMode decompressionMode) {
        try (var stream = switch (decompressionMode) {
                 case NONE -> url.openStream();
                 case XZ -> XZCompressorInputStream.builder().setURI(uri(url)).get();
             };
             var reader = new BufferedReader(new InputStreamReader(stream))) {
            var gson = new GsonBuilder().create();

            return Optional.ofNullable(gson.fromJson(reader, type));
        } catch (JsonSyntaxException e) {
            LOGGER.info("Failed to parse expected JSON from URL: {}", url, e);
            return Optional.empty();
        } catch (IOException e) {
            LOGGER.info("Failed to read expected JSON from URL: {}", url, e);
            return Optional.empty();
        }
    }

    static Optional<Object> json(URL url) {
        try {
            return Optional.of(new JsonSlurper().parse(url));
        } catch (JsonException ignored) {
            LOGGER.info("Failed to parse expected JSON from URL: {}", url);
            return Optional.empty();
        }
    }
}
