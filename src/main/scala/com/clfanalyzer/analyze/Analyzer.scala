package com.clfanalyzer.analyze

import org.apache.spark.sql.{Dataset, Row}
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._


class Analyzer {

  /*
    For each column in the input dataframe, expluding "time" this method will:
    1. Create a Dataset with count and dense rank of the currently column, partitioned by time.
        dense rank is important due to the denormalized nature of what is being asked.
    2. Remove rows higher than N rank level
    3. Join created Datasets by "time" and "denserank" to one easily digestible table

    A nice feature of this way of creating the Datasets is that we could also store or manipulate
    each column rank dataset in future iterations before a join is made.
   */
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
