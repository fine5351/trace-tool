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
 * 一個新的用於比較不同環境中Application Engine跟蹤文件的工具。
 * 此工具分析AE跟蹤文件中的執行時間、步驟、SQL調用、函數調用和方法調用。
 *
 * <p>NewAETraceComparator可以處理不同的跟蹤參數格式：</p>
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
 *   <li>包含差異摘要的輸出文件</li>
 *   <li>包含最重要差異的控制台輸出</li>
 * </ul>
 */
@Slf4j
public class NewAETraceComparator {

    /**
     * 比較兩個跟蹤文件並輸出差異。
     *
     * @param env1Entries         第一個環境的跟蹤條目
     * @param env2Entries         第二個環境的跟蹤條目
     * @param env1Name            第一個環境的名稱
     * @param env2Name            第二個環境的名稱
     * @param outputPath          輸出文件的路徑
     * @param thresholdMultiplier 執行時間差異倍數閾值
     * @throws IOException 如果輸出文件無法寫入
     */
    public static void compareTraces(
            List<TraceEntry> env1Entries,
            List<TraceEntry> env2Entries,
            String env1Name,
            String env2Name,
            String outputPath,
            double thresholdMultiplier) throws IOException {

        List<String> outputLines = new ArrayList<>();
        outputLines.add("Type,Identifier," + env1Name + "(ms)," + env2Name + "(ms),Diff(ms),Diff(%),Flag,Details");

        log.info("Type        Identifier                                        {} (ms)       {} (ms)       Diff      Diff(%)    Flag      Details", env1Name, env2Name);
        log.info("{}", "=".repeat(140));

        // 創建索引以便快速查找
        Map<String, TraceEntry> env1Map = new HashMap<>();
        for (TraceEntry entry : env1Entries) {
            env1Map.put(entry.identifier, entry);
        }

        Map<String, TraceEntry> env2Map = new HashMap<>();
        for (TraceEntry entry : env2Entries) {
            env2Map.put(entry.identifier, entry);
        }

        // 記錄多執行的代碼
        List<String> extraCodeInEnv1 = new ArrayList<>();
        List<String> extraCodeInEnv2 = new ArrayList<>();

        // 比較兩個環境的條目
        int env1Index = 0;
        int env2Index = 0;

        while (env1Index < env1Entries.size() && env2Index < env2Entries.size()) {
            TraceEntry env1Entry = env1Entries.get(env1Index);
            TraceEntry env2Entry = env2Entries.get(env2Index);

            // 如果兩個條目相同
            if (env1Entry.identifier.equals(env2Entry.identifier)) {
                compareAndRecordEntry(env1Entry, env2Entry, env1Name, env2Name, outputLines, thresholdMultiplier);
                env1Index++;
                env2Index++;
            }
            // 如果env1有而env2沒有的條目
            else if (!env2Map.containsKey(env1Entry.identifier)) {
                recordExtraCode(env1Entry, env1Name, extraCodeInEnv1);
                env1Index++;
            }
            // 如果env2有而env1沒有的條目
            else if (!env1Map.containsKey(env2Entry.identifier)) {
                recordExtraCode(env2Entry, env2Name, extraCodeInEnv2);
                env2Index++;
            }
            // 如果兩個條目都存在但順序不同，優先處理當前索引較小的環境
            else {
                // 查找env2中對應的env1當前條目
                int env2MatchIndex = findEntryIndex(env2Entries, env1Entry.identifier, env2Index);
                // 查找env1中對應的env2當前條目
                int env1MatchIndex = findEntryIndex(env1Entries, env2Entry.identifier, env1Index);

                // 選擇跳過較少條目的路徑
                if (env2MatchIndex - env2Index <= env1MatchIndex - env1Index) {
                    // 跳過env2中的條目直到找到匹配
                    while (env2Index < env2MatchIndex) {
                        recordExtraCode(env2Entries.get(env2Index), env2Name, extraCodeInEnv2);
                        env2Index++;
                    }
                } else {
                    // 跳過env1中的條目直到找到匹配
                    while (env1Index < env1MatchIndex) {
                        recordExtraCode(env1Entries.get(env1Index), env1Name, extraCodeInEnv1);
                        env1Index++;
                    }
                }
            }
        }

        // 處理剩餘的條目
        while (env1Index < env1Entries.size()) {
            recordExtraCode(env1Entries.get(env1Index), env1Name, extraCodeInEnv1);
            env1Index++;
        }

        while (env2Index < env2Entries.size()) {
            recordExtraCode(env2Entries.get(env2Index), env2Name, extraCodeInEnv2);
            env2Index++;
        }

        // 將多執行的代碼添加到輸出
        if (!extraCodeInEnv1.isEmpty()) {
            outputLines.add("\nExtra code in " + env1Name + ":");
            outputLines.addAll(extraCodeInEnv1);
        }

        if (!extraCodeInEnv2.isEmpty()) {
            outputLines.add("\nExtra code in " + env2Name + ":");
            outputLines.addAll(extraCodeInEnv2);
        }

        // 寫入輸出文件
        Files.write(Paths.get(outputPath), outputLines);
        log.info("\n比對結果已輸出到 {}", outputPath);
    }

