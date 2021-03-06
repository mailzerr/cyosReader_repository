/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.geometerplus.android.fbreader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.fbreader.libraryService.LibraryService;
import org.geometerplus.android.fbreader.libraryService.SQLiteBooksDatabase;
import org.geometerplus.android.util.ViewUtil;
import org.geometerplus.fbreader.book.BookCollection;
import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.book.HighlightingStyle;
import org.geometerplus.fbreader.book.SerializerUtil;
import org.geometerplus.fbreader.bookmodel.TOCTree;
import org.geometerplus.fbreader.fbreader.BookmarkHighlighting;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.tree.ZLTree;
import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.ui.android.R;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

public class StructureElementsFragment extends ListFragment implements AdapterView.OnItemClickListener, OnClickListener {
	private TOCAdapter myAdapter;
	private ZLTree<?> mySelectedItem;
	private boolean oldElementsLoaded = false;
	final int animDuration = 500;
	final int layoutChangeValue = 100;
	
	ImageButton myBtnIncrease;
	ImageButton myBtnDecrease;
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
//		beim Schliessen die Liste speichern: http://stackoverflow.com/questions/17810749/how-to-save-state-of-fragment-having-listview
//		oder: http://stackoverflow.com/questions/13680919/saving-listview-state-before-replacing-the-fragment
		super.onSaveInstanceState(outState);
	}
	 
	public void saveStructureElementImproved(Bookmark b, String structElemName) {
		final FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
		TOCTree treeToSelect = null;
		if(fbreader.Model != null){
			TOCTree root = fbreader.Model.TOCTree;
			
			//find right toctree to insert the bookmark b:
			for (int i = 0; i < root.subtrees().size(); i++) {
				if(b.getParagraphIndex() > root.subtrees().get(i).getReference().ParagraphIndex) {
					//richtiges Kapitel zum Einf�gen gefunden:
					treeToSelect = root.subtrees().get(i);
				}
			}
		}
		
		// nun muss man die Einf�geposition im subtree des richtigen Kapitels finden...
		// (weil ja im Exposse (s.8) steht: "gem�� dem Textverlauf"
		List<TOCTree> subtrees = treeToSelect.subtrees();
		
		// Wenn subtree leer ist: als erstes Element einf�gen
		if(subtrees.isEmpty()) {
			TOCTree toc = new TOCTree(treeToSelect);
			toc.setText(b.getText());

			if (toc.getReference() == null) {
				toc.setReference(null, b.ParagraphIndex);
			}
		} else { // sonst solange t.getReference().ParagraphIndex < b.getParagraphIndex()
			for (int i = 0; i < subtrees.size(); i++) {
				if (subtrees.get(i).getReference().ParagraphIndex < b.getParagraphIndex()) {
					if (i != subtrees.size() - 1) {
						continue;
					}
					else {
						i++;
					}
				}
				// richtige Einf�geposition gefunden: einf�gen
				TOCTree toc = new TOCTree(treeToSelect, i); // in TOCActovity habe ich einen speziellen Konstruktor geschrieben,
															// damit man die Einf�gepopsition auch �bergeben kann
				toc.setText(b.getText());
				if (toc.getReference() == null) {
					toc.setReference(null, b.ParagraphIndex);
				}
				break;
			}
		}
//		//informiere den Adapter �ber die �nderungen
		myAdapter.notifyDataSetChanged();
		return;
	}


	// GUTE IDEE: Im Strukturbereich die Kommentare zum Strukturelement
	// anbringen: als Kind dem TOC-Objekt des Strukturelements 
	// hinzuf�gen! evtl. ein Bild auch..
	// und das Padding auf der gleichen Ebene wie das Strukturelement	
