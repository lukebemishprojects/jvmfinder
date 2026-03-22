package dev.lukebemish.jvmfinder;

import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.jvm.toolchain.JvmImplementation;
import org.gradle.jvm.toolchain.JvmVendorSpec;
import org.gradle.platform.Architecture;
import org.gradle.platform.OperatingSystem;

import java.util.Optional;

record JvmFinderRequest(JvmImplementation impl, JvmVendorSpec vendor, JavaLanguageVersion version,
                        Optional<Boolean> nativeImage, Architecture arch, OperatingSystem os) {
}
