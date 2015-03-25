@ECHO OFF
TITLE signalGreen

REM Signal Green Model Starter
REM By Signal Green Team
REM
REM Script Based on Repast Simphony Model Starter
REM By Michael J. North
REM 

ECHO Signal Green Model Starter
ECHO Initialising path environment...

REM Repast Simphony Directories.
set REPAST_SIMPHONY_ROOT=../repast.simphony/repast.simphony.runtime_2.2.0/
set REPAST_SIMPHONY_LIB=%REPAST_SIMPHONY_ROOT%lib/

REM Define the Core Repast Simphony Directories and JARs
SET CP=%CP%;%REPAST_SIMPHONY_ROOT%bin
SET CP=%CP%;%REPAST_SIMPHONY_LIB%saf.core.runtime.jar
SET CP=%CP%;%REPAST_SIMPHONY_LIB%commons-logging-1.1.2.jar
SET CP=%CP%;%REPAST_SIMPHONY_LIB%javassist-3.17.1-GA.jar
SET CP=%CP%;%REPAST_SIMPHONY_LIB%jpf.jar
SET CP=%CP%;%REPAST_SIMPHONY_LIB%jpf-boot.jar
SET CP=%CP%;%REPAST_SIMPHONY_LIB%log4j-1.2.16.jar
SET CP=%CP%;%REPAST_SIMPHONY_LIB%xpp3_min-1.1.4c.jar
SET CP=%CP%;%REPAST_SIMPHONY_LIB%xstream-1.4.7.jar
SET CP=%CP%;%REPAST_SIMPHONY_LIB%xmlpull-1.1.3.1.jar
SET CP=%CP%;%REPAST_SIMPHONY_LIB%commons-cli-1.2.jar
SET CP=%CP%;../groovylib/groovy-all-2.1.5.jar

ECHO Loaded Repast Simphony Libraries...

REM Change to the Default Repast Simphony Directory
CD signalGreen

REM Start the Model
ECHO Starting Signal Green Simulator V3.0...

START javaw -Xss10M -Xmx400M -cp %CP% repast.simphony.runtime.RepastMain ./signalGreen.rs
