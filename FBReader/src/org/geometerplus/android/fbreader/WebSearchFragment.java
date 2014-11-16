package org.geometerplus.android.fbreader;

import org.geometerplus.zlibrary.ui.android.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebSearchFragment extends Fragment {

	private String mySearchTerm;
	private String myUserChoice;
	public WebView myWebView;

	public WebSearchFragment(String searchTerm, String userChoice) {
		super();
		this.mySearchTerm = searchTerm;
		this.myUserChoice = userChoice;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View webSearchView = inflater.inflate(R.layout.web_search_fragment, container, false);
		myWebView = (WebView) webSearchView.findViewById(R.id.webSearch);
		myWebView.setWebViewClient(new WebViewClient());
		// dictionary
		if (myUserChoice.compareTo("selectionTranslate") == 0) {
			myWebView.loadUrl("http://www.dict.cc/?s=" + mySearchTerm);
			return webSearchView;
		} else if (myUserChoice.compareTo("selectionWikipedia") == 0) {
			myWebView.loadUrl("http://de.wikipedia.org/wiki/" + mySearchTerm);
			return webSearchView;
		} else if (myUserChoice.compareTo("selectionGoogle") == 0) {
			myWebView.loadUrl("http://www.google.com/search?q=" + mySearchTerm);
			return webSearchView;
		} else {
			myWebView.setWebViewClient(new AllowWebBrowsingClient());
//			http://www.google.com/search?q=Tustumena
			myWebView.loadUrl("http://www.google.com/search?q=" + mySearchTerm);
		}
		return webSearchView;
	}

	private class AllowWebBrowsingClient extends WebViewClient {
		// damit man im WebFragment weitere Links anklicken kann
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}
	}
}
