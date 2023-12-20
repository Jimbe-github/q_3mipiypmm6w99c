package com.teratail.q_3mipiypmm6w99c;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.annotation.*;
import androidx.fragment.app.*;
import androidx.recyclerview.widget.RecyclerView;

import java.util.*;
import java.util.function.*;

public class ListFragment extends Fragment {
  private static final String LOG_TAG = "ListFragment";
  private static final String ITEM_FRAGMENT_REQUEST_KEY = "item_frasgment_request_key";

  ListFragment() {
    super(R.layout.fragment_list);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    Adapter adapter = new Adapter();

    RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
    recyclerView.setAdapter(adapter);

    FragmentManager fm = getChildFragmentManager();
    fm.setFragmentResultListener(ITEM_FRAGMENT_REQUEST_KEY, getViewLifecycleOwner(), (rkey,result) -> {
      Grades grades = (Grades)result.getSerializable(ItemDialogFragment.RESULT_KEY_GRADES);
      if(grades == null) return;

      int position = result.getInt(ItemDialogFragment.RESULT_KEY_POSITION, -1);
      Log.d(LOG_TAG, "FragmentResultListener position=" + position);
      if(position < 0) {
        adapter.add(grades);
      } else {
        adapter.set(position, grades);
      }
    });

    adapter.setRowClickListener((position,grades) -> {
      Log.d(LOG_TAG, "RowClickListener position=" + position);
      ItemDialogFragment.getInstance(ITEM_FRAGMENT_REQUEST_KEY, position, grades).show(fm, null);
    });

    Button addButton = view.findViewById(R.id.add_button);
    addButton.setOnClickListener(v -> {
      ItemDialogFragment.getInstance(ITEM_FRAGMENT_REQUEST_KEY, -1, null).show(fm, null);
    });
  }

  private static class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
    private final List<Grades> list = new ArrayList<>();
    private BiConsumer<Integer,Grades> rowClickListener;

    void setRowClickListener(BiConsumer<Integer,Grades> l) {
      rowClickListener = l;
    }

    void add(Grades grades) {
      list.add(grades);
      notifyItemInserted(list.size() - 1);
    }
    void set(int position, Grades grades) {
      list.set(position, grades);
      notifyItemChanged(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      return new ViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
      holder.bind(list.get(position));
    }

    @Override
    public int getItemCount() {
      return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
      private final TextView text1, text2;
      private Grades grades;
      ViewHolder(@NonNull ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false));
        itemView.setOnClickListener(v -> {
          if(rowClickListener != null) rowClickListener.accept(getAdapterPosition(), grades);
        });
        text1 = itemView.findViewById(android.R.id.text1);
        text2 = itemView.findViewById(android.R.id.text2);
      }
      void bind(Grades grades) {
        this.grades = grades;
        text1.setText(grades.subjectName);
        text2.setText(grades.getPercentage() + " %");
      }
    }
  }
}