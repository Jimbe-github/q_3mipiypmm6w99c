package com.teratail.q_3mipiypmm6w99c;

import android.content.Context;
import android.content.res.Resources;

import androidx.annotation.*;

import java.io.Serializable;
import java.util.*;

//成績
class Grades implements Serializable {
  //要素の種類
  enum Type {
    ALPHA, BETA, CAMMA, DELTA, EPSILON; //とりあえずテキトー

    String getLocalizedText(Context context) {
      Resources res = context.getResources();
      @StringRes int id = res.getIdentifier("grades_type_" + name().toLowerCase(), "string", context.getPackageName());
      return id == 0 ? name() : res.getString(id);
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
    Element(Element org) {
      this(org.valid, org.weight, org.achieved);
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
  private Map<Type, Element> elementSet = new EnumMap<>(Type.class); //成績データ

  Grades() {
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
