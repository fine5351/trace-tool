package com.example.core.tool;

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
 * 一個用於比較不同環境中SQR跟蹤文件的工具。
 * 此工具分析SQR跟蹤文件中的執行時間、方法調用和SQL調用。
 *
 * <p>SQRTraceComparator可以處理不同的跟蹤參數格式：</p>
 * <ul>
 *   <li>標準格式：基本跟蹤信息</li>
 *   <li>詳細SQL格式：包括SQL執行計劃和統計信息（-S參數）</li>
 *   <li>詳細時間格式：包括詳細的時間信息（-RT參數）</li>
 *   <li>詳細結果格式：包括結果集信息（-RS參數）</li>
 * </ul>
 *
 * <p>該工具自動檢測跟蹤格式並相應地解析文件。
 * 然後比較來自兩個不同環境的跟蹤條目並生成：</p>
 * <ul>
 *   <li>包含差異摘要的CSV文件</li>
 *   <li>包含深入分析差異的詳細Markdown報告</li>
 *   <li>包含最重要差異的控制台輸出</li>
 * </ul>
 *
 * <p>使用方法：</p>
 * <pre>
 * java SQRTraceComparator &lt;env1_trace_file&gt; &lt;env2_trace_file&gt; [env1_name] [env2_name] [output_path]
 * </pre>
 *
 * <p>示例：</p>
 * <pre>
 * java SQRTraceComparator sit_trace.log uat_trace.log SIT UAT sqr_comparison_result.csv
 * </pre>
 */
@Slf4j
public class SQRTraceComparator {

    /**
     * 檢測SQR跟蹤文件的格式。
     *
     * @param filePath SQR跟蹤文件的路徑
     * @return 檢測到的跟蹤格式
     * @throws IOException 如果文件無法讀取
     */
    public static TraceFormat detectTraceFormat(String filePath) throws IOException {
        // 讀取更多行以提高檢測準確性，但限制在文件大小範圍內
        long fileLineCount = Files.lines(Paths.get(filePath)).count();
        int sampleSize = Math.min(500, (int) fileLineCount);
        List<String> sampleLines = Files.readAllLines(Paths.get(filePath)).subList(0, sampleSize);

        boolean hasDetailedSql = false;
        boolean hasDetailedTime = false;
        boolean hasDetailedResult = false;

        // 擴展檢測模式以支持更多變體
        for (String line : sampleLines) {
            // SQL詳細信息檢測
            if (line.contains("SQL执行计划") || line.contains("SQL执行详情") ||
                line.contains("SQL statement") || line.contains("SQL语句") ||
                line.contains("SQL统计信息") || line.contains("SQL execution plan")) {
                hasDetailedSql = true;
            }

            // 時間詳細信息檢測
            if (line.contains("运行时间详情") || line.contains("执行时间分析") ||
                line.contains("时间分布") || line.contains("Time breakdown") ||
                line.contains("Performance statistics") || line.contains("性能统计")) {
                hasDetailedTime = true;
            }

            // 結果集詳細信息檢測
            if (line.contains("结果集数据") || line.contains("返回数据详情") ||
                line.contains("Result set") || line.contains("结果集") ||
                line.contains("Returned rows") || line.contains("返回行数")) {
                hasDetailedResult = true;
            }

            // 如果已經檢測到所有類型，可以提前結束
            if (hasDetailedSql && hasDetailedTime && hasDetailedResult) {
                break;
            }
        }

        // 使用新的工廠方法創建適當的TraceFormat
        return TraceFormat.fromFeatures(hasDetailedSql, hasDetailedTime, hasDetailedResult);
    }

    static class TraceEntry {
        String identifier; // 程序名稱、過程名稱或SQL語句
        String type;       // PROGRAM, PROCEDURE, SQL, VARIABLE
        long startTime;    // 以毫秒為單位
        long endTime;      // 以毫秒為單位
        String content;    // 額外內容，如SQL語句文本
        Map<String, Object> metadata; // 條目的額外元數據

        TraceEntry() {
            this.metadata = new HashMap<>();
        }

        long duration() {
            return endTime - startTime;
        }
    }

