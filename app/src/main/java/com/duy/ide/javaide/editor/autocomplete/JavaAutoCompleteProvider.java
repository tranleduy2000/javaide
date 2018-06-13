/*
 * Copyright (C) 2018 Tran Le Duy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.duy.ide.javaide.editor.autocomplete;

import android.content.Context;
import android.support.annotation.IntDef;
import android.util.Log;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.duy.android.compiler.env.Environment;
import com.duy.android.compiler.project.JavaProject;
import com.duy.ide.code.api.SuggestItem;
import com.duy.ide.code.api.SuggestionProvider;
import com.duy.ide.editor.internal.suggestion.Editor;
import com.duy.ide.javaide.editor.autocomplete.internal.CompleteClassMember;
import com.duy.ide.javaide.editor.autocomplete.internal.PatternFactory;
import com.duy.ide.javaide.editor.autocomplete.internal.Patterns;
import com.duy.ide.javaide.editor.autocomplete.internal.StatementParser;
import com.duy.ide.javaide.editor.autocomplete.internal.completed.CompleteNewKeyword;
import com.duy.ide.javaide.editor.autocomplete.internal.completed.CompletePackage;
import com.duy.ide.javaide.editor.autocomplete.model.FieldDescription;
import com.duy.ide.javaide.editor.autocomplete.model.JavaSuggestItemImpl;
import com.duy.ide.javaide.editor.autocomplete.parser.IClass;
import com.duy.ide.javaide.editor.autocomplete.parser.IMethod;
import com.duy.ide.javaide.editor.autocomplete.parser.JavaClassManager;
import com.duy.ide.javaide.editor.autocomplete.parser.JavaDexClassLoader;
import com.duy.ide.javaide.editor.autocomplete.parser.JavaParser;
import com.duy.ide.javaide.editor.autocomplete.parser.PackageManager;
import com.google.common.collect.Lists;
import com.sun.tools.javac.tree.JCTree;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.SourceVersion;

import static com.duy.ide.javaide.editor.autocomplete.internal.JavaCompleteMatcherImpl.END_WITH_CHARACTER_OR_DOT;
import static com.duy.ide.javaide.editor.autocomplete.internal.JavaCompleteMatcherImpl.END_WITH_DOT;
import static com.duy.ide.javaide.editor.autocomplete.internal.JavaCompleteMatcherImpl.KEYWORD_DOT;
import static com.duy.ide.javaide.editor.autocomplete.internal.JavaCompleteMatcherImpl.VALID_WHEN_END_WITH_DOT;
import static com.duy.ide.javaide.editor.autocomplete.internal.PatternFactory.lastMatchStr;
import static com.duy.ide.javaide.editor.autocomplete.util.EditorUtil.getPossibleClassName;


/**
 * Created by Duy on 20-Jul-17.
 */

public class JavaAutoCompleteProvider implements SuggestionProvider {
    public static final int KIND_NONE = 0;
    public static final int KIND_PACKAGE = KIND_NONE + 1; //or import
    public static final int KIND_METHOD = KIND_PACKAGE + 1;
    public static final int KIND_IMPORT = KIND_METHOD + 1;
    public static final int KIND_MEMBER = KIND_IMPORT + 1;
    public static final int KIND_THIS = KIND_MEMBER + 1;
    public static final int KIND_SUPER = KIND_THIS + 1;
    public static final int KIND_BUILTIN_TYPE = KIND_SUPER + 1;

    public static final int CONTEXT_OTHER = 0;
    public static final int CONTEXT_AFTER_DOT = CONTEXT_OTHER + 1;
    public static final int CONTEXT_METHOD_PARAM = CONTEXT_AFTER_DOT + 2;
    public static final int CONTEXT_IMPORT = CONTEXT_METHOD_PARAM + 3;
    public static final int CONTEXT_IMPORT_STATIC = CONTEXT_IMPORT + 4;
    public static final int CONTEXT_PACKAGE_DECL = CONTEXT_IMPORT_STATIC + 6;
    public static final int CONTEXT_NEED_TYPE = CONTEXT_PACKAGE_DECL + 7;
    /**
     * Suggest class constructor
     */
    public static final int CONTEXT_NEED_CONSTRUCTOR = CONTEXT_NEED_TYPE + 1;

