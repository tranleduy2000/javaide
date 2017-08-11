/*
 * Copyright (C) 2010 Ken Ellinwood.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kellinwood.logging.log4j;

import org.apache.log4j.Logger;


import kellinwood.logging.LoggerInterface;

public class Log4jLogger implements LoggerInterface {

    Logger log;
    
    public Log4jLogger(String category) {
    
        log = Logger.getLogger( category);
    }

    @Override
    public void debug(String message, Throwable t) {
        log.debug( message, t);
    }

    @Override
    public void debug(String message) {
        log.debug( message);
    }

    @Override
    public void error(String message, Throwable t) {
        log.error( message, t);
    }

    @Override
    public void error(String message) {
        log.error( message);
    }

    @Override
    public void info(String message, Throwable t) {
        log.info( message, t);
    }

    @Override
    public void info(String message) {
        log.info( message);
    }

    @Override
    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    @Override
    public boolean isWarningEnabled() {
        return true;
    }

    @Override
    public void warning(String message, Throwable t) {
        log.warn(message, t);
    }

    @Override
    public void warning(String message) {
        log.warn(message);
    }

}
