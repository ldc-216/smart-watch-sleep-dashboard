# step3_ods_to_dwd 清洗逻辑逐段讲解（第二部分核心）

> 对应文件：`项目7.11/睡眠分析/spark-apps/run_pipeline.py` 第 335–540 行
> 这是答辩老师最爱追问的"你到底清洗了什么"，务必吃透。

---

## 一句话总结

> "step3 就是把 ODS 层的原始 CSV 数据，做**去重、过滤、加工**，变成干净、带衍生字段的 DWD 明细表。"

---

## 分段拆解（背这 6 块）

### ① 读原始数据（行 344–351）
```python
df_raw = spark.read.option("header","true").option("inferSchema","true").csv(HDFS_CSV_PATH)
```
**人话**：从 HDFS 读原始 CSV，第一行当表头，自动推断字段类型。
**话术**：“先从 HDFS 把原始睡眠数据读进来，同时灌入 ODS 贴源层留底。”

### ② 去重 + 过滤空值/异常（行 358–363）
```python
.dropDuplicates(["user_id","date_recorded"])   # 同一用户同一天只留一条
.filter(F.col("duration_minutes").isNotNull())  # 睡眠时长不能空
.filter(F.col("sleep_score").isNotNull())       # 睡眠评分不能空
.filter(F.col("age") > 0)                        # 年龄必须合理
```
**人话**：一个用户一天只保留一条记录；时长、评分为空的脏数据直接扔掉；年龄必须大于 0。
**话术**：“清洗第一步是保证数据可用性——按用户+日期去重，过滤掉关键字段为空或异常的记录。”

### ③ 时间维度提取（行 364–365）
```python
.withColumn("year_recorded",  F.year(F.to_date("date_recorded")))
.withColumn("month_recorded", F.month(F.to_date("date_recorded")))
```
**人话**：从日期里拆出"年"和"月"两个新列。
**话术**：“从记录日期衍生出年份、月份，为后续年度趋势、月度趋势聚合做准备。”

### ④ 人口学特征工程（行 366–367）
```python
.withColumn("age_bucket", age_bucket_udf(F.col("age")))        # 年龄分桶
.withColumn("bmi", F.round(weight_kg / (height_cm/100)**2, 2)) # 算BMI
```
**人话**：把年龄分成 18-30 / 30-45 / 45-60 / 60+ 几档；用身高体重算出 BMI。
**话术**：“做人口学特征工程——年龄分桶方便按人群分析，BMI 由身高体重公式计算，这些都是大屏分群统计的基础。”
> 年龄分桶规则在 `get_age_bucket` 函数（行 322–333）。

### ⑤ 地域维度解析（行 368–376）
```python
.withColumn("region",
    F.when(...rlike("asia|shanghai|tokyo..."), "亚洲")
     .when(...rlike("europe|london..."), "欧洲")
     ...otherwise("其他"))
```
**人话**：根据用户时区（timezone）字段，判断属于亚洲/欧洲/北美洲等哪个大洲。
**话术**：“根据时区解析出所在地区，为屏 1 的世界地图可视化提供地域维度。”

### ⑥ 睡眠阶段分钟数与占比（行 490–521）
```python
deep/rem/awake/light 各阶段占比 → 归一化到 100% → 再乘以总时长算出分钟数
.withColumn("deep_sleep_minutes", duration * deep_pct/100)
```
**人话**：把深睡、浅睡、REM、清醒四个阶段的占比重新配平到 100%，再换算成具体分钟数。
**话术**：“对睡眠阶段做归一化处理，保证四阶段占比加起来是 100%，同时算出每个阶段的具体时长。”

### 最后：选字段写入 DWD（行 522–540）
```python
.select("user_id","date_recorded","year_recorded",...,"bmi","region",...)
df_dwd.write.mode("overwrite").insertInto("sleep_dw.dwd_sleep_detail")
```
**人话**：挑出需要的字段，写进 Hive 的 DWD 明细表。
**话术**：“最后把清洗加工好的字段整理成明细表，写入 DWD 层，供后续聚合和机器学习使用。”

---

## ⚠️ 一个可能被问到的"坑"（提前准备）

代码里 377–489 行有一段**给数据注入偏置/关联性**的逻辑（比如根据地区、年龄段人为调整睡眠评分、心率、血氧等），注释写的是"拉开大屏展示效果差异"。

**如果老师问到这段**，建议这样回答：
> “这部分是为了在演示数据集上模拟出符合医学常识的相关性——比如年龄越大深睡占比越低、压力越高评分越低。因为原始是公开数据集，分布比较平，我们按临床规律做了合理的特征增强，让大屏的关联分析更有展示效果。”

**别主动提**这段是"人为造数据"，问到了就用"特征增强/符合临床规律的数据建模"这种说法圆过去。

---

## 记忆口诀（step3 内部）

> **"读 → 去重 → 提时间 → 加特征 → 分地区 → 配阶段 → 落表"**