    private static final String TAG = "AutoCompleteProvider";
    /**
     * Support complete java constructor
     */
    private final CompleteNewKeyword mCompleteNewKeyword;
    private final CompleteClassMember mCompleteClassMember;
    private final CompletePackage mCompletePackage;

    private JavaDexClassLoader mClassLoader;
    private PackageManager mPackageManager;
    private JavaParser mJavaParser;

    @Nullable
    private JCTree.JCCompilationUnit unit;

    private String mDotExpr = ""; //expression end with .
    /**
     * incomplete word
     * 1. dotExpr.method(|)
     * 2. new className(|)
     * 3. dotExpr.ab|
     * 4. ja
     * 5. method(
     */
    private String mIcompleteWord = "";
    @ContextType
    private int mContextType = CONTEXT_OTHER;
    private Editor mEditor;

    public JavaAutoCompleteProvider(Context context) {
        File outDir = context.getDir("dex", Context.MODE_PRIVATE);
        mClassLoader = new JavaDexClassLoader(Environment.getClasspathFile(context), outDir);
        mPackageManager = new PackageManager();
        mJavaParser = new JavaParser();
        mCompleteNewKeyword = new CompleteNewKeyword(mClassLoader);
        mCompleteClassMember = new CompleteClassMember(mClassLoader);
        mCompletePackage = new CompletePackage(mPackageManager);
    }

    private void resolveContextType(Editor editor, String statement) {
        try {
            this.unit = mJavaParser.parse(editor.getText());
        } catch (Exception e) {
            this.unit = null;
        }

        //reset environment
        mDotExpr = "";
        mIcompleteWord = "";
        mContextType = CONTEXT_OTHER;

        statement = getStatement(editor);
        Log.d(TAG, "findStart statement = " + statement);
        if (END_WITH_CHARACTER_OR_DOT.matcher(statement).find()) {
            boolean isValid = true;
            if (END_WITH_DOT.matcher(statement).find()) {
                isValid = VALID_WHEN_END_WITH_DOT.matcher(statement).find()
                        &&
                        !KEYWORD_DOT.matcher(statement).find();
            }
            if (!isValid) {
                return;
            }
            mContextType = CONTEXT_AFTER_DOT;
        }
        //	" method parameters, treat methodname or 'new' as an incomplete word
        else if (Pattern.compile("\\(\\s*$").matcher(statement).find()) {
            //" TODO: Need to exclude method declaration?
            mContextType = CONTEXT_METHOD_PARAM;
            int pos = statement.lastIndexOf("(");
            statement = statement.replaceAll("\\s*\\(\\s*$", "");
            {
                //case expr.method(|)
                if (statement.charAt(pos - 1) == '.' &&
                        !Patterns.KEYWORDS.matcher(statement.substring(0, statement.lastIndexOf("."))).find()) {
                    mDotExpr = extractCleanExpr(statement.substring(0, statement.lastIndexOf(".")));
                    mIcompleteWord = statement.substring(statement.lastIndexOf(".") + 1);
                }
            }
        }
    }

    public ArrayList<SuggestItem> generateSuggestion() {
        System.out.println("contextType = " + mContextType);
        //" Return list of matches.
        //case: all is empty
        if (mDotExpr.isEmpty() && mIcompleteWord.isEmpty()) {
            return new ArrayList<>();
        }

        //the result
        ArrayList<SuggestItem> result = new ArrayList<>();

        if (!mDotExpr.isEmpty()) {
            switch (mContextType) {
                case CONTEXT_AFTER_DOT:
                    result = completExpression(mEditor.getText(), mDotExpr, mIcompleteWord);
                    break;
                case CONTEXT_IMPORT:
                case CONTEXT_IMPORT_STATIC:
                case CONTEXT_PACKAGE_DECL:
                case CONTEXT_NEED_TYPE:
                    result = getMember(mDotExpr, mIcompleteWord);
                    break;
            }
        }
        //only complete word
        else if (!mIcompleteWord.isEmpty()) {
            //only need method
            switch (mContextType) {
                default:
                    result = completeWord(mEditor.getText(), mIcompleteWord);
                    break;
            }
        }
        return result;
    }

