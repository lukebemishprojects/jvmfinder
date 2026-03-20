package dev.lukebemish.jvmfinder;

import org.gradle.jvm.toolchain.JavaToolchainDownload;
import org.gradle.jvm.toolchain.JavaToolchainRequest;
import org.gradle.jvm.toolchain.JavaToolchainResolver;
import org.gradle.jvm.toolchain.JvmImplementation;
import org.gradle.jvm.toolchain.JvmVendorSpec;

import java.util.Optional;

public abstract class JvmFinderToolchainResolver implements JavaToolchainResolver {
    private static final String ANY_VENDOR = "any vendor";
    private static final String ADOPTIUM = "adoptium";
    
    @Override
    public Optional<JavaToolchainDownload> resolve(JavaToolchainRequest request) {
        var impl = request.getJavaToolchainSpec().getImplementation().getOrElse(JvmImplementation.VENDOR_SPECIFIC);
        var vendor = request.getJavaToolchainSpec().getVendor().getOrElse(JvmVendorSpec.of(ANY_VENDOR));
        var version = request.getJavaToolchainSpec().getLanguageVersion().get();
        boolean nativeImage = request.getJavaToolchainSpec().getNativeImageCapable().getOrElse(false);
        var arch = request.getBuildPlatform().getArchitecture();
        var os = request.getBuildPlatform().getOperatingSystem();
        var requestObj = new JvmFinderRequest(impl, vendor, version, nativeImage, arch, os);
        if (vendor.matches(ANY_VENDOR)) {
            return Adoptium.resolve(requestObj);
        }
        if (vendor.matches(ADOPTIUM)) {
            return Adoptium.resolve(requestObj);
        }
        return Optional.empty();
    }
}
