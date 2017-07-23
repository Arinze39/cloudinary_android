package com.cloudinary.android.sample.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.cloudinary.android.sample.R;
import com.cloudinary.android.sample.model.Resource;
import com.cloudinary.android.sample.widget.GridDividerItemDecoration;

import java.util.List;

public abstract class AbstractPagerFragment extends Fragment {
    private static final int SPAN = 2;

    private RecyclerView recyclerView;
    private View emptyView;
    private int dividerSize;
    private RecyclerView.AdapterDataObserver observer;

    protected abstract ResourcesAdapter getAdapter(int thumbSize);

    protected abstract List<Resource> getData();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_pager_page, container, false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.mainGallery);
        recyclerView.setHasFixedSize(true);
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        GridLayoutManager layoutManager = new GridLayoutManager(inflater.getContext(), SPAN);
        recyclerView.setLayoutManager(layoutManager);
        emptyView = rootView.findViewById(R.id.emptyListView);
        dividerSize = getResources().getDimensionPixelSize(R.dimen.grid_divider_width);
        recyclerView.addItemDecoration(new GridDividerItemDecoration(SPAN, dividerSize));
        observer = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                emptyView.setVisibility(recyclerView.getAdapter().getItemCount() == 0 ? View.VISIBLE : View.INVISIBLE);
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                emptyView.setVisibility(recyclerView.getAdapter().getItemCount() == 0 ? View.VISIBLE : View.INVISIBLE);

            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                emptyView.setVisibility(recyclerView.getAdapter().getItemCount() == 0 ? View.VISIBLE : View.INVISIBLE);

            }
        };

        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();

        if (recyclerView.getWidth() > 0) {
            initThumbSizeAndLoadData();
        } else {
            recyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    recyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                    initThumbSizeAndLoadData();
                    return true;
                }
            });
        }
    }

    private void initThumbSizeAndLoadData() {
        int thumbSize = recyclerView.getWidth() / SPAN - dividerSize / 2;
        final ResourcesAdapter adapter = getAdapter(thumbSize);

        adapter.registerAdapterDataObserver(observer);
        recyclerView.setAdapter(adapter);
        // fetch data after we know the size so we can request the exact size from Cloudinary
        adapter.replaceImages(getData());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        recyclerView.getAdapter().unregisterAdapterDataObserver(observer);
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }
}
