package com.fxn;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fxn.adapters.MainImageAdapter;
import com.fxn.pix.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ShowImageBottomSheet extends BottomSheetDialogFragment {

    private FragmentActivity fContext;
    private MainImageAdapter mainImageAdapter;
    private GridLayoutManager mLayoutManager;
    private RecyclerView recyclerView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        fContext = (FragmentActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.show_image_bottom_sheet_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView(view);
    }

    private void initView(View view) {
        mainImageAdapter = new MainImageAdapter(fContext);
        mLayoutManager = new GridLayoutManager(fContext, MainImageAdapter.SPAN_COUNT);
        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (mainImageAdapter.getItemViewType(position) == MainImageAdapter.HEADER) {
                    return MainImageAdapter.SPAN_COUNT;
                }
                return 1;
            }
        });
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(mainImageAdapter);
    }
}
