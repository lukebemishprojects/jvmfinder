package dev.lukebemish.jvmfinder;

import org.gradle.jvm.toolchain.JavaToolchainDownload;
import org.gradle.jvm.toolchain.JvmImplementation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

class AdoptiumMarketplace {
    private static Map<String, String> vendorImplMap = Map.of(
        "adoptium", "hotspot",
        "ibm", "openj9",
        "microsoft", "hotspot",
        "alibaba", "hotspot",
        "azul", "hotspot"
    );

    static Optional<JavaToolchainDownload> resolve(JvmFinderRequest requestObj, String vendor) {
        if (requestObj.nativeImage().orElse(false)) {
            return Optional.empty();
        }
        var jvmImpl = "";
        if (JvmImplementation.J9.equals(requestObj.impl())) {
            jvmImpl = "openj9";
        } else {
            jvmImpl = vendorImplMap.get(vendor);
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
            "https://marketplace-api.adoptium.net/v1/assets/latest/%s/%s/%s",
            vendor, requestObj.version().asInt(), jvmImpl
        );
        var maybeResponse = Utils.json(Utils.url(url));
        if (maybeResponse.isEmpty()) {
            return Optional.empty();
        }
        var response = (List<?>) maybeResponse.get();
        if (response.isEmpty()) {
            return Optional.empty();
        }
        for (var value : response) {
            var map = (Map<?, ?>) value;
            var binary = map.get("binary");
            if (binary == null) {
                continue;
            }
            var os = ((Map<?, ?>) binary).get("os");
            var arch = ((Map<?, ?>) binary).get("architecture");
            var imageType = ((Map<?, ?>) binary).get("image_type");
            if (osString.equals(os) && archString.equals(arch) && "jdk".equals(imageType)) {
                var packages = ((Map<?, ?>) binary).get("package");
                var link = (String) ((Map<?, ?>) packages).get("link");
                return Utils.available(Utils.url(link)).map(Utils::uri).map(JavaToolchainDownload::fromUri);
            }
        }
        return Optional.empty();
    }
}
