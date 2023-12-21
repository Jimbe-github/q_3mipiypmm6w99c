package com.teratail.q_3mipiypmm6w99c;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.*;

import androidx.annotation.*;
import androidx.constraintlayout.widget.ConstraintLayout;

public class GradesTotalView extends ConstraintLayout {
  private final TextView weightText, achievedText;

  public GradesTotalView(@NonNull Context context) {
    this(context, null);
  }
  public GradesTotalView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }
  public GradesTotalView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    LayoutInflater.from(context).inflate(R.layout.view_grades_total, this);
    weightText = findViewById(R.id.total_weight_text);
    achievedText = findViewById(R.id.total_achieved_text);
  }

  void set(Grades grades) {
    weightText.setText(String.format("%d%%", grades.getWeight()));
    achievedText.setText(String.format("%.1f%%", grades.getPercentage()));
  }
}