    /**
     * 解析SQR跟蹤文件並提取跟蹤條目。
     *
     * @param filePath SQR跟蹤文件的路徑
     * @return 跟蹤條目列表
     * @throws IOException 如果文件無法讀取
     */
    public static List<TraceEntry> parseTrace(String filePath) throws IOException {
        return parseTrace(filePath, detectTraceFormat(filePath));
    }

    /**
     * 解析SQR跟蹤文件並提取跟蹤條目。
     *
     * @param filePath SQR跟蹤文件的路徑
     * @param format 跟蹤格式
     * @return 跟蹤條目列表
     * @throws IOException 如果文件無法讀取
     */
    public static List<TraceEntry> parseTrace(String filePath, TraceFormat format) throws IOException {
        List<TraceEntry> entries = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(filePath));

        log.info("Detected trace format: {}", format);

        // 不同跟蹤條目類型的模式
        Pattern programStartPattern = Pattern.compile("SQR开始执行: (\\d{4}-\\d{2}-\\d{2} (\\d{2}:\\d{2}:\\d{2}))");
        Pattern programEndPattern = Pattern.compile("SQR结束执行: (\\d{4}-\\d{2}-\\d{2} (\\d{2}:\\d{2}:\\d{2}))");
        Pattern programNamePattern = Pattern.compile("程序: (.+\\.SQR)");

        Pattern sqlStartPattern = Pattern.compile("执行SQL \\((\\d{2}:\\d{2}:\\d{2})\\):");
        Pattern sqlEndPattern = Pattern.compile("执行时间: ([\\d.]+)秒");

        // 詳細SQL格式的額外模式
        Pattern sqlPlanPattern = format.hasDetailedSql() ?
                Pattern.compile("SQL执行计划:|SQL execution plan:|SQL Plan:") : null;
        Pattern sqlStatsPattern = format.hasDetailedSql() ?
                Pattern.compile("SQL统计信息:|SQL statistics:|SQL Stats:") : null;

        // 詳細時間格式的額外模式
        Pattern timeBreakdownPattern = format.hasDetailedTime() ?
                Pattern.compile("时间分布:|Time breakdown:|Performance details:") : null;

        // 詳細結果格式的額外模式
        Pattern resultSetPattern = format.hasDetailedResult() ?
                Pattern.compile("结果集:|Result set:|Returned data:") : null;

        Pattern procStartPattern = Pattern.compile("开始过程: (.+) \\((\\d{2}:\\d{2}:\\d{2})\\)");
        Pattern procEndPattern = Pattern.compile("结束过程: (.+) \\((\\d{2}:\\d{2}:\\d{2})\\)");
        Pattern procDurationPattern = Pattern.compile("过程执行时间: (\\d+)秒");

        Pattern varAssignPattern = Pattern.compile("变量赋值 \\((\\d{2}:\\d{2}:\\d{2})\\):");

        Map<String, TraceEntry> activeEntries = new HashMap<>();
        StringBuilder currentSql = new StringBuilder();
        StringBuilder currentSqlPlan = new StringBuilder();
        StringBuilder currentSqlStats = new StringBuilder();
        StringBuilder currentResultSet = new StringBuilder();
        boolean collectingSql = false;
        boolean collectingSqlPlan = false;
        boolean collectingSqlStats = false;
        boolean collectingResultSet = false;
        String currentProgramName = "";
        int sqlCounter = 0;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            // 程序開始
            Matcher programStartMatcher = programStartPattern.matcher(line);
            if (programStartMatcher.find()) {
                String timeStr = programStartMatcher.group(2);
                long timeMillis = parseTimeToMillis(timeStr);

                // 在接下來的幾行中尋找程序名稱
                for (int j = i + 1; j < Math.min(i + 5, lines.size()); j++) {
                    Matcher programNameMatcher = programNamePattern.matcher(lines.get(j));
                    if (programNameMatcher.find()) {
                        currentProgramName = programNameMatcher.group(1);
                        break;
                    }
                }

                TraceEntry entry = new TraceEntry();
                entry.identifier = "PROGRAM: " + currentProgramName;
                entry.type = "PROGRAM";
                entry.startTime = timeMillis;
                activeEntries.put(entry.identifier, entry);
                continue;
            }

