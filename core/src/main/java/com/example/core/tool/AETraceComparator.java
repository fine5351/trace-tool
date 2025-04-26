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
 * 一個用於比較不同環境中Application Engine跟蹤文件的工具。
 * 此工具分析AE跟蹤文件中的執行時間、步驟、SQL調用、函數調用和方法調用。
 *
 * <p>AETraceComparator可以處理不同的跟蹤參數格式：</p>
 * <ul>
 *   <li>標準格式：基本跟蹤信息（-TRACE 1）</li>
 *   <li>詳細SQL格式：包括SQL執行詳情（-DBFLAGS 或 -TOOLSTRACESQL）</li>
 *   <li>詳細PeopleCode格式：包括PeopleCode執行詳情（-TOOLSTRACEPC）</li>
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
 * java AETraceComparator &lt;env1_trace_file&gt; &lt;env2_trace_file&gt; [env1_name] [env2_name] [output_path]
 * </pre>
 *
 * <p>示例：</p>
 * <pre>
 * java AETraceComparator sit_trace.log uat_trace.log SIT UAT ae_comparison_result.csv
 * </pre>
 */
@Slf4j
public class AETraceComparator {

    /**
     * 檢測AE跟蹤文件的格式。
     *
     * @param filePath AE跟蹤文件的路徑
     * @return 檢測到的跟蹤格式
     * @throws IOException 如果文件無法讀取
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
     * 解析AE跟蹤文件並提取跟蹤條目。
     *
     * @param filePath AE跟蹤文件的路徑
     * @return 跟蹤條目列表
     * @throws IOException 如果文件無法讀取
     */
    public static List<TraceEntry> parseTrace(String filePath) throws IOException {
        return parseTrace(filePath, detectTraceFormat(filePath));
    }

    /**
     * 解析AE跟蹤文件並提取跟蹤條目。
     *
     * @param filePath AE跟蹤文件的路徑
     * @param format   跟蹤格式
     * @return 跟蹤條目列表
     * @throws IOException 如果文件無法讀取
     */
    public static List<TraceEntry> parseTrace(String filePath, TraceFormat format) throws IOException {
        List<TraceEntry> entries = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(filePath));

        log.info("Detected trace format: {}", format);

        // 不同跟蹤條目類型的模式
        Pattern stepStartPattern = Pattern.compile(">>>>>>>>>>>>>>>>>>>> Begin Step: (.+)");
        Pattern stepEndPattern = Pattern.compile("<<<<<<<<<<<<<<<<<<<< End Step: (.+)");
        Pattern sqlPattern = Pattern.compile("\\(SQLExec\\)|\\(Statement Execute\\)|\\(PSAPPSRV\\)");
        Pattern functionPattern = Pattern.compile("Function: (.+)");
        Pattern methodPattern = Pattern.compile("Method: (.+)");
        Pattern timePattern = Pattern.compile("(\\d{2}:\\d{2}:\\d{2}\\.\\d{3})");

        // 詳細SQL格式的額外模式
        Pattern sqlTextPattern = format == TraceFormat.DETAILED_SQL ?
                Pattern.compile("SQL statement:(.*)") : null;
        Pattern bindVarsPattern = format == TraceFormat.DETAILED_SQL ?
                Pattern.compile("Bind-Variables:(.*)") : null;

        // 詳細PeopleCode格式的額外模式
        Pattern pcExecutionPattern = format == TraceFormat.DETAILED_PC ?
                Pattern.compile("PeopleCode Execution:(.*)") : null;
        Pattern pcVariablePattern = format == TraceFormat.DETAILED_PC ?
                Pattern.compile("Variable: (.+) = (.+)") : null;

        Map<String, TraceEntry> activeEntries = new HashMap<>();
        StringBuilder currentSql = new StringBuilder();
        StringBuilder currentBindVars = new StringBuilder();
        StringBuilder currentPcExecution = new StringBuilder();
        boolean collectingSql = false;
        boolean collectingBindVars = false;
        boolean collectingPcExecution = false;
        int sqlCounter = 0;
        int functionCounter = 0;
        int methodCounter = 0;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            Matcher timeMatcher = timePattern.matcher(line);

