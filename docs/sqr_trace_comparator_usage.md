# SQR Trace Comparator Usage Guide

## Overview

The SQR Trace Comparator is a tool for analyzing and comparing SQR (Structured Query Report) trace files from different environments. It helps identify performance differences, SQL execution plan changes, and other variations that might affect the behavior of SQR programs.

## Features

- **Automatic Format Detection**: Automatically detects the trace format based on the content of the trace file
- **Multiple Format Support**: Handles different trace parameter formats:
  - Standard format: Basic trace information
  - Detailed SQL format: Includes SQL execution plans and statistics (-S parameter)
  - Detailed Time format: Includes detailed timing information (-RT parameter)
  - Detailed Result format: Includes result set information (-RS parameter)
- **Comprehensive Comparison**: Compares execution times, method calls, SQL calls, and additional metadata
- **Detailed Reporting**: Generates both summary CSV and detailed Markdown reports
- **Alert Flagging**: Automatically flags significant performance differences

## Usage

### Command Line

```
java com.example.core.tool.SQRTraceComparator <env1_trace_file> <env2_trace_file> [env1_name] [env2_name] [output_path]
```

### Parameters

- `env1_trace_file`: Path to the first trace file (e.g., from SIT environment)
- `env2_trace_file`: Path to the second trace file (e.g., from UAT environment)
- `env1_name` (optional): Name of the first environment (default: "ENV1")
- `env2_name` (optional): Name of the second environment (default: "ENV2")
- `output_path` (optional): Path to the output CSV file (default: "sqr_trace_comparison_result.csv")

### Example

```
java com.example.core.tool.SQRTraceComparator sit_trace.log uat_trace.log SIT UAT comparison_result.csv
```

## Output Files

The tool generates two output files:

1. **CSV Summary Report** (e.g., `comparison_result.csv`):
   - Contains a summary of all trace entries with their execution times and differences
   - Flags significant differences with "ALERT"
   - Marks entries that only exist in one environment as "UNIQUE" or "MISSING"

2. **Detailed Markdown Report** (e.g., `comparison_result_detailed.md`):
   - Provides in-depth analysis of differences
   - Includes SQL execution plan differences
   - Shows time breakdown differences
   - Displays result set differences
   - Highlights significant performance variations

## Understanding the Results

### CSV Summary Report Columns

- **Type**: The type of trace entry (PROGRAM, SQL, PROCEDURE, VARIABLE)
- **Identifier**: A unique identifier for the trace entry
- **ENV1(ms)**: Execution time in milliseconds in the first environment
- **ENV2(ms)**: Execution time in milliseconds in the second environment
- **Diff(ms)**: Difference in execution time (ENV2 - ENV1)
- **Diff(%)**: Percentage difference in execution time
- **Flag**: Alert flag for significant differences
- **Details**: Additional details about the differences

### Flag Values

- **ALERT**: Indicates a significant performance difference (>20% and >100ms)
- **UNIQUE**: Entry only exists in the second environment
- **MISSING**: Entry only exists in the first environment

## Best Practices

1. **Use Consistent Trace Parameters**: For the most accurate comparison, use the same trace parameters in both environments.

2. **Compare Similar Workloads**: Ensure that the SQR programs are processing similar data volumes and types in both environments.

3. **Focus on Significant Differences**: Pay special attention to entries flagged with "ALERT" as they represent significant performance variations.

4. **Analyze SQL Plan Changes**: For SQL performance issues, examine the execution plan differences in the detailed report.

5. **Check for Missing or Extra Operations**: Entries marked as "UNIQUE" or "MISSING" might indicate configuration differences between environments.

## Troubleshooting

### Common Issues

1. **Trace Format Not Detected Correctly**:
   - Ensure the trace files contain the expected format markers
   - You can manually specify the format by modifying the code if needed

2. **Timestamps Not Parsed Correctly**:
   - Verify that the trace files use the expected time format (HH:MM:SS or HH:MM:SS.SSS)
   - Adjust the time parsing logic if your trace files use a different format

3. **Entries Not Matched Between Environments**:
   - Check that the same SQR program is being traced in both environments
   - Verify that the trace parameters are similar enough to produce comparable output

## Example Workflow

1. Generate trace files in both environments:
   ```
   sqr -SQRTRACE 3 -S -RT my_report.sqr
   ```

2. Run the comparison:
   ```
   java com.example.core.tool.SQRTraceComparator sit_trace.log uat_trace.log SIT UAT results.csv
   ```

3. Review the CSV summary to identify significant differences

4. Examine the detailed Markdown report for in-depth analysis of flagged issues

5. Address performance problems by optimizing SQL, adjusting configurations, or fixing code issues