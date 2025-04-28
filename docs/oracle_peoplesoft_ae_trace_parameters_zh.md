# Oracle PeopleSoft 应用引擎跟踪参数

## 概述

本文档提供了有关Oracle PeopleSoft应用引擎（AE）进程跟踪参数的指导。跟踪文件对于性能分析、调试和比较不同环境之间的执行情况非常有价值。

## 在PeopleSoft中启用跟踪

### 基本跟踪参数

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

| 参数 | 描述 | 值 |
|-----------|-------------|--------|
| `-TRACE` | 主要跟踪参数 | 1-4（1=最小，4=详细） |
| `-DBFLAGS` | 数据库操作跟踪 | 1-15（标志组合） |
| `-TOOLSTRACESQL` | SQL语句跟踪 | 1-31（标志组合） |
| `-TOOLSTRACEPC` | PeopleCode跟踪 | 1-31（标志组合） |

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

## 跟踪文件格式

PeopleSoft应用引擎跟踪文件包含各种操作的时间戳条目：

### 步骤执行
```
14:25:30.156 (2426) Begin Step: EXAMPLE.MAIN.GBL.Step01
... 步骤内的操作 ...
14:25:32.438 (2426) End Step: EXAMPLE.MAIN.GBL.Step01
```

### SQL执行
```
14:25:31.203 (2426) (SQLExec) SELECT FIELD1, FIELD2 FROM PS_EXAMPLE_TABLE WHERE FIELD3 = :1
14:25:31.250 (2426) Bind-Variables: 1=VALUE
```

### 函数调用
```
14:25:31.328 (2426) Function: GetField
```

### 方法调用
```
14:25:31.456 (2426) Method: GetNextRecord
```

## 使用AETraceComparator工具

我们的`AETraceComparator`工具有助于分析和比较来自不同环境的跟踪文件。

### 使用方法

```
java -cp trace-tool.jar com.example.core.tool.AETraceComparator <SIT_trace_file> <UAT_trace_file>
```

### 工具功能

1. 解析跟踪文件以提取：
   - 步骤执行时间
   - SQL执行时间
   - 函数调用时间
   - 方法调用时间

2. 比较环境之间的执行时间
   - 识别具有显著性能差异的操作（>20%）
   - 将结果输出到控制台和CSV文件

### 输出示例

```
Identifier                                                 SIT (ms)       UAT (ms)       Diff       Flag      
========================================================================================================================
STEP: EXAMPLE.MAIN.GBL.Step01                             1250           1350           100                  
STEP: EXAMPLE.MAIN.GBL.Step02                             2500           3500           1000        ALERT     
SQL#1                                                     150            180            30                   
FUNC: GetField#1                                          75             95             20                   
```

## 最佳实践

1. **使用适当的跟踪级别**
   - 级别1：最少信息，低开销
   - 级别2：基本步骤和SQL信息
   - 级别3：详细的SQL和PeopleCode信息
   - 级别4：最详细，但会创建大文件并可能影响性能

2. **比较类似环境**
   - 确保两个环境具有类似的硬件规格
   - 使用类似的数据量进行准确比较

3. **在代表性负载下分析**
   - 在正常工作时间进行跟踪，以获得类似生产的条件
   - 考虑非工作时间跟踪以获取基准性能

4. **管理跟踪文件大小**
   - 尽可能将跟踪限制在特定步骤
   - 对于长时间运行的进程，考虑仅对有问题的部分启用跟踪

## 故障排除

### 常见问题

1. **缺少跟踪文件**
   - 验证跟踪参数是否正确设置
   - 检查输出目录中的文件权限
   - 确保进程完全运行

2. **跟踪信息不完整**
   - 增加跟踪级别以获取更详细的信息
   - 验证是否启用了所有必需的跟踪标志

3. **性能影响**
   - 在生产环境中降低跟踪级别
   - 考虑仅跟踪特定步骤或部分

## 应用引擎跟踪参数组合及其效果

不同的跟踪参数可以组合使用，以获取更全面的调试信息。以下是常用的参数组合、它们的效果以及产生的输出示例：

### 基本参数组合

