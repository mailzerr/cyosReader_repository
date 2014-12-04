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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.geometerplus.android.fbreader.api.FBReaderIntents;
import org.geometerplus.android.fbreader.library.BookInfoActivity;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.book.SerializerUtil;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

import android.content.Intent;

class ShowBookInfoAction extends FBAndroidAction {
	
	FBReaderApp fbrLocal;
	
	ShowBookInfoAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader);
		 fbrLocal = fbreader;
	}

	@Override
	public boolean isVisible() {
		return Reader.Model != null;
	}

	@Override
	protected void run(Object ... params) {
		final Intent intent =
			new Intent(BaseActivity.getApplicationContext(), BookInfoActivity.class)
				.putExtra(BookInfoActivity.FROM_READING_MODE_KEY, true);
		FBReaderIntents.putBookExtra(intent, Reader.getCurrentBook());
		/*
		
		  ///////////////////////////////////////
		Book b = Reader.getCurrentBook();
		String path = b.File.getPath(); 
		String fileExtension = path.substring(path.length() - 3, path.length());
		
		// // Wenn es sich um eine Datei mit Strukturelementen handelt (.txt) später: TODO .CYOS!!! 
		if(fileExtension.equalsIgnoreCase("txt")) {
		
			/////////////////////////////////////////////////
			List<String> readedStrElem = new ArrayList<String>();
			String[] strArray;
			
			//File einlesen:
			File myFile = new File(path);
			FileInputStream fInput;
			try 
			{
				fInput = new FileInputStream(myFile);
				BufferedReader myBufReader = new BufferedReader(new InputStreamReader(fInput));
				String aDataRow = "";
				String aBuffer = "";
				while ((aDataRow = myBufReader.readLine()) != null) {
					aBuffer += aDataRow;
				}
				if(aBuffer.length() > 0) {
					strArray = aBuffer.split(",_,_,_,_,"); // regular Expression!
					for( String s : strArray){
						if(s.length() > 10) {
							readedStrElem.add(s);
						}
					}
				}
				myBufReader.close();
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			List<Bookmark> importedBookmarks = SerializerUtil.deserializeBookmarkList(readedStrElem);
			if(importedBookmarks.isEmpty()) {
				return;
			}
			
//			if(mode.equalsIgnoreCase("overwrite"))
//			{
//				myCollection.deleteAllBookmarks(myBook);
//			}
//			 weiter in beiden Fällen: 
//			"overwrite": keine Lesezeichen
//			"merge": Lesezeichen verschmelzen
			for(Bookmark bookmark : importedBookmarks){
				Reader.Collection.saveBookmark(bookmark);
			}
		}
		else {
			////////////////////////////////////// 
		*/
		OrientationUtil.startActivity(BaseActivity, intent);
		
//		}
	}
}
