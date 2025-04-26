package com.example.core.tool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AETraceComparator {

    static class TraceEntry {
        String identifier; // 可以是Step名稱、SQL摘要或Function名稱
        long startTime;
        long endTime;

        long duration() {
            return endTime - startTime;
        }
    }

    public static List<TraceEntry> parseTrace(String filePath) throws IOException {
        List<TraceEntry> entries = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(filePath));

        Pattern stepStartPattern = Pattern.compile(">>>>>>>>>>>>>>>>>>>> Begin Step: (.+)");
        Pattern stepEndPattern = Pattern.compile("<<<<<<<<<<<<<<<<<<<< End Step: (.+)");
        Pattern sqlPattern = Pattern.compile("\\(SQLExec\\)|\\(Statement Execute\\)|\\(PSAPPSRV\\)");
        Pattern functionPattern = Pattern.compile("Function: (.+)");
        Pattern methodPattern = Pattern.compile("Method: (.+)");
        Pattern timePattern = Pattern.compile("(\\d{2}:\\d{2}:\\d{2}\\.\\d{3})");

        Map<String, TraceEntry> activeEntries = new HashMap<>();
        int sqlCounter = 0;
        int functionCounter = 0;

        for (String line : lines) {
            Matcher timeMatcher = timePattern.matcher(line);

            if (timeMatcher.find()) {
                long timeMillis = parseTimeToMillis(timeMatcher.group(1));

                if (line.contains(">>>>>>>>>>>>>>>>>>>> Begin Step:")) {
                    Matcher startMatcher = stepStartPattern.matcher(line);
                    if (startMatcher.find()) {
                        String identifier = "STEP: " + startMatcher.group(1);
                        TraceEntry entry = new TraceEntry();
                        entry.identifier = identifier;
                        entry.startTime = timeMillis;
                        activeEntries.put(identifier, entry);
                    }
                } else if (line.contains("<<<<<<<<<<<<<<<<<<<< End Step:")) {
                    Matcher endMatcher = stepEndPattern.matcher(line);
                    if (endMatcher.find()) {
                        String identifier = "STEP: " + endMatcher.group(1);
                        if (activeEntries.containsKey(identifier)) {
                            TraceEntry entry = activeEntries.get(identifier);
                            entry.endTime = timeMillis;
                            entries.add(entry);
                            activeEntries.remove(identifier);
                        }
                    }
                } else if (sqlPattern.matcher(line).find()) {
                    String identifier = "SQL#" + (++sqlCounter);
                    TraceEntry entry = new TraceEntry();
                    entry.identifier = identifier;
                    entry.startTime = timeMillis;
                    activeEntries.put(identifier, entry);
                } else if (line.contains("Bind-Variables") || line.contains("\n")) {
                    // 如果有Bind-Variables或遇到新行但沒有新SQL，則關閉最後一個SQL Entry
                    if (!activeEntries.isEmpty()) {
                        String lastKey = new ArrayList<>(activeEntries.keySet()).get(activeEntries.size() - 1);
                        TraceEntry entry = activeEntries.get(lastKey);
                        if (entry.endTime == 0) { // 避免重複關閉
                            entry.endTime = timeMillis;
                            entries.add(entry);
                            activeEntries.remove(lastKey);
                        }
                    }
                } else if (functionPattern.matcher(line).find()) {
                    Matcher functionMatcher = functionPattern.matcher(line);
                    if (functionMatcher.find()) {
                        String identifier = "FUNC: " + functionMatcher.group(1) + "#" + (++functionCounter);
                        TraceEntry entry = new TraceEntry();
                        entry.identifier = identifier;
                        entry.startTime = timeMillis;
                        activeEntries.put(identifier, entry);
                    }
                } else if (methodPattern.matcher(line).find()) {
                    Matcher methodMatcher = methodPattern.matcher(line);
                    if (methodMatcher.find()) {
                        String identifier = "METH: " + methodMatcher.group(1) + "#" + (++functionCounter);
                        TraceEntry entry = new TraceEntry();
                        entry.identifier = identifier;
                        entry.startTime = timeMillis;
                        activeEntries.put(identifier, entry);
                    }
                }
            }
        }

        return entries;
    }

    private static long parseTimeToMillis(String timeStr) {
        String[] parts = timeStr.split("[:\\.]");
        int hh = Integer.parseInt(parts[0]);
        int mm = Integer.parseInt(parts[1]);
        int ss = Integer.parseInt(parts[2]);
        int ms = Integer.parseInt(parts[3]);
        return (hh * 3600 + mm * 60 + ss) * 1000L + ms;
    }

    public static void compareTraces(List<TraceEntry> sitEntries, List<TraceEntry> uatEntries) throws IOException {
        Map<String, TraceEntry> sitMap = new HashMap<>();
        for (TraceEntry entry : sitEntries) {
            sitMap.put(entry.identifier, entry);
        }

        List<String> outputLines = new ArrayList<>();
        outputLines.add("Identifier,SIT(ms),UAT(ms),Diff(ms),Flag");

        System.out.printf("%-60s %-15s %-15s %-10s %-10s\n", "Identifier", "SIT (ms)", "UAT (ms)", "Diff", "Flag");
        System.out.println("=".repeat(120));

        for (TraceEntry uatEntry : uatEntries) {
            TraceEntry sitEntry = sitMap.get(uatEntry.identifier);
            if (sitEntry != null) {
                long sitTime = sitEntry.duration();
                long uatTime = uatEntry.duration();
                long diff = uatTime - sitTime;
                String flag = Math.abs(diff) > sitTime * 0.2 ? "ALERT" : "";
                System.out.printf("%-60s %-15d %-15d %-10d %-10s\n", uatEntry.identifier, sitTime, uatTime, diff, flag);
                outputLines.add(String.format("%s,%d,%d,%d,%s", uatEntry.identifier, sitTime, uatTime, diff, flag));
            }
        }

        Files.write(Paths.get("trace_comparison_result.csv"), outputLines);
        System.out.println("\n比對結果已輸出到 trace_comparison_result.csv");
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Usage: java TraceComparator <SIT_trace_file> <UAT_trace_file>");
            return;
        }

        List<TraceEntry> sitEntries = parseTrace(args[0]);
        List<TraceEntry> uatEntries = parseTrace(args[1]);

        compareTraces(sitEntries, uatEntries);
    }

}
