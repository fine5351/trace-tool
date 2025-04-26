package com.example.core.tool.analyzer;

import java.io.IOException;
import java.util.List;

/**
 * Interface for trace file analyzers.
 * Different implementations can handle different trace parameter formats.
 */
public interface TraceAnalyzer {

    /**
     * Parse a trace file and extract trace entries.
     *
     * @param filePath Path to the trace file
     * @return List of trace entries
     * @throws IOException If the file cannot be read
     */
    List<TraceEntry> parseTrace(String filePath) throws IOException;
}