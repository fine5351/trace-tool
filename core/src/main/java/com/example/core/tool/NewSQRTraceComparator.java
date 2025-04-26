package com.example.core.tool;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 一個新的用於Oracle PeopleSoft SQR跟蹤文件的比較器。
 * 此工具分析SQR跟蹤文件中的執行時間並識別差異。
 *
 * <p>功能：</p>
 * <ul>
 *   <li>在main方法中設定兩個文件路徑</li>
 *   <li>處理具有相同跟蹤參數的跟蹤文件</li>
 *   <li>分析每行數據並封裝在DTO中</li>
 *   <li>查找執行時間差異超過n倍的行（n是可變的）</li>
 *   <li>處理一個文件有而另一個文件沒有的代碼的情況</li>
 *   <li>將差異輸出到文件</li>
 * </ul>
 */
@Slf4j
public class NewSQRTraceComparator {

    /**
     * 解析SQR跟蹤文件並提取跟蹤條目。
     *
     * @param filePath SQR跟蹤文件的路徑
     * @return 跟蹤條目列表
     * @throws IOException 如果文件無法讀取
     */
    public static List<TraceEntry> parseTrace(String filePath) throws IOException {
        List<TraceEntry> entries = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(filePath));

        // 匹配跟蹤文件中執行時間的模式
        // 根據跟蹤文件的實際格式，此模式可能需要調整
        Pattern timePattern = Pattern.compile("\\((\\d{2}:\\d{2}:\\d{2})\\)");

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            Matcher timeMatcher = timePattern.matcher(line);

