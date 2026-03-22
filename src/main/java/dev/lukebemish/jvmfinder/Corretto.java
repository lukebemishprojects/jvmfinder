package dev.lukebemish.jvmfinder;

import org.gradle.jvm.toolchain.JavaToolchainDownload;
import org.gradle.jvm.toolchain.JvmImplementation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

class Corretto {
    static Optional<JavaToolchainDownload> resolve(JvmFinderRequest requestObj) {
        if (!JvmImplementation.VENDOR_SPECIFIC.equals(requestObj.impl()) || requestObj.nativeImage().orElse(false)) {
            return Optional.empty();
        }
        var osString = switch (requestObj.os()) {
            case FREE_BSD, UNIX, SOLARIS -> null;
            case LINUX -> "linux";
            case MAC_OS -> "macos";
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
        var indexmapUrl = "https://corretto.github.io/corretto-downloads/latest_links/indexmap_with_checksum.json";
        var maybeIndexMap = Utils.json(Utils.url(indexmapUrl));
        if (maybeIndexMap.isEmpty()) {
            return Optional.empty();
        }
        var indexMap = (Map<?, ?>) maybeIndexMap.get();
        var forOs = (Map<?, ?>) indexMap.get(osString);
        var forArch = forOs == null ? null : (Map<?, ?>) forOs.get(archString);
        var jdk = forArch == null ? null : (Map<?, ?>) forArch.get("jdk");
        var forRelease = jdk == null ? null : (Map<?, ?>) jdk.get(String.valueOf(requestObj.version().asInt()));
        if (forRelease == null) {
            return Optional.empty();
        }
        var release = (Map<?, ?>) forRelease.get("zip");
        if (release == null) {
            release = (Map<?, ?>) forRelease.get("tar.gz");
            if (release == null) {
                return Optional.empty();
            }
        }
        var resource = (String) release.get("resource");
        var link = "https://corretto.aws"+resource;
        return Utils.available(Utils.url(link)).map(Utils::uri).map(JavaToolchainDownload::fromUri);
    }
}
