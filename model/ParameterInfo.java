package model;

/**
 * Represents parameter information for a Cucumber step definition.
 */
public class ParameterInfo {
    
    private String placeholder;      // {string}, {int}, etc.
    private String javaType;         // String, int, CustomBean
    private String parameterName;    // Method parameter name
    private boolean isDataTable;     // Steps ending with ":"
    private boolean isCustomType;    // Non-primitive types
    
    public ParameterInfo() {
    }
    
    public ParameterInfo(String placeholder, String javaType) {
        this.placeholder = placeholder;
        this.javaType = javaType;
        this.isCustomType = !isPrimitiveType(javaType);
        this.isDataTable = false;
    }
    
    public ParameterInfo(String placeholder, String javaType, String parameterName) {
        this(placeholder, javaType);
        this.parameterName = parameterName;
    }
    
    // Getters and Setters
    public String getPlaceholder() {
        return placeholder;
    }
    
    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }
    
    public String getJavaType() {
        return javaType;
    }
    
    public void setJavaType(String javaType) {
        this.javaType = javaType;
        this.isCustomType = !isPrimitiveType(javaType);
    }
    
    public String getParameterName() {
        return parameterName;
    }
    
    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }
    
    public boolean isDataTable() {
        return isDataTable;
    }
    
    public void setDataTable(boolean dataTable) {
        this.isDataTable = dataTable;
    }
    
    public boolean isCustomType() {
        return isCustomType;
    }
    
    public void setCustomType(boolean customType) {
        this.isCustomType = customType;
    }
    
    /**
     * Check if a Java type is a primitive/standard type
     */
    private boolean isPrimitiveType(String type) {
        if (type == null) return false;
        
        // Remove generic type parameters if present
        String baseType = type.replaceAll("<.*>", "");
        
        return baseType.equals("String") ||
               baseType.equals("int") ||
               baseType.equals("Integer") ||
               baseType.equals("long") ||
               baseType.equals("Long") ||
               baseType.equals("double") ||
               baseType.equals("Double") ||
               baseType.equals("float") ||
               baseType.equals("Float") ||
               baseType.equals("boolean") ||
               baseType.equals("Boolean") ||
               baseType.equals("char") ||
               baseType.equals("Character");
    }
    
    /**
     * Convert Java type to Cucumber placeholder
     */
    public String toCucumberPlaceholder() {
        if (placeholder != null && !placeholder.isEmpty()) {
            return placeholder;
        }
        
        if (javaType == null) return "{string}";
        
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
            default: return "{" + baseType + "}";
        }
    }
    
    @Override
    public String toString() {
        return String.format("ParameterInfo{placeholder='%s', javaType='%s', paramName='%s', isDataTable=%s}", 
            placeholder, javaType, parameterName, isDataTable);
    }
}