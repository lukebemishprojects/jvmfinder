package dev.lukebemish.jvmfinder;

import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;
import org.gradle.jvm.toolchain.JvmToolchainManagement;

public abstract class JvmFinderConventionPlugin implements Plugin<Settings> {
    @Override
    public void apply(Settings settings) {
        settings.getPluginManager().apply(JvmFinderPlugin.class);
        
        settings.toolchainManagement(toolchainManagement -> {
            var jvm = toolchainManagement.getExtensions().getByType(JvmToolchainManagement.class);
            jvm.getJavaRepositories().repository("jvmfinder", repo -> {
                repo.getResolverClass().set(JvmFinderToolchainResolver.class);
            });
        });
    }
}
