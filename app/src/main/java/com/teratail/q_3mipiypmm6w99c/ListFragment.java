package com.teratail.q_3mipiypmm6w99c;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.annotation.*;
import androidx.fragment.app.*;
import androidx.lifecycle.*;
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

    //どちらか選択
    GradesStorage gradesStorage = new SQLiteGradesStorage(requireContext());
    //GradesStorage gradesStorage = new SharedPreferencesGradesStorage(requireContext());

    if(gradesStorage instanceof LifecycleObserver) {
      getLifecycle().addObserver((LifecycleObserver) gradesStorage);
    }

    gradesStorage.load().observe(getViewLifecycleOwner(), adapter::setList);
    adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
      @Override
      public void onChanged() { gradesStorage.save(adapter.getList()); }
      @Override
      public void onItemRangeChanged(int positionStart, int itemCount) { onChanged(); }
      @Override
      public void onItemRangeInserted(int positionStart, int itemCount) { onChanged(); }
      @Override
      public void onItemRangeRemoved(int positionStart, int itemCount) { onChanged(); }
      @Override
      public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) { onChanged(); }
    });

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

    @SuppressLint("NotifyDataSetChanged")
    void setList(List<Grades> newList) {
      list.clear();
      list.addAll(newList); //防御コピー
      notifyDataSetChanged();
    }
    List<Grades> getList() {
      return new ArrayList<>(list); //防御コピー
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
      private final TextView subjectsText;
      private final GradesTotalView gradesTotalView;
      private Grades grades;
      ViewHolder(@NonNull ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row, parent, false));
        itemView.setOnClickListener(v -> {
          if(rowClickListener != null) rowClickListener.accept(getAdapterPosition(), grades);
        });
        subjectsText = itemView.findViewById(R.id.subjects_text);
        gradesTotalView = itemView.findViewById(R.id.grades_total_view);
      }
      void bind(Grades grades) {
        this.grades = grades;
        subjectsText.setText(grades.subjectName);
        gradesTotalView.set(grades);
      }
    }
  }
}