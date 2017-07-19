/*
 *  Copyright (c) 2017 Tran Le Duy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duy.ide.utils.xml;

/**
 * Created by Daniel Hoogen on 19/02/2016.
 * <p/>
 * This exception is thrown when an unexpected element is found in a xml based file
 */
public class UnexpectedElementException extends Exception {
    /**
     * Constructor which creates an error string from the given string parameters
     *
     * @param elementName the name of the unexpected element
     */
    public UnexpectedElementException(String elementName) {
        super("Unexpected element: " + elementName + "!");
    }
}