package com.duy.ide.autocomplete;

import android.content.Context;
import android.support.annotation.IntDef;
import android.util.Log;
import android.widget.EditText;

import com.android.annotations.NonNull;
import com.duy.ide.autocomplete.autocomplete.AutoCompletePackage;
import com.duy.ide.autocomplete.autocomplete.PackageImporter;
import com.duy.ide.autocomplete.autocomplete.PatternFactory;
import com.duy.ide.autocomplete.dex.JavaClassReader;
import com.duy.ide.autocomplete.dex.JavaDexClassLoader;
import com.duy.ide.autocomplete.model.ClassDescription;
import com.duy.ide.autocomplete.model.ConstructorDescription;
import com.duy.ide.autocomplete.model.Description;
import com.duy.ide.autocomplete.model.FieldDescription;
import com.duy.ide.autocomplete.model.Member;
import com.duy.ide.autocomplete.model.MethodDescription;
import com.duy.ide.autocomplete.util.EditorUtil;
import com.duy.ide.file.FileManager;
import com.duy.project.file.java.JavaProjectFolder;
import com.google.common.collect.Lists;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.SourceVersion;

import static com.duy.ide.autocomplete.dex.JavaClassManager.determineClassName;
import static java.util.regex.Pattern.compile;
import static javax.lang.model.SourceVersion.isKeyword;


/**
 * Created by Duy on 20-Jul-17.
 */

public class AutoCompleteProvider {
    private static final int CONTEXT_AFTER_DOT = 1;
    private static final int CONTEXT_METHOD_PARAM = 2;
    private static final int CONTEXT_IMPORT = 3;
    private static final int CONTEXT_IMPORT_STATIC = 4;
    private static final int CONTEXT_PACKAGE_DECL = 6;
    private static final int CONTEXT_NEED_TYPE = 7;
    private static final int CONTEXT_OTHER = 0;

    private static final String TAG = "AutoCompleteProvider";
    private JavaDexClassLoader mClassLoader;
    private PackageImporter packageImporter;
    private AutoCompletePackage completePackage;

    private String statement = ""; //statement before cursor
    private String dotExpr = ""; //expression end with .
    /**
     * incomplete word
     * 1. dotExpr.method(|)
     * 2. new className(|)
     * 3. dotExpr.ab|
     * 4. ja
     * 5. method(
     */
    private String incomplete = "";
    private String errorMsg;
    @ContextType
    private int contextType = CONTEXT_OTHER;
    private String padding;


    public AutoCompleteProvider(Context context) {
        File outDir = context.getDir("dex", Context.MODE_PRIVATE);
        mClassLoader = new JavaDexClassLoader(FileManager.getClasspathFile(context), outDir);
    }

