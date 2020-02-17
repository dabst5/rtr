package com.clfanalyzer.driver

import com.clfanalyzer.process.Parse
import org.apache.spark.SparkFiles
import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import org.apache.log4j.Logger


import com.clfanalyzer.analyze.Analyzer

object ClfAnalyzerDriver {

  private  val logger = Logger.getLogger("com.clfanalyzer.driver")

  def main(args: Array[String]): Unit = {
    var dataSource = "ftp://ita.ee.lbl.gov/traces/NASA_access_log_Jul95.gz"
    var topN = 10
    if (args.length == 2){
      dataSource = args(0)
      topN = args(1).toInt
    } else {
      logger.info("No arguments entered. Using defaults")
    }

    // Create spark session. Currently assumes local spark.
    val spark: SparkSession = SparkSession.builder.master("local").appName("webscraper").getOrCreate
    val sc = spark.sparkContext
    import spark.implicits._

    // Not a sftp so we can use the spark library to download and read the compressed file
    sc.addFile(dataSource)
    val fileName = SparkFiles.get(dataSource.split("/").last)
    val fileRDD = sc.textFile(fileName)

    // The original file has no inherent way of delineating columns for Spark to put in a DataFrame.
    // Instead, we'll parse using the parseCLF method we've written and map the message so that we can turn it into a DataFrame.
    val parse = new Parse
    val accessLogs = fileRDD.map(parse.parseCLF).filter(!_.visitorHost.equals("Empty"))

    // Here we would convert the rdd to a dataframe to perform any filtering and mapping, as datasets do not handle this as efficiently.
    // However we filtered on entry so we can convert it directly to a dateset.
    val logsDs = accessLogs.toDS()
    // Converting our time String to a DateType and repartition on time.
    val logsDsRepartition = logsDs.select($"visitorhost", to_date(to_timestamp($"time")).alias("time"), $"url" ).repartition($"time")
    // We cache the Dataset so the next actions will be faster, as will recovery if necessary.
    logsDsRepartition.cache()

    /*
    "determine the top-n most frequent visitors and urls for each day of the trace."
     */
    val analyzer = new Analyzer

    // This will calculate the rank of any columns that are not 'time' as well.
    // In future iterations it would be preferred if columns were set via property and defaulted to "url" and "visitorhost"
    val sDs = analyzer.topNAnalyzer(logsDsRepartition, topN)

    // Save the calculated rank dataframe. We avoid using coalesce at all costs as it pulls all data onto one now.
    // This may not be an issue with small data sets but it will not scale
    sDs.write.mode(SaveMode.Overwrite).csv("./src/main/resources/output.csv")
    sDs.show(30,false)

  }

}
