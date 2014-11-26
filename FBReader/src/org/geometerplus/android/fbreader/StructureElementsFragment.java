package org.geometerplus.android.fbreader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.geometerplus.android.util.ViewUtil;
import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.book.SerializerUtil;
import org.geometerplus.fbreader.bookmodel.TOCTree;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.tree.ZLTree;
import org.geometerplus.zlibrary.text.view.ZLTextWordCursor;
import org.geometerplus.zlibrary.ui.android.R;

import android.animation.ValueAnimator;
import android.app.ListFragment;
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
	private List<Bookmark> myStructElem;
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

	public void saveStructureElement(Bookmark b, String structElemName) {
		final FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
		TOCTree treeToSelect = fbreader.getCurrentTOCElement();
		//____________________________________//____________________________________
		
//		if(treeToSelect == null) return; // NPE? fixen!
		//____________________________________//____________________________________

		// finde Ebene, wo die Kapitelüberschriften sind:
		while (treeToSelect.Level > 1) {
			treeToSelect = treeToSelect.Parent;
		}// treeToSelect zeigt jetzt auf ein TOCElement des richtigen Kapitels (d.h. der Strukturelement befindet sich im treeToSelect)
		
		// nun muss man die Einfügeposition im subtree des richtigen Kapitels finden...
		// (weil ja im Exposse (s.8) steht: "gemäß dem Textverlauf"
		List<TOCTree> subtrees = treeToSelect.subtrees();
		
		// Wenn subtree leer ist: als erstes Element einfügen
		if(subtrees.isEmpty()) {
			TOCTree toc = new TOCTree(treeToSelect);
			// TODO toc initialisieren

			// Bookmark-text
			if (b.getText().length() > 30) {
				toc.setText(b.getText().substring(0, 30) + "..");
			} else {
				toc.setText(b.getText());
			}

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
				TOCTree toc = new TOCTree(treeToSelect, i); //in TOCActovity habe ich einen speziellen Konstruktor geschrieben,
				//damit man die Einfügepopsition auch übergeben kann
				//Bookmark-text

				if (b.getText().length() > 30) {
					toc.setText(b.getText().substring(0, 30) + "..");
				} else {
					toc.setText(b.getText());
				}

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
			
			try {
				exportStructureElementsToFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// Code TOCActivity.java start
			final TOCTree root = fbreader.Model.TOCTree;
			myAdapter = new TOCAdapter(root);   // TODO set onItemLongClickListener, evtl. auch mOnScrollListener, mOnHierarchyChangeListener
			final ZLTextWordCursor cursor = fbreader.BookTextView.getStartCursor();
			int index = cursor.getParagraphIndex();
			if (cursor.isEndOfParagraph()) {
				++index;
			}
			TOCTree treeToSelect = fbreader.getCurrentTOCElement();
			myAdapter.selectItem(treeToSelect);
			mySelectedItem = treeToSelect;

			// init Buttons and their listener
			myBtnIncrease = (ImageButton) getActivity().findViewById(R.id.increase);
			myBtnDecrease = (ImageButton) getActivity().findViewById(R.id.decrease);
			myBtnIncrease.setOnClickListener(this);
			myBtnDecrease.setOnClickListener(this);
			
			//holen von zuvor allen gespeicherten Strukturelementen
				oldElementsLoaded = true;
				List<Bookmark> oldElements = fbreader.getVisibleBookmarks();
				for(Bookmark b : oldElements){
					saveStructureElement(b, "old");
				}
				update();
			
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
		public void onCreateContextMenu(ContextMenu menu, View view,
				ContextMenu.ContextMenuInfo menuInfo) {
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
			ViewUtil.findTextView(view, R.id.toc_tree_item_text).setText(tree.getText());
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

	public void restoreStructureElementsFromDB() {
	 	final FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
//		Activity act = (Activity) fbreader.getMyWindow();
//		FragmentManager fm = act.getFragmentManager();
		StructureElementsFragment myFragment = (StructureElementsFragment) getFragmentManager().findFragmentByTag("StructureElementsFragmentTag");

		List<Bookmark> myLiskkt = fbreader.getVisibleBookmarks();
		if (myLiskkt != null && myFragment != null) {
			for (Bookmark b : myLiskkt) {
				myFragment.saveStructureElement(b, "TESTTEST");
			}
		}
	}
	
	public void update() {
	//	myAdapter.notifyDataSetChanged();
	}
	
	public void exportStructureElementsToFile() throws IOException{
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
				myOutWriter.append(s + ",,,,,");
			}
			
			myOutWriter.close();
			fout.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String tempPath = "/storage/emulated/0/Books" + "/" + exportedFileName;
		importStructureElementsFromFile(tempPath);
	}
	
	public void importStructureElementsFromFile(String pathToOpen) throws IOException{
		final FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
		List<String> readedStrElem = new ArrayList<String>();
		String[] strArray;
		
		//File einlesen:
//		File myFile = new File(pathToOpen);
		File myFile = new File(pathToOpen);
		FileInputStream fInput = new FileInputStream(myFile);
		BufferedReader myBufReader = new BufferedReader(new InputStreamReader(fInput));
		String aDataRow = "";
		String aBuffer = "";
		while ((aDataRow = myBufReader.readLine()) != null) {
			aBuffer += aDataRow;
		}
		if(aBuffer.length() > 0) {
			strArray = aBuffer.split(",,,,,");
			for( String s : strArray){
				if(s.length() > 10){
					readedStrElem.add(s);
				}
			}
		}
		myBufReader.close();
		//////////////////////

		List<Bookmark> importedBookmarks = SerializerUtil.deserializeBookmarkList(readedStrElem);
		if(importedBookmarks.isEmpty()) {
			Toast.makeText(getActivity(), "Sie haben nichts zu speichern", Toast.LENGTH_LONG).show();
			return;
		}
		
		//find duplicate and erase them from importedBookmarks list:
		List<Bookmark> bookmarks = fbreader.getVisibleBookmarks();
		bookmarks.retainAll(importedBookmarks); // TODO testen!!
	}
}
