package dev.lukebemish.jvmfinder;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import org.gradle.jvm.toolchain.JavaToolchainDownload;
import org.gradle.jvm.toolchain.JavaToolchainRequest;
import org.gradle.jvm.toolchain.JavaToolchainResolver;
import org.gradle.jvm.toolchain.JvmImplementation;
import org.gradle.jvm.toolchain.JvmVendorSpec;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class JetbrainsToolchainResolver implements JavaToolchainResolver {
    private static final URL JETBRAINS_JVM_INDEX = Utils.url("https://download.jetbrains.com/jdk/feed/v1/jdks.json.xz");

    record JvmList(
        List<JvmInfo> jdks
    ) {}

    record JvmInfo(
        String vendor,
        String product,
        String flavour,
        boolean preview,
        @SerializedName("jdk_version_major") int jdkVersionMajor,
        List<JvmPackage> packages
    ) {}

    record JvmPackage(
        String os,
        String arch,
        String url,
        @SerializedName("package_type") String packageType
    ) {}

    private static int vendorPrecedence(String vendor, String product) {
        var vendorString = (vendor + " " + product);
        if (JvmVendorSpec.ADOPTIUM.matches(vendorString)) {
            return 1;
        }
        if (JvmVendorSpec.ADOPTOPENJDK.matches(vendorString)) {
            return 2;
        }
        if (JvmVendorSpec.AMAZON.matches(vendorString)) {
            return 3;
        }
        if (JvmVendorSpec.APPLE.matches(vendorString)) {
            return 4;
        }
        if (JvmVendorSpec.AZUL.matches(vendorString)) {
            return 5;
        }
        if (JvmVendorSpec.BELLSOFT.matches(vendorString)) {
            return 6;
        }
        if (JvmVendorSpec.GRAAL_VM.matches(vendorString)) {
            return 7;
        }
        if (JvmVendorSpec.HEWLETT_PACKARD.matches(vendorString)) {
            return 8;
        }
        if (JvmVendorSpec.IBM.matches(vendorString)) {
            return 9;
        }
        if (JvmVendorSpec.JETBRAINS.matches(vendorString)) {
            return 10;
        }
        if (JvmVendorSpec.MICROSOFT.matches(vendorString)) {
            return 11;
        }
        if (JvmVendorSpec.ORACLE.matches(vendorString)) {
            return 12;
        }
        if (JvmVendorSpec.SAP.matches(vendorString)) {
            return 13;
        }
        if (JvmVendorSpec.TENCENT.matches(vendorString)) {
            return 14;
        }
        return Integer.MAX_VALUE;
    }

    @Override
    public Optional<JavaToolchainDownload> resolve(JavaToolchainRequest request) {
        var impl = request.getJavaToolchainSpec().getImplementation().getOrElse(JvmImplementation.VENDOR_SPECIFIC);
        var vendor = request.getJavaToolchainSpec().getVendor().map(Optional::of).getOrElse(Optional.empty());
        var version = request.getJavaToolchainSpec().getLanguageVersion().map(Optional::of).getOrElse(Optional.empty());
        var nativeImage = request.getJavaToolchainSpec().getNativeImageCapable().map(Optional::of).getOrElse(Optional.empty());
        var arch = request.getBuildPlatform().getArchitecture();
        var os = request.getBuildPlatform().getOperatingSystem();

        var infos = Utils.json(JETBRAINS_JVM_INDEX, new TypeToken<JvmList>() {}, Utils.DecompressionMode.XZ);
        if (infos.isEmpty()) {
            return Optional.empty();
        }

        return infos.get().jdks().stream()
            .filter(jdk -> !jdk.preview())
            .filter(jdk -> {
                if (jdk.product().toLowerCase(Locale.ROOT).contains("early-access")) {
                    // Skip early-access builds
                    return false;
                } else {
                    return true;
                }
            })
            .filter(jdk -> version.isEmpty() || jdk.jdkVersionMajor() == version.get().asInt())
            .filter(jdk -> vendor.isEmpty() || vendor.get().matches(jdk.vendor() + " " + jdk.product()))
            .filter(jdk -> {
                if (impl == JvmImplementation.J9) {
                    // Assume openj9-ness is encoded in the flavour
                    return jdk.flavour().toLowerCase(Locale.ROOT).contains("openj9");
                } else {
                    return true;
                }
            })
            .filter(jdk -> {
                // Assume all GraalVM images here have native-image, and no others
                if (nativeImage.isPresent()) {
                    return JvmVendorSpec.GRAAL_VM.matches(jdk.vendor() + " " + jdk.product()) == nativeImage.get();
                } else {
                    return true;
                }
            })
            .sorted(Comparator.comparing(JvmInfo::jdkVersionMajor).reversed().thenComparing(jdk -> vendorPrecedence(jdk.vendor(), jdk.product())))
            .flatMap(jdk -> jdk.packages().stream())
            .filter(pkg -> switch (os) {
                case FREE_BSD, UNIX, SOLARIS -> false;
                case LINUX -> pkg.os().toLowerCase(Locale.ROOT).equals("linux");
                case MAC_OS -> pkg.os().toLowerCase(Locale.ROOT).equals("macos");
                case WINDOWS -> pkg.os().toLowerCase(Locale.ROOT).equals("windows");
            })
            .filter(pkg -> switch (arch) {
                case X86 -> false;
                case AARCH64 -> pkg.arch().toLowerCase(Locale.ROOT).equals("aarch64");
                case X86_64 -> pkg.arch().toLowerCase(Locale.ROOT).equals("x86_64");
            })
            .map(JvmPackage::url)
            .map(Utils::url)
            .flatMap(url -> Utils.available(url).stream())
            .map(Utils::uri)
            .map(JavaToolchainDownload::fromUri)
            .findFirst();
    }
}
