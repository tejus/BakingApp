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
import java.util.Locale;

public class StepsOverviewAdapter extends RecyclerView.Adapter<StepsOverviewAdapter.StepsOverviewViewHolder> {

    private List<Step> mSteps;

    @NonNull
    @Override
    public StepsOverviewViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_step, viewGroup, false);
        return new StepsOverviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StepsOverviewViewHolder holder, int i) {
        String number = String.format(Locale.getDefault(), "%02d", (mSteps.get(i).getId() + 1));
        holder.mItemNumber.setText(number + ".");
        holder.mItemName.setText(mSteps.get(i).getShortDescription());
    }

    @Override
    public int getItemCount() {
        if (mSteps == null) return 0;
        return mSteps.size();
    }

    public void setSteps(List<Step> steps) {
        this.mSteps = steps;
        notifyDataSetChanged();
    }

    class StepsOverviewViewHolder extends RecyclerView.ViewHolder {
        TextView mItemNumber;
        TextView mItemName;

        StepsOverviewViewHolder(@NonNull View itemView) {
            super(itemView);
            mItemNumber = itemView.findViewById(R.id.item_step_number);
            mItemName = itemView.findViewById(R.id.item_step_name);
        }
    }
}