            // 程序結束
            Matcher programEndMatcher = programEndPattern.matcher(line);
            if (programEndMatcher.find()) {
                String timeStr = programEndMatcher.group(2);
                long timeMillis = parseTimeToMillis(timeStr);

                String identifier = "PROGRAM: " + currentProgramName;
                if (activeEntries.containsKey(identifier)) {
                    TraceEntry entry = activeEntries.get(identifier);
                    entry.endTime = timeMillis;
                    entries.add(entry);
                    activeEntries.remove(identifier);
                }
                continue;
            }

            // SQL執行開始
            Matcher sqlStartMatcher = sqlStartPattern.matcher(line);
            if (sqlStartMatcher.find()) {
                String timeStr = sqlStartMatcher.group(1);
                long timeMillis = parseTimeToMillis(timeStr);

                sqlCounter++;
                TraceEntry entry = new TraceEntry();
                entry.identifier = "SQL#" + sqlCounter;
                entry.type = "SQL";
                entry.startTime = timeMillis;
                activeEntries.put(entry.identifier, entry);

                // 開始收集SQL文本
                collectingSql = true;
                currentSql = new StringBuilder();
                continue;
            }

            // SQL執行計劃（用於包含詳細SQL信息的格式）
            if (format.hasDetailedSql() && sqlPlanPattern != null) {
                Matcher sqlPlanMatcher = sqlPlanPattern.matcher(line);
                if (sqlPlanMatcher.find()) {
                    collectingSqlPlan = true;
                    currentSqlPlan = new StringBuilder();
                    continue;
                }
            }

            // SQL統計信息（用於包含詳細SQL信息的格式）
            if (format.hasDetailedSql() && sqlStatsPattern != null) {
                Matcher sqlStatsMatcher = sqlStatsPattern.matcher(line);
                if (sqlStatsMatcher.find()) {
                    collectingSqlPlan = false; // 如果正在收集計劃，則結束收集
                    collectingSqlStats = true;
                    currentSqlStats = new StringBuilder();
                    continue;
                }
            }

            // 結果集（用於包含詳細結果集信息的格式）
            if (format.hasDetailedResult() && resultSetPattern != null) {
                Matcher resultSetMatcher = resultSetPattern.matcher(line);
                if (resultSetMatcher.find()) {
                    collectingResultSet = true;
                    currentResultSet = new StringBuilder();
                    continue;
                }
            }

            // 收集SQL文本
            if (collectingSql) {
                Matcher sqlEndMatcher = sqlEndPattern.matcher(line);
                if (sqlEndMatcher.find()) {
                    collectingSql = false;
                    double seconds = Double.parseDouble(sqlEndMatcher.group(1));
                    long durationMillis = (long)(seconds * 1000);

                    String identifier = "SQL#" + sqlCounter;
                    if (activeEntries.containsKey(identifier)) {
                        TraceEntry entry = activeEntries.get(identifier);
                        entry.content = currentSql.toString().trim();
                        entry.endTime = entry.startTime + durationMillis;

                        // 如果我們處於包含詳細時間信息的格式，尋找時間分解
                        if (format.hasDetailedTime() && timeBreakdownPattern != null) {
                            // 向前查找時間分解
                            for (int j = i + 1; j < Math.min(i + 10, lines.size()); j++) {
                                Matcher timeBreakdownMatcher = timeBreakdownPattern.matcher(lines.get(j));
                                if (timeBreakdownMatcher.find()) {
                                    StringBuilder timeBreakdown = new StringBuilder();
                                    // 收集時間分解行
                                    for (int k = j + 1; k < Math.min(j + 10, lines.size()); k++) {
                                        String breakdownLine = lines.get(k).trim();
                                        if (breakdownLine.isEmpty()) break;
                                        timeBreakdown.append(breakdownLine).append("\n");
                                    }
                                    entry.metadata.put("timeBreakdown", timeBreakdown.toString().trim());
                                    break;
                                }
                            }
                        }

                        entries.add(entry);
                        activeEntries.remove(identifier);
                    }
                } else if (!line.contains("返回行数:") && !line.contains("执行时间:")) {
                    // 如果不是元數據，則將行添加到SQL文本
                    currentSql.append(line).append("\n");
                }
                continue;
            }

