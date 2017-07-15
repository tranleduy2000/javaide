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

package com.duy.frontend.utils.clipboard;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;

@SuppressLint("NewApi")
public class ClipboardManagerCompatV11 implements ClipboardManagerCompat {
    private final ClipboardManager clip;

    public ClipboardManagerCompatV11(Context context) {
        clip = (ClipboardManager) context.getApplicationContext()
                .getSystemService(Context.CLIPBOARD_SERVICE);
    }

    @Override
    public CharSequence getText() {
        if (clip.getPrimaryClip().getItemCount() >= 1) {
            ClipData.Item item = clip.getPrimaryClip().getItemAt(0);
            return item.getText();
        }
        return "";
    }

    @Override
    public void setText(CharSequence text) {
        ClipData clipData = ClipData.newPlainText("simple text", text);
        clip.setPrimaryClip(clipData);
    }

    @Override
    public boolean hasText() {
        return (clip.hasPrimaryClip() && clip.getPrimaryClipDescription()
                .hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN));
    }
}
