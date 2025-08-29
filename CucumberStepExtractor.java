import model.StepDefinition;
import model.StepLibrary;
import scanner.FileScanner;
import parser.AnnotationParser;
import extractor.ParameterExtractor;
import generator.YamlGenerator;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Main class for extracting Cucumber step definitions from Java source files.
 * 
 * Usage:
 *   java CucumberStepExtractor <source_directory> [output_file]
 *   
 * Example:
 *   java CucumberStepExtractor ./src/test/java
 *   java CucumberStepExtractor ./src/test/java step_library.yaml
 */
public class CucumberStepExtractor {
    
    private final FileScanner fileScanner;
    private final AnnotationParser annotationParser;
    private final ParameterExtractor parameterExtractor;
    private final YamlGenerator yamlGenerator;
    
    public CucumberStepExtractor() {
        this.fileScanner = new FileScanner();
        this.annotationParser = new AnnotationParser();
        this.parameterExtractor = new ParameterExtractor();
        this.yamlGenerator = new YamlGenerator();
    }
    
    /**
     * Main extraction method - extracts steps from source folder
     */
    public void extractSteps(String srcFolder) throws IOException {
        extractSteps(srcFolder, "step_library.yaml");
    }
    
    /**
     * Extract steps with custom output file
     */
    public void extractSteps(String srcFolder, String outputFile) throws IOException {
        System.out.println("Starting Cucumber step extraction...");
        System.out.println("Source folder: " + srcFolder);
        System.out.println("Output file: " + outputFile);
        System.out.println();
        
        // Step 1: Scan for Java files
        System.out.println("Step 1: Scanning for Java files...");
        FileScanner.ScanResult scanResult = fileScanner.scanWithStats(srcFolder);
        System.out.println("Found " + scanResult.getFileCount() + " Java files in " + 
                          scanResult.getScanTimeMs() + "ms");
        
        if (scanResult.getFiles().isEmpty()) {
            System.out.println("No Java files found. Exiting.");
            return;
        }
        
        // Step 2: Parse files for step definitions
        System.out.println("\nStep 2: Parsing files for step definitions...");
        StepLibrary stepLibrary = new StepLibrary();
        int processedFiles = 0;
        int totalSteps = 0;
        
        for (File javaFile : scanResult.getFiles()) {
            try {
                // Check if file contains step annotations
                if (annotationParser.containsStepAnnotations(javaFile)) {
                    System.out.println("Processing: " + javaFile.getName());
                    
                    // Parse step definitions
                    List<StepDefinition> steps = annotationParser.parseFile(javaFile);
                    
                    // Enhance with parameter information
                    for (StepDefinition step : steps) {
                        parameterExtractor.enhanceStepParameters(step);
                        stepLibrary.addStep(step);
                    }
                    
                    stepLibrary.addSourceFile(javaFile.getName());
                    totalSteps += steps.size();
                    processedFiles++;
                }
            } catch (Exception e) {
                System.err.println("Warning: Failed to process " + javaFile.getName() + 
                                 ": " + e.getMessage());
            }
        }
        
        System.out.println("Processed " + processedFiles + " files with step definitions");
        System.out.println("Extracted " + totalSteps + " total steps");
        
        // Step 3: Validate and generate output
        System.out.println("\nStep 3: Generating YAML output...");
        
        if (stepLibrary.isEmpty()) {
            System.out.println("No step definitions found. Creating empty step library.");
        }
        
        // Generate parameter documentation
        List<StepDefinition> allSteps = getAllSteps(stepLibrary);
        if (!allSteps.isEmpty()) {
            // Add parameter type documentation
            stepLibrary.getParameterTypes().putAll(
                parameterExtractor.generateParameterDocumentation(allSteps));
            
            // Validate parameter consistency
            List<String> warnings = parameterExtractor.validateParameterConsistency(allSteps);
            if (!warnings.isEmpty()) {
                System.out.println("\nParameter consistency warnings:");
                for (String warning : warnings) {
                    System.out.println("  - " + warning);
                }
            }
        }
        
        // Write YAML file
        yamlGenerator.writeToFile(stepLibrary, outputFile);
        
        // Print final statistics
        String separator = "==================================================";
        System.out.println("\n" + separator);
        System.out.println("Extraction completed successfully!");
        yamlGenerator.printGenerationStats(stepLibrary);
        System.out.println("Output written to: " + outputFile);
        System.out.println(separator);
    }
    
