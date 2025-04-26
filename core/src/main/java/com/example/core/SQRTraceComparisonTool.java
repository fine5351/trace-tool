package com.example.core;

import com.example.core.tool.NewSQRTraceComparator;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * A command-line tool for comparing Oracle PeopleSoft SQR trace files.
 * This tool uses the NewSQRTraceComparator to analyze and compare trace files.
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Accepts two file paths as input</li>
 *   <li>Allows specifying a threshold for time differences</li>
 *   <li>Outputs differences to a file</li>
 * </ul>
 *
 * <p>Usage:</p>
 * <pre>
 * java SQRTraceComparisonTool &lt;file1_path&gt; &lt;file2_path&gt; [threshold] [output_path]
 * </pre>
 *
 * <p>Example:</p>
 * <pre>
 * java SQRTraceComparisonTool D:\traces\file1.log D:\traces\file2.log 2.0 differences.txt
 * </pre>
 */
@Slf4j
public class SQRTraceComparisonTool {

    /**
     * Main method to run the SQR trace comparison tool.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        log.info("Starting SQR Trace Comparison Tool");

        if (args.length < 2) {
            System.out.println("Usage: java SQRTraceComparisonTool <file1_path> <file2_path> [threshold] [output_path]");
            System.out.println("  threshold: Ratio for time difference (default: 2.0)");
            System.out.println("  output_path: Path to output file (default: sqr_trace_differences.txt)");
            return;
        }

        try {
            // Forward the arguments to the NewSQRTraceComparator
            NewSQRTraceComparator.main(args);
            log.info("SQR Trace comparison completed successfully");
        } catch (IOException e) {
            log.error("Error comparing SQR trace files: {}", e.getMessage(), e);
            System.err.println("Error comparing SQR trace files: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage(), e);
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }
}