            if (timeMatcher.find()) {
                long timeMillis = parseTimeToMillis(timeMatcher.group(1));

                // 步驟開始
                if (line.contains(">>>>>>>>>>>>>>>>>>>> Begin Step:")) {
                    Matcher startMatcher = stepStartPattern.matcher(line);
                    if (startMatcher.find()) {
                        String stepName = startMatcher.group(1);
                        String identifier = "STEP: " + stepName;
                        TraceEntry entry = new TraceEntry();
                        entry.identifier = identifier;
                        entry.type = "STEP";
                        entry.startTime = timeMillis;
                        entry.content = stepName;
                        activeEntries.put(identifier, entry);
                    }
                }
                // 步驟結束
                else if (line.contains("<<<<<<<<<<<<<<<<<<<< End Step:")) {
                    Matcher endMatcher = stepEndPattern.matcher(line);
                    if (endMatcher.find()) {
                        String stepName = endMatcher.group(1);
                        String identifier = "STEP: " + stepName;
                        if (activeEntries.containsKey(identifier)) {
                            TraceEntry entry = activeEntries.get(identifier);
                            entry.endTime = timeMillis;
                            entries.add(entry);
                            activeEntries.remove(identifier);
                        }
                    }
                }
                // SQL執行開始
                else if (sqlPattern.matcher(line).find()) {
                    sqlCounter++;
                    String identifier = "SQL#" + sqlCounter;
                    TraceEntry entry = new TraceEntry();
                    entry.identifier = identifier;
                    entry.type = "SQL";
                    entry.startTime = timeMillis;

                    // 如果是詳細SQL格式，嘗試提取SQL文本
                    if (format == TraceFormat.DETAILED_SQL && i + 1 < lines.size()) {
                        // 查找SQL文本
                        for (int j = i + 1; j < Math.min(i + 5, lines.size()); j++) {
                            if (sqlTextPattern != null) {
                                Matcher sqlTextMatcher = sqlTextPattern.matcher(lines.get(j));
                                if (sqlTextMatcher.find()) {
                                    collectingSql = true;
                                    currentSql = new StringBuilder(sqlTextMatcher.group(1).trim());
                                    break;
                                }
                            }
                        }
                    }

                    activeEntries.put(identifier, entry);
                }
                // SQL綁定變量
                else if (format == TraceFormat.DETAILED_SQL && line.contains("Bind-Variables")) {
                    if (bindVarsPattern != null) {
                        Matcher bindVarsMatcher = bindVarsPattern.matcher(line);
                        if (bindVarsMatcher.find()) {
                            collectingBindVars = true;
                            currentBindVars = new StringBuilder(bindVarsMatcher.group(1).trim());

                            // 將綁定變量添加到最近的SQL條目
                            if (!activeEntries.isEmpty() && sqlCounter > 0) {
                                String identifier = "SQL#" + sqlCounter;
                                if (activeEntries.containsKey(identifier)) {
                                    TraceEntry entry = activeEntries.get(identifier);
                                    entry.metadata.put("bindVariables", currentBindVars.toString());
                                }
                            }
                        }
                    }

                    // 如果有Bind-Variables，則關閉最後一個SQL Entry
                    if (!activeEntries.isEmpty() && sqlCounter > 0) {
                        String identifier = "SQL#" + sqlCounter;
                        if (activeEntries.containsKey(identifier)) {
                            TraceEntry entry = activeEntries.get(identifier);
                            if (entry.endTime == 0) { // 避免重複關閉
                                entry.endTime = timeMillis;

                                // 如果收集了SQL文本，添加到條目
                                if (collectingSql && currentSql.length() > 0) {
                                    entry.content = currentSql.toString().trim();
                                    collectingSql = false;
                                }

                                entries.add(entry);
                                activeEntries.remove(identifier);
                            }
                        }
                    }
                }
                // 函數調用
                else if (functionPattern.matcher(line).find()) {
                    Matcher functionMatcher = functionPattern.matcher(line);
                    if (functionMatcher.find()) {
                        functionCounter++;
                        String funcName = functionMatcher.group(1);
                        String identifier = "FUNC: " + funcName + "#" + functionCounter;
                        TraceEntry entry = new TraceEntry();
                        entry.identifier = identifier;
                        entry.type = "FUNCTION";
                        entry.content = funcName;
                        entry.startTime = timeMillis;

                        // 如果是詳細PeopleCode格式，嘗試提取更多信息
                        if (format == TraceFormat.DETAILED_PC && pcExecutionPattern != null) {
                            for (int j = i + 1; j < Math.min(i + 5, lines.size()); j++) {
                                Matcher pcExecMatcher = pcExecutionPattern.matcher(lines.get(j));
                                if (pcExecMatcher.find()) {
                                    collectingPcExecution = true;
                                    currentPcExecution = new StringBuilder(pcExecMatcher.group(1).trim());
                                    entry.metadata.put("pcExecution", currentPcExecution.toString());
                                    break;
                                }
                            }
                        }

                        activeEntries.put(identifier, entry);
                    }
                }
                // 方法調用
                else if (methodPattern.matcher(line).find()) {
                    Matcher methodMatcher = methodPattern.matcher(line);
                    if (methodMatcher.find()) {
                        methodCounter++;
                        String methodName = methodMatcher.group(1);
                        String identifier = "METH: " + methodName + "#" + methodCounter;
                        TraceEntry entry = new TraceEntry();
                        entry.identifier = identifier;
                        entry.type = "METHOD";
                        entry.content = methodName;
                        entry.startTime = timeMillis;
                        activeEntries.put(identifier, entry);
                    }
                }
                // 檢查是否有新行但沒有新的跟蹤條目開始，可能是某個條目的結束
                else if (!line.trim().isEmpty()) {
                    // 嘗試關閉最近的活動條目
                    if (!activeEntries.isEmpty()) {
                        // 獲取最後添加的條目
                        String lastKey = new ArrayList<>(activeEntries.keySet()).get(activeEntries.size() - 1);
                        TraceEntry entry = activeEntries.get(lastKey);

                        // 如果是函數或方法，並且沒有明確的結束標記，則在這裡關閉
                        if ((entry.type.equals("FUNCTION") || entry.type.equals("METHOD")) && entry.endTime == 0) {
                            entry.endTime = timeMillis;
                            entries.add(entry);
                            activeEntries.remove(lastKey);
                        }
                        // 如果是SQL，並且沒有明確的結束標記，則在這裡關閉
                        else if (entry.type.equals("SQL") && entry.endTime == 0) {
                            entry.endTime = timeMillis;

                            // 如果收集了SQL文本，添加到條目
                            if (collectingSql && currentSql.length() > 0) {
                                entry.content = currentSql.toString().trim();
                                collectingSql = false;
                            }

                            entries.add(entry);
                            activeEntries.remove(lastKey);
                        }
                    }
                }
            }

