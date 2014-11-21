package org.geometerplus.android.fbreader;

import java.util.List;

import org.geometerplus.android.util.ViewUtil;
import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.bookmodel.TOCTree;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.tree.ZLTree;
import org.geometerplus.zlibrary.text.view.ZLTextWordCursor;
import org.geometerplus.zlibrary.ui.android.R;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Fragment;
import android.app.ListFragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.webkit.WebView.FindListener;
import android.widget.AdapterView;
import android.widget.ImageButton;

public class StructureElementsFragment extends ListFragment implements AdapterView.OnItemClickListener {
	private TOCAdapter myAdapter;
	private ZLTree<?> mySelectedItem;
	private List<Bookmark> myStructElem;
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		/* beim Schliessen die Liste speichern: http://stackoverflow.com/questions/17810749/how-to-save-state-of-fragment-having-listview
		oder: http://stackoverflow.com/questions/13680919/saving-listview-state-before-replacing-the-fragment
		*/
		super.onSaveInstanceState(outState);
	}



	public void saveStructureElement(Bookmark b, String structElemName) {
		final FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
		TOCTree treeToSelect = fbreader.getCurrentTOCElement();

//		getActivity().getActionBar().setIcon(icon); Hier kann man uni hagen setzen
		
		// finde Ebene, wo die Kapitelüberschriften sind:
		while (treeToSelect.Level > 1) {
			treeToSelect = treeToSelect.Parent;
		}// treeToSelect zeigt jetzt auf ein TOCElement des richtigen Kapitels (d.h. der Strukturelement befindet sich im treeToSelect)
		
		// nun muss man die Einfügeposition im subtree des richtigen Kapitels finden...
		// (weil ja  im Exposse (s.8) steht: "gemäß dem Textverlauf"
		List<TOCTree> subtrees = treeToSelect.subtrees();
		
		// Wenn subtree leer ist: als erstes Element einfügen
		if(subtrees.isEmpty()) {
			TOCTree toc = new TOCTree(treeToSelect);
			// TODO toc initialisieren
			
//			if (structElemName.length() > 15) {
//				toc.setText(structElemName.substring(0, 15) + "..");
//			} else {
//				toc.setText(structElemName);
//			}
			// Bookmark-text
			if (b.getText().length() > 15) {
				toc.setText(b.getText().substring(0, 15) + "..");
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
				
//				if (structElemName.length() > 15) {
//					toc.setText(structElemName.substring(0, 15) + "..");
//				} else {
//					toc.setText(structElemName);
//				}
				//Bookmark-text
				if (b.getText().length() > 15) {
					toc.setText(b.getText().substring(0, 15) + "..");
				} else {
					toc.setText(b.getText());
				}

				if (toc.getReference() == null) {
					toc.setReference(null, b.ParagraphIndex);
				}
				break;
			}
		}
		
//		evtl. interessante Funktionen:
//		TOCTree toInsert = treeToSelect.getTreeByParagraphNumber(b.getParagraphIndex());
//		TOCTree structElDoppelt = new TOCTree(null);
//		structEl.getTreeByParagraphNumber(2);
		
		// Im Modus ohne Kapitelüberschriften muss beim Longklick ein Contextmenu angezeigt werden, in dem nach dem Elternelement gefragt wird. 
		// Beispiel: ich möchte alle Zitate unter einen Hut bringen und setze als Elternelement einen bestimmten Knoten.
		
		// GUTE IDEE: Im Strukturbereich die Kommentare zum Strukturelement
		// anbringen: als Kind dem TOC-Objekt des Strukturelements
		// hinzufügen! evtl. ein Bild auch..
		// und das Padding auf der gleichen Ebene wie das Strukturelement
		return;
	}



	@Override
	public void onResume() {
		super.onResume();
		final FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
		if (fbreader.Model == null) {
			fbreader.reloadBook();
		} else {
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

			// HIER BEGINNT DIE TESTPHASE FÜR STRUKTURELEMENTE
/*
			//alle bookmarks holen:
			final List<Bookmark> bookmarks = fbreader.Collection.bookmarks(new BookmarkQuery(fbreader.Model.Book, false, 10));
			if (fbreader.Model != null) {
				if (fbreader.Model.TOCTree.subtrees() != null) { // Kapitelüberschriften sind bereits eingelesen
					if (fbreader.Model.TOCTree.subtrees().get(0).subtrees() != null) {
						// neues TOCTree-Objekt für das neue Strukturelement.
						//TODO: für jeden Bookmark 1: prüfen, ob bookmark schon in myStructElem bereits drin ist
						for(Bookmark b : bookmarks){
							if (myStructElem.contains(b)){
								continue;
							}
							else {
								//die Einfügeposition finden
								for (int i = 0; i < fbreader.Model.TOCTree.subtrees().size(); i++){
									if (b.ParagraphIndex < fbreader.Model.TOCTree.subtrees().get(i).getReference().ParagraphIndex) {
										//dann einfügen:
										TOCTree testTOC = new TOCTree(fbreader.Model.TOCTree.subtrees().get(i));
//										String onWidgetSelectedText = fbreader.getTextView().getSelectedText();
										testTOC.setText(b.getText());
									}
									else {
										continue;
									}
								}
							}
						}
//						TOCTree testTOC = new TOCTree(fbreader.Model.TOCTree.subtrees().get(0));
//						String onWidgetSelectedText = fbreader.getTextView().getSelectedText();
//						testTOC.setText(onWidgetSelectedText);
						fbreader.BookTextView.gotoPosition(21, 25, 1);
					}
				}
			}
*/

			//start of buttons handling 

			final int animDuration = 500;
			final int layoutChangeValue = 100;
			
			ImageButton btnIncrease = (ImageButton) getActivity().findViewById(R.id.increase);
			btnIncrease.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final View view = getActivity().findViewById(R.id.fragment_container);
					int newWidth = view.getWidth();
					int newHeight = view.getHeight();

					ImageButton imgButtonIncrease = (ImageButton) getActivity().findViewById(R.id.increase);
					ImageButton imgButtonDecrease = (ImageButton) getActivity().findViewById(R.id.decrease);
					
					if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {

						//set the color fo decrease button to default
						imgButtonDecrease.setImageResource(R.drawable.decrease_land_default);
						
						//prüfen, ob der Strukturbereich nicht zu gross ist
						if(newWidth > 600) {
							imgButtonIncrease.setImageResource(R.drawable.increase_land_disabled);
							return;
						}
						else {
							imgButtonIncrease.setImageResource(R.drawable.increase_land_default);
						}
						// start anim with Value Animator:
							ValueAnimator anim = ValueAnimator.ofInt(newWidth, newWidth + layoutChangeValue);
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
						// end animation 
					}
					else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

						//set the color fo decrease button to default
						imgButtonDecrease.setImageResource(R.drawable.decrease_port_default);

						//prüfen, ob der Strukturbereich nicht zu gross wird
						if(newHeight > 500) {
							imgButtonIncrease.setImageResource(R.drawable.increase_port_disabled);
							return;
						}
						else {
							imgButtonIncrease.setImageResource(R.drawable.increase_port_default);
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
						//anim end
					}
				}
			});
			
			
			ImageButton btnDecrease = (ImageButton) getActivity().findViewById(R.id.decrease);
			btnDecrease.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final View view = getActivity().findViewById(R.id.fragment_container);
					int mWidth = view.getWidth();
					int mHeight = view.getHeight();

					ImageButton imgButtonIncrease = (ImageButton) getActivity().findViewById(R.id.increase);
					ImageButton imgButtonDecrease = (ImageButton) getActivity().findViewById(R.id.decrease);
					
					if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
						//set the color of increase button to default
						imgButtonIncrease.setImageResource(R.drawable.increase_land_default);
						
						//prüfen, ob der Strukturbereich gross genug ist
						if(mWidth < 250) {
							imgButtonDecrease.setImageResource(R.drawable.decrease_land_disabled);
							return;
						}
						else {
							imgButtonDecrease.setImageResource(R.drawable.decrease_land_default);
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
						//anim end
					 /*
					    ResizeWidthAnimation anim = new ResizeWidthAnimation(view, mWidth - layoutChangeValue);
					    anim.setDuration(animDuration);
					    view.startAnimation(anim);
					    */
					}
					else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
						//set the color of increase button to default
						imgButtonIncrease.setImageResource(R.drawable.increase_port_default);
						//prüfen, ob der Strukturbereich gross genug ist
						
						if (mHeight < 250) {
							imgButtonDecrease.setImageResource(R.drawable.decrease_port_disabled);
							return;
						}
						else {
							imgButtonDecrease.setImageResource(R.drawable.decrease_port_default);
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
						//anim end
					}
				}
			});
			
			// end of buttons handling
			

