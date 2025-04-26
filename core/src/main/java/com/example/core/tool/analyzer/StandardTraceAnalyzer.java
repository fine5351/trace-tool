package com.example.core.tool.analyzer;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Analyzer for standard AE trace format (-TRACE 1)
 */
@Slf4j
public class StandardTraceAnalyzer implements TraceAnalyzer {

    /**
     * Parse time string to milliseconds since midnight.
     *
     * @param timeStr Time string in format HH:MM:SS or HH:MM:SS.SSS
     * @return Milliseconds since midnight
     */
    protected long parseTimeToMillis(String timeStr) {
        // Handle both formats: HH:MM:SS and HH:MM:SS.SSS
        DateTimeFormatter formatter;
        if (timeStr.contains(".")) {
            formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
        } else {
            formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        }

        LocalTime time = LocalTime.parse(timeStr, formatter);
        return time.toNanoOfDay() / 1_000_000; // Convert nanoseconds to milliseconds
    }

    @Override
    public List<TraceEntry> parseTrace(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        List<TraceEntry> entries = new ArrayList<>();

        // Patterns for different trace entry types
        Pattern stepPattern = Pattern.compile("(\\d{2}:\\d{2}:\\d{2}(?:\\.\\d{3})?)\\s+Step:(\\S+)\\s+started");
        Pattern stepEndPattern = Pattern.compile("(\\d{2}:\\d{2}:\\d{2}(?:\\.\\d{3})?)\\s+Step:(\\S+)\\s+ended");
        Pattern sqlPattern = Pattern.compile("(\\d{2}:\\d{2}:\\d{2}(?:\\.\\d{3})?)\\s+SQL:(\\S+)\\s+started");
        Pattern sqlEndPattern = Pattern.compile("(\\d{2}:\\d{2}:\\d{2}(?:\\.\\d{3})?)\\s+SQL:(\\S+)\\s+ended");
        Pattern functionPattern = Pattern.compile("(\\d{2}:\\d{2}:\\d{2}(?:\\.\\d{3})?)\\s+Function:(\\S+)\\s+started");
        Pattern functionEndPattern = Pattern.compile("(\\d{2}:\\d{2}:\\d{2}(?:\\.\\d{3})?)\\s+Function:(\\S+)\\s+ended");

        Map<String, TraceEntry> activeEntries = new HashMap<>();

        for (String line : lines) {
            // Process step entries
            processEntryType(line, stepPattern, stepEndPattern, "STEP", activeEntries, entries);

            // Process SQL entries
            processEntryType(line, sqlPattern, sqlEndPattern, "SQL", activeEntries, entries);

            // Process function entries
            processEntryType(line, functionPattern, functionEndPattern, "FUNCTION", activeEntries, entries);
        }

        return entries;
    }

    private void processEntryType(String line, Pattern startPattern, Pattern endPattern,
                                  String type, Map<String, TraceEntry> activeEntries,
                                  List<TraceEntry> entries) {
        Matcher startMatcher = startPattern.matcher(line);
        if (startMatcher.find()) {
            String time = startMatcher.group(1);
            String identifier = startMatcher.group(2);
            String entryKey = type + ":" + identifier;

            TraceEntry entry = new TraceEntry();
            entry.type = type;
            entry.identifier = identifier;
            entry.startTime = parseTimeToMillis(time);

            activeEntries.put(entryKey, entry);
        }

        Matcher endMatcher = endPattern.matcher(line);
        if (endMatcher.find()) {
            String time = endMatcher.group(1);
            String identifier = endMatcher.group(2);
            String entryKey = type + ":" + identifier;

            TraceEntry entry = activeEntries.get(entryKey);
            if (entry != null) {
                entry.endTime = parseTimeToMillis(time);
                entries.add(entry);
                activeEntries.remove(entryKey);
            }
        }
    }
}
