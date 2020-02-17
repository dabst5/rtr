FROM cjonesy/docker-spark:2.4.4 AS build

MAINTAINER "Dar Swift <d.swift510@gmail.com>"

ADD ./target/ClfAnalyzer-0.0.1-SNAPSHOT-jar-with-dependencies.jar /app
COPY ./target/ClfAnalyzer-0.0.1-SNAPSHOT-jar-with-dependencies.jar /app
WORKDIR /app

CMD [ \
    "spark-submit", \
    "--master=local", \
    "--class=com.clfanalyzer.driver.ClfAnalyzer", \
    "--master spark://spark:7077", \
    "/ClfAnalyzer-0.0.1-SNAPSHOT-jar-with-dependencies.jar" \
    ]