    private ArrayList<SuggestItem> filter(ArrayList<SuggestItem> input, String incomplete) {
        ArrayList<SuggestItem> result = new ArrayList<>();
        for (SuggestItem s : input) {
            // TODO: 14-Aug-17 improve
            if (s.getName().contains(incomplete)) {
                result.add(s);
            }
        }
        return result;
    }

    /**
     * " Precondition:	incomplete must be a word without '.'.
     * " return all the matched, variables, fields, methods, types, packages
     */
    private ArrayList<SuggestItem> completeWord(String source, String incomplete) {
        if (incomplete.endsWith(" ")) {
            return new ArrayList<>();
        }
        incomplete = incomplete.trim();
        ArrayList<SuggestItem> result = new ArrayList<>();
        //parse current file
//        getPossibleResultInCurrentFile(result, unit, incomplete);
        List<IClass> classes = mClassLoader.findAllWithPrefix(incomplete);
        setInfo(classes);
        result.addAll(classes);

        Collections.sort(result, new Comparator<SuggestItem>() {
            @Override
            public int compare(SuggestItem o1, SuggestItem o2) {
                return Integer.valueOf(o1.getSuggestionPriority()).compareTo(o2.getSuggestionPriority());
            }
        });
        return result;
    }

    private void setInfo(List<? extends SuggestItem> items) {
        for (SuggestItem item : items) {
            if (item instanceof JavaSuggestItemImpl) {
                ((JavaSuggestItemImpl) item).setEditor(mEditor);
                ((JavaSuggestItemImpl) item).setIncomplete(mIcompleteWord);
            }
        }
    }


    private ArrayList<SuggestItem> completeMethodParams(String incomplete) {
        return new ArrayList<>();
    }

    @NonNull
    private ArrayList<SuggestItem> getConstructors(Editor editor, String incomplete) {
        ArrayList<SuggestItem> result = new ArrayList<>();
        mCompleteNewKeyword.getSuggestion(editor, incomplete, result);
        return result;
    }

    /**
     * get member of class name, package ...
     * e.g.
     * java.lang.String.to|
     * <p>                ^ cursor
     * return toString, toCharArray, toLowerCase,... etc
     * +      ^^        ^^           ^^
     *
     * @param prefix     - the expression end with ".", it can be a full class name
     *                   or empty (java.lang.* classes)
     * @param incomplete - incomplete word
     */
    @NonNull
    private ArrayList<SuggestItem> getMember(@NonNull String prefix, @NonNull String incomplete) {
        System.out.println("JavaAutoCompleteProvider.getMember");
        ArrayList<SuggestItem> result = new ArrayList<>();
        //get class member
        mCompleteClassMember.getSuggestion(mEditor, prefix + incomplete, result);
        //package members
        mCompletePackage.getSuggestion(mEditor, prefix + incomplete, result);
        return result;
    }