    /**
     * find start position incomplete
     */
    private int findStart(EditText editor) {
        int selectionStart = editor.getSelectionStart();
        Pattern pattern;
        //reset environment
        dotExpr = "";
        incomplete = "";
        contextType = CONTEXT_OTHER;

        statement = getStatement(editor); //ok
        Log.d(TAG, "findStart statement = " + statement);
        if (compile("[.0-9A-Za-z_]\\s*$").matcher(statement).find()) {
            boolean valid = true;
            if (statement.matches("\\.\\s*$")) {
                valid = statement.matches("[\")0-9A-Za-z_\\]]\\s*\\.\\s*$")
                        && !statement.matches("\\H\\w\\+\\.\\s*$")
                        && !statement.matches("(" + Patterns.RE_KEYWORDS.toString() + ")\\.\\s*");
            }
            if (!valid) {
                return -1;
            }
            contextType = CONTEXT_AFTER_DOT;
            //import or package declaration
            if (compile("^\\s*(import|package)\\s+").matcher(statement).find()) {
                statement = statement.replaceAll("\\s+\\.", ".");
                statement = statement.replaceAll("\\.\\s+", ".");
                if (compile("^\\s*(import)\\s+").matcher(statement).find()) {
                    //static import
                    if (compile("^\\s*(import)\\s+(static)\\s+").matcher(statement).find()) {
                        contextType = CONTEXT_IMPORT_STATIC;
                    } else { //normal import
                        contextType = CONTEXT_IMPORT;
                    }
                    Pattern importStatic = compile("^\\s*(import)\\s+(static\\s+)?");
                    Matcher matcher = importStatic.matcher(statement);
                    if (matcher.find()) {
                        dotExpr = statement.substring(matcher.end() + 1);
                    }
                } else {
                    contextType = CONTEXT_PACKAGE_DECL;
                    Pattern _package = compile("^\\s*(package)\\s+?");
                    Matcher matcher = _package.matcher(statement);
                    if (matcher.find()) {
                        dotExpr = statement.substring(matcher.end());
                    }
                }
            }
            //String literal
            else if (compile("\"\\s*\\.\\s*$").matcher(statement).find()) {
                dotExpr = statement.replaceAll("\\s*\\.\\s*$", ".");
                return selectionStart - incomplete.length();
            }
            //" type declaration		NOTE: not supported generic yet.
            else {
                Matcher matcher = compile("^\\s*" + Patterns.RE_TYPE_DECL).matcher(statement);
                if (matcher.find()) {
                    dotExpr = statement.substring(matcher.start());
                    if (!compile("^\\s*(extend|implements)\\s+").matcher(dotExpr).find()) {
                        // TODO: 13-Aug-17 suggest class
                        return -1;
                    }
                    contextType = CONTEXT_NEED_TYPE;
                }
                dotExpr = extractCleanExpr(statement);
            }

            //" all cases: " java.ut|" or " java.util.|" or "ja|"
            if (dotExpr.contains(".")) {
                incomplete = dotExpr.substring(dotExpr.lastIndexOf(".") + 1);
                dotExpr = dotExpr.substring(0, dotExpr.lastIndexOf("."));
            }
            //incomplete
            return selectionStart - incomplete.length();
        }
        //	" method parameters, treat methodname or 'new' as an incomplete word
        else if (compile("\\(\\s*$").matcher(statement).find()) {
            //" TODO: Need to exclude method declaration?
            contextType = CONTEXT_METHOD_PARAM;
            int pos = statement.lastIndexOf("(");
            padding = statement.substring(pos + 1);
            selectionStart = selectionStart - (statement.length() - pos);
            statement = statement.replaceAll("\\s*\\(\\s*$", "");
            //" new ClassName?

            if (compile("^\\s*new\\s+" + Patterns.RE_QUALID + "$").matcher(statement).find()) {
                statement = statement.replaceAll("^\\s*new\\s+", "");
                if (!Patterns.KEYWORDS.matcher(statement).find()) {
                    incomplete = "+";
                    dotExpr = statement;
                    return selectionStart - dotExpr.length();
                }
            } else { //in case \(\s*$
                Matcher matcher = compile("\\s*" + Patterns.RE_IDENTIFIER + "$").matcher(statement);
                matcher.find();
                pos = matcher.start();
                //case: "method(|)", "this(|)", "super(|)"
                if (pos == 0) {
                    statement = statement.replaceAll("^\\s*", "");
                    //treat "this" or "super" as a type name
                    if (statement.equals("this") || statement.equals("supper")) {
                        dotExpr = statement;
                        incomplete = "+";
                        return selectionStart - dotExpr.length();
                    } else if (!Patterns.KEYWORDS.matcher(statement).find()) {
                        incomplete = statement;
                        return selectionStart - incomplete.length();
                    }
                }
                //case expr.method(|)
                else if (statement.charAt(pos - 1) == '.' &&
                        !Patterns.KEYWORDS.matcher(statement.substring(0, statement.lastIndexOf("."))).find()) {
                    dotExpr = extractCleanExpr(statement.substring(0, statement.lastIndexOf(".")));
                    incomplete = statement.substring(statement.lastIndexOf(".") + 1);
                    return selectionStart - incomplete.length();
                }
            }
        }
        return -1;
    }