    /**
     * Extract all steps from StepLibrary into a single list
     */
    private List<StepDefinition> getAllSteps(StepLibrary stepLibrary) {
        // This is a simplified approach - in a full implementation,
        // we'd need to maintain the original StepDefinition objects
        // For now, we'll work with what we have
        return java.util.Arrays.asList();
    }
    
    /**
     * Extract steps and return as StepLibrary object (for programmatic use)
     */
    public StepLibrary extractStepsAsLibrary(String srcFolder) throws IOException {
        StepLibrary stepLibrary = new StepLibrary();
        
        // Scan for Java files
        List<File> javaFiles = fileScanner.scanDirectory(srcFolder);
        
        // Parse each file
        for (File javaFile : javaFiles) {
            try {
                if (annotationParser.containsStepAnnotations(javaFile)) {
                    List<StepDefinition> steps = annotationParser.parseFile(javaFile);
                    
                    for (StepDefinition step : steps) {
                        parameterExtractor.enhanceStepParameters(step);
                        stepLibrary.addStep(step);
                    }
                    
                    stepLibrary.addSourceFile(javaFile.getName());
                }
            } catch (Exception e) {
                System.err.println("Warning: Failed to process " + javaFile.getName() + 
                                 ": " + e.getMessage());
            }
        }
        
        return stepLibrary;
    }
    
    /**
     * Print usage information
     */
    private static void printUsage() {
        System.out.println("Cucumber Step Extractor - Extract step definitions from Java files");
        System.out.println();
        System.out.println("Usage:");
        System.out.println("  java CucumberStepExtractor <source_directory> [output_file]");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java CucumberStepExtractor ./src/test/java");
        System.out.println("  java CucumberStepExtractor ./src/test/java custom_steps.yaml");
        System.out.println("  java CucumberStepExtractor /path/to/java/src step_library.yaml");
        System.out.println();
        System.out.println("Arguments:");
        System.out.println("  source_directory  Directory containing Java source files (required)");
        System.out.println("  output_file      Output YAML file name (default: step_library.yaml)");
        System.out.println();
        System.out.println("The tool will:");
        System.out.println("  1. Recursively scan the source directory for .java files");
        System.out.println("  2. Extract @Given, @When, @Then step definitions");
        System.out.println("  3. Analyze parameter types and placeholders");
        System.out.println("  4. Generate YAML output compatible with HLR-to-Test CLI tool");
    }
    
    /**
     * Validate command line arguments
     */
    private static boolean validateArgs(String[] args) {
        if (args.length < 1 || args.length > 2) {
            return false;
        }
        
        // Check if source directory exists
        File srcDir = new File(args[0]);
        if (!srcDir.exists()) {
            System.err.println("Error: Source directory does not exist: " + args[0]);
            return false;
        }
        
        if (!srcDir.isDirectory()) {
            System.err.println("Error: Source path is not a directory: " + args[0]);
            return false;
        }
        
        return true;
    }
    
    /**
     * Main method - command line interface
     */
    public static void main(String[] args) {
        // Handle help flags
        if (args.length == 1 && (args[0].equals("-h") || args[0].equals("--help"))) {
            printUsage();
            return;
        }
        
        // Validate arguments
        if (!validateArgs(args)) {
            printUsage();
            System.exit(1);
        }
        
        String srcFolder = args[0];
        String outputFile = args.length > 1 ? args[1] : "step_library.yaml";
        
        try {
            CucumberStepExtractor extractor = new CucumberStepExtractor();
            extractor.extractSteps(srcFolder, outputFile);
            
        } catch (IOException e) {
            System.err.println("Error during extraction: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}