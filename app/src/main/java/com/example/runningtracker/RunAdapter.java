package com.example.runningtracker;

import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class RunAdapter extends RecyclerView.Adapter<RunAdapter.RunViewHolder> {

    private List<Run> data;
    private Context context;
    private LayoutInflater layoutInflater;
    private ItemClickListener mClickListener;

    public RunAdapter(Context context) {
        this.data = new ArrayList<>();
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // create viewholder
    @Override
    public RunViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.db_layout_view, parent, false);
        return new RunViewHolder(itemView);
    }

    // bind viewholder to data
    @Override
    public void onBindViewHolder(RunViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    // get number of runs in adapter
    @Override
    public int getItemCount() {
        return data.size();
    }

    // update date of adapter
    public void setData(List<Run> newData) {
        if (data != null) {
            data.clear();
            data.addAll(newData);
            notifyDataSetChanged();
        } else {
            data = newData;
        }
    }

    // nested class for viewholder with listener for click and item texts
    class RunViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView titleText;

        RunViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.text1);
            itemView.setOnClickListener(this);
        }

        void bind(final Run run) {

            if (run != null) {
                DecimalFormat df = new DecimalFormat("#.00");
                titleText.setText(run.getDate() + " - " + df.format(run.getSpeed()) + "km/h");
            }
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) {
                try {
                    mClickListener.onItemClick(view, getAdapterPosition());
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // get run item from adapter based on position
    Run getItem(int position) {
        return data.get(position);
    }

    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position) throws ExecutionException, InterruptedException;
    }

}