    /**
     * 在列表中查找指定標識符的條目索引
     */
    private static int findEntryIndex(List<TraceEntry> entries, String identifier, int startIndex) {
        for (int i = startIndex; i < entries.size(); i++) {
            if (entries.get(i).identifier.equals(identifier)) {
                return i;
            }
        }
        return entries.size(); // 如果找不到，返回列表大小
    }

    /**
     * 比較並記錄兩個條目之間的差異
     */
    private static void compareAndRecordEntry(
            TraceEntry env1Entry,
            TraceEntry env2Entry,
            String env1Name,
            String env2Name,
            List<String> outputLines,
            double thresholdMultiplier) {

        long env1Time = env1Entry.duration();
        long env2Time = env2Entry.duration();
        long diff = env2Time - env1Time;
        double diffPercent = env1Time > 0 ? (diff * 100.0 / env1Time) : 0;

        // 檢查執行時間差異是否超過閾值
        boolean exceedsThreshold = false;
        if (env1Time > 0 && env2Time > 0) {
            double ratio = (double) env2Time / env1Time;
            exceedsThreshold = ratio > thresholdMultiplier || (1.0 / ratio) > thresholdMultiplier;
        }

        String flag = exceedsThreshold ? "THRESHOLD_EXCEEDED" : "";

        // 構建詳細信息
        StringBuilder details = new StringBuilder();

        // 比較SQL文本（如果有）
        if (env1Entry.type.equals("SQL") && env2Entry.type.equals("SQL")) {
            String env1Content = env1Entry.content != null ? env1Entry.content : "";
            String env2Content = env2Entry.content != null ? env2Entry.content : "";

            if (!env1Content.isEmpty() && !env2Content.isEmpty() && !env1Content.equals(env2Content)) {
                details.append("SQL文本不同; ");
            }

            // 比較綁定變量（如果有）
            String env1BindVars = (String) env1Entry.metadata.getOrDefault("bindVariables", "");
            String env2BindVars = (String) env2Entry.metadata.getOrDefault("bindVariables", "");

            if (!env1BindVars.isEmpty() && !env2BindVars.isEmpty() && !env1BindVars.equals(env2BindVars)) {
                details.append("綁定變量不同; ");
            }
        }

        // 比較PeopleCode執行信息（如果有）
        if ((env1Entry.type.equals("FUNCTION") || env1Entry.type.equals("METHOD")) &&
            (env2Entry.type.equals("FUNCTION") || env2Entry.type.equals("METHOD"))) {

            String env1PcExec = (String) env1Entry.metadata.getOrDefault("pcExecution", "");
            String env2PcExec = (String) env2Entry.metadata.getOrDefault("pcExecution", "");

            if (!env1PcExec.isEmpty() && !env2PcExec.isEmpty() && !env1PcExec.equals(env2PcExec)) {
                details.append("PeopleCode執行不同; ");
            }

            // 比較變量（如果有）
            Map<String, String> env1Vars = (Map<String, String>) env1Entry.metadata.getOrDefault("variables", new HashMap<>());
            Map<String, String> env2Vars = (Map<String, String>) env2Entry.metadata.getOrDefault("variables", new HashMap<>());

            if (!env1Vars.isEmpty() && !env2Vars.isEmpty() && !env1Vars.equals(env2Vars)) {
                details.append("變量不同; ");
            }
        }

        String detailsStr = details.toString().trim();

        // 只記錄超過閾值的條目
        if (exceedsThreshold) {
            log.info("{} {} {} {} {} {} {} {}",
                    env2Entry.type, env2Entry.identifier, env1Time, env2Time, diff,
                    String.format("%.2f", diffPercent), flag, detailsStr);

            outputLines.add(String.format("%s,%s,%d,%d,%d,%.2f,%s,%s",
                    env2Entry.type, env2Entry.identifier, env1Time, env2Time, diff, diffPercent, flag, detailsStr));
        }
    }