    /**
     * " Precondition:	expr must end with '.'
     * " return members of the value of expression
     */
    private ArrayList<SuggestItem> completExpression(String source, String dotExpr, String incomplete) {
        ArrayList<String> items = parseExpr(dotExpr);
        if (items.size() == 0) {
            return new ArrayList<>();
        }

        //0. String literal
        if (items.get(items.size() - 1).matches("\"$")) {
            return getMember(dotExpr, String.class.getName());
        }

        ArrayList<SuggestItem> result = new ArrayList<>();
        int ii = 1; //item index;
        @ItemKind
        int itemKind = 0;

        /**
         " optimized process
         " search the longest expr consisting of ident
         */
        int i = 0, k = 0;
        while (i < items.size() && Pattern.compile("^\\s*" + Patterns.IDENTIFIER + "\\s*$").matcher(items.get(i)).find()) {
            String ident = items.get(i).replaceAll("\\s", "");
            if (ident.equals("class") || ident.equals("this") || ident.equals("super")) {
                k = i;
            }
            // " return when found other keywords
            else if (isKeyword(ident)) {
                return new ArrayList<>();
            }
            items.set(i, items.get(i).replaceAll("\\s", ""));
            i++;
        }

        if (i > 0) {
            //  " cases: "this.|", "super.|", "ClassName.this.|", "ClassName.super.|", "TypeName.class.|"
            String itemAtK = items.get(k);
            if (itemAtK.equals("class") || itemAtK.equals("this") || itemAtK.equals("super")) {
                result = getClassMembers(
                        itemAtK.equals("class") ? "java.lang.Class" : join(items, 0, k, "."),
                        incomplete);
                if (!result.isEmpty()) {
                    itemKind = !itemAtK.equals("this") ? KIND_THIS : !itemAtK.equals("super") ? KIND_SUPER : KIND_NONE;
                    ii = k + 1;
                }
            }
            //   " case: "java.io.File.|"
            else {
                String className = join(items, 0, i - 1, ".");
                result = getStaticAccess(className);
            }
        }

        //"
        //" first item
        //"
        if (result.isEmpty()) {
            // cases:
            // 1) "int.|", "void.|"	- primitive type or pseudo-type, return `class`
            // 2) "this.|", "super.|"	- special reference
            // 3) "var.|"		- variable or field
            // 4) "String.|" 		- type imported or defined locally
            // 5) "java.|"   		- package
            if (Pattern.compile("^\\s*" + Patterns.IDENTIFIER + "\\s*").matcher(items.get(0)).find()) {
                String ident = items.get(0).replaceAll("\\s", "");
                if (SourceVersion.isKeyword(ident)) {
                    // 1)
                    if (ident.equals("void") || isBuiltinType(ident)) {
                        result = getClassMembers(int.class.getName(), incomplete);
                        itemKind = KIND_BUILTIN_TYPE;
                    }
                    // 2)
                    else if (ident.equals("this") || ident.equals("super")) {
                        itemKind = ident.equals("this") ? KIND_THIS : ident.equals("super") ? KIND_SUPER : KIND_NONE;
                        result = getClassMembers(ident, incomplete);
                    }
                } else {
                    // 3)
                    String typeName = getDeclaredClassName(source, ident);
                    if (!typeName.isEmpty()) {
                        if (typeName.charAt(0) == '[' && typeName.charAt(typeName.length() - 1) == ']') {
                            result = getClassMembers(Object[].class.getName(), incomplete);
                        } else if (!typeName.equals("void") && !isBuiltinType(typeName)) {
                            result = getClassMembers(typeName, incomplete);
                        }
                    } else { //typeName is empty
                        // 4) TypeName.|
                        result = getClassMembers(ident, incomplete);
                        itemKind = KIND_MEMBER;

                        // 5) package
                        if (result.isEmpty()) {
                            result = getMember(dotExpr, incomplete);
                            itemKind = KIND_PACKAGE;
                        }
                    }
                }
            }
            //" method invocation:	"method().|"	- "this.method().|"
            else if (Pattern.compile("^\\s*" + Patterns.IDENTIFIER + "\\s*\\(").matcher(items.get(0)).find()) {
                result = methodInvocation(items.get(0), result, itemKind);
            }
            //" array type, return `class`: "int[] [].|", "java.lang.String[].|", "NestedClass[].|"
            else if (items.get(0).matches(Patterns.RE_ARRAY_TYPE.toString())) {
                Matcher matcher = Patterns.RE_ARRAY_TYPE.matcher(items.get(0));
                if (matcher.find()) {
                    String qid = matcher.group(1); //class name
                    if (isBuiltinType(qid) || (!isKeyword(qid) && !getClassMembers(qid, incomplete).isEmpty())) {
                        result = getClassMembers(int.class.getName(), incomplete);
                        itemKind = KIND_MEMBER;
                    }
                }
            }
            //" class instance creation expr:	"new String().|", "new NonLoadableClass().|"
//            " array creation expr:	"new int[i=1] [val()].|", "new java.lang.String[].|"
            else if (Pattern.compile("^new\\s+").matcher(items.get(0)).find()) {
                String clean = items.get(0).replaceAll("^new\\s+", "");
                clean = clean.replaceAll("\\s", "");
                Pattern compile = Pattern.compile("(" + Patterns.RE_QUALID + ")\\s*([(\\[])");
                Matcher matcher = compile.matcher(clean);
                if (matcher.find()) {
                    if (matcher.group(2).charAt(0) == '[') {
                        result = getClassMembers(int[].class.getName(), incomplete);
                    } else if (matcher.group(2).charAt(0) == '(') {
                        result = getClassMembers(matcher.group(1), incomplete);
                    }
                }
            }
            // " casting conversion:	"(Object)o.|"
            else if (Patterns.RE_CASTING.matcher(items.get(0)).find()) {
                Matcher matcher = Patterns.RE_CASTING.matcher(items.get(0));
                if (matcher.find()) {
                    result = getClassMembers(matcher.group(1), incomplete);
                }
            }
            //" array access:	"var[i][j].|"		Note: "var[i][]" is incorrect
            else if (Patterns.RE_ARRAY_ACCESS.matcher(items.get(0)).find()) {
                Matcher matcher = Patterns.RE_ARRAY_ACCESS.matcher(items.get(0));
                matcher.find();
                String typeName = matcher.group(1);
                typeName = getDeclaredClassName(source, typeName);
                if (!typeName.isEmpty()) {
                    result = arrayAccess(typeName, items.get(0));
                }
            }
        }


        /*
         * next items
         */
        while (!result.isEmpty() && ii < items.size()) {
            // method invocation:	"PrimaryExpr.method(parameters)[].|"
            if (Pattern.compile("^\\s*" + Patterns.IDENTIFIER + "\\s*\\(").matcher(items.get(ii)).find()) {
                Log.d(TAG, "completeAfterDot: RE_IDENTIFIER ( ");
                result = methodInvocation(items.get(ii), result, itemKind);
                itemKind = KIND_NONE;
                ii++;
                continue;
            }
            //" expression of selection, field access, array access
            else if (Patterns.RE_SELECT_OR_ACCESS.matcher(items.get(ii)).find()) {
                Log.d(TAG, "completeAfterDot: RE_SELECT_OR_ACCESS ");
                Matcher matcher = Patterns.RE_SELECT_OR_ACCESS.matcher(items.get(ii));
                matcher.find();
                String ident = matcher.group(1);
                String bracket = matcher.group(2);
                if (itemKind == KIND_PACKAGE && bracket.isEmpty() && !isKeyword(ident)) {

                }
                //" type members
                else if (itemKind == KIND_MEMBER && bracket.isEmpty()) {
                    if (ident.equals("class") || ident.equals("this") || ident.equals("super")) {
                        result = getClassMembers(ident.equals("class") ?
                                "java.lang.Class" : join(items, 0, ii - 1, "."), incomplete);
                        itemKind = ident.equals("this") ? KIND_THIS : ident.equals("super") ? KIND_SUPER : KIND_NONE;
                    } else if (!isKeyword(ident) /*&& type == class*/) {
                        //accessible static field
                        //result = get info of stattic field
                    }
                }
            }
        }
        return filter(result, incomplete);
    }

