# Oracle PeopleSoft SQR 跟蹤比較器使用指南

本文檔提供了使用新的Oracle PeopleSoft SQR跟蹤比較器工具的說明。

## 概述

SQR跟蹤比較器是一個用於比較兩個Oracle PeopleSoft SQR跟蹤文件之間執行時間的工具。它可以識別：

- 執行時間差異超過指定閾值的行
- 存在於一個文件但不存在於另一個文件的代碼
- 跟蹤文件之間的其他差異

## 功能

- 在main方法中設定兩個文件路徑
- 處理具有相同跟蹤參數的跟蹤文件
- 分析每行數據並封裝在DTO中
- 查找執行時間差異超過n倍的行（n是可變的）
- 處理一個文件有而另一個文件沒有的代碼的情況
- 將差異輸出到文件

## 使用方法

### 程式碼使用

您可以在代碼中直接使用`NewSQRTraceComparator`類：

```java
import com.example.core.tool.NewSQRTraceComparator;

import java.io.IOException;
import java.util.List;

public class Example {
    public static void main(String[] args) throws IOException {
        // 解析跟蹤文件
        List<NewSQRTraceComparator.TraceEntry> file1Entries =
                NewSQRTraceComparator.parseTrace("path/to/file1.log");
        List<NewSQRTraceComparator.TraceEntry> file2Entries =
                NewSQRTraceComparator.parseTrace("path/to/file2.log");

        // 比較跟蹤，閾值為2.0
        NewSQRTraceComparator.compareTraces(
                file1Entries,
                file2Entries,
                2.0,
                "output.txt"
        );
    }
}
```

## 輸出格式

輸出文件是一個Markdown格式的文本文件，包含：

1. 帶有比較閾值的標題
2. 檢測到的時間差異部分
3. 在任一文件中發現的額外代碼部分
4. 不匹配行的部分

輸出示例：

```
# SQR跟蹤比較結果
閾值：2.0倍

## 檢測到時間差異
行內容：执行SQL (10:01:00):
文件1行：3，執行時間：36060000毫秒
文件2行：3，執行時間：72120000毫秒
比率：2.00倍

## 文件2中的額外代碼
開始於行：6
行6：执行SQL (10:01:30):
行7：SELECT * FROM DEPARTMENTS
行8：执行时间: 1.5秒
```

## 跟蹤文件格式

該工具設計用於處理使用`-S -TIMING -debugfgt -E`等跟蹤參數的Oracle PeopleSoft SQR跟蹤文件。它會在每行中查找格式為`(HH:MM:SS)`的時間信息。

## 故障排除

如果遇到問題：

1. 驗證跟蹤文件是否存在且可訪問
2. 檢查跟蹤文件是否包含預期格式的時間信息
3. 確保閾值是有效的數字
4. 檢查輸出目錄是否存在且可寫入