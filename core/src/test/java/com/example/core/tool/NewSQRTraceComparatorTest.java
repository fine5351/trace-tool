package com.example.core.tool;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the NewSQRTraceComparator class.
 */
public class NewSQRTraceComparatorTest {

    @TempDir
    Path tempDir;

    /**
     * Test parsing a trace file.
     */
    @Test
    public void testParseTrace() throws IOException {
        // Create a sample trace file
        Path traceFile = tempDir.resolve("sample_trace.log");
        List<String> traceLines = List.of(
                "SQR开始执行: 2023-01-01 (10:00:00)",
                "程序: TEST.SQR",
                "执行SQL (10:01:00):",
                "SELECT * FROM DUAL",
                "执行时间: 1.5秒",
                "执行SQL (10:02:00):",
                "SELECT * FROM EMPLOYEES",
                "执行时间: 2.5秒",
                "SQR结束执行: 2023-01-01 (10:03:00)"
        );
        Files.write(traceFile, traceLines);

        // Parse the trace file
        List<NewSQRTraceComparator.TraceEntry> entries = NewSQRTraceComparator.parseTrace(traceFile.toString());

        // Verify the results
        assertNotNull(entries);
        assertEquals(3, entries.size()); // Should find 3 entries with timestamps

        // Check the first entry
        assertEquals("SQR开始执行: 2023-01-01 (10:00:00)", entries.get(0).lineContent);
        assertEquals(36000000, entries.get(0).executionTime); // 10:00:00 in milliseconds
        assertEquals(1, entries.get(0).lineNumber);

        // Check the second entry
        assertEquals("执行SQL (10:01:00):", entries.get(1).lineContent);
        assertEquals(36060000, entries.get(1).executionTime); // 10:01:00 in milliseconds
        assertEquals(3, entries.get(1).lineNumber);

        // Check the third entry
        assertEquals("执行SQL (10:02:00):", entries.get(2).lineContent);
        assertEquals(36120000, entries.get(2).executionTime); // 10:02:00 in milliseconds
        assertEquals(6, entries.get(2).lineNumber);
    }

    /**
     * Test comparing two trace files with time differences.
     */
    @Test
    public void testCompareTracesWithTimeDifferences() throws IOException {
        // Create two sample trace files
        Path traceFile1 = tempDir.resolve("trace1.log");
        List<String> traceLines1 = List.of(
                "SQR开始执行: 2023-01-01 (10:00:00)",
                "程序: TEST.SQR",
                "执行SQL (10:01:00):",
                "SELECT * FROM DUAL",
                "执行时间: 1.0秒",
                "执行SQL (10:02:00):",
                "SELECT * FROM EMPLOYEES",
                "执行时间: 2.0秒",
                "SQR结束执行: 2023-01-01 (10:03:00)"
        );
        Files.write(traceFile1, traceLines1);

        Path traceFile2 = tempDir.resolve("trace2.log");
        List<String> traceLines2 = List.of(
                "SQR开始执行: 2023-01-01 (10:00:00)",
                "程序: TEST.SQR",
                "执行SQL (10:01:00):",
                "SELECT * FROM DUAL",
                "执行时间: 1.0秒",
                "执行SQL (10:02:00):",
                "SELECT * FROM EMPLOYEES",
                "执行时间: 5.0秒", // 2.5x slower
                "SQR结束执行: 2023-01-01 (10:03:00)"
        );
        Files.write(traceFile2, traceLines2);

        // Parse the trace files
        List<NewSQRTraceComparator.TraceEntry> entries1 = NewSQRTraceComparator.parseTrace(traceFile1.toString());
        List<NewSQRTraceComparator.TraceEntry> entries2 = NewSQRTraceComparator.parseTrace(traceFile2.toString());

        // Compare the traces
        Path outputFile = tempDir.resolve("output.txt");
        NewSQRTraceComparator.compareTraces(entries1, entries2, 2.0, outputFile.toString());

        // Verify the output file exists
        assertTrue(Files.exists(outputFile));

        // Read the output file
        List<String> outputLines = Files.readAllLines(outputFile);

        // Verify the output contains the time difference
        boolean foundTimeDifference = false;
        for (int i = 0; i < outputLines.size(); i++) {
            if (outputLines.get(i).contains("Time Difference Detected")) {
                foundTimeDifference = true;
                break;
            }
        }
        assertTrue(foundTimeDifference, "Should detect time difference");
    }