            // 收集SQL文本（如果在收集模式）
            if (collectingSql && format == TraceFormat.DETAILED_SQL && sqlTextPattern != null) {
                // 如果遇到新的時間戳或綁定變量，則停止收集SQL文本
                if (timePattern.matcher(line).find() || line.contains("Bind-Variables")) {
                    collectingSql = false;

                    // 將SQL文本添加到最近的SQL條目
                    if (!activeEntries.isEmpty() && sqlCounter > 0) {
                        String identifier = "SQL#" + sqlCounter;
                        if (activeEntries.containsKey(identifier)) {
                            TraceEntry entry = activeEntries.get(identifier);
                            entry.content = currentSql.toString().trim();
                        }
                    }
                }
                // 否則繼續收集SQL文本
                else if (!line.trim().isEmpty()) {
                    currentSql.append(" ").append(line.trim());
                }
            }

            // 收集PeopleCode執行信息（如果在收集模式）
            if (collectingPcExecution && format == TraceFormat.DETAILED_PC && pcExecutionPattern != null) {
                // 如果遇到新的時間戳或空行，則停止收集
                if (timePattern.matcher(line).find() || line.trim().isEmpty()) {
                    collectingPcExecution = false;

                    // 將PeopleCode執行信息添加到最近的函數條目
                    if (!activeEntries.isEmpty() && functionCounter > 0) {
                        String identifier = "FUNC: " + activeEntries.keySet().stream()
                                .filter(k -> k.startsWith("FUNC:"))
                                .findFirst().orElse("");
                        if (!identifier.isEmpty() && activeEntries.containsKey(identifier)) {
                            TraceEntry entry = activeEntries.get(identifier);
                            entry.metadata.put("pcExecution", currentPcExecution.toString());
                        }
                    }
                }
                // 否則繼續收集PeopleCode執行信息
                else {
                    currentPcExecution.append("\n").append(line.trim());
                }
            }

