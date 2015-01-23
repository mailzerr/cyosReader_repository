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

import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.ui.android.R;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
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
	ImageButton myBtnEdit;
	
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
		{// wird ebenfalls bei der Änderung der Orientierung ausgeführt
			mySearchTerm = savedInstanceState.getString("mySearchTerm");
			myUserChoice = savedInstanceState.getString("myUserChoice");
		}
		
		myWebView = (WebView) webSearchView.findViewById(R.id.webSearch);

		//Java Script einschalten: 
		myWebView.getSettings().setJavaScriptEnabled(true);
		myWebView.setWebViewClient(new WebViewClient());
		
		//get the current URLs from Shared Preferences
		SharedPreferences sp_search = getActivity().getSharedPreferences("mySearchURLs", Context.MODE_MULTI_PROCESS);
		
		String searchURL = sp_search.getString("search", "http://www.google.com/search?q=");
		String dictURL =   sp_search.getString("dictionary", "http://www.dict.cc/?s=");
		String wikiURL =   sp_search.getString("wikipedia", "http://de.wikipedia.org/wiki/");
		
		//first initialization
		SharedPreferences.Editor editor = sp_search.edit();
		if(searchURL.isEmpty()) {
			editor.putString("search", "http://www.google.com/search?q=");
		}
		// dictionary
		if (myUserChoice.compareTo("selectionTranslate") == 0) {
			myWebView.loadUrl(dictURL + mySearchTerm);
			return webSearchView;
		} else if (myUserChoice.compareTo("selectionWikipedia") == 0) {
			myWebView.loadUrl(wikiURL + mySearchTerm);
			return webSearchView;
		} else if (myUserChoice.compareTo("selectionGoogle") == 0) {
			myWebView.loadUrl(searchURL + mySearchTerm);
			return webSearchView;
		} else {
			// default action, for external links!
			myWebView.loadUrl(myUserChoice);
		}
		
		return webSearchView;
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
		if (v.getId() == R.id.edit_webSearch) {
			AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
			alert.setTitle("Die URLs für die Websuche ändern:");
			//alert.setMessage("Andern Sie die URL wie gewünscht:\n");
			
			LayoutInflater inflater = getActivity().getLayoutInflater();
			final View dialogLayout = inflater.inflate(R.layout.web_search_edit_dialog, myWebView, false);

//			final FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
//			Activity act = (Activity) fbreader.getMyWindow();
			
			alert.setView(dialogLayout); //act.findViewById(R.layout.web_search_fragment)));
			
			final EditText search = (EditText) dialogLayout.findViewById(R.id.edit_suchmaschine);
			final EditText dict =   (EditText) dialogLayout.findViewById(R.id.edit_dictionary);
			final EditText wiki =   (EditText) dialogLayout.findViewById(R.id.edit_wikipedia);
			/*
			final EditText search = (EditText) getActivity().findViewById(R.id.edit_suchmaschine);
			final EditText dict =   (EditText) getActivity().findViewById(R.id.edit_dictionary);
			final EditText wiki =   (EditText) getActivity().findViewById(R.id.edit_wikipedia);
			*/
			SharedPreferences sp_search = getActivity().getSharedPreferences("mySearchURLs", Context.MODE_MULTI_PROCESS);
			
			String searchURL = sp_search.getString("search", "");
			String dictURL = sp_search.getString("dictionary", "");
			String wikiURL = sp_search.getString("wikipedia", "");
			
			//first initialization
			SharedPreferences.Editor editor = sp_search.edit();
			if(searchURL.isEmpty()) {
				editor.putString("search", "http://www.google.com/search?q=");
			}
			else {
				search.setText(searchURL);
			}
			
			if(dictURL.isEmpty()) {
				editor.putString("dictionary", "http://www.dict.cc/?s=");
			}
			else {
				dict.setText(dictURL);
			}
			
			if(wikiURL.isEmpty()) {
				editor.putString("wikipedia", "http://de.wikipedia.org/wiki/");
			}
			else {
				wiki.setText(wikiURL);
			}
			
			
			editor.commit();
			
			alert.setPositiveButton("Speichern", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					SharedPreferences sharedPreferences = getActivity().getSharedPreferences("mySearchURLs", Context.MODE_MULTI_PROCESS);
					
					/*final EditText search = (EditText) getActivity().findViewById(R.id.edit_suchmaschine);
					final EditText dict   = (EditText) getActivity().findViewById(R.id.edit_dictionary);
					final EditText wiki   = (EditText) getActivity().findViewById(R.id.edit_wikipedia);
					*/
					final EditText search = (EditText) dialogLayout.findViewById(R.id.edit_suchmaschine);
					final EditText dict   = (EditText) dialogLayout.findViewById(R.id.edit_dictionary);
					final EditText wiki   = (EditText) dialogLayout.findViewById(R.id.edit_wikipedia);
					
					
					SharedPreferences.Editor editor = sharedPreferences.edit();
					editor.putString("search", search.getText().toString());
					editor.putString("dictionary", dict.getText().toString());
					editor.putString("wikipedia", wiki.getText().toString());
					editor.commit();
					dialog.cancel();
				}
			});

			alert.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					dialog.cancel();
				}
			});
			
			alert.show().getWindow().setLayout(900, 800);
			//neu ab hier:
//			AlertDialog alertDialog = alert.create();
//			alertDialog.getWindow().setLayout(600, 400);
//			alertDialog.show();
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
			myBtnEdit     = (ImageButton) getActivity().findViewById(R.id.edit_webSearch);
			
			myBtnIncrease.setOnClickListener(this);
			myBtnDecrease.setOnClickListener(this);
			myBtnClose.setOnClickListener(this);
			myBtnEdit.setOnClickListener(this);
		}
	}
}
