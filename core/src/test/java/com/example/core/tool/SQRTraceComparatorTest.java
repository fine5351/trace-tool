package com.example.core.tool;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the SQRTraceComparator class.
 */
class SQRTraceComparatorTest {

    @TempDir
    Path tempDir;

    /**
     * Test that the trace format detection works correctly.
     */
    @Test
    void testDetectTraceFormat() throws IOException {
        // Create sample trace files with different formats
        Path standardTrace = createSampleTraceFile("standard_trace.log",
                "SQR开始执行: 2023-05-15 14:25:30\n" +
                "程序: TEST.SQR\n" +
                "执行SQL (14:25:31):\n" +
                "SELECT * FROM DUAL\n" +
                "执行时间: 1.25秒\n" +
                "SQR结束执行: 2023-05-15 14:32:45\n");

        Path detailedSqlTrace = createSampleTraceFile("detailed_sql_trace.log",
                "SQR开始执行: 2023-05-15 14:25:30\n" +
                "程序: TEST.SQR\n" +
                "执行SQL (14:25:31):\n" +
                "SELECT * FROM DUAL\n" +
                "SQL执行计划:\n" +
                "TABLE ACCESS FULL DUAL\n" +
                "执行时间: 1.25秒\n" +
                "SQR结束执行: 2023-05-15 14:32:45\n");

        Path detailedTimeTrace = createSampleTraceFile("detailed_time_trace.log",
                "SQR开始执行: 2023-05-15 14:25:30\n" +
                "程序: TEST.SQR\n" +
                "执行SQL (14:25:31):\n" +
                "SELECT * FROM DUAL\n" +
                "执行时间: 1.25秒\n" +
                "运行时间详情:\n" +
                "解析: 0.25秒\n" +
                "执行: 1.00秒\n" +
                "SQR结束执行: 2023-05-15 14:32:45\n");

        Path detailedResultTrace = createSampleTraceFile("detailed_result_trace.log",
                "SQR开始执行: 2023-05-15 14:25:30\n" +
                "程序: TEST.SQR\n" +
                "执行SQL (14:25:31):\n" +
                "SELECT * FROM DUAL\n" +
                "执行时间: 1.25秒\n" +
                "结果集数据:\n" +
                "DUMMY\n" +
                "X\n" +
                "SQR结束执行: 2023-05-15 14:32:45\n");

        // Test format detection
        assertEquals(SQRTraceComparator.TraceFormat.STANDARD,
                SQRTraceComparator.detectTraceFormat(standardTrace.toString()));

        assertEquals(SQRTraceComparator.TraceFormat.DETAILED_SQL,
                SQRTraceComparator.detectTraceFormat(detailedSqlTrace.toString()));

        assertEquals(SQRTraceComparator.TraceFormat.DETAILED_TIME,
                SQRTraceComparator.detectTraceFormat(detailedTimeTrace.toString()));

        assertEquals(SQRTraceComparator.TraceFormat.DETAILED_RESULT,
                SQRTraceComparator.detectTraceFormat(detailedResultTrace.toString()));
    }

    /**
     * Test that the trace parsing works correctly.
     */
    @Test
    void testParseTrace() throws IOException {
        // Create a sample trace file
        Path traceFile = createSampleTraceFile("parse_test.log",
                "SQR开始执行: 2023-05-15 14:25:30\n" +
                "程序: TEST.SQR\n" +
                "用户: PS\n" +
                "数据库: PSFT_HR\n" +
                "\n" +
                "执行SQL (14:25:31):\n" +
                "SELECT * FROM DUAL\n" +
                "返回行数: 1\n" +
                "执行时间: 1.25秒\n" +
                "\n" +
                "开始过程: PROCESS_DATA (14:26:05)\n" +
                "变量赋值 (14:26:10):\n" +
                "$TOTAL_AMOUNT = 15250.75\n" +
                "结束过程: PROCESS_DATA (14:26:15)\n" +
                "过程执行时间: 10秒\n" +
                "\n" +
                "SQR结束执行: 2023-05-15 14:32:45\n" +
                "总运行时间: 00:07:15\n");

        // Parse the trace file
        List<SQRTraceComparator.TraceEntry> entries = SQRTraceComparator.parseTrace(traceFile.toString());

        // Verify the parsed entries
        assertNotNull(entries);
        assertFalse(entries.isEmpty());

        // Should have 4 entries: program, SQL, procedure, variable
        assertEquals(4, entries.size());

        // Verify program entry
        SQRTraceComparator.TraceEntry programEntry = entries.stream()
                .filter(e -> e.type.equals("PROGRAM"))
                .findFirst()
                .orElse(null);
        assertNotNull(programEntry);
        assertEquals("PROGRAM: TEST.SQR", programEntry.identifier);

        // Verify SQL entry
        SQRTraceComparator.TraceEntry sqlEntry = entries.stream()
                .filter(e -> e.type.equals("SQL"))
                .findFirst()
                .orElse(null);
        assertNotNull(sqlEntry);
        assertTrue(sqlEntry.identifier.startsWith("SQL#"));
        assertEquals("SELECT * FROM DUAL", sqlEntry.content.trim());
        assertEquals(1250, sqlEntry.duration()); // 1.25 seconds = 1250 ms

        // Verify procedure entry
        SQRTraceComparator.TraceEntry procEntry = entries.stream()
                .filter(e -> e.type.equals("PROCEDURE"))
                .findFirst()
                .orElse(null);
        assertNotNull(procEntry);
        assertEquals("PROC: PROCESS_DATA", procEntry.identifier);
        assertEquals(10000, procEntry.duration()); // 10 seconds = 10000 ms

        // Verify variable entry
        SQRTraceComparator.TraceEntry varEntry = entries.stream()
                .filter(e -> e.type.equals("VARIABLE"))
                .findFirst()
                .orElse(null);
        assertNotNull(varEntry);
        assertTrue(varEntry.identifier.contains("$TOTAL_AMOUNT = 15250.75"));
    }