//	evtl. interessante Funktionen:
//	TOCTree toInsert = treeToSelect.getTreeByParagraphNumber(b.getParagraphIndex());
//	TOCTree structElDoppelt = new TOCTree(null);
//	structEl.getTreeByParagraphNumber(2);
////////////////////////////////////////////////////
	@Override
	public void onResume() {
		super.onResume();
		final FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
		if (fbreader.Model == null) {
			fbreader.reloadBook();
		} else {
			refreshStructureArea();
			// get sharedPreferences
			SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPath", Context.MODE_MULTI_PROCESS);
			String path = sharedPreferences.getString("pathToInsert", "");
			String choice = sharedPreferences.getString("choice", "");
			//l�schen nach dem Auslesen
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putString("pathToInsert", "");
			editor.putString("choice", "");
			editor.commit();
			
			if(!path.isEmpty()){
				try {
					importStructureElementsFromFile(path, choice);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// end shared Preferences
			
			fbreader.setBookmarkHighlightings(fbreader.getTextView(), null);
			final TOCTree root = fbreader.Model.TOCTree;
			myAdapter = new TOCAdapter(root);   // TODO nicht zur�cksetzen!
			
			
			
			TOCTree treeToSelect = fbreader.getCurrentTOCElement();
			myAdapter.selectItem(treeToSelect);
			mySelectedItem = treeToSelect;

			// init Buttons and their listener
			myBtnIncrease = (ImageButton) getActivity().findViewById(R.id.increase);
			myBtnDecrease = (ImageButton) getActivity().findViewById(R.id.decrease);
			myBtnIncrease.setOnClickListener(this);
			myBtnDecrease.setOnClickListener(this);
			
			//holen von zuvor allen gespeicherten Strukturelementen um sie im Strukturbereich anzuzeigen:
			List<Bookmark> oldElements = fbreader.getVisibleBookmarks();
			if (oldElementsLoaded == false && !oldElements.isEmpty()) {
				oldElementsLoaded = true; // garantieren, dass der Code unten nur ein Mal ausgef�hrt wird (alte Lesezeichen geholt werden)
				
				Activity act = (Activity) fbreader.getMyWindow(); 
				FragmentManager fm = act.getFragmentManager();
				StructureElementsFragment myFragment = (StructureElementsFragment) fm.findFragmentByTag("StructureElementsFragmentTag");
				if(myFragment != null) {
					for(Bookmark b : oldElements) {
//						myFragment.restoreStructureElement(b, "restored");
						myFragment.saveStructureElementImproved(b, "old_bookmarks");
//						myAdapter.notifyDataSetChanged();
					}
					myAdapter.notifyDataSetChanged();
				}
			}
			refreshStructureArea();
//			getListView().invalidateViews();
		}
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		final FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
		if (fbreader.Model == null) {
			getActivity().recreate(); // hat Fehler am Anfang verursacht, da
			// Model = null war (also nicht bef�llt  mit Daten)
			// Beim Recreate wird die Activity nochmals erstellt und Model mit Daten bef�llt.
			return;
		}
		refreshStructureArea();
	}

	private static final int PROCESS_TREE_ITEM_ID = 0;
	private static final int READ_BOOK_ITEM_ID = 1;
	
	public boolean onContextItemSelected(MenuItem item) {
		final int position = ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position;
		final TOCTree tree = (TOCTree) myAdapter.getItem(position);
		switch (item.getItemId()) {
		case PROCESS_TREE_ITEM_ID:
			myAdapter.runTreeItem(tree);
			return true;
		case READ_BOOK_ITEM_ID:
			myAdapter.openBookText(tree);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.my_list_fragment, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();
		refreshStructureArea();
	}
	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
	}

	// private Adapterklasse um mit der Listview zu arbeiten.
	private final class TOCAdapter extends ZLTreeAdapter /*implements OnItemLongClickListener*/{
		TOCAdapter(TOCTree root) {
			super(getListView(), root);
		}

		@Override
		public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
		
			final int position = ((AdapterView.AdapterContextMenuInfo) menuInfo).position;
			final TOCTree tree = (TOCTree) getItem(position);
			if (tree.hasChildren()) {
				menu.setHeaderTitle(tree.getText());
				final ZLResource resource = ZLResource.resource("tocView");
				menu.add(
						0,
						PROCESS_TREE_ITEM_ID,
						0,
						resource.getResource(
								isOpen(tree) ? "collapseTree" : "expandTree")
								.getValue());
				menu.add(0, READ_BOOK_ITEM_ID, 0,
						resource.getResource("readText").getValue());
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final View view = (convertView != null)
					? convertView
					: LayoutInflater.from(parent.getContext()).inflate(R.layout.toc_tree_item, parent, false);
			final TOCTree tree = (TOCTree) getItem(position);
			view.setBackgroundColor(tree == mySelectedItem 
					? 0x808080
					: 0);

			setIcon(ViewUtil.findImageView(view, R.id.toc_tree_item_icon), tree);
			
			//NEW
//			final BookCollectionShadow myCollection = new BookCollectionShadow();
//			final List<HighlightingStyle> styles = myCollection.highlightingStyles();
//			HighlightingStyle style = myCollection.getHighlightingStyle(1);
//			if(style != null){
//				String styleName = style.getName();
//			}
			
			//END NEW
			// Anzeigetext abk�rzen
			String textToShow = "";
			if (tree.getText().length() > 50) {
				textToShow = tree.getText().substring(0, 50) + "..";
			} else {
				textToShow = tree.getText();
			}
			ViewUtil.findTextView(view, R.id.toc_tree_item_text).setText(textToShow);
			//________________________________//________________________________
			//new color play
			/*
			final BookCollectionShadow myCollection = new BookCollectionShadow();
			int color = myCollection.getHighlightingStyle(1).getBackgroundColor().intValue();
			
			ViewUtil.findTextView(view, R.id.toc_tree_item_text).setTextColor(color);
			*/
			//________________________________//________________________________
			return view;
		}

		void openBookText(TOCTree tree) { // �bedenken!
			final TOCTree.Reference reference = tree.getReference();
			if (reference != null) {
				// finish(); !!
				final FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
				fbreader.addInvisibleBookmark();
				fbreader.BookTextView.gotoPosition(reference.ParagraphIndex, 0, 0);
				fbreader.showBookTextView();
				fbreader.storePosition();
			}
		}

		@Override
		protected boolean runTreeItem(ZLTree<?> tree) {
			if (super.runTreeItem(tree)) {
				return true;
			}
			openBookText((TOCTree) tree);
			return true;
		}
	}
	
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.increase) {
		
			final View view = getActivity().findViewById(R.id.fragment_container);
			int newWidth = view.getWidth();
			int newHeight = view.getHeight();

			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				// set the color fo decrease button to default
				myBtnDecrease.setImageResource(R.drawable.decrease_land_default);
				// pr�fen, ob der Strukturbereich nicht zu gross ist
				if (newWidth > 600) {
					myBtnIncrease.setImageResource(R.drawable.increase_land_disabled);
					return;
				} else {
					myBtnIncrease.setImageResource(R.drawable.increase_land_default);
				}
				// start anim with Value Animator:
				ValueAnimator anim = ValueAnimator.ofInt(newWidth, newWidth + layoutChangeValue);
				//Adds a listener to the set of listeners that are sent update events through the 
				//life of an animation. This method is called on all listeners for every frame of
				//the animation, after the values for the animation have been calculated.



				anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
					@Override
					// onAnimationUpdate informiert �ber das Auftreten eines anderen Frames in Animation
					public void onAnimationUpdate(ValueAnimator valueAnimator) {
						int val = (Integer) valueAnimator.getAnimatedValue();
						ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
						layoutParams.width = val;
						view.setLayoutParams(layoutParams);
					}
				});
				anim.setDuration(animDuration);
				anim.start();
				// end animation
			} else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

				// set the color fo decrease button to default
				myBtnDecrease.setImageResource(R.drawable.decrease_port_default);
				// pr�fen, ob der Strukturbereich nicht zu gross wird
				if (newHeight > 500) {
					myBtnIncrease.setImageResource(R.drawable.increase_port_disabled);
					return;
				} else {
					myBtnIncrease.setImageResource(R.drawable.increase_port_default);
				}
				// start anim with Value Animator:
				ValueAnimator anim = ValueAnimator.ofInt(view.getHeight(), view.getHeight() + layoutChangeValue);
				anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
					@Override
					public void onAnimationUpdate(ValueAnimator valueAnimator) {
						int val = (Integer) valueAnimator.getAnimatedValue();
						ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
						layoutParams.height = val;
						view.setLayoutParams(layoutParams);
					}
				});
				anim.setDuration(animDuration);
				anim.start();
			}
		}
		if (v.getId() == R.id.decrease) {
				final View view = getActivity().findViewById(R.id.fragment_container);
				int mWidth = view.getWidth();
				int mHeight = view.getHeight();

				if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
					//set the color of increase button to default
					myBtnIncrease.setImageResource(R.drawable.increase_land_default);
					//pr�fen, ob der Strukturbereich gross genug ist
					if(mWidth < 250) {
						myBtnDecrease.setImageResource(R.drawable.decrease_land_disabled);
						return;
					}
					else {
						myBtnDecrease.setImageResource(R.drawable.decrease_land_default);
					}
					
					// start anim with Value Animator:
						ValueAnimator anim = ValueAnimator.ofInt(mWidth, mWidth - layoutChangeValue);
					    anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
					        @Override
					        //onAnimationUpdate informiert �ber das Auftreten eines anderen Frames in Animation
					        public void onAnimationUpdate(ValueAnimator valueAnimator) {
					            int val = (Integer) valueAnimator.getAnimatedValue();
					            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
					            layoutParams.width = val;
					            view.setLayoutParams(layoutParams);
					        }
					    });
					    anim.setDuration(animDuration);
					    anim.start();
				}
				else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
					//set the color of increase button to default
					myBtnIncrease.setImageResource(R.drawable.increase_port_default);
					//pr�fen, ob der Strukturbereich gross genug ist
					if (mHeight < 250) {
						myBtnDecrease.setImageResource(R.drawable.decrease_port_disabled);
						return;
					}
					else {
						myBtnDecrease.setImageResource(R.drawable.decrease_port_default);
					}
					// start anim with Value Animator:
					ValueAnimator anim = ValueAnimator.ofInt(view.getHeight(), view.getHeight() - layoutChangeValue);
					    anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
					        @Override
					        public void onAnimationUpdate(ValueAnimator valueAnimator) {
					            int val = (Integer) valueAnimator.getAnimatedValue();
					            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
					            layoutParams.height = val;
					            view.setLayoutParams(layoutParams);
					        }
					    });
					    anim.setDuration(animDuration);
					    anim.start();
				}
		}
	}

	public void exportStructureElementsToFile() throws IOException {
		final FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
		
		//get saved bookmarks and serialize the bookmark list:
		List<Bookmark> bookmarks = fbreader.getVisibleBookmarks();
		List<String> serializedBookmarks = SerializerUtil.serializeBookmarkList(bookmarks);
		
		if(serializedBookmarks.isEmpty()){
			Toast.makeText(getActivity(), "Sie haben nichts zu speichern", Toast.LENGTH_LONG).show();;
			return;
		}
		
		String path = fbreader.getCurrentBook().File.getPath();
		String fileName = fbreader.getCurrentBook().File.getShortName();
		path = path.substring(0, path.length() - fileName.length());
		
		//Create Current timestamp:
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMANY);
        String currentTimeStamp = dateFormat.format(new Date()); 
		
		// Generate a filename
		String exportedFileName = "Strukturelemente_" + fileName + "_" + currentTimeStamp +".txt";
		//create File:
		File structElementsFile = new File ("/storage/emulated/0/Books" + "/" + exportedFileName);
		try {
//			Boolean success = structElementsFile.createNewFile();
			FileOutputStream fout = new FileOutputStream(structElementsFile);
			OutputStreamWriter myOutWriter = new OutputStreamWriter(fout);
			
			for(String s : serializedBookmarks) {
				myOutWriter.append(s + ",_,_,_,_,"); // Trennzeichen f�r die Bookmarks, um sie sp�ter mit RegEx zu splitten
			}
		
			myOutWriter.close();
			fout.close();
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// SHOW saved path to the user:
		//DIALOG TEST
		AlertDialog ad = new AlertDialog.Builder(getActivity()).create();
		ad.setCancelable(false);
		ad.setTitle("Exportieren erfolgreich abgeschlossen");
		ad.setMessage("Sie finden die exportierten Strukturelemente in der cyosReader Bibliothek.\n"
				+ "Dateiname: " + exportedFileName);
		ad.setButton("Schlie�en", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		ad.show();
//DIALOG TEST END
	}
	
	public void importStructureElementsFromFile(String pathToOpen, String mode) throws IOException {
		final FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
		List<String> readedStrElem = new ArrayList<String>();
		String[] strArray;
		
		//pr�fen, ob die ausgew�hlte Strukturelemente wirklich zum ge�ffneten Buch geh�ren:
		String bookName = fbreader.Model.Book.File.getShortName().substring(0, fbreader.Model.Book.File.getShortName().length() - 5);
		String[] allWordsFromBookName = bookName.split(" ");

		boolean containsAllNeededWords = true;
		for(String s : allWordsFromBookName){
			if(!pathToOpen.contains(s)){
				containsAllNeededWords = false;
			}
		}
		//pr�fen, ob die ausgew�hle Datei Strukturelemente enth�lt:
		if(!pathToOpen.contains("Strukturelemente")){
			containsAllNeededWords = false;
		}
		
		if(!containsAllNeededWords){
			//fehlermeldung
			AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
			alert.setTitle("Die gew�hlte Datei geh�rt zu einem anderen Buch.");
			alert.setMessage("W�hlen Sie bitte die Datei, die im Titel das gerade ge�ffnete Buch enth�lt");

			alert.setNegativeButton("ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					dialog.cancel();
				}
			});
			alert.show();
			return;
		}
		
		
		//File einlesen:
		File myFile = new File(pathToOpen);
		FileInputStream fInput = new FileInputStream(myFile);
		BufferedReader myBufReader = new BufferedReader(new InputStreamReader(fInput));
		String aDataRow = "";
		String aBuffer = "";
		while ((aDataRow = myBufReader.readLine()) != null) {
			aBuffer += aDataRow;
		}
		if(aBuffer.length() > 0) {
			strArray = aBuffer.split(",_,_,_,_,"); // regular Expression!
			for( String s : strArray){
				if(s.length() > 10){
					readedStrElem.add(s);
				}
			}
		}
		myBufReader.close();
		//////////////////////

		List<Bookmark> importedBookmarks = SerializerUtil.deserializeBookmarkList(readedStrElem);
		
		//set the proper book id:
		for (Bookmark b : importedBookmarks){
			b.setBookId(fbreader.Model.Book.getId());
			b.setBookTitle(fbreader.Model.Book.getTitle());
		}
		
		
		if(importedBookmarks.isEmpty()) {
			Toast.makeText(getActivity(), "Sie haben nichts zu speichern", Toast.LENGTH_LONG).show();
			return;
		}
		
		List<Bookmark> bookmarks = fbreader.getVisibleBookmarks();
		if(mode.equalsIgnoreCase("overwrite")) {
			//fbreader.eraseVisibleBookmarks();
			SQLiteBooksDatabase DBconn = new SQLiteBooksDatabase(getActivity());
			for(Bookmark b : bookmarks) {
				DBconn.deleteBookmark(b);
				deleteStructureElement(b);
				myAdapter = new TOCAdapter(fbreader.Model.TOCTree);
			}
			myAdapter.notifyDataSetChanged();
		}

		//find duplicates and delete them from importedBookmarks list:
		importedBookmarks.removeAll(bookmarks); // l�scht Duplikate aus bereits vorhandenen Bookmarks


