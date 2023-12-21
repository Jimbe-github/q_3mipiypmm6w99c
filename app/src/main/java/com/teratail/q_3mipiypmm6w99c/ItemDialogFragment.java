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

    GradesTotalView totalView = view.findViewById(R.id.grades_total_view);
    totalView.set(grades);

    @IdRes int[] ids = new int[] { R.id.element_1, R.id.element_2, R.id.element_3, R.id.element_4, R.id.element_5 };
    for(int i=0; i<5; i++) {
      Grades.Type type = Grades.Type.values()[i];
      ElementView elementView = view.findViewById(ids[i]);
      elementView.set(type, grades.getElement(type));
      elementView.setChangeListener(v -> {
        grades.setElement(type, v.getElement());
        totalView.set(grades);
      });
    }

    return new AlertDialog.Builder(requireContext())
            .setTitle(R.string.item_dialog_title)
            .setView(view)
            .setPositiveButton(R.string.item_dialog_positive_button, (v, w) -> {
              Bundle result = new Bundle();
              result.putInt(RESULT_KEY_POSITION, position);
              result.putSerializable(RESULT_KEY_GRADES, grades);
              getParentFragmentManager().setFragmentResult(requestKey, result);
            })
            .setNegativeButton(R.string.item_dialog_negative_button, null)
            .create();
  }
}
