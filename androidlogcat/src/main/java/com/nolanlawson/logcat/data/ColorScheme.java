package com.nolanlawson.logcat.data;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.nolanlawson.logcat.R;
import com.nolanlawson.logcat.util.ArrayUtil;


public enum ColorScheme {
	Dark (R.string.pref_theme_choice_dark_value, R.color.main_background_dark, 
			R.color.main_foreground_dark, R.array.dark_theme_colors, R.color.spinner_droptown_dark,
			R.color.main_bubble_background_dark_2, false, R.color.yellow1),
	Light (R.string.pref_theme_choice_light_value, R.color.main_background_light, 
			R.color.main_foreground_light, R.array.light_theme_colors, R.color.spinner_droptown_light,
			R.color.main_bubble_background_light_2, true, R.color.main_bubble_background_light_2),
	Android (R.string.pref_theme_choice_android_value, R.color.main_background_android, 
			R.color.main_foreground_android, R.array.android_theme_colors, R.color.spinner_droptown_android,
			R.color.main_bubble_background_light, true, R.color.yellow1),
	Verizon (R.string.pref_theme_choice_verizon_value, R.color.main_background_verizon, 
			R.color.main_foreground_verizon, R.array.dark_theme_colors, R.color.spinner_droptown_verizon,
			R.color.main_bubble_background_verizon, false, R.color.yellow1),
	Att (R.string.pref_theme_choice_att_value, R.color.main_background_att, 
			R.color.main_foreground_att, R.array.light_theme_colors, R.color.spinner_droptown_att,
			R.color.main_bubble_background_light, true, R.color.main_bubble_background_light_2),
	Sprint (R.string.pref_theme_choice_sprint_value, R.color.main_background_sprint, 
			R.color.main_foreground_sprint, R.array.dark_theme_colors, R.color.spinner_droptown_sprint,
			R.color.main_bubble_background_dark, false, R.color.yellow1),
	Tmobile (R.string.pref_theme_choice_tmobile_value, R.color.main_background_tmobile, 
			R.color.main_foreground_tmobile, R.array.light_theme_colors, R.color.spinner_droptown_tmobile,
			R.color.main_bubble_background_tmobile, true, R.color.main_bubble_background_light_2),

	;
	
	private int nameResource;
	private int backgroundColorResource;
	private int foregroundColorResource;
	private int spinnerColorResource;
	private int bubbleBackgroundColorResource;
	private int tagColorsResource;
	private boolean useLightProgressBar;
	private int selectedColorResource;
	
	private int backgroundColor = -1;
	private int foregroundColor = -1;
	private int spinnerColor = -1;
	private int bubbleBackgroundColor = -1;
	private int selectedColor = -1;
	private int[] tagColors;
	
	private static Map<String, ColorScheme> preferenceNameToColorScheme = new HashMap<String, ColorScheme>();
	
	private ColorScheme(int nameResource, int backgroundColorResource, int foregroundColorResource,
			int tagColorsResource, int spinnerColorResource, int bubbleBackgroundColorResource,
			boolean useLightProgressBar, int selectedColorResource) {
		this.nameResource = nameResource;
		this.backgroundColorResource = backgroundColorResource;
		this.foregroundColorResource = foregroundColorResource;
		this.tagColorsResource = tagColorsResource;
		this.spinnerColorResource = spinnerColorResource;
		this.bubbleBackgroundColorResource = bubbleBackgroundColorResource;
		this.useLightProgressBar = useLightProgressBar;
		this.selectedColorResource = selectedColorResource;
	}

	public String getDisplayableName(Context context) {
		
		CharSequence[] themeChoiceValues = context.getResources().getStringArray(R.array.pref_theme_choices_values);
		int idx = ArrayUtil.indexOf(themeChoiceValues,context.getString(nameResource));
		return context.getResources().getStringArray(R.array.pref_theme_choices_names)[idx];
		
	}
	
	public int getNameResource() {
		return nameResource;
	}	
	
	public int getSelectedColor(Context context) {
		if (selectedColor == -1) {
			selectedColor = context.getResources().getColor(selectedColorResource);
		}
		return selectedColor;
	}
	
	public int getBackgroundColor(Context context) {
		if (backgroundColor == -1) {
			backgroundColor = context.getResources().getColor(backgroundColorResource);
		}
		return backgroundColor;
	}
	
	public int getForegroundColor(Context context) {
		if (foregroundColor == -1) {
			foregroundColor = context.getResources().getColor(foregroundColorResource);
		}
		return foregroundColor;
	}
	
	public int[] getTagColors(Context context) {
		if (tagColors == null) {
			tagColors = context.getResources().getIntArray(tagColorsResource);
		}
		return tagColors;
	}
	
	public int getSpinnerColor(Context context) {
		if (spinnerColor == -1) {
			spinnerColor = context.getResources().getColor(spinnerColorResource);
		}
		return spinnerColor;
	}
	
	public int getBubbleBackgroundColor(Context context) {
		if (bubbleBackgroundColor == -1) {
			bubbleBackgroundColor = context.getResources().getColor(bubbleBackgroundColorResource);
		}
		return bubbleBackgroundColor;
	}
	
	public boolean isUseLightProgressBar() {
		return useLightProgressBar;
	}

	public static ColorScheme findByPreferenceName(String name, Context context) {
		if (preferenceNameToColorScheme.isEmpty()) {
			// initialize map
			for (ColorScheme colorScheme : values()) {
				preferenceNameToColorScheme.put(context.getText(colorScheme.getNameResource()).toString(), colorScheme);
			}
		}
		return preferenceNameToColorScheme.get(name);
	}
}