            // 收集SQL計劃
            if (collectingSqlPlan) {
                if (line.trim().isEmpty() || (sqlStatsPattern != null && sqlStatsPattern.matcher(line).find())) {
                    collectingSqlPlan = false;

                    // 將SQL計劃存儲在最近的SQL條目中
                    String identifier = "SQL#" + sqlCounter;
                    if (activeEntries.containsKey(identifier)) {
                        TraceEntry entry = activeEntries.get(identifier);
                        entry.metadata.put("sqlPlan", currentSqlPlan.toString().trim());
                    }

                    if (sqlStatsPattern != null && sqlStatsPattern.matcher(line).find()) {
                        collectingSqlStats = true;
                        currentSqlStats = new StringBuilder();
                    }
                } else {
                    currentSqlPlan.append(line).append("\n");
                }
                continue;
            }

            // 收集SQL統計信息
            if (collectingSqlStats) {
                if (line.trim().isEmpty()) {
                    collectingSqlStats = false;

                    // 將SQL統計信息存儲在最近的SQL條目中
                    String identifier = "SQL#" + sqlCounter;
                    if (activeEntries.containsKey(identifier)) {
                        TraceEntry entry = activeEntries.get(identifier);
                        entry.metadata.put("sqlStats", currentSqlStats.toString().trim());
                    }
                } else {
                    currentSqlStats.append(line).append("\n");
                }
                continue;
            }

            // 收集結果集
            if (collectingResultSet) {
                if (line.trim().isEmpty()) {
                    collectingResultSet = false;

                    // 將結果集存儲在最近的SQL條目中
                    String identifier = "SQL#" + sqlCounter;
                    if (activeEntries.containsKey(identifier)) {
                        TraceEntry entry = activeEntries.get(identifier);
                        entry.metadata.put("resultSet", currentResultSet.toString().trim());
                    }
                } else {
                    currentResultSet.append(line).append("\n");
                }
                continue;
            }

            // Procedure start
            Matcher procStartMatcher = procStartPattern.matcher(line);
            if (procStartMatcher.find()) {
                String procName = procStartMatcher.group(1);
                String timeStr = procStartMatcher.group(2);
                long timeMillis = parseTimeToMillis(timeStr);

                String identifier = "PROC: " + procName;
                TraceEntry entry = new TraceEntry();
                entry.identifier = identifier;
                entry.type = "PROCEDURE";
                entry.startTime = timeMillis;
                activeEntries.put(identifier, entry);
                continue;
            }

            // Procedure end
            Matcher procEndMatcher = procEndPattern.matcher(line);
            if (procEndMatcher.find()) {
                String procName = procEndMatcher.group(1);
                String timeStr = procEndMatcher.group(2);
                long timeMillis = parseTimeToMillis(timeStr);

                String identifier = "PROC: " + procName;
                if (activeEntries.containsKey(identifier)) {
                    TraceEntry entry = activeEntries.get(identifier);
                    entry.endTime = timeMillis;

                    // Look for procedure duration
                    Matcher procDurationMatcher = procDurationPattern.matcher(line);
                    if (procDurationMatcher.find()) {
                        int seconds = Integer.parseInt(procDurationMatcher.group(1));
                        entry.metadata.put("reportedDuration", seconds * 1000L); // Convert to milliseconds
                    }

                    // If we're in a format with detailed time information, look for time breakdown
                    if (format.hasDetailedTime() && timeBreakdownPattern != null) {
                        // Look ahead for time breakdown
                        for (int j = i + 1; j < Math.min(i + 10, lines.size()); j++) {
                            Matcher timeBreakdownMatcher = timeBreakdownPattern.matcher(lines.get(j));
                            if (timeBreakdownMatcher.find()) {
                                StringBuilder timeBreakdown = new StringBuilder();
                                // Collect time breakdown lines
                                for (int k = j + 1; k < Math.min(j + 10, lines.size()); k++) {
                                    String breakdownLine = lines.get(k).trim();
                                    if (breakdownLine.isEmpty()) break;
                                    timeBreakdown.append(breakdownLine).append("\n");
                                }
                                entry.metadata.put("timeBreakdown", timeBreakdown.toString().trim());
                                break;
                            }
                        }
                    }

                    entries.add(entry);
                    activeEntries.remove(identifier);
                }
                continue;
            }

