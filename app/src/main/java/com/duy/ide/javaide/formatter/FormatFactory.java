package com.duy.ide.javaide.formatter;

import android.content.Context;

import com.android.annotations.Nullable;
import com.duy.ide.java.setting.AppSetting;
import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import com.google.googlejavaformat.java.JavaFormatterOptions;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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

    public static String format(Context context, String src, @Nullable Type type) throws Exception {
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
        AppSetting setting = new AppSetting(context);
        JavaFormatterOptions.Builder builder = JavaFormatterOptions.builder();
        builder.style(setting.getFormatType() == 0
                ? JavaFormatterOptions.Style.GOOGLE : JavaFormatterOptions.Style.AOSP);
        return new Formatter(builder.build()).formatSource(src);
    }

    private static String formatXml(Context context, String src) throws TransformerException,
            ParserConfigurationException, IOException, SAXException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(src)));

        AppSetting setting = new AppSetting(context);
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
