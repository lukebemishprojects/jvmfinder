package dev.lukebemish.jvmfinder;

import groovy.json.JsonException;
import groovy.json.JsonSlurper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

class Utils {
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
            // TODO: log
            return Optional.empty();
        }
    }

    static Optional<Object> json(URL url) {
        try {
            return Optional.of(new JsonSlurper().parse(url));
        } catch (JsonException ignored) {
            // TODO: log
            return Optional.empty();
        }
    }
}
