# Java Cucumber Step Extractor

Automatically extract Cucumber step definitions from Java source files and generate YAML step libraries compatible with the HLR-to-Test CLI tool.

## Features

- ✅ **Recursive Directory Scanning**: Find all Java files containing Cucumber steps
- ✅ **Multi-Annotation Support**: Extract @Given, @When, @Then step definitions
- ✅ **Parameter Analysis**: Identify {string}, {int}, custom beans, and data tables
- ✅ **YAML Generation**: Output compatible with HLR-to-Test CLI tool
- ✅ **Zero Dependencies**: Pure Java implementation, no external libraries required
- ✅ **Java 8+ Compatible**: Works with Java 8 and higher versions

## Quick Start

### 1. Compile the Project
```bash
javac -d . *.java */*.java
```

### 2. Run the Extractor
```bash
# Extract steps from directory
java CucumberStepExtractor /path/to/java/src

# Extract with custom output file
java CucumberStepExtractor /path/to/java/src my_steps.yaml
```

### 3. Use Generated Steps with HLR-to-Test CLI
```bash
# Copy generated steps to CLI tool
cp step_library.yaml ../hlr_to_test/resources/

# Run CLI tool with extracted steps
cd ../hlr_to_test
python3 main.py -i ../HLR1.md -o ../HLT1.feature
```

## Usage Examples

### Command Line Interface
```bash
# Basic extraction
java CucumberStepExtractor ./src/test/java

# Custom output file
java CucumberStepExtractor ./src/test/java custom_steps.yaml

# Extract from current directory
java CucumberStepExtractor .

# Show help
java CucumberStepExtractor --help
```

### Programmatic Usage
```java
CucumberStepExtractor extractor = new CucumberStepExtractor();

// Extract to default file
extractor.extractSteps("./src/test/java");

// Extract with custom output
extractor.extractSteps("./src/test/java", "my_steps.yaml");

// Get StepLibrary object for processing
StepLibrary library = extractor.extractStepsAsLibrary("./src/test/java");
```

## Supported Step Patterns

### Basic Steps
```java
@Given("User is logged in")
@When("User clicks submit button") 
@Then("Page should display success message")
```

### Parameterized Steps
```java
@Given("User enters {string} in username field")
@When("User waits for {int} seconds")
@Then("Page should contain {string} text")
```

### Data Table Steps
```java
@Given("User creates flight plan with:")
public void createFlightPlan(FlightPlanBean data) { ... }

@When("System processes the following data:")
public void processData(DataTable table) { ... }
```

### Complex Parameters
```java
@Given("Airwork has been Planned with:")
public void planAirwork(AirworkIntentInitiationBean data) { ... }
```

## Output Format

Generated YAML is compatible with the HLR-to-Test CLI tool:

```yaml
# Generated Cucumber step library from Java source files
given_steps:
  - "User is logged in"
  - "User enters {string} in username field"
  - "Airwork has been Planned with:"

when_steps:
  - "User clicks submit button"
  - "System processes the following data:"

then_steps:
  - "Page should display success message"
  - "Page should contain {string} text"

parameter_types:
  FlightPlanBean: "Custom parameter type: FlightPlanBean"
  AirworkIntentInitiationBean: "Custom parameter type: AirworkIntentInitiationBean"

metadata:
  extraction_date: "2025-08-29T10:31:52Z"
  source_files: 
    - "StepDefinitions.java"
    - "HmiAirworkSteps.java"
  total_steps: 25
```

## Test Results

Successfully tested with `HmiAirworkSteps.java`:

```
✅ Found 13 step definitions
✅ Extracted 6 Given steps
✅ Extracted 7 When steps  
✅ Identified 2 custom parameter types
✅ Generated valid YAML output
✅ Compatible with HLR-to-Test CLI tool
```

## Architecture

```
CucumberStepExtractor
├── model/
│   ├── StepDefinition.java       # Step data model
│   ├── ParameterInfo.java        # Parameter information
│   └── StepLibrary.java          # Complete step library
├── scanner/
│   └── FileScanner.java          # Recursive file discovery
├── parser/
│   └── AnnotationParser.java     # Extract annotations from source
├── extractor/
│   └── ParameterExtractor.java   # Parameter analysis
└── generator/
    └── YamlGenerator.java        # YAML output generation
```

## Configuration

### Include/Exclude Patterns
The file scanner supports customizable patterns:

```java
FileScanner scanner = new FileScanner();
scanner.addIncludePattern("**/*Steps.java");
scanner.addExcludePattern("**/target/**");
```

### Default Exclusions
- `**/target/**` (Maven build directory)
- `**/build/**` (Gradle build directory)  
- `**/.git/**` (Git directory)

## Troubleshooting

### No Java Files Found
```
Found 0 Java files in 425ms
No Java files found. Exiting.
```

**Solutions:**
- Check the source directory path is correct
- Verify Java files exist in the directory
- Ensure files have `.java` extension
- Check file permissions

### No Step Definitions Found
```
Processed 0 files with step definitions
Extracted 0 total steps
```

**Solutions:**
- Verify files contain `@Given`, `@When`, or `@Then` annotations
- Check imports include `io.cucumber.java.en.*`
- Ensure annotation syntax is correct

### Compilation Errors
```
error: cannot find symbol
```

**Solutions:**
- Ensure Java 8+ is installed
- Check all required files are present
- Verify classpath is set correctly

## Integration with HLR-to-Test CLI

### Step 1: Extract Steps from Java
```bash
java CucumberStepExtractor /path/to/cucumber/steps step_library.yaml
```

### Step 2: Copy to CLI Resources
```bash
cp step_library.yaml ../hlr_to_test/resources/
```

### Step 3: Generate Tests
```bash
cd ../hlr_to_test
python3 main.py -i ../HLR1.md -o ../HLT1.feature
```

### Step 4: Verify Generated Tests Use Extracted Steps
The HLR-to-Test CLI will now use your extracted Java step definitions when generating test cases.

## Contributing

### Adding New Features
1. Follow existing code structure
2. Add comprehensive error handling  
3. Include unit tests where possible
4. Update documentation

### Extending Parameter Support
1. Update `ParameterExtractor.java`
2. Add new type mappings in `TYPE_MAPPINGS`
3. Test with real step definition files

### Improving YAML Output
1. Modify `YamlGenerator.java`
2. Ensure compatibility with existing tools
3. Validate output format

## Performance

- **Small Projects** (< 100 files): < 1 second
- **Medium Projects** (< 1000 files): < 5 seconds  
- **Large Projects** (1000+ files): < 30 seconds

Memory usage scales linearly with number of step definitions found.

## Requirements

- **Java 8+**: Compatible with Java 8 through Java 21
- **No External Dependencies**: Uses only standard Java libraries
- **Cross-Platform**: Works on Windows, macOS, and Linux

## License

This tool is designed to work with the HLR-to-Test CLI application for automating aviation software test generation.