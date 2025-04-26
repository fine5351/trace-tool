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
 * Analyzer for AE trace format with detailed SQL information (-DBFLAGS or -TOOLSTRACESQL)
 */
@Slf4j
public class DetailedSqlTraceAnalyzer extends StandardTraceAnalyzer {

    @Override
    public List<TraceEntry> parseTrace(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        List<TraceEntry> entries = new ArrayList<>();

        // First use the standard analyzer to get basic entries
        List<TraceEntry> standardEntries = super.parseTrace(filePath);
        entries.addAll(standardEntries);

        // Additional patterns for SQL details
        Pattern sqlStatementPattern = Pattern.compile("SQL statement:\\s*(.+)");
        Pattern bindVariablesPattern = Pattern.compile("Bind-Variables:\\s*(.+)");

        // Map to track SQL entries by identifier
        Map<String, TraceEntry> sqlEntries = new HashMap<>();
        for (TraceEntry entry : entries) {
            if ("SQL".equals(entry.type)) {
                sqlEntries.put(entry.identifier, entry);
            }
        }

        // Process SQL details
        StringBuilder currentSqlText = new StringBuilder();
        String currentSqlId = null;
        boolean collectingSql = false;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int lineNumber = i + 1; // Line numbers are 1-based

            // Check for SQL statement start
            Matcher sqlMatcher = sqlStatementPattern.matcher(line);
            if (sqlMatcher.find()) {
                collectingSql = true;
                currentSqlText = new StringBuilder();
                currentSqlText.append(sqlMatcher.group(1).trim());

                // Try to extract SQL ID from nearby lines
                for (int j = Math.max(0, i - 5); j < Math.min(lines.size(), i + 1); j++) {
                    String nearbyLine = lines.get(j);
                    if (nearbyLine.contains("SQL:")) {
                        Pattern sqlIdPattern = Pattern.compile("SQL:(\\S+)");
                        Matcher sqlIdMatcher = sqlIdPattern.matcher(nearbyLine);
                        if (sqlIdMatcher.find()) {
                            currentSqlId = sqlIdMatcher.group(1);

                            // Store the line number in the SQL entry if found
                            if (sqlEntries.containsKey(currentSqlId)) {
                                TraceEntry sqlEntry = sqlEntries.get(currentSqlId);
                                if (sqlEntry.lineNumber == -1) { // Only update if not already set
                                    sqlEntry.lineNumber = lineNumber;
                                }
                            }

                            break;
                        }
                    }
                }
                continue;
            }

            // Collect SQL text
            if (collectingSql) {
                if (line.trim().isEmpty() || line.contains("Bind-Variables:") || line.contains("SQL:")) {
                    collectingSql = false;

                    // Store SQL text in the corresponding entry
                    if (currentSqlId != null && sqlEntries.containsKey(currentSqlId)) {
                        TraceEntry sqlEntry = sqlEntries.get(currentSqlId);
                        sqlEntry.content = currentSqlText.toString().trim();
                    }
                } else {
                    currentSqlText.append("\n").append(line.trim());
                }
            }

            // Check for bind variables
            Matcher bindMatcher = bindVariablesPattern.matcher(line);
            if (bindMatcher.find()) {
                String bindVars = bindMatcher.group(1).trim();

                // Store bind variables in the corresponding entry
                if (currentSqlId != null && sqlEntries.containsKey(currentSqlId)) {
                    TraceEntry sqlEntry = sqlEntries.get(currentSqlId);
                    sqlEntry.metadata.put("bindVariables", bindVars);
                }
            }
        }

        return entries;
    }
}
