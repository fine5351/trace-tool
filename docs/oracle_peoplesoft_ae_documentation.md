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

应用引擎跟踪文件包含各种操作的时间戳条目。不同的跟踪级别和参数会产生不同详细程度的输出。以下是各种跟踪输出的示例：

#### 程序启动和结束

基本的程序启动和结束信息（跟踪级别1）：

```
PSAPPSRV.9016 (1) [2023-05-15T14:25:30.000000] 开始执行应用引擎程序 PROCESSNAME.SECTION
...
PSAPPSRV.9016 (1) [2023-05-15T14:32:45.000000] 应用引擎程序 PROCESSNAME.SECTION 执行完成
总运行时间: 00:07:15
```

#### 步骤执行跟踪

应用引擎步骤执行跟踪（跟踪级别2）：

```
PSAPPSRV.9016 (1) [2023-05-15T14:25:30.500000] 开始执行步骤: PROCESSNAME.SECTION.STEP01
PSAPPSRV.9016 (1) [2023-05-15T14:25:32.200000] 步骤执行完成: PROCESSNAME.SECTION.STEP01
步骤执行时间: 1.7秒

PSAPPSRV.9016 (1) [2023-05-15T14:25:32.300000] 开始执行步骤: PROCESSNAME.SECTION.STEP02
PSAPPSRV.9016 (1) [2023-05-15T14:25:35.800000] 步骤执行完成: PROCESSNAME.SECTION.STEP02
步骤执行时间: 3.5秒

PSAPPSRV.9016 (1) [2023-05-15T14:25:35.900000] 开始执行步骤: PROCESSNAME.SECTION.STEP03
PSAPPSRV.9016 (1) [2023-05-15T14:25:40.100000] 步骤执行完成: PROCESSNAME.SECTION.STEP03
步骤执行时间: 4.2秒
```

#### SQL执行

基本SQL执行信息（使用`-TRACE 2`或`-TOOLSTRACESQL 1`）：

```
PSAPPSRV.9016 (1) [2023-05-15T14:25:31.000000] 执行SQL:
SELECT FIELD1, FIELD2 FROM PS_EXAMPLE_TABLE WHERE FIELD3 = 'VALUE'
返回行数: 150
执行时间: 1.25秒
```

详细SQL执行信息（使用`-TOOLSTRACESQL 3`）：

```
PSAPPSRV.9016 (1) [2023-05-15T14:25:31.000000] 执行SQL:
SELECT FIELD1, FIELD2 FROM PS_EXAMPLE_TABLE WHERE FIELD3 = 'VALUE'
绑定变量:
  :1 = 'VALUE'
执行计划:
  TABLE ACCESS BY INDEX ROWID PS_EXAMPLE_TABLE
    INDEX RANGE SCAN PS_EXAMPLE_TABLE_IDX1
返回行数: 150
执行时间: 1.25秒
```

SQL获取数据示例（使用`-TOOLSTRACESQL 8`）：

```
PSAPPSRV.9016 (1) [2023-05-15T14:25:31.500000] 获取数据:
行 1: FIELD1='ABC', FIELD2='数据1'
行 2: FIELD1='DEF', FIELD2='数据2'
行 3: FIELD1='GHI', FIELD2='数据3'
...（更多行）
```

#### PeopleCode执行

基本PeopleCode执行信息（使用`-TRACE 2`或`-TOOLSTRACEPC 1`）：

```
PSAPPSRV.9016 (1) [2023-05-15T14:26:05.000000] 开始执行PeopleCode: RECORD.FIELD FieldFormula
...
PSAPPSRV.9016 (1) [2023-05-15T14:26:15.000000] PeopleCode执行完成: RECORD.FIELD FieldFormula
执行时间: 10秒
```

详细PeopleCode执行信息（使用`-TOOLSTRACEPC 7`）：

```
PSAPPSRV.9016 (1) [2023-05-15T14:26:05.000000] 开始执行PeopleCode: RECORD.FIELD FieldFormula
PSAPPSRV.9016 (1) [2023-05-15T14:26:05.010000] 语句: &Result = 0;
PSAPPSRV.9016 (1) [2023-05-15T14:26:05.020000] 函数调用: GetRecord("RECORD")
PSAPPSRV.9016 (1) [2023-05-15T14:26:05.030000] 函数返回: [Record:RECORD]
PSAPPSRV.9016 (1) [2023-05-15T14:26:05.040000] 变量赋值: &MyRecord = [Record:RECORD]
PSAPPSRV.9016 (1) [2023-05-15T14:26:05.050000] 函数调用: &MyRecord.GetField("FIELD")
PSAPPSRV.9016 (1) [2023-05-15T14:26:05.060000] 函数返回: [Field:FIELD]
PSAPPSRV.9016 (1) [2023-05-15T14:26:05.070000] 变量赋值: &MyField = [Field:FIELD]
PSAPPSRV.9016 (1) [2023-05-15T14:26:05.080000] 函数调用: &MyField.Value
PSAPPSRV.9016 (1) [2023-05-15T14:26:05.090000] 函数返回: "测试值"
PSAPPSRV.9016 (1) [2023-05-15T14:26:05.100000] 条件判断: If &MyField.Value = "测试值" Then
PSAPPSRV.9016 (1) [2023-05-15T14:26:05.110000] 条件结果: True
PSAPPSRV.9016 (1) [2023-05-15T14:26:05.120000] 语句: &Result = 1;
PSAPPSRV.9016 (1) [2023-05-15T14:26:05.130000] 变量赋值: &Result = 1
...
PSAPPSRV.9016 (1) [2023-05-15T14:26:15.000000] PeopleCode执行完成: RECORD.FIELD FieldFormula
执行时间: 10秒
```

