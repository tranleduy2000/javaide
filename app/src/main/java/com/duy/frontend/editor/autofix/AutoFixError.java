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

package com.duy.frontend.editor.autofix;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.Layout;

import com.duy.pascal.interperter.declaration.lang.function.FunctionDeclaration;
import com.duy.pascal.interperter.ast.runtime_value.value.access.ConstantAccess;
import com.duy.pascal.interperter.ast.runtime_value.value.access.VariableAccess;
import com.duy.pascal.interperter.linenumber.LineInfo;
import com.duy.pascal.interperter.parse_exception.convert.UnConvertibleTypeException;
import com.duy.pascal.interperter.parse_exception.define.TypeIdentifierExpectException;
import com.duy.pascal.interperter.parse_exception.define.UnknownIdentifierException;
import com.duy.pascal.interperter.parse_exception.grouping.GroupingException;
import com.duy.pascal.interperter.parse_exception.missing.MissingTokenException;
import com.duy.pascal.interperter.parse_exception.value.ChangeValueConstantException;
import com.duy.pascal.interperter.declaration.lang.types.Type;
import com.duy.frontend.DLog;
import com.duy.frontend.editor.completion.KeyWord;
import com.duy.frontend.editor.completion.Patterns;
import com.duy.frontend.editor.view.AutoIndentEditText;
import com.duy.frontend.editor.view.HighlightEditor;
import com.duy.frontend.editor.view.LineUtils;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used to automatically correct some errors when compiling
 * Such as declare variable , type, function, ..
 * For example:
 * <code>
 * begin
 * a := 2;
 * end.
 * </code>
 * =>
 * <code>
 * var
 * a: integer; {or some other type}
 * begin
 * a := 2;
 * end;
 * </code>
 * <p>
 * Created by Duy on 23-May-17.
 */
public class AutoFixError {
    private static final String TAG = "AutoFix";
    private HighlightEditor editable;

    public AutoFixError(@NonNull HighlightEditor editText) {
        this.editable = editText;
    }

    /**
     * This method will be import new type for program
     * the {@link TypeIdentifierExpectException} contains missing type.
     * <p>
     * First, we find the "type" keyword, if not found we will be create new keyword
     * Then, we insert a structure <code>"name" = "type"</code>
     */
    public void fixMissingType(TypeIdentifierExpectException e) {
        //don't work if has selection
        //sub string from 0 to postion error
        TextData text = getText(e.getScope().getStartLine(), e.getLineInfo());

        String type = e.getMissingType();
        String textToInsert;
        Matcher matcher = Patterns.TYPE.matcher(text.getText());
        int insertPosition = 0;

        if (matcher.find()) {
            insertPosition = matcher.end();
            textToInsert = "\n" + "    " + type + " = %t ;";
        } else {
            /*
            if not found "type" keyword, insert new type keyword
            type    <== keyword type must be above var
                ....
            var
                ....
            */
            if ((matcher = Patterns.PROGRAM.matcher(text.getText())).find()) {
                insertPosition = matcher.end();
            } else if ((matcher = Patterns.VAR.matcher(text.getText())).find()) {
                insertPosition = matcher.start();
            }
            //if not found var keyword, insert "type" above "uses" keyword
            else if ((matcher = Patterns.USES.matcher(text.getText())).find()) {
                insertPosition = matcher.end();
            }
            textToInsert = "\ntype\n" + "    " + type + " = %t ;\n";
        }

        matcher = Patterns.REPLACE_CURSOR.matcher(textToInsert);
        if (matcher.find()) {
            textToInsert = textToInsert.replaceAll("%\\w", "");

            insertPosition += text.getOffset();
            insertPosition = Math.max(0, insertPosition); //normalize

            editable.getText().insert(insertPosition, textToInsert);
            editable.setSelection(insertPosition + matcher.start());

            //set suggest data
            editable.restoreAfterClick(KeyWord.DATA_TYPE);
        }
    }


