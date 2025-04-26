package com.example.core.tool.analyzer;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a trace entry with timing and metadata information.
 */
public class TraceEntry {
    public String identifier; // Can be Step name, SQL digest, or Function name
    public String type;       // STEP, SQL, FUNCTION, METHOD
    public long startTime;    // In milliseconds
    public long endTime;      // In milliseconds
    public String content;    // Additional content, such as SQL statement text
    public Map<String, Object> metadata; // Additional metadata for the entry
    public int lineNumber;    // Line number in the original trace file

    public TraceEntry() {
        this.metadata = new HashMap<>();
        this.lineNumber = -1; // Default value indicating not set
    }

    /**
     * Calculate the duration of this trace entry.
     *
     * @return Duration in milliseconds
     */
    public long duration() {
        return endTime - startTime;
    }
}
