package com.nolanlawson.logcat.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nolanlawson.logcat.R;
import com.nolanlawson.logcat.util.UtilLogger;

/**
 * Adapter that shows any apps in the system that respond to Intent.ACTION_SEND intents.  Filters out any apps in
 * the FILTER_LIST and, as a bonus, adds the option to just copy the body text to the clipboard.
 * @author nlawson
 *
 */
public class SenderAppAdapter extends ArrayAdapter<ResolveInfo> {
	
	// used to exclude facebook, but don't need to anymore because it works again
	public static final Set<String> FILTER_SET = Collections.emptySet();
	
	private static UtilLogger log = new UtilLogger(SenderAppAdapter.class);
	
	private Context mContext;
	
	public SenderAppAdapter(Context context, boolean addClipboard, SendLogDetails.AttachmentType attachmentType) {
		super(context, R.layout.chooser_row, new ArrayList<ResolveInfo>());
		
		mContext = getContext();		
		List<ResolveInfo> items = createItems(addClipboard, attachmentType);
		for (ResolveInfo item : items) {
			add(item);
		}
	}

	public void respondToClick(int position, String subject, String body, SendLogDetails.AttachmentType attachmentType, File attachment) {

		ResolveInfo launchable = getItem(position);
		ActivityInfo activity=launchable.activityInfo;
		
		if (launchable instanceof DummyClipboardLaunchable) {
			ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE); 
			
			clipboard.setText(body);
			Toast.makeText(mContext, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
		} else {
		
			ComponentName name= new ComponentName(activity.applicationInfo.packageName, activity.name);
			
			Intent actionSendIntent= createSendIntent(subject, body, attachmentType, attachment);
			actionSendIntent.setComponent(name);
	 
			mContext.startActivity(actionSendIntent);	
		}
	}
	
	private List<ResolveInfo> createItems(boolean addClipboard, SendLogDetails.AttachmentType attachmentType) {
		
		List<ResolveInfo> items = mContext.getPackageManager().queryIntentActivities(createDummyIntent(attachmentType), 0);
		
		log.d("items are: %s", items);
		
		filter(items);
		Collections.sort(items, new ResolveInfo.DisplayNameComparator(mContext.getPackageManager())); 

		if (addClipboard) {
			items.add(0, new DummyClipboardLaunchable());
		}
		
		
		log.d("items are: %s", items);
		
		return items;
	}
	
	 
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView==null) {
			convertView=newView(parent);
		}

		bindView(position, convertView);

		return(convertView);
	}
	
	private View newView(ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		return inflater.inflate(R.layout.chooser_row, parent, false);
	}

	private void bindView(int position, View row) {
		
		PackageManager packageManager = mContext.getPackageManager();
		ResolveInfo item = getItem(position);
		
		TextView label=(TextView)row.findViewById(android.R.id.title);

		label.setText(item.loadLabel(packageManager));

		ImageView iconImageView=(ImageView)row.findViewById(android.R.id.icon);

		Drawable drawableIcon = item.loadIcon(packageManager);
		
		if (drawableIcon != null) {
		
			Bitmap iconBitmap = resizeBitmap(drawableIcon);
			
			iconImageView.setImageBitmap(iconBitmap);
			iconImageView.setVisibility(View.VISIBLE);
		
		} else {
			iconImageView.setVisibility(View.INVISIBLE);
		}
	}
	
	private void filter(List<ResolveInfo> apps) {
		for (Iterator<ResolveInfo> iter = apps.iterator(); iter.hasNext();) {
			ResolveInfo resolveInfo = iter.next();
			
			if (FILTER_SET.contains(resolveInfo.activityInfo.name)) { 
				// forget facebook - it doesn't work with anything but URLs
				iter.remove();
			} 	
		}
	}	
	
	/**
	 * Create an intent just for querying available apps.
	 * @param moreThanOneAttachment
	 * @return
	 */
	private static Intent createDummyIntent(SendLogDetails.AttachmentType attachmentType) {
		Intent actionSendIntent = new Intent(android.content.Intent.ACTION_SEND);
		actionSendIntent.setType(attachmentType.getMimeType());
		
		return actionSendIntent;
	}
	
	private static Intent createSendIntent(String subject, String body, SendLogDetails.AttachmentType attachmentType, File attachment) {
		
		String action = android.content.Intent.ACTION_SEND;
		Intent actionSendIntent = new Intent(action);

		actionSendIntent.setType(attachmentType.getMimeType());
		actionSendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
		if (!TextUtils.isEmpty(body)) {
			actionSendIntent.putExtra(Intent.EXTRA_TEXT, body);
		}
		if (attachment != null) {
			Uri uri = Uri.fromFile(attachment);
			log.d("uri is: %s", uri);
			actionSendIntent.putExtra(Intent.EXTRA_STREAM, uri);
		}
		
		return actionSendIntent;
	}
	
	private Bitmap resizeBitmap(Drawable drawable) {
		// resize the icon bitmap, because sometimes, seemingly randomly, an icon from the PackageManager 
		// will be too large
		
		int iconSize = mContext.getResources().getDimensionPixelSize(android.R.dimen.app_icon_size);
				
		Bitmap bmp = Bitmap.createBitmap(iconSize, iconSize, Config.ARGB_8888);
		Canvas c = new Canvas(bmp);
		drawable.setBounds(new Rect(0,0,iconSize,iconSize));
		drawable.draw(c);

		return bmp;
	}
	
	private class DummyClipboardLaunchable extends ResolveInfo {

			
		public Drawable loadIcon(PackageManager pm) {
			return null;
		}

		public CharSequence loadLabel(PackageManager pm) {
			return mContext.getResources().getString(R.string.copy_to_clipboard);
		}
		
		public String toString() {
			return "Dummy";
		}
	}
}