#### 错误和警告

错误信息示例：

```
PSAPPSRV.9016 (1) [2023-05-15T14:26:10.000000] 错误: SQL执行失败
ORA-00942: 表或视图不存在
SQL: SELECT * FROM PS_NONEXISTENT_TABLE
```

警告信息示例：

```
PSAPPSRV.9016 (1) [2023-05-15T14:26:12.000000] 警告: 记录未找到
记录: PS_EMPLOYEE_TBL
条件: WHERE EMPLID = '12345'
```

#### 完整跟踪示例

以下是一个使用`-TRACE 3 -TOOLSTRACESQL 7 -TOOLSTRACEPC 7`参数的更完整跟踪示例片段：

```
PSAPPSRV.9016 (1) [2023-05-15T14:25:30.000000] 开始执行应用引擎程序 HR_CALC_PAYROLL.MAIN
PSAPPSRV.9016 (1) [2023-05-15T14:25:30.100000] 应用引擎参数:
  RUN_CONTROL_ID = 'PAYROLL_MAY2023'
  PROCESS_INSTANCE = 1042567
  OPRID = 'PS'

PSAPPSRV.9016 (1) [2023-05-15T14:25:30.500000] 开始执行步骤: HR_CALC_PAYROLL.MAIN.INIT
PSAPPSRV.9016 (1) [2023-05-15T14:25:30.510000] 执行SQL:
SELECT RUN_CNTL_ID, BUSINESS_UNIT, PAY_END_DT
FROM PS_PAY_RUNCTL
WHERE RUN_CNTL_ID = :1
AND OPRID = :2
绑定变量:
  :1 = 'PAYROLL_MAY2023'
  :2 = 'PS'
返回行数: 1
执行时间: 0.05秒

PSAPPSRV.9016 (1) [2023-05-15T14:25:30.600000] 开始执行PeopleCode: PAY_RUNCTL.PAY_END_DT FieldChange
PSAPPSRV.9016 (1) [2023-05-15T14:25:30.610000] 变量赋值: &PayEndDate = "2023-05-31"
PSAPPSRV.9016 (1) [2023-05-15T14:25:30.620000] 函数调用: DateValue(&PayEndDate)
PSAPPSRV.9016 (1) [2023-05-15T14:25:30.630000] 函数返回: 2023-05-31
PSAPPSRV.9016 (1) [2023-05-15T14:25:30.640000] 变量赋值: &PayEndDt = 2023-05-31
PSAPPSRV.9016 (1) [2023-05-15T14:25:30.700000] PeopleCode执行完成: PAY_RUNCTL.PAY_END_DT FieldChange
执行时间: 0.1秒

PSAPPSRV.9016 (1) [2023-05-15T14:25:30.800000] 步骤执行完成: HR_CALC_PAYROLL.MAIN.INIT
步骤执行时间: 0.3秒

PSAPPSRV.9016 (1) [2023-05-15T14:25:30.900000] 开始执行步骤: HR_CALC_PAYROLL.MAIN.LOAD_EMPLOYEES
PSAPPSRV.9016 (1) [2023-05-15T14:25:30.910000] 执行SQL:
INSERT INTO PS_PAY_CALC_TMP (PROCESS_INSTANCE, EMPLID, NAME, DEPTID, JOBCODE, ANNUAL_RT, HOURLY_RT)
SELECT :1, E.EMPLID, E.NAME, J.DEPTID, J.JOBCODE, J.ANNUAL_RT, J.HOURLY_RT
FROM PS_PERSONAL_DATA E, PS_JOB J
WHERE E.EMPLID = J.EMPLID
AND J.EFFDT = (SELECT MAX(J1.EFFDT) FROM PS_JOB J1
               WHERE J1.EMPLID = J.EMPLID
               AND J1.EFFDT <= :2)
AND J.EMPL_STATUS = 'A'
AND J.BUSINESS_UNIT = :3
绑定变量:
  :1 = 1042567
  :2 = '2023-05-31'
  :3 = 'CORP01'
执行计划:
  INSERT STATEMENT
    HASH JOIN
      TABLE ACCESS FULL PS_PERSONAL_DATA
      FILTER
        TABLE ACCESS BY INDEX ROWID PS_JOB
          INDEX RANGE SCAN PS_JOB_EMPLID
        SORT AGGREGATE
          TABLE ACCESS BY INDEX ROWID PS_JOB
            INDEX RANGE SCAN PS_JOB_EMPLID
插入行数: 1250
执行时间: 3.75秒

PSAPPSRV.9016 (1) [2023-05-15T14:25:34.700000] 步骤执行完成: HR_CALC_PAYROLL.MAIN.LOAD_EMPLOYEES
步骤执行时间: 3.8秒

...（更多步骤）

PSAPPSRV.9016 (1) [2023-05-15T14:32:45.000000] 应用引擎程序 HR_CALC_PAYROLL.MAIN 执行完成
总运行时间: 00:07:15
```

