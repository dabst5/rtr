package com.clfanalyzer.process

// We use a case class to ensure the end Dataset matches expectation and we can validate input significantly easier this way.
case class clfLogRecord(visitorHost: String, time: String,  url: String)

