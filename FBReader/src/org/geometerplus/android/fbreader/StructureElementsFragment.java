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
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.geometerplus.android.fbreader.libraryService.SQLiteBooksDatabase;
import org.geometerplus.android.util.ViewUtil;
import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.book.SerializerUtil;
import org.geometerplus.fbreader.bookmodel.TOCTree;
import org.geometerplus.fbreader.fbreader.BookmarkHighlighting;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.tree.ZLTree;
import org.geometerplus.zlibrary.ui.android.R;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
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
	/*
	public void saveStructureElement(Bookmark b, String structElemName) {
		final FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
		
		TOCTree treeToSelect = fbreader.getCurrentTOCElement();  //alt (26.11.2014)
//		TOCTree treeToSelect = fbreader.Model.TOCTree; //neu	
		
		// finde die Ebene, wo die Kapitelüberschriften sind:
		while (treeToSelect.Level > 1) {
			treeToSelect = treeToSelect.Parent;
		}
		// treeToSelect zeigt ab jetzt auf ein TOCElement des richtigen Kapitels (d.h. der Strukturelement befindet sich im treeToSelect)
		
		// nun muss man die Einfügeposition im subtree des richtigen Kapitels finden...
		// (weil ja im Exposse (s.8) steht: "gemäß dem Textverlauf"
		List<TOCTree> subtrees = treeToSelect.subtrees();
		
		// Wenn subtree leer ist: als erstes Element einfügen
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
				// richtige Einfügeposition gefunden: einfügen
				TOCTree toc = new TOCTree(treeToSelect, i); // in TOCActovity habe ich einen speziellen Konstruktor geschrieben,
															// damit man die Einfügepopsition auch übergeben kann
				toc.setText(b.getText());
				if (toc.getReference() == null) {
					toc.setReference(null, b.ParagraphIndex);
				}
				break;
			}
		}
//		//informiere den Adapter über die Änderungen
//		myAdapter.notifyDataSetChanged();
		return;
	}*/
	 
	public void saveStructureElementImproved(Bookmark b, String structElemName) {
		final FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
		TOCTree treeToSelect = null;
		if(fbreader.Model != null){
			TOCTree root = fbreader.Model.TOCTree;
			//find right toctree to insert the bookmark b:
			for (int i = 0; i < root.subtrees().size(); i++) {
				if(b.getParagraphIndex() > root.subtrees().get(i).getReference().ParagraphIndex) {
					//richtiges Kapitel zum Einfügen gefunden:
					treeToSelect = root.subtrees().get(i);
				}
			}
		}
		
		// nun muss man die Einfügeposition im subtree des richtigen Kapitels finden...
		// (weil ja im Exposse (s.8) steht: "gemäß dem Textverlauf"
		List<TOCTree> subtrees = treeToSelect.subtrees();
		
		// Wenn subtree leer ist: als erstes Element einfügen
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
				// richtige Einfügeposition gefunden: einfügen
				TOCTree toc = new TOCTree(treeToSelect, i); // in TOCActovity habe ich einen speziellen Konstruktor geschrieben,
															// damit man die Einfügepopsition auch übergeben kann
				toc.setText(b.getText());
				if (toc.getReference() == null) {
					toc.setReference(null, b.ParagraphIndex);
				}
				break;
			}
		}
//		//informiere den Adapter über die Änderungen
//		myAdapter.notifyDataSetChanged();
		return;
	}

	/////////////////////////////////////////////////////
//___________________EVTL. WICHTIGE VERBESSERUNGEN___________________
	// Im Modus ohne Kapitelüberschriften muss beim Longklick ein Contextmenu angezeigt werden, in dem nach dem Elternelement gefragt wird. 
	// Beispiel: ich möchte alle Zitate unter einen Hut bringen und setze als Elternelement einen bestimmten Knoten.

	// GUTE IDEE: Im Strukturbereich die Kommentare zum Strukturelement
	// anbringen: als Kind dem TOC-Objekt des Strukturelements 
	// hinzufügen! evtl. ein Bild auch..
	// und das Padding auf der gleichen Ebene wie das Strukturelement	
////////////////////////////////////////////////////
//	___________________EVTL. WICHTIGE CODESCHNIPSEL:___________________
	