    public ArrayList<? extends Description> complete(EditText editor) {
        //" Return list of matches.
        //case: all is empty
        if (dotExpr.matches("^\\s*$") && incomplete.matches("^\\s*$")) {
            return new ArrayList<>();
        }
        //the result
        ArrayList<? extends Description> result = new ArrayList<>();

        if (!dotExpr.matches("^\\s*$")) { //if not empty
            if (contextType == CONTEXT_AFTER_DOT) {
                result = completeAfterDot(dotExpr);
            } else if (contextType == CONTEXT_IMPORT || contextType == CONTEXT_IMPORT_STATIC
                    || contextType == CONTEXT_PACKAGE_DECL || contextType == CONTEXT_NEED_TYPE) {
                result = getMember(dotExpr);
            } else if (contextType == CONTEXT_METHOD_PARAM) {
                if (incomplete.equals("+")) {
                    result = getConstructorList(dotExpr);
                } else {
                    result = completeAfterDot(dotExpr);
                }
            }
        }


        //only complete word
        else if (!incomplete.matches("^\\s*$")) {
            //only need method
            if (contextType == CONTEXT_METHOD_PARAM) {
                result = searchForName(incomplete);
            } else {
                result = completeAfterWord(editor, incomplete);
            }
        }
        incomplete = "";
        if (result.size() > 0) {
            //  " filter according to b:incomplete
            if (incomplete.length() > 0 && !incomplete.equals("+")) {
                result = filter(result, incomplete);
            }
        }
        return result;
    }

    private ArrayList<Description> filter(ArrayList<? extends Description> input, String incomplete) {
        ArrayList<Description> result = new ArrayList<>();
        for (Description s : input) {
            // TODO: 14-Aug-17 improve
            if (s.getName().startsWith(incomplete)) {
                result.add(s);
            }
        }
        return result;
    }

    /**
     * " Precondition:	incomplete must be a word without '.'.
     * " return all the matched, variables, fields, methods, types, packages
     */
    private ArrayList<? extends Description> completeAfterWord(EditText editor, String incomplete) {
        ArrayList<Description> result = new ArrayList<>();
        //todo all variable
        // TODO: 14-Aug-17 field
        // TODO: 14-Aug-17 method
        // TODO: 14-Aug-17 type
        // TODO: 14-Aug-17 package
        if (contextType != CONTEXT_PACKAGE_DECL) {
//            // TODO: 14-Aug-17 add import current file
//
//            //current file declare
//            ArrayList<String> list = PatternFactory.allMatch(editor.getText().toString(),
//                    Pattern.compile("(class|enum|interface)[\\s+\t\r\n]" + incomplete + "[a-zA-Z0-9_]*"));
//            result.addAll(list);
//
//            // TODO: 14-Aug-17 uses java parser parse all file and get field, member, method public
//
//            // TODO find all class, get public meber
//            ArrayList aClass = mClassLoader.findClass(incomplete);

        }
        // TODO: 14-Aug-17 sort by field -> method
        return result;
    }

    private ArrayList<? extends Description> searchForName(String incomplete) {
        return new ArrayList<>();
    }

    @NonNull
    private ArrayList<ConstructorDescription> getConstructorList(String className) {
        ArrayList<ClassDescription> classes = mClassLoader.findClass(className);
        ArrayList<ConstructorDescription> constructors = new ArrayList<>();
        for (ClassDescription c : classes) {
            constructors.addAll(c.getConstructors());
        }
        return constructors;
    }

    @NonNull
    private ArrayList<Description> getMember(String className) {
        ArrayList<ClassDescription> classes = mClassLoader.findClass(className);
        ArrayList<Description> constructors = new ArrayList<>();
        for (ClassDescription c : classes) {
            constructors.addAll(c.getMember(""));
        }
        return constructors;
    }

