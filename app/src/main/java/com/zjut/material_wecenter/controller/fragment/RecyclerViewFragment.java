package com.zjut.material_wecenter.controller.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.github.florent37.materialviewpager.adapter.RecyclerViewMaterialAdapter;
import com.zjut.material_wecenter.Client;
import com.zjut.material_wecenter.R;
import com.zjut.material_wecenter.controller.adapter.ActionrViewAdapter;
import com.zjut.material_wecenter.models.Action;
import com.zjut.material_wecenter.models.Result;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewFragment extends Fragment {

    public static int PUBLISH = 101;
    public static int ANSWER = 201;
    private boolean loading = true;

    private String uid;
    private int actions;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;

    private List<Action> mContentItems = new ArrayList<>();

    private int page = 1;   // 当前页面的页码

    public static RecyclerViewFragment newInstance(String uid, int actions) {
        Bundle args = new Bundle();
        args.putString("uid", uid);
        args.putInt("actions", actions);
        RecyclerViewFragment fragment = new RecyclerViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        uid = args.getString("uid");
        actions = args.getInt("actions");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recyclerview, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new RecyclerViewMaterialAdapter(new ActionrViewAdapter(getActivity(), mContentItems, actions));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int pastVisiblesItems = layoutManager.findFirstVisibleItemPosition();
                if (loading) {
                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        // 达到底部加载更多
                        loading = false;
                        new LoadActions().execute();
                    }
                }
            }
        });
        MaterialViewPagerHelper.registerRecyclerView(getActivity(), mRecyclerView, null);
        new LoadActions().execute();
    }

    class LoadActions extends AsyncTask<Void, Void, Void> {

        private Result result;

        @Override
        protected Void doInBackground(Void... params) {
            result = Client.getInstance().getUserActions(uid, actions, page);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (result!=null && result.getErrno()==1) {
                mContentItems.addAll((ArrayList<Action>) result.getRsm());
                mAdapter.notifyDataSetChanged();
                page++;
                loading = true;
            }
        }
    }
}