    private TextData getText(LineInfo startLine, LineInfo endLine) {
        CharSequence text = editable.getText().subSequence(
                editable.getLayout().getLineStart(startLine.getLine())
                        + startLine.getColumn(),

                editable.getLayout().getLineEnd(endLine.getLine()));

        int offset = editable.getLayout().getLineStart(startLine.getLine())
                + startLine.getColumn()
                + startLine.getLength();

        if (offset < 0) offset = 0;
        return new TextData(text, offset);
    }

    /**
     * This method will be add missing define, such as variable,
     * constant, function or procedure
     */
    public void fixMissingDefine(UnknownIdentifierException e) {
        DLog.d(TAG, "fixMissingDefine() called with: e = [" + e + "]" + " " + e.getFitType());
        if (e.getFitType() == DefineType.DECLARE_VAR) {
            //add missing var
            declareVar(e);
        } else if (e.getFitType() == DefineType.DECLARE_CONST) {
            //add missing const
            declareConst(e);
        } else if (e.getFitType() == DefineType.DECLARE_FUNCTION) {
            //add missing function
            declareFunction(e);
        } else if (e.getFitType() == DefineType.DECLARE_PROCEDURE) {
            //add missing procedure
        }
    }

    /**
     * This method will be declare const, the constant pascal
     * usually in the top of program, below "program" or "uses" keyword
     */
    private void declareConst(UnknownIdentifierException e) {
        //sub string from 0 to postion error
        TextData text = getText(e.getScope().getStartLine(), e.getLineInfo());

        String textToInsert = "";
        int insertPosition = 0;
        String name = e.getName();

        Matcher matcher = Patterns.CONST.matcher(text.getText());
        if (matcher.find()) {
            insertPosition = matcher.end();
            textToInsert = "\n" + "    " + name + " = %v ;";
        } else {
            if ((matcher = Patterns.PROGRAM.matcher(text.getText())).find()) {
                insertPosition = matcher.end();
            } else if ((matcher = Patterns.USES.matcher(text.getText())).find()) {
                insertPosition = matcher.end();
            } else if ((matcher = Patterns.TYPE.matcher(text.getText())).find()) {
                insertPosition = matcher.start();
            }
            textToInsert = "\nconst \n" + AutoIndentEditText.TAB_CHARACTER + name + " = %v ;";
        }

        insertPosition += text.getOffset();

        matcher = Patterns.REPLACE_CURSOR.matcher(textToInsert);
        if (matcher.find()) {
            textToInsert = textToInsert.replaceAll("%\\w", "");

            editable.getText().insert(insertPosition, textToInsert);
            editable.setSelection(insertPosition + matcher.start());
        }
    }

    private void declareFunction(UnknownIdentifierException e) {
    }

    /**
     * This method will be declare variable, the variable often below the
     * "const", "uses", "program" keyword,
     * First, match position of list keyword
     * Then insert new variable
     */
    private void declareVar(UnknownIdentifierException e) {
        declareVar(new LineInfo[]{e.getScope().getStartLine(), e.getLineInfo()},
                e.getName(),
                "",//unknown type
                null); //non init value
    }


    private boolean declareVar(LineInfo[] lines, String name, String type, String initValue) {
        if (lines.length != 2) throw new RuntimeException("The length line array must be 2");
        TextData text = getText(lines[0], lines[1]);
        return declareVar(text, name, type, initValue);
    }

    private boolean declareVar(TextData text, String name, String type, String initValue) {
        String textToInsert = "";
        int insertPosition = 0;
        int startSelect;
        int endSelect;

        Matcher matcher = Patterns.VAR.matcher(text.getText());
        if (matcher.find()) {
            insertPosition = matcher.end();
            textToInsert = AutoIndentEditText.TAB_CHARACTER + name + ": ";

            startSelect = textToInsert.length();
            endSelect = startSelect + type.length();

            textToInsert += type + (initValue != null ? " = " + initValue : "") + ";\n";
        } else {
            if ((matcher = Patterns.TYPE.matcher(text.getText())).find()) {
                insertPosition = matcher.end();
            } else if ((matcher = Patterns.USES.matcher(text.getText())).find()) {
                insertPosition = matcher.end();
            } else if ((matcher = Patterns.PROGRAM.matcher(text.getText())).find()) {
                insertPosition = matcher.end();
            }
            textToInsert = "\nvar\n" + AutoIndentEditText.TAB_CHARACTER + name + ": ";

            startSelect = textToInsert.length();
            endSelect = startSelect + type.length();

            textToInsert += type + (initValue != null ? " = " + initValue : "") + ";\n";
        }

        editable.getText().insert(text.getOffset() + insertPosition, textToInsert);
        editable.setSelection(text.getOffset() + insertPosition + startSelect,
                text.getOffset() + insertPosition + endSelect);

        //set suggest data
        editable.restoreAfterClick(KeyWord.DATA_TYPE);

        editable.showKeyboard();
        return true;
    }