    /**
     * " Precondition:	expr must end with '.'
     * " return members of the value of expression
     */
    private ArrayList<? extends Description> completeAfterDot(String dotExpr) {
        ArrayList<String> items = parseExpr(dotExpr);
        if (items.size() == 0) {
            return new ArrayList<>();
        }

        //0. String literal
        if (items.get(items.size() - 1).matches("\"$")) {
            return getMember(String.class.getName());
        }

        ArrayList<? extends Description> ti = new ArrayList<>();
        int ii = 1; //item index;
        int itemKind = 0;

        /**
         " optimized process
         " search the longest expr consisting of ident
         */
        int i = 1, k = 1;
        while (i < items.size() && items.get(i).matches("^\\s*" + Patterns.RE_IDENTIFIER + "\\s*$")) {
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
        if (i > 1) {
            //  " cases: "this.|", "super.|", "ClassName.this.|", "ClassName.super.|", "TypeName.class.|"
            String itemAtK = items.get(k);
            if (!itemAtK.equals("class") || !itemAtK.equals("this") || !itemAtK.equals("super")) {
                ti = doGetClassInfo(itemAtK.equals("class") ? "java.lang.Class" : join(items, 0, k - 1, "."));
                if (!ti.isEmpty()) {
                    itemKind = !itemAtK.equals("this") ? 1 : !itemAtK.equals("super") ? 2 : 0;
                    ii = k + 1;
                }
            }
            //   " case: "java.io.File.|"
            else {
                String className = join(items, 0, i - 1, ".");
                ti = getStaticAccess(className);
            }
        }

        //"
        //" first item
        //"
        if (ti.isEmpty()) {
            // cases:
            // 1) "int.|", "void.|"	- primitive type or pseudo-type, return `class`
            // 2) "this.|", "super.|"	- special reference
            // 3) "var.|"		- variable or field
            // 4) "String.|" 		- type imported or defined locally
            // 5) "java.|"   		- package
            if (items.get(0).matches("^\\s*" + Patterns.RE_IDENTIFIER + "\\s*")) {
                String ident = items.get(0).replaceAll("\\s", "");
                if (SourceVersion.isKeyword(ident)) {
                    // 1)
                    if (ident.equals("void") || isBuiltinType(ident)) {
                        ti = doGetClassInfo(int.class.getName());
                        itemKind = 11;
                    }
                    // 2)
                    else if (ident.equals("this") || ident.equals("super")) {
                        itemKind = ident.equals("this") ? 1 : ident.equals("super") ? 2 : 0;
                        ti = doGetClassInfo(ident);
                    }
                } else {
                    // 3)
                    String typeName = getDelaredClassName(ident);
                    if (!typeName.isEmpty()) {
                        if (typeName.charAt(0) == '[' && typeName.charAt(typeName.length() - 1) == ']') {
                            ti = doGetClassInfo(Object[].class.getName());
                        } else if (!typeName.equals("void") && !isBuiltinType(typeName)) {
                            ti = doGetClassInfo(typeName);
                        }
                    } else {
                        // 4)
                        ti = doGetClassInfo(typeName);
                        itemKind = 11;

                        // 5)
                        if (ti.isEmpty()) {
                            ti = getMember(ident);
                            itemKind = 20;
                        }
                    }
                }
            }
            //" method invocation:	"method().|"	- "this.method().|"
            else if (items.get(0).matches("^\\s*" + Patterns.RE_IDENTIFIER + "\\s*\\(")) {
                ti = methodInvokecation(items.get(0), ti, itemKind);
            }
            //" array type, return `class`: "int[] [].|", "java.lang.String[].|", "NestedClass[].|"
            else if (items.get(0).matches(Patterns.RE_ARRAY_TYPE.toString())) {
                Matcher matcher = Patterns.RE_ARRAY_TYPE.matcher(items.get(0));
                if (matcher.find()) {
                    String qid = matcher.group(1); //class name
                    if (isBuiltinType(qid) || (!isKeyword(qid) && !doGetClassInfo(qid).isEmpty())) {
                        ti = doGetClassInfo(int.class.getName());
                        itemKind = 11;
                    }
                }
            }
            //" class instance creation expr:	"new String().|", "new NonLoadableClass().|"
//            " array creation expr:	"new int[i=1] [val()].|", "new java.lang.String[].|"
            else if (items.get(0).matches("^new\\s+")) {
                String clean = items.get(0).replaceAll("^new\\s+", "");
                clean = clean.replaceAll("\\s", "");
                Pattern compile = compile("(" + Patterns.RE_QUALID + ")\\s*([(\\[])");
                Matcher matcher = compile.matcher(clean);
                if (matcher.find()) {
                    if (matcher.group(2).charAt(0) == '[') {
                        ti = doGetClassInfo(int[].class.getName());
                    } else if (matcher.group(2).charAt(0) == '(') {
                        ti = doGetClassInfo(matcher.group(1));
                    }
                }
            }
            // " casting conversion:	"(Object)o.|"
            else if (items.get(0).matches(Patterns.RE_CASTING.toString())) {
                Matcher matcher = Patterns.RE_CASTING.matcher(items.get(0));
                if (matcher.find()) {
                    ti = doGetClassInfo(matcher.group(1));
                }
            }
            //" array access:	"var[i][j].|"		Note: "var[i][]" is incorrect
            else if (items.get(0).matches(Patterns.RE_ARRAY_ACCESS.toString())) {
                Matcher matcher = Patterns.RE_ARRAY_ACCESS.matcher(items.get(0));
                matcher.find();
                String typeName = matcher.group(1);
                typeName = getDelaredClassName(typeName);
                if (!typeName.isEmpty()) {
                    ti = arrayAcesss(typeName, items.get(0));
                }
            }
        }

        /**
         * next items
         */
        return ti;
    }

    private ArrayList<? extends Description> arrayAcesss(String typeName, String s) {
        return null;
    }

    private String substitute(String input, Pattern pattern, int group, String replaceBy) {
        StringBuilder stringBuilder = new StringBuilder(input);
        Matcher matcher = pattern.matcher(stringBuilder);
        if (matcher.find()) {
            stringBuilder.replace(matcher.start(), matcher.end(), matcher.group(group));
        }
        return null;
    }

    private ArrayList<? extends Description> methodInvokecation(String s, ArrayList<? extends Description> ti, int itemKind) {
        return ti;
    }

    private String getDelaredClassName(String ident) {
        return null;
    }

    private boolean isBuiltinType(String ident) {
        return false;
    }

    @NonNull
    private ArrayList<? extends Description> getStaticAccess(String className) {
        ClassDescription classDescription = mClassLoader.getClassReader().readClassByName(className, null);
        ArrayList<Description> result = new ArrayList<>();
        if (classDescription != null) {
            for (FieldDescription fieldDescription : classDescription.getFields()) {
                if (Modifier.isStatic(fieldDescription.getModifiers())
                        && Modifier.isPublic(fieldDescription.getModifiers())) {
                    result.add(fieldDescription);
                }
            }
            for (MethodDescription methodDescription : classDescription.getMethods()) {
                if (Modifier.isStatic(methodDescription.getModifiers())
                        && Modifier.isPublic(methodDescription.getModifiers())) {
                    result.add(methodDescription);
                }
            }
        }
        return result;
    }

    private ArrayList<Description> doGetClassInfo(String className) {
        ArrayList<Description> descriptions = new ArrayList<>();
        ClassDescription classDescription = mClassLoader.getClassReader().readClassByName(className, null);
        descriptions.add(classDescription);
        return descriptions;
    }

    private String join(ArrayList<String> items, int start, int end, String s) {
        List<String> strings = items.subList(start, end);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < strings.size(); i++) {
            result.append(strings.get(i)).append(i == strings.size() - 1 ? "" : s);
        }
        return result.toString();
    }


