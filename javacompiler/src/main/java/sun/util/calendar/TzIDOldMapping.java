/*
 * Copyright (c) 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package sun.util.calendar;

import java.util.Map;
import java.util.HashMap;

class TzIDOldMapping {
    static final Map<String, String> MAP = new HashMap<String, String>();
    static {
        String[][] oldmap = {
            { "ACT", "Australia/Darwin" },
            { "AET", "Australia/Sydney" },
            { "AGT", "America/Argentina/Buenos_Aires" },
            { "ART", "Africa/Cairo" },
            { "AST", "America/Anchorage" },
            { "BET", "America/Sao_Paulo" },
            { "BST", "Asia/Dhaka" },
            { "CAT", "Africa/Harare" },
            { "CNT", "America/St_Johns" },
            { "CST", "America/Chicago" },
            { "CTT", "Asia/Shanghai" },
            { "EAT", "Africa/Addis_Ababa" },
            { "ECT", "Europe/Paris" },
            { "EST", "America/New_York" },
            { "HST", "Pacific/Honolulu" },
            { "IET", "America/Indianapolis" },
            { "IST", "Asia/Calcutta" },
            { "JST", "Asia/Tokyo" },
            { "MIT", "Pacific/Apia" },
            { "MST", "America/Denver" },
            { "NET", "Asia/Yerevan" },
            { "NST", "Pacific/Auckland" },
            { "PLT", "Asia/Karachi" },
            { "PNT", "America/Phoenix" },
            { "PRT", "America/Puerto_Rico" },
            { "PST", "America/Los_Angeles" },
            { "SST", "Pacific/Guadalcanal" },
            { "VST", "Asia/Saigon" },
        };
        for (String[] pair : oldmap) {
            MAP.put(pair[0], pair[1]);
        }
    }
}