            // 檢查PeopleCode變量（如果是詳細PeopleCode格式）
            if (format == TraceFormat.DETAILED_PC && pcVariablePattern != null) {
                Matcher varMatcher = pcVariablePattern.matcher(line);
                if (varMatcher.find()) {
                    String varName = varMatcher.group(1);
                    String varValue = varMatcher.group(2);

                    // 將變量添加到最近的函數或方法條目
                    if (!activeEntries.isEmpty()) {
                        String identifier = activeEntries.keySet().stream()
                                .filter(k -> k.startsWith("FUNC:") || k.startsWith("METH:"))
                                .findFirst().orElse("");
                        if (!identifier.isEmpty() && activeEntries.containsKey(identifier)) {
                            TraceEntry entry = activeEntries.get(identifier);
                            Map<String, String> variables = (Map<String, String>) entry.metadata
                                    .getOrDefault("variables", new HashMap<String, String>());
                            variables.put(varName, varValue);
                            entry.metadata.put("variables", variables);
                        }
                    }
                }
            }
        }

        // 關閉所有未關閉的條目
        for (String key : new ArrayList<>(activeEntries.keySet())) {
            TraceEntry entry = activeEntries.get(key);
            if (entry.endTime == 0) {
                log.warn("Entry not properly closed: {}", entry.identifier);
                // 使用最後一行的時間作為結束時間
                if (!lines.isEmpty()) {
                    String lastLine = lines.get(lines.size() - 1);
                    Matcher lastTimeMatcher = timePattern.matcher(lastLine);
                    if (lastTimeMatcher.find()) {
                        entry.endTime = parseTimeToMillis(lastTimeMatcher.group(1));
                        entries.add(entry);
                    }
                }
            }
        }

        return entries;
    }

    /**
     * 將時間字符串解析為自午夜以來的毫秒數。
     *
     * @param timeStr 格式為HH:MM:SS.SSS的時間字符串
     * @return 自午夜以來的毫秒數
     */
    private static long parseTimeToMillis(String timeStr) {
        // 使用Java 8時間API解析時間
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
        LocalTime time = LocalTime.parse(timeStr, formatter);
        return time.toNanoOfDay() / 1_000_000; // 將納秒轉換為毫秒
    }

    /**
     * 比較兩個跟蹤文件並輸出差異。
     *
     * @param env1Entries 第一個環境的跟蹤條目
     * @param env2Entries 第二個環境的跟蹤條目
     * @param env1Name    第一個環境的名稱
     * @param env2Name    第二個環境的名稱
     * @param outputPath  輸出CSV文件的路徑
     * @throws IOException 如果輸出文件無法寫入
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

        // 創建詳細報告文件
        List<String> detailedReport = new ArrayList<>();
        detailedReport.add("# AE Trace Comparison Detailed Report");
        detailedReport.add("## Comparison between " + env1Name + " and " + env2Name);
        detailedReport.add("");

        for (TraceEntry env2Entry : env2Entries) {
            TraceEntry env1Entry = env1Map.get(env2Entry.identifier);
            if (env1Entry != null) {
                long env1Time = env1Entry.duration();
                long env2Time = env2Entry.duration();
                long diff = env2Time - env1Time;
                double diffPercent = env1Time > 0 ? (diff * 100.0 / env1Time) : 0;

                // 標記顯著差異（>20%且>100ms）
                String flag = (Math.abs(diff) > env1Time * 0.2 && Math.abs(diff) > 100) ? "ALERT" : "";

                // 檢查是否有額外的詳細信息需要比較
                StringBuilder details = new StringBuilder();

                // 比較SQL文本（如果有）
                if (env1Entry.type.equals("SQL") && env2Entry.type.equals("SQL")) {
                    String env1Content = env1Entry.content != null ? env1Entry.content : "";
                    String env2Content = env2Entry.content != null ? env2Entry.content : "";

                    if (!env1Content.isEmpty() && !env2Content.isEmpty() && !env1Content.equals(env2Content)) {
                        details.append("SQL Text differs; ");

                        // 添加到詳細報告
                        detailedReport.add("### SQL Text Difference for " + env2Entry.identifier);
                        detailedReport.add("#### " + env1Name + " SQL:");
                        detailedReport.add("```sql");
                        detailedReport.add(env1Content);
                        detailedReport.add("```");
                        detailedReport.add("#### " + env2Name + " SQL:");
                        detailedReport.add("```sql");
                        detailedReport.add(env2Content);
                        detailedReport.add("```");
                        detailedReport.add("");
                    }

                    // 比較綁定變量（如果有）
                    String env1BindVars = (String) env1Entry.metadata.getOrDefault("bindVariables", "");
                    String env2BindVars = (String) env2Entry.metadata.getOrDefault("bindVariables", "");

                    if (!env1BindVars.isEmpty() && !env2BindVars.isEmpty() && !env1BindVars.equals(env2BindVars)) {
                        details.append("Bind Variables differ; ");

                        // 添加到詳細報告
                        detailedReport.add("### Bind Variables Difference for " + env2Entry.identifier);
                        detailedReport.add("#### " + env1Name + " Bind Variables:");
                        detailedReport.add("```");
                        detailedReport.add(env1BindVars);
                        detailedReport.add("```");
                        detailedReport.add("#### " + env2Name + " Bind Variables:");
                        detailedReport.add("```");
                        detailedReport.add(env2BindVars);
                        detailedReport.add("```");
                        detailedReport.add("");
                    }
                }

                // 比較PeopleCode執行信息（如果有）
                if ((env1Entry.type.equals("FUNCTION") || env1Entry.type.equals("METHOD")) &&
                    (env2Entry.type.equals("FUNCTION") || env2Entry.type.equals("METHOD"))) {

                    String env1PcExec = (String) env1Entry.metadata.getOrDefault("pcExecution", "");
                    String env2PcExec = (String) env2Entry.metadata.getOrDefault("pcExecution", "");

                    if (!env1PcExec.isEmpty() && !env2PcExec.isEmpty() && !env1PcExec.equals(env2PcExec)) {
                        details.append("PeopleCode Execution differs; ");

                        // 添加到詳細報告
                        detailedReport.add("### PeopleCode Execution Difference for " + env2Entry.identifier);
                        detailedReport.add("#### " + env1Name + " PeopleCode Execution:");
                        detailedReport.add("```");
                        detailedReport.add(env1PcExec);
                        detailedReport.add("```");
                        detailedReport.add("#### " + env2Name + " PeopleCode Execution:");
                        detailedReport.add("```");
                        detailedReport.add(env2PcExec);
                        detailedReport.add("```");
                        detailedReport.add("");
                    }

                    // 比較變量（如果有）
                    Map<String, String> env1Vars = (Map<String, String>) env1Entry.metadata.getOrDefault("variables", new HashMap<>());
                    Map<String, String> env2Vars = (Map<String, String>) env2Entry.metadata.getOrDefault("variables", new HashMap<>());

                    if (!env1Vars.isEmpty() && !env2Vars.isEmpty() && !env1Vars.equals(env2Vars)) {
                        details.append("Variables differ; ");

                        // 添加到詳細報告
                        detailedReport.add("### Variables Difference for " + env2Entry.identifier);
                        detailedReport.add("#### " + env1Name + " Variables:");
                        detailedReport.add("```");
                        for (Map.Entry<String, String> var : env1Vars.entrySet()) {
                            detailedReport.add(var.getKey() + " = " + var.getValue());
                        }
                        detailedReport.add("```");
                        detailedReport.add("#### " + env2Name + " Variables:");
                        detailedReport.add("```");
                        for (Map.Entry<String, String> var : env2Vars.entrySet()) {
                            detailedReport.add(var.getKey() + " = " + var.getValue());
                        }
                        detailedReport.add("```");
                        detailedReport.add("");
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

                // 如果有ALERT標記，添加到詳細報告
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
                // 條目只存在於env2
                log.info("{} {} {} {} {} {} {} {}",
                        env2Entry.type, env2Entry.identifier, "N/A", env2Entry.duration(), "N/A", "N/A", "UNIQUE", "");

                outputLines.add(String.format("%s,%s,%s,%d,%s,%s,%s,%s",
                        env2Entry.type, env2Entry.identifier, "N/A", env2Entry.duration(), "N/A", "N/A", "UNIQUE", ""));

                // 添加唯一條目到詳細報告
                detailedReport.add("### UNIQUE: " + env2Entry.type + " - " + env2Entry.identifier);
                detailedReport.add("* Only exists in " + env2Name);
                detailedReport.add("* Duration: " + env2Entry.duration() + " ms");
                detailedReport.add("");
            }
        }

        // 檢查只存在於env1的條目
        for (TraceEntry env1Entry : env1Entries) {
            boolean foundInEnv2 = env2Entries.stream()
                    .anyMatch(e -> e.identifier.equals(env1Entry.identifier));

            if (!foundInEnv2) {
                log.info("{} {} {} {} {} {} {} {}",
                        env1Entry.type, env1Entry.identifier, env1Entry.duration(), "N/A", "N/A", "N/A", "MISSING", "");

                outputLines.add(String.format("%s,%s,%d,%s,%s,%s,%s,%s",
                        env1Entry.type, env1Entry.identifier, env1Entry.duration(), "N/A", "N/A", "N/A", "MISSING", ""));

                // 添加缺失條目到詳細報告
                detailedReport.add("### MISSING: " + env1Entry.type + " - " + env1Entry.identifier);
                detailedReport.add("* Only exists in " + env1Name);
                detailedReport.add("* Duration: " + env1Entry.duration() + " ms");
                detailedReport.add("");
            }
        }

        // 寫入CSV輸出
        Files.write(Paths.get(outputPath), outputLines);
        log.info("\n比對結果已輸出到 {}", outputPath);

        // 寫入詳細報告
        String detailedReportPath = outputPath.replace(".csv", "_detailed.md");
        Files.write(Paths.get(detailedReportPath), detailedReport);
        log.info("詳細報告已輸出到 {}", detailedReportPath);
    }

    /**
     * 比較兩個跟蹤文件並輸出差異（使用默認參數）。
     *
     * @param env1Entries 第一個環境的跟蹤條目
     * @param env2Entries 第二個環境的跟蹤條目
     * @throws IOException 如果輸出文件無法寫入
     */
    public static void compareTraces(List<TraceEntry> env1Entries, List<TraceEntry> env2Entries) throws IOException {
        compareTraces(env1Entries, env2Entries, "ENV1", "ENV2", "ae_trace_comparison_result.csv");
    }

    /**
     * 主方法，用於運行比較器。
     *
     * @param args 命令行參數：env1TraceFile env2TraceFile [env1Name] [env2Name] [outputPath]
     * @throws IOException 如果文件無法讀取或寫入
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            log.info("Usage: java AETraceComparator <env1_trace_file> <env2_trace_file> [env1_name] [env2_name] [output_path]");
            log.info("Example: java AETraceComparator sit_trace.log uat_trace.log SIT UAT ae_comparison_result.csv");
            return;
        }

        String env1Name = args.length > 2 ? args[2] : "ENV1";
        String env2Name = args.length > 3 ? args[3] : "ENV2";
        String outputPath = args.length > 4 ? args[4] : "ae_trace_comparison_result.csv";

        List<TraceEntry> env1Entries = parseTrace(args[0]);
        List<TraceEntry> env2Entries = parseTrace(args[1]);

        compareTraces(env1Entries, env2Entries, env1Name, env2Name, outputPath);
    }

    /**
     * 表示不同AE跟蹤參數格式的枚舉
     */
    public enum TraceFormat {
        STANDARD,       // 包含基本信息的標準格式 (-TRACE 1)
        DETAILED_SQL,   // 包含詳細SQL信息 (-DBFLAGS 或 -TOOLSTRACESQL)
        DETAILED_PC     // 包含詳細PeopleCode信息 (-TOOLSTRACEPC)
    }

    static class TraceEntry {
        String identifier; // 可以是Step名稱、SQL摘要或Function名稱
        String type;       // STEP, SQL, FUNCTION, METHOD
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

}