    private ArrayList<String> parseExpr(String expr) {
        ArrayList<String> items = new ArrayList<>();
        int s = 0;
        //recognize ClassInstanceCreationExpr as a whole
        int e = PatternFactory.lastMatch(expr, compile("^new\\s+" + Patterns.RE_QUALID + "\\s*[(\\]]")) - 1;
        if (e < 0) {//not found
            e = PatternFactory.firstMatch(expr, compile("[.(\\[]"));
        }
        boolean isParen = false;
        while (e >= 0) {
            if (expr.charAt(e) == '.') {
                String subExpr = expr.substring(s, e);
                items.addAll(isParen ? processParentheses(subExpr) : Lists.newArrayList(subExpr));
                isParen = false;
                s = e + 1;
            } else if (expr.charAt(e) == '(') {
                e = getMatchIndexEnd(expr, e, '(', ')');
                isParen = true;
                if (e < 0) {
                    break;
                } else {
                    e = PatternFactory.matchEnd(expr, compile("^\\s*[.\\[]"), e + 1) - 1;
                    continue;
                }
            } else if (expr.charAt(e) == '[') {
                e = getMatchIndexEnd(expr, e, '[', ']');
                if (e < 0) {
                    break;
                } else {
                    e = PatternFactory.matchEnd(expr, compile("^\\s*[.\\[]"), e + 1) - 1;
                    continue;
                }
            }
            e = PatternFactory.matchEnd(expr, compile("[.(\\[]"), s);
        }
        String tail = expr.substring(s);
        if (!tail.matches("^\\s*$")) {//is empty
            items.addAll(isParen ? processParentheses(tail) : Lists.newArrayList(tail));
        }
        return items;
    }

