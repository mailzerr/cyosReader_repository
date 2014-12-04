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

package org.geometerplus.android.fbreader.library;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.*;
import android.os.Bundle;
import android.preference.PreferenceManager.OnActivityResultListener;
import android.provider.BaseColumns;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.fbreader.book.*;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.library.*;
import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.android.util.*;
import org.geometerplus.android.fbreader.*;
import org.geometerplus.android.fbreader.api.FBReaderIntents;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.fbreader.tree.TreeActivity;

public class LibraryActivity extends TreeActivity<LibraryTree> implements MenuItem.OnMenuItemClickListener, View.OnCreateContextMenuListener, IBookCollection.Listener {
	static final String START_SEARCH_ACTION = "action.fbreader.library.start-search";

	private final BookCollectionShadow myCollection = new BookCollectionShadow();
	private volatile RootTree myRootTree;
	private Book mySelectedBook;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		mySelectedBook = FBReaderIntents.getBookExtra(getIntent());

		new LibraryTreeAdapter(this);

		getListView().setTextFilterEnabled(true);
		getListView().setOnCreateContextMenuListener(this);

		deleteRootTree();

		myCollection.bindToService(this, new Runnable() {
			public void run() {
				setProgressBarIndeterminateVisibility(!myCollection.status().IsCompleted);
				myRootTree = new RootTree(myCollection);
				myCollection.addListener(LibraryActivity.this);
				init(getIntent());
			}
		});
	}

	@Override
	protected void onNewIntent(Intent intent) {
		if (START_SEARCH_ACTION.equals(intent.getAction())) {
			final String pattern = intent.getStringExtra(SearchManager.QUERY);
			if (pattern != null && pattern.length() > 0) {
				startBookSearch(pattern);
			}
		} else {
			super.onNewIntent(intent);
		}
	}

	@Override
	protected LibraryTree getTreeByKey(FBTree.Key key) {
		return key != null ? myRootTree.getLibraryTree(key) : myRootTree;
	}

	private synchronized void deleteRootTree() {
		if (myRootTree != null) {
			myCollection.removeListener(this);
			myCollection.unbind();
			myRootTree = null;
		}
	}

	@Override
	protected void onDestroy() {
		deleteRootTree();
		super.onDestroy();
	}

	@Override
	public boolean isTreeSelected(FBTree tree) {
		final LibraryTree lTree = (LibraryTree)tree;
		return lTree.isSelectable() && lTree.containsBook(mySelectedBook);
	}

	@Override
	protected void onListItemClick(ListView listView, View view, int position, long rowId) {
		final LibraryTree tree = (LibraryTree)getListAdapter().getItem(position);
		final Book book = tree.getBook();
		if (book != null) {
			showBookInfo(book);
		} else {
			openTree(tree);
		}
	}

	//
	// show BookInfoActivity
	//
	private void showBookInfo(Book book) {
		// LAST EDIT
		final String path = book.File.getPath(); 
		String fileExtension = path.substring(path.length() - 3, path.length());
		if(fileExtension.equalsIgnoreCase("txt"))
		{ // File mit Strukturelementen
			///////////
			//USER DIALOG START Quelle: http://www.androidsnippets.com/prompt-user-input-with-an-alertdialog
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Strukturelemente importieren");
//			alert.setTitle("Path:" + book.File.getPath());
			alert.setMessage("Bitte wählen Sie die gewünschte Option aus: " + "endung:" + fileExtension);
			
			alert.setNegativeButton("Elemente überschreiben", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					SharedPreferences sharedPreferences = getSharedPreferences("MyPath", Context.MODE_MULTI_PROCESS);
					SharedPreferences.Editor editor = sharedPreferences.edit();
					editor.putString("pathToInsert", path.toString());
					editor.putString("choice", "overwrite");
					editor.commit();
					finish();
				}
			});

			alert.setNeutralButton("Elemente zusammenfügen", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					SharedPreferences sharedPreferences = getSharedPreferences("MyPath", Context.MODE_MULTI_PROCESS);
					SharedPreferences.Editor editor = sharedPreferences.edit();
					editor.putString("pathToInsert", path.toString());
					editor.putString("choice", "merge");
					editor.commit();
					finish();
				}
			});
			
			alert.setPositiveButton("Abbrechen", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			  // close the running activity
				SharedPreferences sharedPreferences = getSharedPreferences("MyPath", Context.MODE_MULTI_PROCESS);
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putString("pathToInsert", "");
				editor.putString("choice", "");
				editor.commit();
				finish();
			  }
			});

			alert.show();
			//USER DIALOG END
			///////////
			
			/*
			SharedPreferences sharedPreferences = getSharedPreferences("MyPath", Context.MODE_MULTI_PROCESS);
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putString("pathToInsert", path.toString());
			editor.commit();
			finish();
			*/
			
			/*//Versuch mit Intents
			Intent returnIntent = new Intent();
			returnIntent.putExtra("path", path);
			setResult(RESULT_OK, returnIntent);
			finishActivity(558);*/
		}
		else
		{
			final Intent intent = new Intent(getApplicationContext(), BookInfoActivity.class);
			FBReaderIntents.putBookExtra(intent, book);
			OrientationUtil.startActivity(this, intent);	
		}
		
	}

	//
	// Search
	//
	private final ZLStringOption BookSearchPatternOption =
		new ZLStringOption("BookSearch", "Pattern", "");

	private void openSearchResults() {
		final LibraryTree tree = myRootTree.getSearchResultsTree();
		if (tree != null) {
			openTree(tree);
		}
	}

	@Override
	public boolean onSearchRequested() {
		if (DeviceType.Instance().hasStandardSearchDialog()) {
			startSearch(BookSearchPatternOption.getValue(), true, null, false);
		} else {
			SearchDialogUtil.showDialog(this, LibrarySearchActivity.class, BookSearchPatternOption.getValue(), null);
		}
		return true;
	}

	private interface ContextItemId {
		int OpenBook              = 0;
		int ShowBookInfo          = 1;
		int ShareBook             = 2;
		int AddToFavorites        = 3;
		int RemoveFromFavorites   = 4;
		int MarkAsRead            = 5;
		int MarkAsUnread          = 6;
		int DeleteBook            = 7;
		int UploadAgain           = 8;
		int TryAgain              = 9;
	}
	private interface OptionsItemId {
		int Search                = 0;
		int Rescan                = 1;
		int UploadAgain           = 2;
		int TryAgain              = 3;
		int DeleteAll             = 4;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
		final int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
		final Book book = ((LibraryTree)getListAdapter().getItem(position)).getBook();
		if (book != null) {
			createBookContextMenu(menu, book);
		}
	}

	private void createBookContextMenu(ContextMenu menu, Book book) {
		final ZLResource resource = LibraryTree.resource();
		final List<String> labels = book.labels();
		menu.setHeaderTitle(book.getTitle());
		menu.add(0, ContextItemId.OpenBook, 0, resource.getResource("openBook").getValue());
		menu.add(0, ContextItemId.ShowBookInfo, 0, resource.getResource("showBookInfo").getValue());
		if (book.File.getPhysicalFile() != null) {
			menu.add(0, ContextItemId.ShareBook, 0, resource.getResource("shareBook").getValue());
		}
		if (labels.contains(Book.FAVORITE_LABEL)) {
			menu.add(0, ContextItemId.RemoveFromFavorites, 0, resource.getResource("removeFromFavorites").getValue());
		} else {
			menu.add(0, ContextItemId.AddToFavorites, 0, resource.getResource("addToFavorites").getValue());
		}
		if (labels.contains(Book.READ_LABEL)) {
			menu.add(0, ContextItemId.MarkAsUnread, 0, resource.getResource("markAsUnread").getValue());
		} else {
			menu.add(0, ContextItemId.MarkAsRead, 0, resource.getResource("markAsRead").getValue());
		}
		if (BookUtil.canRemoveBookFile(book)) {
			menu.add(0, ContextItemId.DeleteBook, 0, resource.getResource("deleteBook").getValue());
		}
		if (labels.contains(Book.SYNC_DELETED_LABEL)) {
			menu.add(0, ContextItemId.UploadAgain, 0, resource.getResource("uploadAgain").getValue());
		}
		if (labels.contains(Book.SYNC_FAILURE_LABEL)) {
			menu.add(0, ContextItemId.TryAgain, 0, resource.getResource("tryAgain").getValue());
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final int position = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
		final Book book = ((LibraryTree)getListAdapter().getItem(position)).getBook();
		if (book != null) {
			return onContextItemSelected(item.getItemId(), book);
		}
		return super.onContextItemSelected(item);
	}

	private void syncAgain(Book book) {
		book.removeLabel(Book.SYNC_FAILURE_LABEL);
		book.removeLabel(Book.SYNC_DELETED_LABEL);
		book.addLabel(Book.SYNC_TOSYNC_LABEL);
		myCollection.saveBook(book);
	}

	private boolean onContextItemSelected(int itemId, Book book) {
		switch (itemId) {
			case ContextItemId.OpenBook:
				FBReader.openBookActivity(this, book, null);
				return true;
			case ContextItemId.ShowBookInfo:
				showBookInfo(book);
				return true;
			case ContextItemId.ShareBook:
				FBUtil.shareBook(this, book);
				return true;
			case ContextItemId.AddToFavorites:
				book.addLabel(Book.FAVORITE_LABEL);
				myCollection.saveBook(book);
				return true;
			case ContextItemId.RemoveFromFavorites:
				book.removeLabel(Book.FAVORITE_LABEL);
				myCollection.saveBook(book);
				if (getCurrentTree().onBookEvent(BookEvent.Updated, book)) {
					getListAdapter().replaceAll(getCurrentTree().subtrees(), true);
				}
				return true;
			case ContextItemId.MarkAsRead:
				book.addLabel(Book.READ_LABEL);
				myCollection.saveBook(book);
				getListView().invalidateViews();
				return true;
			case ContextItemId.MarkAsUnread:
				book.removeLabel(Book.READ_LABEL);
				myCollection.saveBook(book);
				getListView().invalidateViews();
				return true;
			case ContextItemId.DeleteBook:
				tryToDeleteBook(book);
				return true;
			case ContextItemId.UploadAgain:
			case ContextItemId.TryAgain:
				syncAgain(book);
				if (getCurrentTree().onBookEvent(BookEvent.Updated, book)) {
					getListAdapter().replaceAll(getCurrentTree().subtrees(), true);
				}
				return true;
		}
		return false;
	}

	//
	// Options menu
	//

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		addMenuItem(menu, OptionsItemId.Search, "localSearch", R.drawable.ic_menu_search);
		addMenuItem(menu, OptionsItemId.Rescan, "rescan", R.drawable.ic_menu_refresh);
		addMenuItem(menu, OptionsItemId.UploadAgain, "uploadAgain", -1);
		addMenuItem(menu, OptionsItemId.TryAgain, "tryAgain", -1);
		addMenuItem(menu, OptionsItemId.DeleteAll, "deleteAll", -1);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		boolean enableUploadAgain = false;
		boolean enableTryAgain = false;
		boolean enableDeleteAll = false;
		final LibraryTree tree = getCurrentTree();
		if (tree instanceof SyncLabelTree) {
			final String label = ((SyncLabelTree)tree).Label;
			if (Book.SYNC_DELETED_LABEL.equals(label)) {
				enableUploadAgain = true;
				enableDeleteAll = true;
			} else if (Book.SYNC_FAILURE_LABEL.equals(label)) {
				enableTryAgain = true;
			}
		}

		final MenuItem rescanItem = menu.findItem(OptionsItemId.Rescan);
		myCollection.bindToService(this, new Runnable() {
			public void run() {
				rescanItem.setEnabled(myCollection.status().IsCompleted);
			}
		});
		rescanItem.setVisible(tree == myRootTree);
		menu.findItem(OptionsItemId.UploadAgain).setVisible(enableUploadAgain);
		menu.findItem(OptionsItemId.TryAgain).setVisible(enableTryAgain);
		menu.findItem(OptionsItemId.DeleteAll).setVisible(enableDeleteAll);

		return true;
	}

	private MenuItem addMenuItem(Menu menu, int id, String resourceKey, int iconId) {
		final String label = LibraryTree.resource().getResource(resourceKey).getValue();
		final MenuItem item = menu.add(0, id, Menu.NONE, label);
		item.setOnMenuItemClickListener(this);
		if (iconId != -1) {
			item.setIcon(iconId);
		}
		return item;
	}

	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
			case OptionsItemId.Search:
				return onSearchRequested();
			case OptionsItemId.Rescan:
				if (myCollection.status().IsCompleted) {
					myCollection.reset(true);
					openTree(myRootTree);
				}
				return true;
			case OptionsItemId.UploadAgain:
			case OptionsItemId.TryAgain:
				for (FBTree tree : getCurrentTree().subtrees()) {
					if (tree instanceof BookTree) {
						syncAgain(((BookTree)tree).Book);
					}
				}
				getListAdapter().replaceAll(getCurrentTree().subtrees(), true);
				return true;
			case OptionsItemId.DeleteAll:
			{
				final List<Book> books = new LinkedList<Book>();
				for (FBTree tree : getCurrentTree().subtrees()) {
					if (tree instanceof BookTree) {
						books.add(((BookTree)tree).Book);
					}
				}
				tryToDeleteBooks(books);
			}
			default:
				return true;
		}
	}

	//
	// Book deletion
	//
	private class BookDeleter implements DialogInterface.OnClickListener {
		private final List<Book> myBooks;

		BookDeleter(List<Book> books) {
			myBooks = new ArrayList<Book>(books);
		}

		public void onClick(DialogInterface dialog, int which) {
			if (getCurrentTree() instanceof FileTree) {
				for (Book book : myBooks) {
					getListAdapter().remove(new FileTree((FileTree)getCurrentTree(), book.File));
					myCollection.removeBook(book, true);
				}
				getListView().invalidateViews();
			} else {
				boolean doReplace = false;
				for (Book book : myBooks) {
					doReplace |= getCurrentTree().onBookEvent(BookEvent.Removed, book);
					myCollection.removeBook(book, true);
				}
				if (doReplace) {
					getListAdapter().replaceAll(getCurrentTree().subtrees(), true);
				}
			}
		}
	}

	private void tryToDeleteBooks(List<Book> books) {
		final int size = books.size();
		if (size == 0) {
			return;
		}
		final ZLResource dialogResource = ZLResource.resource("dialog");
		final ZLResource buttonResource = dialogResource.getResource("button");
		final ZLResource boxResource = dialogResource.getResource(
			size == 1 ? "deleteBookBox" : "deleteMultipleBookBox"
		);
		final String title = size == 1
			? books.get(0).getTitle()
			: boxResource.getResource("title").getValue();
		final String message =
			boxResource.getResource("message").getValue(size).replaceAll("%s", String.valueOf(size));
		new AlertDialog.Builder(this)
			.setTitle(title)
			.setMessage(message)
			.setIcon(0)
			.setPositiveButton(buttonResource.getResource("yes").getValue(), new BookDeleter(books))
			.setNegativeButton(buttonResource.getResource("no").getValue(), null)
			.create().show();
	}

	private void tryToDeleteBook(Book book) {
		tryToDeleteBooks(Collections.singletonList(book));
	}

	private void startBookSearch(final String pattern) {
		BookSearchPatternOption.setValue(pattern);

		final Thread searcher = new Thread("Library.searchBooks") {
			public void run() {
				final SearchResultsTree oldSearchResults = myRootTree.getSearchResultsTree();

				if (oldSearchResults != null && pattern.equals(oldSearchResults.Pattern)) {
					onSearchEvent(true);
				} else if (myCollection.hasBooks(new Filter.ByPattern(pattern))) {
					if (oldSearchResults != null) {
						oldSearchResults.removeSelf();
					}
					myRootTree.createSearchResultsTree(pattern);
					onSearchEvent(true);
				} else {
					onSearchEvent(false);
				}
			}
		};
		searcher.setPriority((Thread.MIN_PRIORITY + Thread.NORM_PRIORITY) / 2);
		searcher.start();
	}

	private void onSearchEvent(final boolean found) {
		runOnUiThread(new Runnable() {
			public void run() {
				if (found) {
					openSearchResults();
				} else {
					UIUtil.showErrorMessage(LibraryActivity.this, "bookNotFound");
				}
			}
		});
	}

	public void onBookEvent(BookEvent event, Book book) {
		if (getCurrentTree().onBookEvent(event, book)) {
			getListAdapter().replaceAll(getCurrentTree().subtrees(), true);
		}
	}

	public void onBuildEvent(IBookCollection.Status status) {
		setProgressBarIndeterminateVisibility(!status.IsCompleted);
	}
}
