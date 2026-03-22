package dev.lukebemish.jvmfinder;

import org.gradle.jvm.toolchain.JavaToolchainDownload;
import org.gradle.jvm.toolchain.JvmImplementation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

class Azul {
    static Optional<JavaToolchainDownload> resolve(JvmFinderRequest requestObj) {
        if (!JvmImplementation.VENDOR_SPECIFIC.equals(requestObj.impl()) || requestObj.nativeImage().orElse(false)) {
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
                "https://api.azul.com/metadata/v1/zulu/packages/?java_version=%s&arch=%s&os=%s&archive_type=zip&java_package_type=jdk&latest=true",
                requestObj.version().asInt(),
                archString, osString
        );
        var maybeResponse = Utils.json(Utils.url(url));
        if (maybeResponse.isEmpty()) {
            return Optional.empty();
        }
        var response = (List<?>) maybeResponse.get();
        if (response.isEmpty()) {
            return Optional.empty();
        }
        var link = (String) ((Map<?, ?>) response.get(0)).get("download_url");
        return Utils.available(Utils.url(link)).map(Utils::uri).map(JavaToolchainDownload::fromUri);
    }
}
