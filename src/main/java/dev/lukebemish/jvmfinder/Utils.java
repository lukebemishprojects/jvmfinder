package dev.lukebemish.jvmfinder;

import groovy.json.JsonException;
import groovy.json.JsonSlurper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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

    static Optional<Object> json(URL url) {
        try {
            return Optional.of(new JsonSlurper().parse(url));
        } catch (JsonException ignored) {
            LOGGER.info("Failed to parse expected JSON from URL: {}", url);
            return Optional.empty();
        }
    }
}
