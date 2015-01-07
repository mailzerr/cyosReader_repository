/*
 * Copyright (C) 2010-2014 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.android.fbreader.image.ImageViewActivity;
import org.geometerplus.fbreader.bookmodel.FBHyperlinkType;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.FBView;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.image.ZLFileImage;
import org.geometerplus.zlibrary.core.image.ZLImageData;
import org.geometerplus.zlibrary.core.image.ZLImageManager;
import org.geometerplus.zlibrary.text.view.ZLTextHyperlink;
import org.geometerplus.zlibrary.text.view.ZLTextHyperlinkRegionSoul;
import org.geometerplus.zlibrary.text.view.ZLTextImageRegionSoul;
import org.geometerplus.zlibrary.text.view.ZLTextRegion;
import org.geometerplus.zlibrary.text.view.ZLTextWordRegionSoul;
import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

class ProcessHyperlinkAction extends FBAndroidAction {
	ProcessHyperlinkAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader);
	}

	@Override
	public boolean isEnabled() {
		return Reader.getTextView().getSelectedRegion() != null;
	}

	@Override
	protected void run(Object ... params) {
		final ZLTextRegion region = Reader.getTextView().getSelectedRegion();
		if (region == null) {
			return;
		}

		final ZLTextRegion.Soul soul = region.getSoul();
		if (soul instanceof ZLTextHyperlinkRegionSoul) {
			Reader.getTextView().hideSelectedRegionBorder();
			Reader.getViewWidget().repaint();
			final ZLTextHyperlink hyperlink = ((ZLTextHyperlinkRegionSoul)soul).Hyperlink;
			switch (hyperlink.Type) {
				case FBHyperlinkType.EXTERNAL:
					openInBrowser(hyperlink.Id);
					break;
				case FBHyperlinkType.INTERNAL:
					Reader.Collection.markHyperlinkAsVisited(Reader.getCurrentBook(), hyperlink.Id);
					Reader.tryOpenFootnote(hyperlink.Id);
					break;
			}
		} else if (soul instanceof ZLTextImageRegionSoul) {
			Reader.getTextView().hideSelectedRegionBorder();
			Reader.getViewWidget().repaint();
			final String url = ((ZLTextImageRegionSoul)soul).ImageElement.URL;
			if (url != null) {
				try {
					final Intent intent = new Intent();
					intent.setClass(BaseActivity, ImageViewActivity.class);
					intent.setData(Uri.parse(url));
//					new stuff
//					ImageShowFragment myImgFragment = new ImageShowFragment("file:///sdcard/epub/testbuch/imgname.png"); //funktioniert
					final Uri uri = Uri.parse(url);
					final ZLFileImage image = ZLFileImage.byUrlPath(uri.getPath());
					final ZLImageData imageData = ZLImageManager.Instance().getImageData(image);
					Bitmap myBitmap = ((ZLAndroidImageData)imageData).getFullSizeBitmap();

					ImageShowFragment myImgFragment = new ImageShowFragment(myBitmap);
					
					FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
					Activity act = (Activity) fbreader.getMyWindow(); 
					FragmentManager fm = act.getFragmentManager(); // Referenz auf meinen einzigen FragmentManager holen
					FragmentTransaction transaction = fm.beginTransaction();

					Fragment StructElFrag = fm.findFragmentByTag("StructureElementsFragmentTag");
					transaction.detach(StructElFrag); // hide the structure elements fragment 
					if (fm.getBackStackEntryCount() > 1) {
						fm.popBackStack();
					}
					
					transaction.add(R.id.fragment_container, myImgFragment, "ImageFragmentTag");
					transaction.addToBackStack("ImageFragmentTag");
					transaction.commit();
					return;
					//end new stuff
					
					//löschen:
//					intent.putExtra(
//						ImageViewActivity.BACKGROUND_COLOR_KEY,
//						Reader.ImageOptions.ImageViewBackground.getValue().intValue()
//					);
//					OrientationUtil.startActivity(BaseActivity, intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else if (soul instanceof ZLTextWordRegionSoul) {
			DictionaryUtil.openWordInDictionary(
				BaseActivity, ((ZLTextWordRegionSoul)soul).Word, region
			);
		}
	}

	private void openInBrowser(final String url) {
		
		final FBView fbview = Reader.getTextView();
		WebSearchFragment myWVFrag = new WebSearchFragment(fbview.getSelectedText(), url);
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
