docker build --rm -t cjonesy/docker-spark:2.4.4 ../../../

#WORKDIR=../../../

#spark-submit --master=local --class=com.clfanalyzer.driver.ClfAnalyzer --master spark://spark:7077 $WORKDIR/target/ClfAnalyzer-0.0.1-SNAPSHOT-jar-with-dependencies.jar 5
