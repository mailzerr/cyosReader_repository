package org.geometerplus.android.fbreader;

import org.geometerplus.zlibrary.ui.android.R;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class StructureElementsFragment extends ListFragment implements AdapterView.OnItemClickListener{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.my_list_fragment, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) { 
		super.onActivityCreated(savedInstanceState);
		ArrayAdapter arrayAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.heroes, android.R.layout.simple_list_item_1);
		setListAdapter(arrayAdapter);
		getListView().setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
		Toast.makeText(getActivity(), "Item " + i, Toast.LENGTH_SHORT).show();
		
//		TextView ausgabe = (TextView) getActivity().findViewById(R.id.textView1);
//		ausgabe.setText("Du hast angeklickt: " + i);
	}
	
}

/*
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;



public class StructureElementsFragment extends ListFragment {
	OnHeadlineSelectedListener mCallback;

	public interface OnHeadlineSelectedListener {
		// wird beim StrictureElements aufgerufen, wenn ein Element der Liste
		// angeklickt wird
		public void onArticleSelected(int position);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		int layout = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? android.R.layout.simple_list_item_activated_1
				: android.R.layout.simple_list_item_1;

		// ArrayAdapter fuer die ListView
		setListAdapter(new ArrayAdapter<String>(getActivity(), layout,
				Ipsum.Headlines));
	}

	
	//TODO ZERR diese Funktion auch implementieren???
//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup container,
//			Bundle savedInstanceState) {
//		return inflater.inflate(R.layout., root, attachToRoot);
//	}

	@Override
	public void onStart() {
		super.onStart();
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mCallback = (OnHeadlineSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnHeadlineSelectedListener");
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		mCallback.onArticleSelected(position);

		getListView().setItemChecked(position, true);
	}
}
*/