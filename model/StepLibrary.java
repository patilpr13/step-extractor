package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Represents the complete step library extracted from Java source files.
 */
public class StepLibrary {
    
    private List<String> givenSteps;
    private List<String> whenSteps;
    private List<String> thenSteps;
    private Map<String, String> parameterTypes;
    private Map<String, Object> metadata;
    
    public StepLibrary() {
        this.givenSteps = new ArrayList<>();
        this.whenSteps = new ArrayList<>();
        this.thenSteps = new ArrayList<>();
        this.parameterTypes = new LinkedHashMap<>();
        this.metadata = new LinkedHashMap<>();
        initializeMetadata();
    }
    
    private void initializeMetadata() {
        metadata.put("extraction_date", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z");
        metadata.put("source_files", new ArrayList<String>());
        metadata.put("total_steps", 0);
    }
    
    public void addStep(StepDefinition stepDef) {
        String pattern = stepDef.getPattern();
        
        switch (stepDef.getType()) {
            case GIVEN:
                if (!givenSteps.contains(pattern)) {
                    givenSteps.add(pattern);
                }
                break;
            case WHEN:
                if (!whenSteps.contains(pattern)) {
                    whenSteps.add(pattern);
                }
                break;
            case THEN:
                if (!thenSteps.contains(pattern)) {
                    thenSteps.add(pattern);
                }
                break;
        }
        
        // Add parameter types
        for (ParameterInfo param : stepDef.getParameters()) {
            if (param.isCustomType() && !parameterTypes.containsKey(param.getJavaType())) {
                parameterTypes.put(param.getJavaType(), 
                    "Custom parameter type: " + param.getJavaType());
            }
        }
        
        updateTotalSteps();
    }
    
    public void addSourceFile(String fileName) {
        @SuppressWarnings("unchecked")
        List<String> sourceFiles = (List<String>) metadata.get("source_files");
        if (!sourceFiles.contains(fileName)) {
            sourceFiles.add(fileName);
        }
    }
    
    private void updateTotalSteps() {
        int total = givenSteps.size() + whenSteps.size() + thenSteps.size();
        metadata.put("total_steps", total);
    }
    
    // Getters and Setters
    public List<String> getGivenSteps() {
        return givenSteps;
    }
    
    public void setGivenSteps(List<String> givenSteps) {
        this.givenSteps = givenSteps;
        updateTotalSteps();
    }
    
    public List<String> getWhenSteps() {
        return whenSteps;
    }
    
    public void setWhenSteps(List<String> whenSteps) {
        this.whenSteps = whenSteps;
        updateTotalSteps();
    }
    
    public List<String> getThenSteps() {
        return thenSteps;
    }
    
    public void setThenSteps(List<String> thenSteps) {
        this.thenSteps = thenSteps;
        updateTotalSteps();
    }
    
    public Map<String, String> getParameterTypes() {
        return parameterTypes;
    }
    
    public void setParameterTypes(Map<String, String> parameterTypes) {
        this.parameterTypes = parameterTypes;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public boolean isEmpty() {
        return givenSteps.isEmpty() && whenSteps.isEmpty() && thenSteps.isEmpty();
    }
    
    public int getTotalStepCount() {
        return givenSteps.size() + whenSteps.size() + thenSteps.size();
    }
    
    @Override
    public String toString() {
        return String.format("StepLibrary{given=%d, when=%d, then=%d, paramTypes=%d}", 
            givenSteps.size(), whenSteps.size(), thenSteps.size(), parameterTypes.size());
    }
}