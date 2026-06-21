package dk.mineclub.minecore.platform.common.hooks;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/** Loads optional MineCore hook jars from the plugin data directory. */
public final class CommonHookLoader {
    private static final String METADATA_RESOURCE = "minecore-hook.properties";

    private final Plugin plugin;
    private final List<LoadedHook> loadedHooks = new ArrayList<>();

    public CommonHookLoader(Plugin plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        Path hooksDir = plugin.getDataFolder().toPath().resolve("hooks");
        try {
            Files.createDirectories(hooksDir);
        } catch (IOException ex) {
            plugin.getLogger().warning("Failed to create hooks folder: " + ex.getMessage());
            return;
        }

        List<Path> jars = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(hooksDir, "*.jar")) {
            for (Path path : stream) {
                jars.add(path);
            }
        } catch (IOException ex) {
            plugin.getLogger().warning("Failed to scan hooks folder: " + ex.getMessage());
            return;
        }

        jars.sort(Comparator.comparing(path -> path.getFileName().toString()));
        if (jars.isEmpty()) {
            plugin.getLogger().info("No hook jars found in " + hooksDir.toAbsolutePath());
            return;
        }

        for (Path jar : jars) {
            loadHookJar(jar);
        }
    }

    public void disable() {
        for (int i = loadedHooks.size() - 1; i >= 0; i--) {
            LoadedHook loadedHook = loadedHooks.get(i);
            try {
                loadedHook.disable.invoke(loadedHook.bootstrap);
            } catch (Exception ex) {
                plugin.getLogger()
                        .warning(
                                "Failed to disable hook from "
                                        + loadedHook.jar.getFileName()
                                        + ": "
                                        + ex.getMessage());
            }

            try {
                loadedHook.classLoader.close();
            } catch (IOException ignored) {
                // Best effort shutdown.
            }
        }

        loadedHooks.clear();
    }

    private void loadHookJar(Path jar) {
        try {
            URLClassLoader classLoader =
                    new URLClassLoader(
                            new URL[] {jar.toUri().toURL()}, getClass().getClassLoader());
            Properties metadata = loadMetadata(classLoader);
            String bootstrapClassName = metadata.getProperty("bootstrap-class");
            String hookName = metadata.getProperty("hook-name", jar.getFileName().toString());
            if (bootstrapClassName == null || bootstrapClassName.trim().isEmpty()) {
                plugin.getLogger()
                        .warning(
                                "Skipping "
                                        + jar.getFileName()
                                        + " because it is missing bootstrap-class metadata");
                classLoader.close();
                return;
            }

            Class<?> bootstrapClass = Class.forName(bootstrapClassName, true, classLoader);
            Object bootstrap = bootstrapClass.getDeclaredConstructor().newInstance();
            Method enable = bootstrapClass.getMethod("enable", JavaPlugin.class);
            Method disable = bootstrapClass.getMethod("disable");
            enable.invoke(bootstrap, plugin);
            loadedHooks.add(new LoadedHook(hookName, jar, classLoader, bootstrap, disable));
            plugin.getLogger().info("Loaded hook: " + hookName + " from " + jar.getFileName());
        } catch (ClassNotFoundException ex) {
            plugin.getLogger()
                    .warning(
                            "Skipping "
                                    + jar.getFileName()
                                    + " because bootstrap class was not found");
        } catch (LinkageError ex) {
            plugin.getLogger()
                    .warning(
                            "Skipping "
                                    + jar.getFileName()
                                    + " because it is not compatible with this Java runtime: "
                                    + ex.getMessage());
        } catch (Exception ex) {
            plugin.getLogger()
                    .warning(
                            "Failed to load hook jar "
                                    + jar.getFileName()
                                    + ": "
                                    + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private Properties loadMetadata(URLClassLoader classLoader) throws IOException {
        try (InputStream inputStream = classLoader.getResourceAsStream(METADATA_RESOURCE)) {
            Properties properties = new Properties();
            if (inputStream != null) {
                properties.load(inputStream);
            }
            return properties;
        }
    }

    private static final class LoadedHook {
        private final String name;
        private final Path jar;
        private final URLClassLoader classLoader;
        private final Object bootstrap;
        private final Method disable;

        private LoadedHook(
                String name,
                Path jar,
                URLClassLoader classLoader,
                Object bootstrap,
                Method disable) {
            this.name = name;
            this.jar = jar;
            this.classLoader = classLoader;
            this.bootstrap = bootstrap;
            this.disable = disable;
        }
    }
}
