package scanner;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Scans directories for Java source files.
 */
public class FileScanner {
    
    private final List<String> includePatterns;
    private final List<String> excludePatterns;
    
    public FileScanner() {
        this.includePatterns = new ArrayList<>();
        this.excludePatterns = new ArrayList<>();
        
        // Default include pattern
        includePatterns.add("*.java");
        
        // Default exclude patterns
        excludePatterns.add("**/target/**");
        excludePatterns.add("**/build/**");
        excludePatterns.add("**/.git/**");
    }
    
    /**
     * Scan directory recursively for Java files
     */
    public List<File> scanDirectory(String directoryPath) throws IOException {
        Path startPath = Paths.get(directoryPath);
        
        if (!Files.exists(startPath)) {
            throw new IOException("Directory does not exist: " + directoryPath);
        }
        
        if (!Files.isDirectory(startPath)) {
            throw new IOException("Path is not a directory: " + directoryPath);
        }
        
        List<File> javaFiles = new ArrayList<>();
        
        try (Stream<Path> paths = Files.walk(startPath)) {
            paths
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".java"))
                .filter(this::shouldIncludeFile)
                .forEach(path -> javaFiles.add(path.toFile()));
        }
        
        return javaFiles;
    }
    
    /**
     * Scan single file if it's a Java file
     */
    public List<File> scanFile(String filePath) throws IOException {
        List<File> result = new ArrayList<>();
        File file = new File(filePath);
        
        if (!file.exists()) {
            throw new IOException("File does not exist: " + filePath);
        }
        
        if (!file.isFile()) {
            throw new IOException("Path is not a file: " + filePath);
        }
        
        if (file.getName().endsWith(".java") && shouldIncludeFile(file.toPath())) {
            result.add(file);
        }
        
        return result;
    }
    
    /**
     * Check if file should be included based on patterns
     */
    private boolean shouldIncludeFile(Path path) {
        String pathString = path.toString().replace(File.separatorChar, '/');
        
        // Check exclude patterns first
        for (String excludePattern : excludePatterns) {
            if (matchesPattern(pathString, excludePattern)) {
                return false;
            }
        }
        
        // Check include patterns
        for (String includePattern : includePatterns) {
            if (matchesPattern(pathString, includePattern)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Simple pattern matching (supports * wildcards)
     */
    private boolean matchesPattern(String text, String pattern) {
        // Convert glob pattern to regex
        String regex = pattern
            .replace("**", "DOUBLE_STAR")
            .replace("*", "[^/]*")
            .replace("DOUBLE_STAR", ".*")
            .replace(".", "\\.");
        
        return text.matches(regex);
    }
    
    /**
     * Add include pattern
     */
    public void addIncludePattern(String pattern) {
        includePatterns.add(pattern);
    }
    
    /**
     * Add exclude pattern
     */
    public void addExcludePattern(String pattern) {
        excludePatterns.add(pattern);
    }
    
    /**
     * Clear all patterns and set defaults
     */
    public void resetPatterns() {
        includePatterns.clear();
        excludePatterns.clear();
        
        includePatterns.add("*.java");
        excludePatterns.add("**/target/**");
        excludePatterns.add("**/build/**");
        excludePatterns.add("**/.git/**");
    }
    
    /**
     * Get statistics about the scan
     */
    public ScanResult scanWithStats(String directoryPath) throws IOException {
        long startTime = System.currentTimeMillis();
        List<File> files = scanDirectory(directoryPath);
        long endTime = System.currentTimeMillis();
        
        return new ScanResult(files, endTime - startTime, directoryPath);
    }
    
    /**
     * Container for scan results with statistics
     */
    public static class ScanResult {
        private final List<File> files;
        private final long scanTimeMs;
        private final String rootPath;
        
        public ScanResult(List<File> files, long scanTimeMs, String rootPath) {
            this.files = files;
            this.scanTimeMs = scanTimeMs;
            this.rootPath = rootPath;
        }
        
        public List<File> getFiles() {
            return files;
        }
        
        public long getScanTimeMs() {
            return scanTimeMs;
        }
        
        public String getRootPath() {
            return rootPath;
        }
        
        public int getFileCount() {
            return files.size();
        }
        
        @Override
        public String toString() {
            return String.format("ScanResult{files=%d, time=%dms, root='%s'}", 
                files.size(), scanTimeMs, rootPath);
        }
    }
}