package dev.lukebemish.jvmfinder;

import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;
import org.gradle.jvm.toolchain.JavaToolchainResolverRegistry;

import javax.inject.Inject;

public abstract class JvmFinderJetbrainsPlugin implements Plugin<Settings> {
    @Inject
    protected abstract JavaToolchainResolverRegistry getToolchainResolverRegistry();

    public void apply(Settings settings) {
        settings.getPluginManager().apply("jvm-toolchain-management");

        JavaToolchainResolverRegistry registry = getToolchainResolverRegistry();
        registry.register(JetbrainsToolchainResolver.class);
    }
}
