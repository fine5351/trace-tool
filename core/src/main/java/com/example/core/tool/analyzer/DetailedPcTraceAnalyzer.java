package com.example.core.tool.analyzer;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Analyzer for AE trace format with detailed PeopleCode information (-TOOLSTRACEPC)
 */
@Slf4j
public class DetailedPcTraceAnalyzer extends StandardTraceAnalyzer {

    @Override
    public List<TraceEntry> parseTrace(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        List<TraceEntry> entries = new ArrayList<>();

        // First use the standard analyzer to get basic entries
        List<TraceEntry> standardEntries = super.parseTrace(filePath);
        entries.addAll(standardEntries);

        // Additional patterns for PeopleCode details
        Pattern methodStartPattern = Pattern.compile("(\\d{2}:\\d{2}:\\d{2}(?:\\.\\d{3})?)\\s+Method:(\\S+)\\s+started");
        Pattern methodEndPattern = Pattern.compile("(\\d{2}:\\d{2}:\\d{2}(?:\\.\\d{3})?)\\s+Method:(\\S+)\\s+ended");
        Pattern pcProgramPattern = Pattern.compile("PeopleCode program\\s+(.+)");

        // Process method entries
        Map<String, TraceEntry> activeEntries = new HashMap<>();

        for (String line : lines) {
            // Process method entries
            Matcher startMatcher = methodStartPattern.matcher(line);
            if (startMatcher.find()) {
                String time = startMatcher.group(1);
                String identifier = startMatcher.group(2);
                String entryKey = "METHOD:" + identifier;

                TraceEntry entry = new TraceEntry();
                entry.type = "METHOD";
                entry.identifier = identifier;
                entry.startTime = parseTimeToMillis(time);

                activeEntries.put(entryKey, entry);
            }

            Matcher endMatcher = methodEndPattern.matcher(line);
            if (endMatcher.find()) {
                String time = endMatcher.group(1);
                String identifier = endMatcher.group(2);
                String entryKey = "METHOD:" + identifier;

                TraceEntry entry = activeEntries.get(entryKey);
                if (entry != null) {
                    entry.endTime = parseTimeToMillis(time);
                    entries.add(entry);
                    activeEntries.remove(entryKey);
                }
            }

            // Process PeopleCode program information
            Matcher pcProgramMatcher = pcProgramPattern.matcher(line);
            if (pcProgramMatcher.find()) {
                String pcProgram = pcProgramMatcher.group(1).trim();

                // Find the most recent function or method entry to associate this with
                TraceEntry lastEntry = null;
                for (TraceEntry entry : entries) {
                    if (("FUNCTION".equals(entry.type) || "METHOD".equals(entry.type)) &&
                        (lastEntry == null || entry.startTime > lastEntry.startTime)) {
                        lastEntry = entry;
                    }
                }

                if (lastEntry != null) {
                    lastEntry.metadata.put("peopleCodeProgram", pcProgram);
                }
            }
        }

        return entries;
    }
}