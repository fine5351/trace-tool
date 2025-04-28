# Oracle PeopleSoft 应用引擎 (AE) 文档

## 目录

1. [概述](#概述)
2. [应用引擎跟踪参数](#应用引擎跟踪参数)
    - [在PeopleSoft中启用跟踪](#在peoplesoft中启用跟踪)
    - [常用跟踪参数](#常用跟踪参数)
    - [跟踪文件格式](#跟踪文件格式)
    - [跟踪的最佳实践](#跟踪的最佳实践)
    - [常见问题排查](#常见问题排查)
    - [高级跟踪技巧](#高级跟踪技巧)
3. [应用引擎跟踪比较器](#应用引擎跟踪比较器)
    - [概述](#跟踪比较器概述)
    - [功能](#功能)
    - [使用方法](#使用方法)
    - [输出格式](#输出格式)
    - [工作原理](#工作原理)
    - [故障排除](#故障排除)

## 概述

本文档提供了有关Oracle PeopleSoft应用引擎（AE）进程跟踪参数的指导，以及应用引擎跟踪比较器工具的使用说明。跟踪文件对于性能分析、调试和比较不同环境之间的执行情况非常有价值。

## 应用引擎跟踪参数

### 在PeopleSoft中启用跟踪

#### 基本跟踪参数

要为应用引擎进程启用跟踪，您可以设置以下参数：

1. **在进程调度器配置中：**
    - 导航至PeopleTools > Process Scheduler > Servers
    - 选择适当的进程调度器服务器
    - 转到"Trace"部分
    - 将应用引擎跟踪设置为级别1-4（数字越高提供的详细信息越多）

2. **在进程监视器中（针对单个运行）：**
    - 导航至PeopleTools > Process Scheduler > Process Monitor
    - 选择要跟踪的进程
    - 点击"Update Process"并设置跟踪选项
    - 设置应用引擎跟踪级别（1-4）

3. **在进程定义中：**
    - 导航至PeopleTools > Process Scheduler > Processes
    - 选择应用引擎进程
    - 转到"Override Options"选项卡
    - 设置跟踪参数

### 常用跟踪参数

| 参数               | 描述           | 值              |
|------------------|--------------|----------------|
| `-TRACE`         | 主要跟踪参数       | 1-4（1=最小，4=详细） |
| `-DBFLAGS`       | 数据库操作跟踪      | 1-15（标志组合）     |
| `-TOOLSTRACESQL` | SQL语句跟踪      | 1-31（标志组合）     |
| `-TOOLSTRACEPC`  | PeopleCode跟踪 | 1-31（标志组合）     |

#### DBFLAGS值

- 1: SQL语句
- 2: SQL语句变量
- 4: SQL连接、断开连接、提交和回滚
- 8: 行获取

#### TOOLSTRACESQL值

- 1: SQL语句
- 2: SQL语句变量
- 4: SQL连接、断开连接、提交和回滚
- 8: 行获取
- 16: 所有其他API调用

#### TOOLSTRACEPC值

- 1: 程序启动
- 2: 每个语句
- 4: 函数调用
- 8: 变量赋值
- 16: 内部函数调用

### 跟踪文件格式

应用引擎跟踪文件包含各种操作的时间戳条目：

#### 程序启动和结束

```
PSAPPSRV.9016 (1) [2023-05-15T14:25:30.000000] 开始执行应用引擎程序 PROCESSNAME.SECTION
...
PSAPPSRV.9016 (1) [2023-05-15T14:32:45.000000] 应用引擎程序 PROCESSNAME.SECTION 执行完成
总运行时间: 00:07:15
```

#### SQL执行

```
PSAPPSRV.9016 (1) [2023-05-15T14:25:31.000000] 执行SQL:
SELECT FIELD1, FIELD2 FROM PS_EXAMPLE_TABLE WHERE FIELD3 = 'VALUE'
返回行数: 150
执行时间: 1.25秒
```

#### PeopleCode执行

```
PSAPPSRV.9016 (1) [2023-05-15T14:26:05.000000] 开始执行PeopleCode: RECORD.FIELD FieldFormula
...
PSAPPSRV.9016 (1) [2023-05-15T14:26:15.000000] PeopleCode执行完成: RECORD.FIELD FieldFormula
执行时间: 10秒
```

### 跟踪的最佳实践

1. **使用适当的跟踪级别**
    - 级别1：基本信息，适合一般问题排查
    - 级别2：包含SQL语句，适合数据库相关问题
    - 级别3-4：详细信息，但会创建大文件并可能影响性能

2. **结合使用多个跟踪参数**
    - 对于SQL问题，组合使用`-TRACE`和`-TOOLSTRACESQL`
    - 对于PeopleCode问题，组合使用`-TRACE`和`-TOOLSTRACEPC`
    - 对于全面分析，使用`-TRACE 3 -TOOLSTRACESQL 31 -TOOLSTRACEPC 31`

3. **管理跟踪文件大小**
    - 对于大型程序，考虑仅跟踪特定部分
    - 使用条件跟踪（在特定步骤中启用/禁用跟踪）
    - 定期清理旧的跟踪文件

4. **在代表性数据下分析**
    - 使用与生产环境类似的数据量
    - 考虑数据分布对性能的影响

### 常见问题排查

#### 性能问题

1. **应用引擎程序运行缓慢**
    - 使用`-TOOLSTRACESQL`参数识别耗时的SQL语句
    - 检查是否有不必要的循环或重复查询
    - 验证索引使用情况

2. **内存使用过高**
    - 减少大型数组的使用
    - 检查是否有内存泄漏（未释放的资源）
    - 考虑分批处理大量数据

#### 数据问题

1. **结果不正确**
    - 使用`-TOOLSTRACESQL`和`-TOOLSTRACEPC`参数查看SQL和PeopleCode执行
    - 检查变量赋值和计算逻辑
    - 验证SQL语句的WHERE条件

2. **缺少数据**
    - 检查JOIN条件
    - 验证数据筛选条件
    - 确认数据源是否包含预期数据

#### 连接问题

1. **数据库连接失败**
    - 验证连接字符串和凭据
    - 检查网络连接
    - 确认数据库服务是否运行

2. **连接超时**
    - 检查长时间运行的SQL
    - 验证数据库资源限制
    - 考虑增加超时设置

### 高级跟踪技巧

#### 条件跟踪

在应用引擎程序中实现条件跟踪可以减少跟踪文件大小并专注于问题区域：

```
/* 在特定步骤中启用详细跟踪 */
If &ProblemSection Then
   &OldTraceLevel = GetTraceLevel();
   SetTraceLevel(4);
End-If;

/* 执行可能有问题的代码 */
...

/* 恢复原始跟踪级别 */
If &ProblemSection Then
   SetTraceLevel(&OldTraceLevel);
End-If;
```

#### 性能分析

创建自定义性能跟踪框架：

```
/* 开始计时 */
Local datetime &StartTime = %Datetime;

/* 执行代码 */
...

/* 结束计时并记录 */
Local datetime &EndTime = %Datetime;
Local number &ElapsedSeconds = DateTimeDiff(&EndTime, &StartTime);
WriteToLog(0, "执行时间: " | &ElapsedSeconds | " 秒");
```

## 应用引擎跟踪比较器

### 跟踪比较器概述

应用引擎跟踪比较器是一个用于比较来自不同环境的Oracle PeopleSoft Application Engine跟踪文件的工具。它分析执行时间，识别显著差异，并检测在一个环境中执行但在另一个环境中未执行的代码。

### 功能

- 比较两个跟踪文件并识别具有显著时间差异的条目
- 检测在一个环境中执行但在另一个环境中未执行的代码
- 可配置的时间差异阈值（n倍）
- 支持多种跟踪参数（-TRACE、-TOOLSTRACEPC、-TOOLSTRACESQL等）
- 将结果输出到文件

### 使用方法

#### 程序代码使用

您可以在代码中直接使用`NewAETraceComparator`类：

```java
import com.example.core.tool.NewAETraceComparator;
import com.example.core.tool.analyzer.TraceAnalyzer;
import com.example.core.tool.analyzer.TraceAnalyzerFactory;
import com.example.core.tool.analyzer.TraceEntry;

import java.io.IOException;
import java.util.List;

public class Example {
    public static void main(String[] args) throws IOException {
        // 设置参数
        String env1TraceFile = "D:\\traces\\env1_trace.log";
        String env2TraceFile = "D:\\traces\\env2_trace.log";
        double thresholdMultiplier = 2.0;
        String outputPath = "ae_trace_comparison_result.txt";
        String traceParams = "-TRACE 3 -TOOLSTRACEPC 4044 -TOOLSTRACESQL 31";

        // 提取环境名称
        String env1Name = "环境1";
        String env2Name = "环境2";

        // 创建分析器
        List<TraceAnalyzer> analyzers = TraceAnalyzerFactory.createAnalyzersForParams(traceParams);

        // 解析跟踪文件
        List<TraceEntry> env1Entries = NewAETraceComparator.parseTraceWithMultipleAnalyzers(env1TraceFile, analyzers);
        List<TraceEntry> env2Entries = NewAETraceComparator.parseTraceWithMultipleAnalyzers(env2TraceFile, analyzers);

        // 比较跟踪
        NewAETraceComparator.compareTraces(env1Entries, env2Entries, env1Name, env2Name, outputPath, thresholdMultiplier);
    }
}
```

### 输出格式

输出文件包含：

1. 一个CSV格式的部分，包含以下列：
    - 类型：跟踪条目的类型（STEP、SQL、FUNCTION、METHOD）
    - 标识符：跟踪条目的标识符
    - ENV1(ms)：第一个环境中的持续时间（毫秒）
    - ENV2(ms)：第二个环境中的持续时间（毫秒）
    - Diff(ms)：绝对差异（毫秒）
    - Diff(%)：百分比差异
    - 标志：如果时间差异超过阈值，则为THRESHOLD_EXCEEDED
    - 详细信息：关于内容差异的附加详细信息

2. 列出在一个环境中执行但在另一个环境中未执行的代码的部分：
    - ENV1中的额外代码：仅出现在第一个环境中的条目列表
    - ENV2中的额外代码：仅出现在第二个环境中的条目列表

### 工作原理

1. 该工具使用基于跟踪参数的适当分析器解析跟踪文件。
2. 它比较两个环境之间具有相同标识符的条目。
3. 它标记时间差异超过指定阈值的条目。
4. 它识别并记录在一个环境中执行但在另一个环境中未执行的代码。
5. 它将结果输出到文件。

### 故障排除

如果遇到问题：

1. 验证跟踪文件是否存在且可访问。
2. 检查跟踪参数是否与用于生成跟踪文件的参数匹配。
3. 确保阈值乘数适合您的分析需求。
4. 检查控制台输出中的错误消息。