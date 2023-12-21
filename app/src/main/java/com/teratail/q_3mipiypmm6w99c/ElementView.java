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
  private final CheckBox validCheck;
  private final TextView typeText;
  private final EditText weightEdit, achievedEdit;

  private boolean valid;
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

    LayoutInflater.from(context).inflate(R.layout.view_element, this);
    validCheck = findViewById(R.id.valid_check);
    typeText = findViewById(R.id.type_text);
    weightEdit = findViewById(R.id.weight_edit);
    achievedEdit = findViewById(R.id.achieved_edit);

    setEnabled(false);
    validCheck.setOnCheckedChangeListener((v,b) -> {
      valid = b;
      setEnabled(b);
      notifyChanged();
    });
    weightEdit.addTextChangedListener(new NumberWatcher(v -> weight = v));
    achievedEdit.addTextChangedListener(new NumberWatcher(v -> achieved = v));
  }

  void set(Grades.Type type, Grades.Element element) {
    validCheck.setChecked(element.valid);
    typeText.setText(type.getLocalizedText(getContext()));
    weightEdit.setText(String.valueOf(element.weight));
    achievedEdit.setText(String.valueOf(element.achieved));
  }

  void setChangeListener(Consumer<ElementView> l) { changeListener = l; }

  Grades.Element getElement() { return new Grades.Element(valid, weight, achieved); }

  private void notifyChanged() {
    if(changeListener != null) changeListener.accept(ElementView.this);
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    //setEditable が非推奨なのでフォーカスが行かないようにすることで代用(?)
    weightEdit.setFocusable(enabled);
    weightEdit.setFocusableInTouchMode(enabled);
    achievedEdit.setFocusable(enabled);
    achievedEdit.setFocusableInTouchMode(enabled);
  }

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
        notifyChanged();
      } catch(NumberFormatException ignore) {
        /*no process*/
      }
    }
  }
}