//    frag.getFragmentManager().saveFragmentInstanceState(f); WICHTIG FÜR ERSETZEN DER FRAGMENTE
	
	
//	 myAdapter.IGNORE_ITEM_VIEW_TYPE //das passt vielleicht dazu, die
//	 Inhaltsverzeichniselemente auszublenden.

	
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
			
			// get sharedPreferences
			SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPath", Context.MODE_MULTI_PROCESS);
			String path = sharedPreferences.getString("pathToInsert", "");
			String choice = sharedPreferences.getString("choice", "");
			//löschen nach dem Auslesen
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
			myAdapter = new TOCAdapter(root);   // TODO nicht zurücksetzen!
			
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
				oldElementsLoaded = true; // garantieren, dass der Code unten nur ein Mal ausgeführt wird (alte Lesezeichen geholt werden)
				
				Activity act = (Activity) fbreader.getMyWindow(); 
				FragmentManager fm = act.getFragmentManager();
				StructureElementsFragment myFragment = (StructureElementsFragment) fm.findFragmentByTag("StructureElementsFragmentTag");
				if(myFragment != null) {
					for(Bookmark b : oldElements) {
//						myFragment.restoreStructureElement(b, "restored");
						myFragment.saveStructureElementImproved(b, "old_bookmarks");
//						myAdapter.notifyDataSetChanged();
					}
				}
			}
		}
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		final FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
		if (fbreader.Model == null) {
			getActivity().recreate(); // hat Fehler am Anfang verursacht, da
			// Model = null war (also nicht befüllt  mit Daten)
			// Beim Recreate wird die Activity nochmals erstellt und Model mit Daten befüllt.
			return;
		}
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
//		restoreStructureElementsFromDB();
		return inflater.inflate(R.layout.my_list_fragment, container, false);
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
			
			// Anzeigetext abkürzen
			String textToShow = "";
			if (tree.getText().length() > 30) {
				textToShow = tree.getText().substring(0, 30) + "..";
			} else {
				textToShow = tree.getText();
			}
			ViewUtil.findTextView(view, R.id.toc_tree_item_text).setText(textToShow);
			// Anzeigetext abkürzen
			//ViewUtil.findTextView(view, R.id.toc_tree_item_text).setText(tree.getText());
			return view;
		}

		void openBookText(TOCTree tree) { // übedenken!
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
				// prüfen, ob der Strukturbereich nicht zu gross ist
				if (newWidth > 600) {
					myBtnIncrease.setImageResource(R.drawable.increase_land_disabled);
					return;
				} else {
					myBtnIncrease.setImageResource(R.drawable.increase_land_default);
				}
				// start anim with Value Animator:
				ValueAnimator anim = ValueAnimator.ofInt(newWidth, newWidth + layoutChangeValue);
				anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
					@Override
					// onAnimationUpdate informiert über das Auftreten eines anderen Frames in Animation
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
				// prüfen, ob der Strukturbereich nicht zu gross wird
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
					//prüfen, ob der Strukturbereich gross genug ist
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
					        //onAnimationUpdate informiert über das Auftreten eines anderen Frames in Animation
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
					//prüfen, ob der Strukturbereich gross genug ist
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
				myOutWriter.append(s + ",_,_,_,_,"); // Trennzeichen für die Bookmarks, um sie später mit RegEx zu splitten
			}
		
			myOutWriter.close();
			fout.close();
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// SHOW save path to the user:
		//DIALOG TEST
		AlertDialog ad = new AlertDialog.Builder(getActivity()).create();
		ad.setCancelable(false);
		ad.setTitle("Exportieren erfolgreich abgeschlossen");
		ad.setMessage("Sie finden die exportierten Strukturelemente in der cyosReader Bibliothek.\n"
				+ "Dateiname: " + exportedFileName);
		ad.setButton("Schließen", new DialogInterface.OnClickListener() {
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
		List<Bookmark> bookmarks = fbreader.getVisibleBookmarks();
		
		if(importedBookmarks.isEmpty()) {
			Toast.makeText(getActivity(), "Sie haben nichts zu speichern", Toast.LENGTH_LONG).show();
			return;
		}
		
		if(mode.equalsIgnoreCase("overwrite")){
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
		importedBookmarks.removeAll(bookmarks); // löscht Duplikate aus bereits vorhandenen Bookmarks


//		SQLiteDatabase myDB = getActivity().openOrCreateDatabase("books.db", Context.MODE_PRIVATE, null);
		SQLiteBooksDatabase mySuperDB = new SQLiteBooksDatabase(getActivity());
		for(Bookmark b : importedBookmarks) {
			b.setId(-1); // to start insert action instead update
			mySuperDB.saveBookmark(b);
			fbreader.getTextView().addHighlighting(new BookmarkHighlighting(fbreader.getTextView(), fbreader.Collection, b));
			fbreader.saveImportedBookmarks(importedBookmarks);
			fbreader.setBookmarkHighlightings(fbreader.getTextView(), null);
		}

		// ACHTUNG: "public" zurück auf protected in der Klasse SQLiteBooksDatabase weg machen und DB zeug selber schreiben!! bessere Kapselung USW!!
		// hier der Code von saveBookmark einfügen SQL (statement build proces

		if( !importedBookmarks.isEmpty() ){
			for(Bookmark b : importedBookmarks){
				saveStructureElementImproved(b, "imported"); // speichern im Strukturbereich
				fbreader.Collection.saveBookmark(b);
				
			}		}		myAdapter.notifyDataSetChanged();
	}
	
	public void update() {
		myAdapter.notifyDataSetChanged();
	}
	
	public void deleteStructureElement(Bookmark b){
		final FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
		if(fbreader.Model != null) {
			TOCTree root = fbreader.Model.TOCTree; // Level 0
			
			//find right toctree to insert the bookmark b:
			for (int i = 0; i < root.subtrees().size(); i++) { // für alle
																// (Kapitel)
				if (root.subtrees().get(i).subtrees().size() > 0) {
					ArrayList<TOCTree> strElemList = (ArrayList<TOCTree>) root.subtrees().get(i).subtrees(); // für alle Strukturelemente in jedem Kapitel...

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
	
}
