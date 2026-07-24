from pyspark.sql import SparkSession
import urllib.request
import json

spark = (
    SparkSession.builder
    .appName("Check_Avg_Score")
    .enableHiveSupport()
    .config("spark.sql.warehouse.dir", "hdfs://namenode:9000/user/hive/warehouse")
    .getOrCreate()
)

print("=== Hive Table Stats ===")
df = spark.table("sleep_dw.dwd_sleep_detail")
df.selectExpr("AVG(sleep_score)", "MIN(sleep_score)", "MAX(sleep_score)", "COUNT(*)").show()

print("=== Elasticsearch Index Stats ===")
try:
    url = "http://elasticsearch:9200/sleep_records/_search"
    query = {
        "size": 0,
        "aggs": {
            "avg_score": { "avg": { "field": "sleep_score" } }
        }
    }
    req = urllib.request.Request(
        url,
        data=json.dumps(query).encode("utf-8"),
        headers={"Content-Type": "application/json"}
    )
    with urllib.request.urlopen(req) as res:
        resp = json.loads(res.read().decode("utf-8"))
        print(resp["aggregations"])
except Exception as e:
    print("Error querying ES:", e)

spark.stop()
