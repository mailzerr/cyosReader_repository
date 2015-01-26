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

import java.util.ArrayList;

import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.ui.android.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ZoomButton;

abstract class ButtonsPopupPanel extends PopupPanel implements View.OnClickListener {
	class ActionButton extends ZoomButton {
		final String ActionId;
		final boolean IsCloseButton;

		ActionButton(Context context, String actionId, boolean isCloseButton) {
			super(context);
			ActionId = actionId;
			IsCloseButton = isCloseButton;
		}
	}

	private final ArrayList<ActionButton> myButtons = new ArrayList<ActionButton>();

	ButtonsPopupPanel(FBReaderApp fbReader) {
		super(fbReader);
	}

	protected void addButton(String actionId, boolean isCloseButton, int imageId) {
		final ActionButton button = new ActionButton(myWindow.getContext(), actionId, isCloseButton);
		button.setImageResource(imageId);
		myWindow.addView(button);
		button.setOnClickListener(this);
		myButtons.add(button);
	}

	@Override
	protected void update() {
		for (ActionButton button : myButtons) {
			button.setEnabled(Application.isActionEnabled(button.ActionId));
		}
	}

	public void onClick(View view) {
		final ActionButton button = (ActionButton)view;
		
		//TESTTESTTESTTESTTESTTESTTEST
		/*
		final FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
		Activity act = (Activity) fbreader.getMyWindow();
		act.runOnUiThread(new Runnable() {
        public void run() {
			final FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
			Activity act = (Activity) fbreader.getMyWindow();
			AlertDialog ad = new AlertDialog.Builder(act).create();
			ad.setCancelable(false);
			ad.setTitle("Exportieren erfolgreich abgeschlossen");
			ad.setMessage("Sie finden die exportierten Strukturelemente in der cyosReader Bibliothek.\n" + "Dateiname: " + "hallo");
			ad.setButton("Schlieﬂen", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
			}
			});
			ad.show();
               }
           });
           */
		//TESTTESTTESTTESTTESTTESTTEST
		
		Application.runAction(button.ActionId);
		if (button.IsCloseButton) {
			storePosition();
			StartPosition = null;
			Application.hideActivePopup();
		}
	}
}