    /**
     * Test that the trace comparison works correctly.
     */
    @Test
    void testCompareTraces() throws IOException {
        // Create two sample trace files with some differences
        Path env1Trace = createSampleTraceFile("env1_trace.log",
                "SQR开始执行: 2023-05-15 14:25:30\n" +
                "程序: TEST.SQR\n" +
                "\n" +
                "执行SQL (14:25:31):\n" +
                "SELECT * FROM DUAL\n" +
                "返回行数: 1\n" +
                "执行时间: 1.25秒\n" +
                "\n" +
                "开始过程: PROCESS_DATA (14:26:05)\n" +
                "结束过程: PROCESS_DATA (14:26:15)\n" +
                "过程执行时间: 10秒\n" +
                "\n" +
                "SQR结束执行: 2023-05-15 14:32:45\n" +
                "总运行时间: 00:07:15\n");

        Path env2Trace = createSampleTraceFile("env2_trace.log",
                "SQR开始执行: 2023-05-15 14:25:30\n" +
                "程序: TEST.SQR\n" +
                "\n" +
                "执行SQL (14:25:31):\n" +
                "SELECT * FROM DUAL\n" +
                "返回行数: 1\n" +
                "执行时间: 2.50秒\n" + // Slower in env2
                "\n" +
                "开始过程: PROCESS_DATA (14:26:05)\n" +
                "结束过程: PROCESS_DATA (14:26:25)\n" + // Slower in env2
                "过程执行时间: 20秒\n" +
                "\n" +
                "执行SQL (14:26:30):\n" + // Additional SQL in env2
                "SELECT COUNT(*) FROM DUAL\n" +
                "返回行数: 1\n" +
                "执行时间: 0.50秒\n" +
                "\n" +
                "SQR结束执行: 2023-05-15 14:33:45\n" + // Longer total time
                "总运行时间: 00:08:15\n");

        // Create output file path
        Path outputPath = tempDir.resolve("comparison_result.csv");

        // Compare the traces
        List<SQRTraceComparator.TraceEntry> env1Entries = SQRTraceComparator.parseTrace(env1Trace.toString());
        List<SQRTraceComparator.TraceEntry> env2Entries = SQRTraceComparator.parseTrace(env2Trace.toString());

        SQRTraceComparator.compareTraces(env1Entries, env2Entries, "ENV1", "ENV2", outputPath.toString());

        // Verify that the output files were created
        assertTrue(Files.exists(outputPath));
        assertTrue(Files.exists(Path.of(outputPath.toString().replace(".csv", "_detailed.md"))));

        // Verify the content of the CSV file
        List<String> csvLines = Files.readAllLines(outputPath);
        assertFalse(csvLines.isEmpty());

        // Should have header line + at least 3 data lines (program, SQL, procedure)
        assertTrue(csvLines.size() >= 4);

        // Verify that the CSV contains the expected columns
        assertTrue(csvLines.get(0).contains("Type,Identifier,ENV1(ms),ENV2(ms),Diff(ms),Diff(%)"));

        // Verify that the SQL with increased time is flagged
        boolean foundSlowerSql = false;
        for (String line : csvLines) {
            if (line.startsWith("SQL,") && line.contains("ALERT")) {
                foundSlowerSql = true;
                break;
            }
        }
        assertTrue(foundSlowerSql, "Should flag the slower SQL");

        // Verify that the unique SQL in env2 is marked as UNIQUE
        boolean foundUniqueSql = false;
        for (String line : csvLines) {
            if (line.startsWith("SQL,") && line.contains("UNIQUE")) {
                foundUniqueSql = true;
                break;
            }
        }
        assertTrue(foundUniqueSql, "Should mark the unique SQL as UNIQUE");
    }

    /**
     * Helper method to create a sample trace file.
     */
    private Path createSampleTraceFile(String fileName, String content) throws IOException {
        Path filePath = tempDir.resolve(fileName);
        Files.writeString(filePath, content);
        return filePath;
    }
}