package com.clfanalyzer.test

import java.nio.file.Files

import com.clfanalyzer.process._
import org.junit._


class TestProcess {

  import java.io.IOException
  import java.nio.file.Paths

  @Test
  @throws[IOException]
  def testLogProcessor(): Unit = {
    val inputData = Files.readAllLines(Paths.get(System.getProperty("user.dir") + "/src/test/resources/test_data.txt"))

    val parser = new Parse

  }
}
