把 Spark 训练完导出的 sleep_score_rf.pmml 文件放到这个目录下。

对应关系：
  05_ml_sleep_score_prediction.py (Spark) 训练模型
      -> jpmml-sparkml 导出 sleep_score_rf.pmml
      -> 拷贝到本目录 (src/main/resources/models/sleep_score_rf.pmml)
      -> PmmlConfig.java 启动时加载
      -> PredictionService.java 提供 /api/screen4/predict 接口实时预测

没有这个文件，项目会在启动时报错（PmmlConfig 里 resource 找不到），
这是故意的：宁可启动失败，也不要接口返回假数据。