    private ArrayList<SuggestItem> arrayAccess(String typeName, String s) {
        return null;
    }

    private ArrayList<SuggestItem> methodInvocation(String s, ArrayList<SuggestItem> ti, int itemKind) {
        return ti;
    }

    private String getDeclaredClassName(String src, String ident) {
        ident = ident.trim();
        if (Pattern.compile("this|super").matcher(ident).find()) {
            return ident; //TODO Return current class
        }
        /*
         " code sample:
         " String tmp; java.
         " 	lang.  String str, value;
         " for (int i = 0, j = 0; i < 10; i++) {
         "   j = 0;
         " }
         */
        int pos = mEditor.getCursor();
        int start = Math.max(0, pos - 2500);
        String range = src.substring(start, pos);

        //BigInteger num = new BigInteger(); -> BigInteger num =
        String instance = lastMatchStr(range, PatternFactory.makeInstance(ident));
        if (instance != null) {
            //BigInteger num =  -> BigInteger
            instance = instance.replaceAll("(\\s?)(" + ident + ")(\\s?[,;=)])", "").trim(); //clear name
            //generic ArrayList<String> -> ArrayList
            ident = instance.replaceAll("<.*>", ""); //clear generic

            ArrayList<String> possibleClassName = getPossibleClassName(src, ident, "");
            for (String className : possibleClassName) {
                IClass clazz = mClassLoader.getClassReader().getParsedClass(className);
                if (clazz != null) {
                    return className;
                }
            }
        }
        return "";
    }

