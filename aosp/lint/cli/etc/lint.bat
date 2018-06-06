@echo off
rem Copyright (C) 2011 The Android Open Source Project
rem
rem Licensed under the Apache License, Version 2.0 (the "License");
rem you may not use this file except in compliance with the License.
rem You may obtain a copy of the License at
rem
rem      http://www.apache.org/licenses/LICENSE-2.0
rem
rem Unless required by applicable law or agreed to in writing, software
rem distributed under the License is distributed on an "AS IS" BASIS,
rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem See the License for the specific language governing permissions and
rem limitations under the License.

rem don't modify the caller's environment
setlocal

rem Set up prog to be the path of this script, including following symlinks,
rem and set up progdir to be the fully-qualified pathname of its directory.
set prog=%~f0

rem Grab current directory before we change it
set work_dir=%cd%

rem Change current directory and drive to where the script is, to avoid
rem issues with directories containing whitespaces.
cd /d %~dp0

rem Get the CWD as a full path with short names only (without spaces)
for %%i in ("%cd%") do set prog_dir=%%~fsi

rem Check we have a valid Java.exe in the path.
set java_exe=
call lib\find_java.bat
if not defined java_exe goto :EOF

set jarfile=lint.jar
set frameworkdir=.

if exist %frameworkdir%\%jarfile% goto JarFileOk
    set frameworkdir=lib

if exist %frameworkdir%\%jarfile% goto JarFileOk
    set frameworkdir=..\framework

:JarFileOk

if debug NEQ "%1" goto NoDebug
    set java_debug=-agentlib:jdwp=transport=dt_socket,server=y,address=8050,suspend=y
    shift 1
:NoDebug

set jarpath=%frameworkdir%\%jarfile%
set javaextdirs=%frameworkdir%

call "%java_exe%" %java_debug% -Xmx1024m "-Dcom.android.tools.lint.bindir=%prog_dir%" "-Dcom.android.tools.lint.workdir=%work_dir%" -Djava.awt.headless=true -classpath "%jarpath%" com.android.tools.lint.Main %*

