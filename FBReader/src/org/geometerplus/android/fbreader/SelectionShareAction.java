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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

public class SelectionShareAction extends FBAndroidAction {
	SelectionShareAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader);
	}

	@Override
	protected void run(Object ... params) {
		final String text = Reader.getTextView().getSelectedText();
		final String title = Reader.getCurrentBook().getTitle();
		Reader.getTextView().clearSelection();

		final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(android.content.Intent.EXTRA_SUBJECT,
			ZLResource.resource("selection").getResource("quoteFrom").getValue().replace("%s", title)
		);
		intent.putExtra(android.content.Intent.EXTRA_TEXT, text);
		
		//*/*/*/*/*/*/*/*/
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
						// Speichern in SharedPreferences
						final FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
						Activity act = (Activity) fbreader.getMyWindow();
						SharedPreferences sharedPreferences = act.getSharedPreferences("myHeading", Context.MODE_MULTI_PROCESS);
						final EditText heading = (EditText) dialogLayout.findViewById(R.id.edit_heading);
						SharedPreferences.Editor editor = sharedPreferences.edit();
						editor.putString("myHeading", heading.getText().toString());
						editor.commit();
						dialog.cancel();
					}
				});
		
		alert.setNegativeButton("Ausgewählten Text direkt übernehmen",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						//myHeading leeren
						final FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
						Activity act = (Activity) fbreader.getMyWindow();
						SharedPreferences sharedPreferences = act.getSharedPreferences("myHeading", Context.MODE_MULTI_PROCESS);
						SharedPreferences.Editor editor = sharedPreferences.edit();
						editor.putString("myHeading", "");
						editor.commit();
						dialog.cancel();
					}
		});
		WindowManager.LayoutParams wmlp = act.getWindow().getAttributes();
		wmlp.gravity = Gravity.BOTTOM;
		wmlp.x = 50; // x position
		wmlp.y = 50; // y position
		
		alert.show().getWindow().setLayout(500, 500);
		
		//*/*/*/*/*/*/*/*/
		
		//BaseActivity.startActivity(Intent.createChooser(intent, null));
	}
}
