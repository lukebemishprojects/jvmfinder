package dev.lukebemish.jvmfinder;

import groovy.json.JsonSlurper;

import java.net.URI;
import java.net.URL;
import java.util.Map;

class Utils {
    static URL url(String url) {
        try {
            return new URL(url);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    static URI uri(String uri) {
        try {
            return new URI(uri);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    static Object json(URL url) {
        return new JsonSlurper().parse(url);
    }
}
