package dev.lukebemish.jvmfinder;

import org.gradle.jvm.toolchain.JavaToolchainDownload;
import org.gradle.jvm.toolchain.JvmImplementation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

class Adoptium {
    static Optional<JavaToolchainDownload> resolve(JvmFinderRequest requestObj) {
        if (!JvmImplementation.VENDOR_SPECIFIC.equals(requestObj.impl()) || requestObj.nativeImage()) {
            return Optional.empty();
        }
        var osString = switch (requestObj.os()) {
            case FREE_BSD, UNIX -> null;
            case LINUX -> "linux";
            case MAC_OS -> "mac";
            case SOLARIS -> "solaris";
            case WINDOWS -> "windows";
        };
        var archString = switch (requestObj.arch()) {
            case AARCH64 -> "aarch64";
            case X86 -> "x86";
            case X86_64 -> "x64";
        };
        if (osString == null) {
            return Optional.empty();
        }
        var url = String.format(
                "https://api.adoptium.net/v3/assets/latest/%s/hotspot?architecture=%s&image_type=jdk&os=%s&vendor=eclipse",
                requestObj.version().asInt(),
                archString, osString
        );
        var response = (List<?>) Utils.json(Utils.url(url));
        if (response.isEmpty()) {
            return Optional.empty();
        }
        var binary = ((Map<?, ?>) response.get(0)).get("binary");
        var packages = ((Map<?, ?>) binary).get("package");
        var link = (String) ((Map<?, ?>) packages).get("link");
        return Optional.of(JavaToolchainDownload.fromUri(Utils.uri(link)));
    }
}
