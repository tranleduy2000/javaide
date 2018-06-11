package com.duy.ide.javaide.editor.autocomplete.internal;

import com.android.annotations.NonNull;
import com.duy.common.DLog;
import com.duy.ide.code.api.SuggestItem;
import com.duy.ide.editor.internal.suggestion.Editor;
import com.duy.ide.javaide.editor.autocomplete.model.PackageDescription;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Complete package
 * e.g.
 * <p>
 * java.i|
 * java.util.|
 * java.util.Hash|
 */
public class CompletePackage extends JavaCompleteMatcherImpl {
    private static final String TAG = "CompletePackage";
    @NonNull
    private final JavaPackageManager mJavaPackageManager;

    public CompletePackage(@NonNull JavaPackageManager javaPackageManager) {
        mJavaPackageManager = javaPackageManager;
    }

    @Override
    public boolean process() {
        return false;
    }

    @Override
    public void getSuggestion(Editor editor, String expr, List<SuggestItem> suggestItems) {
        if (DLog.DEBUG) {
            DLog.d(TAG, "getSuggestion() called with:" +
                    " editor = [" + editor + "]," +
                    " expr = [" + expr + "]," +
                    " suggestItems = [" + suggestItems + "]");
        }

        //package must be contains dot (.)
        if (!expr.contains(".")) {
            return;
        }

        //completed package java.lang
        int lastDotIndex = expr.lastIndexOf(".");
        String completedPkg = expr.substring(0, lastDotIndex /*not contains dot*/);
        String incompletePkg = expr.substring(lastDotIndex + 1);

        PackageDescription packages = mJavaPackageManager.trace(completedPkg);
        if (packages != null) {
            //members of current package
            //such as java has more member (util, io, lang)
            HashMap<String, PackageDescription> members = packages.getChild();
            for (Map.Entry<String, PackageDescription> entry : members.entrySet()) {
                PackageDescription packageDescription = entry.getValue();
                if (packageDescription.getName().startsWith(incompletePkg)) {
                    setInfo(packageDescription, editor, incompletePkg);
                    suggestItems.add(packageDescription);
                }

            }
        }
    }
}
