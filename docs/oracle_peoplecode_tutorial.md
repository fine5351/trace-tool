# Oracle PeopleCode 教學文件

## 目錄

1. [概述](#概述)
2. [入門級教學](#入門級教學)
    - [什麼是 PeopleCode](#什麼是-peoplecode)
    - [基本語法](#基本語法)
    - [變數與數據類型](#變數與數據類型)
    - [系統變數](#系統變數)
    - [運算符](#運算符)
    - [條件語句](#條件語句)
    - [循環語句](#循環語句)
    - [註釋](#註釋)
    - [入門練習](#入門練習)
3. [進階教學](#進階教學)
    - [內建函數](#內建函數)
    - [SQL 執行](#sql-執行)
    - [陣列與列表](#陣列與列表)
    - [物件導向程式設計](#物件導向程式設計)
    - [事件與觸發器](#事件與觸發器)
    - [應用類](#應用類)
    - [錯誤處理](#錯誤處理)
    - [進階練習](#進階練習)
4. [高級教學](#高級教學)
    - [效能調教](#效能調教)
    - [問題排查](#問題排查)
    - [最佳實踐](#最佳實踐)
    - [安全性考量](#安全性考量)
    - [整合技術](#整合技術)
    - [高級案例研究](#高級案例研究)

## 概述

本教學文件旨在幫助不同程度的學習者掌握 Oracle PeopleCode 程式語言。無論您是完全沒有程式設計經驗的初學者，還是已經了解基礎語法的進階學習者，或是想要深入了解效能調教和問題排查的高級使用者，本文檔都能為您提供所需的知識和技能。

PeopleCode 是 Oracle PeopleSoft 應用程式中使用的程式語言，用於自定義和擴展 PeopleSoft 系統的功能。通過學習 PeopleCode，您可以開發自定義應用程式、自動化業務流程、創建複雜的計算邏輯，以及與其他系統進行整合。

## 入門級教學

本節適合完全沒有程式設計經驗的初學者。我們將從最基本的概念開始，逐步建立您對 PeopleCode 的理解。

### 什麼是 PeopleCode

PeopleCode 是 Oracle PeopleSoft 應用程式中使用的專屬程式語言。它的主要用途包括：

- 驗證用戶輸入的數據
- 執行計算和業務邏輯
- 自動填充欄位值
- 控制頁面元素的顯示和行為
- 與數據庫交互
- 自動化業務流程

PeopleCode 語法類似於 Basic 和 Java，但有其獨特的特性和功能，專為 PeopleSoft 環境設計。

### 基本語法

PeopleCode 程式由一系列語句組成，每個語句通常以分號 (;) 結束。以下是一個簡單的 PeopleCode 程式示例：

```peoplecode
/* 這是一個簡單的 PeopleCode 程式 */
&姓名 = "小明";
&年齡 = 15;
&問候語 = "你好，" | &姓名 | "！你今年" | &年齡 | "歲了。";
MessageBox(0, "", 0, 0, &問候語);
```

這個程式做了以下事情：

1. 創建一個名為 `&姓名` 的變數並賦值為 "小明"
2. 創建一個名為 `&年齡` 的變數並賦值為 15
3. 創建一個名為 `&問候語` 的變數，將其他變數和文字組合起來
4. 使用 MessageBox 函數顯示問候語

### 變數與數據類型

在 PeopleCode 中，變數名稱前面必須加上 & 符號。變數不需要事先聲明，它們會在首次使用時自動創建。

#### 基本數據類型

PeopleCode 支持以下基本數據類型：

- **String（字符串）**：文本數據，用引號括起來，例如 `"你好"`
- **Number（數字）**：數值數據，例如 `42` 或 `3.14`
- **Boolean（布爾值）**：`True` 或 `False`
- **Date（日期）**：例如 `%Date("2023-10-15")`
- **Time（時間）**：例如 `%Time("14:30:00")`
- **DateTime（日期時間）**：例如 `%DateTime("2023-10-15 14:30:00")`

#### 變數示例

```peoplecode
&姓名 = "小明";                           /* 字符串 */
&年齡 = 15;                              /* 數字 */
&是學生 = True;                           /* 布爾值 */
&今天 = %Date("2023-10-15");              /* 日期 */
&現在時間 = %Time("14:30:00");             /* 時間 */
&完整時間 = %DateTime("2023-10-15 14:30:00"); /* 日期時間 */
```

### 系統變數

PeopleCode 提供了許多系統變數，這些變數以 % 符號開頭，用於獲取系統信息或執行特定功能。

#### 常用系統變數

- **%Date** - 當前日期
- **%DateTime** - 當前日期和時間
- **%Time** - 當前時間
- **%UserId** - 當前用戶的 ID
- **%UserName** - 當前用戶的名稱
- **%OperatorClass** - 當前用戶的操作員類別
- **%LanguageCode** - 當前語言代碼
- **%PrimaryLanguage** - 用戶的主要語言

#### 系統變數示例

```peoplecode
&當前用戶 = %UserName;
&當前日期 = %Date;
&問候語 = "你好，" | &當前用戶 | "！今天是 " | &當前日期;
MessageBox(0, "", 0, 0, &問候語);
```

### 運算符

PeopleCode 支持多種運算符，用於執行各種操作。

#### 算術運算符

- `+` - 加法
- `-` - 減法
- `*` - 乘法
- `/` - 除法
- `^` - 冪運算

#### 字符串運算符

- `|` - 字符串連接

#### 比較運算符

- `=` - 等於
- `<>` - 不等於
- `>` - 大於
- `<` - 小於
- `>=` - 大於或等於
- `<=` - 小於或等於

#### 邏輯運算符

- `And` - 邏輯與
- `Or` - 邏輯或
- `Not` - 邏輯非

#### 運算符示例

```peoplecode
&數字1 = 10;
&數字2 = 5;

&和 = &數字1 + &數字2;      /* 結果: 15 */
&差 = &數字1 - &數字2;      /* 結果: 5 */
&積 = &數字1 * &數字2;      /* 結果: 50 */
&商 = &數字1 / &數字2;      /* 結果: 2 */
&冪 = &數字1 ^ 2;          /* 結果: 100 */

&全名 = "小" | "明";        /* 結果: "小明" */

&是否相等 = (&數字1 = &數字2);  /* 結果: False */
&是否大於 = (&數字1 > &數字2);  /* 結果: True */

&條件1 = True;
&條件2 = False;
&結果 = &條件1 And &條件2;    /* 結果: False */
&結果 = &條件1 Or &條件2;     /* 結果: True */
&結果 = Not &條件1;          /* 結果: False */
```

### 條件語句

條件語句用於根據特定條件執行不同的代碼。

#### If-Then-Else 語句

```peoplecode
&分數 = 85;

If &分數 >= 90 Then
   &等級 = "A";
Else If &分數 >= 80 Then
   &等級 = "B";
Else If &分數 >= 70 Then
   &等級 = "C";
Else If &分數 >= 60 Then
   &等級 = "D";
Else
   &等級 = "F";
End-If;

MessageBox(0, "", 0, 0, "你的等級是: " | &等級);
```

#### Evaluate 語句

Evaluate 語句類似於其他語言中的 switch 或 case 語句。

```peoplecode
&星期 = 3;
&星期幾 = "";

Evaluate &星期
   When 1
      &星期幾 = "星期一";
      Break;
   When 2
      &星期幾 = "星期二";
      Break;
   When 3
      &星期幾 = "星期三";
      Break;
   When 4
      &星期幾 = "星期四";
      Break;
   When 5
      &星期幾 = "星期五";
      Break;
   When 6
      &星期幾 = "星期六";
      Break;
   When 7
      &星期幾 = "星期日";
      Break;
   When-Other
      &星期幾 = "無效的星期";
      Break;
End-Evaluate;

MessageBox(0, "", 0, 0, "今天是: " | &星期幾);
```

### 循環語句

循環語句用於重複執行代碼塊。

#### For 循環

```peoplecode
&總和 = 0;

For &i = 1 To 10
   &總和 = &總和 + &i;
End-For;

MessageBox(0, "", 0, 0, "1到10的總和是: " | &總和);
```

#### While 循環

```peoplecode
&計數 = 1;
&總和 = 0;

While &計數 <= 10
   &總和 = &總和 + &計數;
   &計數 = &計數 + 1;
End-While;

MessageBox(0, "", 0, 0, "1到10的總和是: " | &總和);
```

#### Repeat-Until 循環

```peoplecode
&計數 = 1;
&總和 = 0;

Repeat
   &總和 = &總和 + &計數;
   &計數 = &計數 + 1;
Until &計數 > 10;

MessageBox(0, "", 0, 0, "1到10的總和是: " | &總和);
```

### 註釋

註釋用於解釋代碼，但不會被執行。PeopleCode 支持兩種類型的註釋：

#### 單行註釋

```peoplecode
/* 這是一個單行註釋 */
&姓名 = "小明"; /* 這也是一個單行註釋 */
```

#### 多行註釋

```peoplecode
/* 這是一個
   多行
   註釋 */
&姓名 = "小明";
```

### 入門練習

以下是一些簡單的練習，幫助您鞏固所學的基礎知識：

#### 練習1：計算平均分

```peoplecode
/* 計算三個科目的平均分 */
&數學 = 85;
&英語 = 92;
&科學 = 78;

&總分 = &數學 + &英語 + &科學;
&平均分 = &總分 / 3;

MessageBox(0, "", 0, 0, "三科總分: " | &總分 | ", 平均分: " | &平均分);
```

#### 練習2：判斷奇偶數

```peoplecode
/* 判斷一個數字是奇數還是偶數 */
&數字 = 7;

If &數字 Mod 2 = 0 Then
   &結果 = "偶數";
Else
   &結果 = "奇數";
End-If;

MessageBox(0, "", 0, 0, &數字 | " 是一個 " | &結果);
```

#### 練習3：計算階乘

```peoplecode
/* 計算一個數字的階乘 */
&數字 = 5;
&階乘 = 1;

For &i = 1 To &數字
   &階乘 = &階乘 * &i;
End-For;

MessageBox(0, "", 0, 0, &數字 | " 的階乘是: " | &階乘);
```

#### 練習4：簡易薪資計算器

```peoplecode
/* 簡易薪資計算器 */
&基本薪資 = 30000;
&工作年資 = 3;
&績效評分 = 4.5;  /* 滿分 5 分 */

/* 計算年資加給 (每年加薪 3%) */
&年資加給 = &基本薪資 * (&工作年資 * 0.03);

/* 計算績效獎金 (依績效比例) */
&績效係數 = &績效評分 / 5;
&績效獎金 = &基本薪資 * 0.2 * &績效係數;

/* 計算總薪資 */
&總薪資 = &基本薪資 + &年資加給 + &績效獎金;

/* 顯示結果 */
MessageBox(0, "薪資計算結果", 0, 0, "基本薪資: $" | &基本薪資 | 
                                 %NewLine | "年資加給: $" | Round(&年資加給, 0) | 
                                 %NewLine | "績效獎金: $" | Round(&績效獎金, 0) | 
                                 %NewLine | "總薪資: $" | Round(&總薪資, 0));
```

#### 練習5：簡易通訊錄

```peoplecode
/* 簡易通訊錄 */

/* 創建聯絡人陣列 */
&聯絡人1 = CreateArray("張小明", "0912-345-678", "taipei@example.com");
&聯絡人2 = CreateArray("李小華", "0923-456-789", "taichung@example.com");
&聯絡人3 = CreateArray("王大雄", "0934-567-890", "kaohsiung@example.com");

/* 將聯絡人加入通訊錄 */
&通訊錄 = CreateArrayRept(CreateArray("", "", ""), 0);
&通訊錄.Push(&聯絡人1);
&通訊錄.Push(&聯絡人2);
&通訊錄.Push(&聯絡人3);

/* 顯示所有聯絡人 */
&訊息 = "通訊錄中的聯絡人:" | %NewLine | %NewLine;

For &i = 1 To &通訊錄.Len()
   &聯絡人 = &通訊錄[&i];
   &訊息 = &訊息 | "姓名: " | &聯絡人[1] | 
                  %NewLine | "電話: " | &聯絡人[2] | 
                  %NewLine | "Email: " | &聯絡人[3] | 
                  %NewLine | %NewLine;
End-For;

MessageBox(0, "通訊錄", 0, 0, &訊息);

/* 搜尋聯絡人 */
&搜尋名稱 = "李小華";
&找到 = False;

For &i = 1 To &通訊錄.Len()
   &聯絡人 = &通訊錄[&i];
   If &聯絡人[1] = &搜尋名稱 Then
      MessageBox(0, "搜尋結果", 0, 0, "找到聯絡人:" | 
                                    %NewLine | "姓名: " | &聯絡人[1] | 
                                    %NewLine | "電話: " | &聯絡人[2] | 
                                    %NewLine | "Email: " | &聯絡人[3]);
      &找到 = True;
      break;
   End-If;
End-For;

If Not &找到 Then
   MessageBox(0, "搜尋結果", 0, 0, "找不到聯絡人: " | &搜尋名稱);
End-If;
```

## 進階教學

本節適合已經了解基礎語法的學習者。我們將探討更複雜的概念，如內建函數、SQL 執行、物件導向程式設計等。

### 內建函數

PeopleCode 提供了許多內建函數，用於執行各種操作。以下是一些常用的內建函數：

#### 字符串函數

- **Len(string)** - 返回字符串的長度
- **Substring(string, start, length)** - 從字符串中提取子字符串
- **Upper(string)** - 將字符串轉換為大寫
- **Lower(string)** - 將字符串轉換為小寫
- **Trim(string)** - 移除字符串前後的空格

```peoplecode
&姓名 = "  小明  ";
&姓名長度 = Len(&姓名);                /* 結果: 6 */
&姓名首字 = Substring(&姓名, 3, 1);     /* 結果: "小" */
&姓名大寫 = Upper(&姓名);              /* 結果: "  小明  " (中文不變) */
&姓名修剪 = Trim(&姓名);               /* 結果: "小明" */
```

#### 數學函數

- **Abs(number)** - 返回數字的絕對值
- **Round(number, decimals)** - 將數字四捨五入到指定的小數位
- **Int(number)** - 返回數字的整數部分
- **Mod(number1, number2)** - 返回 number1 除以 number2 的餘數
- **Sqrt(number)** - 返回數字的平方根

```peoplecode
&數字 = -7.89;
&絕對值 = Abs(&數字);                /* 結果: 7.89 */
&四捨五入 = Round(&數字, 1);          /* 結果: -7.9 */
&整數部分 = Int(&數字);               /* 結果: -7 */
&餘數 = Mod(10, 3);                 /* 結果: 1 */
&平方根 = Sqrt(16);                 /* 結果: 4 */
```

#### 日期和時間函數

- **DateTimeToLocalDateTime(datetime)** - 將日期時間轉換為本地日期時間
- **DateTimeToTimeZone(datetime, timezone)** - 將日期時間轉換為指定時區
- **DateTimeToSeconds(datetime)** - 將日期時間轉換為秒數
- **SecondsToDateTime(seconds)** - 將秒數轉換為日期時間
- **DateDiff(unit, date1, date2)** - 計算兩個日期之間的差異

```peoplecode
&現在 = %DateTime;
&本地時間 = DateTimeToLocalDateTime(&現在);
&紐約時間 = DateTimeToTimeZone(&現在, "America/New_York");
&秒數 = DateTimeToSeconds(&現在);
&一小時後 = SecondsToDateTime(&秒數 + 3600);
&天數差異 = DateDiff("day", %Date, %Date + 7);  /* 結果: 7 */
```

### SQL 執行

PeopleCode 允許您執行 SQL 語句來查詢和操作數據庫。

#### 基本 SQL 查詢

```peoplecode
&sql = CreateSQL("SELECT FIRST_NAME, LAST_NAME FROM PS_PERSONAL_DATA WHERE EMPLID = :1", "00001");
&firstName = "";
&lastName = "";

If &sql.Execute(&firstName, &lastName) Then
   MessageBox(0, "", 0, 0, "姓名: " | &firstName | " " | &lastName);
Else
   MessageBox(0, "", 0, 0, "查詢失敗");
End-If;
```

#### 使用 SQL 對象

```peoplecode
&sql = CreateSQL("SELECT COUNT(*) FROM PS_JOB WHERE DEPTID = :1", "HR");
&count = 0;

If &sql.Execute(&count) Then
   MessageBox(0, "", 0, 0, "HR 部門的員工數量: " | &count);
Else
   MessageBox(0, "", 0, 0, "查詢失敗");
End-If;
```

#### 插入和更新數據

```peoplecode
/* 插入數據 */
&sql = CreateSQL("INSERT INTO PS_CUSTOM_TABLE (EMPLID, NAME, DEPARTMENT) VALUES (:1, :2, :3)", "00002", "小明", "IT");
&sql.Execute();

/* 更新數據 */
&sql = CreateSQL("UPDATE PS_CUSTOM_TABLE SET DEPARTMENT = :1 WHERE EMPLID = :2", "HR", "00002");
&sql.Execute();
```

### 陣列與列表

PeopleCode 支持陣列和列表，用於存儲和操作多個值。

#### 陣列

```peoplecode
/* 創建陣列 */
Local array of string &姓名陣列;
&姓名陣列 = CreateArrayRept("", 0);

/* 添加元素 */
&姓名陣列.Push("小明");
&姓名陣列.Push("小紅");
&姓名陣列.Push("小剛");

/* 訪問元素 */
&第一個姓名 = &姓名陣列[1];  /* 注意：PeopleCode 陣列索引從 1 開始 */

/* 遍歷陣列 */
For &i = 1 To &姓名陣列.Len()
   MessageBox(0, "", 0, 0, "姓名 " | &i | ": " | &姓名陣列[&i]);
End-For;
```

#### 列表

```peoplecode
/* 創建列表 */
Local JavaList &學生列表 = CreateJavaList();

/* 添加元素 */
&學生列表.add("小明");
&學生列表.add("小紅");
&學生列表.add("小剛");

/* 訪問元素 */
&第一個學生 = &學生列表.get(0);  /* 注意：JavaList 索引從 0 開始 */

/* 遍歷列表 */
Local number &大小 = &學生列表.size();
For &i = 0 To &大小 - 1
   MessageBox(0, "", 0, 0, "學生 " | (&i + 1) | ": " | &學生列表.get(&i));
End-For;
```

### 物件導向程式設計

PeopleCode 支持物件導向程式設計，允許您創建和使用類和對象。

#### 使用內置對象

```peoplecode
/* 使用 Record 對象 */
&個人記錄 = CreateRecord(Record.PERSONAL_DATA);
&個人記錄.EMPLID.Value = "00001";
&個人記錄.SelectByKey();

&姓名 = &個人記錄.NAME.Value;
MessageBox(0, "", 0, 0, "姓名: " | &姓名);

/* 使用 Row 對象 */
&行 = CreateRowset(Record.JOB);
&行.Fill("WHERE EMPLID = :1", "00001");

If &行.ActiveRowCount > 0 Then
   &部門 = &行.GetRow(1).JOB.DEPTID.Value;
   MessageBox(0, "", 0, 0, "部門: " | &部門);
End-If;
```

### 事件與觸發器

PeopleCode 可以附加到各種事件和觸發器，以響應用戶操作或系統事件。

#### 常見事件

- **FieldChange** - 當欄位值改變時觸發
- **RowInit** - 當行初始化時觸發
- **SavePreChange** - 在保存前觸發
- **SavePostChange** - 在保存後觸發
- **SearchInit** - 當搜索頁面初始化時觸發
- **SearchSave** - 當搜索條件保存時觸發

#### 事件示例

```peoplecode
/* FieldChange 事件示例 */
/* 當 DEPARTMENT_TBL.DEPTID 欄位值改變時執行 */
If DEPTID.Value <> "" Then
   DESCR.Enabled = True;
Else
   DESCR.Enabled = False;
End-If;

/* SavePreChange 事件示例 */
/* 在保存前驗證數據 */
If SALARY.Value < 0 Then
   Error MsgGet(20000, 30, "薪資不能為負數");
   Return False;
End-If;
Return True;
```

### 應用類

應用類是 PeopleCode 中的自定義類，用於創建可重用的代碼。

#### 定義應用類

```peoplecode
class 計算器
   method 計算器();
   method 加法(&數字1 As number, &數字2 As number) Returns number;
   method 減法(&數字1 As number, &數字2 As number) Returns number;
   method 乘法(&數字1 As number, &數字2 As number) Returns number;
   method 除法(&數字1 As number, &數字2 As number) Returns number;
private
   instance number &結果;
end-class;

/* 構造函數 */
method 計算器
   &結果 = 0;
end-method;

/* 加法方法 */
method 加法
   &結果 = &數字1 + &數字2;
   Return &結果;
end-method;

/* 減法方法 */
method 減法
   &結果 = &數字1 - &數字2;
   Return &結果;
end-method;

/* 乘法方法 */
method 乘法
   &結果 = &數字1 * &數字2;
   Return &結果;
end-method;

/* 除法方法 */
method 除法
   If &數字2 = 0 Then
      Error "除數不能為零";
      Return 0;
   End-If;
   &結果 = &數字1 / &數字2;
   Return &結果;
end-method;
```

#### 使用應用類

```peoplecode
/* 創建計算器對象 */
Local 計算器 &我的計算器 = create 計算器();

/* 使用計算器方法 */
&加法結果 = &我的計算器.加法(10, 5);
&減法結果 = &我的計算器.減法(10, 5);
&乘法結果 = &我的計算器.乘法(10, 5);
&除法結果 = &我的計算器.除法(10, 5);

MessageBox(0, "", 0, 0, "加法結果: " | &加法結果);
MessageBox(0, "", 0, 0, "減法結果: " | &減法結果);
MessageBox(0, "", 0, 0, "乘法結果: " | &乘法結果);
MessageBox(0, "", 0, 0, "除法結果: " | &除法結果);
```

### 錯誤處理

PeopleCode 提供了多種錯誤處理機制，用於捕獲和處理運行時錯誤。

#### 使用 try-catch 塊

```peoplecode
try
   &結果 = 10 / 0;  /* 這會導致除以零錯誤 */
   MessageBox(0, "", 0, 0, "結果: " | &結果);
catch Exception &e
   MessageBox(0, "", 0, 0, "發生錯誤: " | &e.ToString());
end-try;
```

#### 使用 Error 和 Warning 函數

```peoplecode
If &薪資 < 0 Then
   Error MsgGet(20000, 30, "薪資不能為負數");
   Return False;
End-If;

If &薪資 < 3000 Then
   Warning MsgGet(20000, 31, "薪資低於最低標準");
End-If;
```

### 進階練習

以下是一些進階練習，幫助您鞏固所學的進階知識：

#### 練習1：使用 SQL 查詢員工信息

```peoplecode
/* 查詢特定部門的員工信息 */
&部門ID = "HR";
&sql = CreateSQL("SELECT EMPLID, NAME, JOB_TITLE FROM PS_EMPLOYEES WHERE DEPTID = :1", &部門ID);

&員工ID = "";
&姓名 = "";
&職稱 = "";

While &sql.Fetch(&員工ID, &姓名, &職稱)
   MessageBox(0, "", 0, 0, "員工ID: " | &員工ID | ", 姓名: " | &姓名 | ", 職稱: " | &職稱);
End-While;
```

#### 練習2：創建學生成績管理類

```peoplecode
class 學生成績管理
   method 學生成績管理();
   method 添加學生(&學生ID As string, &姓名 As string);
   method 添加成績(&學生ID As string, &科目 As string, &分數 As number);
   method 計算平均分(&學生ID As string) Returns number;
   method 獲取等級(&分數 As number) Returns string;
private
   instance array of array of string &學生數據;
   instance array of array of any &成績數據;
end-class;

/* 構造函數 */
method 學生成績管理
   &學生數據 = CreateArrayRept(CreateArrayRept("", 0), 0);
   &成績數據 = CreateArrayRept(CreateArrayRept("", 0), 0);
end-method;

/* 添加學生方法 */
method 添加學生
   Local array of string &學生 = CreateArrayRept("", 2);
   &學生[1] = &學生ID;
   &學生[2] = &姓名;
   &學生數據.Push(&學生);
end-method;

/* 添加成績方法 */
method 添加成績
   Local array of any &成績 = CreateArrayRept("", 3);
   &成績[1] = &學生ID;
   &成績[2] = &科目;
   &成績[3] = &分數;
   &成績數據.Push(&成績);
end-method;

/* 計算平均分方法 */
method 計算平均分
   Local number &總分 = 0;
   Local number &科目數 = 0;

   For &i = 1 To &成績數據.Len()
      If &成績數據[&i][1] = &學生ID Then
         &總分 = &總分 + &成績數據[&i][3];
         &科目數 = &科目數 + 1;
      End-If;
   End-For;

   If &科目數 > 0 Then
      Return &總分 / &科目數;
   Else
      Return 0;
   End-If;
end-method;

/* 獲取等級方法 */
method 獲取等級
   If &分數 >= 90 Then
      Return "A";
   Else If &分數 >= 80 Then
      Return "B";
   Else If &分數 >= 70 Then
      Return "C";
   Else If &分數 >= 60 Then
      Return "D";
   Else
      Return "F";
   End-If;
end-method;
```

#### 練習3：使用應用類管理學生成績

```peoplecode
/* 創建學生成績管理對象 */
Local 學生成績管理 &成績管理 = create 學生成績管理();

/* 添加學生 */
&成績管理.添加學生("S001", "小明");
&成績管理.添加學生("S002", "小紅");

/* 添加成績 */
&成績管理.添加成績("S001", "數學", 85);
&成績管理.添加成績("S001", "英語", 92);
&成績管理.添加成績("S001", "科學", 78);
&成績管理.添加成績("S002", "數學", 90);
&成績管理.添加成績("S002", "英語", 85);
&成績管理.添加成績("S002", "科學", 88);

/* 計算平均分並獲取等級 */
Local number &小明平均分 = &成績管理.計算平均分("S001");
Local string &小明等級 = &成績管理.獲取等級(&小明平均分);
MessageBox(0, "", 0, 0, "小明的平均分: " | &小明平均分 | ", 等級: " | &小明等級);

Local number &小紅平均分 = &成績管理.計算平均分("S002");
Local string &小紅等級 = &成績管理.獲取等級(&小紅平均分);
MessageBox(0, "", 0, 0, "小紅的平均分: " | &小紅平均分 | ", 等級: " | &小紅等級);
```

#### 練習4：員工資料管理系統

```peoplecode
/* 員工資料管理系統 - 展示進階 PeopleCode 功能 */

/* 定義員工類 */
class 員工
   property string 員工ID;
   property string 姓名;
   property string 部門;
   property string 職位;
   property number 基本薪資;
   property date 雇用日期;

   method 員工(&員工ID As string, &姓名 As string, &部門 As string, &職位 As string, &基本薪資 As number, &雇用日期 As date);
   method 計算薪資() Returns number;
   method 計算工作年資() Returns number;
   method 顯示資料();
end-class;

/* 員工類構造函數 */
method 員工
   /+ &員工ID as String, +/
   /+ &姓名 as String, +/
   /+ &部門 as String, +/
   /+ &職位 as String, +/
   /+ &基本薪資 as Number, +/
   /+ &雇用日期 as Date +/

   %This.員工ID = &員工ID;
   %This.姓名 = &姓名;
   %This.部門 = &部門;
   %This.職位 = &職位;
   %This.基本薪資 = &基本薪資;
   %This.雇用日期 = &雇用日期;
end-method;

/* 計算薪資方法 */
method 計算薪資
   /+ Returns Number +/

   Local number &工作年資 = %This.計算工作年資();
   Local number &年資加給 = %This.基本薪資 * (&工作年資 * 0.05);  /* 每年加薪5% */

   /* 部門獎金 */
   Local number &部門獎金 = 0;
   Evaluate %This.部門
      When = "IT"
         &部門獎金 = %This.基本薪資 * 0.10;
         Break;
      When = "銷售"
         &部門獎金 = %This.基本薪資 * 0.15;
         Break;
      When = "人力資源"
         &部門獎金 = %This.基本薪資 * 0.08;
         Break;
      When-Other
         &部門獎金 = %This.基本薪資 * 0.05;
         Break;
   End-Evaluate;

   Return %This.基本薪資 + &年資加給 + &部門獎金;
end-method;

/* 計算工作年資方法 */
method 計算工作年資
   /+ Returns Number +/

   Local date &今天 = %Date;
   Local number &年資 = DateDiff("year", %This.雇用日期, &今天);
   Return &年資;
end-method;

/* 顯示員工資料方法 */
method 顯示資料
   Local string &資料 = "員工ID: " | %This.員工ID | 
                      %NewLine | "姓名: " | %This.姓名 | 
                      %NewLine | "部門: " | %This.部門 | 
                      %NewLine | "職位: " | %This.職位 | 
                      %NewLine | "基本薪資: $" | %This.基本薪資 | 
                      %NewLine | "雇用日期: " | %This.雇用日期 | 
                      %NewLine | "工作年資: " | %This.計算工作年資() | " 年" | 
                      %NewLine | "實際薪資: $" | Round(%This.計算薪資(), 0);

   MessageBox(0, "員工資料", 0, 0, &資料);
end-method;

/* 定義經理類 (繼承員工類) */
class 經理 extends 員工
   property array of string &下屬;
   property number 管理獎金比例;

   method 經理(&員工ID As string, &姓名 As string, &部門 As string, &職位 As string, &基本薪資 As number, &雇用日期 As date, &管理獎金比例 As number);
   method 添加下屬(&下屬姓名 As string);
   method 計算薪資() Returns number;
   method 顯示資料();
end-class;

/* 經理類構造函數 */
method 經理
   /+ &員工ID as String, +/
   /+ &姓名 as String, +/
   /+ &部門 as String, +/
   /+ &職位 as String, +/
   /+ &基本薪資 as Number, +/
   /+ &雇用日期 as Date, +/
   /+ &管理獎金比例 as Number +/

   %Super = create 員工(&員工ID, &姓名, &部門, &職位, &基本薪資, &雇用日期);
   %This.下屬 = CreateArrayRept("", 0);
   %This.管理獎金比例 = &管理獎金比例;
end-method;

/* 添加下屬方法 */
method 添加下屬
   /+ &下屬姓名 as String +/

   %This.下屬.Push(&下屬姓名);
end-method;

/* 重寫計算薪資方法 */
method 計算薪資
   /+ Returns Number +/

   Local number &基本薪資 = %Super.計算薪資();
   Local number &管理獎金 = &基本薪資 * %This.管理獎金比例;
   Local number &下屬獎金 = %This.下屬.Len() * 1000;  /* 每個下屬增加$1000獎金 */

   Return &基本薪資 + &管理獎金 + &下屬獎金;
end-method;

/* 重寫顯示資料方法 */
method 顯示資料
   Local string &下屬列表 = "";

   If %This.下屬.Len() > 0 Then
      &下屬列表 = "下屬名單:" | %NewLine;
      For &i = 1 To %This.下屬.Len()
         &下屬列表 = &下屬列表 | "  - " | %This.下屬[&i] | %NewLine;
      End-For;
   Else
      &下屬列表 = "無下屬";
   End-If;

   Local string &資料 = "員工ID: " | %This.員工ID | 
                      %NewLine | "姓名: " | %This.姓名 | 
                      %NewLine | "部門: " | %This.部門 | 
                      %NewLine | "職位: " | %This.職位 | 
                      %NewLine | "基本薪資: $" | %This.基本薪資 | 
                      %NewLine | "雇用日期: " | %This.雇用日期 | 
                      %NewLine | "工作年資: " | %This.計算工作年資() | " 年" | 
                      %NewLine | "管理獎金比例: " | (%This.管理獎金比例 * 100) | "%" | 
                      %NewLine | "下屬數量: " | %This.下屬.Len() | 
                      %NewLine | &下屬列表 | 
                      %NewLine | "實際薪資: $" | Round(%This.計算薪資(), 0);

   MessageBox(0, "經理資料", 0, 0, &資料);
end-method;

/* 創建員工資料庫 */
Local array of 員工 &員工資料庫 = CreateArrayRept(Null, 0);

/* 創建普通員工 */
Local 員工 &員工1 = create 員工("E001", "張小明", "IT", "軟體工程師", 50000, %Date("2018-05-15"));
Local 員工 &員工2 = create 員工("E002", "李小華", "銷售", "銷售代表", 45000, %Date("2020-03-10"));
Local 員工 &員工3 = create 員工("E003", "王大雄", "人力資源", "招聘專員", 48000, %Date("2019-11-22"));

/* 創建經理 */
Local 經理 &經理1 = create 經理("M001", "陳經理", "IT", "IT經理", 80000, %Date("2015-08-01"), 0.15);
Local 經理 &經理2 = create 經理("M002", "林經理", "銷售", "銷售經理", 85000, %Date("2016-04-12"), 0.18);

/* 添加下屬 */
&經理1.添加下屬("張小明");
&經理1.添加下屬("劉小剛");
&經理1.添加下屬("趙小強");

&經理2.添加下屬("李小華");
&經理2.添加下屬("吳小敏");

/* 將員工添加到資料庫 */
&員工資料庫.Push(&員工1);
&員工資料庫.Push(&員工2);
&員工資料庫.Push(&員工3);
&員工資料庫.Push(&經理1);
&員工資料庫.Push(&經理2);

/* 顯示所有員工資料 */
For &i = 1 To &員工資料庫.Len()
   &員工資料庫[&i].顯示資料();
End-For;

/* 計算公司總薪資支出 */
Local number &總薪資 = 0;
For &i = 1 To &員工資料庫.Len()
   &總薪資 = &總薪資 + &員工資料庫[&i].計算薪資();
End-For;

MessageBox(0, "公司薪資統計", 0, 0, "員工數量: " | &員工資料庫.Len() | 
                                 %NewLine | "總薪資支出: $" | Round(&總薪資, 0) | 
                                 %NewLine | "平均薪資: $" | Round(&總薪資 / &員工資料庫.Len(), 0));
```

#### 練習5：數據庫操作與異常處理

```peoplecode
/* 數據庫操作與異常處理示例 */

/* 定義日誌函數 */
Function 寫入日誌(&訊息 As string)
   Local string &日誌文件 = "C:\temp\db_operations.log";
   Local File &文件;

   try
      &文件 = GetFile(&日誌文件, "A");
      If &文件.IsOpen Then
         &文件.WriteLine(%Datetime | " - " | &訊息);
         &文件.Close();
      End-If;
   catch Exception &e
      MessageBox(0, "錯誤", 0, 0, "無法寫入日誌: " | &e.ToString());
   end-try;
End-Function;

/* 定義數據庫操作函數 */
Function 獲取部門員工(&部門ID As string) Returns array of array of string
   Local array of array of string &結果 = CreateArrayRept(CreateArrayRept("", 0), 0);
   Local SQL &sql;
   Local string &員工ID, &姓名, &職位;
   Local number &薪資;

   try
      /* 創建SQL查詢 */
      &sql = CreateSQL("SELECT EMPLID, NAME, POSITION, SALARY FROM PS_EMPLOYEES WHERE DEPTID = :1 ORDER BY NAME", &部門ID);

      /* 執行查詢並獲取結果 */
      While &sql.Fetch(&員工ID, &姓名, &職位, &薪資)
         Local array of string &員工 = CreateArrayRept("", 4);
         &員工[1] = &員工ID;
         &員工[2] = &姓名;
         &員工[3] = &職位;
         &員工[4] = String(&薪資);
         &結果.Push(&員工);
      End-While;

      寫入日誌("成功查詢部門 " | &部門ID | " 的員工，找到 " | &結果.Len() | " 條記錄");
      Return &結果;

   catch Exception &e
      寫入日誌("查詢部門 " | &部門ID | " 的員工時出錯: " | &e.ToString());
      MessageBox(0, "數據庫錯誤", 0, 0, "查詢失敗: " | &e.ToString());
      Return &結果;
   end-try;
End-Function;

Function 更新員工薪資(&員工ID As string, &新薪資 As number) Returns boolean
   try
      /* 檢查薪資是否合理 */
      If &新薪資 < 0 Then
         throw CreateException(0, 0, "薪資不能為負數");
      End-If;

      /* 檢查員工是否存在 */
      Local SQL &檢查SQL = CreateSQL("SELECT COUNT(*) FROM PS_EMPLOYEES WHERE EMPLID = :1", &員工ID);
      Local integer &計數 = 0;
      &檢查SQL.Execute(&計數);

      If &計數 = 0 Then
         throw CreateException(0, 0, "員工ID不存在: " | &員工ID);
      End-If;

      /* 更新薪資 */
      Local SQL &更新SQL = CreateSQL("UPDATE PS_EMPLOYEES SET SALARY = :1 WHERE EMPLID = :2", &新薪資, &員工ID);
      &更新SQL.Execute();

      寫入日誌("成功更新員工 " | &員工ID | " 的薪資為 " | &新薪資);
      Return True;

   catch Exception &e
      寫入日誌("更新員工 " | &員工ID | " 的薪資時出錯: " | &e.ToString());
      MessageBox(0, "更新錯誤", 0, 0, "無法更新薪資: " | &e.ToString());
      Return False;
   end-try;
End-Function;

/* 使用示例 */
Local string &部門ID = "IT";
Local array of array of string &員工列表;

/* 獲取部門員工 */
&員工列表 = 獲取部門員工(&部門ID);

/* 顯示員工列表 */
If &員工列表.Len() > 0 Then
   Local string &訊息 = &部門ID | " 部門的員工:" | %NewLine | %NewLine;

   For &i = 1 To &員工列表.Len()
      &訊息 = &訊息 | "員工ID: " | &員工列表[&i][1] | 
                    %NewLine | "姓名: " | &員工列表[&i][2] | 
                    %NewLine | "職位: " | &員工列表[&i][3] | 
                    %NewLine | "薪資: $" | &員工列表[&i][4] | 
                    %NewLine | %NewLine;
   End-For;

   MessageBox(0, &部門ID | " 部門員工列表", 0, 0, &訊息);

   /* 嘗試更新第一個員工的薪資 */
   If &員工列表.Len() > 0 Then
      Local string &員工ID = &員工列表[1][1];
      Local number &當前薪資 = Value(&員工列表[1][4]);
      Local number &新薪資 = &當前薪資 * 1.1;  /* 加薪10% */

      If 更新員工薪資(&員工ID, &新薪資) Then
         MessageBox(0, "更新成功", 0, 0, "員工 " | &員工列表[1][2] | " 的薪資已從 $" | &當前薪資 | " 更新為 $" | &新薪資);
      End-If;
   End-If;
Else
   MessageBox(0, "查詢結果", 0, 0, &部門ID | " 部門沒有員工");
End-If;

/* 嘗試更新不存在的員工 */
更新員工薪資("INVALID_ID", 50000);

/* 嘗試設置無效的薪資 */
If &員工列表.Len() > 0 Then
   更新員工薪資(&員工列表[1][1], -1000);
End-If;
```

## 高級教學

本節適合已經熟練掌握 PeopleCode 基礎和進階知識的學習者。我們將探討效能調教、問題排查、最佳實踐等高級主題。

### 效能調教

在開發 PeopleCode 程式時，效能是一個重要的考量因素。以下是一些提高 PeopleCode 程式效能的技巧：

#### 減少數據庫訪問

```peoplecode
/* 低效的方式：在循環中多次訪問數據庫 */
For &i = 1 To &員工ID陣列.Len()
   &sql = CreateSQL("SELECT NAME FROM PS_EMPLOYEES WHERE EMPLID = :1", &員工ID陣列[&i]);
   &sql.Execute(&姓名);
   /* 處理數據... */
End-For;

/* 高效的方式：一次性獲取所有數據 */
&條件 = "";
For &i = 1 To &員工ID陣列.Len()
   If &i > 1 Then
      &條件 = &條件 | " OR ";
   End-If;
   &條件 = &條件 | "EMPLID = '" | &員工ID陣列[&i] | "'";
End-For;

&sql = CreateSQL("SELECT EMPLID, NAME FROM PS_EMPLOYEES WHERE " | &條件);
&員工ID = "";
&姓名 = "";

While &sql.Fetch(&員工ID, &姓名)
   /* 處理數據... */
End-While;
```

#### 使用緩存

```peoplecode
/* 使用全局變數緩存常用數據 */
If All(&緩存_部門列表) Then
   /* 使用緩存的數據 */
Else
   /* 從數據庫獲取數據並緩存 */
   &緩存_部門列表 = CreateArrayRept("", 0);
   &sql = CreateSQL("SELECT DEPTID, DESCR FROM PS_DEPARTMENT_TBL");
   &部門ID = "";
   &描述 = "";

   While &sql.Fetch(&部門ID, &描述)
      Local array of string &部門 = CreateArrayRept("", 2);
      &部門[1] = &部門ID;
      &部門[2] = &描述;
      &緩存_部門列表.Push(&部門);
   End-While;
End-If;
```

#### 優化循環

```peoplecode
/* 低效的循環 */
For &i = 1 To &大陣列.Len()
   For &j = 1 To &大陣列.Len()
      /* 處理數據... */
   End-For;
End-For;

/* 高效的循環 */
Local number &長度 = &大陣列.Len();  /* 避免重複計算長度 */
For &i = 1 To &長度
   For &j = 1 To &長度
      /* 處理數據... */
   End-For;
End-For;
```

#### 減少字符串連接

```peoplecode
/* 低效的字符串連接 */
&結果 = "";
For &i = 1 To 1000
   &結果 = &結果 | "項目" | &i | ", ";
End-For;

/* 高效的字符串連接 */
Local array of string &項目陣列 = CreateArrayRept("", 0);
For &i = 1 To 1000
   &項目陣列.Push("項目" | &i);
End-For;
&結果 = Join(&項目陣列, ", ");
```

### 問題排查

開發和維護 PeopleCode 程式時，您可能會遇到各種問題。以下是一些常見問題的排查技巧：

#### 使用 MessageBox 進行調試

```peoplecode
/* 在關鍵點顯示變數值 */
MessageBox(0, "", 0, 0, "變數值: " | &變數);

/* 跟踪程式執行流程 */
MessageBox(0, "", 0, 0, "進入函數 XYZ");
/* 函數代碼... */
MessageBox(0, "", 0, 0, "離開函數 XYZ");
```

#### 使用 WriteToLog 函數

```peoplecode
/* 寫入應用服務器日誌 */
WriteToLog(0, "調試信息: 變數值 = " | &變數);
WriteToLog(0, "錯誤信息: " | &錯誤消息);
```

#### 使用 SQLExec 跟踪 SQL

```peoplecode
/* 啟用 SQL 跟踪 */
SQLExec("SET TRACE ON");

/* 執行 SQL 操作... */

/* 禁用 SQL 跟踪 */
SQLExec("SET TRACE OFF");
```

#### 常見錯誤及解決方案

1. **未定義變數錯誤**
   ```peoplecode
   /* 錯誤：使用未定義的變數 */
   &結果 = &數字1 + &數字2;  /* 如果 &數字2 未定義，會出錯 */

   /* 解決方案：使用 All 函數檢查變數是否已定義 */
   If All(&數字1) And All(&數字2) Then
      &結果 = &數字1 + &數字2;
   Else
      Error "數字1 或 數字2 未定義";
   End-If;
   ```

2. **SQL 錯誤**
   ```peoplecode
   /* 錯誤：SQL 語法錯誤或表不存在 */
   &sql = CreateSQL("SELECT * FORM PS_EMPLOYEES");  /* FORM 拼寫錯誤 */

   /* 解決方案：使用 try-catch 捕獲 SQL 錯誤 */
   try
      &sql = CreateSQL("SELECT * FROM PS_EMPLOYEES");
      /* 處理結果... */
   catch Exception &e
      WriteToLog(0, "SQL 錯誤: " | &e.ToString());
      Error "執行 SQL 時出錯，請查看日誌";
   end-try;
   ```

3. **空指針錯誤**
   ```peoplecode
   /* 錯誤：訪問空對象的屬性 */
   &記錄 = CreateRecord(Record.EMPLOYEES);
   &記錄.EMPLID.Value = "00001";  /* 如果 EMPLID 字段不存在，會出錯 */

   /* 解決方案：檢查對象是否為空 */
   &記錄 = CreateRecord(Record.EMPLOYEES);
   If None(&記錄) Then
      Error "無法創建記錄對象";
      Return;
   End-If;

   If &記錄.IsFieldExists("EMPLID") Then
      &記錄.EMPLID.Value = "00001";
   Else
      Error "EMPLID 字段不存在";
   End-If;
   ```

### 最佳實踐

以下是開發 PeopleCode 程式的一些最佳實踐：

#### 代碼組織

```peoplecode
/* 使用函數組織代碼 */
Function 處理員工數據(&員工ID As string) Returns boolean
   /* 函數實現... */
   Return True;
End-Function;

/* 主程序 */
If 處理員工數據("00001") Then
   /* 成功處理... */
Else
   /* 處理失敗... */
End-If;
```

#### 錯誤處理

```peoplecode
/* 全面的錯誤處理 */
Function 處理數據(&參數 As string) Returns boolean
   try
      /* 驗證參數 */
      If None(&參數) Then
         Error "參數不能為空";
         Return False;
      End-If;

      /* 執行操作 */
      &結果 = DoSomething(&參數);

      /* 驗證結果 */
      If &結果 < 0 Then
         Error "操作返回無效結果: " | &結果;
         Return False;
      End-If;

      Return True;
   catch Exception &e
      WriteToLog(0, "處理數據時出錯: " | &e.ToString());
      Error "處理數據時出錯，請查看日誌";
      Return False;
   end-try;
End-Function;
```

#### 代碼註釋

```peoplecode
/*
 * 函數: 計算員工薪資
 * 描述: 根據員工的基本工資、獎金和扣除額計算最終薪資
 * 參數:
 *   - &員工ID: 員工的唯一標識符
 *   - &月份: 薪資月份 (YYYY-MM 格式)
 * 返回值:
 *   - 計算後的薪資金額，如果出錯則返回 -1
 */
Function 計算員工薪資(&員工ID As string, &月份 As string) Returns number
   /* 函數實現... */
   Return &薪資;
End-Function;
```

#### 變數命名

```peoplecode
/* 使用有意義的變數名稱 */
&員工總數 = 0;
&部門名稱 = "人力資源";
&是否活躍 = True;

/* 使用前綴表示變數類型 */
&str姓名 = "小明";
&num年齡 = 25;
&bool是學生 = True;
&arr學生列表 = CreateArrayRept("", 0);
&rec員工記錄 = CreateRecord(Record.EMPLOYEES);
```

### 安全性考量

在開發 PeopleCode 程式時，安全性是一個重要的考量因素。以下是一些安全性最佳實踐：

#### 防止 SQL 注入

```peoplecode
/* 不安全的方式：直接拼接 SQL */
&用戶輸入 = Request.GetParameter("EMPLID");
&sql = CreateSQL("SELECT * FROM PS_EMPLOYEES WHERE EMPLID = '" | &用戶輸入 | "'");

/* 安全的方式：使用參數化查詢 */
&用戶輸入 = Request.GetParameter("EMPLID");
&sql = CreateSQL("SELECT * FROM PS_EMPLOYEES WHERE EMPLID = :1", &用戶輸入);
```

#### 數據驗證

```peoplecode
/* 驗證用戶輸入 */
&用戶輸入 = Request.GetParameter("EMPLID");

/* 檢查是否為空 */
If None(&用戶輸入) Then
   Error "員工ID不能為空";
   Return;
End-If;

/* 檢查格式 */
If Not Regex("^\d{5}$", &用戶輸入) Then
   Error "員工ID必須是5位數字";
   Return;
End-If;

/* 檢查是否存在 */
&sql = CreateSQL("SELECT COUNT(*) FROM PS_EMPLOYEES WHERE EMPLID = :1", &用戶輸入);
&count = 0;
&sql.Execute(&count);

If &count = 0 Then
   Error "員工ID不存在";
   Return;
End-If;
```

#### 權限檢查

```peoplecode
/* 檢查用戶權限 */
If Not IsUserInRole("管理員") And Not IsUserInRole("人力資源") Then
   Error "您沒有訪問此頁面的權限";
   Return;
End-If;

/* 根據權限顯示不同內容 */
If IsUserInRole("管理員") Then
   /* 顯示管理員選項... */
Else If IsUserInRole("人力資源") Then
   /* 顯示人力資源選項... */
Else
   /* 顯示一般用戶選項... */
End-If;
```

### 整合技術

PeopleCode 可以與其他技術整合，擴展其功能。以下是一些常見的整合技術：

#### 與 Web 服務整合

```peoplecode
/* 調用 REST Web 服務 */
Local string &url = "https://api.example.com/data";
Local string &方法 = %Request_Method_GET;
Local string &響應;

/* 創建請求 */
&請求 = CreateSOAPDoc();
&請求_節點 = &請求.DocumentElement;

/* 添加請求頭 */
&請求_節點.AddAttribute("Content-Type", "application/json");
&請求_節點.AddAttribute("Authorization", "Bearer " | &訪問令牌);

/* 發送請求 */
&連接 = CreateJavaObject("com.peoplesoft.pt.net.HttpConnection");
&連接.SetRequestMethod(&方法);
&連接.SetURL(&url);

&響應 = &連接.SendRequest(&請求);

/* 處理響應 */
&響應_文檔 = CreateXmlDoc(&響應);
&數據 = &響應_文檔.DocumentElement.GetElementsByTagName("data");

For &i = 1 To &數據.Len()
   &項目 = &數據.Item(&i).NodeValue;
   /* 處理數據... */
End-For;
```

#### 與 Java 整合

```peoplecode
/* 使用 Java 類 */
&日期工具 = CreateJavaObject("java.util.Date");
&當前時間戳 = &日期工具.getTime();

&格式化器 = CreateJavaObject("java.text.SimpleDateFormat", "yyyy-MM-dd HH:mm:ss");
&格式化日期 = &格式化器.format(&日期工具);

MessageBox(0, "", 0, 0, "當前時間: " | &格式化日期);
```

#### 與文件系統整合

```peoplecode
/* 讀取文件 */
&文件 = GetFile("C:\temp\data.txt", "R");
If &文件.IsOpen Then
   &內容 = "";
   While &文件.ReadLine(&行)
      &內容 = &內容 | &行 | Char(10);
   End-While;
   &文件.Close();
   MessageBox(0, "", 0, 0, "文件內容: " | &內容);
Else
   Error "無法打開文件";
End-If;

/* 寫入文件 */
&文件 = GetFile("C:\temp\output.txt", "W");
If &文件.IsOpen Then
   &文件.WriteLine("這是第一行");
   &文件.WriteLine("這是第二行");
   &文件.Close();
   MessageBox(0, "", 0, 0, "文件已寫入");
Else
   Error "無法創建文件";
End-If;
```

### 高級案例研究

以下是一些高級案例研究，展示如何將所學知識應用到實際問題中：

#### 案例1：批量數據處理

```peoplecode
/*
 * 批量處理員工數據
 * 1. 從文件讀取員工數據
 * 2. 驗證數據
 * 3. 更新數據庫
 * 4. 生成報告
 */
Function 批量處理員工數據(&文件路徑 As string) Returns boolean
   /* 讀取文件 */
   &文件 = GetFile(&文件路徑, "R");
   If Not &文件.IsOpen Then
      Error "無法打開文件: " | &文件路徑;
      Return False;
   End-If;

   /* 準備數據結構 */
   &員工數據 = CreateArrayRept(CreateArrayRept("", 0), 0);
   &錯誤記錄 = CreateArrayRept(CreateArrayRept("", 0), 0);

   /* 讀取和解析數據 */
   &行號 = 0;
   While &文件.ReadLine(&行)
      &行號 = &行號 + 1;
      If &行號 = 1 Then
         /* 跳過標題行 */
         continue;
      End-If;

      /* 解析CSV行 */
      &欄位 = Split(&行, ",");
      If &欄位.Len() < 5 Then
         &錯誤 = CreateArrayRept("", 2);
         &錯誤[1] = &行號;
         &錯誤[2] = "欄位數量不足";
         &錯誤記錄.Push(&錯誤);
         continue;
      End-If;

      /* 提取數據 */
      &員工ID = &欄位[1];
      &姓名 = &欄位[2];
      &部門 = &欄位[3];
      &職位 = &欄位[4];
      &薪資 = Value(&欄位[5]);

      /* 驗證數據 */
      If None(&員工ID) Or Len(Trim(&員工ID)) <> 5 Then
         &錯誤 = CreateArrayRept("", 2);
         &錯誤[1] = &行號;
         &錯誤[2] = "員工ID必須是5位數字";
         &錯誤記錄.Push(&錯誤);
         continue;
      End-If;

      If &薪資 <= 0 Then
         &錯誤 = CreateArrayRept("", 2);
         &錯誤[1] = &行號;
         &錯誤[2] = "薪資必須大於零";
         &錯誤記錄.Push(&錯誤);
         continue;
      End-If;

      /* 存儲有效數據 */
      &員工 = CreateArrayRept("", 5);
      &員工[1] = &員工ID;
      &員工[2] = &姓名;
      &員工[3] = &部門;
      &員工[4] = &職位;
      &員工[5] = &薪資;
      &員工數據.Push(&員工);
   End-While;

   &文件.Close();

   /* 更新數據庫 */
   &成功計數 = 0;
   &失敗計數 = 0;

   For &i = 1 To &員工數據.Len()
      &員工 = &員工數據[&i];
      &員工ID = &員工[1];
      &姓名 = &員工[2];
      &部門 = &員工[3];
      &職位 = &員工[4];
      &薪資 = &員工[5];

      try
         /* 檢查員工是否存在 */
         &sql = CreateSQL("SELECT COUNT(*) FROM PS_EMPLOYEES WHERE EMPLID = :1", &員工ID);
         &count = 0;
         &sql.Execute(&count);

         If &count > 0 Then
            /* 更新現有員工 */
            &sql = CreateSQL("UPDATE PS_EMPLOYEES SET NAME = :1, DEPTID = :2, POSITION = :3, SALARY = :4 WHERE EMPLID = :5", &姓名, &部門, &職位, &薪資, &員工ID);
         Else
            /* 添加新員工 */
            &sql = CreateSQL("INSERT INTO PS_EMPLOYEES (EMPLID, NAME, DEPTID, POSITION, SALARY) VALUES (:1, :2, :3, :4, :5)", &員工ID, &姓名, &部門, &職位, &薪資);
         End-If;

         &sql.Execute();
         &成功計數 = &成功計數 + 1;
      catch Exception &e
         &錯誤 = CreateArrayRept("", 2);
         &錯誤[1] = &員工ID;
         &錯誤[2] = "數據庫錯誤: " | &e.ToString();
         &錯誤記錄.Push(&錯誤);
         &失敗計數 = &失敗計數 + 1;
      end-try;
   End-For;

   /* 生成報告 */
   &報告文件 = GetFile("C:\temp\處理報告.txt", "W");
   If &報告文件.IsOpen Then
      &報告文件.WriteLine("批量處理報告");
      &報告文件.WriteLine("處理時間: " | %DateTime);
      &報告文件.WriteLine("總記錄數: " | (&行號 - 1));
      &報告文件.WriteLine("有效記錄數: " | &員工數據.Len());
      &報告文件.WriteLine("成功更新數: " | &成功計數);
      &報告文件.WriteLine("失敗更新數: " | &失敗計數);
      &報告文件.WriteLine("");

      If &錯誤記錄.Len() > 0 Then
         &報告文件.WriteLine("錯誤記錄:");
         For &i = 1 To &錯誤記錄.Len()
            &錯誤 = &錯誤記錄[&i];
            &報告文件.WriteLine("  - 行號/ID: " | &錯誤[1] | ", 錯誤: " | &錯誤[2]);
         End-For;
      End-If;

      &報告文件.Close();
      MessageBox(0, "", 0, 0, "處理完成，報告已生成");
   Else
      Error "無法創建報告文件";
   End-If;

   Return (&失敗計數 = 0);
End-Function;
```

#### 案例2：複雜業務規則實現

```peoplecode
/*
 * 計算員工年終獎金
 * 獎金計算規則：
 * 1. 基本獎金 = 月薪 * 績效係數
 * 2. 績效係數：A=2.0, B=1.5, C=1.0, D=0.5, F=0
 * 3. 服務年限獎勵：每滿1年增加5%，最高50%
 * 4. 部門調整：銷售部門+10%，研發部門+5%
 * 5. 管理職位：經理+15%，總監+25%，副總裁+40%
 */
Function 計算年終獎金(&員工ID As string) Returns number
   /* 獲取員工基本信息 */
   &sql = CreateSQL("SELECT NAME, DEPTID, POSITION, SALARY, PERF_RATING, HIRE_DATE FROM PS_EMPLOYEES WHERE EMPLID = :1", &員工ID);
   &姓名 = "";
   &部門 = "";
   &職位 = "";
   &月薪 = 0;
   &績效 = "";
   &入職日期 = "";

   If Not &sql.Execute(&姓名, &部門, &職位, &月薪, &績效, &入職日期) Then
      Error "無法獲取員工信息";
      Return -1;
   End-If;

   /* 計算績效係數 */
   &績效係數 = 0;
   Evaluate &績效
      When "A"
         &績效係數 = 2.0;
         Break;
      When "B"
         &績效係數 = 1.5;
         Break;
      When "C"
         &績效係數 = 1.0;
         Break;
      When "D"
         &績效係數 = 0.5;
         Break;
      When "F"
         &績效係數 = 0;
         Break;
      When-Other
         Error "無效的績效等級: " | &績效;
         Return -1;
   End-Evaluate;

   /* 計算基本獎金 */
   &基本獎金 = &月薪 * &績效係數;

   /* 計算服務年限 */
   &當前日期 = %Date;
   &入職日期對象 = DateValue(&入職日期);
   &服務年限 = DateDiff("year", &入職日期對象, &當前日期);

   /* 計算服務年限獎勵 */
   &服務年限獎勵 = Min(&服務年限 * 0.05, 0.5);  /* 最高50% */

   /* 計算部門調整 */
   &部門調整 = 0;
   Evaluate &部門
      When "SALES"
         &部門調整 = 0.10;  /* 銷售部門+10% */
         Break;
      When "RND"
         &部門調整 = 0.05;  /* 研發部門+5% */
         Break;
      When-Other
         &部門調整 = 0;
         Break;
   End-Evaluate;

   /* 計算職位獎勵 */
   &職位獎勵 = 0;
   Evaluate &職位
      When "MANAGER"
         &職位獎勵 = 0.15;  /* 經理+15% */
         Break;
      When "DIRECTOR"
         &職位獎勵 = 0.25;  /* 總監+25% */
         Break;
      When "VP"
         &職位獎勵 = 0.40;  /* 副總裁+40% */
         Break;
      When-Other
         &職位獎勵 = 0;
         Break;
   End-Evaluate;

   /* 計算最終獎金 */
   &最終獎金 = &基本獎金 * (1 + &服務年限獎勵 + &部門調整 + &職位獎勵);

   /* 記錄獎金計算過程 */
   WriteToLog(0, "員工ID: " | &員工ID | ", 姓名: " | &姓名);
   WriteToLog(0, "  月薪: " | &月薪);
   WriteToLog(0, "  績效: " | &績效 | ", 係數: " | &績效係數);
   WriteToLog(0, "  基本獎金: " | &基本獎金);
   WriteToLog(0, "  服務年限: " | &服務年限 | "年, 獎勵: " | (&服務年限獎勵 * 100) | "%");
   WriteToLog(0, "  部門: " | &部門 | ", 調整: " | (&部門調整 * 100) | "%");
   WriteToLog(0, "  職位: " | &職位 | ", 獎勵: " | (&職位獎勵 * 100) | "%");
   WriteToLog(0, "  最終獎金: " | &最終獎金);

   Return &最終獎金;
End-Function;
```

#### 案例3：自動化報表生成

```peoplecode
/*
 * 生成月度銷售報表
 * 1. 從數據庫獲取銷售數據
 * 2. 計算各種統計指標
 * 3. 生成Excel報表
 * 4. 發送電子郵件通知
 */
Function 生成月度銷售報表(&年份 As string, &月份 As string) Returns boolean
   /* 驗證參數 */
   If Not Regex("^\d{4}$", &年份) Then
      Error "年份格式無效，應為4位數字";
      Return False;
   End-If;

   If Not Regex("^(0[1-9]|1[0-2])$", &月份) Then
      Error "月份格式無效，應為01-12";
      Return False;
   End-If;

   /* 設置日期範圍 */
   &開始日期 = &年份 | "-" | &月份 | "-01";
   &下個月 = &月份 = "12" ? "01" : ToString(Value(&月份) + 1, "00");
   &下一年 = &月份 = "12" ? ToString(Value(&年份) + 1) : &年份;
   &結束日期 = &下一年 | "-" | &下個月 | "-01";

   /* 獲取銷售數據 */
   &sql = CreateSQL("
      SELECT 
         PS_SALES_DATA.PRODUCT_ID,
         PS_PRODUCTS.PRODUCT_NAME,
         PS_PRODUCTS.CATEGORY,
         PS_SALES_DATA.REGION,
         SUM(PS_SALES_DATA.QUANTITY) AS TOTAL_QUANTITY,
         SUM(PS_SALES_DATA.AMOUNT) AS TOTAL_AMOUNT
      FROM 
         PS_SALES_DATA
         JOIN PS_PRODUCTS ON PS_SALES_DATA.PRODUCT_ID = PS_PRODUCTS.PRODUCT_ID
      WHERE 
         PS_SALES_DATA.SALE_DATE >= :1 AND PS_SALES_DATA.SALE_DATE < :2
      GROUP BY 
         PS_SALES_DATA.PRODUCT_ID,
         PS_PRODUCTS.PRODUCT_NAME,
         PS_PRODUCTS.CATEGORY,
         PS_SALES_DATA.REGION
      ORDER BY 
         PS_PRODUCTS.CATEGORY,
         PS_SALES_DATA.PRODUCT_ID,
         PS_SALES_DATA.REGION
   ", &開始日期, &結束日期);

   /* 準備數據結構 */
   &銷售數據 = CreateArrayRept(CreateArrayRept("", 0), 0);
   &產品ID = "";
   &產品名稱 = "";
   &類別 = "";
   &地區 = "";
   &總數量 = 0;
   &總金額 = 0;

   /* 讀取數據 */
   While &sql.Fetch(&產品ID, &產品名稱, &類別, &地區, &總數量, &總金額)
      &記錄 = CreateArrayRept("", 6);
      &記錄[1] = &產品ID;
      &記錄[2] = &產品名稱;
      &記錄[3] = &類別;
      &記錄[4] = &地區;
      &記錄[5] = &總數量;
      &記錄[6] = &總金額;
      &銷售數據.Push(&記錄);
   End-While;

   /* 計算統計指標 */
   &類別統計 = CreateJavaMap();
   &地區統計 = CreateJavaMap();
   &總銷售額 = 0;

   For &i = 1 To &銷售數據.Len()
      &記錄 = &銷售數據[&i];
      &類別 = &記錄[3];
      &地區 = &記錄[4];
      &金額 = Value(&記錄[6]);
      &總銷售額 = &總銷售額 + &金額;

      /* 更新類別統計 */
      If &類別統計.containsKey(&類別) Then
         &類別金額 = Value(&類別統計.get(&類別)) + &金額;
         &類別統計.put(&類別, &類別金額);
      Else
         &類別統計.put(&類別, &金額);
      End-If;

      /* 更新地區統計 */
      If &地區統計.containsKey(&地區) Then
         &地區金額 = Value(&地區統計.get(&地區)) + &金額;
         &地區統計.put(&地區, &地區金額);
      Else
         &地區統計.put(&地區, &金額);
      End-If;
   End-For;

   /* 創建Excel工作簿 */
   &excel = CreateJavaObject("com.peoplesoft.pt.excel.ExcelWriter");
   &excel.createWorkbook();

   /* 添加銷售數據工作表 */
   &excel.createSheet("銷售數據");

   /* 添加標題行 */
   &excel.setCellValue(0, 0, "產品ID");
   &excel.setCellValue(0, 1, "產品名稱");
   &excel.setCellValue(0, 2, "類別");
   &excel.setCellValue(0, 3, "地區");
   &excel.setCellValue(0, 4, "銷售數量");
   &excel.setCellValue(0, 5, "銷售金額");

   /* 添加數據行 */
   For &i = 1 To &銷售數據.Len()
      &記錄 = &銷售數據[&i];
      &excel.setCellValue(&i, 0, &記錄[1]);
      &excel.setCellValue(&i, 1, &記錄[2]);
      &excel.setCellValue(&i, 2, &記錄[3]);
      &excel.setCellValue(&i, 3, &記錄[4]);
      &excel.setCellValue(&i, 4, Value(&記錄[5]));
      &excel.setCellValue(&i, 5, Value(&記錄[6]));
   End-For;

   /* 添加類別統計工作表 */
   &excel.createSheet("類別統計");
   &excel.setCellValue(0, 0, "類別");
   &excel.setCellValue(0, 1, "銷售金額");
   &excel.setCellValue(0, 2, "佔比");

   &類別列表 = &類別統計.keySet().toArray();
   For &i = 1 To &類別列表.length
      &類別 = &類別列表[&i - 1];
      &金額 = Value(&類別統計.get(&類別));
      &佔比 = &金額 / &總銷售額;

      &excel.setCellValue(&i, 0, &類別);
      &excel.setCellValue(&i, 1, &金額);
      &excel.setCellValue(&i, 2, &佔比);
   End-For;

   /* 添加地區統計工作表 */
   &excel.createSheet("地區統計");
   &excel.setCellValue(0, 0, "地區");
   &excel.setCellValue(0, 1, "銷售金額");
   &excel.setCellValue(0, 2, "佔比");

   &地區列表 = &地區統計.keySet().toArray();
   For &i = 1 To &地區列表.length
      &地區 = &地區列表[&i - 1];
      &金額 = Value(&地區統計.get(&地區));
      &佔比 = &金額 / &總銷售額;

      &excel.setCellValue(&i, 0, &地區);
      &excel.setCellValue(&i, 1, &金額);
      &excel.setCellValue(&i, 2, &佔比);
   End-For;

   /* 保存Excel文件 */
   &文件名 = "銷售報表_" | &年份 | &月份 | ".xlsx";
   &文件路徑 = "C:\reports\" | &文件名;
   &excel.save(&文件路徑);

   /* 發送電子郵件通知 */
   &郵件 = CreateJavaObject("com.peoplesoft.pt.mail.Mail");
   &郵件.setSubject(&年份 | "年" | &月份 | "月銷售報表");
   &郵件.setFrom("system@example.com");
   &郵件.addTo("sales@example.com");
   &郵件.addCc("manager@example.com");

   &郵件內容 = "親愛的銷售團隊：" | Char(10) | Char(10);
   &郵件內容 = &郵件內容 | &年份 | "年" | &月份 | "月銷售報表已生成，請查收附件。" | Char(10) | Char(10);
   &郵件內容 = &郵件內容 | "總銷售額：" | &總銷售額 | Char(10);
   &郵件內容 = &郵件內容 | "銷售記錄數：" | &銷售數據.Len() | Char(10) | Char(10);
   &郵件內容 = &郵件內容 | "此郵件由系統自動生成，請勿回復。";

   &郵件.setBody(&郵件內容);
   &郵件.addAttachment(&文件路徑);
   &郵件.send();

   MessageBox(0, "", 0, 0, "銷售報表已生成並發送");
   Return True;
End-Function;
```

#### 案例4：高級系統整合與監控儀表板

```peoplecode
/*
 * 高級系統整合與監控儀表板
 * 功能：
 * 1. 從多個來源系統收集數據（數據庫、Web服務、文件）
 * 2. 處理和分析數據
 * 3. 創建實時監控儀表板
 * 4. 實現警報機制
 * 5. 使用緩存優化性能
 */

/* 定義全局變數和緩存 */
Global array of array of any &緩存_系統狀態;
Global datetime &緩存_最後更新時間;
Global number &緩存_有效期 = 300; /* 5分鐘緩存 */

/* 主函數 */
Function 生成系統監控儀表板(&刷新緩存 As boolean) Returns JavaObject
   /* 檢查緩存是否有效 */
   Local boolean &使用緩存 = False;

   If All(&緩存_系統狀態) And Not &刷新緩存 Then
      Local number &緩存時間 = DateTimeDiff(%Datetime, &緩存_最後更新時間);
      If &緩存時間 < &緩存_有效期 Then
         &使用緩存 = True;
         WriteToLog(0, "使用緩存數據，緩存時間: " | &緩存時間 | " 秒");
      End-If;
   End-If;

   /* 如果緩存無效，收集新數據 */
   If Not &使用緩存 Then
      WriteToLog(0, "緩存無效或強制刷新，收集新數據");
      &緩存_系統狀態 = CreateArrayRept(CreateArrayRept("", 0), 0);

      try
         /* 收集數據庫系統狀態 */
         收集數據庫狀態();

         /* 收集應用服務器狀態 */
         收集應用服務器狀態();

         /* 收集Web服務器狀態 */
         收集Web服務器狀態();

         /* 收集批處理狀態 */
         收集批處理狀態();

         /* 收集接口狀態 */
         收集接口狀態();

         /* 更新緩存時間戳 */
         &緩存_最後更新時間 = %Datetime;

      catch Exception &e
         Error "收集系統狀態時出錯: " | &e.ToString();
         Return Null;
      end-try;
   End-If;

   /* 生成儀表板 */
   Return 創建儀表板界面(&緩存_系統狀態);
End-Function;

/* 收集數據庫狀態 */
Function 收集數據庫狀態()
   /* 連接到數據庫 */
   Local SQL &sql;

   /* 檢查數據庫連接 */
   try
      &sql = CreateSQL("SELECT 1 FROM DUAL");
      &sql.Execute();

      /* 收集數據庫統計信息 */
      &sql = CreateSQL("
         SELECT NAME, VALUE 
         FROM V$SYSSTAT 
         WHERE NAME IN ('physical reads', 'physical writes', 'user commits', 'user rollbacks')
      ");

      Local string &名稱, &值;
      While &sql.Fetch(&名稱, &值)
         Local array of string &統計 = CreateArrayRept("", 3);
         &統計[1] = "數據庫";
         &統計[2] = &名稱;
         &統計[3] = &值;
         &緩存_系統狀態.Push(&統計);
      End-While;

      /* 收集表空間使用情況 */
      &sql = CreateSQL("
         SELECT TABLESPACE_NAME, 
                ROUND((BYTES_ALLOC - NVL(BYTES_FREE, 0))/BYTES_ALLOC*100, 2) PCT_USED
         FROM (
            SELECT A.TABLESPACE_NAME TABLESPACE_NAME,
                   SUM(A.BYTES) BYTES_ALLOC,
                   SUM(B.FREE_BYTES) BYTES_FREE
            FROM DBA_DATA_FILES A,
                 (SELECT TABLESPACE_NAME, SUM(BYTES) FREE_BYTES FROM DBA_FREE_SPACE GROUP BY TABLESPACE_NAME) B
            WHERE A.TABLESPACE_NAME = B.TABLESPACE_NAME(+)
            GROUP BY A.TABLESPACE_NAME
         )
         WHERE PCT_USED > 80
         ORDER BY PCT_USED DESC
      ");

      Local string &表空間名稱;
      Local number &使用百分比;
      While &sql.Fetch(&表空間名稱, &使用百分比)
         Local array of string &統計 = CreateArrayRept("", 4);
         &統計[1] = "數據庫";
         &統計[2] = "表空間使用率";
         &統計[3] = &表空間名稱;
         &統計[4] = &使用百分比;
         &緩存_系統狀態.Push(&統計);

         /* 檢查是否需要發出警報 */
         If &使用百分比 > 90 Then
            發送警報("表空間警報", &表空間名稱 | " 表空間使用率達到 " | &使用百分比 | "%，請及時擴容！", "高");
         End-If;
      End-While;

      /* 添加數據庫狀態摘要 */
      Local array of string &狀態 = CreateArrayRept("", 3);
      &狀態[1] = "系統狀態";
      &狀態[2] = "數據庫";
      &狀態[3] = "正常";
      &緩存_系統狀態.Push(&狀態);

   catch Exception &e
      /* 記錄錯誤並添加數據庫狀態為異常 */
      WriteToLog(0, "收集數據庫狀態時出錯: " | &e.ToString());

      Local array of string &狀態 = CreateArrayRept("", 3);
      &狀態[1] = "系統狀態";
      &狀態[2] = "數據庫";
      &狀態[3] = "異常";
      &緩存_系統狀態.Push(&狀態);

      發送警報("數據庫警報", "數據庫連接或查詢出錯: " | &e.ToString(), "高");
   end-try;
End-Function;

/* 收集應用服務器狀態 */
Function 收集應用服務器狀態()
   try
      /* 使用Java獲取JVM信息 */
      Local JavaObject &runtime = GetJavaClass("java.lang.Runtime").getRuntime();
      Local number &總內存 = &runtime.totalMemory();
      Local number &空閒內存 = &runtime.freeMemory();
      Local number &已用內存 = &總內存 - &空閒內存;
      Local number &內存使用率 = Round((&已用內存 / &總內存) * 100, 1);

      /* 添加內存使用統計 */
      Local array of string &內存統計 = CreateArrayRept("", 4);
      &內存統計[1] = "應用服務器";
      &內存統計[2] = "內存使用";
      &內存統計[3] = "JVM內存使用率";
      &內存統計[4] = &內存使用率;
      &緩存_系統狀態.Push(&內存統計);

      /* 檢查是否需要發出警報 */
      If &內存使用率 > 85 Then
         發送警報("內存警報", "應用服務器內存使用率達到 " | &內存使用率 | "%，可能需要重啟服務！", "中");
      End-If;

      /* 獲取活動會話數 */
      Local SQL &sql = CreateSQL("
         SELECT COUNT(*) 
         FROM PSOPRDEFN 
         WHERE LASTUPDDTTM > %DateTimeOut(%DateTimeAdd(%Datetime, -1, 0, 0, 0))
      ");

      Local number &活動會話數;
      &sql.Execute(&活動會話數);

      /* 添加會話統計 */
      Local array of string &會話統計 = CreateArrayRept("", 4);
      &會話統計[1] = "應用服務器";
      &會話統計[2] = "用戶會話";
      &會話統計[3] = "活動會話數";
      &會話統計[4] = &活動會話數;
      &緩存_系統狀態.Push(&會話統計);

      /* 添加應用服務器狀態摘要 */
      Local array of string &狀態 = CreateArrayRept("", 3);
      &狀態[1] = "系統狀態";
      &狀態[2] = "應用服務器";
      &狀態[3] = "正常";
      &緩存_系統狀態.Push(&狀態);

   catch Exception &e
      /* 記錄錯誤並添加應用服務器狀態為異常 */
      WriteToLog(0, "收集應用服務器狀態時出錯: " | &e.ToString());

      Local array of string &狀態 = CreateArrayRept("", 3);
      &狀態[1] = "系統狀態";
      &狀態[2] = "應用服務器";
      &狀態[3] = "異常";
      &緩存_系統狀態.Push(&狀態);

      發送警報("應用服務器警報", "收集應用服務器狀態時出錯: " | &e.ToString(), "高");
   end-try;
End-Function;

/* 收集Web服務器狀態 */
Function 收集Web服務器狀態()
   try
      /* 使用HTTP請求檢查Web服務器狀態 */
      Local JavaObject &url = CreateJavaObject("java.net.URL", "http://localhost:8000/status");
      Local JavaObject &connection = &url.openConnection();
      &connection.setConnectTimeout(5000);
      &connection.setReadTimeout(5000);

      Local number &responseCode = &connection.getResponseCode();

      /* 添加Web服務器響應時間統計 */
      Local datetime &開始時間 = %Datetime;
      &connection = CreateJavaObject("java.net.URL", "http://localhost:8000/").openConnection();
      &connection.connect();
      Local number &響應時間 = DateTimeDiff(%Datetime, &開始時間) * 1000; /* 毫秒 */

      Local array of string &響應統計 = CreateArrayRept("", 4);
      &響應統計[1] = "Web服務器";
      &響應統計[2] = "性能";
      &響應統計[3] = "響應時間(ms)";
      &響應統計[4] = &響應時間;
      &緩存_系統狀態.Push(&響應統計);

      /* 檢查是否需要發出警報 */
      If &響應時間 > 500 Then
         發送警報("Web服務器警報", "Web服務器響應時間達到 " | &響應時間 | "ms，超過閾值！", "中");
      End-If;

      /* 添加Web服務器狀態摘要 */
      Local array of string &狀態 = CreateArrayRept("", 3);
      &狀態[1] = "系統狀態";
      &狀態[2] = "Web服務器";
      &狀態[3] = "正常";
      &緩存_系統狀態.Push(&狀態);

   catch Exception &e
      /* 記錄錯誤並添加Web服務器狀態為異常 */
      WriteToLog(0, "收集Web服務器狀態時出錯: " | &e.ToString());

      Local array of string &狀態 = CreateArrayRept("", 3);
      &狀態[1] = "系統狀態";
      &狀態[2] = "Web服務器";
      &狀態[3] = "異常";
      &緩存_系統狀態.Push(&狀態);

      發送警報("Web服務器警報", "Web服務器可能無法訪問: " | &e.ToString(), "高");
   end-try;
End-Function;

/* 收集批處理狀態 */
Function 收集批處理狀態()
   try
      /* 查詢進程調度器狀態 */
      Local SQL &sql = CreateSQL("
         SELECT SERVERNAME, SERVERTYPE, STATUS 
         FROM PS_SERVERDEFN 
         WHERE SERVERTYPE = 'PSPRCSRV'
      ");

      Local string &服務器名稱, &服務器類型, &狀態;
      Local boolean &所有服務器正常 = True;

      While &sql.Fetch(&服務器名稱, &服務器類型, &狀態)
         Local array of string &服務器統計 = CreateArrayRept("", 4);
         &服務器統計[1] = "批處理";
         &服務器統計[2] = "進程調度器";
         &服務器統計[3] = &服務器名稱;
         &服務器統計[4] = &狀態;
         &緩存_系統狀態.Push(&服務器統計);

         If &狀態 <> "ACTIVE" Then
            &所有服務器正常 = False;
            發送警報("批處理警報", "進程調度器 " | &服務器名稱 | " 狀態為 " | &狀態, "高");
         End-If;
      End-While;

      /* 查詢長時間運行的進程 */
      &sql = CreateSQL("
         SELECT PRCSINSTANCE, PRCSNAME, RUNDTTM, MSGDATA 
         FROM PS_PMN_PRCSLIST 
         WHERE RUNSTATUS = '7' 
         AND RUNDTTM < %DateTimeOut(%DateTimeAdd(%Datetime, -2, 0, 0, 0))
      ");

      Local string &實例ID, &進程名稱, &運行時間, &消息;
      Local boolean &有長時間運行進程 = False;

      While &sql.Fetch(&實例ID, &進程名稱, &運行時間, &消息)
         &有長時間運行進程 = True;

         Local array of string &進程統計 = CreateArrayRept("", 5);
         &進程統計[1] = "批處理";
         &進程統計[2] = "長時間運行進程";
         &進程統計[3] = &進程名稱;
         &進程統計[4] = &實例ID;
         &進程統計[5] = &運行時間;
         &緩存_系統狀態.Push(&進程統計);

         發送警報("批處理警報", "進程 " | &進程名稱 | " (實例 " | &實例ID | ") 已運行超過2小時", "中");
      End-While;

      /* 添加批處理狀態摘要 */
      Local array of string &狀態 = CreateArrayRept("", 3);
      &狀態[1] = "系統狀態";
      &狀態[2] = "批處理";

      If &所有服務器正常 And Not &有長時間運行進程 Then
         &狀態[3] = "正常";
      Else
         &狀態[3] = "警告";
      End-If;

      &緩存_系統狀態.Push(&狀態);

   catch Exception &e
      /* 記錄錯誤並添加批處理狀態為異常 */
      WriteToLog(0, "收集批處理狀態時出錯: " | &e.ToString());

      Local array of string &狀態 = CreateArrayRept("", 3);
      &狀態[1] = "系統狀態";
      &狀態[2] = "批處理";
      &狀態[3] = "異常";
      &緩存_系統狀態.Push(&狀態);

      發送警報("批處理警報", "收集批處理狀態時出錯: " | &e.ToString(), "高");
   end-try;
End-Function;

/* 收集接口狀態 */
Function 收集接口狀態()
   try
      /* 查詢接口錯誤日誌 */
      Local SQL &sql = CreateSQL("
         SELECT INTERFACE_ID, ERROR_TYPE, COUNT(*) ERROR_COUNT 
         FROM PS_INTERFACE_ERROR_LOG 
         WHERE ERROR_DTTM > %DateTimeOut(%DateTimeAdd(%Datetime, -24, 0, 0, 0))
         GROUP BY INTERFACE_ID, ERROR_TYPE
         HAVING COUNT(*) > 5
      ");

      Local string &接口ID, &錯誤類型;
      Local number &錯誤計數;
      Local boolean &有接口錯誤 = False;

      While &sql.Fetch(&接口ID, &錯誤類型, &錯誤計數)
         &有接口錯誤 = True;

         Local array of string &錯誤統計 = CreateArrayRept("", 5);
         &錯誤統計[1] = "接口";
         &錯誤統計[2] = "錯誤統計";
         &錯誤統計[3] = &接口ID;
         &錯誤統計[4] = &錯誤類型;
         &錯誤統計[5] = &錯誤計數;
         &緩存_系統狀態.Push(&錯誤統計);

         發送警報("接口警報", "接口 " | &接口ID | " 在過去24小時內發生 " | &錯誤計數 | " 次 " | &錯誤類型 | " 錯誤", "高");
      End-While;

      /* 查詢接口狀態 */
      &sql = CreateSQL("
         SELECT INTERFACE_ID, STATUS, LAST_RUN_DTTM 
         FROM PS_INTERFACE_STATUS 
         WHERE STATUS <> 'ACTIVE'
      ");

      Local string &接口ID, &狀態;
      Local datetime &最後運行時間;

      While &sql.Fetch(&接口ID, &狀態, &最後運行時間)
         &有接口錯誤 = True;

         Local array of string &接口統計 = CreateArrayRept("", 5);
         &接口統計[1] = "接口";
         &接口統計[2] = "狀態";
         &接口統計[3] = &接口ID;
         &接口統計[4] = &狀態;
         &接口統計[5] = &最後運行時間;
         &緩存_系統狀態.Push(&接口統計);

         發送警報("接口警報", "接口 " | &接口ID | " 狀態為 " | &狀態 | "，最後運行時間: " | &最後運行時間, "高");
      End-While;

      /* 添加接口狀態摘要 */
      Local array of string &狀態 = CreateArrayRept("", 3);
      &狀態[1] = "系統狀態";
      &狀態[2] = "接口";

      If Not &有接口錯誤 Then
         &狀態[3] = "正常";
      Else
         &狀態[3] = "警告";
      End-If;

      &緩存_系統狀態.Push(&狀態);

   catch Exception &e
      /* 記錄錯誤並添加接口狀態為異常 */
      WriteToLog(0, "收集接口狀態時出錯: " | &e.ToString());

      Local array of string &狀態 = CreateArrayRept("", 3);
      &狀態[1] = "系統狀態";
      &狀態[2] = "接口";
      &狀態[3] = "異常";
      &緩存_系統狀態.Push(&狀態);

      發送警報("接口警報", "收集接口狀態時出錯: " | &e.ToString(), "高");
   end-try;
End-Function;

/* 發送警報 */
Function 發送警報(&標題 As string, &消息 As string, &優先級 As string)
   /* 記錄警報 */
   WriteToLog(0, "警報 [" | &優先級 | "]: " | &標題 | " - " | &消息);

   try
      /* 保存警報到數據庫 */
      Local SQL &sql = CreateSQL("
         INSERT INTO PS_SYSTEM_ALERTS (
            ALERT_ID, ALERT_DTTM, ALERT_TITLE, ALERT_MSG, PRIORITY, STATUS
         ) VALUES (
            NEXT_ALERT_ID, %DateTimeIn(%Datetime), :1, :2, :3, 'NEW'
         )
      ");

      &sql.Execute(&標題, &消息, &優先級);

      /* 對於高優先級警報，發送電子郵件 */
      If &優先級 = "高" Then
         Local JavaObject &郵件 = CreateJavaObject("com.peoplesoft.pt.mail.Mail");
         &郵件.setSubject("[高優先級警報] " | &標題);
         &郵件.setFrom("system@example.com");
         &郵件.addTo("admin@example.com");
         &郵件.setBody(&消息);
         &郵件.send();
      End-If;

   catch Exception &e
      WriteToLog(0, "發送警報時出錯: " | &e.ToString());
   end-try;
End-Function;

/* 創建儀表板界面 */
Function 創建儀表板界面(&數據 As array of array of any) Returns JavaObject
   /* 使用Java創建儀表板UI */
   Local JavaObject &儀表板 = CreateJavaObject("com.peoplesoft.pt.dashboard.Dashboard");

   /* 設置儀表板標題 */
   &儀表板.setTitle("系統監控儀表板");
   &儀表板.setSubtitle("最後更新: " | %Datetime);

   /* 創建系統狀態面板 */
   Local JavaObject &狀態面板 = &儀表板.createPanel("系統狀態摘要");

   /* 添加系統狀態指示器 */
   For &i = 1 To &數據.Len()
      If &數據[&i][1] = "系統狀態" Then
         Local string &組件 = &數據[&i][2];
         Local string &狀態 = &數據[&i][3];

         If &狀態 = "正常" Then
            &狀態面板.addStatusIndicator(&組件, &狀態, "GREEN");
         Else If &狀態 = "警告" Then
            &狀態面板.addStatusIndicator(&組件, &狀態, "YELLOW");
         Else
            &狀態面板.addStatusIndicator(&組件, &狀態, "RED");
         End-If;
      End-If;
   End-For;

   /* 創建數據庫面板 */
   Local JavaObject &數據庫面板 = &儀表板.createPanel("數據庫狀態");

   /* 添加數據庫統計 */
   For &i = 1 To &數據.Len()
      If &數據[&i][1] = "數據庫" Then
         If &數據[&i][2] = "表空間使用率" Then
            &數據庫面板.addGauge(&數據[&i][3], Value(&數據[&i][4]), 0, 100);
         Else
            &數據庫面板.addMetric(&數據[&i][2], &數據[&i][3]);
         End-If;
      End-If;
   End-For;

   /* 創建應用服務器面板 */
   Local JavaObject &應用服務器面板 = &儀表板.createPanel("應用服務器狀態");

   /* 添加應用服務器統計 */
   For &i = 1 To &數據.Len()
      If &數據[&i][1] = "應用服務器" Then
         If &數據[&i][3] = "JVM內存使用率" Then
            &應用服務器面板.addGauge("JVM內存使用率", Value(&數據[&i][4]), 0, 100);
         Else If &數據[&i][3] = "活動會話數" Then
            &應用服務器面板.addCounter("活動會話數", Value(&數據[&i][4]));
         Else
            &應用服務器面板.addMetric(&數據[&i][2], &數據[&i][3]);
         End-If;
      End-If;
   End-For;

   /* 創建批處理面板 */
   Local JavaObject &批處理面板 = &儀表板.createPanel("批處理狀態");

   /* 添加批處理統計 */
   For &i = 1 To &數據.Len()
      If &數據[&i][1] = "批處理" Then
         If &數據[&i][2] = "長時間運行進程" Then
            &批處理面板.addAlert(&數據[&i][3] | " (" | &數據[&i][4] | ")", "運行時間: " | &數據[&i][5]);
         Else
            &批處理面板.addMetric(&數據[&i][2], &數據[&i][3] | ": " | &數據[&i][4]);
         End-If;
      End-If;
   End-For;

   /* 創建接口面板 */
   Local JavaObject &接口面板 = &儀表板.createPanel("接口狀態");

   /* 添加接口統計 */
   For &i = 1 To &數據.Len()
      If &數據[&i][1] = "接口" Then
         If &數據[&i][2] = "錯誤統計" Then
            &接口面板.addAlert(&數據[&i][3], &錯誤類型 | ": " | &數據[&i][5] | " 次");
         Else
            &接口面板.addMetric(&數據[&i][2], &數據[&i][3] | ": " | &數據[&i][4]);
         End-If;
      End-If;
   End-For;

   /* 返回完成的儀表板 */
   Return &儀表板;
End-Function;
```

通過這些案例研究，您可以看到如何將 PeopleCode 的各種功能和技術整合起來，解決實際業務問題。這些例子展示了如何處理文件、操作數據庫、執行複雜計算、生成報表、發送電子郵件、系統監控和整合等高級任務。

隨著您對 PeopleCode 的深入學習和實踐，您將能夠開發出更加複雜和強大的應用程序，滿足各種業務需求。記住，良好的代碼組織、全面的錯誤處理和適當的註釋是開發高質量 PeopleCode 程序的關鍵。

祝您在 PeopleCode 的學習和應用中取得成功！
