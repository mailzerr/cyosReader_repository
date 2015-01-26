/*
 * Copyright (C) 2007-2014 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.android.fbreader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.test.ActivityUnitTestCase;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.android.fbreader.api.FBReaderIntents;
import org.geometerplus.android.fbreader.style.StyleListActivity;
import org.geometerplus.android.util.UIUtil;

public class SelectionBookmarkAction extends FBAndroidAction {
	SelectionBookmarkAction(FBReader baseApplication, FBReaderApp fbreader) {
		super(baseApplication, fbreader);
	}

	@Override
	protected void run(Object ... params) {
		final boolean existingBookmark;
		final Bookmark bookmark;
		if (params.length != 0) {
			existingBookmark = true;
			bookmark = (Bookmark)params[0];
			final Intent intent = new Intent(BaseActivity.getApplicationContext(), StyleListActivity.class);
			FBReaderIntents.putBookmarkExtra(intent, bookmark);
			intent.putExtra(StyleListActivity.EXISTING_BOOKMARK_KEY, existingBookmark);
			OrientationUtil.startActivity(BaseActivity, intent);

		} else {
			existingBookmark = false;
			//show dialog in activity:
			//*/*/*/*/*/*/*/*/*/*/*/*/
			final FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
			Activity act = (Activity) fbreader.getMyWindow();
			AlertDialog.Builder alert = new AlertDialog.Builder(act);//.getApplicationContext()); // !!
			alert.setTitle("Geben Sie bitte ein Stichwort für Ihre Auswahl ein:");
			LayoutInflater inflater = act.getLayoutInflater();
			RelativeLayout relLayout = (RelativeLayout) act.findViewById(R.id.root_view);
			final View dialogLayout = inflater.inflate(R.layout.fragment_edit_name, relLayout, false);
			alert.setView(dialogLayout);
			
			alert.setPositiveButton("Speichern",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						final FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
						Activity act = (Activity) fbreader.getMyWindow();
						SharedPreferences sharedPreferences = act.getSharedPreferences("myHeading", Context.MODE_MULTI_PROCESS);
						final EditText heading = (EditText) dialogLayout.findViewById(R.id.edit_heading);
						SharedPreferences.Editor editor = sharedPreferences.edit();
						editor.putString("myHeading", heading.getText().toString());
						editor.commit();
						final Bookmark bookmark;
						bookmark = Reader.addSelectionBookmark(heading.getText().toString());
						
						final Intent intent = new Intent(BaseActivity.getApplicationContext(), StyleListActivity.class);
						FBReaderIntents.putBookmarkExtra(intent, bookmark);
						//achtung wegen false!! 1 Zeile unten
						intent.putExtra(StyleListActivity.EXISTING_BOOKMARK_KEY, existingBookmark);
						OrientationUtil.startActivity(BaseActivity, intent);
						dialog.cancel();
					}
				});
			
			alert.setNegativeButton("Text direkt übernehmen",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							final Bookmark bookmark;
							bookmark = Reader.addSelectionBookmark("");
							
							final Intent intent = new Intent(BaseActivity.getApplicationContext(), StyleListActivity.class);
							FBReaderIntents.putBookmarkExtra(intent, bookmark);
							//achtung wegen false!! 1 Zeile unten
							intent.putExtra(StyleListActivity.EXISTING_BOOKMARK_KEY, existingBookmark);
							OrientationUtil.startActivity(BaseActivity, intent);
							dialog.cancel();
						}
			});
			WindowManager.LayoutParams wmlp = act.getWindow().getAttributes();
			wmlp.gravity = Gravity.BOTTOM;
			wmlp.x = 50; // x position
			wmlp.y = 50; // y position
			
			alert.show().getWindow().setLayout(500, 500);
//			AlertDialog alertDialog = alert.create();
//	        alertDialog.show();
			//////////////////////////////////////////////////////////////////////////////////////
			//get heading
			SharedPreferences sharedPreferences = act.getSharedPreferences("myHeading", Context.MODE_MULTI_PROCESS);
			String heading = sharedPreferences.getString("myHeading", "");
			//myHeading leeren:
			/*SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putString("myHeading", "");
			editor.commit();
	*/

			
//			bookmark = Reader.addSelectionBookmark(heading);
			
			
			/*UIUtil.showMessageText(
				BaseActivity,
				ZLResource.resource("selection").getResource("bookmarkCreated").getValue()
					.replace("%s", bookmark.getText())
			);*/
		}
	}
	
	
}