    private int getMatchIndexEnd(String expr, int start, char open, char close) {
        return 0;
    }

    private ArrayList<String> processParentheses(String subExpr) {
        return null;
    }

    private String extractCleanExpr(String statement) {
        // TODO: 13-Aug-17 impl
        return statement.replaceAll("\\s", "");
    }

    /**
     * " Search back from the cursor position till meeting '{' or ';'.
     * " '{' means statement start, ';' means end of a previous statement.
     *
     * @return statement before cursor
     * " Note: It's the base for parsing. And It's OK for most cases.
     */
    @NonNull
    private String getStatement(EditText editor) {
        String lineBeforeCursor = getCurrentLine(editor);
        if (lineBeforeCursor.matches("^\\s*(import|package)\\s+")) {
            return lineBeforeCursor;
        }
        int oldCursor = editor.getSelectionStart();
        int newCursor = oldCursor;
        while (true) {
            if (newCursor == 0) break;
            char c = editor.getText().charAt(newCursor);
            if (c == '{' || c == '}' || c == ';') {
                newCursor++;
                break;
            }
            newCursor--;
        }
        String statement = editor.getText().subSequence(newCursor, oldCursor).toString();
        return mergeLine(statement);
    }

    private String mergeLine(String statement) {
        statement = cleanStatement(statement);
        return statement;
    }

    private String getCurrentLine(EditText editText) {
        return EditorUtil.getLineBeforeCursor(editText, editText.getSelectionStart());
    }

    private int findChar(EditText editor, String s) {
        int selectionEnd = editor.getSelectionEnd();
        while (selectionEnd > -1 && editor.getText().charAt(selectionEnd) != s.charAt(0)) {
            selectionEnd--;
        }
        return selectionEnd;
    }

    /**
     * set string literal empty, remove comments, trim begining or ending spaces
     * test case: ' 	sb. /* block comment"/ append( "stringliteral" ) // comment '
     */
    private String cleanStatement(String code) {
        if (code.matches("\\s*")) {
            return "";
        }
        code = removeComment(code); //clear all comment
        code = code.replaceAll(Patterns.STRINGS.toString(), "\"\""); //clear all string content
        code = code.replaceAll("[\n\t\r]", "");
        return code;
    }

    /**
     * remove all comment
     */
    private String removeComment(String code) {
        return code.replaceAll(Patterns.JAVA_COMMENTS.toString(), "");
    }

    private boolean inComment() {
        return false;
    }

    private boolean inString() {
        return false;
    }

    public void load(JavaProjectFolder projectFile) {
        mClassLoader.loadAllClasses(true, projectFile);
    }

    public boolean isLoaded() {
        return mClassLoader.getClassReader().isLoaded();
    }


