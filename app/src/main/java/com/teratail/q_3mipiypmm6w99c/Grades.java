package com.teratail.q_3mipiypmm6w99c;

import android.content.Context;

import androidx.annotation.*;

import java.io.Serializable;
import java.util.*;

//成績
class Grades implements Serializable {
  //要素の種類
  enum Type {
    //とりあえずテキトー
    ALPHA(R.string.grades_type_alpha),
    BETA(R.string.grades_type_beta),
    CAMMA(R.string.grades_type_camma),
    DELTA(R.string.grades_type_delta),
    EPSILON(R.string.grades_type_epsilon);

    private final @StringRes int textId;
    Type(@StringRes int textId) {
      this.textId = textId;
    }
    String getLocalizedText(Context context) {
      return context.getString(textId);
    }
  }

  //要素
  static class Element {
    boolean valid; //有効/無効
    int weight; //成績内での割合(0-100?)
    int achieved; //達成度(0-100)

    Element() {
      this(false, 0, 0);
    }
    //copy
    Element(@Nullable Element org) {
      this(org != null && org.valid,
           org == null ? 0 : org.weight,
           org == null ? 0 : org.achieved);
    }

    Element(boolean valid, int weight, int achieved) {
      this.valid = valid;
      this.weight = weight;
      this.achieved = achieved;
    }

    int getWeight() { return valid && weight>0 ? weight : 0; }
    double getPercentage() { return valid && weight>0 ? weight*achieved/100.0 : 0; }
  }

  String subjectName; //科目名
  private final Map<Type, Element> elementSet = new EnumMap<>(Type.class); //成績データ

  Grades() {
    this("");
  }
  Grades(String subject) {
    this.subjectName = subject;
    for(Type type : Type.values())
      elementSet.put(type, new Element());
  }

  Element getElement(Type type) {
    return new Element(elementSet.get(type)); //防御コピー
  }

  void setElement(Type type, Element element) {
    elementSet.put(type, new Element(element)); //防御コピー
  }

  int getWeight() {
    int total = 0;
    for(Element e : elementSet.values()) total += e.getWeight();
    return total;
  }

  //達成割合
  double getPercentage() {
    double percentage = 0;
    for(Element e : elementSet.values()) percentage += e.getPercentage();
    return percentage;
  }
}
