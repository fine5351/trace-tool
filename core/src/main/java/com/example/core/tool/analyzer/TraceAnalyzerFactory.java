package com.example.core.tool.analyzer;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory for creating trace analyzers based on trace format.
 */
@Slf4j
public class TraceAnalyzerFactory {

    /**
     * Create an appropriate analyzer based on the trace format.
     *
     * @param format The trace format
     * @return The appropriate trace analyzer
     */
    public static TraceAnalyzer createAnalyzer(TraceFormat format) {
        switch (format) {
            case DETAILED_SQL:
                return new DetailedSqlTraceAnalyzer();
            case DETAILED_PC:
                return new DetailedPcTraceAnalyzer();
            case STANDARD:
            default:
                return new StandardTraceAnalyzer();
        }
    }

    /**
     * Detect the format of a trace file.
     *
     * @param filePath The path to the trace file
     * @return The detected trace format
     * @throws IOException If the file cannot be read
     */
    public static TraceFormat detectTraceFormat(String filePath) throws IOException {
        List<String> sampleLines = Files.readAllLines(Paths.get(filePath)).subList(0, Math.min(100, (int) Files.lines(Paths.get(filePath)).count()));

        boolean hasDetailedSql = false;
        boolean hasDetailedPC = false;

        for (String line : sampleLines) {
            if (line.contains("DBFLAGS") || line.contains("TOOLSTRACESQL") ||
                line.contains("SQL statement") || line.contains("Bind-Variables")) {
                hasDetailedSql = true;
            }
            if (line.contains("TOOLSTRACEPC") || line.contains("PeopleCode program") ||
                line.contains("PeopleCode Execution") || line.contains("PeopleCode trace")) {
                hasDetailedPC = true;
            }
        }

        if (hasDetailedPC) {
            return TraceFormat.DETAILED_PC;
        } else if (hasDetailedSql) {
            return TraceFormat.DETAILED_SQL;
        } else {
            return TraceFormat.STANDARD;
        }
    }

    /**
     * Create an appropriate analyzer for a trace file by detecting its format.
     *
     * @param filePath The path to the trace file
     * @return The appropriate trace analyzer
     * @throws IOException If the file cannot be read
     */
    public static TraceAnalyzer createAnalyzerForFile(String filePath) throws IOException {
        TraceFormat format = detectTraceFormat(filePath);
        log.info("Detected trace format for {}: {}", filePath, format);
        return createAnalyzer(format);
    }

    /**
     * Create an analyzer based on the specified trace parameter.
     *
     * @param traceParam The trace parameter (e.g., "TRACE", "DBFLAGS", "TOOLSTRACEPC")
     * @return The appropriate trace analyzer
     */
    public static TraceAnalyzer createAnalyzerForParam(String traceParam) {
        if (traceParam == null || traceParam.isEmpty()) {
            return new StandardTraceAnalyzer();
        }

        traceParam = traceParam.toUpperCase();

        if (traceParam.contains("TOOLSTRACEPC")) {
            return new DetailedPcTraceAnalyzer();
        } else if (traceParam.contains("DBFLAGS") || traceParam.contains("TOOLSTRACESQL")) {
            return new DetailedSqlTraceAnalyzer();
        } else {
            return new StandardTraceAnalyzer();
        }
    }

    /**
     * Create multiple analyzers based on the specified trace parameters string.
     * The string can contain multiple parameters like "-TRACE 3 -TOOLSTRACEPC 4044 -TOOLSTRACESQL 31"
     *
     * @param traceParamsString The trace parameters string
     * @return List of appropriate trace analyzers
     */
    public static List<TraceAnalyzer> createAnalyzersForParams(String traceParamsString) {
        if (traceParamsString == null || traceParamsString.isEmpty()) {
            return List.of(new StandardTraceAnalyzer());
        }

        List<TraceAnalyzer> analyzers = new ArrayList<>();
        boolean hasStandardAnalyzer = false;
        boolean hasSqlAnalyzer = false;
        boolean hasPcAnalyzer = false;

        // Convert to uppercase for case-insensitive matching
        String upperCaseParams = traceParamsString.toUpperCase();

        // Check for each type of analyzer
        if (upperCaseParams.contains("TOOLSTRACEPC")) {
            analyzers.add(new DetailedPcTraceAnalyzer());
            hasPcAnalyzer = true;
        }

        if (upperCaseParams.contains("DBFLAGS") || upperCaseParams.contains("TOOLSTRACESQL")) {
            analyzers.add(new DetailedSqlTraceAnalyzer());
            hasSqlAnalyzer = true;
        }

        if (upperCaseParams.contains("TRACE") && !hasPcAnalyzer && !hasSqlAnalyzer) {
            analyzers.add(new StandardTraceAnalyzer());
            hasStandardAnalyzer = true;
        }

        // If no specific analyzers were added, use the standard analyzer
        if (analyzers.isEmpty()) {
            analyzers.add(new StandardTraceAnalyzer());
        }

        return analyzers;
    }

    /**
     * Trace format enum
     */
    public enum TraceFormat {
        STANDARD,       // Standard format with basic information (-TRACE 1)
        DETAILED_SQL,   // Detailed SQL information (-DBFLAGS or -TOOLSTRACESQL)
        DETAILED_PC     // Detailed PeopleCode information (-TOOLSTRACEPC)
    }
}