            if (timeMatcher.find()) {
                String timeStr = timeMatcher.group(1);
                long timeMillis = parseTimeToMillis(timeStr);

                // Create a trace entry for this line
                TraceEntry entry = new TraceEntry(line, timeMillis, i + 1);
                entries.add(entry);
            }
        }

        return entries;
    }

    /**
     * 比較兩個跟蹤文件並輸出差異。
     *
     * @param file1Entries 第一個文件的跟蹤條目
     * @param file2Entries 第二個文件的跟蹤條目
     * @param threshold    時間差異的閾值（例如，2.0表示慢2倍）
     * @param outputPath   輸出文件的路徑
     * @throws IOException 如果輸出文件無法寫入
     */
    public static void compareTraces(
            List<TraceEntry> file1Entries,
            List<TraceEntry> file2Entries,
            double threshold,
            String outputPath) throws IOException {

        List<String> outputLines = new ArrayList<>();
        outputLines.add("# SQR Trace Comparison Results");
        outputLines.add("Threshold: " + threshold + " times");
        outputLines.add("");

        int i = 0, j = 0;

        while (i < file1Entries.size() && j < file2Entries.size()) {
            TraceEntry entry1 = file1Entries.get(i);
            TraceEntry entry2 = file2Entries.get(j);

            // If the line content matches, compare execution times
            if (entry1.lineContent.equals(entry2.lineContent)) {
                // Check if execution time difference exceeds threshold
                if (entry1.executionTime > 0 && entry2.executionTime > 0) {
                    double ratio = (double) entry2.executionTime / entry1.executionTime;

                    if (ratio > threshold || ratio < 1.0 / threshold) {
                        outputLines.add("## Time Difference Detected");
                        outputLines.add("Line content: " + entry1.lineContent);
                        outputLines.add("File 1 line: " + entry1.lineNumber + ", execution time: " + entry1.executionTime + " ms");
                        outputLines.add("File 2 line: " + entry2.lineNumber + ", execution time: " + entry2.executionTime + " ms");
                        outputLines.add("Ratio: " + String.format("%.2f", ratio) + " times");
                        outputLines.add("");
                    }
                }

                i++;
                j++;
            }
            // If line content doesn't match, handle the case where one file has code that the other doesn't
            else {
                // Try to find matching line in file2
                boolean found = false;
                int lookAhead = 1;
                while (j + lookAhead < file2Entries.size() && lookAhead <= 10) { // Look ahead up to 10 lines
                    if (entry1.lineContent.equals(file2Entries.get(j + lookAhead).lineContent)) {
                        found = true;
                        break;
                    }
                    lookAhead++;
                }

                if (found) {
                    // 文件2有額外的代碼，記錄並跳過
                    outputLines.add("## 文件2中的額外代碼");
                    outputLines.add("開始於行: " + file2Entries.get(j).lineNumber);
                    for (int k = 0; k < lookAhead; k++) {
                        outputLines.add("行 " + file2Entries.get(j + k).lineNumber + ": " + file2Entries.get(j + k).lineContent);
                    }
                    outputLines.add("");

                    j += lookAhead; // 在文件2中向前跳過
                } else {
                    // 嘗試在文件1中查找匹配行
                    found = false;
                    lookAhead = 1;
                    while (i + lookAhead < file1Entries.size() && lookAhead <= 10) { // 向前查看最多10行
                        if (entry2.lineContent.equals(file1Entries.get(i + lookAhead).lineContent)) {
                            found = true;
                            break;
                        }
                        lookAhead++;
                    }

                    if (found) {
                        // 文件1有額外的代碼，記錄並跳過
                        outputLines.add("## 文件1中的額外代碼");
                        outputLines.add("開始於行: " + file1Entries.get(i).lineNumber);
                        for (int k = 0; k < lookAhead; k++) {
                            outputLines.add("行 " + file1Entries.get(i + k).lineNumber + ": " + file1Entries.get(i + k).lineContent);
                        }
                        outputLines.add("");

                        i += lookAhead; // 在文件1中向前跳過
                    } else {
                        // 行不匹配且前面沒有找到匹配，只需向前移動
                        outputLines.add("## 不匹配的行");
                        outputLines.add("文件1行 " + entry1.lineNumber + ": " + entry1.lineContent);
                        outputLines.add("文件2行 " + entry2.lineNumber + ": " + entry2.lineContent);
                        outputLines.add("");

                        i++;
                        j++;
                    }
                }
            }
        }

        // 處理文件1中剩餘的條目
        while (i < file1Entries.size()) {
            TraceEntry entry = file1Entries.get(i);
            outputLines.add("## 文件1中的額外代碼（文件末尾）");
            outputLines.add("行 " + entry.lineNumber + ": " + entry.lineContent);
            i++;
        }

        // 處理文件2中剩餘的條目
        while (j < file2Entries.size()) {
            TraceEntry entry = file2Entries.get(j);
            outputLines.add("## 文件2中的額外代碼（文件末尾）");
            outputLines.add("行 " + entry.lineNumber + ": " + entry.lineContent);
            j++;
        }

        // 將輸出寫入文件
        Files.write(Paths.get(outputPath), outputLines);
        log.info("比較結果已寫入：{}", outputPath);
    }

    /**
     * 將時間字符串解析為自午夜以來的毫秒數。
     *
     * @param timeStr 格式為HH:MM:SS的時間字符串
     * @return 自午夜以來的毫秒數
     */
    private static long parseTimeToMillis(String timeStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalTime time = LocalTime.parse(timeStr, formatter);
        return time.toNanoOfDay() / 1_000_000; // 將納秒轉換為毫秒
    }

    /**
     * 運行比較器的主方法。
     * 在方法中直接設定文件路徑和其他參數。
     *
     * @throws IOException 如果文件無法讀取或寫入
     */
    public static void main(String[] args) throws IOException {
        // 在此處直接設定參數，而不是從命令行獲取
        String file1Path = "D:\\traces\\file1.log";
        String file2Path = "D:\\traces\\file2.log";
        double threshold = 2.0;
        String outputPath = "sqr_trace_differences.txt";

        log.info("比較SQR跟蹤文件：");
        log.info("文件1：{}", file1Path);
        log.info("文件2：{}", file2Path);
        log.info("閾值：{} 倍", threshold);
        log.info("輸出：{}", outputPath);

        // 解析跟蹤文件
        List<TraceEntry> file1Entries = parseTrace(file1Path);
        List<TraceEntry> file2Entries = parseTrace(file2Path);

        log.info("從文件1解析出 {} 個條目", file1Entries.size());
        log.info("從文件2解析出 {} 個條目", file2Entries.size());

        // 比較跟蹤
        compareTraces(file1Entries, file2Entries, threshold, outputPath);
    }

    /**
     * 表示具有時間和內容信息的跟蹤條目。
     */
    static class TraceEntry {
        String lineContent;  // 行的內容
        long executionTime;  // 執行時間（毫秒）
        int lineNumber;      // 文件中的行號

        TraceEntry(String lineContent, long executionTime, int lineNumber) {
            this.lineContent = lineContent;
            this.executionTime = executionTime;
            this.lineNumber = lineNumber;
        }
    }
}
