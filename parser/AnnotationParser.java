package parser;

import model.StepDefinition;
import model.ParameterInfo;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses Java source files to extract Cucumber step annotations.
 */
public class AnnotationParser {
    
    // Regex patterns for different annotation types
    private static final Pattern GIVEN_PATTERN = Pattern.compile(
        "@Given\\s*\\(\\s*\"([^\"\\\\]*(\\\\.[^\"\\\\]*)*)\"\\s*\\)", 
        Pattern.MULTILINE);
    
    private static final Pattern WHEN_PATTERN = Pattern.compile(
        "@When\\s*\\(\\s*\"([^\"\\\\]*(\\\\.[^\"\\\\]*)*)\"\\s*\\)", 
        Pattern.MULTILINE);
    
    private static final Pattern THEN_PATTERN = Pattern.compile(
        "@Then\\s*\\(\\s*\"([^\"\\\\]*(\\\\.[^\"\\\\]*)*)\"\\s*\\)", 
        Pattern.MULTILINE);
    
    // Pattern to find method signature after annotation
    private static final Pattern METHOD_PATTERN = Pattern.compile(
        "public\\s+(?:final\\s+)?void\\s+(\\w+)\\s*\\(([^)]*)\\)\\s*\\{",
        Pattern.MULTILINE);
    
    // Pattern to extract parameter information
    private static final Pattern PARAM_PATTERN = Pattern.compile(
        "(?:final\\s+)?(\\w+(?:<[^>]+>)?)\\s+(\\w+)");
    
    // Pattern to find import statements
    private static final Pattern IMPORT_PATTERN = Pattern.compile(
        "import\\s+([\\w.]+);");
    
    /**
     * Parse a Java file and extract step definitions
     */
    public List<StepDefinition> parseFile(File javaFile) throws IOException {
        List<StepDefinition> steps = new ArrayList<>();
        
        String content = readFileContent(javaFile);
        String className = extractClassName(javaFile);
        
        // Extract all annotations and their corresponding methods
        steps.addAll(extractSteps(content, className, StepDefinition.StepType.GIVEN, GIVEN_PATTERN));
        steps.addAll(extractSteps(content, className, StepDefinition.StepType.WHEN, WHEN_PATTERN));
        steps.addAll(extractSteps(content, className, StepDefinition.StepType.THEN, THEN_PATTERN));
        
        return steps;
    }
    
    /**
     * Extract steps of a specific type from content
     */
    private List<StepDefinition> extractSteps(String content, String className, 
                                             StepDefinition.StepType stepType, Pattern pattern) {
        List<StepDefinition> steps = new ArrayList<>();
        Matcher matcher = pattern.matcher(content);
        
        while (matcher.find()) {
            String stepText = matcher.group(1);
            int annotationEnd = matcher.end();
            
            // Find the corresponding method
            MethodInfo methodInfo = findMethodAfterAnnotation(content, annotationEnd);
            
            if (methodInfo != null) {
                StepDefinition step = new StepDefinition(stepType, stepText);
                step.setClassName(className);
                step.setMethodName(methodInfo.methodName);
                step.setLineNumber(getLineNumber(content, matcher.start()));
                
                // Extract parameters from method signature
                extractParameters(step, methodInfo.parameters, stepText);
                
                steps.add(step);
            }
        }
        
        return steps;
    }
    
    /**
     * Find method signature after annotation
     */
    private MethodInfo findMethodAfterAnnotation(String content, int startPos) {
        // Look for method signature in the next few lines after annotation
        String remainingContent = content.substring(startPos);
        Matcher methodMatcher = METHOD_PATTERN.matcher(remainingContent);
        
        if (methodMatcher.find()) {
            String methodName = methodMatcher.group(1);
            String parameters = methodMatcher.group(2).trim();
            
            return new MethodInfo(methodName, parameters);
        }
        
        return null;
    }
    
    /**
     * Extract parameter information from method signature and step text
     */
    private void extractParameters(StepDefinition step, String methodParams, String stepText) {
        if (methodParams.isEmpty()) {
            return;
        }
        
        // Parse method parameters
        List<ParameterInfo> paramInfos = new ArrayList<>();
        String[] paramPairs = methodParams.split(",");
        
        for (String paramPair : paramPairs) {
            paramPair = paramPair.trim();
            if (paramPair.isEmpty()) continue;
            
            Matcher paramMatcher = PARAM_PATTERN.matcher(paramPair);
            if (paramMatcher.find()) {
                String type = paramMatcher.group(1);
                String name = paramMatcher.group(2);
                
                // Determine the placeholder based on step text and parameter type
                String placeholder = determinePlaceholder(stepText, type, name);
                
                ParameterInfo paramInfo = new ParameterInfo(placeholder, type, name);
                
                // Check if this is a data table step (ends with ":")
                if (stepText.trim().endsWith(":")) {
                    paramInfo.setDataTable(true);
                }
                
                paramInfos.add(paramInfo);
            }
        }
        
        step.setParameters(paramInfos);
    }
    
    /**
     * Determine the appropriate placeholder for a parameter
     */
    private String determinePlaceholder(String stepText, String javaType, String paramName) {
        // Check if step text contains parameter placeholders
        if (stepText.contains("{string}")) return "{string}";
        if (stepText.contains("{int}")) return "{int}";
        if (stepText.contains("{double}")) return "{double}";
        if (stepText.contains("{float}")) return "{float}";
        if (stepText.contains("{long}")) return "{long}";
        if (stepText.contains("{boolean}")) return "{boolean}";
        
        // Map Java types to Cucumber placeholders
        String baseType = javaType.replaceAll("<.*>", "");
        switch (baseType) {
            case "String": return "{string}";
            case "int":
            case "Integer": return "{int}";
            case "long":
            case "Long": return "{long}";
            case "double":
            case "Double": return "{double}";
            case "float":
            case "Float": return "{float}";
            case "boolean":
            case "Boolean": return "{boolean}";
            default:
                // For custom types, return the type name as placeholder
                return "{" + baseType + "}";
        }
    }
    
    /**
     * Read file content as string
     */
    private String readFileContent(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }
    
    /**
     * Extract class name from file
     */
    private String extractClassName(File file) {
        String fileName = file.getName();
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }
    
    /**
     * Calculate line number for a given position in content
     */
    private int getLineNumber(String content, int position) {
        int lineNumber = 1;
        for (int i = 0; i < position && i < content.length(); i++) {
            if (content.charAt(i) == '\n') {
                lineNumber++;
            }
        }
        return lineNumber;
    }
    
    /**
     * Extract import statements from content
     */
    private Set<String> extractImports(String content) {
        Set<String> imports = new HashSet<>();
        Matcher matcher = IMPORT_PATTERN.matcher(content);
        
        while (matcher.find()) {
            imports.add(matcher.group(1));
        }
        
        return imports;
    }
    
    /**
     * Check if file contains Cucumber step annotations
     */
    public boolean containsStepAnnotations(File file) throws IOException {
        String content = readFileContent(file);
        
        return GIVEN_PATTERN.matcher(content).find() ||
               WHEN_PATTERN.matcher(content).find() ||
               THEN_PATTERN.matcher(content).find();
    }
    
    /**
     * Container for method information
     */
    private static class MethodInfo {
        final String methodName;
        final String parameters;
        
        MethodInfo(String methodName, String parameters) {
            this.methodName = methodName;
            this.parameters = parameters;
        }
    }
}