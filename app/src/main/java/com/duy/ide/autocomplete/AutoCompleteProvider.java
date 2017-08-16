package com.duy.ide.autocomplete;

import android.content.Context;
import android.support.annotation.IntDef;
import android.util.Log;
import android.widget.EditText;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
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
import com.duy.ide.autocomplete.model.PackageDescription;
import com.duy.ide.autocomplete.parser.JavaParser;
import com.duy.ide.autocomplete.util.EditorUtil;
import com.duy.ide.file.FileManager;
import com.duy.project.file.java.JavaProjectFolder;
import com.google.common.collect.Lists;
import com.sun.tools.javac.tree.JCTree;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.SourceVersion;

import static com.duy.ide.autocomplete.autocomplete.PatternFactory.lastMatchStr;
import static com.duy.ide.autocomplete.util.EditorUtil.getPossibleClassName;
import static java.util.regex.Pattern.compile;


/**
 * Created by Duy on 20-Jul-17.
 */

public class AutoCompleteProvider {
    public static final int KIND_NONE = 0;
    public static final int KIND_PACKAGE = KIND_NONE + 1; //or import
    public static final int KIND_METHOD = KIND_PACKAGE + 1;
    public static final int KIND_IMPORT = KIND_METHOD + 1;
    public static final int KIND_MEMBER = KIND_IMPORT + 1;
    public static final int KIND_THIS = KIND_MEMBER + 1;
    public static final int KIND_SUPER = KIND_THIS + 1;
    public static final int KIND_BUILTIN_TYPE = KIND_SUPER + 1;
    public static final int KIND_STRING_TYPE = KIND_BUILTIN_TYPE + 1;
    public static final int KIND_ARRAY_TYPE = KIND_STRING_TYPE + 1;

    public static final int CONTEXT_OTHER = 0;
    public static final int CONTEXT_AFTER_DOT = CONTEXT_OTHER + 1;
    public static final int CONTEXT_METHOD_PARAM = CONTEXT_AFTER_DOT + 2;
    public static final int CONTEXT_IMPORT = CONTEXT_METHOD_PARAM + 3;
    public static final int CONTEXT_IMPORT_STATIC = CONTEXT_IMPORT + 4;
    public static final int CONTEXT_PACKAGE_DECL = CONTEXT_IMPORT_STATIC + 6;
    public static final int CONTEXT_NEED_TYPE = CONTEXT_PACKAGE_DECL + 7;
    public static final int CONTEXT_NEED_CONSTRUCTOR = CONTEXT_NEED_TYPE + 1;

    private static final String TAG = "AutoCompleteProvider";
    private JavaDexClassLoader mClassLoader;
    private PackageImporter packageImporter;
    private AutoCompletePackage mPackageProvider;
    private JavaParser mJavaParser;

    @Nullable
    private JCTree.JCCompilationUnit unit;
    private String source;
    private int cursor;

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
    @ContextType
    private int contextType = CONTEXT_OTHER;

    public AutoCompleteProvider(Context context) {
        File outDir = context.getDir("dex", Context.MODE_PRIVATE);
        mClassLoader = new JavaDexClassLoader(FileManager.getClasspathFile(context), outDir);
        mPackageProvider = new AutoCompletePackage();
        mJavaParser = new JavaParser();
    }