            // Variable assignment
            Matcher varAssignMatcher = varAssignPattern.matcher(line);
            if (varAssignMatcher.find()) {
                String timeStr = varAssignMatcher.group(1);
                long timeMillis = parseTimeToMillis(timeStr);

                // Look for variable value in next line
                if (i + 1 < lines.size()) {
                    String varLine = lines.get(i + 1);
                    TraceEntry entry = new TraceEntry();
                    entry.identifier = "VAR: " + varLine.trim();
                    entry.type = "VARIABLE";
                    entry.startTime = timeMillis;
                    entry.endTime = timeMillis; // Variable assignments are instantaneous
                    entries.add(entry);
                }
            }
        }

        return entries;
    }

    /**
     * Compare two trace files and output the differences.
     *
     * @param env1Entries Trace entries from first environment
     * @param env2Entries Trace entries from second environment
     * @param env1Name Name of first environment
     * @param env2Name Name of second environment
     * @param outputPath Path to output CSV file
     * @throws IOException If output file cannot be written
     */
    public static void compareTraces(
            List<TraceEntry> env1Entries,
            List<TraceEntry> env2Entries,
            String env1Name,
            String env2Name,
            String outputPath) throws IOException {

        Map<String, TraceEntry> env1Map = new HashMap<>();
        for (TraceEntry entry : env1Entries) {
            env1Map.put(entry.identifier, entry);
        }

        List<String> outputLines = new ArrayList<>();
        outputLines.add("Type,Identifier," + env1Name + "(ms)," + env2Name + "(ms),Diff(ms),Diff(%),Flag,Details");

        log.info("Type        Identifier                                        {} (ms)       {} (ms)       Diff      Diff(%)    Flag      Details", env1Name, env2Name);
        log.info("{}", "=".repeat(140));

        // Create detailed report file
        List<String> detailedReport = new ArrayList<>();
        detailedReport.add("# SQR Trace Comparison Detailed Report");
        detailedReport.add("## Comparison between " + env1Name + " and " + env2Name);
        detailedReport.add("");

        for (TraceEntry env2Entry : env2Entries) {
            TraceEntry env1Entry = env1Map.get(env2Entry.identifier);
            if (env1Entry != null) {
                long env1Time = env1Entry.duration();
                long env2Time = env2Entry.duration();
                long diff = env2Time - env1Time;
                double diffPercent = env1Time > 0 ? (diff * 100.0 / env1Time) : 0;

                // Flag significant differences (>20% and >100ms)
                String flag = (Math.abs(diff) > env1Time * 0.2 && Math.abs(diff) > 100) ? "ALERT" : "";

                // Check for additional details to compare
                StringBuilder details = new StringBuilder();

                // Compare SQL execution plans if available
                if (env1Entry.type.equals("SQL") && env2Entry.type.equals("SQL")) {
                    String env1Plan = (String) env1Entry.metadata.getOrDefault("sqlPlan", "");
                    String env2Plan = (String) env2Entry.metadata.getOrDefault("sqlPlan", "");

                    if (!env1Plan.isEmpty() && !env2Plan.isEmpty() && !env1Plan.equals(env2Plan)) {
                        details.append("SQL Plan differs; ");

                        // Add to detailed report
                        detailedReport.add("### SQL Plan Difference for " + env2Entry.identifier);
                        detailedReport.add("#### " + env1Name + " Plan:");
                        detailedReport.add("```");
                        detailedReport.add(env1Plan);
                        detailedReport.add("```");
                        detailedReport.add("#### " + env2Name + " Plan:");
                        detailedReport.add("```");
                        detailedReport.add(env2Plan);
                        detailedReport.add("```");
                        detailedReport.add("");
                    }

                    // Compare SQL statistics if available
                    String env1Stats = (String) env1Entry.metadata.getOrDefault("sqlStats", "");
                    String env2Stats = (String) env2Entry.metadata.getOrDefault("sqlStats", "");

                    if (!env1Stats.isEmpty() && !env2Stats.isEmpty() && !env1Stats.equals(env2Stats)) {
                        details.append("SQL Stats differs; ");

                        // Add to detailed report
                        detailedReport.add("### SQL Statistics Difference for " + env2Entry.identifier);
                        detailedReport.add("#### " + env1Name + " Statistics:");
                        detailedReport.add("```");
                        detailedReport.add(env1Stats);
                        detailedReport.add("```");
                        detailedReport.add("#### " + env2Name + " Statistics:");
                        detailedReport.add("```");
                        detailedReport.add(env2Stats);
                        detailedReport.add("```");
                        detailedReport.add("");
                    }

                    // Compare result sets if available
                    String env1ResultSet = (String) env1Entry.metadata.getOrDefault("resultSet", "");
                    String env2ResultSet = (String) env2Entry.metadata.getOrDefault("resultSet", "");

                    if (!env1ResultSet.isEmpty() && !env2ResultSet.isEmpty() && !env1ResultSet.equals(env2ResultSet)) {
                        details.append("Result Set differs; ");

                        // Add to detailed report
                        detailedReport.add("### Result Set Difference for " + env2Entry.identifier);
                        detailedReport.add("#### " + env1Name + " Result Set:");
                        detailedReport.add("```");
                        detailedReport.add(env1ResultSet);
                        detailedReport.add("```");
                        detailedReport.add("#### " + env2Name + " Result Set:");
                        detailedReport.add("```");
                        detailedReport.add(env2ResultSet);
                        detailedReport.add("```");
                        detailedReport.add("");
                    }
                }

                // Compare time breakdowns if available
                String env1TimeBreakdown = (String) env1Entry.metadata.getOrDefault("timeBreakdown", "");
                String env2TimeBreakdown = (String) env2Entry.metadata.getOrDefault("timeBreakdown", "");

                if (!env1TimeBreakdown.isEmpty() && !env2TimeBreakdown.isEmpty() && !env1TimeBreakdown.equals(env2TimeBreakdown)) {
                    details.append("Time Breakdown differs; ");

                    // Add to detailed report
                    detailedReport.add("### Time Breakdown Difference for " + env2Entry.identifier);
                    detailedReport.add("#### " + env1Name + " Time Breakdown:");
                    detailedReport.add("```");
                    detailedReport.add(env1TimeBreakdown);
                    detailedReport.add("```");
                    detailedReport.add("#### " + env2Name + " Time Breakdown:");
                    detailedReport.add("```");
                    detailedReport.add(env2TimeBreakdown);
                    detailedReport.add("```");
                    detailedReport.add("");
                }

                // Compare reported durations if available
                Object env1ReportedDuration = env1Entry.metadata.get("reportedDuration");
                Object env2ReportedDuration = env2Entry.metadata.get("reportedDuration");

                if (env1ReportedDuration != null && env2ReportedDuration != null) {
                    long env1RepDur = (long) env1ReportedDuration;
                    long env2RepDur = (long) env2ReportedDuration;
                    long repDiff = env2RepDur - env1RepDur;
                    double repDiffPercent = env1RepDur > 0 ? (repDiff * 100.0 / env1RepDur) : 0;

                    if (Math.abs(repDiff) > env1RepDur * 0.2 && Math.abs(repDiff) > 100) {
                        details.append(String.format("Reported Duration: %d vs %d ms (%+.2f%%); ",
                                env1RepDur, env2RepDur, repDiffPercent));
                    }
                }

                String detailsStr = details.toString();
                if (detailsStr.endsWith("; ")) {
                    detailsStr = detailsStr.substring(0, detailsStr.length() - 2);
                }

                log.info("{} {} {} {} {} {:.2f} {} {}",
                        env2Entry.type, env2Entry.identifier, env1Time, env2Time, diff, diffPercent, flag,
                        detailsStr.length() > 20 ? detailsStr.substring(0, 17) + "..." : detailsStr);

                outputLines.add(String.format("%s,%s,%d,%d,%d,%.2f,%s,\"%s\"",
                        env2Entry.type, env2Entry.identifier, env1Time, env2Time, diff, diffPercent, flag, detailsStr));

                // Add entry to detailed report if it has an ALERT flag
                if (flag.equals("ALERT")) {
                    detailedReport.add("### " + flag + ": " + env2Entry.type + " - " + env2Entry.identifier);
                    detailedReport.add("* " + env1Name + " Duration: " + env1Time + " ms");
                    detailedReport.add("* " + env2Name + " Duration: " + env2Time + " ms");
                    detailedReport.add("* Difference: " + diff + " ms (" + String.format("%.2f", diffPercent) + "%)");
                    if (!detailsStr.isEmpty()) {
                        detailedReport.add("* Details: " + detailsStr);
                    }
                    detailedReport.add("");
                }
            } else {
                // Entry only exists in env2
                log.info("{} {} {} {} {} {} {} {}",
                        env2Entry.type, env2Entry.identifier, "N/A", env2Entry.duration(), "N/A", "N/A", "UNIQUE", "");

                outputLines.add(String.format("%s,%s,%s,%d,%s,%s,%s,%s",
                        env2Entry.type, env2Entry.identifier, "N/A", env2Entry.duration(), "N/A", "N/A", "UNIQUE", ""));

                // Add unique entry to detailed report
                detailedReport.add("### UNIQUE: " + env2Entry.type + " - " + env2Entry.identifier);
                detailedReport.add("* Only exists in " + env2Name);
                detailedReport.add("* Duration: " + env2Entry.duration() + " ms");
                detailedReport.add("");
            }
        }

        // Check for entries only in env1
        for (TraceEntry env1Entry : env1Entries) {
            boolean foundInEnv2 = env2Entries.stream()
                    .anyMatch(e -> e.identifier.equals(env1Entry.identifier));

            if (!foundInEnv2) {
                System.out.printf("%-10s %-50s %-15d %-15s %-10s %-10s %-10s %-20s\n",
                        env1Entry.type, env1Entry.identifier, env1Entry.duration(), "N/A", "N/A", "N/A", "MISSING", "");

                outputLines.add(String.format("%s,%s,%d,%s,%s,%s,%s,%s",
                        env1Entry.type, env1Entry.identifier, env1Entry.duration(), "N/A", "N/A", "N/A", "MISSING", ""));

                // Add missing entry to detailed report
                detailedReport.add("### MISSING: " + env1Entry.type + " - " + env1Entry.identifier);
                detailedReport.add("* Only exists in " + env1Name);
                detailedReport.add("* Duration: " + env1Entry.duration() + " ms");
                detailedReport.add("");
            }
        }

        // Write CSV output
        Files.write(Paths.get(outputPath), outputLines);
        System.out.println("\n比對結果已輸出到 " + outputPath);

        // Write detailed report
        String detailedReportPath = outputPath.replace(".csv", "_detailed.md");
        Files.write(Paths.get(detailedReportPath), detailedReport);
        System.out.println("詳細報告已輸出到 " + detailedReportPath);
    }

    /**
     * Parse time string to milliseconds since midnight.
     *
     * @param timeStr Time string in format HH:MM:SS or HH:MM:SS.SSS
     * @return Milliseconds since midnight
     */
    private static long parseTimeToMillis(String timeStr) {
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

    /**
     * 表示不同SQR跟蹤參數格式的枚舉
     * 使用位運算支持混合格式
     */
    public enum TraceFormat {
        STANDARD(0),       // 包含基本信息的標準格式
        DETAILED_SQL(1),   // 包含詳細SQL信息（-S參數）
        DETAILED_TIME(2),  // 包含詳細時間信息（-RT參數）
        DETAILED_RESULT(4), // 包含詳細結果集信息（-RS參數）
        // 混合格式
        DETAILED_SQL_TIME(3),      // SQL + 時間 (-S -RT)
        DETAILED_SQL_RESULT(5),    // SQL + 結果 (-S -RS)
        DETAILED_TIME_RESULT(6),   // 時間 + 結果 (-RT -RS)
        DETAILED_ALL(7);           // 所有詳細信息 (-S -RT -RS)

        private final int value;

        TraceFormat(int value) {
            this.value = value;
        }

        public static TraceFormat fromFeatures(boolean hasDetailedSql, boolean hasDetailedTime, boolean hasDetailedResult) {
            int value = 0;
            if (hasDetailedSql) value |= DETAILED_SQL.value;
            if (hasDetailedTime) value |= DETAILED_TIME.value;
            if (hasDetailedResult) value |= DETAILED_RESULT.value;

            for (TraceFormat format : values()) {
                if (format.value == value) {
                    return format;
                }
            }
            return STANDARD;
        }

        public int getValue() {
            return value;
        }

        public boolean hasDetailedSql() {
            return (value & DETAILED_SQL.value) != 0;
        }

        public boolean hasDetailedTime() {
            return (value & DETAILED_TIME.value) != 0;
        }

        public boolean hasDetailedResult() {
            return (value & DETAILED_RESULT.value) != 0;
        }
    }

    /**
     * Parse trace parameters string and convert to TraceFormat.
     * Handles multiple parameters like "-S -TIMING -debugfgt -E".
     *
     * @param traceParams Trace parameters string
     * @return The appropriate TraceFormat
     */
    public static TraceFormat parseTraceParams(String traceParams) {
        if (traceParams == null || traceParams.isEmpty()) {
            return TraceFormat.STANDARD;
        }

        boolean hasDetailedSql = false;
        boolean hasDetailedTime = false;
        boolean hasDetailedResult = false;

        // Convert to uppercase for case-insensitive matching
        String upperCaseParams = traceParams.toUpperCase();

        // Check for SQL detailed info parameters
        if (upperCaseParams.contains("-S") || upperCaseParams.contains("-SQL")) {
            hasDetailedSql = true;
        }

        // Check for timing detailed info parameters
        if (upperCaseParams.contains("-TIMING") || upperCaseParams.contains("-RT") ||
            upperCaseParams.contains("-TIME")) {
            hasDetailedTime = true;
        }

        // Check for result set detailed info parameters
        if (upperCaseParams.contains("-E") || upperCaseParams.contains("-RS") ||
            upperCaseParams.contains("-RESULT")) {
            hasDetailedResult = true;
        }

        // Use the factory method to create the appropriate format
        return TraceFormat.fromFeatures(hasDetailedSql, hasDetailedTime, hasDetailedResult);
    }

    /**
     * Parse trace file with specific trace parameters.
     *
     * @param filePath    Path to the trace file
     * @param traceParams Trace parameters string
     * @return List of trace entries
     * @throws IOException If the file cannot be read
     */
    public static List<TraceEntry> parseTraceWithParams(String filePath, String traceParams) throws IOException {
        TraceFormat format = parseTraceParams(traceParams);
        log.info("Using trace format: {} for parameters: {}", format, traceParams);
        return parseTrace(filePath, format);
    }

    /**
     * Main method to run the comparator.
     * Directly specifies analysis files, trace parameters, and output files.
     *
     * @throws IOException If files cannot be read or written
     */
    public static void main(String[] args) throws IOException {
        // Directly specify files and parameters in the code
        String env1TraceFile = "D:\\traces\\env1_trace.log";
        String env2TraceFile = "D:\\traces\\env2_trace.log";
        String env1Name = "DEV";
        String env2Name = "TEST";
        String outputPath = "sqr_trace_comparison_result.csv";

        // Specify trace parameters (supports multiple parameters)
        String traceParams = "-S -TIMING -debugfgt -E";

        log.info("Using parameters {} to analyze {} and {} files", traceParams, env1TraceFile, env2TraceFile);

        // Parse trace files with the specified parameters
        List<TraceEntry> env1Entries = parseTraceWithParams(env1TraceFile, traceParams);
        List<TraceEntry> env2Entries = parseTraceWithParams(env2TraceFile, traceParams);

        log.info("Parsed {} entries from {}", env1Entries.size(), env1TraceFile);
        log.info("Parsed {} entries from {}", env2Entries.size(), env2TraceFile);

        // Compare the traces
        compareTraces(env1Entries, env2Entries, env1Name, env2Name, outputPath);
    }
}