通过分析这些跟踪文件，您可以识别性能瓶颈、错误来源和程序执行流程，从而更有效地排查问题和优化应用引擎程序。

### 跟踪的最佳实践

以下是一些应用引擎跟踪的最佳实践，包括具体示例和应用场景：

1. **使用适当的跟踪级别**
    - 级别1：基本信息，适合一般问题排查
    - 级别2：包含SQL语句，适合数据库相关问题
    - 级别3-4：详细信息，但会创建大文件并可能影响性能

   **应用示例**：
    ```
    # 场景1：初步排查应用引擎程序执行流程问题
    # 使用基本跟踪级别，获取程序执行的主要步骤和时间
    -TRACE 1

    # 场景2：排查SQL相关问题
    # 使用中等跟踪级别，获取SQL语句和执行时间
    -TRACE 2

    # 场景3：深入分析性能问题
    # 使用详细跟踪级别，获取完整的执行信息
    -TRACE 4
    ```

   **实际应用**：对于一个运行缓慢的薪资计算程序，您可以先使用`-TRACE 2`识别耗时的SQL语句，然后针对特定步骤使用`-TRACE 4`进行深入分析。

2. **结合使用多个跟踪参数**
    - 对于SQL问题，组合使用`-TRACE`和`-TOOLSTRACESQL`
    - 对于PeopleCode问题，组合使用`-TRACE`和`-TOOLSTRACEPC`
    - 对于全面分析，使用`-TRACE 3 -TOOLSTRACESQL 31 -TOOLSTRACEPC 31`

   **应用示例**：
    ```
    # 场景1：排查SQL性能问题
    # 获取SQL语句、绑定变量和执行计划
    -TRACE 2 -TOOLSTRACESQL 7

    # 场景2：排查PeopleCode逻辑问题
    # 获取PeopleCode执行流程、变量赋值和函数调用
    -TRACE 2 -TOOLSTRACEPC 15

    # 场景3：全面分析复杂问题
    # 获取所有详细信息，包括SQL和PeopleCode
    -TRACE 3 -TOOLSTRACESQL 31 -TOOLSTRACEPC 31
    ```

   **实际应用**：当您怀疑某个应用引擎步骤中的PeopleCode逻辑有问题时，可以使用`-TRACE 2 -TOOLSTRACEPC 15`跟踪变量值和条件判断，以验证程序逻辑是否按预期执行。

3. **管理跟踪文件大小**
    - 对于大型程序，考虑仅跟踪特定部分
    - 使用条件跟踪（在特定步骤中启用/禁用跟踪）
    - 定期清理旧的跟踪文件

   **应用示例**：
    ```
    # 在PeopleCode中实现条件跟踪
    If &ProblemSection Then
       &OldTraceLevel = GetTraceLevel();
       SetTraceLevel(4);  /* 提高跟踪级别 */

       /* 执行可能有问题的代码 */
       ...

       SetTraceLevel(&OldTraceLevel);  /* 恢复原始跟踪级别 */
    End-If;

    # 使用操作系统脚本定期清理跟踪文件
    # Windows批处理示例
    forfiles /p "C:\temp\traces" /s /m *.trc /d -30 /c "cmd /c del @path"

    # Unix/Linux shell示例
    find /tmp/traces -name "*.trc" -type f -mtime +30 -delete
    ```

   **实际应用**：对于一个包含多个部分的大型应用引擎程序，如果您只关注其中的薪资计算部分，可以在该部分开始前提高跟踪级别，结束后恢复，从而减少跟踪文件大小。

4. **在代表性数据下分析**
    - 使用与生产环境类似的数据量
    - 考虑数据分布对性能的影响

   **应用示例**：
    ```
    # 创建代表性测试数据的SQL脚本
    INSERT INTO PS_TEST_DATA
    SELECT * FROM PS_PRODUCTION_DATA
    WHERE BUSINESS_UNIT = 'CORP01'
    AND DEPTID IN ('IT', 'HR', 'FIN')  /* 选择代表性部门 */
    AND EFFDT BETWEEN '2023-01-01' AND '2023-06-30';  /* 选择最近数据 */

    # 使用数据分析查询了解数据分布
    SELECT DEPTID, COUNT(*) 
    FROM PS_JOB 
    GROUP BY DEPTID 
    ORDER BY COUNT(*) DESC;
    ```

   **实际应用**：在测试环境中排查性能问题时，确保测试数据不仅在数量上接近生产环境，而且在分布上也要类似。例如，如果生产环境中某个部门的员工数量特别多，测试环境中也应该反映这一特点。

5. **使用跟踪结果进行比较分析**
   - 比较问题环境和正常环境的跟踪结果
   - 比较代码更改前后的性能差异
   - 使用跟踪比较工具自动识别差异

   **应用示例**：
    ```
    # 使用跟踪比较器比较两个环境的跟踪文件
    java -jar trace-comparator.jar \
      --file1 prod_trace.log \
      --file2 test_trace.log \
      --threshold 2.0 \
      --output comparison_report.txt

    # 在代码更改前后使用相同参数运行跟踪
    # 更改前
    -TRACE 3 -TOOLSTRACESQL 7 -TOOLSTRACEPC 0

    # 更改后（使用相同参数）
    -TRACE 3 -TOOLSTRACESQL 7 -TOOLSTRACEPC 0
    ```

   **实际应用**：当您对应用引擎程序进行优化后，可以比较优化前后的跟踪文件，确认SQL执行时间是否如预期减少，以及是否有任何意外的副作用。