    private void initializer(EditText editor) {
        this.cursor = editor.getSelectionStart();
        this.source = editor.getText().toString();
        try {
            this.unit = mJavaParser.parse(source);
        } catch (Exception e) {
            this.unit = null;
        }

        //reset environment
        dotExpr = "";
        incomplete = "";
        contextType = CONTEXT_OTHER;

        statement = getStatement(editor);
        Log.d(TAG, "findStart statement = " + statement);
        if (compile("[.0-9A-Za-z_]\\s*$").matcher(statement).find()) {
            boolean valid = true;
            if (compile("\\.\\s*$").matcher(statement).find()) {
                valid = compile("[\")0-9A-Za-z_\\]]\\s*\\.\\s*$").matcher(statement).find()
                        && !compile("(" + Patterns.RE_KEYWORDS.toString() + ")\\.\\s*").matcher(statement).find();
            }
            if (!valid) return;

            contextType = CONTEXT_AFTER_DOT;
            //import or package declaration
            if (compile("^\\s*(import|package)\\s+").matcher(statement).find()) {
                progressImportPackage();
            }
            //String literal
            else if (compile("\"\\s*\\.\\s*$").matcher(statement).find()) {
                dotExpr = statement.replaceAll("\\s*\\.\\s*$", ".");
                return;
            }
            //" type declaration		NOTE: not supported generic yet.
            else {
                Matcher matcher = compile("^\\s*" + Patterns.RE_TYPE_DECL).matcher(statement);
                if (matcher.find()) {
                    dotExpr = statement.substring(matcher.start());
                    if (!compile("^\\s*(extend|implements)\\s+").matcher(dotExpr).find()) {
                        // TODO: 13-Aug-17 suggest class
                        return;

                    }
                    contextType = CONTEXT_NEED_TYPE;
                } else {
                    matcher = compile("(\\s*new\\s+)(" + Patterns.RE_QUALID + ")$").matcher(statement);
                    if (matcher.find()) {
                        statement = matcher.group(2);
                        if (!Patterns.RE_KEYWORDS.matcher(statement).find()) {
                            incomplete = statement;
                            dotExpr = "";
                            contextType = CONTEXT_NEED_CONSTRUCTOR;
                            return;
                        }
                    }
                }
                dotExpr = extractCleanExpr(statement);
            }

            //" all cases: " java.ut|" or " java.util.|" or "ja|"
            if (dotExpr.contains(".")) {
                incomplete = dotExpr.substring(dotExpr.lastIndexOf(".") + 1);
                dotExpr = dotExpr.substring(0, dotExpr.lastIndexOf(".") + 1); //include "." character
            } else {
                incomplete = dotExpr;
                dotExpr = "";
            }
            //incomplete
            return;

        }
        //	" method parameters, treat methodname or 'new' as an incomplete word
        else if (compile("\\(\\s*$").matcher(statement).find()) {
            //" TODO: Need to exclude method declaration?
            contextType = CONTEXT_METHOD_PARAM;
            int pos = statement.lastIndexOf("(");
            statement = statement.replaceAll("\\s*\\(\\s*$", "");
            //" new ClassName?

            if (compile("\\s*new\\s+" + Patterns.RE_QUALID + "$").matcher(statement).find()) {
                statement = statement.replaceAll("^\\s*new\\s+", "");
                if (!Patterns.KEYWORDS.matcher(statement).find()) {
                    incomplete = "+";
                    dotExpr = statement;
                    contextType = CONTEXT_NEED_CONSTRUCTOR;
                    return;

                }
            } else {
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
                        return;

                    } else if (!Patterns.KEYWORDS.matcher(statement).find()) {
                        incomplete = statement;
                        return;

                    }
                }
                //case expr.method(|)
                else if (statement.charAt(pos - 1) == '.' &&
                        !Patterns.KEYWORDS.matcher(statement.substring(0, statement.lastIndexOf("."))).find()) {
                    dotExpr = extractCleanExpr(statement.substring(0, statement.lastIndexOf(".")));
                    incomplete = statement.substring(statement.lastIndexOf(".") + 1);
                    return;
                }
            }
        }
    }

    private void progressImportPackage() {
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
                dotExpr = statement.substring(matcher.end());
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

    public ArrayList<Description> complete() {
        //" Return list of matches.
        //case: all is empty
        if (dotExpr.isEmpty() && incomplete.isEmpty()) {
            return new ArrayList<>();
        }

        //the result
        ArrayList<Description> result = new ArrayList<>();

        if (!dotExpr.isEmpty()) {
            switch (contextType) {
                case CONTEXT_AFTER_DOT:
                    result = completeAfterDot(source, dotExpr, incomplete);
                    break;
                case CONTEXT_IMPORT:
                case CONTEXT_IMPORT_STATIC:
                case CONTEXT_PACKAGE_DECL:
                case CONTEXT_NEED_TYPE:
                    result = getMember(dotExpr, incomplete);
                    break;
                case CONTEXT_METHOD_PARAM:
                    result = completeAfterDot(source, dotExpr, incomplete);
                    break;
                case CONTEXT_NEED_CONSTRUCTOR:
                    result = getConstructorList(dotExpr);
                    break;
                case CONTEXT_OTHER:
                    result = new ArrayList<>();
                    break;
            }
//            if (contextType == CONTEXT_AFTER_DOT) {
//                result = completeAfterDot(editor, dotExpr);
//            } else if (contextType == CONTEXT_IMPORT
//                    || contextType == CONTEXT_IMPORT_STATIC
//                    || contextType == CONTEXT_PACKAGE_DECL
//                    || contextType == CONTEXT_NEED_TYPE) {
//                result = getMember(dotExpr, incomplete);
//            } else if (contextType == CONTEXT_METHOD_PARAM) {
//                if (incomplete.equals("+")) {
//                    result = getConstructorList(dotExpr);
//                } else {
//                    result = completeAfterDot(editor, dotExpr);
//                }
//            }
        }
        //only complete word
        else if (!incomplete.isEmpty()) {
            //only need method
            switch (contextType) {
                case CONTEXT_METHOD_PARAM:
                    result = completeMethodParams(incomplete);
                    break;
                case CONTEXT_NEED_CONSTRUCTOR:
                    result = getConstructorList(incomplete);
                    break;
                default:
                    result = completeWord(source, incomplete);
                    break;
            }
        }
        return result;
    }

    private ArrayList<Description> filter(ArrayList<Description> input, String incomplete) {
        ArrayList<Description> result = new ArrayList<>();
        for (Description s : input) {
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
    private ArrayList<Description> completeWord(String source, String incomplete) {
        if (incomplete.endsWith(" ")) {
            return new ArrayList<>();
        }
        incomplete = incomplete.trim();
        ArrayList<Description> result = new ArrayList<>();
        if (contextType != CONTEXT_PACKAGE_DECL) {
            //parse current file
            if (unit != null) {
                //add import current file
                com.sun.tools.javac.util.List<JCTree.JCImport> imports = unit.getImports();
                for (JCTree.JCImport anImport : imports) {
                    JavaClassReader classReader = mClassLoader.getClassReader();
                    ClassDescription clazz = classReader.readClassByName(anImport.getQualifiedIdentifier().toString(), null);
                    if (clazz != null && clazz.getSimpleName().startsWith(incomplete)) {
                        result.add(clazz);
                    }
                }
                //current file declare
                com.sun.tools.javac.util.List<JCTree> typeDecls = unit.getTypeDecls();
                if (!typeDecls.isEmpty()) {
                    JCTree jcTree = typeDecls.get(0);
                    if (jcTree instanceof JCTree.JCClassDecl) {
                        com.sun.tools.javac.util.List<JCTree> members = ((JCTree.JCClassDecl) jcTree).getMembers();
                        for (JCTree member : members) {
                            if (member instanceof JCTree.JCVariableDecl) {
                                JCTree.JCVariableDecl field = (JCTree.JCVariableDecl) member;
                                if (field.getName().toString().startsWith(incomplete)) {
                                    result.add(new FieldDescription(
                                            field.getName().toString(),
                                            field.getType().toString(),
                                            (int) field.getModifiers().flags));
                                }
                            } else if (member instanceof JCTree.JCMethodDecl) {
                                JCTree.JCMethodDecl method = (JCTree.JCMethodDecl) member;
                                if (((JCTree.JCMethodDecl) member).getName().toString().startsWith(incomplete)) {
                                    com.sun.tools.javac.util.List<JCTree.JCTypeParameter> typeParameters = method.getTypeParameters();
                                    ArrayList<String> paramsStr = new ArrayList<>();
                                    for (JCTree.JCTypeParameter typeParameter : typeParameters) {
                                        paramsStr.add(typeParameter.toString());
                                    }
                                    result.add(new MethodDescription(
                                            method.getName().toString(),
                                            method.getReturnType().toString(),
                                            method.getModifiers().flags,
                                            paramsStr));
                                }
                                //if the cursor in method scope
                                if (method.getStartPosition() <= cursor
                                        && method.getBody().getEndPosition(unit.endPositions) >= cursor) {
                                    //add field from start position of method to the cursor
                                    com.sun.tools.javac.util.List<JCTree.JCStatement> statements = method.getBody().getStatements();
                                    for (JCTree.JCStatement jcStatement : statements) {
                                        if (jcStatement instanceof JCTree.JCVariableDecl) {
                                            JCTree.JCVariableDecl field = (JCTree.JCVariableDecl) jcStatement;
                                            if (field.getName().toString().startsWith(incomplete)) {
                                                result.add(new FieldDescription(
                                                        field.getName().toString(),
                                                        field.getType().toString(),
                                                        (int) field.getModifiers().flags));
                                            }
                                        }
                                    }
                                    //add params
                                    com.sun.tools.javac.util.List<JCTree.JCVariableDecl> parameters = method.getParameters();
                                    for (JCTree.JCVariableDecl parameter : parameters) {
                                        JCTree.JCVariableDecl field = parameter;
                                        if (field.getName().toString().startsWith(incomplete)) {
                                            result.add(new FieldDescription(
                                                    field.getName().toString(),
                                                    field.getType().toString(),
                                                    (int) field.getModifiers().flags));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            ArrayList<ClassDescription> aClass = mClassLoader.findClassWithPrefix(incomplete);
            result.addAll(aClass);

        }
        Collections.sort(result, new Comparator<Description>() {
            @Override
            public int compare(Description o1, Description o2) {
                return Integer.valueOf(o1.getDescriptionType()).compareTo(o2.getDescriptionType());
            }
        });
        return result;
    }

    private ArrayList<Description> completeMethodParams(String incomplete) {
        return new ArrayList<>();
    }

    @NonNull
    private ArrayList<Description> getConstructorList(String className) {
        if (className.isEmpty()) return new ArrayList<>();
        ArrayList<ClassDescription> classes = mClassLoader.findClassWithPrefix(className);
        ArrayList<Description> constructors = new ArrayList<>();
        for (ClassDescription c : classes) {
            constructors.addAll(c.getConstructors());
        }
        return constructors;
    }


    /**
     * get member of class name, package ...
     *
     * @param prefix - end with "."
     * @param suffix - incomplete word
     * @return
     */
    @NonNull
    private ArrayList<Description> getMember(String prefix, String suffix) {
        //get class member
        ClassDescription classDescription = mClassLoader.getClassReader().readClassByName(prefix, null);
        ArrayList<Description> members = new ArrayList<>();
        if (classDescription != null) {
            members.addAll(classDescription.getMember(suffix));
        }
        PackageDescription packageDescription = mPackageProvider.trace(prefix.substring(0, prefix.lastIndexOf(".")));
        if (packageDescription != null) {
            HashMap<String, PackageDescription> child = packageDescription.getChild();
            for (Map.Entry<String, PackageDescription> entry : child.entrySet()) {
                if (entry.getValue().getName().startsWith(suffix)) {
                    members.add(entry.getValue());
                }

            }
        }
        return members;
    }

    /**
     * " Precondition:	expr must end with '.'
     * " return members of the value of expression
     */
    private ArrayList<Description> completeAfterDot(String source, String dotExpr, String incomplete) {
        ArrayList<String> items = parseExpr(dotExpr);
        if (items.size() == 0) {
            return new ArrayList<>();
        }

        //0. String literal
        if (items.get(items.size() - 1).matches("\"$")) {
            return getMember(dotExpr, String.class.getName());
        }

        ArrayList<Description> ti = new ArrayList<>();
        int ii = 1; //item index;
        @ItemKind
        int itemKind = 0;

        /**
         " optimized process
         " search the longest expr consisting of ident
         */
        int i = 0, k = 0;
        while (i < items.size() && compile("^\\s*" + Patterns.RE_IDENTIFIER + "\\s*$").matcher(items.get(i)).find()) {
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
                ti = doGetClassInfo(itemAtK.equals("class") ? "java.lang.Class" : join(items, 0, k, "."));
                if (!ti.isEmpty()) {
                    itemKind = !itemAtK.equals("this") ? KIND_THIS : !itemAtK.equals("super") ? KIND_SUPER : KIND_NONE;
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
            if (Pattern.compile("^\\s*" + Patterns.RE_IDENTIFIER + "\\s*").matcher(items.get(0)).find()) {
                String ident = items.get(0).replaceAll("\\s", "");
                if (SourceVersion.isKeyword(ident)) {
                    // 1)
                    if (ident.equals("void") || isBuiltinType(ident)) {
                        ti = doGetClassInfo(int.class.getName());
                        itemKind = KIND_BUILTIN_TYPE;
                    }
                    // 2)
                    else if (ident.equals("this") || ident.equals("super")) {
                        itemKind = ident.equals("this") ? KIND_THIS : ident.equals("super") ? KIND_SUPER : KIND_NONE;
                        ti = doGetClassInfo(ident);
                    }
                } else {
                    // 3)
                    String typeName = getDeclaredClassName(source, ident);
                    if (!typeName.isEmpty()) {
                        if (typeName.charAt(0) == '[' && typeName.charAt(typeName.length() - 1) == ']') {
                            ti = doGetClassInfo(Object[].class.getName());
                        } else if (!typeName.equals("void") && !isBuiltinType(typeName)) {
                            ti = doGetClassInfo(typeName);
                        }
                    } else { //typeName is empty
                        // 4) TypeName.|
                        ti = doGetClassInfo(ident);
                        itemKind = KIND_MEMBER;

                        // 5) package
                        if (ti.isEmpty()) {
                            ti = getMember(dotExpr, ident);
                            itemKind = KIND_PACKAGE;
                        }
                    }
                }
            }
            //" method invocation:	"method().|"	- "this.method().|"
            else if (compile("^\\s*" + Patterns.RE_IDENTIFIER + "\\s*\\(").matcher(items.get(0)).find()) {
                ti = methodInvocation(items.get(0), ti, itemKind);
            }
            //" array type, return `class`: "int[] [].|", "java.lang.String[].|", "NestedClass[].|"
            else if (items.get(0).matches(Patterns.RE_ARRAY_TYPE.toString())) {
                Matcher matcher = Patterns.RE_ARRAY_TYPE.matcher(items.get(0));
                if (matcher.find()) {
                    String qid = matcher.group(1); //class name
                    if (isBuiltinType(qid) || (!isKeyword(qid) && !doGetClassInfo(qid).isEmpty())) {
                        ti = doGetClassInfo(int.class.getName());
                        itemKind = KIND_MEMBER;
                    }
                }
            }
            //" class instance creation expr:	"new String().|", "new NonLoadableClass().|"
//            " array creation expr:	"new int[i=1] [val()].|", "new java.lang.String[].|"
            else if (compile("^new\\s+").matcher(items.get(0)).find()) {
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
            else if (Patterns.RE_CASTING.matcher(items.get(0)).find()) {
                Matcher matcher = Patterns.RE_CASTING.matcher(items.get(0));
                if (matcher.find()) {
                    ti = doGetClassInfo(matcher.group(1));
                }
            }
            //" array access:	"var[i][j].|"		Note: "var[i][]" is incorrect
            else if (Patterns.RE_ARRAY_ACCESS.matcher(items.get(0)).find()) {
                Matcher matcher = Patterns.RE_ARRAY_ACCESS.matcher(items.get(0));
                matcher.find();
                String typeName = matcher.group(1);
                typeName = getDeclaredClassName(source, typeName);
                if (!typeName.isEmpty()) {
                    ti = arrayAccess(typeName, items.get(0));
                }
            }
        }


        /*
         * next items
         */
        while (!ti.isEmpty() && ii < items.size()) {
            // method invocation:	"PrimaryExpr.method(parameters)[].|"
            if (compile("^\\s*" + Patterns.RE_IDENTIFIER + "\\s*\\(").matcher(items.get(ii)).find()) {
                Log.d(TAG, "completeAfterDot: RE_IDENTIFIER ( ");
                ti = methodInvocation(items.get(ii), ti, itemKind);
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
                        ti = doGetClassInfo(ident.equals("class") ? "java.lang.Class" : join(items, 0, ii - 1, "."));
                        itemKind = ident.equals("this") ? KIND_THIS : ident.equals("super") ? KIND_SUPER : KIND_NONE;
                    } else if (!isKeyword(ident) /*&& type == class*/) {
                        //accessible static field
                        //ti = get info of stattic field
                    }
                }
            }
        }
        return filter(ti, incomplete);
    }

    private ArrayList<Description> arrayAccess(String typeName, String s) {
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

    private ArrayList<Description> methodInvocation(String s, ArrayList<Description> ti, int itemKind) {
        return ti;
    }

    private String getDeclaredClassName(String src, String ident) {
        ident = ident.trim();
        if (compile("this|super").matcher(ident).find()) {
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
        int pos = cursor;
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
                ClassDescription classDescription = mClassLoader.getClassReader().readClassByName(className, null);
                if (classDescription != null) {
                    return className;
                }
            }
        }
        return "";
    }

    private ArrayList<Description> getVariableDeclaration() {
        return new ArrayList<>();
    }

    private boolean isBuiltinType(String ident) {
        return Patterns.PRIMITIVE_TYPES.matcher(ident).find();
    }

    private boolean isKeyword(String ident) {
        return Patterns.RE_KEYWORDS.matcher(ident).find();
    }

    @NonNull
    private ArrayList<Description> getStaticAccess(String className) {
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

    private ArrayList<Description> doGetClassInfo(String fullName) {
        ArrayList<Description> descriptions = new ArrayList<>();
        ClassDescription classDescription = mClassLoader.getClassReader().readClassByName(fullName, null);
        if (classDescription != null) {
            ArrayList<Description> members = classDescription.getMember("");
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
        Matcher matcher = compile("^\\s*new\\s+" + Patterns.RE_QUALID + "\\s*[(\\]]").matcher(expr);
        int e = -1;
        if (matcher.find()) {
            e = matcher.end() - 1;
            Log.i(TAG, "parseExpr: found instance at " + matcher.group());
        }
        if (e < 0) {//not found instance
            matcher = compile("[.(\\[]").matcher(expr); //(String) str, ((Char) c)
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
                    Pattern pattern = compile("^\\s*[.\\[]");
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
                    Pattern pattern = compile("^\\s*[.\\[]");
                    matcher = pattern.matcher(expr);
                    if (matcher.find(e + 1)) {
                        e = matcher.end() - 1;
                        continue;
                    }
                }
            }
            matcher = Pattern.compile("[.(\\[]").matcher(expr);
            if (matcher.find(last)) {
                e = PatternFactory.matchEnd(expr, compile("[.(\\[]"), last);
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
        Pattern pattern = compile("^\\s*\\(");
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
                if (compile("^\\s*\\[").matcher(tail).find()) {

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

    private int searchPairBackward(String str, int index, char open, char close) {
        int count = 1;
        if (str.charAt(index) != open) {
            return -1;
        }
        int i = 0;
        while (i >= 0) {
            if (str.charAt(i) == open) {
                count--;
                if (count == 0) {
                    return i;
                }
            } else if (str.charAt(i) == close) {
                count++;
            }
            i--;
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
        mPackageProvider.init(projectFile, mClassLoader.getClassReader());
    }

    public boolean isLoaded() {
        return mClassLoader.getClassReader().isLoaded();
    }

    public ArrayList<Description> getSuggestions(EditText editor) {
        try {
            this.initializer(editor);
            ArrayList<Description> complete = complete();
            return complete;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }


    public void onInsertSuggestion(EditText editText, Description suggestion) {
        if (suggestion instanceof ClassDescription) {
            PackageImporter.importClass(editText, ((ClassDescription) suggestion).getClassName());
        } else if (suggestion instanceof ConstructorDescription) {
            PackageImporter.importClass(editText, suggestion.getName());
        } else if (suggestion instanceof Member) {
            mClassLoader.touchClass(suggestion.getType());
        }
    }

    public void dispose() {
        mClassLoader.getClassReader().dispose();
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
