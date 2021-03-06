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

package org.geometerplus.android.fbreader.style;

import java.util.ArrayList;
import java.util.List;

import org.geometerplus.android.fbreader.StructureElementsFragment;
import org.geometerplus.android.fbreader.api.FBReaderIntents;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.util.ViewUtil;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.BookEvent;
import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.book.HighlightingStyle;
import org.geometerplus.fbreader.book.IBookCollection;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.util.ZLAndroidColorUtil;

import yuku.ambilwarna.widget.AmbilWarnaPrefWidgetView;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class StyleListActivity extends ListActivity implements IBookCollection.Listener {
	public static final String EXISTING_BOOKMARK_KEY = "existing.bookmark";

	private final BookCollectionShadow myCollection = new BookCollectionShadow();
	private boolean myExistingBookmark;
	private Bookmark myBookmark;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	}

	@Override
	protected void onStart() {
		super.onStart();
		myCollection.bindToService(this, new Runnable() {
			public void run() {
				myExistingBookmark = getIntent().getBooleanExtra(EXISTING_BOOKMARK_KEY, false);
				myBookmark = FBReaderIntents.getBookmarkExtra(getIntent());
				if (myBookmark == null) {
					finish();
					return;
				}
				final List<HighlightingStyle> styles = myCollection.highlightingStyles();
				if (styles.isEmpty()) {
					finish();
					return;
				}
				final ActionListAdapter adapter = new ActionListAdapter(styles);
				setListAdapter(adapter);
				getListView().setOnItemClickListener(adapter);
				myCollection.addListener(StyleListActivity.this);
			}
		});
	}

	@Override
	protected void onDestroy() {
		myCollection.unbind();
		super.onDestroy();
	}

	// method from IBookCollection.Listener
	public void onBookEvent(BookEvent event, Book book) {
		if (event == BookEvent.BookmarkStyleChanged) {
			((ActionListAdapter)getListAdapter()).setStyleList(myCollection.highlightingStyles());
		}
	}

	// method from IBookCollection.Listener
	public void onBuildEvent(IBookCollection.Status status) {
	}

	private class ActionListAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
		private final List<HighlightingStyle> myStyles;

		ActionListAdapter(List<HighlightingStyle> styles) {
			myStyles = new ArrayList<HighlightingStyle>(styles);
		}

		public synchronized void setStyleList(List<HighlightingStyle> styles) {
			myStyles.clear();
			myStyles.addAll(styles);
			notifyDataSetChanged();
		}

		public final synchronized int getCount() {
			return myExistingBookmark ? myStyles.size() + 1 : myStyles.size();
		}

		public final synchronized HighlightingStyle getItem(int position) {
			return position < myStyles.size() ? myStyles.get(position) : null;
		}

		public final long getItemId(int position) {
			return position;
		}

		public final synchronized View getView(int position, View convertView, final ViewGroup parent) {
			final View view = convertView != null
				? convertView
				: LayoutInflater.from(parent.getContext()).inflate(R.layout.style_item, parent, false);
			final HighlightingStyle style = getItem(position);

			final AmbilWarnaPrefWidgetView colorView =
				(AmbilWarnaPrefWidgetView)ViewUtil.findView(view, R.id.style_item_color);
			final TextView titleView = ViewUtil.findTextView(view, R.id.style_item_title);
			final Button button = (Button)ViewUtil.findView(view, R.id.style_item_edit_button);

			final ZLResource resource = ZLResource.resource("highlightingStyleMenu");

			if (style != null) {
				String name = style.getName();
				if (name == null || "".equals(name)) {
					name = resource
						.getResource("style").getValue()
						.replace("%s", String.valueOf(style.Id));
				}
				final ZLColor color = style.getBackgroundColor();
				final int rgb = color != null ? ZLAndroidColorUtil.rgb(color) : -1;

				colorView.setVisibility(View.VISIBLE);
				if (rgb != -1) {
					colorView.showCross(false);
					colorView.setBackgroundColor(rgb);
				} else {
					colorView.showCross(true);
					colorView.setBackgroundColor(0);
				}
				titleView.setText(name);

				button.setVisibility(View.VISIBLE);
				button.setText(resource.getResource("editStyle").getValue());
				button.setOnClickListener(new Button.OnClickListener() {
					@Override
					public void onClick(View view) {
						startActivity(
							new Intent(StyleListActivity.this, EditStyleActivity.class)
								.putExtra(EditStyleActivity.STYLE_ID_KEY, style.Id)
						);
					}
				});
			} else {
				colorView.setVisibility(View.GONE);
				button.setVisibility(View.GONE);
				titleView.setText(
					resource
						.getResource("deleteBookmark").getValue()
				);
			}

			return view;
		}

		public final synchronized void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			final HighlightingStyle style = getItem(position);
			myCollection.bindToService(StyleListActivity.this, new Runnable() {
				public void run() {
					if (style != null) {
						myBookmark.setStyleId(style.Id);
						myCollection.saveBookmark(myBookmark); 
						
						// TODO Stelle um die Daten zum Fragment zu schicken!!
						// zuerst soll ich einen Zugriff auf meine Activity bekommen:
						final FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
						Activity act = (Activity) fbreader.getMyWindow(); //Mein ERfolg!!! Wichtig f�r Interface-basierte Kommunikation mit dem Fragment
						FragmentManager fm = act.getFragmentManager();
						StructureElementsFragment myFragment = (StructureElementsFragment) fm.findFragmentByTag("StructureElementsFragmentTag");
						
						
						/////////////////
//						final FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
//						Activity act = (Activity) fbreader.getMyWindow();
//						AlertDialog ad = new AlertDialog.Builder(act).create();
//						ad.setCancelable(false);
//						ad.setTitle("Exportieren erfolgreich abgeschlossen");
//						ad.setMessage("Sie finden die exportierten Strukturelemente in der cyosReader Bibliothek.\n"
//						+ "Dateiname: " + "hallo");
//						ad.setButton("Schlie�en", new DialogInterface.OnClickListener() {
//						public void onClick(DialogInterface dialog, int which) {
//						dialog.dismiss();
//						}
//						});
//						ad.show();
						////////////////
						
						
						
						if(myFragment != null){
							
/*							///////////////////////////////
							String userDescription;
							AlertDialog.Builder alert = new AlertDialog.Builder(act.getApplicationContext());

							alert.setTitle("mein Titel");
							alert.setMessage("M�chten Sie Ihrer Auswahl einen Namen geben?");

							// Set an EditText view to get user input 
							final EditText input = new EditText(act.getApplicationContext());
							alert.setView(input);

							alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								final FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
								Activity act = (Activity) fbreader.getMyWindow(); //Mein ERfolg!!! Wichtig f�r Interface-basierte Kommunikation mit dem Fragment
								FragmentManager fm = act.getFragmentManager();
								StructureElementsFragment myFragment = (StructureElementsFragment) fm.findFragmentByTag("StructureElementsFragmentTag");
								if(myFragment != null){
									input.getText().toString();
								}
							  }
							});

							alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							  public void onClick(DialogInterface dialog, int whichButton) {
							    // Canceled.
							  }
							});

							alert.show();
							// see http://androidsnippets.com/prompt-user-input-with-an-alertdialog
							///////////////////////////////
*/
							myFragment.update();
						}
					} else {
						// TODO: HIER AUCH das Strukturelement aus dem Strukturbereich l�schen!!
						myCollection.deleteBookmark(myBookmark);
						final FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
						Activity act = (Activity) fbreader.getMyWindow(); 
						FragmentManager fm = act.getFragmentManager();
						StructureElementsFragment myFragment = (StructureElementsFragment) fm.findFragmentByTag("StructureElementsFragmentTag");
						if(myFragment != null){
							myFragment.deleteStructureElement(myBookmark);
							myFragment.update();
						}
					}
					finish();
				}
			});
		}
	}
}