    private ArrayList<SuggestItem> getVariableDeclaration() {
        return new ArrayList<>();
    }

    private boolean isBuiltinType(String ident) {
        return Patterns.PRIMITIVE_TYPES.matcher(ident).find();
    }

    private boolean isKeyword(String ident) {
        return Patterns.RE_KEYWORDS.matcher(ident).find();
    }

    @NonNull
    private ArrayList<SuggestItem> getStaticAccess(String className) {
        IClass clazz = mClassLoader.getClassReader().getParsedClass(className);
        ArrayList<SuggestItem> result = new ArrayList<>();
        if (clazz != null) {
            for (FieldDescription fieldDescription : clazz.getFields()) {
                if (Modifier.isStatic(fieldDescription.getModifiers())
                        && Modifier.isPublic(fieldDescription.getModifiers())) {
                    result.add(fieldDescription);
                }
            }
            for (IMethod method : clazz.getMethods()) {
                if (Modifier.isStatic(method.getModifiers())
                        && Modifier.isPublic(method.getModifiers())) {
//                    result.add(method);
                }
            }
        }
        return result;
    }

    private ArrayList<SuggestItem> getClassMembers(String fullClassName, String incomplete) {
        ArrayList<SuggestItem> descriptions = new ArrayList<>();
        JavaClassManager classReader = mClassLoader.getClassReader();
        IClass clazz = classReader.getParsedClass(fullClassName);
        if (clazz != null) {
            List<SuggestItem> members = clazz.getMember(incomplete);
            setInfo(members);
            descriptions.addAll(members);
        }
        return descriptions;
    }