通过应用这些最佳实践，您可以更有效地使用应用引擎跟踪功能，快速识别和解决问题，同时最小化对系统性能的影响。

### 常见问题排查

以下是应用引擎程序中常见问题的排查方法和实际案例：

#### 性能问题

1. **应用引擎程序运行缓慢**
    - 使用`-TOOLSTRACESQL`参数识别耗时的SQL语句
    - 检查是否有不必要的循环或重复查询
    - 验证索引使用情况

   **排查示例**：
    ```
    # 启用SQL跟踪
    -TRACE 2 -TOOLSTRACESQL 7

    # 跟踪文件中识别耗时SQL示例
    PSAPPSRV.9016 (1) [2023-05-15T14:25:30.910000] 执行SQL:
    SELECT A.EMPLID, A.NAME, B.DEPTID, B.JOBCODE
    FROM PS_PERSONAL_DATA A, PS_JOB B
    WHERE A.EMPLID = B.EMPLID
    AND B.EFFDT = (SELECT MAX(EFFDT) FROM PS_JOB 
                  WHERE EMPLID = B.EMPLID AND EFFDT <= SYSDATE)
    执行时间: 45.32秒

    # 优化SQL示例
    SELECT A.EMPLID, A.NAME, B.DEPTID, B.JOBCODE
    FROM PS_PERSONAL_DATA A
    JOIN (SELECT EMPLID, DEPTID, JOBCODE, ROW_NUMBER() 
          OVER (PARTITION BY EMPLID ORDER BY EFFDT DESC) AS RN
          FROM PS_JOB
          WHERE EFFDT <= SYSDATE) B
    ON A.EMPLID = B.EMPLID AND B.RN = 1
    ```

   **实际案例**：某公司的月末财务报表应用引擎程序运行时间从4小时增加到12小时。通过启用SQL跟踪，发现一个查询账户余额的SQL语句执行时间超过3小时。进一步分析发现，该SQL使用了子查询且缺少适当的索引。添加索引并重写SQL后，整个程序运行时间减少到2小时。

2. **内存使用过高**
    - 减少大型数组的使用
    - 检查是否有内存泄漏（未释放的资源）
    - 考虑分批处理大量数据

   **排查示例**：
    ```
    # 使用PeopleCode跟踪内存使用
    -TRACE 3 -TOOLSTRACEPC 15

    # 跟踪文件中识别内存问题示例
    PSAPPSRV.9016 (1) [2023-05-15T14:30:15.000000] 变量赋值: &AllEmployees = CreateArray()
    PSAPPSRV.9016 (1) [2023-05-15T14:30:20.000000] 警告: 内存使用量增加到 1.2GB

    # 优化代码示例 - 分批处理
    Local number &BatchSize = 1000;
    Local number &TotalBatches = &TotalCount / &BatchSize + 1;

    For &Batch = 1 To &TotalBatches
       Local number &StartRow = (&Batch - 1) * &BatchSize + 1;
       Local number &EndRow = &Batch * &BatchSize;

       /* 处理当前批次 */
       ProcessBatch(&StartRow, &EndRow);

       /* 清理内存 */
       &TempData = Null;
    End-For;
    ```

   **实际案例**：一个处理员工数据的应用引擎程序在处理大型组织的数据时崩溃。通过跟踪发现，程序试图将所有50,000名员工的数据一次性加载到内存中。修改程序采用每批1,000名员工的分批处理方式后，内存使用量降低了85%，程序稳定运行。

#### 数据问题

1. **结果不正确**
    - 使用`-TOOLSTRACESQL`和`-TOOLSTRACEPC`参数查看SQL和PeopleCode执行
    - 检查变量赋值和计算逻辑
    - 验证SQL语句的WHERE条件

   **排查示例**：
    ```
    # 启用SQL和PeopleCode跟踪
    -TRACE 3 -TOOLSTRACESQL 3 -TOOLSTRACEPC 15

    # 跟踪文件中识别数据问题示例
    PSAPPSRV.9016 (1) [2023-05-15T14:40:10.000000] 执行SQL:
    SELECT SUM(EARNINGS) FROM PS_EARNINGS_TBL
    WHERE EMPLID = :1 AND EARN_TYPE IN ('REG', 'OVT')
    AND EFFDT BETWEEN :2 AND :3
    绑定变量:
      :1 = '12345'
      :2 = '2023-05-01'
      :3 = '2023-05-15'  /* 应该是月末日期 '2023-05-31' */

    # 变量赋值问题示例
    PSAPPSRV.9016 (1) [2023-05-15T14:40:05.000000] 变量赋值: &EndDate = "2023-05-15"
    /* 应该是 &EndDate = "2023-05-31" */
    ```

   **实际案例**：某公司的薪资计算程序产生的结果比预期低15%。通过跟踪发现，计算期间的结束日期错误地设置为月中（15日）而不是月末（30/31日），导致半个月的收入未被计算。修正日期逻辑后，计算结果恢复正确。

