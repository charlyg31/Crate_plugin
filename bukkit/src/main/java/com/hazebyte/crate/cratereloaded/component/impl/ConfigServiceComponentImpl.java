package com.hazebyte.crate.cratereloaded.component.impl;

import com.hazebyte.crate.cratereloaded.component.ConfigServiceComponent;
import com.hazebyte.crate.cratereloaded.model.Config;
import com.hazebyte.utils.file.FileHelper;
import com.hazebyte.utils.tree.BaseTrie;
import com.hazebyte.utils.tree.Trie;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import javax.inject.Inject;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigServiceComponentImpl implements ConfigServiceComponent {

    
    private final JavaPlugin plugin;

    
    private final Trie<Config> configs;

    @Inject
    public ConfigServiceComponentImpl(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configs = new BaseTrie<>();

        try {
            createDefaultConfigs();
            insertDefaultConfigs();
        } catch (IOException ex) {
            throw new IllegalArgumentException("The plugin has failed to initialize the config service.");
        }
    }

    @Override
    public Config addConfig(File file) {
        Config config = new Config(plugin, file);
        String relativePath =
                plugin.getDataFolder().toURI().relativize(file.toURI()).toString();
        plugin.getLogger().finer(String.format("Inserting to config manager with path [%s]", relativePath));
        configs.insert(relativePath, config);
        return config;
    }

    @Override
    public Config getConfigWithName(String fileName) {
        return configs.searchExact(fileName);
    }

    @Override
    public Collection<Config> getConfigsStartingWith(String searchIndex) {
        return configs.startsWith(searchIndex);
    }

    private FileFilter isDirectoryOrConfig() {
        return (file) -> file.isDirectory() || file.getName().endsWith(".yml");
    }

    private void insertDefaultConfigs() {
        Set<File> configFiles = FileHelper.getFiles(plugin.getDataFolder(), isDirectoryOrConfig());
        plugin.getLogger().finer(String.format("Config found [%s] configs", configFiles.size()));
        for (File file : configFiles) {
            addConfig(file);
        }
    }

    private void createDefaultConfigs() throws IOException {
        File locationFile = new File(plugin.getDataFolder(), "location.yml");
        File crateFolder = new File(plugin.getDataFolder(), "crates");
        Predicate<File> fileExists = (file) -> file.exists();
        Predicate<File> parentFolderExistAndNotEmpty =
                (file) -> file.getParentFile().exists() && file.getParentFile().listFiles().length != 0;
        copyConfigFromZip("config.yml", new File(plugin.getDataFolder(), "config.yml"), fileExists);
        copyConfigFromZip("crate.yml", new File(crateFolder, "crate.yml"), fileExists.or(parentFolderExistAndNotEmpty));
        if (!locationFile.exists()) locationFile.createNewFile();
    }

    /**
     * This creates a default config from the source JAR file. This takes in a relative source path
     * and transfers the file to the targetPath.
     *
     * @param sourcePath The relative path from the JAR.
     * @param targetPath The target file.
     * @param skip This determines whether we allow the creation of the default config.
     * @throws IOException if the source file does not exist.
     */
    private void copyConfigFromZip(String sourcePath, File targetPath, Predicate<File> skip) throws IOException {
        if (skip.test(targetPath)) {
            return;
        }

        targetPath.getParentFile().mkdirs();

        try (InputStream in = plugin.getClass().getClassLoader().getResourceAsStream(sourcePath);
                OutputStream out = new FileOutputStream(targetPath)) {
            plugin.getLogger().finest("createFromDefault: " + sourcePath);
            if (in == null) {
                throw new IOException("Resource not found: " + sourcePath);
            }

            byte[] buf = new byte[1024];
            int length;
            while ((length = in.read(buf)) > 0) {
                out.write(buf, 0, length);
            }
        } catch (IOException e) {
            plugin.getLogger()
                    .log(
                            java.util.logging.Level.WARNING,
                            String.format("Unable to create file from default: %s", sourcePath),
                            e);
            throw e;
        }
    }
}
