package org.jetbrains.java.decompiler.main.extern;

import org.jetbrains.java.decompiler.util.InterpreterUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.ASCII_STRING_CHARACTERS;
import static org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.BANNER;
import static org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.BOOLEAN_TRUE_ONE;
import static org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.BYTECODE_SOURCE_MAPPING;
import static org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.DECOMPILE_ASSERTIONS;
import static org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.DECOMPILE_CLASS_1_4;
import static org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.DECOMPILE_ENUM;
import static org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.DECOMPILE_GENERIC_SIGNATURES;
import static org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.DECOMPILE_INNER;
import static org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.DUMP_ORIGINAL_LINES;
import static org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.FINALLY_DEINLINE;
import static org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.HIDE_DEFAULT_CONSTRUCTOR;
import static org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.HIDE_EMPTY_SUPER;
import static org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.IDEA_NOT_NULL_ANNOTATION;
import static org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.IGNORE_INVALID_BYTECODE;
import static org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.INDENT_STRING;
import static org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.LAMBDA_TO_ANONYMOUS_CLASS;
import static org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.LITERALS_AS_IS;
import static org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.LOG_LEVEL;
import static org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.MAX_PROCESSING_METHOD;
import static org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.NEW_LINE_SEPARATOR;
import static org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.NO_EXCEPTIONS_RETURN;
import static org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.REMOVE_BRIDGE;
import static org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.REMOVE_EMPTY_RANGES;
import static org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.REMOVE_GET_CLASS_NEW;
import static org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.REMOVE_SYNTHETIC;
import static org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.RENAME_ENTITIES;
import static org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.SYNTHETIC_NOT_SET;
import static org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.UNDEFINED_PARAM_TYPE_OBJECT;
import static org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.UNIT_TEST_MODE;
import static org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.USE_DEBUG_VAR_NAMES;
import static org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences.USE_METHOD_PARAMETERS;

public class IFernflowerPreferencesStatic {
    static Map<String, Object> getDefaults() {
        Map<String, Object> defaults = new HashMap<>();

        defaults.put(REMOVE_BRIDGE, "1");
        defaults.put(REMOVE_SYNTHETIC, "0");
        defaults.put(DECOMPILE_INNER, "1");
        defaults.put(DECOMPILE_CLASS_1_4, "1");
        defaults.put(DECOMPILE_ASSERTIONS, "1");
        defaults.put(HIDE_EMPTY_SUPER, "1");
        defaults.put(HIDE_DEFAULT_CONSTRUCTOR, "1");
        defaults.put(DECOMPILE_GENERIC_SIGNATURES, "0");
        defaults.put(NO_EXCEPTIONS_RETURN, "1");
        defaults.put(DECOMPILE_ENUM, "1");
        defaults.put(REMOVE_GET_CLASS_NEW, "1");
        defaults.put(LITERALS_AS_IS, "0");
        defaults.put(BOOLEAN_TRUE_ONE, "1");
        defaults.put(ASCII_STRING_CHARACTERS, "0");
        defaults.put(SYNTHETIC_NOT_SET, "0");
        defaults.put(UNDEFINED_PARAM_TYPE_OBJECT, "1");
        defaults.put(USE_DEBUG_VAR_NAMES, "1");
        defaults.put(USE_METHOD_PARAMETERS, "1");
        defaults.put(REMOVE_EMPTY_RANGES, "1");
        defaults.put(FINALLY_DEINLINE, "1");
        defaults.put(IDEA_NOT_NULL_ANNOTATION, "1");
        defaults.put(LAMBDA_TO_ANONYMOUS_CLASS, "0");
        defaults.put(BYTECODE_SOURCE_MAPPING, "0");
        defaults.put(IGNORE_INVALID_BYTECODE, "0");

        defaults.put(LOG_LEVEL, IFernflowerLogger.Severity.INFO.name());
        defaults.put(MAX_PROCESSING_METHOD, "0");
        defaults.put(RENAME_ENTITIES, "0");
        defaults.put(NEW_LINE_SEPARATOR, (InterpreterUtil.IS_WINDOWS ? "0" : "1"));
        defaults.put(INDENT_STRING, "   ");
        defaults.put(BANNER, "");
        defaults.put(UNIT_TEST_MODE, "0");
        defaults.put(DUMP_ORIGINAL_LINES, "0");

        return Collections.unmodifiableMap(defaults);
    }

}
