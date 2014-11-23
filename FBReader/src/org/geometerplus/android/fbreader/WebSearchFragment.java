package org.geometerplus.android.fbreader;

import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.ui.android.R;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;

public class WebSearchFragment extends Fragment implements OnClickListener {

	private String mySearchTerm;
	private String myUserChoice;
	public WebView myWebView;

	final int animDuration = 500;
	final int layoutChangeValue = 100;
	
	ImageButton myBtnIncrease;
	ImageButton myBtnDecrease;
	ImageButton myBtnClose; 
	
	public WebSearchFragment(String searchTerm, String userChoice) {
		super();
		this.mySearchTerm = searchTerm;
		this.myUserChoice = userChoice;
	}

	public WebSearchFragment() {
		super();
		myUserChoice = "Android";
		mySearchTerm = "http://de.wikipedia.org/wiki/";
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	@JavascriptInterface
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View webSearchView = inflater.inflate(R.layout.web_search_fragment, container, false);
		
		if (savedInstanceState != null) // TODO
		{// wird bei Änderung der Orientierung ausgeführt
			mySearchTerm = savedInstanceState.getString("mySearchTerm");
			myUserChoice = savedInstanceState.getString("myUserChoice");
		}
		
		myWebView = (WebView) webSearchView.findViewById(R.id.webSearch);

		//Java Script einschalten: 
		myWebView.getSettings().setJavaScriptEnabled(true);
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
			// http://www.google.com/search?q=Tustumena
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

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("myUserChoice", myUserChoice);
		outState.putString("mySearchTerm", mySearchTerm);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.close_websearch) {
			
			final FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
			Activity act = (Activity) fbreader.getMyWindow();
			FragmentManager fm = act.getFragmentManager();
			FragmentTransaction transaction = fm.beginTransaction();
			Fragment StructElFrag = fm.findFragmentByTag("StructureElementsFragmentTag");
			// entferne das geöffnete Fragment bis auf den Strukturbereich:
			// TODO: überprüfen, was passiert, wenn es mehrere Fragmente auf dem
			// Stack sind
			if (fm.getBackStackEntryCount() > 1) {
				fm.popBackStack();
			}
			Fragment webFragment = fm.findFragmentByTag("websearch");
			transaction.remove(webFragment);
			transaction.attach(StructElFrag); // show strElFrag:
			transaction.commit();
		}
		if (v.getId() == R.id.increasewebsearch) {
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
				ValueAnimator anim = ValueAnimator.ofInt(newWidth, newWidth	+ layoutChangeValue);
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
				// anim end
			}
		}
		
		if (v.getId() == R.id.decreasewebsearch) {
			final View view = getActivity().findViewById(R.id.fragment_container);
			int mWidth = view.getWidth();
			int mHeight = view.getHeight();

			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				// set the color of increase button to default
				myBtnIncrease.setImageResource(R.drawable.increase_land_default);
				// prüfen, ob der Strukturbereich gross genug ist
				if (mWidth < 250) {
					myBtnDecrease.setImageResource(R.drawable.decrease_land_disabled);
					return;
				} else {
					myBtnDecrease.setImageResource(R.drawable.decrease_land_default);
				}

				// start anim with Value Animator:
				ValueAnimator anim = ValueAnimator.ofInt(mWidth, mWidth - layoutChangeValue);
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
				// anim end
				/*
				 * ResizeWidthAnimation anim = new
				 * ResizeWidthAnimation(view, mWidth - layoutChangeValue);
				 * anim.setDuration(animDuration);
				 * view.startAnimation(anim);
				 */
			} else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
				// set the color of increase button to default
				myBtnIncrease.setImageResource(R.drawable.increase_port_default);
				// prüfen, ob der Strukturbereich gross genug ist
				if (mHeight < 250) {
					myBtnDecrease.setImageResource(R.drawable.decrease_port_disabled);
					return;
				} else {
					myBtnDecrease.setImageResource(R.drawable.decrease_port_default);
				}

				// start anim with Value Animator:
				ValueAnimator anim = ValueAnimator.ofInt(view.getHeight(),
						view.getHeight() - layoutChangeValue);
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
				// anim end
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		WebSearchFragment webFrag = (WebSearchFragment) getActivity().getFragmentManager().findFragmentByTag("websearch"); 
		if(webFrag != null){
			myBtnIncrease = (ImageButton) getActivity().findViewById(R.id.increasewebsearch);
			myBtnDecrease = (ImageButton) getActivity().findViewById(R.id.decreasewebsearch);
			myBtnClose    = (ImageButton) getActivity().findViewById(R.id.close_websearch);
			
			myBtnIncrease.setOnClickListener(this);
			myBtnDecrease.setOnClickListener(this);
			myBtnClose.setOnClickListener(this);
		}
	}
}
