package com.example.core.tool;

import com.example.core.tool.analyzer.TraceAnalyzer;
import com.example.core.tool.analyzer.TraceAnalyzerFactory;
import com.example.core.tool.analyzer.TraceEntry;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

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
 * <p>該工具支持同時處理多個跟蹤參數，例如 "-TRACE 3 -TOOLSTRACEPC 4044 -TOOLSTRACESQL 31"。
 * 它會創建多個分析器來處理不同的跟蹤參數格式，並將結果合併以進行全面分析。</p>
 *
 * <p>該工具使用不同的分析器來處理不同的跟蹤參數格式，並相應地解析文件。
 * 然後比較來自兩個不同環境的跟蹤條目並生成：</p>
 * <ul>
 *   <li>包含差異摘要的CSV文件</li>
 *   <li>包含深入分析差異的詳細Markdown報告</li>
 *   <li>包含最重要差異的控制台輸出</li>
 * </ul>
 */
@Slf4j
public class AETraceComparator {

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
                        detailedReport.add("| Variable | " + env1Name + " | " + env2Name + " |");
                        detailedReport.add("|----------|------------|------------|");

                        // 合併兩個環境的所有變量名
                        Set<String> allVars = new HashSet<>(env1Vars.keySet());
                        allVars.addAll(env2Vars.keySet());

                        for (String varName : allVars) {
                            String env1Value = env1Vars.getOrDefault(varName, "N/A");
                            String env2Value = env2Vars.getOrDefault(varName, "N/A");
                            if (!env1Value.equals(env2Value)) {
                                detailedReport.add("| " + varName + " | " + env1Value + " | " + env2Value + " |");
                            }
                        }
                        detailedReport.add("");
                    }
                }

                String detailsStr = details.toString().trim();
                log.info("{} {} {} {} {} {} {} {}",
                        env2Entry.type, env2Entry.identifier, env1Time, env2Time, diff, String.format("%.2f", diffPercent), flag, detailsStr);

                outputLines.add(String.format("%s,%s,%d,%d,%d,%.2f,%s,%s",
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
     * 使用多個分析器解析跟蹤文件，並將結果合併。
     * 此方法允許同時使用多種不同的分析器（例如標準分析器、SQL分析器和PeopleCode分析器）
     * 來處理同一個跟蹤文件，從而獲取更全面的分析結果。
     * <p>
     * 合併邏輯：
     * - 如果多個分析器發現相同的條目（相同的類型和標識符），則合併這些條目
     * - 保留最早的開始時間和最晚的結束時間
     * - 如果一個分析器提供了內容而另一個沒有，則使用有內容的那個
     * - 合併所有元數據，保留所有唯一的鍵值對
     *
     * @param filePath  跟蹤文件路徑
     * @param analyzers 分析器列表
     * @return 合併後的跟蹤條目列表
     * @throws IOException 如果文件無法讀取
     */
    public static List<TraceEntry> parseTraceWithMultipleAnalyzers(String filePath, List<TraceAnalyzer> analyzers) throws IOException {
        List<TraceEntry> allEntries = new ArrayList<>();
        Map<String, TraceEntry> entryMap = new HashMap<>();

        // 使用每個分析器解析文件
        for (TraceAnalyzer analyzer : analyzers) {
            List<TraceEntry> entries = analyzer.parseTrace(filePath);

            // 合併結果
            for (TraceEntry entry : entries) {
                String key = entry.type + ":" + entry.identifier;

                if (entryMap.containsKey(key)) {
                    // 已存在此條目，合併元數據
                    TraceEntry existingEntry = entryMap.get(key);

                    // 保留最早的開始時間和最晚的結束時間
                    existingEntry.startTime = Math.min(existingEntry.startTime, entry.startTime);
                    existingEntry.endTime = Math.max(existingEntry.endTime, entry.endTime);

                    // 如果新條目有內容而現有條目沒有，則使用新條目的內容
                    if (existingEntry.content == null && entry.content != null) {
                        existingEntry.content = entry.content;
                    }

                    // 合併元數據
                    for (Map.Entry<String, Object> metadataEntry : entry.metadata.entrySet()) {
                        if (!existingEntry.metadata.containsKey(metadataEntry.getKey())) {
                            existingEntry.metadata.put(metadataEntry.getKey(), metadataEntry.getValue());
                        }
                    }
                } else {
                    // 新條目，添加到映射
                    entryMap.put(key, entry);
                }
            }
        }

        // 將映射轉換為列表
        allEntries.addAll(entryMap.values());
        return allEntries;
    }

    /**
     * 主方法，用於運行比較器。
     * 直接在代碼中指定兩個trace檔案路徑與使用的trace參數。
     *
     * @throws IOException 如果文件無法讀取或寫入
     */
    public static void main(String[] args) throws IOException {
        // 直接在代碼中指定檔案路徑和參數，而不是從命令行獲取
        String env1TraceFile = "D:\\traces\\env1_trace.log";
        String env2TraceFile = "D:\\traces\\env2_trace.log";
        String env1Name = "DEV";
        String env2Name = "TEST";
        String outputPath = "ae_trace_comparison_result.csv";

        // 指定使用的trace參數（支持多個參數）
        String traceParams = "-TRACE 3 -TOOLSTRACEPC 4044 -TOOLSTRACESQL 31"; // 多個trace參數

        // 根據trace參數創建多個分析器
        List<TraceAnalyzer> analyzers = TraceAnalyzerFactory.createAnalyzersForParams(traceParams);

        log.info("使用參數 {} 分析 {} 和 {} 檔案", traceParams, env1TraceFile, env2TraceFile);
        log.info("創建了 {} 個分析器", analyzers.size());

        // 使用多個分析器解析trace檔案
        List<TraceEntry> env1Entries = parseTraceWithMultipleAnalyzers(env1TraceFile, analyzers);
        List<TraceEntry> env2Entries = parseTraceWithMultipleAnalyzers(env2TraceFile, analyzers);

        log.info("從 {} 解析出 {} 個條目", env1TraceFile, env1Entries.size());
        log.info("從 {} 解析出 {} 個條目", env2TraceFile, env2Entries.size());

        // 比較兩個環境的trace結果
        compareTraces(env1Entries, env2Entries, env1Name, env2Name, outputPath);
    }
}
