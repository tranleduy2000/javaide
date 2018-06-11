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

package com.duy.ide.javaide.uidesigner.dynamiclayoutinflator;

import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nick on 8/10/15.
 * <p>
 * Taken from http://stackoverflow.com/questions/8343971/how-to-parse-a-dimension-string-and-convert-it-to-a-dimension-value
 */
public class DimensionConverter {
    // -- Initialize dimension string to constant lookup.
    public static final Map<String, Integer> dimensionConstantLookup = initDimensionConstantLookup();
    // -- Initialize pattern for dimension string.
    private static final Pattern DIMENSION_PATTERN = Pattern.compile("^\\s*(\\d+(\\.\\d+)*)\\s*([a-zA-Z]+)\\s*$");
    public static Map<String, Float> cached = new HashMap<>();

    private static Map<String, Integer> initDimensionConstantLookup() {
        Map<String, Integer> m = new HashMap<String, Integer>();
        m.put("px", TypedValue.COMPLEX_UNIT_PX);
        m.put("dip", TypedValue.COMPLEX_UNIT_DIP);
        m.put("dp", TypedValue.COMPLEX_UNIT_DIP);
        m.put("sp", TypedValue.COMPLEX_UNIT_SP);
        m.put("pt", TypedValue.COMPLEX_UNIT_PT);
        m.put("in", TypedValue.COMPLEX_UNIT_IN);
        m.put("mm", TypedValue.COMPLEX_UNIT_MM);
        return Collections.unmodifiableMap(m);
    }

    public static int stringToDimensionPixelSize(String dimension, DisplayMetrics metrics, ViewGroup parent, boolean horizontal) {
        if (dimension.endsWith("%")) {
            float pct = Float.parseFloat(dimension.substring(0, dimension.length() - 1)) / 100.0f;
            return (int) (pct * (horizontal ? parent.getMeasuredWidth() : parent.getMeasuredHeight()));
        }
        return stringToDimensionPixelSize(dimension, metrics);
    }

    public static int stringToDimensionPixelSize(String dimension, DisplayMetrics metrics) {
        // -- Mimics TypedValue.complexToDimensionPixelSize(int data, DisplayMetrics metrics).
        final float f;
        if (cached.containsKey(dimension)) {
            f = cached.get(dimension);
        } else {
            InternalDimension internalDimension = stringToInternalDimension(dimension);
            final float value = internalDimension.value;
            f = TypedValue.applyDimension(internalDimension.unit, value, metrics);
            cached.put(dimension, f);
        }
        final int res = (int) (f + 0.5f);
        if (res != 0) return res;
        if (f == 0) return 0;
        if (f > 0) return 1;
        return -1;
    }

    public static float stringToDimension(String dimension, DisplayMetrics metrics) {
        if (cached.containsKey(dimension)) return cached.get(dimension);
        // -- Mimics TypedValue.complexToDimension(int data, DisplayMetrics metrics).
        InternalDimension internalDimension = stringToInternalDimension(dimension);
        float val = TypedValue.applyDimension(internalDimension.unit, internalDimension.value, metrics);
        cached.put(dimension, val);
        return val;
    }

    private static InternalDimension stringToInternalDimension(String dimension) {
        // -- Match target against pattern.
        Matcher matcher = DIMENSION_PATTERN.matcher(dimension);
        if (matcher.matches()) {
            // -- Match found.
            // -- Extract value.
            float value = Float.valueOf(matcher.group(1)).floatValue();
            // -- Extract dimension units.
            String unit = matcher.group(3).toLowerCase();
            // -- Get Android dimension constant.
            Integer dimensionUnit = dimensionConstantLookup.get(unit);
            if (dimensionUnit == null) {
                // -- Invalid format.
                throw new NumberFormatException();
            } else {
                // -- Return valid dimension.
                return new InternalDimension(value, dimensionUnit);
            }
        } else {
            Log.e("DimensionConverter", "Invalid number format: " + dimension);
            // -- Invalid format.
            throw new NumberFormatException();
        }
    }

    private static class InternalDimension {
        float value;
        int unit;

        public InternalDimension(float value, int unit) {
            this.value = value;
            this.unit = unit;
        }
    }
}
