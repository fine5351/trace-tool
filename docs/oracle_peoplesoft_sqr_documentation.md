# Oracle PeopleSoft SQR (结构化查询报表) 文档

## 目录

1. [概述](#概述)
2. [SQR跟踪参数](#sqr跟踪参数)
    - [在PeopleSoft中启用SQR跟踪](#在peoplesoft中启用sqr跟踪)
    - [常用SQR跟踪参数](#常用sqr跟踪参数)
    - [SQR跟踪文件格式](#sqr跟踪文件格式)
    - [SQR跟踪的最佳实践](#sqr跟踪的最佳实践)
    - [SQR程序中的内置跟踪功能](#sqr程序中的内置跟踪功能)
    - [常见问题排查](#常见问题排查)
    - [高级跟踪技巧](#高级跟踪技巧)
    - [SQR跟踪参数组合及其效果](#sqr跟踪参数组合及其效果)
3. [SQR跟踪比较器](#sqr跟踪比较器)
    - [概述](#跟踪比较器概述)
    - [功能](#功能)
    - [使用方法](#使用方法)
    - [输出格式](#输出格式)
    - [跟踪文件格式要求](#跟踪文件格式要求)
    - [故障排除](#故障排除)

## 概述

本文档提供了有关Oracle PeopleSoft SQR（结构化查询报表）进程跟踪参数的指导，以及SQR跟踪比较器工具的使用说明。SQR是PeopleSoft中用于报表生成和批处理的重要工具，跟踪功能对于性能分析、调试和解决SQR程序问题非常有价值。

## SQR跟踪参数

### 在PeopleSoft中启用SQR跟踪

#### 基本跟踪参数

要为SQR进程启用跟踪，您可以设置以下参数：

1. **在进程调度器配置中：**
   - 导航至PeopleTools > Process Scheduler > Servers
   - 选择适当的进程调度器服务器
   - 转到"Trace"部分
   - 将SQR跟踪设置为适当的级别

2. **在进程监视器中（针对单个运行）：**
   - 导航至PeopleTools > Process Scheduler > Process Monitor
   - 选择要跟踪的SQR进程
   - 点击"Update Process"并设置跟踪选项
   - 设置SQR跟踪级别

3. **在进程定义中：**
   - 导航至PeopleTools > Process Scheduler > Processes
   - 选择SQR进程
   - 转到"Override Options"选项卡
   - 设置跟踪参数

### 常用SQR跟踪参数

| 参数 | 描述 | 值 |
|-----------|-------------|--------|
| `-SQRTRACE` | 启用SQR跟踪 | 0-4（0=禁用，4=最详细） |
| `-DEBUG` | 启用调试模式 | Y/N |
| `-S` | 显示SQL语句 | N/A |
| `-RS` | 显示SQL结果集 | N/A |
| `-RT` | 显示运行时间 | N/A |
| `-PRINTER:LP` | 将输出发送到日志 | N/A |

#### SQRTRACE值详解
- 0: 禁用跟踪
- 1: 基本程序流程和错误
- 2: 包括SQL语句
- 3: 包括变量值和中间结果
- 4: 最详细，包括所有操作和内存使用情况

#### DEBUG值详解
- Y: 启用调试模式，提供以下功能：
  - 在控制台显示详细的执行信息
  - 显示程序流程和变量值
  - 输出中间计算结果
  - 显示条件判断的评估过程
  - 在出错时提供更详细的错误上下文
- N: 禁用调试模式（默认值）
- 字符串格式（如`-debugabcde`）: 启用特定的调试选项，每个字符代表一个不同的调试标志：
  - a: 显示程序流程和控制结构（包括过程进入/退出、条件分支执行）
  - b: 显示变量赋值和计算（包括变量初始化、值变更和计算过程）
  - c: 显示SQL语句执行（包括完整SQL文本和执行计划）
  - d: 显示数据库操作结果（包括返回的行数和数据内容）
  - e: 显示内存使用情况（包括内存分配和释放）
  - f: 显示文件操作（包括文件打开、关闭、读取和写入）
  - g: 显示图形和打印操作（包括页面布局和格式化）
  - h: 显示HTTP和网络操作（包括网络请求和响应）
  - i: 显示输入参数处理（包括命令行和运行时参数）
  - j: 显示作业控制信息（包括作业启动和结束）
  - k: 显示键值和索引操作（包括键的创建和使用）
  - l: 显示日志和消息（包括系统消息和用户定义消息）
  - m: 显示数学运算详情（包括复杂计算和函数调用）
  - n: 显示命名约定和对象引用（包括变量命名和引用解析）
  - o: 显示优化信息（包括代码和查询优化）
  - p: 显示性能统计（包括执行时间和资源使用）
  - q: 显示查询构建过程（包括动态SQL生成）
  - r: 显示记录处理（包括记录读取、写入和更新）
  - s: 显示字符串操作（包括字符串连接、截取和格式化）
  - t: 显示事务处理（包括事务开始、提交和回滚）
  - u: 显示用户交互（包括用户输入和输出）
  - v: 显示验证逻辑（包括数据验证和错误检查）
  - w: 显示警告信息（包括非致命错误和警告）
  - x: 显示XML处理（包括XML解析和生成）
  - y: 显示同步操作（包括多线程和锁定）
  - z: 显示压缩/解压缩操作（包括数据压缩和解压缩）

使用DEBUG参数时的注意事项：
- 启用DEBUG会显著降低执行速度，仅建议在开发和问题排查时使用
- 可与SQRTRACE参数结合使用以获得更全面的调试信息
- 在生产环境中应设置为N或完全移除此参数
- 使用字符串格式（如`-debugabcde`）可以更精确地控制需要调试的特定方面，减少不必要的信息输出

### SQR跟踪文件格式

SQR跟踪文件包含各种操作的时间戳条目：

#### 程序启动和结束
```
SQR开始执行: 2023-05-15 14:25:30
程序: EXAMPLE.SQR
用户: PS
数据库: PSFT_HR
...
SQR结束执行: 2023-05-15 14:32:45
总运行时间: 00:07:15
```

#### SQL执行
```
执行SQL (14:25:31):
SELECT FIELD1, FIELD2 FROM PS_EXAMPLE_TABLE WHERE FIELD3 = 'VALUE'
返回行数: 150
执行时间: 1.25秒
```

#### 过程调用
```
开始过程: PROCESS_DATA (14:26:05)
...
结束过程: PROCESS_DATA (14:26:15)
过程执行时间: 10秒
```

#### 变量赋值
```
变量赋值 (14:26:10):
$TOTAL_AMOUNT = 15250.75
```

### SQR跟踪的最佳实践

1. **使用适当的跟踪级别**
   - 级别1：基本信息，适合一般问题排查
   - 级别2：包含SQL语句，适合数据库相关问题
   - 级别3-4：详细信息，但会创建大文件并可能影响性能

2. **结合使用多个跟踪参数**
   - 对于SQL问题，组合使用`-SQRTRACE`和`-S`
   - 对于性能问题，添加`-RT`参数
   - 对于数据问题，添加`-RS`参数查看结果集

3. **管理跟踪文件大小**
   - 对于大型报表，考虑仅跟踪特定部分
   - 使用条件跟踪（在SQR代码中动态开启/关闭跟踪）
   - 定期清理旧的跟踪文件

4. **在代表性数据下分析**
   - 使用与生产环境类似的数据量
   - 考虑数据分布对性能的影响

### SQR程序中的内置跟踪功能

SQR提供了可以直接在程序中使用的内置跟踪命令：

#### SHOW命令
```
show '当前处理: ' $current_id
show 'SQL结果: ' #sql_count
```

#### DISPLAY命令
```
display 'DEBUG: 计算总额...'
display 'DEBUG: 总额 = ' #total_amount
```

#### 使用#DEBUG变量
```
begin-setup
  declare-variable
    #debug = 1  ! 0=禁用调试，1=启用调试
  end-declare
end-setup

if #debug
  show '调试信息: 正在处理部门 ' $dept_id
end-if
```

### 常见问题排查

#### 性能问题

1. **SQR程序运行缓慢**
   - 使用`-RT`参数识别耗时的SQL语句
   - 检查是否有不必要的循环或重复查询
   - 验证索引使用情况

2. **内存使用过高**
   - 减少大型数组的使用
   - 检查是否有内存泄漏（未释放的资源）
   - 考虑分批处理大量数据

#### 数据问题

1. **结果不正确**
   - 使用`-RS`参数查看SQL结果集
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

在SQR程序中实现条件跟踪可以减少跟踪文件大小并专注于问题区域：

```
begin-procedure enable-trace
  if $problem_section = 'Y'
    alter-printer
      point-size = 0
      font = 0
      symbol-set = 0
      trace = 3
    end-alter
  end-if
end-procedure

begin-procedure disable-trace
  alter-printer
    trace = 0
  end-alter
end-procedure
```

#### 性能分析

创建自定义性能跟踪框架：

```
begin-procedure start-timer($timer_name)
  do get-current-time
  let $start_time_{$timer_name} = $current_time
  if #debug
    show '开始计时: ' $timer_name ' 在 ' $current_time
  end-if
end-procedure

begin-procedure end-timer($timer_name)
  do get-current-time
  let #elapsed = datediff('ss', $start_time_{$timer_name}, $current_time)
  if #debug
    show '结束计时: ' $timer_name ' 耗时: ' #elapsed ' 秒'
  end-if
end-procedure
```

### SQR跟踪参数组合及其效果

不同的跟踪参数可以组合使用，以获取更全面的调试信息。以下是常用的参数组合、它们的效果以及适用场景：

#### 基本参数组合

| 参数组合          | 描述                | 适用场景     | 输出文件大小 | 性能影响 |
|---------------|-------------------|----------|--------|------|
| `-SQRTRACE 1` | 基本程序流程和错误         | 一般性问题排查  | 小      | 低    |
| `-SQRTRACE 2` | 包括SQL语句           | 数据库相关问题  | 中      | 中    |
| `-SQRTRACE 3` | 包括变量值和中间结果        | 数据计算问题   | 大      | 高    |
| `-SQRTRACE 4` | 最详细，包括所有操作和内存使用情况 | 复杂问题深入分析 | 非常大    | 非常高  |
| `-DEBUG Y`    | 调试模式，显示详细执行信息     | 开发和测试阶段  | 中      | 中    |
| `-S`          | 显示SQL语句           | SQL相关问题  | 中      | 低    |
| `-RS`         | 显示SQL结果集          | 数据问题     | 大      | 中    |
| `-RT`         | 显示运行时间            | 性能问题     | 小      | 低    |

#### 高级参数组合

| 参数组合                     | 描述                     | 适用场景      |
|--------------------------|------------------------|-----------| 
| `-SQRTRACE 2 -S`         | 详细SQL执行信息，包括SQL语句和执行计划 | SQL性能问题   |
| `-SQRTRACE 2 -RT`        | SQL语句及其执行时间            | SQL性能瓶颈识别 |
| `-SQRTRACE 3 -RS`        | 变量值、中间结果和SQL结果集        | 数据计算和转换问题 |
| `-SQRTRACE 3 -S -RT`     | 变量值、SQL语句和执行时间         | 全面性能分析    |
| `-SQRTRACE 3 -S -RS`     | 变量值、SQL语句和结果集          | 数据流问题     |
| `-SQRTRACE 4 -S -RT -RS` | 最全面的跟踪信息               | 复杂问题的深入分析 |
| `-DEBUG abcde -S`        | 特定调试标志与SQL跟踪结合         | 针对性调试     |

#### 参数组合选择指南

1. **性能问题排查**
    - 推荐组合: `-SQRTRACE 2 -S -RT`
    - 关注点: SQL执行时间、执行计划、时间分布

2. **数据问题排查**
    - 推荐组合: `-SQRTRACE 3 -S -RS`
    - 关注点: SQL语句、结果集、变量值

3. **程序逻辑问题**
    - 推荐组合: `-SQRTRACE 3 -DEBUG Y`
    - 关注点: 程序流程、条件判断、变量值

4. **全面分析**
    - 推荐组合: `-SQRTRACE 4 -S -RT -RS`
    - 关注点: 所有方面，但注意文件大小和性能影响

5. **针对性调试**
    - 推荐组合: `-DEBUG` 加特定标志（如`-DEBUG abcde`）
    - 关注点: 根据选择的调试标志定制输出

选择合适的参数组合时，应平衡调试需求与性能影响。在生产环境中，建议使用较低级别的跟踪参数，或者仅在必要时短暂启用高级别跟踪。

## SQR跟踪比较器

### 跟踪比较器概述

SQR跟踪比较器是一个用于比较两个Oracle PeopleSoft SQR跟踪文件之间执行时间的工具。它可以识别执行时间差异超过指定阈值的行、存在于一个文件但不存在于另一个文件的代码，以及跟踪文件之间的其他差异。

### 功能

- 在main方法中设定两个文件路径
- 处理具有相同跟踪参数的跟踪文件
- 分析每行数据并封装在DTO中
- 查找执行时间差异超过n倍的行（n是可变的）
- 处理一个文件有而另一个文件没有的代码的情况
- 将差异输出到文件

### 使用方法

#### 程序代码使用

您可以在代码中直接使用`NewSQRTraceComparator`类：

```java
import com.example.core.tool.NewSQRTraceComparator;

import java.io.IOException;
import java.util.List;

public class Example {
    public static void main(String[] args) throws IOException {
        // 解析跟踪文件
        List<NewSQRTraceComparator.TraceEntry> file1Entries =
                NewSQRTraceComparator.parseTrace("path/to/file1.log");
        List<NewSQRTraceComparator.TraceEntry> file2Entries =
                NewSQRTraceComparator.parseTrace("path/to/file2.log");

        // 比较跟踪，阈值为2.0
        NewSQRTraceComparator.compareTraces(
                file1Entries,
                file2Entries,
                2.0,
                "output.txt"
        );
    }
}
```

### 输出格式

输出文件是一个Markdown格式的文本文件，包含：

1. 带有比较阈值的标题
2. 检测到的时间差异部分
3. 在任一文件中发现的额外代码部分
4. 不匹配行的部分

输出示例：

```
# SQR跟踪比较结果
阈值：2.0倍

## 检测到时间差异
行内容：执行SQL (10:01:00):
文件1行：3，执行时间：36060000毫秒
文件2行：3，执行时间：72120000毫秒
比率：2.00倍

## 文件2中的额外代码
开始于行：6
行6：执行SQL (10:01:30):
行7：SELECT * FROM DEPARTMENTS
行8：执行时间: 1.5秒
```

### 跟踪文件格式要求

该工具设计用于处理使用`-S -TIMING -debugfgt -E`等跟踪参数的Oracle PeopleSoft SQR跟踪文件。它会在每行中查找格式为`(HH:MM:SS)`的时间信息。

### 故障排除

如果遇到问题：

1. 验证跟踪文件是否存在且可访问
2. 检查跟踪文件是否包含预期格式的时间信息
3. 确保阈值是有效的数字
4. 检查输出目录是否存在且可写入