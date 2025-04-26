package com.example.core;

import com.example.core.tool.NewAETraceComparator;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * 一個簡單的命令行工具，用於比較兩個Oracle PeopleSoft Application Engine跟蹤文件。
 * 此工具使用NewAETraceComparator來執行比較。
 */
@Slf4j
public class AETraceComparisonTool {

    /**
     * 主方法，用於運行比較工具。
     *
     * @param args 命令行參數
     */
    public static void main(String[] args) {
        try {
            // 直接將參數傳遞給NewAETraceComparator的main方法
            NewAETraceComparator.main(args);
        } catch (IOException e) {
            log.error("比較跟蹤文件時發生錯誤: {}", e.getMessage(), e);
            System.err.println("比較跟蹤文件時發生錯誤: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            log.error("執行比較工具時發生未預期的錯誤: {}", e.getMessage(), e);
            System.err.println("執行比較工具時發生未預期的錯誤: " + e.getMessage());
            System.exit(2);
        }
    }
}