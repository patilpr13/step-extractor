import model.StepDefinition;
import model.StepLibrary;
import parser.AnnotationParser;
import extractor.ParameterExtractor;
import generator.YamlGenerator;

import java.io.File;
import java.util.List;

/**
 * Test class to validate the step extractor with HmiAirworkSteps.java
 */
public class TestExtractor {
    
    public static void main(String[] args) {
        try {
            System.out.println("Testing Cucumber Step Extractor...");
            
            // Initialize components
            AnnotationParser parser = new AnnotationParser();
            ParameterExtractor paramExtractor = new ParameterExtractor();
            YamlGenerator yamlGenerator = new YamlGenerator();
            
            // Test file
            File testFile = new File("../HmiAirworkSteps.java");
            
            if (!testFile.exists()) {
                System.err.println("Test file not found: " + testFile.getAbsolutePath());
                return;
            }
            
            System.out.println("Processing file: " + testFile.getName());
            
            // Check if file contains step annotations
            boolean hasSteps = parser.containsStepAnnotations(testFile);
            System.out.println("Contains step annotations: " + hasSteps);
            
            if (hasSteps) {
                // Parse step definitions
                List<StepDefinition> steps = parser.parseFile(testFile);
                System.out.println("Found " + steps.size() + " step definitions");
                
                // Print each step
                System.out.println("\nExtracted steps:");
                for (StepDefinition step : steps) {
                    System.out.println("  " + step.getType() + ": " + step.getPattern());
                    
                    // Enhance with parameter information
                    paramExtractor.enhanceStepParameters(step);
                    
                    if (!step.getParameters().isEmpty()) {
                        System.out.println("    Parameters: " + step.getParameters().size());
                        step.getParameters().forEach(param -> 
                            System.out.println("      - " + param.getJavaType() + " -> " + param.getPlaceholder()));
                    }
                }
                
                // Create step library and generate YAML
                StepLibrary stepLibrary = new StepLibrary();
                for (StepDefinition step : steps) {
                    stepLibrary.addStep(step);
                }
                stepLibrary.addSourceFile(testFile.getName());
                
                // Generate YAML
                System.out.println("\nGenerating YAML...");
                String yaml = yamlGenerator.generateYaml(stepLibrary);
                
                // Write to file
                yamlGenerator.writeToFile(stepLibrary, "test_extracted_steps.yaml");
                
                System.out.println("YAML generated successfully!");
                System.out.println("\nPreview of generated YAML:");
                String separator = "==================================================";
                System.out.println(separator);
                System.out.println(yaml.substring(0, Math.min(yaml.length(), 800)) + "...");
                System.out.println(separator);
                
                // Print statistics
                yamlGenerator.printGenerationStats(stepLibrary);
                
            } else {
                System.out.println("No step annotations found in file.");
            }
            
        } catch (Exception e) {
            System.err.println("Error during testing: " + e.getMessage());
            e.printStackTrace();
        }
    }
}