    public ArrayList<Description> getSuggestions(EditText editor, int position) {
        try {
            int start = findStart(editor);
            Log.d(TAG, "getSuggestions start = " + start);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // text: 'package.Class.me', prefix: 'package.Class', suffix: 'me'
        // text: 'package.Cla', prefix: 'package', suffix: 'Cla'
        // text: 'Cla', prefix: '', suffix: 'Cla'
        // line: 'new Cla', text: 'Cla', prevWord: 'new'
        String preWord = EditorUtil.getPreWord(editor, position);
        String current = EditorUtil.getWord(editor, position).replace("@", "");
        String prefix = "";
        String suffix = "";
        if (current.contains(".")) {
            prefix = current.substring(0, current.lastIndexOf("."));
            suffix = current.substring(current.lastIndexOf(".") + 1);
        } else {
            suffix = current;
        }
        Log.d(TAG, "getSuggestions suffix = " + suffix + " prefix = " + prefix + " current = " + current
                + " preWord = " + preWord);
        boolean couldBeClass = suffix.matches(PatternFactory.IDENTIFIER.toString());

        ArrayList<Description> result = null;

        if (couldBeClass) {
            Log.d(TAG, "getSuggestions couldBeClass = " + true);
            ArrayList<ClassDescription> classes = this.mClassLoader.findClass(current);

            //Object o = new Object(); //handle new keyword
            if (preWord != null && preWord.equals("new")) {
                result = new ArrayList<>();
                for (ClassDescription description : classes) {
                    ArrayList<ConstructorDescription> constructors = description.getConstructors();
                    for (ConstructorDescription constructor : constructors) {
                        result.add(constructor);
                    }
                }
                return result;
            } else {
                result = new ArrayList<>();
                for (ClassDescription aClass : classes) {
                    if (!aClass.getSimpleName().equals(current)) {
                        result.add(aClass);
                    }
                }
            }
        }


        if (result == null || result.size() == 0) {
            ArrayList<String> classes = determineClassName(editor, position, current, prefix, suffix);
            if (classes != null) {
                for (String className : classes) {
                    JavaClassReader classReader = mClassLoader.getClassReader();
                    ClassDescription classDescription = classReader.readClassByName(className, null);
                    Log.d(TAG, "getSuggestions classDescription = " + classDescription);
                    if (classDescription != null) {
                        result = new ArrayList<>();
                        result.addAll(classDescription.getMember(suffix));
                    }
                }
            }/* else {
                if (prefix.isEmpty() && !suffix.isEmpty()) {
                    if (JavaUtil.isValidClassName(suffix)) { //could be class
                        ArrayList<ClassDescription> possibleClass = mClassLoader.findClass(suffix);
                    }
                }
            }*/
        }
        return result;
    }

    public String getFormattedReturnType(Member member) {
        // TODO: 20-Jul-17
        return null;
    }

    private String createSnippet(Description desc, String line, String prefix, boolean addMemberClass) {
//        boolean useFullClassName = desc.getType().equals("class")
//                ? line.matches("^(import)") : prefix.contains(".");
//        String text = useFullClassName ? desc.getClassName() : desc.getSimpleName();
//        if (desc.getMember() != null) {
//            text = (addMemberClass ? "${1:" + text + "}." : "")
//                    + createMemberSnippet(desc.getMember(), desc.getType());
//        }
//        return text;
        return "";
    }

    private String createMemberSnippet(Description member) {
        return null;
        // TODO: 20-Jul-17
    }

    public void onInsertSuggestion(EditText editText, Description suggestion) {
        if (suggestion instanceof ClassDescription) {
//            if (!suggestion.getSnippet().contains(".")) {
            PackageImporter.importClass(editText, ((ClassDescription) suggestion).getClassName());
//            }/
        } else if (suggestion instanceof ConstructorDescription) {
            PackageImporter.importClass(editText, suggestion.getName());
        } else if (suggestion instanceof Member) {
        }
//        mClassLoader.touchClass(suggestion.getDescription());
    }

    public void dispose() {
        mClassLoader.getClassReader().dispose();
    }

    @IntDef({CONTEXT_AFTER_DOT, CONTEXT_METHOD_PARAM, CONTEXT_IMPORT, CONTEXT_IMPORT_STATIC,
            CONTEXT_PACKAGE_DECL, CONTEXT_NEED_TYPE, CONTEXT_OTHER})
    public @interface ContextType {
    }
}
