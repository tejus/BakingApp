package com.tejus.bakingapp.ui.detail;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tejus.bakingapp.R;
import com.tejus.bakingapp.model.Step;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StepsOverviewAdapter extends RecyclerView.Adapter<StepsOverviewAdapter.StepsOverviewViewHolder> {

    private List<Step> mSteps;
    private OnStepAdapterClickListener mClickListener;

    StepsOverviewAdapter(OnStepAdapterClickListener clickListener) {
        mClickListener = clickListener;
    }

    @NonNull
    @Override
    public StepsOverviewViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_step, viewGroup, false);
        return new StepsOverviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StepsOverviewViewHolder holder, int i) {
        String number = holder.itemView.getResources()
                .getString(R.string.detail_step_overview_number, (mSteps.get(i).getId() + 1));
        holder.mItemNumber.setText(number);
        holder.mItemName.setText(mSteps.get(i).getShortDescription());
        holder.itemView.setOnClickListener(v -> mClickListener.onStepClick(i));
    }

    @Override
    public int getItemCount() {
        if (mSteps == null) return 0;
        return mSteps.size();
    }

    void setSteps(List<Step> steps) {
        this.mSteps = steps;
        notifyDataSetChanged();
    }

    class StepsOverviewViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_step_number)
        TextView mItemNumber;
        @BindView(R.id.item_step_name)
        TextView mItemName;

        StepsOverviewViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public interface OnStepAdapterClickListener {
        void onStepClick(int position);
    }
}
