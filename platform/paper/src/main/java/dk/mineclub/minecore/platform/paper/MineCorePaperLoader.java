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
    @Override
    public void classloader(PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addRepository(
                new RemoteRepository.Builder(
                                "central",
                                "default",
                                MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR)
                        .build());
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
        resolver.addDependency(new Dependency(new DefaultArtifact(coordinates), null));
    }

    private static void loadLibrariesFromResource(MavenLibraryResolver resolver) {
        Properties properties = new Properties();
        try (InputStream input =
                MineCorePaperLoader.class
                        .getClassLoader()
                        .getResourceAsStream("minecore-loader-libraries.properties")) {
            if (input == null) {
                throw new IllegalStateException(
                        "Missing minecore-loader-libraries.properties resource");
            }

            properties.load(input);
            String libraries = properties.getProperty("libraries", "").trim();
            if (libraries.isEmpty()) {
                throw new IllegalStateException(
                        "No libraries configured in minecore-loader-libraries.properties");
            }

            Arrays.stream(libraries.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(coordinates -> addDependency(resolver, coordinates));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load Paper runtime libraries", ex);
        }
    }
}