//		SQLiteDatabase myDB = getActivity().openOrCreateDatabase("books.db", Context.MODE_PRIVATE, null);
		SQLiteBooksDatabase mySuperDB = new SQLiteBooksDatabase(getActivity());
		for(Bookmark b : importedBookmarks) {
			b.setId(-1); // to start insert action instead update
			mySuperDB.saveBookmark(b);
			fbreader.getTextView().addHighlighting(new BookmarkHighlighting(fbreader.getTextView(), fbreader.Collection, b));
	//		fbreader.saveImportedBookmarks(importedBookmarks); // new!
			fbreader.setBookmarkHighlightings(fbreader.getTextView(), null);
		}

		// ACHTUNG: "public" zur�ck auf protected in der Klasse SQLiteBooksDatabase weg machen und DB statements 
		// nochmals schreiben!! bessere Kapselung USW!!
		// hier der Code von saveBookmark einf�gen SQL (statement build process)

		if( !importedBookmarks.isEmpty() ){
			for(Bookmark b : importedBookmarks){
		//		saveStructureElementImproved(b, "imported"); // speichern im Strukturbereich
	//			fbreader.Collection.saveBookmark(b);
			}		}		myAdapter.notifyDataSetChanged();
	}
	
	public void update() {
		myAdapter.notifyDataSetChanged();
	}
	
	public void deleteStructureElement(Bookmark b){
		final FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
		if(fbreader.Model != null) {
			TOCTree root = fbreader.Model.TOCTree; // Level 0
			
			//find right toctree 
			for (int i = 0; i < root.subtrees().size(); i++) { // f�r alle
																// (Kapitel)
				if (root.subtrees().get(i).subtrees().size() > 0) {
					ArrayList<TOCTree> strElemList = (ArrayList<TOCTree>) root.subtrees().get(i).subtrees(); // f�r alle Strukturelemente in jedem Kapitel...

					for (TOCTree toc : strElemList) {
						String tocStr = toc.getText();
						String bText = b.getText();
						if (tocStr.equals(bText)) {
							strElemList.remove(toc);
							break;
						}
					}
				}
			}
		}
		myAdapter.notifyDataSetChanged();
	}
	 public void refreshStructureArea() {
		 final FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
		 if(fbreader.Model == null){
			 return;
		 }
		 TOCTree root = fbreader.Model.TOCTree;
		 
		 myAdapter = new TOCAdapter(root);
		 for (int i = 0; i < root.subtrees().size(); i++) {
			 root.subtrees().get(i).clear(); //alle Strukturelemente l�schen
		 }
		 //jedes einzelne Bookmark einf�gen
		 List<Bookmark> bookmarks = fbreader.getVisibleBookmarks();
		for (Bookmark b : bookmarks) {
			TOCTree treeToSelect = null;
			if (root != null) {
				// find right toctree to insert the bookmark b:
				for (int i = 0; i < root.subtrees().size(); i++) {
					if (b.getParagraphIndex() > root.subtrees().get(i).getReference().ParagraphIndex) {
						// richtiges Kapitel zum Einf�gen gefunden:
						treeToSelect = root.subtrees().get(i);
					}
				}
			}
			// nun muss man die Einf�geposition im subtree des richtigen Kapitels finden... "gem�� dem Textverlauf"
			List<TOCTree> subtrees = treeToSelect.subtrees();
			// Wenn subtree leer ist: als erstes Element einf�gen
			if (subtrees.isEmpty()) {
				TOCTree toc = new TOCTree(treeToSelect);
				toc.setText(b.getText());
				if (toc.getReference() == null) {
					toc.setReference(null, b.ParagraphIndex);
				}
			} else { // sonst solange t.getReference().ParagraphIndex <
						// b.getParagraphIndex()
				for (int i = 0; i < subtrees.size(); i++) {
					if (subtrees.get(i).getReference().ParagraphIndex < b
							.getParagraphIndex()) {
						if (i != subtrees.size() - 1) {
							continue;
						} else {
							i++;
						}
					}
					// richtige Einf�geposition gefunden: einf�gen
					TOCTree toc = new TOCTree(treeToSelect, i); 
					toc.setText(b.getText());
					if (toc.getReference() == null) {
						toc.setReference(null, b.ParagraphIndex);
					}
					break;
				}
			}
		}
		getListView().invalidateViews();
		myAdapter.notifyDataSetChanged();
		
	 }
}
