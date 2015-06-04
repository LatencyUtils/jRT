# jRT
----------------------------------------------------------------------------

Version: 0.0.1
----------------------------------------------------------------------------
----------------------------------------------------------------------------

jRT is a instrumentation tool that logs and records networking I/O
operations "response times" (applicaion response time if be correct).

jRT can be executed in one of three main ways:

1. It can be run as a Java agent (using: java -javaagent:jRT.jar)

2. It can be injected into a running application (using: jRT -pid <pid>)

----------------------------------------------------------------------------

# Using jRT as a Java agent:

jRT is most often used as a java agent. This is useful for platforms and
environments where a java agent is simpler to integrate into launch scripts,
or in environments where using the bash jRT wrapper script is not practical
(e.g. Windows, and environments where java is not directly launched from
the command line).

jRT.jar can be used as a java agent using the following launch syntax:

% java -javaagent:jRT.jar MyProgram

or

% java -javaagent:jRT.jar="<options>" MyProgram.jar -a -b -c

You can find the available options for the Java agent mode by running:

% java -javaagent:jRT.jar="-h"

Here is a Java agent usage example with explicit parameters:

% java -javaagent:jRT.jar="-start=10000,-si=1000,-l=jRTs.%p-%h.%d.%i,-mode=i2o" MyProgram.jar -a -b -c

This example will record response times experienced during the running of MyProgram.jar
in log file jRTs.< PID >-< HOST NAME >.< DATE >.< INSTANCE# >.< i2o | o2i >.hlog. 
Measurement will start in 10 second delay, and interval data will be records every 1 second.

Useful java agent related notes:

Note 1: When used as a java agent, jRT will treat spaces, commas, and
semicolons as delimiting characters ([ ,;]+). For example, the option string
"-start=0 -si=1000" is equivalent to the option string "-start=0,-si=1000". This is
useful for environments where placing space delimiters into quoted strings
is difficult or confusing.

Note 2: I find that a common way people add jRT as a java agent is by using
the _JAVA_OPTIONS environment variable. This often allows one to add the jRT
 java agent without significant launch script surgery. For example:

export _JAVA_OPTIONS='-javaagent:/path/to/jRT/target/jRT.jar="-start=20000 -si=1000"'

----------------------------------------------------------------------------

# Reading and processing the jRT log with jRTLogProcessor:

jRT logs response time information in a histogram log (see HdrHistogram.org).
This histogram log contains a full, high fidelity histogram of all collected
result sin each interval, in a highly compressed form (typically using only
~200-400 bytes per interval). However, other than the timestamp and maximum
response time magnitude found in the given interval, the rest of the log line for
each interval is not human readable (it is a base64 encoding of a compressed
HdrHistogram).

To translate the jRT log file to a more human-readable form, the jRTLogProcessor
utility is provided. In it's simplest form, this utility can be used as such

% jRTLogProcessor -i mylog.hlog -o mylog

Which will produce log file mylog and mylog.hgrm containing a human readable
interval log (with selcted percentiles in each interval), as well as a human
readable histogram percentile distribution log.

jRTLogProcessor can also be used to produce log files for an arbitrary
section of the jRT log, by using the optional -start and -end parameters.

See jRTLogProcessor -h for more details.

----------------------------------------------------------------------------

# Launching jRT by attaching it to existing, running application:

The jRT agent can be injected into a live, running Java application
if the environment supports the java attach API (which is typically available
in java environments running Java SE 6 or later).

$ java -Xbootclasspath/a:jRT.jar -jar jRT.jar -pid <pid>

NOTE: In order to attach to a running java application, the running
application needs to have ${JAVA_HOME}/lib/tools.jar in it's classpath.
While this is commonly the case already for many IDE and desktop environments,
and for environments that involve or enable other attachable agents (such as
profilers), you may find that it is not included in your application's
classpath, and that it needs to be added if attaching jRT at runtime
is needed (launching jRT as a Java agent per the below may be a good
alternative).

----------------------------------------------------------------------------

# Response time Charts: Plotting jRT results

A jRTPlotter.xls Excel spreadsheet is included to conveniently
plot jRT log files produced by jRTLogProcessor in "Response time Chart"form.
To use the spreadsheet, load it into Excel, (make sure to enable macros),
and follow the 2-step instructions in the main menu worksheet to automatically
import the log files and produce the Response time Chart.

Note that jRTPlotter.xls reads the log files produced by jRTLogProcessor,
(the interval log and the .hgrm histogram percentile distribution log), and
not the .hlog log format that jRT outputs directly.

----------------------------------------------------------------------------

# Supported/Tested platforms:

The jRT command is expected to work and has been tested on the following
frameworks:
- tomcat
- VolanoMark
- cassandra
- netty
- jetty

If you use jRT on other applications, please report back
on your experience so that we can expand the list.

----------------------------------------------------------------------------

# Example: adding jRT to Tomcat runs:

In Tomcat's catalina.sh script, replace the following line:
exec  "$_RUNJAVA" "$LOGGING_CONFIG" $JAVA_OPTS $CATALINA_OPTS

with:
exec "$_RUNJAVA" -javaagent:$JRT_HOME/jRT.jar "$LOGGING_CONFIG" $JAVA_OPTS $CATALINA_OPTS

----------------------------------------------------------------------------

# Note: Use of HdrHistogram.

jRT depends on and makes systemic use of HdrHistogram to collected and
report on the statistical distribution of response times. This package includes an
HdrHistogram.jar jar file to support this functionality. HdrHistogram sources,
documentation, and a ready to use jar file can all be found on GitHub, at
http://giltene.github.com/HdrHistogram

----------------------------------------------------------------------------

# Building jRT:

jRT can be (re)built from source files using Maven:

% mvn clean package
