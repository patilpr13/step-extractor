package generator;

import model.StepLibrary;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * Generates YAML output from StepLibrary data.
 * Simple YAML generation without external dependencies.
 */
public class YamlGenerator {
    
    private static final String DEFAULT_OUTPUT_FILE = "step_library.yaml";
    
    /**
     * Generate YAML content from StepLibrary
     */
    public String generateYaml(StepLibrary stepLibrary) {
        StringBuilder yaml = new StringBuilder();
        
        // Header comment
        yaml.append("# Generated Cucumber step library from Java source files\n");
        yaml.append("# This file is compatible with the HLR-to-Test CLI tool\n\n");
        
        // Given steps
        yaml.append("given_steps:\n");
        appendStepList(yaml, stepLibrary.getGivenSteps());
        yaml.append("\n");
        
        // When steps
        yaml.append("when_steps:\n");
        appendStepList(yaml, stepLibrary.getWhenSteps());
        yaml.append("\n");
        
        // Then steps
        yaml.append("then_steps:\n");
        appendStepList(yaml, stepLibrary.getThenSteps());
        yaml.append("\n");
        
        // Parameter types documentation
        if (!stepLibrary.getParameterTypes().isEmpty()) {
            yaml.append("parameter_types:\n");
            appendParameterTypes(yaml, stepLibrary.getParameterTypes());
            yaml.append("\n");
        }
        
        // Metadata
        yaml.append("metadata:\n");
        appendMetadata(yaml, stepLibrary.getMetadata());
        
        return yaml.toString();
    }
    
    /**
     * Append list of steps to YAML
     */
    private void appendStepList(StringBuilder yaml, List<String> steps) {
        if (steps.isEmpty()) {
            yaml.append("  []\n");
            return;
        }
        
        for (String step : steps) {
            yaml.append("  - \"").append(escapeYamlString(step)).append("\"\n");
        }
    }
    
    /**
     * Append parameter types to YAML
     */
    private void appendParameterTypes(StringBuilder yaml, Map<String, String> parameterTypes) {
        for (Map.Entry<String, String> entry : parameterTypes.entrySet()) {
            yaml.append("  ").append(entry.getKey()).append(": \"")
                .append(escapeYamlString(entry.getValue())).append("\"\n");
        }
    }
    
    /**
     * Append metadata to YAML
     */
    private void appendMetadata(StringBuilder yaml, Map<String, Object> metadata) {
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            yaml.append("  ").append(key).append(": ");
            
            if (value instanceof List) {
                appendListValue(yaml, (List<?>) value);
            } else if (value instanceof String) {
                yaml.append("\"").append(escapeYamlString((String) value)).append("\"");
            } else {
                yaml.append(value.toString());
            }
            
            yaml.append("\n");
        }
    }
    
    /**
     * Append list value to YAML
     */
    private void appendListValue(StringBuilder yaml, List<?> list) {
        if (list.isEmpty()) {
            yaml.append("[]");
            return;
        }
        
        yaml.append("\n");
        for (Object item : list) {
            yaml.append("    - \"").append(escapeYamlString(item.toString())).append("\"\n");
        }
        // Remove the last newline to prevent extra spacing
        if (yaml.length() > 0 && yaml.charAt(yaml.length() - 1) == '\n') {
            yaml.setLength(yaml.length() - 1);
        }
    }
    
    /**
     * Escape special characters in YAML strings
     */
    private String escapeYamlString(String str) {
        if (str == null) return "";
        
        return str
            .replace("\\", "\\\\")  // Escape backslashes first
            .replace("\"", "\\\"")  // Escape quotes
            .replace("\n", "\\n")   // Escape newlines
            .replace("\r", "\\r")   // Escape carriage returns
            .replace("\t", "\\t");  // Escape tabs
    }
    
    /**
     * Write YAML content to file
     */
    public void writeToFile(StepLibrary stepLibrary, String outputPath) throws IOException {
        String yamlContent = generateYaml(stepLibrary);
        
        File outputFile = new File(outputPath);
        
        // Create parent directories if they don't exist
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IOException("Failed to create parent directories for: " + outputPath);
            }
        }
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            writer.print(yamlContent);
        }
    }
    
    /**
     * Write YAML content to default file in current directory
     */
    public void writeToDefaultFile(StepLibrary stepLibrary) throws IOException {
        writeToFile(stepLibrary, DEFAULT_OUTPUT_FILE);
    }
    
    /**
     * Validate that the step library is not empty
     */
    public boolean validateStepLibrary(StepLibrary stepLibrary) {
        if (stepLibrary == null) {
            return false;
        }
        
        return !stepLibrary.isEmpty();
    }
    
    /**
     * Generate minimal YAML for compatibility testing
     */
    public String generateMinimalYaml(StepLibrary stepLibrary) {
        StringBuilder yaml = new StringBuilder();
        
        yaml.append("given_steps:\n");
        if (stepLibrary.getGivenSteps().isEmpty()) {
            yaml.append("  []\n");
        } else {
            for (String step : stepLibrary.getGivenSteps()) {
                yaml.append("  - \"").append(escapeYamlString(step)).append("\"\n");
            }
        }
        
        yaml.append("when_steps:\n");
        if (stepLibrary.getWhenSteps().isEmpty()) {
            yaml.append("  []\n");
        } else {
            for (String step : stepLibrary.getWhenSteps()) {
                yaml.append("  - \"").append(escapeYamlString(step)).append("\"\n");
            }
        }
        
        yaml.append("then_steps:\n");
        if (stepLibrary.getThenSteps().isEmpty()) {
            yaml.append("  []\n");
        } else {
            for (String step : stepLibrary.getThenSteps()) {
                yaml.append("  - \"").append(escapeYamlString(step)).append("\"\n");
            }
        }
        
        return yaml.toString();
    }
    
    /**
     * Print statistics about the generated YAML
     */
    public void printGenerationStats(StepLibrary stepLibrary) {
        System.out.println("YAML Generation Statistics:");
        System.out.println("  Given steps: " + stepLibrary.getGivenSteps().size());
        System.out.println("  When steps: " + stepLibrary.getWhenSteps().size());
        System.out.println("  Then steps: " + stepLibrary.getThenSteps().size());
        System.out.println("  Total steps: " + stepLibrary.getTotalStepCount());
        System.out.println("  Parameter types: " + stepLibrary.getParameterTypes().size());
        
        @SuppressWarnings("unchecked")
        List<String> sourceFiles = (List<String>) stepLibrary.getMetadata().get("source_files");
        if (sourceFiles != null) {
            System.out.println("  Source files: " + sourceFiles.size());
        }
    }
}