    /**
     * 記錄多執行的代碼
     */
    private static void recordExtraCode(TraceEntry entry, String envName, List<String> extraCodeList) {
        String extraInfo = String.format("%s: %s (%s) - 持續時間: %d 毫秒",
                envName, entry.identifier, entry.type, entry.duration());

        if (entry.content != null && !entry.content.isEmpty()) {
            extraInfo += "\n內容: " + entry.content;
        }

        extraCodeList.add(extraInfo);
        extraCodeList.add("---");

        log.info("{}中的額外代碼: {} ({}) - 持續時間: {} 毫秒",
                envName, entry.identifier, entry.type, entry.duration());
    }

    /**
     * 使用多個分析器解析跟蹤文件，並將結果合併。
     * 此方法允許同時使用多種不同的分析器（例如標準分析器、SQL分析器和PeopleCode分析器）
     * 來處理同一個跟蹤文件，從而獲取更全面的分析結果。
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

        // 按開始時間排序
        allEntries.sort(Comparator.comparingLong(e -> e.startTime));

        return allEntries;
    }

    /**
     * 主方法，用於運行比較器。
     * 在方法中直接設定文件路徑和其他參數。
     *
     * @throws IOException 如果文件無法讀取或寫入
     */
    public static void main(String[] args) throws IOException {
        // 在此處直接設定參數，而不是從命令行獲取
        String env1TraceFile = "D:\\traces\\env1_trace.log";
        String env2TraceFile = "D:\\traces\\env2_trace.log";
        double thresholdMultiplier = 2.0;
        String outputPath = "ae_trace_comparison_result.txt";
        String traceParams = "-TRACE 3 -TOOLSTRACEPC 4044 -TOOLSTRACESQL 31";

        // 環境名稱從文件名中提取
        String env1Name = Paths.get(env1TraceFile).getFileName().toString().replaceAll("\\.[^.]+$", "");
        String env2Name = Paths.get(env2TraceFile).getFileName().toString().replaceAll("\\.[^.]+$", "");

        // 根據trace參數創建多個分析器
        List<TraceAnalyzer> analyzers = TraceAnalyzerFactory.createAnalyzersForParams(traceParams);

        log.info("使用參數 {} 分析 {} 和 {} 檔案", traceParams, env1TraceFile, env2TraceFile);
        log.info("創建了 {} 個分析器", analyzers.size());
        log.info("時間差異閾值倍數: {}", thresholdMultiplier);

        // 使用多個分析器解析trace檔案
        List<TraceEntry> env1Entries = parseTraceWithMultipleAnalyzers(env1TraceFile, analyzers);
        List<TraceEntry> env2Entries = parseTraceWithMultipleAnalyzers(env2TraceFile, analyzers);

        log.info("從 {} 解析出 {} 個條目", env1TraceFile, env1Entries.size());
        log.info("從 {} 解析出 {} 個條目", env2TraceFile, env2Entries.size());

        // 比較兩個環境的trace結果
        compareTraces(env1Entries, env2Entries, env1Name, env2Name, outputPath, thresholdMultiplier);
    }
}
