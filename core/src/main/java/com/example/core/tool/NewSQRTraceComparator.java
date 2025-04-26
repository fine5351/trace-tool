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

        // Match pattern for execution time in trace file
        // This pattern may need to be adjusted based on the actual format of the trace file
        Pattern timePattern = Pattern.compile("\\((\\d{2}:\\d{2}:\\d{2})\\)");

        // Pattern to match execution time lines like "执行时间: 1.0秒"
        Pattern executionTimePattern = Pattern.compile("执行时间: (\\d+\\.\\d+)秒");

        // Skip the "SQR结束执行" line which also contains a timestamp but should not be counted
        Pattern endPattern = Pattern.compile("SQR结束执行");

        TraceEntry lastEntry = null;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            // Skip end execution line
            if (endPattern.matcher(line).find()) {
                continue;
            }

            // Check if this is an execution time line
            Matcher executionTimeMatcher = executionTimePattern.matcher(line);
            if (executionTimeMatcher.find() && lastEntry != null) {
                // Extract the execution time in seconds
                double executionTimeSeconds = Double.parseDouble(executionTimeMatcher.group(1));
                // Convert to milliseconds and store in the last entry
                lastEntry.actualExecutionTime = (long) (executionTimeSeconds * 1000);
                continue;
            }

            // Check if this is a line with a timestamp
            Matcher timeMatcher = timePattern.matcher(line);
            if (timeMatcher.find()) {
                String timeStr = timeMatcher.group(1);
                long timeMillis = parseTimeToMillis(timeStr);

                // Create a trace entry for this line
                TraceEntry entry = new TraceEntry(line, timeMillis, i + 1);
                entries.add(entry);
                lastEntry = entry;
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
                if (entry1.actualExecutionTime > 0 && entry2.actualExecutionTime > 0) {
                    double ratio = (double) entry2.actualExecutionTime / entry1.actualExecutionTime;

                    if (ratio > threshold || ratio < 1.0 / threshold) {
                        outputLines.add("## Time Difference Detected");
                        outputLines.add("Line content: " + entry1.lineContent);
                        outputLines.add("File 1 line: " + entry1.lineNumber + ", execution time: " + entry1.actualExecutionTime + " ms");
                        outputLines.add("File 2 line: " + entry2.lineNumber + ", execution time: " + entry2.actualExecutionTime + " ms");
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
                    // Extra code in file2, record and skip
                    outputLines.add("## Extra Code in File 2");
                    outputLines.add("Starting at line: " + file2Entries.get(j).lineNumber);
                    for (int k = 0; k < lookAhead; k++) {
                        outputLines.add("Line " + file2Entries.get(j + k).lineNumber + ": " + file2Entries.get(j + k).lineContent);
                    }
                    outputLines.add("");

                    j += lookAhead; // Skip ahead in file2
                } else {
                    // Try to find matching line in file1
                    found = false;
                    lookAhead = 1;
                    while (i + lookAhead < file1Entries.size() && lookAhead <= 10) { // Look ahead up to 10 lines
                        if (entry2.lineContent.equals(file1Entries.get(i + lookAhead).lineContent)) {
                            found = true;
                            break;
                        }
                        lookAhead++;
                    }

                    if (found) {
                        // Extra code in file1, record and skip
                        outputLines.add("## Extra Code in File 1");
                        outputLines.add("Starting at line: " + file1Entries.get(i).lineNumber);
                        for (int k = 0; k < lookAhead; k++) {
                            outputLines.add("Line " + file1Entries.get(i + k).lineNumber + ": " + file1Entries.get(i + k).lineContent);
                        }
                        outputLines.add("");

                        i += lookAhead; // Skip ahead in file1
                    } else {
                        // Lines don't match and no match found ahead, just move forward
                        outputLines.add("## Mismatched Lines");
                        outputLines.add("File 1 line " + entry1.lineNumber + ": " + entry1.lineContent);
                        outputLines.add("File 2 line " + entry2.lineNumber + ": " + entry2.lineContent);
                        outputLines.add("");

                        i++;
                        j++;
                    }
                }
            }
        }

        // Handle remaining entries in file1
        while (i < file1Entries.size()) {
            TraceEntry entry = file1Entries.get(i);
            outputLines.add("## Extra Code in File 1 (End of File)");
            outputLines.add("Line " + entry.lineNumber + ": " + entry.lineContent);
            i++;
        }

        // Handle remaining entries in file2
        while (j < file2Entries.size()) {
            TraceEntry entry = file2Entries.get(j);
            outputLines.add("## Extra Code in File 2 (End of File)");
            outputLines.add("Line " + entry.lineNumber + ": " + entry.lineContent);
            j++;
        }

        // Write output to file
        Files.write(Paths.get(outputPath), outputLines);
        log.info("Comparison results written to: {}", outputPath);
    }

    /**
     * Parse a time string into milliseconds since midnight.
     *
     * @param timeStr Time string in HH:MM:SS format
     * @return Milliseconds since midnight
     */
    private static long parseTimeToMillis(String timeStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalTime time = LocalTime.parse(timeStr, formatter);
        return time.toNanoOfDay() / 1_000_000; // Convert nanoseconds to milliseconds
    }

    /**
     * Main method to run the comparator.
     * File paths and other parameters are set directly in the method.
     *
     * @throws IOException If files cannot be read or written
     */
    public static void main(String[] args) throws IOException {
        // Parse command line arguments
        String file1Path = args[0];
        String file2Path = args[1];
        double threshold = args.length > 2 ? Double.parseDouble(args[2]) : 2.0;
        String outputPath = args.length > 3 ? args[3] : "sqr_trace_differences.txt";

        log.info("Comparing SQR trace files:");
        log.info("File 1: {}", file1Path);
        log.info("File 2: {}", file2Path);
        log.info("Threshold: {} times", threshold);
        log.info("Output: {}", outputPath);

        // Parse trace files
        List<TraceEntry> file1Entries = parseTrace(file1Path);
        List<TraceEntry> file2Entries = parseTrace(file2Path);

        log.info("Parsed {} entries from file 1", file1Entries.size());
        log.info("Parsed {} entries from file 2", file2Entries.size());

        // Compare traces
        compareTraces(file1Entries, file2Entries, threshold, outputPath);
    }

    /**
     * Represents a trace entry with time and content information.
     */
    static class TraceEntry {
        String lineContent;  // Content of the line
        long executionTime;  // Timestamp (milliseconds since midnight)
        long actualExecutionTime;  // Actual execution time (milliseconds)
        int lineNumber;      // Line number in the file

        TraceEntry(String lineContent, long executionTime, int lineNumber) {
            this.lineContent = lineContent;
            this.executionTime = executionTime;
            this.actualExecutionTime = 0; // Default actual execution time
            this.lineNumber = lineNumber;
        }
    }
}
