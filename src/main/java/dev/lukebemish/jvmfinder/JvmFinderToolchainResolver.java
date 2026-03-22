package dev.lukebemish.jvmfinder;

import org.gradle.jvm.toolchain.JavaToolchainDownload;
import org.gradle.jvm.toolchain.JavaToolchainRequest;
import org.gradle.jvm.toolchain.JavaToolchainResolver;
import org.gradle.jvm.toolchain.JvmImplementation;

import java.util.Optional;

public abstract class JvmFinderToolchainResolver implements JavaToolchainResolver {
    private static final String ADOPTIUM = "adoptium";
    private static final String AZUL = "azul";
    private static final String GRAALVM = "graalvm";
    private static final String IBM = "ibm";
    private static final String CORRETTO = "corretto";
    private static final String MICROSOFT = "microsoft";
    private static final String ALIBABA = "alibaba";

    @Override
    public Optional<JavaToolchainDownload> resolve(JavaToolchainRequest request) {
        var impl = request.getJavaToolchainSpec().getImplementation().getOrElse(JvmImplementation.VENDOR_SPECIFIC);
        var vendor = request.getJavaToolchainSpec().getVendor().get();
        var version = request.getJavaToolchainSpec().getLanguageVersion().get();
        Optional<Boolean> nativeImage = request.getJavaToolchainSpec().getNativeImageCapable().map(Optional::of).getOrElse(Optional.empty());
        var arch = request.getBuildPlatform().getArchitecture();
        var os = request.getBuildPlatform().getOperatingSystem();
        var requestObj = new JvmFinderRequest(impl, vendor, version, nativeImage, arch, os);
        var match = Optional.<JavaToolchainDownload>empty();
        if (vendor.matches(ADOPTIUM)) {
            match = AdoptiumMarketplace.resolve(requestObj, ADOPTIUM);
        }
        if (match.isEmpty() && vendor.matches(CORRETTO)) {
            match = Corretto.resolve(requestObj);
        }
        if (match.isEmpty() && vendor.matches(AZUL)) {
            match = Azul.resolve(requestObj);
        }
        if (match.isEmpty() && vendor.matches(GRAALVM)) {
            match = OracleGraalVM.resolve(requestObj);
        }
        if (match.isEmpty() && vendor.matches(IBM)) {
            match = AdoptiumMarketplace.resolve(requestObj, IBM);
        }
        if (match.isEmpty() && vendor.matches(MICROSOFT)) {
            match = AdoptiumMarketplace.resolve(requestObj, MICROSOFT);
        }
        if (match.isEmpty() && vendor.matches(ALIBABA)) {
            match = AdoptiumMarketplace.resolve(requestObj, ALIBABA);
        }
        return match;
    }
}
