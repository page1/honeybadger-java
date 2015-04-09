honeybadger-java
================

Java Client to report exceptions to Honeybadger.io

By adding the following line to your code(Possibly in the main function). All errors that you don't catch will be caught and sent to honeybadger.

    Honeybadger honeybadger = new Honeybadger();

In order for this to work, you must have the following environmental variables set.
    HONEYBADGER_API_KEY
    JAVA_ENV

HONEYBADGER_API_KEY: Contains your Honeybadger api key. This is found in your projects settings on the Honeybadger web page.

JAVA_ENV: Describes the environment where your program is running. I use development and production.


log4j appender
==============

The HoneybadgerAppender is a log4j appender that reports log messages set to the ERROR level to Honeybadger.

To use it, you will need to add a few lines to log4j.properties in addition to the environment variables
required for the Honeybadger Java client. First, add HoneybadgerAppender as a root appender:

    log4j.rootLogger=INFO, file, console [...], honeybadger

Then set the appender:

    log4j.appender.honeybadger=honeybadger.HoneybadgerAppender
    log4j.appender.honeybadger.Threshold=ERROR

Note that, since HoneybadgerAppender instantiates Honeybadger, all uncaught exceptions will *also* be reported to Honeybadger.