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
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;

public class ImageShowFragment extends Fragment implements OnClickListener {

	Bitmap myBitmap;
	ImageView myImageView;
	String myUrl;
	
	public WebView myWebView;

	final int animDuration = 500;
	final int layoutChangeValue = 100;
	
	ImageButton myBtnIncrease;
	ImageButton myBtnDecrease;
	ImageButton myBtnClose; 
	
	public ImageShowFragment(String url) {
		super();
		myUrl = url;
	}
	
	public ImageShowFragment(Bitmap bitmap) {
		super();
		myBitmap = bitmap;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View myView = inflater.inflate(R.layout.img_show_fragment, container, false);
		myImageView = (ImageView) myView.findViewById(R.id.myImage);
		myImageView.setImageBitmap(myBitmap);
		return myView;
	}

	@Override
	public void onClick(View v) {
		ImageButton btnIncrease = (ImageButton) getActivity().findViewById(R.id.increaseshowimage);
		ImageButton btnDecrease = (ImageButton) getActivity().findViewById(R.id.decreaseshowimage);
		if (v.getId() == R.id.close_showimage) {
			
			final FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
			Activity act = (Activity) fbreader.getMyWindow();
			FragmentManager fm = act.getFragmentManager();
			FragmentTransaction transaction = fm.beginTransaction();
			Fragment StructElFrag = fm.findFragmentByTag("StructureElementsFragmentTag");

			if (fm.getBackStackEntryCount() > 1) {
				fm.popBackStack();
			}
			
			Fragment imgFragment = fm.findFragmentByTag("ImageFragmentTag");
			transaction.remove(imgFragment);
			transaction.attach(StructElFrag); // show strElFrag:
			transaction.commit();
		}
		if (v.getId() == R.id.increaseshowimage) {
			final View view = getActivity().findViewById(R.id.fragment_container);
			int newWidth = view.getWidth();
			int newHeight = view.getHeight();

			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				// set the color fo decrease button to default
				btnDecrease.setImageResource(R.drawable.decrease_land_default);

				// prüfen, ob der Strukturbereich nicht zu gross ist
				if (newWidth > 600) {
					btnIncrease.setImageResource(R.drawable.increase_land_disabled);
					return;
				} else {
					btnIncrease.setImageResource(R.drawable.increase_land_default);
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
				btnDecrease.setImageResource(R.drawable.decrease_port_default);

				// prüfen, ob der Strukturbereich nicht zu gross wird
				if (newHeight > 500) {
					btnIncrease.setImageResource(R.drawable.increase_port_disabled);
					return;
				} else {
					btnIncrease.setImageResource(R.drawable.increase_port_default);
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
		
		if (v.getId() == R.id.decreaseshowimage) {
			final View view = getActivity().findViewById(R.id.fragment_container);
			int mWidth = view.getWidth();
			int mHeight = view.getHeight();

			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				// set the color of increase button to default
				btnIncrease.setImageResource(R.drawable.increase_land_default);
				// prüfen, ob der Strukturbereich gross genug ist
				if (mWidth < 250) {
					btnDecrease.setImageResource(R.drawable.decrease_land_disabled);
					return;
				} else {
					btnDecrease.setImageResource(R.drawable.decrease_land_default);
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
				btnIncrease.setImageResource(R.drawable.increase_port_default);
				// prüfen, ob der Strukturbereich gross genug ist
				if (mHeight < 250) {
					btnDecrease.setImageResource(R.drawable.decrease_port_disabled);
					return;
				} else {
					btnDecrease.setImageResource(R.drawable.decrease_port_default);
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
		ImageShowFragment webFrag = (ImageShowFragment) getActivity().getFragmentManager().findFragmentByTag("ImageFragmentTag"); 
		if(webFrag != null){
			myBtnIncrease = (ImageButton) getActivity().findViewById(R.id.increaseshowimage);
			myBtnDecrease = (ImageButton) getActivity().findViewById(R.id.decreaseshowimage);
			myBtnClose    = (ImageButton) getActivity().findViewById(R.id.close_showimage);
			
			myBtnIncrease.setOnClickListener(this);
			myBtnDecrease.setOnClickListener(this);
			myBtnClose.setOnClickListener(this);
		}
	}
}
