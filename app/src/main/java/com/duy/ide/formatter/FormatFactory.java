package com.duy.ide.formatter;

import android.content.Context;

import com.android.annotations.Nullable;
import com.duy.ide.setting.JavaPreferences;
import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import com.google.googlejavaformat.java.JavaFormatterOptions;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Created by Duy on 13-Aug-17.
 */

public class FormatFactory {
    @Nullable
    public static Type getType(File ext) {
        if (ext.getPath().contains(".")) {
            switch (ext.getPath().substring(ext.getPath().lastIndexOf(".") + 1).toLowerCase()) {
                case "java":
                    return Type.JAVA;
                case "xml":
                    return Type.XML;
                default:
                    return null;
            }
        } else {
            return null;
        }
    }

    public static String format(Context context, String src, @Nullable Type type) throws Exception {
        if (type == null) throw new UnsupportedTypeException();
        switch (type) {
            case JAVA:
                return formatJava(context, src);
            case XML:
                return formatXml(context, src);
            default:
                return src;
        }
    }

    private static String formatJava(Context context, String src) throws FormatterException {
        JavaPreferences setting = new JavaPreferences(context);
        JavaFormatterOptions.Builder builder = JavaFormatterOptions.builder();
        builder.style(setting.getFormatType() == 0
                ? JavaFormatterOptions.Style.GOOGLE : JavaFormatterOptions.Style.AOSP);
        return new Formatter(builder.build()).formatSource(src);
    }

    private static String formatXml(Context context, String src) throws TransformerException,
            ParserConfigurationException, IOException, SAXException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(src)));

        JavaPreferences setting = new JavaPreferences(context);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", setting.getTab().length() + "");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

        //initialize StreamResult with File object to save to file
        StreamResult result = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(doc);
        transformer.transform(source, result);
        return result.getWriter().toString();
    }

    public enum Type {XML, JAVA}
}