    /**
     * Auto wrong type
     * For example
     * <code>
     * var c: integer;
     * begin
     * c := 'hello';            <=== this is wrong type
     * end.
     * </code>
     * <p>
     * This method will be match position of variable or function and change to
     * <code>
     * var c: string;             <== change to String
     * begin
     * c := 'hello';
     * end.
     * </code>
     *
     * @param e
     */
    public void fixUnConvertType(UnConvertibleTypeException e) {
        //get a part of text
        TextData text = getText(e.getScope().getStartLine(), e.getLineInfo());
        if (e.getIdentifier() instanceof VariableAccess) {
            if (e.getScope() instanceof FunctionDeclaration.FunctionExpressionContext) {
                String name = ((FunctionDeclaration.FunctionExpressionContext) e.getScope()).function.getName();
                //this is function name
                if (name.equalsIgnoreCase(((VariableAccess) e.getIdentifier()).getName())) {
                    changeTypeFunction(name, text, e.getValueType());
                } else {
                    changeTypeVar(text, (VariableAccess) e.getIdentifier(), e.getValueType());
                }
            } else {
                changeTypeVar(text, (VariableAccess) e.getIdentifier(), e.getValueType());
            }
        } else if (e.getIdentifier() instanceof ConstantAccess) {
            changeTypeConst(text, (ConstantAccess) e.getIdentifier(), e.getValueType());

        } else if (e.getValue() instanceof VariableAccess) {
            if (e.getScope() instanceof FunctionDeclaration.FunctionExpressionContext) {
                String name = ((FunctionDeclaration.FunctionExpressionContext) e.getScope()).function.getName();
                //this is function name
                if (name.equalsIgnoreCase(((VariableAccess) e.getValue()).getName())) {
                    changeTypeFunction(name, text, e.getTargetType());
                } else {
                    changeTypeVar(text, (VariableAccess) e.getValue(), e.getTargetType());
                }
            } else {
                changeTypeVar(text, (VariableAccess) e.getValue(), e.getTargetType());
            }

        } else if (e.getValue() instanceof ConstantAccess) {
            changeTypeConst(text, (ConstantAccess) e.getValue(), e.getTargetType());
        }
    }

    /**
     * This method will be Change type constant to type of value
     * if constant is define with type
     * <p>
     * Example
     * const a: integer = 'adsda'; => change to string
     */
    private void changeTypeConst(TextData text, ConstantAccess identifier, Type valueType) {
        DLog.d(TAG, "fixUnConvertType: constant " + identifier);

        if (identifier.getName() == null) { //can not replace because it is not a identifier
            DLog.d(TAG, "changeTypeConst: this is not identifier");
            return;
        }

        String name = identifier.getName();
        Pattern pattern = Pattern.compile("(^const\\s+|\\s+const\\s+)" + //match "const"  //1
                        "(.*?)" + //other const                                  //2
                        "(" + name + ")" + //name of const                       //3
                        "(\\s?)" +//one or more white space                         //4
                        "(:)" + //colon                                             //5
                        "(.*?)" + //type????                                        //6
                        "(=)" +
                        "(.*?)" +
                        "(;)",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text.getText());

        if (matcher.find()) {
            DLog.d(TAG, "fixUnConvertType: match " + matcher);
            final int start = matcher.start(6) + text.getOffset();
            int end = matcher.end(6) + text.getOffset();

            final String insertText = valueType.toString();
            editable.getEditableText().replace(start, end, insertText);
            editable.post(new Runnable() {
                @Override
                public void run() {
                    editable.setSelection(start, start + insertText.length());
                }
            });
            editable.showKeyboard();
        }
    }

