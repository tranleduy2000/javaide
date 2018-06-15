// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.main.extern;

import java.util.Map;

public interface IFernflowerPreferences {
    String REMOVE_BRIDGE = "rbr";
    String REMOVE_SYNTHETIC = "rsy";
    String DECOMPILE_INNER = "din";
    String DECOMPILE_CLASS_1_4 = "dc4";
    String DECOMPILE_ASSERTIONS = "das";
    String HIDE_EMPTY_SUPER = "hes";
    String HIDE_DEFAULT_CONSTRUCTOR = "hdc";
    String DECOMPILE_GENERIC_SIGNATURES = "dgs";
    String NO_EXCEPTIONS_RETURN = "ner";
    String DECOMPILE_ENUM = "den";
    String REMOVE_GET_CLASS_NEW = "rgn";
    String LITERALS_AS_IS = "lit";
    String BOOLEAN_TRUE_ONE = "bto";
    String ASCII_STRING_CHARACTERS = "asc";
    String SYNTHETIC_NOT_SET = "nns";
    String UNDEFINED_PARAM_TYPE_OBJECT = "uto";
    String USE_DEBUG_VAR_NAMES = "udv";
    String USE_METHOD_PARAMETERS = "ump";
    String REMOVE_EMPTY_RANGES = "rer";
    String FINALLY_DEINLINE = "fdi";
    String IDEA_NOT_NULL_ANNOTATION = "inn";
    String LAMBDA_TO_ANONYMOUS_CLASS = "lac";
    String BYTECODE_SOURCE_MAPPING = "bsm";
    String IGNORE_INVALID_BYTECODE = "iib";

    String LOG_LEVEL = "log";
    String MAX_PROCESSING_METHOD = "mpm";
    String RENAME_ENTITIES = "ren";
    String USER_RENAMER_CLASS = "urc";
    String NEW_LINE_SEPARATOR = "nls";
    String INDENT_STRING = "ind";
    String BANNER = "ban";

    String DUMP_ORIGINAL_LINES = "__dump_original_lines__";
    String UNIT_TEST_MODE = "__unit_test_mode__";

    String LINE_SEPARATOR_WIN = "\r\n";
    String LINE_SEPARATOR_UNX = "\n";

    Map<String, Object> DEFAULTS = IFernflowerPreferencesStatic.getDefaults();

}