| 参数组合               | 描述                  | 适用场景    | 输出文件大小 | 性能影响 |
|--------------------|---------------------|---------|--------|------|
| `-TRACE 1`         | 基本程序流程和错误           | 一般性问题排查 | 小      | 低    |
| `-TRACE 2`         | 包括SQL语句和步骤执行        | 数据库相关问题 | 中      | 中    |
| `-TRACE 3`         | 详细的SQL和PeopleCode信息 | 复杂问题分析  | 大      | 高    |
| `-TRACE 4`         | 最详细，包括所有操作和内存使用情况   | 深入调试    | 非常大    | 非常高  |
| `-DBFLAGS 1`       | 仅SQL语句              | SQL语法问题 | 小      | 低    |
| `-DBFLAGS 3`       | SQL语句和变量            | 数据绑定问题  | 中      | 低    |
| `-TOOLSTRACESQL 1` | 仅SQL语句              | SQL语法问题 | 小      | 低    |
| `-TOOLSTRACEPC 1`  | 程序启动                | 初始化问题   | 小      | 低    |
| `-TOOLSTRACEPC 3`  | 程序启动和函数调用           | 函数执行问题  | 中      | 中    |

### 高级参数组合

| 参数组合                                          | 描述                  | 适用场景           | 输出示例                                                    |
|-----------------------------------------------|---------------------|----------------|---------------------------------------------------------|
| `-TRACE 2 -DBFLAGS 3`                         | 步骤执行和SQL语句（含变量）     | SQL执行问题        | [见下文](#trace-2--dbflags-3-输出示例)                         |
| `-TRACE 2 -TOOLSTRACESQL 3`                   | 步骤执行和SQL语句（含变量）     | SQL执行问题        | [见下文](#trace-2--toolstracesql-3-输出示例)                   |
| `-TRACE 3 -TOOLSTRACEPC 3`                    | 详细步骤和PeopleCode函数调用 | PeopleCode执行问题 | [见下文](#trace-3--toolstracepc-3-输出示例)                    |
| `-TRACE 3 -TOOLSTRACEPC 7`                    | 详细步骤、函数调用和变量赋值      | 数据处理问题         | [见下文](#trace-3--toolstracepc-7-输出示例)                    |
| `-TRACE 3 -TOOLSTRACESQL 7`                   | 详细步骤、SQL语句和变量       | 数据库交互问题        | [见下文](#trace-3--toolstracesql-7-输出示例)                   |
| `-TRACE 3 -TOOLSTRACESQL 7 -TOOLSTRACEPC 7`   | 全面的SQL和PeopleCode跟踪 | 复杂问题的深入分析      | [见下文](#trace-3--toolstracesql-7--toolstracepc-7-输出示例)   |
| `-TRACE 4 -TOOLSTRACESQL 31 -TOOLSTRACEPC 31` | 最详细的跟踪信息            | 系统级问题排查        | [见下文](#trace-4--toolstracesql-31--toolstracepc-31-输出示例) |

### 参数组合输出示例

#### `-TRACE 2 -DBFLAGS 3` 输出示例

此组合提供步骤执行和SQL语句（含变量）信息，适合分析SQL执行问题：

```
14:25:30.156 (2426) Begin Step: EXAMPLE.MAIN.GBL.Step01
14:25:31.203 (2426) (SQLExec) SELECT FIELD1, FIELD2 FROM PS_EXAMPLE_TABLE WHERE FIELD3 = :1
14:25:31.250 (2426) Bind-Variables: 1=VALUE
14:25:32.438 (2426) End Step: EXAMPLE.MAIN.GBL.Step01
```

#### `-TRACE 2 -TOOLSTRACESQL 3` 输出示例

此组合提供步骤执行和SQL语句（含变量）信息，功能类似于`-TRACE 2 -DBFLAGS 3`但使用PeopleTools SQL跟踪：

```
14:25:30.156 (2426) Begin Step: EXAMPLE.MAIN.GBL.Step01
14:25:31.203 (2426) (SQLExec) SELECT FIELD1, FIELD2 FROM PS_EXAMPLE_TABLE WHERE FIELD3 = :1
14:25:31.250 (2426) Bind-Variables: 1=VALUE
14:25:31.875 (2426) Rows fetched: 10
14:25:32.438 (2426) End Step: EXAMPLE.MAIN.GBL.Step01
```

#### `-TRACE 3 -TOOLSTRACEPC 3` 输出示例

此组合提供详细步骤和PeopleCode函数调用信息，适合分析PeopleCode执行问题：

```
14:25:30.156 (2426) Begin Step: EXAMPLE.MAIN.GBL.Step01
14:25:30.203 (2426) Executing PeopleCode in EXAMPLE.MAIN FieldFormula
14:25:30.250 (2426) Function: GetField
14:25:30.328 (2426) Function: GetNextRecord
14:25:31.203 (2426) (SQLExec) SELECT FIELD1, FIELD2 FROM PS_EXAMPLE_TABLE WHERE FIELD3 = :1
14:25:31.250 (2426) Bind-Variables: 1=VALUE
14:25:32.438 (2426) End Step: EXAMPLE.MAIN.GBL.Step01
```

#### `-TRACE 3 -TOOLSTRACEPC 7` 输出示例

此组合提供详细步骤、函数调用和变量赋值信息，适合分析数据处理问题：

```
14:25:30.156 (2426) Begin Step: EXAMPLE.MAIN.GBL.Step01
14:25:30.203 (2426) Executing PeopleCode in EXAMPLE.MAIN FieldFormula
14:25:30.250 (2426) Function: GetField
14:25:30.275 (2426) Variable &DEPTID = "10000"
14:25:30.328 (2426) Function: GetNextRecord
14:25:30.350 (2426) Variable &FOUND = True
14:25:31.203 (2426) (SQLExec) SELECT FIELD1, FIELD2 FROM PS_EXAMPLE_TABLE WHERE FIELD3 = :1
14:25:31.250 (2426) Bind-Variables: 1=VALUE
14:25:32.438 (2426) End Step: EXAMPLE.MAIN.GBL.Step01
```

#### `-TRACE 3 -TOOLSTRACESQL 7` 输出示例

此组合提供详细步骤、SQL语句和变量信息，适合分析数据库交互问题：

```
14:25:30.156 (2426) Begin Step: EXAMPLE.MAIN.GBL.Step01
14:25:31.203 (2426) (SQLExec) SELECT FIELD1, FIELD2 FROM PS_EXAMPLE_TABLE WHERE FIELD3 = :1
14:25:31.250 (2426) Bind-Variables: 1=VALUE
14:25:31.875 (2426) Rows fetched: 10
14:25:31.900 (2426) Column values:
14:25:31.901 (2426) FIELD1=10000, FIELD2=Finance
14:25:31.902 (2426) FIELD1=20000, FIELD2=HR
14:25:31.903 (2426) FIELD1=30000, FIELD2=IT
14:25:32.000 (2426) (SQLExec) COMMIT
14:25:32.438 (2426) End Step: EXAMPLE.MAIN.GBL.Step01
```

#### `-TRACE 3 -TOOLSTRACESQL 7 -TOOLSTRACEPC 7` 输出示例

此组合提供全面的SQL和PeopleCode跟踪，适合复杂问题的深入分析：

```
14:25:30.156 (2426) Begin Step: EXAMPLE.MAIN.GBL.Step01
14:25:30.203 (2426) Executing PeopleCode in EXAMPLE.MAIN FieldFormula
14:25:30.250 (2426) Function: GetField
14:25:30.275 (2426) Variable &DEPTID = "10000"
14:25:30.328 (2426) Function: GetNextRecord
14:25:30.350 (2426) Variable &FOUND = True
14:25:31.203 (2426) (SQLExec) SELECT FIELD1, FIELD2 FROM PS_EXAMPLE_TABLE WHERE FIELD3 = :1
14:25:31.250 (2426) Bind-Variables: 1=VALUE
14:25:31.875 (2426) Rows fetched: 10
14:25:31.900 (2426) Column values:
14:25:31.901 (2426) FIELD1=10000, FIELD2=Finance
14:25:31.902 (2426) FIELD1=20000, FIELD2=HR
14:25:31.903 (2426) FIELD1=30000, FIELD2=IT
14:25:32.000 (2426) (SQLExec) COMMIT
14:25:32.100 (2426) Function: SetNextRecord
14:25:32.150 (2426) Variable &RESULT = True
14:25:32.438 (2426) End Step: EXAMPLE.MAIN.GBL.Step01
```

#### `-TRACE 4 -TOOLSTRACESQL 31 -TOOLSTRACEPC 31` 输出示例

此组合提供最详细的跟踪信息，适合系统级问题排查：

```
14:25:30.000 (2426) Application Engine program EXAMPLE.MAIN starting
14:25:30.050 (2426) Memory allocation: Initial heap size = 8MB
14:25:30.100 (2426) Database connection established: User=PS, Database=PSFT_HR
14:25:30.156 (2426) Begin Step: EXAMPLE.MAIN.GBL.Step01
14:25:30.203 (2426) Executing PeopleCode in EXAMPLE.MAIN FieldFormula
14:25:30.220 (2426) PeopleCode program loaded from cache
14:25:30.230 (2426) PeopleCode execution context initialized
14:25:30.240 (2426) PeopleCode API version: 8.59.12
14:25:30.250 (2426) Function: GetField
14:25:30.260 (2426) Function parameters: DEPTID, RECORD.DEPT_TBL, 1
14:25:30.275 (2426) Variable &DEPTID = "10000"
14:25:30.280 (2426) Memory usage: Current heap = 12MB, +4MB
14:25:30.328 (2426) Function: GetNextRecord
14:25:30.340 (2426) Function parameters: RECORD.DEPT_TBL
14:25:30.350 (2426) Variable &FOUND = True
14:25:31.000 (2426) SQL statement preparation started
14:25:31.100 (2426) SQL statement parsed
14:25:31.150 (2426) SQL execution plan:
14:25:31.151 (2426) OPERATION                  OPTIONS           OBJECT_NAME
14:25:31.152 (2426) -----------------------------------------------------------
14:25:31.153 (2426) TABLE ACCESS               BY INDEX ROWID    PS_EXAMPLE_TABLE
14:25:31.154 (2426)   INDEX                    RANGE SCAN        PS_EXAMPLE_IDX
14:25:31.203 (2426) (SQLExec) SELECT FIELD1, FIELD2 FROM PS_EXAMPLE_TABLE WHERE FIELD3 = :1
14:25:31.250 (2426) Bind-Variables: 1=VALUE
14:25:31.300 (2426) SQL statement execution started
14:25:31.400 (2426) SQL buffer allocation: 256KB
14:25:31.500 (2426) Database statistics:
14:25:31.501 (2426) Buffer gets: 120
14:25:31.502 (2426) Disk reads: 5
14:25:31.503 (2426) CPU time: 0.15 seconds
14:25:31.600 (2426) Wait events:
14:25:31.601 (2426) db file sequential read (5 times, total 0.05 seconds)
14:25:31.875 (2426) Rows fetched: 10
14:25:31.900 (2426) Column values:
14:25:31.901 (2426) FIELD1=10000, FIELD2=Finance
14:25:31.902 (2426) FIELD1=20000, FIELD2=HR
14:25:31.903 (2426) FIELD1=30000, FIELD2=IT
14:25:32.000 (2426) (SQLExec) COMMIT
14:25:32.050 (2426) Transaction committed
14:25:32.100 (2426) Function: SetNextRecord
14:25:32.110 (2426) Function parameters: RECORD.DEPT_TBL, &DEPT_REC
14:25:32.150 (2426) Variable &RESULT = True
14:25:32.200 (2426) Memory usage: Current heap = 14MB, +2MB
14:25:32.438 (2426) End Step: EXAMPLE.MAIN.GBL.Step01
14:25:32.500 (2426) Step execution time: 2.344 seconds
14:25:32.550 (2426) Step performance breakdown:
14:25:32.551 (2426) SQL execution: 1.2 seconds (51.2%)
14:25:32.552 (2426) PeopleCode execution: 0.8 seconds (34.1%)
14:25:32.553 (2426) Other operations: 0.344 seconds (14.7%)
14:25:32.600 (2426) Application Engine program EXAMPLE.MAIN ending
14:25:32.650 (2426) Total execution time: 2.65 seconds
14:25:32.700 (2426) Memory usage summary:
14:25:32.701 (2426) Peak heap size: 15MB
14:25:32.702 (2426) Total allocated: 25MB
14:25:32.703 (2426) Total freed: 10MB
```

### 参数组合选择指南

1. **性能问题排查**
   - 推荐组合: `-TRACE 3 -TOOLSTRACESQL 7`
   - 关注点: SQL执行时间、行获取数量、提交/回滚操作

2. **数据问题排查**
   - 推荐组合: `-TRACE 3 -TOOLSTRACESQL 7 -TOOLSTRACEPC 7`
   - 关注点: SQL语句、绑定变量、结果集、变量赋值

3. **PeopleCode逻辑问题**
   - 推荐组合: `-TRACE 3 -TOOLSTRACEPC 15`
   - 关注点: 函数调用、变量赋值、内部函数调用

4. **全面分析**
   - 推荐组合: `-TRACE 4 -TOOLSTRACESQL 31 -TOOLSTRACEPC 31`
   - 关注点: 所有方面，但注意文件大小和性能影响

5. **特定步骤问题**
   - 推荐组合: `-TRACE 3` 并在进程监视器中仅为特定步骤启用跟踪
   - 关注点: 特定步骤的执行详情，减少跟踪文件大小

选择合适的参数组合时，应平衡调试需求与性能影响。在生产环境中，建议使用较低级别的跟踪参数，或者仅在必要时短暂启用高级别跟踪。

## 结论

PeopleSoft应用引擎跟踪参数提供了对进程执行和性能的宝贵见解。通过正确配置跟踪参数并使用AETraceComparator等分析工具，您可以识别性能瓶颈，比较环境，并优化您的PeopleSoft应用程序。
