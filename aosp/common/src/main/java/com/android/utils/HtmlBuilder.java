/*
 * Copyright (C) 2013 The Android Open Source Project
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
package com.android.utils;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;

import java.net.URL;

public class HtmlBuilder {
    @NonNull private final StringBuilder mStringBuilder;
    private String mTableDataExtra;

    public HtmlBuilder(@NonNull StringBuilder stringBuilder) {
        mStringBuilder = stringBuilder;
    }

    public HtmlBuilder() {
        mStringBuilder = new StringBuilder(100);
    }

    public HtmlBuilder openHtmlBody() {
        addHtml("<html><body>");

        return this;
    }

    public HtmlBuilder closeHtmlBody() {
        addHtml("</body></html>");

        return this;
    }

    public HtmlBuilder addHtml(@NonNull String html) {
        mStringBuilder.append(html);

        return this;
    }

    public HtmlBuilder addNbsp() {
        mStringBuilder.append("&nbsp;");

        return this;
    }

    public HtmlBuilder addNbsps(int count) {
        for (int i = 0; i < count; i++) {
            addNbsp();
        }

        return this;
    }

    public HtmlBuilder newline() {
        mStringBuilder.append("<BR/>");

        return this;
    }

    public HtmlBuilder newlineIfNecessary() {
        if (!SdkUtils.endsWith(mStringBuilder, "<BR/>")) {
            mStringBuilder.append("<BR/>");
        }

        return this;
    }

    public HtmlBuilder addLink(@Nullable String textBefore,
            @NonNull String linkText,
            @Nullable String textAfter,
            @NonNull String url) {
        if (textBefore != null) {
            add(textBefore);
        }

        addLink(linkText, url);

        if (textAfter != null) {
            add(textAfter);
        }

        return this;
    }

    public HtmlBuilder addLink(@NonNull String text, @NonNull String url) {
        int begin = 0;
        int length = text.length();
        for (; begin < length; begin++) {
            char c = text.charAt(begin);
            if (Character.isWhitespace(c)) {
                mStringBuilder.append(c);
            } else {
                break;
            }
        }
        mStringBuilder.append("<A HREF=\"");
        mStringBuilder.append(url);
        mStringBuilder.append("\">");

        XmlUtils.appendXmlTextValue(mStringBuilder, text.trim());
        mStringBuilder.append("</A>");

        int end = length - 1;
        for (; end > begin; end--) {
            char c = text.charAt(begin);
            if (Character.isWhitespace(c)) {
                mStringBuilder.append(c);
            }
        }

        return this;
    }

    public HtmlBuilder add(@NonNull String text) {
        XmlUtils.appendXmlTextValue(mStringBuilder, text);

        return this;
    }

    @NonNull
    public String getHtml() {
        return mStringBuilder.toString();
    }

    public HtmlBuilder beginBold() {
        mStringBuilder.append("<B>");

        return this;
    }

    public HtmlBuilder endBold() {
        mStringBuilder.append("</B>");

        return this;
    }

    public HtmlBuilder addBold(String text) {
        beginBold();
        add(text);
        endBold();

        return this;
    }

    public HtmlBuilder beginItalic() {
        mStringBuilder.append("<I>");

        return this;
    }

    public HtmlBuilder endItalic() {
        mStringBuilder.append("</I>");

        return this;
    }

    public HtmlBuilder addItalic(String text) {
        beginItalic();
        add(text);
        endItalic();

        return this;
    }

    public HtmlBuilder beginDiv() {
        return beginDiv(null);
    }

    public HtmlBuilder beginDiv(@Nullable String cssStyle) {
        mStringBuilder.append("<div");
        if (cssStyle != null) {
            mStringBuilder.append(" style=\"");
            mStringBuilder.append(cssStyle);
            mStringBuilder.append("\"");
        }
        mStringBuilder.append('>');
        return this;
    }

    public HtmlBuilder endDiv() {
        mStringBuilder.append("</div>");
        return this;
    }

    public HtmlBuilder addHeading(@NonNull String text, @NonNull String fontColor) {
        mStringBuilder.append("<font style=\"font-weight:bold; color:").append(fontColor)
                .append(";\">");
        add(text);
        mStringBuilder.append("</font>");

        return this;
    }

    /**
     * The JEditorPane HTML renderer creates really ugly bulleted lists; the
     * size is hardcoded to use a giant heavy bullet. So, use a definition
     * list instead.
     */
    private static final boolean USE_DD_LISTS = true;

    public HtmlBuilder beginList() {
        if (USE_DD_LISTS) {
            mStringBuilder.append("<DL>");
        } else {
            mStringBuilder.append("<UL>");
        }

        return this;
    }

    public HtmlBuilder endList() {
        if (USE_DD_LISTS) {
            mStringBuilder.append("</DL>");
        } else {
            mStringBuilder.append("</UL>");
        }

        return this;
    }

    public HtmlBuilder listItem() {
        if (USE_DD_LISTS) {
            mStringBuilder.append("<DD>");
            mStringBuilder.append("-&NBSP;");
        } else {
            mStringBuilder.append("<LI>");
        }

        return this;
    }

    public HtmlBuilder addImage(URL url, @Nullable String altText) {
        String link = "";
        try {
            link = url.toURI().toURL().toExternalForm();
        }
        catch (Throwable t) {
            // pass
        }
        mStringBuilder.append("<img src='");
        mStringBuilder.append(link);
        mStringBuilder.append("'");

        if (altText != null) {
            mStringBuilder.append(" alt=\"");
            mStringBuilder.append(altText);
            mStringBuilder.append("\"");
        }
        mStringBuilder.append(" />");

        return this;
    }

    public HtmlBuilder addIcon(@Nullable String src) {
        if (src != null) {
            mStringBuilder.append("<img src='");
            mStringBuilder.append(src);
            mStringBuilder.append("' width=16 height=16 border=0 />");
        }

        return this;
    }

    public HtmlBuilder beginTable(@Nullable String tdExtra) {
        mStringBuilder.append("<table>");
        mTableDataExtra = tdExtra;
        return this;
    }

    public HtmlBuilder beginTable() {
        return beginTable(null);
    }

    public HtmlBuilder endTable() {
        mStringBuilder.append("</table>");
        return this;
    }

    public HtmlBuilder beginTableRow() {
        mStringBuilder.append("<tr>");
        return this;
    }

    public HtmlBuilder endTableRow() {
        mStringBuilder.append("</tr>");
        return this;
    }

    public HtmlBuilder addTableRow(boolean isHeader, String... columns) {
        if (columns == null || columns.length == 0) {
            return this;
        }

        String tag = "t" + (isHeader ? 'h' : 'd');

        beginTableRow();
        for (String c : columns) {
            mStringBuilder.append('<');
            mStringBuilder.append(tag);
            if (mTableDataExtra != null) {
                mStringBuilder.append(' ');
                mStringBuilder.append(mTableDataExtra);
            }
            mStringBuilder.append('>');

            mStringBuilder.append(c);

            mStringBuilder.append("</");
            mStringBuilder.append(tag);
            mStringBuilder.append('>');
        }
        endTableRow();

        return this;
    }

    public HtmlBuilder addTableRow(String... columns) {
        return addTableRow(false, columns);
    }

    @NonNull
    public StringBuilder getStringBuilder() {
        return mStringBuilder;
    }
}
