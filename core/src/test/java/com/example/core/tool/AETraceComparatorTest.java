package com.example.core.tool;

import com.example.core.tool.analyzer.TraceAnalyzer;
import com.example.core.tool.analyzer.TraceAnalyzerFactory;
import com.example.core.tool.analyzer.TraceEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the AETraceComparator class.
 * This test class focuses on testing different traceParams combinations.
 */
class AETraceComparatorTest {

    @TempDir
    Path tempDir;

    /**
     * Test that the correct analyzers are created for standard trace parameters.
     */
    @Test
    void testStandardTraceParams() {
        // Test with -TRACE parameter
        String traceParams = "-TRACE 1";
        List<TraceAnalyzer> analyzers = TraceAnalyzerFactory.createAnalyzersForParams(traceParams);

        // Should create one standard analyzer
        assertEquals(1, analyzers.size());
        assertEquals("StandardTraceAnalyzer", analyzers.get(0).getClass().getSimpleName());
    }

    /**
     * Test that the correct analyzers are created for SQL trace parameters.
     */
    @Test
    void testSqlTraceParams() {
        // Test with -TOOLSTRACESQL parameter
        String traceParams = "-TOOLSTRACESQL 31";
        List<TraceAnalyzer> analyzers = TraceAnalyzerFactory.createAnalyzersForParams(traceParams);

        // Should create one SQL analyzer
        assertEquals(1, analyzers.size());
        assertEquals("DetailedSqlTraceAnalyzer", analyzers.get(0).getClass().getSimpleName());

        // Test with -DBFLAGS parameter
        traceParams = "-DBFLAGS 7";
        analyzers = TraceAnalyzerFactory.createAnalyzersForParams(traceParams);

        // Should create one SQL analyzer
        assertEquals(1, analyzers.size());
        assertEquals("DetailedSqlTraceAnalyzer", analyzers.get(0).getClass().getSimpleName());
    }

    /**
     * Test that the correct analyzers are created for PeopleCode trace parameters.
     */
    @Test
    void testPcTraceParams() {
        // Test with -TOOLSTRACEPC parameter
        String traceParams = "-TOOLSTRACEPC 4044";
        List<TraceAnalyzer> analyzers = TraceAnalyzerFactory.createAnalyzersForParams(traceParams);

        // Should create one PeopleCode analyzer
        assertEquals(1, analyzers.size());
        assertEquals("DetailedPcTraceAnalyzer", analyzers.get(0).getClass().getSimpleName());
    }

    /**
     * Test that the correct analyzers are created for combined trace parameters.
     */
    @Test
    void testCombinedTraceParams() {
        // Test with combined parameters
        String traceParams = "-TRACE 3 -TOOLSTRACEPC 4044 -TOOLSTRACESQL 31";
        List<TraceAnalyzer> analyzers = TraceAnalyzerFactory.createAnalyzersForParams(traceParams);

        // Should create two analyzers (PC and SQL)
        assertEquals(2, analyzers.size());

        // Check that we have both types of analyzers
        boolean hasPcAnalyzer = false;
        boolean hasSqlAnalyzer = false;

        for (TraceAnalyzer analyzer : analyzers) {
            String className = analyzer.getClass().getSimpleName();
            if (className.equals("DetailedPcTraceAnalyzer")) {
                hasPcAnalyzer = true;
            } else if (className.equals("DetailedSqlTraceAnalyzer")) {
                hasSqlAnalyzer = true;
            }
        }

        assertTrue(hasPcAnalyzer, "Should have a PeopleCode analyzer");
        assertTrue(hasSqlAnalyzer, "Should have a SQL analyzer");
    }

    /**
     * Test that the parseTraceWithMultipleAnalyzers method correctly merges entries.
     */
    @Test
    void testParseTraceWithMultipleAnalyzers() throws IOException {
        // Create a sample trace file with mixed content that matches the expected format
        Path traceFile = createSampleTraceFile("mixed_trace.log",
                "10:00:00.000 Step:MAIN.STEP1 started\n" +
                "10:00:00.100 SQL:SQL1 started\n" +
                "SQL statement: SELECT * FROM PS_JOB WHERE EMPLID = '12345'\n" +
                "Bind-Variables: EMPLID=12345\n" +
                "10:00:00.200 Function:GetJobData started\n" +
                "PeopleCode Execution: GetJobData()\n" +
                "Variable: $jobRow = Record(JOB)\n" +
                "10:00:00.800 Function:GetJobData ended\n" +
                "10:00:00.600 SQL:SQL1 ended\n" +
                "10:00:01.000 Step:MAIN.STEP1 ended\n");

        // Create analyzers for different trace parameters
        String traceParams = "-TRACE 3 -TOOLSTRACEPC 4044 -TOOLSTRACESQL 31";
        List<TraceAnalyzer> analyzers = TraceAnalyzerFactory.createAnalyzersForParams(traceParams);

        // Parse the trace file with multiple analyzers
        List<TraceEntry> entries = AETraceComparator.parseTraceWithMultipleAnalyzers(traceFile.toString(), analyzers);

        // Verify that entries were parsed and merged correctly
        assertNotNull(entries);
        assertFalse(entries.isEmpty());

        // Check for STEP entry
        TraceEntry stepEntry = findEntryByTypeAndIdentifier(entries, "STEP", "MAIN.STEP1");
        assertNotNull(stepEntry, "Should have a STEP entry");
        assertEquals(1000, stepEntry.duration()); // 1 second = 1000ms

        // Check for SQL entry with content and bind variables
        TraceEntry sqlEntry = findEntryByType(entries, "SQL");
        assertNotNull(sqlEntry, "Should have a SQL entry");
        assertNotNull(sqlEntry.content, "SQL entry should have content");
        assertTrue(sqlEntry.content.contains("SELECT * FROM PS_JOB"), "SQL content should match");
        assertTrue(sqlEntry.metadata.containsKey("bindVariables"), "SQL entry should have bind variables");

        // Check for FUNCTION entry with PeopleCode execution info
        TraceEntry functionEntry = findEntryByType(entries, "FUNCTION");
        assertNotNull(functionEntry, "Should have a FUNCTION entry");
        assertEquals("GetJobData", functionEntry.identifier);
        assertEquals(600, functionEntry.duration()); // 0.6 seconds = 600ms
    }

