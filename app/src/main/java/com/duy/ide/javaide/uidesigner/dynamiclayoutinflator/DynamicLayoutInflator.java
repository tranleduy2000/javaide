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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class DynamicLayoutInflator {
    public static final int NO_LAYOUT_RULE = -999;
    public static final String[] CORNERS = {"TopLeft", "TopRight", "BottomRight", "BottomLeft"};
    private static final String ns = null;
    public static int highestIdNumberUsed = 1234567;
    public static Map<String, ViewParamRunnable> viewRunnables;
    private static ImageLoader imageLoader = null;

    public static void setImageLoader(ImageLoader il) {
        imageLoader = il;
    }

    public static void setDelegate(View root, Object delegate) {
        DynamicLayoutInfo info;
        if (root.getTag() == null || !(root.getTag() instanceof DynamicLayoutInfo)) {
            info = new DynamicLayoutInfo();
            root.setTag(info);
        } else {
            info = (DynamicLayoutInfo) root.getTag();
        }
        info.delegate = delegate;
    }

    public static View inflateName(Context context, String name) {
        return inflateName(context, name, null);
    }

    public static View inflateName(Context context, String name, ViewGroup parent) {
        if (name.startsWith("<")) {
            // Assume it's XML
            return DynamicLayoutInflator.inflate(context, name, parent);
        } else {
            File savedFile = context.getFileStreamPath(name + ".xml");
            try {
                InputStream fileStream = new FileInputStream(savedFile);
                return DynamicLayoutInflator.inflate(context, fileStream, parent);
            } catch (FileNotFoundException e) {
            }
            try {
                InputStream assetStream = context.getAssets().open(name + ".xml");
                return DynamicLayoutInflator.inflate(context, assetStream, parent);
            } catch (IOException e) {
            }
            int id = context.getResources().getIdentifier(name, "layout", context.getPackageName());
            if (id > 0) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                return inflater.inflate(id, parent, false);
            }
        }
        return null;
    }

    public static View inflate(Context context, File xmlPath) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(xmlPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return DynamicLayoutInflator.inflate(context, inputStream);
    }

    public static View inflate(Context context, String xml) {
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes());
        return DynamicLayoutInflator.inflate(context, inputStream);
    }

    public static View inflate(Context context, String xml, ViewGroup parent) {
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes());
        return DynamicLayoutInflator.inflate(context, inputStream, parent);
    }

    public static View inflate(Context context, InputStream inputStream) {
        return inflate(context, inputStream, null);
    }

    public static View inflate(Context context, InputStream inputStream, ViewGroup parent) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(inputStream);
            try {
                return inflate(context, document.getDocumentElement(), parent);
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static View inflate(Context context, Node node) {
        return inflate(context, node, null);
    }

    public static View inflate(Context context, Node node, ViewGroup parent) {
        View mainView = getViewForName(context, node.getNodeName());
        if (parent != null)
            parent.addView(mainView); // have to add to parent to enable certain layout attrs
        applyAttributes(mainView, getAttributesMap(node), parent);
        if (mainView instanceof ViewGroup && node.hasChildNodes()) {
            parseChildren(context, node, (ViewGroup) mainView);
        }
        return mainView;
    }

    private static void parseChildren(Context context, Node node, ViewGroup mainView) {
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);
            if (currentNode.getNodeType() != Node.ELEMENT_NODE) continue;
            inflate(context, currentNode, mainView); // this recursively can call parseChildren
        }
    }

    private static View getViewForName(Context context, String name) {
        try {
            if (!name.contains(".")) {
                name = "android.widget." + name;
            }
            Class<?> clazz = Class.forName(name);
            Constructor<?> constructor = clazz.getConstructor(Context.class);
            return (View) constructor.newInstance(context);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static HashMap<String, String> getAttributesMap(Node currentNode) {
        NamedNodeMap attributeMap = currentNode.getAttributes();
        int attributeCount = attributeMap.getLength();
        HashMap<String, String> attributes = new HashMap<>(attributeCount);
        for (int j = 0; j < attributeCount; j++) {
            Node attr = attributeMap.item(j);
            String nodeName = attr.getNodeName();
            if (nodeName.startsWith("android:")) nodeName = nodeName.substring(8);
            attributes.put(nodeName, attr.getNodeValue());
        }
        return attributes;
    }

    @SuppressLint("NewApi")
    private static void applyAttributes(View view, Map<String, String> attrs, ViewGroup parent) {
        if (viewRunnables == null) createViewRunnables();
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        int layoutRule;
        int marginLeft = 0, marginRight = 0, marginTop = 0, marginBottom = 0,
                paddingLeft = 0, paddingRight = 0, paddingTop = 0, paddingBottom = 0;
        boolean hasCornerRadius = false, hasCornerRadii = false;
        for (Map.Entry<String, String> entry : attrs.entrySet()) {
            String attr = entry.getKey();
            if (viewRunnables.containsKey(attr)) {
                viewRunnables.get(attr).apply(view, entry.getValue(), parent, attrs);
                continue;
            }
            if (attr.startsWith("cornerRadius")) {
                hasCornerRadius = true;
                hasCornerRadii = !attr.equals("cornerRadius");
                continue;
            }
            layoutRule = NO_LAYOUT_RULE;
            boolean layoutTarget = false;
            switch (attr) {
                case "id":
                    String idValue = parseId(entry.getValue());
                    if (parent != null) {
                        DynamicLayoutInfo info = getDynamicLayoutInfo(parent);
                        int newId = highestIdNumberUsed++;
                        view.setId(newId);
                        info.nameToIdNumber.put(idValue, newId);
                    }
                    break;
                case "width":
                case "layout_width":
                    switch (entry.getValue()) {
                        case "wrap_content":
                            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                            break;
                        case "fill_parent":
                        case "match_parent":
                            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                            break;
                        default:
                            layoutParams.width = DimensionConverter.stringToDimensionPixelSize(entry.getValue(), view.getResources().getDisplayMetrics(), parent, true);
                            break;
                    }
                    break;
                case "height":
                case "layout_height":
                    switch (entry.getValue()) {
                        case "wrap_content":
                            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                            break;
                        case "fill_parent":
                        case "match_parent":
                            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                            break;
                        default:
                            layoutParams.height = DimensionConverter.stringToDimensionPixelSize(entry.getValue(), view.getResources().getDisplayMetrics(), parent, false);
                            break;
                    }
                    break;
                case "layout_gravity":
                    if (parent != null && parent instanceof LinearLayout) {
                        ((LinearLayout.LayoutParams) layoutParams).gravity = parseGravity(entry.getValue());
                    } else if (parent != null && parent instanceof FrameLayout) {
                        ((FrameLayout.LayoutParams) layoutParams).gravity = parseGravity(entry.getValue());
                    }
                    break;
                case "layout_weight":
                    if (parent != null && parent instanceof LinearLayout) {
                        ((LinearLayout.LayoutParams) layoutParams).weight = Float.parseFloat(entry.getValue());
                    }
                    break;
                case "layout_below":
                    layoutRule = RelativeLayout.BELOW;
                    layoutTarget = true;
                    break;
                case "layout_above":
                    layoutRule = RelativeLayout.ABOVE;
                    layoutTarget = true;
                    break;
                case "layout_toLeftOf":
                    layoutRule = RelativeLayout.LEFT_OF;
                    layoutTarget = true;
                    break;
                case "layout_toRightOf":
                    layoutRule = RelativeLayout.RIGHT_OF;
                    layoutTarget = true;
                    break;
                case "layout_alignBottom":
                    layoutRule = RelativeLayout.ALIGN_BOTTOM;
                    layoutTarget = true;
                    break;
                case "layout_alignTop":
                    layoutRule = RelativeLayout.ALIGN_TOP;
                    layoutTarget = true;
                    break;
                case "layout_alignLeft":
                case "layout_alignStart":
                    layoutRule = RelativeLayout.ALIGN_LEFT;
                    layoutTarget = true;
                    break;
                case "layout_alignRight":
                case "layout_alignEnd":
                    layoutRule = RelativeLayout.ALIGN_RIGHT;
                    layoutTarget = true;
                    break;
                case "layout_alignParentBottom":
                    layoutRule = RelativeLayout.ALIGN_PARENT_BOTTOM;
                    break;
                case "layout_alignParentTop":
                    layoutRule = RelativeLayout.ALIGN_PARENT_TOP;
                    break;
                case "layout_alignParentLeft":
                case "layout_alignParentStart":
                    layoutRule = RelativeLayout.ALIGN_PARENT_LEFT;
                    break;
                case "layout_alignParentRight":
                case "layout_alignParentEnd":
                    layoutRule = RelativeLayout.ALIGN_PARENT_RIGHT;
                    break;
                case "layout_centerHorizontal":
                    layoutRule = RelativeLayout.CENTER_HORIZONTAL;
                    break;
                case "layout_centerVertical":
                    layoutRule = RelativeLayout.CENTER_VERTICAL;
                    break;
                case "layout_centerInParent":
                    layoutRule = RelativeLayout.CENTER_IN_PARENT;
                    break;
                case "layout_margin":
                    marginLeft = marginRight = marginTop = marginBottom = DimensionConverter.stringToDimensionPixelSize(entry.getValue(), view.getResources().getDisplayMetrics());
                    break;
                case "layout_marginLeft":
                    marginLeft = DimensionConverter.stringToDimensionPixelSize(entry.getValue(), view.getResources().getDisplayMetrics(), parent, true);
                    break;
                case "layout_marginTop":
                    marginTop = DimensionConverter.stringToDimensionPixelSize(entry.getValue(), view.getResources().getDisplayMetrics(), parent, false);
                    break;
                case "layout_marginRight":
                    marginRight = DimensionConverter.stringToDimensionPixelSize(entry.getValue(), view.getResources().getDisplayMetrics(), parent, true);
                    break;
                case "layout_marginBottom":
                    marginBottom = DimensionConverter.stringToDimensionPixelSize(entry.getValue(), view.getResources().getDisplayMetrics(), parent, false);
                    break;
                case "padding":
                    paddingBottom = paddingLeft = paddingRight = paddingTop = DimensionConverter.stringToDimensionPixelSize(entry.getValue(), view.getResources().getDisplayMetrics());
                    break;
                case "paddingLeft":
                    paddingLeft = DimensionConverter.stringToDimensionPixelSize(entry.getValue(), view.getResources().getDisplayMetrics());
                    break;
                case "paddingTop":
                    paddingTop = DimensionConverter.stringToDimensionPixelSize(entry.getValue(), view.getResources().getDisplayMetrics());
                    break;
                case "paddingRight":
                    paddingRight = DimensionConverter.stringToDimensionPixelSize(entry.getValue(), view.getResources().getDisplayMetrics());
                    break;
                case "paddingBottom":
                    paddingBottom = DimensionConverter.stringToDimensionPixelSize(entry.getValue(), view.getResources().getDisplayMetrics());
                    break;

            }
            if (layoutRule != NO_LAYOUT_RULE && parent instanceof RelativeLayout) {
                if (layoutTarget) {
                    int anchor = idNumFromIdString(parent, parseId(entry.getValue()));
                    ((RelativeLayout.LayoutParams) layoutParams).addRule(layoutRule, anchor);
                } else if (entry.getValue().equals("true")) {
                    ((RelativeLayout.LayoutParams) layoutParams).addRule(layoutRule);
                }
            }
        }
        // TODO: this is a giant mess; come up with a simpler way of deciding what to draw for the background
        if (attrs.containsKey("background") || attrs.containsKey("borderColor")) {
            String bgValue = attrs.containsKey("background") ? attrs.get("background") : null;
            if (bgValue != null && bgValue.startsWith("@drawable/")) {
                view.setBackground(getDrawableByName(view, bgValue));
            } else if (bgValue == null || bgValue.startsWith("#") || bgValue.startsWith("@color")) {
                if (view instanceof Button || attrs.containsKey("pressedColor")) {
                    int bgColor = parseColor(view, bgValue == null ? "#00000000" : bgValue);
                    int pressedColor;
                    if (attrs.containsKey("pressedColor")) {
                        pressedColor = parseColor(view, attrs.get("pressedColor"));
                    } else {
                        pressedColor = adjustBrightness(bgColor, 0.9f);
                    }
                    GradientDrawable gd = new GradientDrawable();
                    gd.setColor(bgColor);
                    GradientDrawable pressedGd = new GradientDrawable();
                    pressedGd.setColor(pressedColor);
                    if (hasCornerRadii) {
                        float radii[] = new float[8];
                        for (int i = 0; i < CORNERS.length; i++) {
                            String corner = CORNERS[i];
                            if (attrs.containsKey("cornerRadius" + corner)) {
                                radii[i * 2] = radii[i * 2 + 1] = DimensionConverter.stringToDimension(attrs.get("cornerRadius" + corner), view.getResources().getDisplayMetrics());
                            }
                            gd.setCornerRadii(radii);
                            pressedGd.setCornerRadii(radii);
                        }
                    } else if (hasCornerRadius) {
                        float cornerRadius = DimensionConverter.stringToDimension(attrs.get("cornerRadius"), view.getResources().getDisplayMetrics());
                        gd.setCornerRadius(cornerRadius);
                        pressedGd.setCornerRadius(cornerRadius);
                    }
                    if (attrs.containsKey("borderColor")) {
                        String borderWidth = "1dp";
                        if (attrs.containsKey("borderWidth")) {
                            borderWidth = attrs.get("borderWidth");
                        }
                        int borderWidthPx = DimensionConverter.stringToDimensionPixelSize(borderWidth, view.getResources().getDisplayMetrics());
                        gd.setStroke(borderWidthPx, parseColor(view, attrs.get("borderColor")));
                        pressedGd.setStroke(borderWidthPx, parseColor(view, attrs.get("borderColor")));
                    }
                    StateListDrawable selector = new StateListDrawable();
                    selector.addState(new int[]{android.R.attr.state_pressed}, pressedGd);
                    selector.addState(new int[]{}, gd);
                    view.setBackground(selector);
                    getDynamicLayoutInfo(view).bgDrawable = gd;
                } else if (hasCornerRadius || attrs.containsKey("borderColor")) {
                    GradientDrawable gd = new GradientDrawable();
                    int bgColor = parseColor(view, bgValue == null ? "#00000000" : bgValue);
                    gd.setColor(bgColor);
                    if (hasCornerRadii) {
                        float radii[] = new float[8];
                        for (int i = 0; i < CORNERS.length; i++) {
                            String corner = CORNERS[i];
                            if (attrs.containsKey("cornerRadius" + corner)) {
                                radii[i * 2] = radii[i * 2 + 1] = DimensionConverter.stringToDimension(attrs.get("cornerRadius" + corner), view.getResources().getDisplayMetrics());
                            }
                            gd.setCornerRadii(radii);
                        }
                    } else if (hasCornerRadius) {
                        float cornerRadius = DimensionConverter.stringToDimension(attrs.get("cornerRadius"), view.getResources().getDisplayMetrics());
                        gd.setCornerRadius(cornerRadius);
                    }
                    if (attrs.containsKey("borderColor")) {
                        String borderWidth = "1dp";
                        if (attrs.containsKey("borderWidth")) {
                            borderWidth = attrs.get("borderWidth");
                        }
                        gd.setStroke(DimensionConverter.stringToDimensionPixelSize(borderWidth, view.getResources().getDisplayMetrics()),
                                parseColor(view, attrs.get("borderColor")));
                    }
                    view.setBackground(gd);
                    getDynamicLayoutInfo(view).bgDrawable = gd;
                } else {
                    view.setBackgroundColor(parseColor(view, bgValue));
                }
            }
        }

        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) layoutParams).setMargins(marginLeft, marginTop, marginRight, marginBottom);
        }
        view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        view.setLayoutParams(layoutParams);
    }

    private static DynamicLayoutInfo getDynamicLayoutInfo(View parent) {
        DynamicLayoutInfo info;
        if (parent.getTag() != null && parent.getTag() instanceof DynamicLayoutInfo) {
            info = (DynamicLayoutInfo) parent.getTag();
        } else {
            info = new DynamicLayoutInfo();
            parent.setTag(info);
        }
        return info;
    }

    private static View.OnClickListener getClickListener(final ViewGroup myParent, final String methodName) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewGroup root = myParent;
                DynamicLayoutInfo info = null;
                while (root != null && (root.getParent() instanceof ViewGroup)) {
                    if (root.getTag() != null && root.getTag() instanceof DynamicLayoutInfo) {
                        info = (DynamicLayoutInfo) root.getTag();
                        if (info.delegate != null) break;
                    }
                    root = (ViewGroup) root.getParent();
                }
                if (info != null && info.delegate != null) {
                    final Object delegate = info.delegate;
                    invokeMethod(delegate, methodName, false, view);
                } else {
                    Log.e("DynamicLayoutInflator", "Unable to find valid delegate for click named " + methodName);
                }
            }

            private void invokeMethod(Object delegate, final String methodName, boolean withView, View view) {
                Object[] args = null;
                String finalMethod = methodName;
                if (methodName.endsWith(")")) {
                    String[] parts = methodName.split("[(]", 2);
                    finalMethod = parts[0];
                    try {
                        String argText = parts[1].replace("&quot;", "\"");
                        JSONArray arr = new JSONArray("[" + argText.substring(0, argText.length() - 1) + "]");
                        args = new Object[arr.length()];
                        for (int i = 0; i < arr.length(); i++) {
                            args[i] = arr.get(i);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (withView) {
                    args = new Object[1];
                    args[0] = view;
                }
                Class<?> klass = delegate.getClass();
                try {

                    Class<?>[] argClasses = null;
                    if (args != null && args.length > 0) {
                        argClasses = new Class[args.length];
                        if (withView) {
                            argClasses[0] = View.class;
                        } else {
                            for (int i = 0; i < args.length; i++) {
                                Class<?> argClass = args[i].getClass();
                                if (argClass == Integer.class)
                                    argClass = int.class; // Nobody uses Integer...
                                argClasses[i] = argClass;
                            }
                        }
                    }
                    Method method = klass.getMethod(finalMethod, argClasses);
                    method.invoke(delegate, args);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                    if (!withView && !methodName.endsWith(")")) {
                        invokeMethod(delegate, methodName, true, view);
                    }
                }
            }
        };
    }

    private static String parseId(String value) {
        if (value.startsWith("@+id/")) {
            return value.substring(5);
        } else if (value.startsWith("@id/")) {
            return value.substring(4);
        }
        return value;
    }

    private static int parseGravity(String value) {
        int gravity = Gravity.NO_GRAVITY;
        String[] parts = value.toLowerCase().split("[|]");
        for (String part : parts) {
            switch (part) {
                case "center":
                    gravity = gravity | Gravity.CENTER;
                    break;
                case "left":
                case "textStart":
                    gravity = gravity | Gravity.LEFT;
                    break;
                case "right":
                case "textEnd":
                    gravity = gravity | Gravity.RIGHT;
                    break;
                case "top":
                    gravity = gravity | Gravity.TOP;
                    break;
                case "bottom":
                    gravity = gravity | Gravity.BOTTOM;
                    break;
                case "center_horizontal":
                    gravity = gravity | Gravity.CENTER_HORIZONTAL;
                    break;
                case "center_vertical":
                    gravity = gravity | Gravity.CENTER_VERTICAL;
                    break;
            }
        }
        return gravity;
    }

    public static int idNumFromIdString(View view, String id) {
        if (!(view instanceof ViewGroup)) return 0;
        Object tag = view.getTag();
        if (!(tag instanceof DynamicLayoutInfo)) return 0; // not inflated by this class
        DynamicLayoutInfo info = (DynamicLayoutInfo) view.getTag();
        if (!info.nameToIdNumber.containsKey(id)) {
            ViewGroup grp = (ViewGroup) view;
            for (int i = 0; i < grp.getChildCount(); i++) {
                int val = idNumFromIdString(grp.getChildAt(i), id);
                if (val != 0) return val;
            }
            return 0;
        }
        return info.nameToIdNumber.get(id);
    }

    @Nullable
    public static View findViewByIdString(View view, String id) {
        int idNum = idNumFromIdString(view, id);
        if (idNum == 0) return null;
        return view.findViewById(idNum);
    }

    public static int parseColor(View view, String text) {
        if (text.startsWith("@color/")) {
            Resources resources = view.getResources();
            return resources.getColor(resources.getIdentifier(text.substring("@color/".length()), "color", view.getContext().getPackageName()));
        }
        if (text.length() == 4 && text.startsWith("#")) {
            text = "#" + text.charAt(1) + text.charAt(1) + text.charAt(2) + text.charAt(2) + text.charAt(3) + text.charAt(3);
        }
        return Color.parseColor(text);
    }

    public static int adjustBrightness(int color, float amount) {
        int red = color & 0xFF0000 >> 16;
        int green = color & 0x00FF00 >> 8;
        int blue = color & 0x0000FF;
        int result = (int) (blue * amount);
        result += (int) (green * amount) << 8;
        result += (int) (red * amount) << 16;
        return result;
    }

    public static Drawable getDrawableByName(View view, String name) {
        Resources resources = view.getResources();
        return resources.getDrawable(resources.getIdentifier(name, "drawable",
                view.getContext().getPackageName()));
    }

    public static void createViewRunnables() {
        viewRunnables = new HashMap<>(30);
        viewRunnables.put("scaleType", new ViewParamRunnable() {
            @Override
            public void apply(View view, String value, ViewGroup parent, Map<String, String> attrs) {
                if (view instanceof ImageView) {
                    ImageView.ScaleType scaleType = ((ImageView) view).getScaleType();
                    switch (value.toLowerCase()) {
                        case "center":
                            scaleType = ImageView.ScaleType.CENTER;
                            break;
                        case "center_crop":
                            scaleType = ImageView.ScaleType.CENTER_CROP;
                            break;
                        case "center_inside":
                            scaleType = ImageView.ScaleType.CENTER_INSIDE;
                            break;
                        case "fit_center":
                            scaleType = ImageView.ScaleType.FIT_CENTER;
                            break;
                        case "fit_end":
                            scaleType = ImageView.ScaleType.FIT_END;
                            break;
                        case "fit_start":
                            scaleType = ImageView.ScaleType.FIT_START;
                            break;
                        case "fit_xy":
                            scaleType = ImageView.ScaleType.FIT_XY;
                            break;
                        case "matrix":
                            scaleType = ImageView.ScaleType.MATRIX;
                            break;
                    }
                    ((ImageView) view).setScaleType(scaleType);
                }
            }
        });
        viewRunnables.put("orientation", new ViewParamRunnable() {
            @Override
            public void apply(View view, String value, ViewGroup parent, Map<String, String> attrs) {
                if (view instanceof LinearLayout) {
                    ((LinearLayout) view).setOrientation(value.equals("vertical") ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
                }
            }
        });
        viewRunnables.put("text", new ViewParamRunnable() {
            @Override
            public void apply(View view, String value, ViewGroup parent, Map<String, String> attrs) {
                if (view instanceof TextView) {
                    ((TextView) view).setText(value);
                }
            }
        });
        viewRunnables.put("textSize", new ViewParamRunnable() {
            @Override
            public void apply(View view, String value, ViewGroup parent, Map<String, String> attrs) {
                if (view instanceof TextView) {
                    ((TextView) view).setTextSize(TypedValue.COMPLEX_UNIT_PX, DimensionConverter.stringToDimension(value, view.getResources().getDisplayMetrics()));
                }
            }
        });
        viewRunnables.put("textColor", new ViewParamRunnable() {
            @Override
            public void apply(View view, String value, ViewGroup parent, Map<String, String> attrs) {
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(parseColor(view, value));
                }
            }
        });
        viewRunnables.put("textStyle", new ViewParamRunnable() {
            @Override
            public void apply(View view, String value, ViewGroup parent, Map<String, String> attrs) {
                if (view instanceof TextView) {
                    int typeFace = Typeface.NORMAL;
                    if (value.contains("bold")) typeFace |= Typeface.BOLD;
                    else if (value.contains("italic")) typeFace |= Typeface.ITALIC;
                    ((TextView) view).setTypeface(null, typeFace);
                }
            }
        });
        viewRunnables.put("textAlignment", new ViewParamRunnable() {
            @Override
            public void apply(View view, String value, ViewGroup parent, Map<String, String> attrs) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                    int alignment = View.TEXT_ALIGNMENT_TEXT_START;
                    switch (value) {
                        case "center":
                            alignment = View.TEXT_ALIGNMENT_CENTER;
                            break;
                        case "left":
                        case "textStart":
                            break;
                        case "right":
                        case "textEnd":
                            alignment = View.TEXT_ALIGNMENT_TEXT_END;
                            break;
                    }
                    view.setTextAlignment(alignment);
                } else {
                    int gravity = Gravity.LEFT;
                    switch (value) {
                        case "center":
                            gravity = Gravity.CENTER;
                            break;
                        case "left":
                        case "textStart":
                            break;
                        case "right":
                        case "textEnd":
                            gravity = Gravity.RIGHT;
                            break;
                    }
                    ((TextView) view).setGravity(gravity);
                }
            }
        });
        viewRunnables.put("ellipsize", new ViewParamRunnable() {
            @Override
            public void apply(View view, String value, ViewGroup parent, Map<String, String> attrs) {
                if (view instanceof TextView) {
                    TextUtils.TruncateAt where = TextUtils.TruncateAt.END;
                    switch (value) {
                        case "start":
                            where = TextUtils.TruncateAt.START;
                            break;
                        case "middle":
                            where = TextUtils.TruncateAt.MIDDLE;
                            break;
                        case "marquee":
                            where = TextUtils.TruncateAt.MARQUEE;
                            break;
                        case "end":
                            break;
                    }
                    ((TextView) view).setEllipsize(where);
                }
            }
        });
        viewRunnables.put("singleLine", new ViewParamRunnable() {
            @Override
            public void apply(View view, String value, ViewGroup parent, Map<String, String> attrs) {
                if (view instanceof TextView) {
                    ((TextView) view).setSingleLine();
                }
            }
        });
        viewRunnables.put("hint", new ViewParamRunnable() {
            @Override
            public void apply(View view, String value, ViewGroup parent, Map<String, String> attrs) {
                if (view instanceof EditText) {
                    ((EditText) view).setHint(value);
                }
            }
        });
        viewRunnables.put("inputType", new ViewParamRunnable() {
            @Override
            public void apply(View view, String value, ViewGroup parent, Map<String, String> attrs) {
                if (view instanceof TextView) {
                    int inputType = 0;
                    switch (value) {
                        case "textEmailAddress":
                            inputType |= InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
                            break;
                        case "number":
                            inputType |= InputType.TYPE_CLASS_NUMBER;
                            break;
                        case "phone":
                            inputType |= InputType.TYPE_CLASS_PHONE;
                            break;
                    }
                    if (inputType > 0) ((TextView) view).setInputType(inputType);
                }
            }
        });
        viewRunnables.put("gravity", new ViewParamRunnable() {
            @Override
            public void apply(View view, String value, ViewGroup parent, Map<String, String> attrs) {
                int gravity = parseGravity(value);
                if (view instanceof TextView) {
                    ((TextView) view).setGravity(gravity);
                } else if (view instanceof LinearLayout) {
                    ((LinearLayout) view).setGravity(gravity);
                } else if (view instanceof RelativeLayout) {
                    ((RelativeLayout) view).setGravity(gravity);
                }
            }
        });
        viewRunnables.put("src", new ViewParamRunnable() {
            @Override
            public void apply(View view, String value, ViewGroup parent, Map<String, String> attrs) {
                if (view instanceof ImageView) {
                    String imageName = value;
                    if (imageName.startsWith("//")) imageName = "http:" + imageName;
                    if (imageName.startsWith("http")) {
                        if (imageLoader != null) {
                            if (attrs.containsKey("cornerRadius")) {
                                int radius = DimensionConverter.stringToDimensionPixelSize(attrs.get("cornerRadius"), view.getResources().getDisplayMetrics());
                                imageLoader.loadRoundedImage((ImageView) view, imageName, radius);
                            } else {
                                imageLoader.loadImage((ImageView) view, imageName);
                            }
                        }
                    } else if (imageName.startsWith("@drawable/")) {
                        imageName = imageName.substring("@drawable/".length());
                        ((ImageView) view).setImageDrawable(getDrawableByName(view, imageName));
                    }
                }
            }
        });
        viewRunnables.put("visibility", new ViewParamRunnable() {
            @Override
            public void apply(View view, String value, ViewGroup parent, Map<String, String> attrs) {
                int visibility = View.VISIBLE;
                String visValue = value.toLowerCase();
                if (visValue.equals("gone")) visibility = View.GONE;
                else if (visValue.equals("invisible")) visibility = View.INVISIBLE;
                view.setVisibility(visibility);
            }
        });
        viewRunnables.put("clickable", new ViewParamRunnable() {
            @Override
            public void apply(View view, String value, ViewGroup parent, Map<String, String> attrs) {
                view.setClickable(value.equals("true"));
            }
        });
        viewRunnables.put("tag", new ViewParamRunnable() {
            @Override
            public void apply(View view, String value, ViewGroup parent, Map<String, String> attrs) {
                // Sigh, this is dangerous because we use tags for other purposes
                if (view.getTag() == null) view.setTag(value);
            }
        });
        viewRunnables.put("onClick", new ViewParamRunnable() {
            @Override
            public void apply(View view, String value, ViewGroup parent, Map<String, String> attrs) {
                view.setOnClickListener(getClickListener(parent, value));
            }
        });
    }

    public interface ViewParamRunnable {
        void apply(View view, String value, ViewGroup parent, Map<String, String> attrs);
    }

    public interface ImageLoader {
        void loadImage(ImageView view, String url);

        void loadRoundedImage(ImageView view, String url, int radius);
    }

    public static class DynamicLayoutInfo {
        public HashMap<String, Integer> nameToIdNumber;
        public Object delegate;
        public GradientDrawable bgDrawable;
        public DynamicLayoutInfo() {
            nameToIdNumber = new HashMap<>();
        }
    }

}