    /**
     * Test comparing two trace files with extra code in one file.
     */
    @Test
    public void testCompareTracesWithExtraCode() throws IOException {
        // Create two sample trace files
        Path traceFile1 = tempDir.resolve("trace1.log");
        List<String> traceLines1 = List.of(
                "SQR开始执行: 2023-01-01 (10:00:00)",
                "程序: TEST.SQR",
                "执行SQL (10:01:00):",
                "SELECT * FROM DUAL",
                "执行时间: 1.0秒",
                "执行SQL (10:02:00):",
                "SELECT * FROM EMPLOYEES",
                "执行时间: 2.0秒",
                "SQR结束执行: 2023-01-01 (10:03:00)"
        );
        Files.write(traceFile1, traceLines1);

        Path traceFile2 = tempDir.resolve("trace2.log");
        List<String> traceLines2 = List.of(
                "SQR开始执行: 2023-01-01 (10:00:00)",
                "程序: TEST.SQR",
                "执行SQL (10:01:00):",
                "SELECT * FROM DUAL",
                "执行时间: 1.0秒",
                "执行SQL (10:01:30):", // Extra code
                "SELECT * FROM DEPARTMENTS", // Extra code
                "执行时间: 1.5秒", // Extra code
                "执行SQL (10:02:00):",
                "SELECT * FROM EMPLOYEES",
                "执行时间: 2.0秒",
                "SQR结束执行: 2023-01-01 (10:03:00)"
        );
        Files.write(traceFile2, traceLines2);

        // Parse the trace files
        List<NewSQRTraceComparator.TraceEntry> entries1 = NewSQRTraceComparator.parseTrace(traceFile1.toString());
        List<NewSQRTraceComparator.TraceEntry> entries2 = NewSQRTraceComparator.parseTrace(traceFile2.toString());

        // Compare the traces
        Path outputFile = tempDir.resolve("output.txt");
        NewSQRTraceComparator.compareTraces(entries1, entries2, 2.0, outputFile.toString());

        // Verify the output file exists
        assertTrue(Files.exists(outputFile));

        // Read the output file
        List<String> outputLines = Files.readAllLines(outputFile);

        // Verify the output contains the extra code
        boolean foundExtraCode = false;
        for (int i = 0; i < outputLines.size(); i++) {
            if (outputLines.get(i).contains("Extra Code in File 2")) {
                foundExtraCode = true;
                break;
            }
        }
        assertTrue(foundExtraCode, "Should detect extra code");
    }

    /**
     * Test the main method with valid arguments.
     */
    @Test
    public void testMainWithValidArguments() throws IOException {
        // Create two sample trace files
        Path traceFile1 = tempDir.resolve("trace1.log");
        List<String> traceLines1 = List.of(
                "SQR开始执行: 2023-01-01 (10:00:00)",
                "程序: TEST.SQR",
                "执行SQL (10:01:00):",
                "SELECT * FROM DUAL",
                "执行时间: 1.0秒",
                "SQR结束执行: 2023-01-01 (10:02:00)"
        );
        Files.write(traceFile1, traceLines1);

        Path traceFile2 = tempDir.resolve("trace2.log");
        List<String> traceLines2 = List.of(
                "SQR开始执行: 2023-01-01 (10:00:00)",
                "程序: TEST.SQR",
                "执行SQL (10:01:00):",
                "SELECT * FROM DUAL",
                "执行时间: 3.0秒", // 3x slower
                "SQR结束执行: 2023-01-01 (10:02:00)"
        );
        Files.write(traceFile2, traceLines2);

        Path outputFile = tempDir.resolve("output.txt");

        // Call the main method
        String[] args = {
                traceFile1.toString(),
                traceFile2.toString(),
                "2.0",
                outputFile.toString()
        };
        NewSQRTraceComparator.main(args);

        // Verify the output file exists
        assertTrue(Files.exists(outputFile));

        // Read the output file
        List<String> outputLines = Files.readAllLines(outputFile);

        // Verify the output contains the time difference
        boolean foundTimeDifference = false;
        for (int i = 0; i < outputLines.size(); i++) {
            if (outputLines.get(i).contains("Time Difference Detected")) {
                foundTimeDifference = true;
                break;
            }
        }
        assertTrue(foundTimeDifference, "Should detect time difference");
    }
}