    private String join(ArrayList<String> items, int start, int end, String s) {
        List<String> strings = items.subList(start, end + 1);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < strings.size(); i++) {
            result.append(strings.get(i)).append(i == strings.size() - 1 ? "" : s);
        }
        return result.toString();
    }

    private ArrayList<String> parseExpr(String expr) {
        ArrayList<String> items = new ArrayList<>();
        // TODO: 16-Aug-17 improve
        if (true) {
            expr = expr.trim();
            String[] split = expr.trim().split(".");
            if (split.length > 0) {
                for (String s : split) {
                    items.add(s);
                }
            } else {
                items.add(expr.contains(".") ? expr.substring(0, expr.indexOf(".")) : expr);
            }
            return items;
        }

        //recognize ClassInstanceCreationExpr as a whole
        //case: new String() , new int[]  , new char []
        Matcher matcher = Pattern.compile("^\\s*new\\s+" + Patterns.RE_QUALID + "\\s*[(\\]]").matcher(expr);
        int e = -1;
        if (matcher.find()) {
            e = matcher.end() - 1;
            Log.i(TAG, "parseExpr: found instance at " + matcher.group());
        }
        if (e < 0) {//not found instance
            matcher = Pattern.compile("[.(\\[]").matcher(expr); //(String) str, ((Char) c)
            if (matcher.find()) {
                e = matcher.start();
            }
            Log.i(TAG, "parseExpr: not found instance, but found " + matcher.group());
        }

        int last = 0;
        boolean isParen = false;
        while (e >= 0) { //found . or ( or [
            if (expr.charAt(e) == '.') { //found .
                String subExpr = expr.substring(last, e);
                Log.i(TAG, "parseExpr: found . with " + subExpr);
                items.addAll(isParen ? processParentheses(subExpr) : Lists.newArrayList(subExpr));
                isParen = false;
                last = e + 1;
            } else if (expr.charAt(e) == '(') {
                Log.i(TAG, "parseExpr: found ( with");
                e = getMatchIndexEnd(expr, e, '(', ')');
                isParen = true;
                if (e < 0) {
                    break;
                } else {
                    Pattern pattern = Pattern.compile("^\\s*[.\\[]");
                    matcher = pattern.matcher(expr);
                    if (matcher.find(e + 1)) {
                        e = matcher.end() - 1;
                        continue;
                    }
                }
            } else if (expr.charAt(e) == '[') {
                Log.d(TAG, "parseExpr: end with [");
                e = getMatchIndexEnd(expr, e, '[', ']');
                if (e < 0) {
                    break;
                } else {
                    Pattern pattern = Pattern.compile("^\\s*[.\\[]");
                    matcher = pattern.matcher(expr);
                    if (matcher.find(e + 1)) {
                        e = matcher.end() - 1;
                        continue;
                    }
                }
            }
            matcher = Pattern.compile("[.(\\[]").matcher(expr);
            if (matcher.find(last)) {
                e = PatternFactory.matchEnd(expr, Pattern.compile("[.(\\[]"), last);
            } else {
                e = -1;
            }
        }
        String tail = expr.substring(last);
        if (!tail.trim().isEmpty()) {//is empty
            items.addAll(isParen ? processParentheses(tail) : Lists.newArrayList(tail));
        }
        return items;
    }

    private int getMatchIndexEnd(String expr, int start, char open, char close) {
        return 0;
    }

    //" Given optional argument, call s:ParseExpr() to parser the nonparentheses expr
    private ArrayList<String> processParentheses(String expr) {
        Pattern pattern = Pattern.compile("^\\s*\\(");
        Matcher matcher = pattern.matcher(expr);
        int s;
        if (matcher.find()) {
            s = matcher.end();
        } else {
            s = -1;
        }
        if (s != -1) {
            int e = getMatchedIndexEx(expr, s - 1, '(', ')');
            if (e >= 0) {
                String tail = expr.substring(e + 1);
                if (Pattern.compile("^\\s*\\[").matcher(tail).find()) {

                }
            }
        }
        return new ArrayList<>();
    }

    /**
     * " TODO: search pair used in string, like
     * " 	'create(ao.fox("("), new String).foo().'
     */
    private int getMatchedIndexEx(String str, int index, char open, char close) {
        int count = 1;
        if (str.charAt(index) != open) {
            return -1;
        }
        int i = 0;
        while (i < str.length()) {
            if (str.charAt(i) == open) {
                count++;
            } else if (str.charAt(i) == close) {
                count--;
                if (count == 0) {
                    return i;
                }
            }
            i++;
        }
        return -1;
    }

    private String extractCleanExpr(String statement) {
        return statement.replaceAll("[\n\t\r]", "");
    }

    /**
     * " Search back from the cursor position till meeting '{' or ';'.
     * " '{' means statement start, ';' means end of a previous statement.
     *
     * @return statement before cursor
     * " Note: It's the base for parsing. And It's OK for most cases.
     */
    @NonNull
    private String getStatement(Editor editor) {
        return new StatementParser().resolveStatementFromCursor(editor);
    }

    public void load(JavaProject projectFile) {
        mClassLoader.loadAllClasses(projectFile);
        mPackageManager.init(projectFile, mClassLoader.getClassReader());
    }

    @Override
    public ArrayList<SuggestItem> getSuggestions(Editor editor) {
        try {
            mEditor = editor;
            String statement = getStatement(editor);
            this.resolveContextType(editor, statement);
            ArrayList<SuggestItem> complete = generateSuggestion();
            return complete;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }


    @Retention(RetentionPolicy.SOURCE)
    @IntDef({CONTEXT_AFTER_DOT, CONTEXT_METHOD_PARAM, CONTEXT_IMPORT, CONTEXT_IMPORT_STATIC,
            CONTEXT_PACKAGE_DECL, CONTEXT_NEED_TYPE, CONTEXT_OTHER, CONTEXT_NEED_CONSTRUCTOR})
    public @interface ContextType {
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({KIND_PACKAGE, KIND_METHOD, KIND_IMPORT, KIND_MEMBER, KIND_THIS, KIND_SUPER,
            KIND_BUILTIN_TYPE, KIND_NONE})
    public @interface ItemKind {
    }
}
