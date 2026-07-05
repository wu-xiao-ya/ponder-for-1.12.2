package net.createmod.ponder.foundation.external.scan;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import net.createmod.ponder.Ponder;
import net.minecraftforge.fml.common.Loader;

/**
 * Reusable scanning service for external ponder scene JSON files.
 *
 * <p>Manages a registry of explicitly queued source paths and combines them
 * with auto-scanned directories under the game directory. All collected JSON
 * files are deduplicated by canonical path.</p>
 *
 * <p>This class is safe for concurrent access via its synchronized methods.</p>
 */
public final class ExternalPonderSceneScanner {

    private static final Map<String, File> SCRIPT_JSON_SOURCES = new LinkedHashMap<String, File>();

    private ExternalPonderSceneScanner() {
    }

    /**
     * Register a file or directory path for later scanning.
     *
     * <p>The path is resolved first as an absolute/relative file, then against
     * several well-known subdirectories of the game directory
     * ({@code scripts/}, {@code scripts/ponder}, {@code config/ponder}, {@code ponder}).</p>
     *
     * @param path the raw path to a JSON file or directory containing JSON files
     */
    public static synchronized void rememberScriptJson(String path) {
        File resolved = resolveQueuedPath(path);
        if (resolved == null) {
            Ponder.LOGGER.warn("Skipping external ponder json path '{}': file or directory not found", path);
            return;
        }

        try {
            SCRIPT_JSON_SOURCES.put(resolved.getCanonicalPath(), resolved);
        } catch (IOException exception) {
            SCRIPT_JSON_SOURCES.put(resolved.getAbsolutePath(), resolved);
        }
    }

    /**
     * Clear all previously remembered source paths.
     */
    public static synchronized void clearRememberedScriptJson() {
        SCRIPT_JSON_SOURCES.clear();
    }

    /**
     * Collect all discoverable external ponder JSON files and scan metadata.
     *
     * <p>The scan combines auto-scanned directories under the game directory
     * with individually queued paths. Results are deduplicated by canonical
     * path, preserving insertion order.</p>
     *
     * @return the scan result, never {@code null}
     */
    public static ExternalScanResult collectScanResult() {
        Set<String> seenPaths = new LinkedHashSet<String>();
        List<File> files = new ArrayList<File>();
        List<File> scannedRoots = new ArrayList<File>();
        List<File> skippedRoots = new ArrayList<File>();

        for (File root : getAutoScanRoots()) {
            if (addJsonFiles(root, files, seenPaths)) {
                scannedRoots.add(root);
            } else {
                skippedRoots.add(root);
            }
        }

        Collection<File> queuedSources;
        synchronized (ExternalPonderSceneScanner.class) {
            queuedSources = new ArrayList<File>(SCRIPT_JSON_SOURCES.values());
        }
        for (File source : queuedSources) {
            if (addJsonFiles(source, files, seenPaths)) {
                scannedRoots.add(source);
            } else {
                skippedRoots.add(source);
            }
        }

        return new ExternalScanResult(files, scannedRoots, skippedRoots);
    }

    /**
     * Collect all discoverable external ponder JSON files.
     *
     * @return a deduplicated list of JSON files, never {@code null}
     */
    public static List<File> collectJsonFiles() {
        return collectScanResult().files();
    }

    /**
     * Determine the auto-scan root directories under the game directory.
     */
    private static List<File> getAutoScanRoots() {
        File gameDir = getGameDir();
        if (gameDir == null) {
            return Collections.emptyList();
        }

        List<File> roots = new ArrayList<File>();
        roots.add(new File(gameDir, "scripts/ponder"));
        roots.add(new File(gameDir, "ponder"));
        roots.add(new File(gameDir, "config/ponder"));
        return roots;
    }

    /**
     * Recursively add JSON files from a source file or directory.
     *
     * <p>If {@code source} is a single JSON file it is added directly.
     * Directories are walked recursively. Each file is deduplicated via
     * {@link #rememberFile(List, Set, File)}.</p>
     */
    private static boolean addJsonFiles(File source, List<File> files, Set<String> seenPaths) {
        if (source == null || !source.exists()) {
            return false;
        }

        if (source.isFile()) {
            if (source.getName().toLowerCase(Locale.ROOT).endsWith(".json")) {
                rememberFile(files, seenPaths, source);
            }
            return true;
        }

        try (Stream<Path> walk = Files.walk(source.toPath())) {
            walk.filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".json"))
                .forEach(path -> rememberFile(files, seenPaths, path.toFile()));
            return true;
        } catch (IOException exception) {
            Ponder.LOGGER.warn("Failed to scan external ponder scene directory {}", source, exception);
            return false;
        }
    }

    /**
     * Add a file to the result list if its canonical path has not been seen before.
     */
    private static void rememberFile(List<File> files, Set<String> seenPaths, File file) {
        try {
            String canonicalPath = file.getCanonicalPath();
            if (seenPaths.add(canonicalPath)) {
                files.add(file);
            }
        } catch (IOException exception) {
            String absolutePath = file.getAbsolutePath();
            if (seenPaths.add(absolutePath)) {
                files.add(file);
            }
        }
    }

    /**
     * Resolve a raw path by checking the filesystem directly and then against
     * well-known subdirectories of the game directory.
     *
     * @param rawPath the path to resolve
     * @return the resolved {@link File}, or {@code null} if not found
     */
    private static File resolveQueuedPath(String rawPath) {
        if (rawPath == null || rawPath.trim().isEmpty()) {
            return null;
        }

        File direct = new File(rawPath);
        if (direct.exists()) {
            return direct;
        }

        File gameDir = getGameDir();
        if (gameDir == null) {
            return null;
        }

        File[] candidates = new File[] {
            new File(gameDir, rawPath),
            new File(new File(gameDir, "scripts"), rawPath),
            new File(new File(gameDir, "scripts/ponder"), rawPath),
            new File(new File(gameDir, "config/ponder"), rawPath),
            new File(new File(gameDir, "ponder"), rawPath)
        };

        for (File candidate : candidates) {
            if (candidate.exists()) {
                return candidate;
            }
        }

        return null;
    }

    /**
     * Get the Minecraft game directory via the Forge Loader config dir.
     *
     * @return the game directory, or {@code null} if not available
     */
    private static File getGameDir() {
        File configDir = Loader.instance().getConfigDir();
        return configDir == null ? null : configDir.getParentFile();
    }
}
