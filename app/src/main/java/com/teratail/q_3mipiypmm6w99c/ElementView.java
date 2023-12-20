package com.teratail.q_3mipiypmm6w99c;

import android.content.Context;
import android.text.*;
import android.util.AttributeSet;
import android.view.*;
import android.widget.*;

import androidx.annotation.*;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.function.*;

public class ElementView extends ConstraintLayout {
  private Grades.Type type;
  private final TextView typeText;
  private final EditText weightEdit, achievedEdit;

  private int weight, achieved;

  private Consumer<ElementView> changeListener;

  public ElementView(@NonNull Context context) {
    this(context, null);
  }
  public ElementView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }
  public ElementView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    View view = LayoutInflater.from(context).inflate(R.layout.view_element, this);

    typeText = view.findViewById(R.id.type_text);
    weightEdit = view.findViewById(R.id.weight_edit);
    weightEdit.addTextChangedListener(new NumberWatcher(v -> weight = v));
    achievedEdit = view.findViewById(R.id.achieved_edit);
    achievedEdit.addTextChangedListener(new NumberWatcher(v -> achieved = v));
  }

  void set(Grades.Type type, Grades.Element element) {
    this.type = type;
    typeText.setText("" + type);
    weightEdit.setText("" + element.weight);
    achievedEdit.setText("" + element.achieved);
  }

  void setChangeListener(Consumer<ElementView> l) {
    changeListener = l;
  }

  int getWeight() { return weight; }
  int getAchieved() { return achieved; }

  private class NumberWatcher implements TextWatcher {
    private final Consumer<Integer> valueHolder;
    NumberWatcher(Consumer<Integer> valueHolder) {
      this.valueHolder = valueHolder;
    }
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) { /*no process*/ }
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) { /*no process*/ }
    @Override
    public void afterTextChanged(Editable s) {
      try {
        valueHolder.accept(Integer.parseInt(s.toString()));
        if(changeListener != null) changeListener.accept(ElementView.this);
      } catch(NumberFormatException ignore) {
        /*no process*/
      }
    }
  }
}
