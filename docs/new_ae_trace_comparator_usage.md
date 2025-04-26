# Oracle PeopleSoft Application Engine 跟蹤比較器

本文檔提供了新的Application Engine跟蹤比較器工具的使用說明。

## 概述

新的AE跟蹤比較器是一個用於比較來自不同環境的Oracle PeopleSoft Application Engine跟蹤文件的工具。它分析執行時間，識別顯著差異，並檢測在一個環境中執行但在另一個環境中未執行的代碼。

## 功能

- 比較兩個跟蹤文件並識別具有顯著時間差異的條目
- 檢測在一個環境中執行但在另一個環境中未執行的代碼
- 可配置的時間差異閾值（n倍）
- 支持多種跟蹤參數（-TRACE、-TOOLSTRACEPC、-TOOLSTRACESQL等）
- 將結果輸出到文件

## 使用方法

### 程式碼使用

您可以在代碼中直接使用`NewAETraceComparator`類：

```java
import com.example.core.tool.NewAETraceComparator;
import com.example.core.tool.analyzer.TraceAnalyzer;
import com.example.core.tool.analyzer.TraceAnalyzerFactory;
import com.example.core.tool.analyzer.TraceEntry;

import java.io.IOException;
import java.util.List;

public class Example {
    public static void main(String[] args) throws IOException {
        // 設定參數
        String env1TraceFile = "D:\\traces\\env1_trace.log";
        String env2TraceFile = "D:\\traces\\env2_trace.log";
        double thresholdMultiplier = 2.0;
        String outputPath = "ae_trace_comparison_result.txt";
        String traceParams = "-TRACE 3 -TOOLSTRACEPC 4044 -TOOLSTRACESQL 31";

        // 提取環境名稱
        String env1Name = "環境1";
        String env2Name = "環境2";

        // 創建分析器
        List<TraceAnalyzer> analyzers = TraceAnalyzerFactory.createAnalyzersForParams(traceParams);

        // 解析跟蹤文件
        List<TraceEntry> env1Entries = NewAETraceComparator.parseTraceWithMultipleAnalyzers(env1TraceFile, analyzers);
        List<TraceEntry> env2Entries = NewAETraceComparator.parseTraceWithMultipleAnalyzers(env2TraceFile, analyzers);

        // 比較跟蹤
        NewAETraceComparator.compareTraces(env1Entries, env2Entries, env1Name, env2Name, outputPath, thresholdMultiplier);
    }
}
```

## 輸出格式

輸出文件包含：

1. 一個CSV格式的部分，包含以下列：
    - 類型：跟蹤條目的類型（STEP、SQL、FUNCTION、METHOD）
    - 標識符：跟蹤條目的標識符
    - ENV1(ms)：第一個環境中的持續時間（毫秒）
    - ENV2(ms)：第二個環境中的持續時間（毫秒）
    - Diff(ms)：絕對差異（毫秒）
    - Diff(%)：百分比差異
    - 標誌：如果時間差異超過閾值，則為THRESHOLD_EXCEEDED
    - 詳細信息：關於內容差異的附加詳細信息

2. 列出在一個環境中執行但在另一個環境中未執行的代碼的部分：
    - ENV1中的額外代碼：僅出現在第一個環境中的條目列表
    - ENV2中的額外代碼：僅出現在第二個環境中的條目列表

## 跟蹤參數

該工具支持Oracle PeopleSoft中使用的各種跟蹤參數：

- `-TRACE n`：標準跟蹤級別（1-3）
- `-TOOLSTRACEPC n`：PeopleCode跟蹤級別
- `-TOOLSTRACESQL n`：SQL跟蹤級別
- `-DBFLAGS n`：數據庫標誌

可以組合多個參數，例如`-TRACE 3 -TOOLSTRACEPC 4044 -TOOLSTRACESQL 31`。

## 工作原理

1. 該工具使用基於跟蹤參數的適當分析器解析跟蹤文件。
2. 它比較兩個環境之間具有相同標識符的條目。
3. 它標記時間差異超過指定閾值的條目。
4. 它識別並記錄在一個環境中執行但在另一個環境中未執行的代碼。
5. 它將結果輸出到文件。

## 故障排除

如果遇到問題：

1. 驗證跟蹤文件是否存在且可訪問。
2. 檢查跟蹤參數是否與用於生成跟蹤文件的參數匹配。
3. 確保閾值乘數適合您的分析需求。
4. 檢查控制台輸出中的錯誤消息。