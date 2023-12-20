package com.teratail.q_3mipiypmm6w99c;

import android.app.Dialog;
import android.os.Bundle;
import android.text.*;
import android.view.*;
import android.widget.*;

import androidx.annotation.*;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class ItemDialogFragment extends DialogFragment {
  static final String RESULT_KEY_POSITION = "position";
  static final String RESULT_KEY_GRADES = "grades";

  private static final String ARGS_REQUESTKEY = "requestkey";
  private static final String ARGS_POSITION = "position";
  private static final String ARGS_GRADES = "grades";

  static ItemDialogFragment getInstance(String requestKey, int position, Grades grades) {
    if(requestKey == null) throw new NullPointerException("requestKey is NULL");
    ItemDialogFragment f = new ItemDialogFragment();
    Bundle args = new Bundle();
    args.putString(ARGS_REQUESTKEY, requestKey);
    args.putInt(ARGS_POSITION, position);
    args.putSerializable(ARGS_GRADES, grades);
    f.setArguments(args);
    return f;
  }

  private Grades grades;

  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
    Bundle args = getArguments();
    String requestKey = args.getString(ARGS_REQUESTKEY);
    if(requestKey == null) throw new NullPointerException("requestKey is NULL");
    int position = args.getInt(ARGS_POSITION, -1);
    grades = (Grades)args.getSerializable(ARGS_GRADES);
    if(grades == null) grades = new Grades();

    View view = LayoutInflater.from(requireContext()).inflate(R.layout.fradment_item_dialog, null);

    EditText nameEdit = view.findViewById(R.id.subject_edit);
    nameEdit.setText(grades.subjectName);
    nameEdit.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) { /*no process*/ }
      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) { /*no process*/ }
      @Override
      public void afterTextChanged(Editable s) {
        grades.subjectName = s.toString();
      }
    });

    Grades.Type[] types = Grades.Type.values();
    setElementView(view, R.id.check_1, R.id.element_1, types[0]);
    setElementView(view, R.id.check_2, R.id.element_2, types[1]);
    setElementView(view, R.id.check_3, R.id.element_3, types[2]);
    setElementView(view, R.id.check_4, R.id.element_4, types[3]);
    setElementView(view, R.id.check_5, R.id.element_5, types[4]);

    return new AlertDialog.Builder(requireContext())
            .setTitle("成績")
            .setView(view)
            .setPositiveButton("OK", (v,w) -> {
              Bundle result = new Bundle();
              result.putInt(RESULT_KEY_POSITION, position);
              result.putSerializable(RESULT_KEY_GRADES, grades);
              getParentFragmentManager().setFragmentResult(requestKey, result);
            })
            .setNegativeButton("Cancel", null)
            .create();
  }

  void setElementView(View view, @IdRes int checkId, @IdRes int elementId, Grades.Type type) {
    Grades.Element e = grades.getElement(type);

    CheckBox check = view.findViewById(checkId);
    check.setChecked(e.valid);

    ElementView elementView = view.findViewById(elementId);
    elementView.set(type, e);
    elementView.setEnabled(e.valid);
    elementView.setChangeListener(v -> {
      grades.setElementWeight(type, v.getWeight());
      grades.setElementAchieved(type, v.getAchieved());
    });

    check.setOnCheckedChangeListener((v,b) -> {
      grades.setElementValid(type, b);
      elementView.setEnabled(b);
    });
  }
}