2. **缺少数据**
    - 检查JOIN条件
    - 验证数据筛选条件
    - 确认数据源是否包含预期数据

   **排查示例**：
    ```
    # 启用SQL跟踪
    -TRACE 2 -TOOLSTRACESQL 3

    # 跟踪文件中识别JOIN问题示例
    PSAPPSRV.9016 (1) [2023-05-15T14:50:20.000000] 执行SQL:
    SELECT A.EMPLID, A.NAME, B.SALARY
    FROM PS_PERSONAL_DATA A
    INNER JOIN PS_COMPENSATION B  /* 应该使用LEFT OUTER JOIN */
    ON A.EMPLID = B.EMPLID
    WHERE A.DEPTID = :1
    绑定变量:
      :1 = 'IT001'
    返回行数: 45  /* 预期应该有60行 */

    # 修正SQL示例
    SELECT A.EMPLID, A.NAME, B.SALARY
    FROM PS_PERSONAL_DATA A
    LEFT OUTER JOIN PS_COMPENSATION B
    ON A.EMPLID = B.EMPLID
    WHERE A.DEPTID = :1
    ```

   **实际案例**：一个部门报表应用引擎程序生成的报表缺少某些员工。通过跟踪发现，程序使用了INNER JOIN连接员工表和薪资表，而新入职的员工在薪资表中还没有记录，导致这些员工被排除在结果之外。将连接类型改为LEFT
   OUTER JOIN后，所有员工都包含在报表中。

#### 连接问题

1. **数据库连接失败**
    - 验证连接字符串和凭据
    - 检查网络连接
    - 确认数据库服务是否运行

   **排查示例**：
    ```
    # 跟踪文件中的连接错误示例
    PSAPPSRV.9016 (1) [2023-05-15T15:00:00.000000] 错误: 无法连接到数据库
    ORA-12541: TNS: 无法连接

    # 检查连接配置
    # 在PeopleSoft配置文件中检查数据库连接信息
    [PSDB]
    DBName=PSFT
    DBType=ORACLE
    UserId=PS
    ConnectId=people
    ConnectPswd=******
    ServerName=dbserver.example.com  /* 检查服务器名称是否正确 */
    ```

   **实际案例**：在周末维护后，所有应用引擎程序都无法启动，报告数据库连接错误。通过检查发现，数据库服务器的IP地址在维护过程中发生了变更，但PeopleSoft配置文件中的ServerName未更新。更新配置后，连接恢复正常。

2. **连接超时**
    - 检查长时间运行的SQL
    - 验证数据库资源限制
    - 考虑增加超时设置

   **排查示例**：
    ```
    # 跟踪文件中的超时错误示例
    PSAPPSRV.9016 (1) [2023-05-15T15:10:00.000000] 错误: SQL执行超时
    ORA-01013: 用户请求取消当前的操作

    # 检查之前执行的SQL
    PSAPPSRV.9016 (1) [2023-05-15T15:05:00.000000] 执行SQL:
    SELECT /*+ FULL(A) */ * FROM PS_LARGE_TABLE A
    WHERE A.SOME_FIELD = :1
    /* 没有适当的索引，导致全表扫描 */

    # 增加超时设置示例
    # 在应用服务器配置文件中
    [PSAPPSRV]
    DBFLAGS=0
    SQLEXEC_TIMEOUT=1200  /* 增加到1200秒 */
    ```

   **实际案例**：一个数据归档应用引擎程序在处理大量历史数据时经常超时。通过跟踪发现，程序执行的某个SQL查询需要超过10分钟才能完成，而默认超时设置为600秒。通过增加SQLEXEC_TIMEOUT参数值并优化SQL查询（添加适当的索引），解决了超时问题。

#### 批处理问题

1. **进程卡住或挂起**
   - 检查资源锁定和死锁
   - 验证外部依赖是否可用
   - 检查无限循环或等待条件

   **排查示例**：
    ```
    # 检查数据库锁
    SELECT l.session_id, l.oracle_username, l.os_user_name, 
           o.object_name, l.locked_mode
    FROM v$locked_object l, dba_objects o
    WHERE l.object_id = o.object_id;

    # 跟踪文件中的等待条件示例
    PSAPPSRV.9016 (1) [2023-05-15T15:20:00.000000] 开始等待外部系统响应
    ...
    /* 没有后续日志，表明程序可能在等待中挂起 */
    ```

   **实际案例**：一个与外部系统集成的应用引擎程序经常挂起。通过跟踪发现，程序在等待外部系统的响应时没有设置超时机制，当外部系统不可用时，程序会无限期等待。添加超时处理和错误恢复机制后，程序能够正常处理外部系统不可用的情况。

通过这些实际案例和排查示例，您可以更有效地识别和解决应用引擎程序中的常见问题。记住，详细的跟踪信息是问题排查的关键，选择适当的跟踪参数可以帮助您快速定位问题根源。

### 高级跟踪技巧

以下是一些高级跟踪技巧，帮助您更有效地分析和排查应用引擎程序中的复杂问题：

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

**高级条件跟踪示例**：

