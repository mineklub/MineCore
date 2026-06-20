package dk.mineclub.minecore.platform.paper;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

public class MineCorePaperLoader implements PluginLoader {
    private static final String GENERATED_RESOURCE =
            "minecore-loader-libraries-generated.properties";
    private static final String DEFAULT_MINECORE_DEPENDENCY =
            "com.github.mineklub:MineCore:firsttry-SNAPSHOT";

    @Override
    public void classloader(PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addRepository(
                new RemoteRepository.Builder(
                                "central",
                                "default",
                                "https://maven-central.storage-download.googleapis.com/maven2")
                        .build());
        resolver.addRepository(
                new RemoteRepository.Builder("jitpack", "default", "https://jitpack.io").build());
        resolver.addRepository(
                new RemoteRepository.Builder(
                                "paper",
                                "default",
                                "https://repo.papermc.io/repository/maven-public/")
                        .build());

        loadLibrariesFromResource(resolver);

        classpathBuilder.addLibrary(resolver);
    }

    private static void addDependency(MavenLibraryResolver resolver, String coordinates) {
        System.out.println("Adding dependency: " + coordinates);
        resolver.addDependency(new Dependency(new DefaultArtifact(coordinates), null));
    }

    private static void loadLibrariesFromResource(MavenLibraryResolver resolver) {
        Properties properties = new Properties();
        try (InputStream input = openPropertiesStream()) {
            if (input == null) {
                throw new IllegalStateException("Cannot open properties file");
            }

            properties.load(input);
            String libraries = properties.getProperty("libraries", "").trim();
            if (libraries.isEmpty()) {
                throw new RuntimeException("No libraries found!");
            } else {
                Arrays.stream(libraries.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .forEach(coordinates -> addDependency(resolver, coordinates));
            }

            String minecoreDependency =
                    properties
                            .getProperty("minecoreDependency", DEFAULT_MINECORE_DEPENDENCY)
                            .trim();
            if (minecoreDependency.isEmpty()) {
                minecoreDependency = DEFAULT_MINECORE_DEPENDENCY;
            }
            addDependency(resolver, minecoreDependency);
        } catch (Exception ex) {
            throw new RuntimeException(
                    "Failed to load libraries from resource: " + GENERATED_RESOURCE, ex);
        }
    }

    private static InputStream openPropertiesStream() {
        ClassLoader classLoader = MineCorePaperLoader.class.getClassLoader();
        return classLoader.getResourceAsStream(GENERATED_RESOURCE);
    }
}