    /**
     * @param name - name of function
     * @param text - a part text of the edit start at 0 and end at lineInfo where then function place
     */
    private void changeTypeFunction(final String name, TextData text, Type valueType) {
        Pattern pattern = Pattern.compile(
                "(^function\\s+|\\s+function\\s+)" + //function token //1
                        "(" + name + ")" + //name of function         //2
                        "(\\s?)" + //white space                      //3
                        "(:)" +                                       //4
                        "(.*?)" + //type of function                  //5
                        ";"); //end                                   //6
        Matcher matcher = pattern.matcher(text.getText());
        if (matcher.find()) {
            DLog.d(TAG, "changeTypeFunction: match " + matcher);
            final int start = matcher.start(5) + text.getOffset();
            final int end = matcher.end(5) + text.getOffset();

            final String insertText = valueType.toString();
            editable.getEditableText().replace(start, end, insertText);
            editable.post(new Runnable() {
                @Override
                public void run() {
                    editable.setSelection(start, start + insertText.length());
                    editable.showKeyboard();
                }
            });
        } else {
            DLog.d(TAG, "changeTypeFunction: can not find " + pattern);
        }
    }

    private void changeTypeVar(TextData text, VariableAccess identifier, Type valueType) {
        DLog.d(TAG, "fixUnConvertType: variable");
        final String name = identifier.getName();
        Pattern pattern = Pattern.compile("(^var\\s+|\\s+var\\s+)" + //match "var"  //1
                        "(.*?)" + //other variable                                  //2
                        "(" + name + ")" + //name of variable                       //3
                        "(\\s?)" +//one or more white space                         //4
                        "(:)" + //colon                                             //5
                        "(.*?)" + //any type                                        //6
                        "(;)",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

        Matcher matcher = pattern.matcher(text.getText());
        DLog.d(TAG, "fixUnConvertType: " + text);

        if (matcher.find()) {
            DLog.d(TAG, "fixUnConvertType: match " + matcher);
            final int start = matcher.start(6) + text.getOffset();
            int end = matcher.end(6) + text.getOffset();

            final String insertText = valueType.toString();
            editable.getEditableText().replace(start, end, insertText);
            editable.post(new Runnable() {
                @Override
                public void run() {
                    editable.setSelection(start, start + insertText.length());
                }
            });
            editable.showKeyboard();
        } else {
            DLog.d(TAG, "fixUnConvertType: can not find " + pattern);
        }
    }

    /**
     * replace current token by expect token exactly
     *
     * @param current - current token
     * @param expect  - token for replace
     * @param insert  - true if insert, <code>false</code> if replace
     * @param line    - current lineInfo
     * @param column  - start at column of @lineInfo
     */
    public void fixExpectToken(String current, String expect, boolean insert, int line, int column) {
        DLog.d(TAG, "fixExpectToken() called with: current = [" + current + "], expect = [" + expect + "], insert = [" + insert + "], lineInfo = [" + line + "], column = [" + column + "]");
        //get text in lineInfo
        CharSequence textInLine = getTextInLine(line, column);

        //position from 0 to current token
        int offset = LineUtils.getStartIndexAtLine(editable, line) + column;

        //find token
        Pattern pattern = Pattern.compile("(" + current + ")"); //current token
        Matcher matcher = pattern.matcher(textInLine);
        if (matcher.find()) {

            int start = matcher.start();
            int end = matcher.end();

            //insert or replace other token
            Editable text = editable.getText();
            if (!insert) {
                text.replace(offset + start, offset + start + end, expect);
            } else {
                expect = " " + expect + " ";
                text.insert(offset + start, expect);

            }
            editable.setSelection(offset + start, offset + start + expect.length());
            editable.showKeyboard();
        }
    }

    /**
     * get text in lineInfo
     */
    private CharSequence getTextInLine(int line, int column) {
        Editable text = editable.getText();
        Layout layout = editable.getLayout();
        if (layout != null) {
            int lineStart = layout.getLineStart(line);
            int lineEnd = layout.getLineEnd(line);
            lineStart = lineStart + column;
            if (lineStart > text.length()) lineStart = text.length();
            if (lineStart > lineEnd) lineStart = lineEnd;
            return text.subSequence(lineStart, lineEnd);
        }
        return "";
    }

    public void insertToken(MissingTokenException e) {
        final int start = LineUtils.getStartIndexAtLine(editable, e.getLineInfo().getLine()) + e.getLineInfo().getColumn();
        final String insertText = e.getMissingToken();
        editable.getEditableText().insert(start, insertText);
        editable.post(new Runnable() {
            @Override
            public void run() {
                editable.setSelection(start, insertText.length() + start);
                editable.showKeyboard();
            }
        });

    }

    public void changeConstToVar(ChangeValueConstantException e) {
        FirebaseAnalytics.getInstance(editable.getContext()).logEvent("changeConstToVar", new Bundle());

        DLog.d(TAG, "changeConstToVar: " + e);

        TextData text = getText(e.getScope().getStartLine(), e.getLineInfo());
        ConstantAccess<Object> constant = e.getConst();
        Pattern pattern = Pattern.compile(
                "(^const\\s+|\\s+const\\s+)" + //1
                        "(" + constant.getName() + ")" + //2
                        "(\\s?)" + //3
                        "(=)" +//4
                        "(.*?)" +//5
                        "(;)",//6
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

        Matcher matcher = pattern.matcher(text.getText());
        if (matcher.find()) {
            DLog.d(TAG, "changeConstToVar: " + matcher);
            int start = matcher.start(2) + text.getOffset() - 1;
            start = Math.max(0, start);
            int end = matcher.end(6) + text.getOffset();

            editable.getEditableText().delete(start, end);

            declareVar(text,
                    constant.getName(), //name
                    constant.getRuntimeType(null).declType.toString(), //type
                    constant.toCode()); //initialization value
        } else {
            pattern = Pattern.compile(
                    "(^const\\s+|\\s+const\\s+)" + //1
                            "(" + constant.getName() + ")" + //2
                            "(\\s?)" + //3
                            "(:)" + //4
                            "(\\s?)" +//5
                            "(.*?)" +//6 type
                            "(=)" + //7
                            "(.*?)" +//8
                            "(;)" //9
                    , Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

            matcher = pattern.matcher(text.getText());
            if (matcher.find()) {
                int start = matcher.start(2) + text.getOffset() - 1;
                start = Math.max(0, start);
                int end = matcher.end(9) + text.getOffset();

                editable.getEditableText().delete(start, end);

                declareVar(text,
                        constant.getName(),  //name
                        constant.getRuntimeType(null).declType.toString(), //type
                        constant.toCode());//initialization value
            }
        }
    }

    /**
     * Insert "end" into the final position of the editor
     */
    public void fixGroupException(GroupingException e) {
        if (e.getExceptionTypes() == GroupingException.Type.UNFINISHED_BEGIN_END) {
            String text = "\nend";
            editable.getEditableText().insert(editable.length(), text); //insert

            //select the "end" token and show keyboard
            editable.setSelection(editable.length() - text.length() + 1, //don't selected newline character
                    editable.length());
            editable.showKeyboard();
        }
    }

    public void fixProgramNotFound() {
        editable.getEditableText().insert(editable.length(), "\nbegin\n    \nend.\n");
        editable.setSelection(editable.length() - "\nend.\n".length());
    }

    private class TextData {
        /**
         * content
         */
        CharSequence text;
        int offset;

        public TextData(CharSequence text, int offset) {
            this.text = text;
            this.offset = offset;
        }

        public CharSequence getText() {
            return text;
        }

        public void setText(CharSequence text) {
            this.text = text;
        }

        public int getOffset() {
            return offset;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        @Override
        public String toString() {
            return text + "\n" + "offset = " + offset;
        }
    }
}