```
/* 基于多个条件的智能跟踪 */
Function EnableSmartTracing(&BusinessUnit As string, &ProcessType As string, &DataVolume As number)
   Local boolean &EnableDetailedTrace = False;
   Local number &TraceLevel = 1;  /* 默认基本跟踪 */

   /* 检查是否是已知的问题业务单元 */
   If &BusinessUnit = "CORP01" Or &BusinessUnit = "EMEA02" Then
      &TraceLevel = 2;  /* 提高跟踪级别 */
   End-If;

   /* 检查是否是高风险处理类型 */
   If &ProcessType = "PAYROLL" Or &ProcessType = "GL_POSTING" Then
      &TraceLevel = &TraceLevel + 1;  /* 进一步提高跟踪级别 */
   End-If;

   /* 对大数据量启用详细跟踪 */
   If &DataVolume > 10000 Then
      &EnableDetailedTrace = True;
   End-If;

   /* 保存原始跟踪级别 */
   &OldTraceLevel = GetTraceLevel();

   /* 设置新的跟踪级别 */
   SetTraceLevel(&TraceLevel);

   /* 如果需要详细跟踪，启用SQL和PeopleCode跟踪 */
   If &EnableDetailedTrace Then
      &OldSQLTrace = GetSQLTrace();
      &OldPCTrace = GetPCTrace();
      SetSQLTrace(7);  /* 启用SQL跟踪 */
      SetPCTrace(15);  /* 启用PeopleCode跟踪 */
   End-If;

   Return [&OldTraceLevel, &OldSQLTrace, &OldPCTrace, &EnableDetailedTrace];
End-Function;

/* 恢复原始跟踪设置 */
Function RestoreTracing(&TraceSettings As array)
   SetTraceLevel(&TraceSettings[1]);

   If &TraceSettings[4] Then  /* 如果启用了详细跟踪 */
      SetSQLTrace(&TraceSettings[2]);
      SetPCTrace(&TraceSettings[3]);
   End-If;
End-Function;

/* 使用示例 */
&TraceSettings = EnableSmartTracing("CORP01", "PAYROLL", 15000);

/* 执行业务逻辑 */
...

RestoreTracing(&TraceSettings);
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

**高级性能分析框架示例**：

```
/* 创建性能分析类 */
class PerformanceTracker
   property array of string &StepNames;
   property array of datetime &StartTimes;
   property array of datetime &EndTimes;
   property array of number &ElapsedTimes;
   property string &ProcessName;
   property number &TotalSteps;
   property number &CurrentStep;
   property boolean &LogToFile;
   property string &LogFilePath;

   method PerformanceTracker(&ProcessName As string, &LogToFile As boolean, &LogFilePath As string);
   method StartStep(&StepName As string);
   method EndStep();
   method GenerateReport() Returns string;
   method ExportToCSV();
   private
      method LogMessage(&Message As string);
end-class;

/* 构造函数 */
method PerformanceTracker
   /+ &ProcessName as String, +/
   /+ &LogToFile as Boolean, +/
   /+ &LogFilePath as String +/

   %This.ProcessName = &ProcessName;
   %This.LogToFile = &LogToFile;
   %This.LogFilePath = &LogFilePath;
   %This.StepNames = CreateArrayRept("", 0);
   %This.StartTimes = CreateArrayRept(Null, 0);
   %This.EndTimes = CreateArrayRept(Null, 0);
   %This.ElapsedTimes = CreateArrayRept(0, 0);
   %This.TotalSteps = 0;
   %This.CurrentStep = 0;

   %This.LogMessage("性能跟踪开始: " | %This.ProcessName | " - " | %Datetime);
end-method;

/* 开始步骤 */
method StartStep
   /+ &StepName as String +/

   %This.CurrentStep = %This.CurrentStep + 1;
   %This.TotalSteps = %This.CurrentStep;

   %This.StepNames.Push(&StepName);
   %This.StartTimes.Push(%Datetime);
   %This.EndTimes.Push(Null);
   %This.ElapsedTimes.Push(0);

   %This.LogMessage("步骤开始: " | &StepName | " - " | %Datetime);
end-method;

/* 结束步骤 */
method EndStep
   Local datetime &EndTime = %Datetime;
   Local number &StepIndex = %This.CurrentStep;

   If &StepIndex > 0 And &StepIndex <= %This.StepNames.Len() Then
      %This.EndTimes[&StepIndex] = &EndTime;
      %This.ElapsedTimes[&StepIndex] = DateTimeDiff(&EndTime, %This.StartTimes[&StepIndex]);

      %This.LogMessage("步骤结束: " | %This.StepNames[&StepIndex] | 
                      " - 耗时: " | %This.ElapsedTimes[&StepIndex] | " 秒");
   End-If;

   %This.CurrentStep = %This.CurrentStep - 1;
end-method;

