package dk.mineclub.minecore.platform.common.hooks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Method;
import java.nio.file.Path;
import org.junit.Test;

public class SkriptHookDownloaderTest {
    private final SkriptHookDownloader downloader =
            new SkriptHookDownloader(null) {
                @Override
                protected String findSkriptPluginVersion(Path hooksDir) {
                    return "2.17.1";
                }
            };

    private String select(int javaVersion, String version) throws Exception {
        Method method =
                SkriptHookDownloader.class.getDeclaredMethod(
                        "findBestSkriptVersion", int.class, String.class);
        method.setAccessible(true);
        return (String) method.invoke(downloader, javaVersion, version);
    }

    @Test
    public void selects217ForSupportedJavaVersions() throws Exception {
        for (int javaVersion : new int[] {17, 21, 25}) {
            assertEquals("skript217", select(javaVersion, "2.17.1"));
        }
    }

    @Test
    public void preservesOlderHooksAndRejectsUnsupportedVersions() throws Exception {
        assertEquals("skript216", select(21, "2.16.2"));
        assertEquals("skrip215", select(21, "2.15.4"));
        assertNull(select(21, "2.18.0"));
        assertNull(select(8, "2.17.1"));
        assertNull(select(11, "2.17.1"));
    }
}
