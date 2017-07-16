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

package com.duy.editor.utils.xml;


import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class XmlReader {
    /**
     * Reads the categories node from an input stream of a .bpc file and returns it
     */
    public static NodeList getRootNode(InputStream is) throws ParserConfigurationException, SAXException,
            IOException, UnexpectedElementException {
        //Read document
        Document document;
        document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
        document.getDocumentElement().normalize();

        //Get Nodes from document
        NodeList childRoot = document.getFirstChild().getChildNodes();
        int name = childRoot.getLength();
        return childRoot;
    }
}