/* 生成报告 */
method GenerateReport
   /+ Returns String +/

   Local string &Report = "性能分析报告: " | %This.ProcessName | Char(10) | Char(10);
   Local number &TotalTime = 0;

   &Report = &Report | "步骤名称                  耗时(秒)   百分比" | Char(10);
   &Report = &Report | "----------------------------------------" | Char(10);

   For &i = 1 To %This.TotalSteps
      &TotalTime = &TotalTime + %This.ElapsedTimes[&i];
   End-For;

   For &i = 1 To %This.TotalSteps
      Local string &StepName = %This.StepNames[&i];
      Local number &ElapsedTime = %This.ElapsedTimes[&i];
      Local number &Percentage = 0;

      If &TotalTime > 0 Then
         &Percentage = (&ElapsedTime / &TotalTime) * 100;
      End-If;

      &Report = &Report | PadRight(&StepName, 25) | " " | 
                PadLeft(Round(&ElapsedTime, 2), 10) | "   " | 
                PadLeft(Round(&Percentage, 1), 6) | "%" | Char(10);
   End-For;

   &Report = &Report | "----------------------------------------" | Char(10);
   &Report = &Report | PadRight("总计", 25) | " " | 
             PadLeft(Round(&TotalTime, 2), 10) | "  100.0%" | Char(10);

   %This.LogMessage(&Report);
   Return &Report;
end-method;

/* 导出到CSV */
method ExportToCSV
   If %This.LogToFile Then
      Local string &CSVPath = %This.LogFilePath | ".csv";
      Local File &CSVFile = GetFile(&CSVPath, "W", %FilePath_Absolute);

      If &CSVFile.IsOpen Then
         &CSVFile.WriteLine("步骤,开始时间,结束时间,耗时(秒)");

         For &i = 1 To %This.TotalSteps
            &CSVFile.WriteLine(%This.StepNames[&i] | "," | 
                             %This.StartTimes[&i] | "," | 
                             %This.EndTimes[&i] | "," | 
                             %This.ElapsedTimes[&i]);
         End-For;

         &CSVFile.Close();
         %This.LogMessage("性能数据已导出到: " | &CSVPath);
      End-If;
   End-If;
end-method;

/* 记录消息 */
private method LogMessage
   /+ &Message as String +/

   WriteToLog(0, &Message);

   If %This.LogToFile Then
      Local File &LogFile = GetFile(%This.LogFilePath, "A", %FilePath_Absolute);
      If &LogFile.IsOpen Then
         &LogFile.WriteLine(%Datetime | " - " | &Message);
         &LogFile.Close();
      End-If;
   End-If;
end-method;

/* 使用示例 */
Local PerformanceTracker &Tracker = create PerformanceTracker("月末财务处理", True, "C:\temp\perf_log.txt");

&Tracker.StartStep("初始化");
/* 初始化代码 */
&Tracker.EndStep();

&Tracker.StartStep("数据加载");
/* 数据加载代码 */
&Tracker.EndStep();

&Tracker.StartStep("计算处理");
/* 计算处理代码 */
&Tracker.EndStep();

&Tracker.StartStep("报表生成");
/* 报表生成代码 */
&Tracker.EndStep();

/* 生成并显示报告 */
MessageBox(0, "性能报告", 0, 0, &Tracker.GenerateReport());

/* 导出数据到CSV */
&Tracker.ExportToCSV();
```

#### 自动化跟踪分析

创建脚本自动分析跟踪文件，识别性能瓶颈和异常：

```python
# 跟踪文件分析脚本示例 (trace_analyzer.py)
import re
import sys
import pandas as pd
import matplotlib.pyplot as plt
from datetime import datetime

def parse_trace_file(file_path):
    """解析跟踪文件，提取SQL和步骤执行信息"""
    sql_pattern = r'\[(\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d+)\].*执行SQL:[\s\S]*?执行时间: (\d+\.\d+)秒'
    step_pattern = r'\[(\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d+)\].*开始执行步骤: ([\w\.]+)[\s\S]*?步骤执行时间: (\d+\.\d+)秒'

    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # 提取SQL执行
    sql_matches = re.findall(sql_pattern, content)
    sql_data = []
    for timestamp, duration in sql_matches:
        sql_data.append({
            'timestamp': datetime.fromisoformat(timestamp),
            'duration': float(duration),
            'type': 'SQL'
        })

    # 提取步骤执行
    step_matches = re.findall(step_pattern, content)
    step_data = []
    for timestamp, step_name, duration in step_matches:
        step_data.append({
            'timestamp': datetime.fromisoformat(timestamp),
            'step_name': step_name,
            'duration': float(duration),
            'type': 'Step'
        })

    return sql_data, step_data