//	        frag.getFragmentManager().saveFragmentInstanceState(f); WICHTIG FÜR ERSETZEN DER FRAGMENTE
			
			/*
			 *  
			btnKleiner.setOnClickListener(new View.OnClickListener() {
			
				//http://stackoverflow.com/questions/12423635/how-to-pass-data-from-one-fragment-to-other-in-android
				@Override
				public void onClick(View v) {
					android.app.FragmentManager fragmentManager = getFragmentManager();
					android.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
					// blabla
					fragmentTransaction.commit();
				}
			});
			***********/
			
			// myAdapter.IGNORE_ITEM_VIEW_TYPE //das passt vielleicht dazu, die
			// Inhaltsverzeichniselemente auszublenden.
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		final FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();

		if (fbreader.Model == null) {

			getActivity().recreate(); // hat Fehler am Anfang verursacht, da
			// Model = null war (also nicht befüllt  mit Daten)
			// Beim Recreate wird die Activity nochmals erstellt und Model mit
			// Daten befüllt.
			return;
		}
	}

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
	// Parameter view: welche Zeile wurde angklckt. in meinem Fall besteht sie
	// aus
	public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
	}

	private static final int PROCESS_TREE_ITEM_ID = 0;
	private static final int READ_BOOK_ITEM_ID = 1;

	// privater Adapter um mit der Listview zu arbeiten.
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
				fbreader.BookTextView.gotoPosition(reference.ParagraphIndex, 0,
						0);
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

		/*
		 * oben in der gleichen Datei implments onItemLongClickListener einkommentieren
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			// TODO Auto-generated method stub
			return false;
		}*/
	}

}