    /**
     * Test that the compareTraces method correctly compares entries from two environments.
     */
    @Test
    void testCompareTraces() throws IOException {
        // Create entries for two environments
        List<TraceEntry> env1Entries = createSampleEntries("ENV1");
        List<TraceEntry> env2Entries = createSampleEntries("ENV2");

        // Make some differences in env2
        // 1. Make SQL slower in env2
        TraceEntry env2SqlEntry = findEntryByType(env2Entries, "SQL");
        env2SqlEntry.endTime += 1000; // Add 1 second

        // 2. Add a unique entry to env2
        TraceEntry uniqueEntry = new TraceEntry();
        uniqueEntry.type = "STEP";
        uniqueEntry.identifier = "UNIQUE_STEP";
        uniqueEntry.startTime = 1000;
        uniqueEntry.endTime = 2000;
        env2Entries.add(uniqueEntry);

        // Create output file path
        Path outputPath = tempDir.resolve("ae_comparison_result.csv");

        // Compare the traces
        AETraceComparator.compareTraces(env1Entries, env2Entries, "ENV1", "ENV2", outputPath.toString());

        // Verify that the output files were created
        assertTrue(Files.exists(outputPath));
        assertTrue(Files.exists(Path.of(outputPath.toString().replace(".csv", "_detailed.md"))));

        // Verify the content of the CSV file
        List<String> csvLines = Files.readAllLines(outputPath);
        assertFalse(csvLines.isEmpty());

        // Should have header line + at least 3 data lines
        assertTrue(csvLines.size() >= 4);

        // Verify that the CSV contains the expected columns
        assertTrue(csvLines.get(0).contains("Type,Identifier,ENV1(ms),ENV2(ms),Diff(ms),Diff(%)"));

        // Verify that the slower SQL is flagged
        boolean foundSlowerSql = false;
        for (String line : csvLines) {
            if (line.startsWith("SQL,") && line.contains("ALERT")) {
                foundSlowerSql = true;
                break;
            }
        }
        assertTrue(foundSlowerSql, "Should flag the slower SQL");

        // Verify that the unique step is marked as UNIQUE
        boolean foundUniqueStep = false;
        for (String line : csvLines) {
            if (line.startsWith("STEP,UNIQUE_STEP") && line.contains("UNIQUE")) {
                foundUniqueStep = true;
                break;
            }
        }
        assertTrue(foundUniqueStep, "Should mark the unique step as UNIQUE");
    }

    /**
     * Helper method to create a sample trace file.
     */
    private Path createSampleTraceFile(String fileName, String content) throws IOException {
        Path filePath = tempDir.resolve(fileName);
        Files.writeString(filePath, content);
        return filePath;
    }

    /**
     * Helper method to create sample trace entries for testing.
     */
    private List<TraceEntry> createSampleEntries(String envName) {
        List<TraceEntry> entries = new ArrayList<>();

        // Create a STEP entry
        TraceEntry stepEntry = new TraceEntry();
        stepEntry.type = "STEP";
        stepEntry.identifier = "MAIN.STEP1";
        stepEntry.startTime = 0;
        stepEntry.endTime = 1000;
        entries.add(stepEntry);

        // Create a SQL entry
        TraceEntry sqlEntry = new TraceEntry();
        sqlEntry.type = "SQL";
        sqlEntry.identifier = "SQL#1";
        sqlEntry.startTime = 100;
        sqlEntry.endTime = 600;
        sqlEntry.content = "SELECT * FROM PS_JOB WHERE EMPLID = '12345'";
        sqlEntry.metadata.put("bindVariables", "EMPLID=12345");
        entries.add(sqlEntry);

        // Create a FUNCTION entry
        TraceEntry functionEntry = new TraceEntry();
        functionEntry.type = "FUNCTION";
        functionEntry.identifier = "GetJobData";
        functionEntry.startTime = 200;
        functionEntry.endTime = 800;
        functionEntry.metadata.put("pcExecution", "GetJobData()");
        Map<String, String> variables = new HashMap<>();
        variables.put("$jobRow", "Record(JOB)");
        functionEntry.metadata.put("variables", variables);
        entries.add(functionEntry);

        return entries;
    }

    /**
     * Helper method to find an entry by type and identifier.
     */
    private TraceEntry findEntryByTypeAndIdentifier(List<TraceEntry> entries, String type, String identifier) {
        return entries.stream()
                .filter(e -> e.type.equals(type) && e.identifier.equals(identifier))
                .findFirst()
                .orElse(null);
    }

    /**
     * Helper method to find an entry by type.
     */
    private TraceEntry findEntryByType(List<TraceEntry> entries, String type) {
        return entries.stream()
                .filter(e -> e.type.equals(type))
                .findFirst()
                .orElse(null);
    }
}
