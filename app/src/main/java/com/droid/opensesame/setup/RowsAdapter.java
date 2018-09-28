package com.droid.opensesame.setup;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class RowsAdapter extends RecyclerView.Adapter<RowsAdapter.RowsHolder> {

    private List<Row> rowList;

    public class RowsHolder extends RecyclerView.ViewHolder {
        public TextView header;
        public TextView description;

        public RowsHolder(View view) {
            super(view);
            header = (TextView) view.findViewById(R.id.header);
            description = (TextView) view.findViewById(R.id.description);
        }
    }

    public RowsAdapter(List<Row> rowList) {
        this.rowList = rowList;
    }

    @NonNull
    @Override
    public RowsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_row, parent, false);

        return new RowsHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RowsHolder holder, int position) {
        Row row = rowList.get(position);
        holder.header.setText(row.getHeader());
        holder.description.setText(row.getDescription());
    }

    @Override
    public int getItemCount() {
        return rowList.size();
    }

}
