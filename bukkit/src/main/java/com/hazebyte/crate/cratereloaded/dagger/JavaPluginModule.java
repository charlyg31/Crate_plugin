package com.hazebyte.crate.cratereloaded.dagger;

import com.hazebyte.crate.api.ServerVersion;
import com.hazebyte.crate.cratereloaded.CorePlugin;
import com.hazebyte.crate.cratereloaded.util.ConfigConstants;
import com.hazebyte.crate.exception.ExceptionHandler;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Module
public class JavaPluginModule {

    @Provides
    @Singleton
    JavaPlugin provideJavaPlugin() {
        return (JavaPlugin) Bukkit.getPluginManager().getPlugin(ConfigConstants.PLUGIN_NAME);
    }

    @Provides
    @Singleton
    CorePlugin provideCorePlugin(JavaPlugin javaPlugin) {
        return (CorePlugin) javaPlugin;
    }

    @Provides
    @Singleton
    ServerVersion provideCurrentServerVersion(CorePlugin plugin) {
        return plugin.getServerVersion();
    }

    @Provides
    @Singleton
    ExceptionHandler provideExceptionHandler(JavaPlugin plugin) {
        return new ExceptionHandler(plugin.getLogger());
    }
}