def analyze_trace(file_path):
    """分析跟踪文件并生成报告"""
    sql_data, step_data = parse_trace_file(file_path)

    # 创建DataFrame
    sql_df = pd.DataFrame(sql_data)
    step_df = pd.DataFrame(step_data)

    # 分析SQL执行
    if not sql_df.empty:
        print("SQL执行分析:")
        print(f"总SQL数量: {len(sql_df)}")
        print(f"总执行时间: {sql_df['duration'].sum():.2f}秒")
        print(f"平均执行时间: {sql_df['duration'].mean():.2f}秒")
        print(f"最长执行时间: {sql_df['duration'].max():.2f}秒")

        # 识别慢SQL (超过1秒)
        slow_sql = sql_df[sql_df['duration'] > 1.0]
        if not slow_sql.empty:
            print(f"\n发现{len(slow_sql)}个慢SQL (>1秒):")
            for i, row in slow_sql.sort_values('duration', ascending=False).iterrows():
                print(f"  - 时间: {row['timestamp']}, 耗时: {row['duration']:.2f}秒")

    # 分析步骤执行
    if not step_df.empty:
        print("\n步骤执行分析:")
        print(f"总步骤数量: {len(step_df)}")
        print(f"总执行时间: {step_df['duration'].sum():.2f}秒")

        # 按步骤名称分组
        step_summary = step_df.groupby('step_name')['duration'].agg(['count', 'sum', 'mean', 'max'])
        print("\n步骤执行统计:")
        for step_name, stats in step_summary.iterrows():
            print(f"  - {step_name}: 执行{stats['count']}次, 总时间: {stats['sum']:.2f}秒, 平均: {stats['mean']:.2f}秒, 最长: {stats['max']:.2f}秒")

        # 生成图表
        plt.figure(figsize=(12, 6))
        step_summary.sort_values('sum', ascending=False).head(10)['sum'].plot(kind='bar')
        plt.title('Top 10 最耗时步骤')
        plt.ylabel('执行时间 (秒)')
        plt.tight_layout()
        plt.savefig('step_duration.png')
        print("\n已生成步骤执行时间图表: step_duration.png")

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("用法: python trace_analyzer.py <trace_file_path>")
        sys.exit(1)

    analyze_trace(sys.argv[1])
```

使用示例：

```
python trace_analyzer.py D:\traces\ae_trace_20230515.log
```

#### 高级调试技术

使用自定义调试框架进行复杂问题排查：

```
/* 创建调试上下文类 */
class DebugContext
   property string &ContextName;
   property number &DebugLevel;
   property boolean &Enabled;
   property array of any &Variables;
   property array of string &VariableNames;

   method DebugContext(&ContextName As string, &DebugLevel As number);
   method AddVariable(&Name As string, &Value As any);
   method DumpContext();
   method IsDebugEnabled(&RequiredLevel As number) Returns boolean;
end-class;

/* 构造函数 */
method DebugContext
   /+ &ContextName as String, +/
   /+ &DebugLevel as Number +/

   %This.ContextName = &ContextName;
   %This.DebugLevel = &DebugLevel;
   %This.Enabled = (&DebugLevel > 0);
   %This.Variables = CreateArrayRept("", 0);
   %This.VariableNames = CreateArrayRept("", 0);
end-method;

/* 添加变量 */
method AddVariable
   /+ &Name as String, +/
   /+ &Value as Any +/

   If %This.Enabled Then
      %This.VariableNames.Push(&Name);
      %This.Variables.Push(&Value);
   End-If;
end-method;

/* 输出上下文 */
method DumpContext
   If %This.Enabled Then
      Local string &Output = "调试上下文: " | %This.ContextName | Char(10);
      &Output = &Output | "调试级别: " | %This.DebugLevel | Char(10);
      &Output = &Output | "变量:" | Char(10);

      For &i = 1 To %This.VariableNames.Len()
         Local string &Name = %This.VariableNames[&i];
         Local any &Value = %This.Variables[&i];
         Local string &ValueStr;

         /* 处理不同类型的值 */
         If None(&Value) Then
            &ValueStr = "(null)";
         Else If IsArray(&Value) Then
            &ValueStr = "(Array, 长度: " | &Value.Len() | ")";
         Else
            &ValueStr = String(&Value);
         End-If;

         &Output = &Output | "  " | &Name | " = " | &ValueStr | Char(10);
      End-For;

      WriteToLog(0, &Output);

      /* 如果调试级别高，还可以写入文件 */
      If %This.DebugLevel >= 3 Then
         Local string &FileName = "DEBUG_" | %This.ContextName | "_" | 
                                 LTrim(RTrim(String(%Datetime, "YYYY-MM-DD-HH.MM.SS"))) | ".log";
         Local File &DebugFile = GetFile(&FileName, "W", %FilePath_Absolute);
         If &DebugFile.IsOpen Then
            &DebugFile.WriteLine(&Output);
            &DebugFile.Close();
         End-If;
      End-If;
   End-If;
end-method;

/* 检查是否启用特定级别的调试 */
method IsDebugEnabled
   /+ &RequiredLevel as Number +/
   /+ Returns Boolean +/

   Return %This.Enabled And %This.DebugLevel >= &RequiredLevel;
end-method;

/* 使用示例 */
Local DebugContext &Debug = create DebugContext("薪资计算", 3);

/* 添加上下文变量 */
&Debug.AddVariable("员工ID", &EMPLID);
&Debug.AddVariable("计算日期", &CalcDate);
&Debug.AddVariable("薪资类型", &PayType);

/* 执行业务逻辑 */
If &Debug.IsDebugEnabled(2) Then
   WriteToLog(0, "开始计算基本薪资...");
End-If;

/* 计算基本薪资 */
&BasePay = CalculateBasePay(&EMPLID, &CalcDate);
&Debug.AddVariable("基本薪资", &BasePay);

If &Debug.IsDebugEnabled(2) Then
   WriteToLog(0, "开始计算奖金...");
End-If;

/* 计算奖金 */
&Bonus = CalculateBonus(&EMPLID, &CalcDate);
&Debug.AddVariable("奖金", &Bonus);

/* 输出完整调试上下文 */
&Debug.DumpContext();
```

通过这些高级跟踪技巧，您可以更有效地分析和排查应用引擎程序中的复杂问题，提高调试效率，并获得更深入的性能洞察。

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
