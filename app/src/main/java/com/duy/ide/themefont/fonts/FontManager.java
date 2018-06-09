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

package com.duy.ide.themefont.fonts;

import android.content.Context;
import android.graphics.Typeface;

import com.duy.ide.R;
import com.duy.ide.java.file.FileManager;

import java.io.IOException;
import java.util.Hashtable;

/**
 * Created by Duy on 18-Mar-17.
 */

public class FontManager {
    private static final String PATH_TO_FONT = "fonts/";
    private static final String TAG = "Typefaces";
    private static final Hashtable<String, Typeface> cache = new Hashtable<>();

    private static Typeface get(Context c, String assetPath) throws IOException {
        synchronized (cache) {
            if (!cache.containsKey(assetPath)) {
                try {
                    Typeface t = Typeface.createFromAsset(c.getAssets(), assetPath);
                    cache.put(assetPath, t);
                } catch (Exception e) {
                    throw new IOException("Could not get typeface '" +
                            assetPath + "' because " + e.getMessage());
                }
            }
            return cache.get(assetPath);
        }
    }

    public synchronized static Typeface getFontFromAsset(Context context, String name) {
        try {
            if (name.equalsIgnoreCase(context.getString(R.string.font_consolas))) {
                return get(context, PATH_TO_FONT + "consolas.ttf");
            } else if (name.equalsIgnoreCase(context.getString(R.string.font_courier_new))) {
                return get(context, PATH_TO_FONT + "courier_new.ttf");
            } else if (name.equalsIgnoreCase(context.getString(R.string.font_lucida_sans_typewriter))) {
                return get(context, PATH_TO_FONT + "lucida_sans_typewriter_regular.ttf");
            } else if (name.equalsIgnoreCase(context.getString(R.string.font_monospace))) {
                return Typeface.MONOSPACE;
            } else if (name.equalsIgnoreCase(context.getString(R.string.font_source_code_pro))) {
                return get(context, PATH_TO_FONT + "source_code_pro.ttf");
            } else {
                return get(context, PATH_TO_FONT + name);
            }
        } catch (Exception e) {

        }
        return Typeface.MONOSPACE;
    }

    public synchronized static Typeface getFontFromStorage(String name) {
        try {
            synchronized (cache) {
                if (!cache.containsKey(name)) {
                    try {
                        Typeface font = Typeface.createFromFile(FileManager.EXTERNAL_DIR_SRC + "fonts/" + name);
                        cache.put(name, font);
                    } catch (Exception e) {
                        throw new IOException("Could not get typeface '" + name + "' because " + e.getMessage());
                    }
                }
                return cache.get(name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Typeface.MONOSPACE;
    }

    public static Typeface getFont(FontEntry fontEntry, Context context) {
        return fontEntry.fromStorage ? getFontFromStorage(fontEntry.name) :
                getFontFromAsset(context, fontEntry.name);
    }
}
