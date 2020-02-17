package com.clfanalyzer.analyze

import org.apache.spark.sql.{Dataset, Row}
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._


class Analyzer {

  def topNAnalyzer (ds: Dataset[Row], topN: Int): Dataset[Row] = {
    (for (column <- ds.drop("time").columns) yield {
      val w = Window.partitionBy("time").orderBy(desc("hitstotal"+column))

      ds.groupBy("time",column).count()
        .withColumnRenamed("count",s"hitstotal$column")
        .withColumn("denserank", dense_rank().over(w))
        .filter(col("denserank") <= topN)
    }).reduce(_.join(_,Seq("time","denserank")).orderBy("time", "denserank"))
  }

}
