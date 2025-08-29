package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Cucumber step definition extracted from Java source code.
 */
public class StepDefinition {
    
    public enum StepType {
        GIVEN, WHEN, THEN
    }
    
    private StepType type;
    private String pattern;
    private List<ParameterInfo> parameters;
    private String methodName;
    private String className;
    private int lineNumber;
    
    public StepDefinition() {
        this.parameters = new ArrayList<>();
    }
    
    public StepDefinition(StepType type, String pattern) {
        this();
        this.type = type;
        this.pattern = pattern;
    }
    
    // Getters and Setters
    public StepType getType() {
        return type;
    }
    
    public void setType(StepType type) {
        this.type = type;
    }
    
    public String getPattern() {
        return pattern;
    }
    
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
    
    public List<ParameterInfo> getParameters() {
        return parameters;
    }
    
    public void setParameters(List<ParameterInfo> parameters) {
        this.parameters = parameters;
    }
    
    public void addParameter(ParameterInfo parameter) {
        this.parameters.add(parameter);
    }
    
    public String getMethodName() {
        return methodName;
    }
    
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
    
    public String getClassName() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
    
    public int getLineNumber() {
        return lineNumber;
    }
    
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
    
    @Override
    public String toString() {
        return String.format("StepDefinition{type=%s, pattern='%s', class=%s, method=%s, line=%d}", 
            type, pattern, className, methodName, lineNumber);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        StepDefinition that = (StepDefinition) obj;
        return type == that.type && 
               pattern != null && pattern.equals(that.pattern);
    }
    
    @Override
    public int hashCode() {
        return (type != null ? type.hashCode() : 0) * 31 + 
               (pattern != null ? pattern.hashCode() : 0);
    }
}