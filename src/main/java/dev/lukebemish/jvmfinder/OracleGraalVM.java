package dev.lukebemish.jvmfinder;

import org.gradle.jvm.toolchain.JavaToolchainDownload;
import org.gradle.jvm.toolchain.JvmImplementation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

class OracleGraalVM {
    private static final Logger LOGGER = LoggerFactory.getLogger(OracleGraalVM.class);

    static Optional<JavaToolchainDownload> resolve(JvmFinderRequest requestObj) {
        if (!JvmImplementation.VENDOR_SPECIFIC.equals(requestObj.impl()) || !requestObj.nativeImage().orElse(true)) {
            return Optional.empty();
        }
        var allDownloads = "https://www.oracle.com/a/tech/docs/graalvm-downloads.json";
        var maybeIndexMap = Utils.json(Utils.url(allDownloads));
        if (maybeIndexMap.isEmpty()) {
            return Optional.empty();
        }
        var releases = ((Map<?, ?>) maybeIndexMap.get()).values().stream()
            .filter(m -> "Oracle GraalVM".equals(((Map<?, ?>) m).get("Title")))
            .map(m -> (Map<?, ?>) m)
            .flatMap(m -> ((Map<?, ?>) m.get("Releases")).values().stream())
            .map(m -> (Map<?, ?>) m)
            .map(m -> (String) m.get("JSON File"))
            .toList();
        var osString = switch (requestObj.os()) {
            case FREE_BSD, UNIX, SOLARIS -> null;
            case LINUX -> "Linux";
            case MAC_OS -> "macOS";
            case WINDOWS -> "Windows";
        };
        var archString = switch (requestObj.arch()) {
            case AARCH64 -> "aarch64";
            case X86 -> "x86";
            case X86_64 -> "x64";
        };
        for (var release : releases) {
            var url = "https://www.oracle.com"+release;
            var maybeReleaseMap = Utils.json(Utils.url(url));
            if (maybeReleaseMap.isEmpty()) {
                continue;
            }
            var packages = ((Map<?, ?>) maybeReleaseMap.get()).get("Packages");
            var core = packages == null ? null : ((Map<?, ?>) packages).get("Core");
            var files = core == null ? null : (Map<?, ?>) ((Map<?, ?>) core).get("Files");
            if (files == null) {
                LOGGER.info("Oracle GraalVM JSON file not structured as expected, may not be able to provision GraalVM JDK.");
                continue;
            }
            for (var fileEntry : files.entrySet()) {
                var key = (String) fileEntry.getKey();
                if (key.endsWith("-"+osString+"-"+archString+"-"+requestObj.version().asInt())) {
                    var link = (String) ((Map<?, ?>) fileEntry.getValue()).get("File");
                    return Utils.available(Utils.url(link)).map(Utils::uri).map(JavaToolchainDownload::fromUri);
                }
            }
        }
        return Optional.empty();
    }
}
