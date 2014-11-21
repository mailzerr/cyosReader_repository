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

import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.FBView;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.ui.android.R;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

public class SelectionWebSearchAction extends FBAndroidAction {
	protected String userChoice;
	
	SelectionWebSearchAction(FBReader baseActivity, FBReaderApp fbreader, String choice) {
		super(baseActivity, fbreader);
		userChoice = choice;
	}

	@Override
	protected void run(Object ... params) {
		final FBView fbview = Reader.getTextView();
		
		WebSearchFragment myWVFrag = new WebSearchFragment(fbview.getSelectedText(), userChoice);
		final FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
		Activity act = (Activity) fbreader.getMyWindow(); //Mein ERfolg!!! Wichtig für Interface-basierte Kommunikation mit dem Fragment
		
		FragmentManager fm = act.getFragmentManager(); // meinen einzigen FragmentManager holen
		FragmentTransaction transaction = fm.beginTransaction();

		Fragment StructElFrag = fm.findFragmentByTag("StructureElementsFragmentTag");
//		hide strElFrag:
		transaction.detach(StructElFrag); 
		
//		entferne das geöffnete Fragment bis auf den Strukturbereich:
//		TODO: überprüfen, was passiert, wenn es mehrere Fragmente auf dem Stack sind
		if (fm.getBackStackEntryCount() > 1)	{
			fm.popBackStack();
		}
	
		transaction.add(R.id.fragment_container, myWVFrag, "websearch");
		transaction.addToBackStack("websearch");
		transaction.commit();
		
		fbview.clearSelection();
	}
}
