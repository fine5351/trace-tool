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

## 结论

PeopleSoft应用引擎跟踪参数提供了对进程执行和性能的宝贵见解。通过正确配置跟踪参数并使用AETraceComparator等分析工具，您可以识别性能瓶颈，比较环境，并优化您的PeopleSoft应用程序。