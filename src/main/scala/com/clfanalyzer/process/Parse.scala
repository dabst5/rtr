package com.clfanalyzer.process

import org.apache.log4j.Logger

class Parse extends Serializable {

  private  val logger = Logger.getLogger("com.clfanalyzer.process")
  /*
 CLF is standardized and simple enough to parse using a regular expression.
 Is it cleaner to extract using df.regexp_extract? Maybe but validation of fields/lines is front loaded and initial validation is easier this way.
 As the business requirement only cares about 3 segments of the log, we only look for and capture those 3 segments.
  */
  val PATTERN = "^(\\S+) \\S+ \\S+ \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"\\S+\\s?(.+?(?=\\s+HTTP|\"))".r

  /*
  The downloaded file is using Apache Common LogFile (CLF) format.
  I found some scala libraries that parse this format but they all appear to be unmaintained (lack commits within the past 2 years).
  I prefer not to recreate the wheel, but this being the case, it will be less of a maintenance nightmare to roll my own version.
  I could also use an option here. The code would look more concise but it can be more confusing for Java users to read, hence sticking to try/catch.
  Because we have a specific use case we can capture all necessary data in regex groups.

  We also avoid using a UDF, which could be a more resource intensive.

  */
  def parseCLF(clfLog: String): clfLogRecord = {

    // CLF has non-standard (in context of Spark) time formatting.
    // I didn't find a library to convert it in the five minutes I looked, so I'm implementing the mapping via User Defined Function (UDF).
    val month_map = Map("Jan" -> 1, "Feb" -> 2, "Mar" -> 3, "Apr" -> 4, "May" -> 5, "Jun" -> 6, "Jul" -> 7, "Aug" -> 8
      , "Sep" -> 9, "Oct" -> 10, "Nov" -> 11, "Dec" -> 12)

    def reformatClfTime(s: String) ={
      "%3$s-%2$s-%1$s %4$s:%5$s:%6$s".format(s.substring(0,2),month_map(s.substring(3,6)),s.substring(7,11)
        ,s.substring(12,14),s.substring(15,17),s.substring(18))
    }

    val toTimestamp = reformatClfTime(_)

    try {
      val res = PATTERN.findFirstMatchIn(clfLog)

      if (res.isEmpty) {
        println("Rejected Log Line: " + clfLog)
        logger.debug("Rejected Log Line: " + clfLog)
        //Files.write(Paths.get("./empty.log"), clfLog.concat("\n").getBytes(StandardCharsets.UTF_8),StandardOpenOption.APPEND)
        clfLogRecord("Empty", "-", "-")
      }
      else {
        val m = res.get
        // TODO: Add Validation
        val visitorHost = m.group(1)
        val time = toTimestamp(m.group(2).dropRight(7))
        val url = m.group(3)
        clfLogRecord(visitorHost, time, url)

      }
    } catch
      {
        case e: Exception =>
          //Files.write(Paths.get("./error.log"), clfLog.concat("\n").getBytes(StandardCharsets.UTF_8),StandardOpenOption.APPEND)
          logger.error("Exception on line: " + clfLog + " : " + e.printStackTrace())
          println("Exception on line: " + clfLog + " : " + e.printStackTrace());
          clfLogRecord("Empty", "", "")
      }
  }


}
