package extractor;

import model.ParameterInfo;
import model.StepDefinition;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts and analyzes parameter information from step definitions.
 */
public class ParameterExtractor {
    
    // Pattern to find parameter placeholders in step text
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^}]+)\\}");
    
    // Pattern to identify data table steps
    private static final Pattern DATA_TABLE_PATTERN = Pattern.compile(".*:$");
    
    // Common parameter type mappings
    private static final Map<String, String> TYPE_MAPPINGS = new HashMap<String, String>() {{
        put("String", "{string}");
        put("int", "{int}");
        put("Integer", "{int}");
        put("long", "{long}");
        put("Long", "{long}");
        put("double", "{double}");
        put("Double", "{double}");
        put("float", "{float}");
        put("Float", "{float}");
        put("boolean", "{boolean}");
        put("Boolean", "{boolean}");
        put("char", "{char}");
        put("Character", "{char}");
    }};
    
    /**
     * Enhance step definition with detailed parameter analysis
     */
    public void enhanceStepParameters(StepDefinition step) {
        if (step.getParameters() == null || step.getParameters().isEmpty()) {
            return;
        }
        
        String stepText = step.getPattern();
        List<ParameterInfo> parameters = step.getParameters();
        
        // Check if this is a data table step
        boolean isDataTableStep = DATA_TABLE_PATTERN.matcher(stepText.trim()).matches();
        
        // Extract placeholders from step text
        List<String> placeholders = extractPlaceholdersFromText(stepText);
        
        // Match placeholders with parameters
        matchPlaceholdersToParameters(parameters, placeholders, isDataTableStep);
        
        // Analyze parameter complexity
        analyzeParameterComplexity(parameters);
    }
    
    /**
     * Extract parameter placeholders from step text
     */
    private List<String> extractPlaceholdersFromText(String stepText) {
        List<String> placeholders = new ArrayList<>();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(stepText);
        
        while (matcher.find()) {
            placeholders.add("{" + matcher.group(1) + "}");
        }
        
        return placeholders;
    }
    
    /**
     * Match extracted placeholders with method parameters
     */
    private void matchPlaceholdersToParameters(List<ParameterInfo> parameters, 
                                              List<String> placeholders, 
                                              boolean isDataTableStep) {
        
        int placeholderIndex = 0;
        
        for (ParameterInfo param : parameters) {
            // Set data table flag
            param.setDataTable(isDataTableStep && isDataTableType(param.getJavaType()));
            
            // Match with placeholder if available
            if (placeholderIndex < placeholders.size()) {
                param.setPlaceholder(placeholders.get(placeholderIndex));
                placeholderIndex++;
            } else if (!param.isDataTable()) {
                // Generate placeholder based on Java type
                param.setPlaceholder(generatePlaceholder(param.getJavaType()));
            }
        }
    }
    
    /**
     * Check if a Java type represents a data table
     */
    private boolean isDataTableType(String javaType) {
        if (javaType == null) return false;
        
        // Common data table types in Cucumber
        return javaType.contains("DataTable") ||
               javaType.contains("List<") ||
               javaType.contains("Map<") ||
               javaType.contains("Table") ||
               isCustomBeanType(javaType);
    }
    
    /**
     * Check if a type is a custom bean (not primitive/standard type)
     */
    private boolean isCustomBeanType(String javaType) {
        if (javaType == null) return false;
        
        String baseType = javaType.replaceAll("<.*>", "");
        
        // Check if it's a standard Java type
        return !TYPE_MAPPINGS.containsKey(baseType) &&
               !baseType.startsWith("java.") &&
               !baseType.equals("DataTable");
    }
    
    /**
     * Generate appropriate placeholder for Java type
     */
    private String generatePlaceholder(String javaType) {
        if (javaType == null) return "{string}";
        
        String baseType = javaType.replaceAll("<.*>", "");
        
        // Use predefined mapping if available
        String mapped = TYPE_MAPPINGS.get(baseType);
        if (mapped != null) {
            return mapped;
        }
        
        // For custom types, use the type name
        return "{" + baseType + "}";
    }
    
    /**
     * Analyze parameter complexity and set flags
     */
    private void analyzeParameterComplexity(List<ParameterInfo> parameters) {
        for (ParameterInfo param : parameters) {
            // Check if it's a custom type
            param.setCustomType(isCustomBeanType(param.getJavaType()));
            
            // Additional analysis can be added here
            // e.g., parameter validation, transformation rules, etc.
        }
    }
    
    /**
     * Extract all unique parameter types from a list of step definitions
     */
    public Set<String> extractParameterTypes(List<StepDefinition> steps) {
        Set<String> parameterTypes = new LinkedHashSet<>();
        
        for (StepDefinition step : steps) {
            if (step.getParameters() != null) {
                for (ParameterInfo param : step.getParameters()) {
                    if (param.getJavaType() != null) {
                        parameterTypes.add(param.getJavaType());
                    }
                }
            }
        }
        
        return parameterTypes;
    }
    
    /**
     * Generate parameter type documentation
     */
    public Map<String, String> generateParameterDocumentation(List<StepDefinition> steps) {
        Map<String, String> documentation = new LinkedHashMap<>();
        Set<String> parameterTypes = extractParameterTypes(steps);
        
        for (String type : parameterTypes) {
            String description = generateTypeDescription(type);
            documentation.put(type, description);
        }
        
        return documentation;
    }
    
    /**
     * Generate description for a parameter type
     */
    private String generateTypeDescription(String javaType) {
        if (javaType == null) return "Unknown parameter type";
        
        String baseType = javaType.replaceAll("<.*>", "");
        
        // Standard types
        if (TYPE_MAPPINGS.containsKey(baseType)) {
            switch (baseType) {
                case "String": return "String parameters for text values";
                case "int":
                case "Integer": return "Integer numeric parameters";
                case "long":
                case "Long": return "Long numeric parameters";
                case "double":
                case "Double": return "Double precision numeric parameters";
                case "float":
                case "Float": return "Float precision numeric parameters";
                case "boolean":
                case "Boolean": return "Boolean true/false parameters";
                case "char":
                case "Character": return "Single character parameters";
                default: return "Standard " + baseType + " parameters";
            }
        }
        
        // Collection types
        if (javaType.contains("List<")) return "List collection parameters";
        if (javaType.contains("Map<")) return "Map/dictionary parameters";
        if (javaType.contains("Set<")) return "Set collection parameters";
        
        // Special Cucumber types
        if (baseType.equals("DataTable")) return "Cucumber data table parameters";
        
        // Custom types
        if (isCustomBeanType(javaType)) {
            return "Custom parameter type: " + baseType;
        }
        
        return "Parameter type: " + javaType;
    }
    
    /**
     * Validate parameter consistency across steps
     */
    public List<String> validateParameterConsistency(List<StepDefinition> steps) {
        List<String> warnings = new ArrayList<>();
        Map<String, Set<String>> stepToTypes = new HashMap<>();
        
        // Group parameters by step pattern
        for (StepDefinition step : steps) {
            String pattern = step.getPattern();
            stepToTypes.putIfAbsent(pattern, new HashSet<>());
            
            if (step.getParameters() != null) {
                for (ParameterInfo param : step.getParameters()) {
                    if (param.getJavaType() != null) {
                        stepToTypes.get(pattern).add(param.getJavaType());
                    }
                }
            }
        }
        
        // Check for inconsistencies
        for (Map.Entry<String, Set<String>> entry : stepToTypes.entrySet()) {
            if (entry.getValue().size() > 1) {
                warnings.add("Step '" + entry.getKey() + "' has inconsistent parameter types: " + 
                           entry.getValue());
            }
        }
        
        return warnings;
    }
}