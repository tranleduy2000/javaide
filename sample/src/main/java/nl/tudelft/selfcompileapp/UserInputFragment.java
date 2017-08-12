package nl.tudelft.selfcompileapp;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;

/**
 * Keeps track of the changes made by the user.
 *
 * @author Paul Brussee
 *
 */
public class UserInputFragment extends Fragment {

	String appName;
	Bitmap appIcon;
	String appPackage;
	String appTheme;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	////////////////// GETTERS ////////////////////

	Bitmap getAppIcon(Context appContext) {
		try {
			InputStream is = appContext.getAssets().open(S.pngAppIcon.getName());
			Bitmap b = BitmapFactory.decodeStream(is);
			appIcon = Bitmap.createScaledBitmap(b, ModifyDrawables.XXHDPI_ICON_PIXELS,
					ModifyDrawables.XXHDPI_ICON_PIXELS, false);
			is.close();
			b.recycle();

			System.out.println("Icon: assets/" + S.pngAppIcon.getName());

		} catch (Exception e) {
			e.printStackTrace();
		}
		return appIcon;
	}

	String getAppName() {
		try {
			Document dom = Util.readXml(S.xmlStrings);
			appName = dom.getElementsByTagName("string").item(0).getTextContent();

			System.out.println("Name: " + appName);

		} catch (Exception e) {
			e.printStackTrace();
		}

		// Set name of the final output file
		S.apkRedistributable = new File(S.dirRoot, appName + ".apk");

		return appName;
	}

	String getAppTheme() {
		try {
			Document dom = Util.readXml(S.xmlStyles);

			NodeList lstNode = dom.getElementsByTagName("style");
			for (int i = 0; i < lstNode.getLength(); i++) {
				Node node = lstNode.item(i);

				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element elmnt = (Element) node;
					appTheme = elmnt.getAttribute("parent").replace("android:", "");

					System.out.println("Theme: " + appTheme);
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return appTheme;
	}

	Integer getAppThemeId() throws Exception {
		String fieldName = appTheme.replace(".", "_");
		System.out.println("android.R.style." + fieldName);

		Field f = android.R.style.class.getField(fieldName);
		Class<?> t = f.getType();
		if (t == int.class) {
			return f.getInt(null);
		}
		return null;
	}

	String getAppPackage() {
		try {
			Document dom = Util.readXml(S.xmlMan);
			appPackage = dom.getDocumentElement().getAttributes().getNamedItem("package").getNodeValue();

			System.out.println("Package: " + appPackage);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return appPackage;
	}

	////////////////// SETTERS ////////////////////

	void setAppIcon(Bitmap icon) {
		appIcon = icon;

		if (isAdded()) {
			SelfCompileActivity activity = (SelfCompileActivity) getActivity();
			activity.taskManager.modifyDrawables(activity, null);
		}
	}

	void setAppName(String name) {
		appName = name;

		if (isAdded()) {
			SelfCompileActivity activity = (SelfCompileActivity) getActivity();
			activity.taskManager.modifyStrings(activity, null);
		}
	}

	void setAppTheme(String theme) {
		appTheme = theme;

		if (isAdded()) {
			SelfCompileActivity activity = (SelfCompileActivity) getActivity();
			activity.taskManager.modifyStyles(activity, null);
		}
	}

	void setAppPackage(String packagePath) {
		appPackage = packagePath;

		if (isAdded()) {
			SelfCompileActivity activity = (SelfCompileActivity) getActivity();
			activity.taskManager.modifySource(activity, null);
		}
	}

}
