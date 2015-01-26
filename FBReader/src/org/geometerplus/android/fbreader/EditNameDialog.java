package org.geometerplus.android.fbreader;

import org.geometerplus.zlibrary.ui.android.R;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class EditNameDialog extends DialogFragment implements OnEditorActionListener {

    public interface EditNameDialogListener {
        void onFinishEditDialog(String inputText);
    }

    private EditText mEditText;

    public EditNameDialog() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_name, container);
        mEditText = (EditText) view.findViewById(R.id.edit_heading);
        getDialog().setTitle("Hello");

        // Show soft keyboard automatically
        mEditText.requestFocus();
        getDialog().getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        mEditText.setOnEditorActionListener(this);

        return view;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (EditorInfo.IME_ACTION_DONE == actionId) {
            // Return input text to activity
            EditNameDialogListener activity = (EditNameDialogListener) getActivity();
            String WOOOOW = mEditText.getText().toString();
            //HIER: speichern in der Datei:
            activity.onFinishEditDialog(mEditText.getText().toString());
            this.dismiss();
            return true;
        }
        return false;
    }

}

/*

//////////////____________________
final FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
Activity act = (Activity) fbreader.getMyWindow();
AlertDialog.Builder alert = new AlertDialog.Builder(act);//.getApplicationContext()); // !!
alert.setTitle("Geben Sie bitte ein Stichwort für Ihre Auswahl ein:");
LayoutInflater inflater = act.getLayoutInflater();
RelativeLayout relLayout = (RelativeLayout) act.findViewById(R.id.root_view);
final View dialogLayout = inflater.inflate(R.layout.heading_edit_dialog, relLayout, false);
alert.setView(dialogLayout);

alert.setPositiveButton("Speichern",
		new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Speichern in SharedPreferences
				final FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
				Activity act = (Activity) fbreader.getMyWindow();
				SharedPreferences sharedPreferences = act.getSharedPreferences("myHeading", Context.MODE_MULTI_PROCESS);
				final EditText heading = (EditText) dialogLayout.findViewById(R.id.edit_heading);
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putString("myHeading", heading.getText().toString());
				editor.commit();
				dialog.cancel();
			}
		});

alert.setNegativeButton("Text direkt übernehmen",
		new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				dialog.cancel();
			}
});
WindowManager.LayoutParams wmlp = act.getWindow().getAttributes();
wmlp.gravity = Gravity.BOTTOM;
wmlp.x = 50; // x position
wmlp.y = 50; // y position

//alert.show().getWindow().setLayout(500, 500);
AlertDialog alertDialog = alert.create();
alertDialog.show();
// ////////////////////////////////////////////////////////////////////////////////////
//get heading
SharedPreferences sharedPreferences = act.getSharedPreferences("myHeading", Context.MODE_MULTI_PROCESS);
heading = sharedPreferences.getString("myHeading", "");
//myHeading leeren:
SharedPreferences.Editor editor = sharedPreferences.edit();
editor.putString("myHeading", "");
editor.commit();
*/