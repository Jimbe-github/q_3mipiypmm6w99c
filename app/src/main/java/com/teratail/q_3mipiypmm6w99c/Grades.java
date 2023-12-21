package com.teratail.q_3mipiypmm6w99c;

import java.io.Serializable;
import java.util.*;

class Grades implements Serializable { //成績
  enum Type { //要素の種類
    ALPHA, BETA, CAMMA, DELTA, EPSILON; //とりあえずテキトー
  }

  static class Element { //要素
    boolean valid; //有効/無効
    int weight; //成績内での割合(0-100?)
    int achieved; //達成度(0-100)

    Element() {
      this(false, 0, 0);
    }

    Element(Element org) { //copy
      this(org.valid, org.weight, org.achieved);
    }

    Element(boolean valid, int weight, int achieved) {
      this.valid = valid;
      this.weight = weight;
      this.achieved = achieved;
    }

    double getPercentage() {
      if(!valid || weight <= 0)
        return 0;
      return weight * achieved / 100.0;
    }
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

  //達成割合
  double getPercentage() {
    double percentage = 0;
    for(Element e : elementSet.values())
      percentage += e.getPercentage();
    return percentage;
  }
}
