package com.huawei.agc.subscribedemo.ui;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.agc.subscribedemo.R;
import com.huawei.agc.subscribedemo.db.Article;

import java.util.ArrayList;

/**
 * RecyclerView adapter
 *
 * @date 2022/08/02
 */
public class RvAdapter extends RecyclerView.Adapter<RvAdapter.DataViewHolder> implements View.OnClickListener {
    private Context mContext;
    private ArrayList<Article> list;
    private RecyclerView mrecyclerView;
    private OnItemClickListener onItemClickListener;

    public RvAdapter(Context mContext, ArrayList<Article> mList, RecyclerView recyclerView) {
        this.mContext = mContext;
        this.list = mList;
        this.mrecyclerView = recyclerView;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void update() {
        this.notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position, Article data);
    }

    @NonNull
    @Override
    public DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.item_main, null);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(params);

        view.setOnClickListener(this);

        return new DataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DataViewHolder holder, int position) {

        holder.nick.setText("nick  " + list.get(position).getAuthorId());
//        holder.look.setText(list.get(position).getAuthorId() + "");
        holder.title.setText(list.get(position).getTitle() + "");
        holder.content.setText(list.get(position).getContent() + "");
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onClick(View v) {
        // Obtain the position of the current view based on the RecyclerView.
        int position = mrecyclerView.getChildAdapterPosition(v);
        if (onItemClickListener != null) {
            onItemClickListener.onItemClick(v, position, list.get(position));
        }
    }

    // Create ViewHolder
    public static class DataViewHolder extends RecyclerView.ViewHolder {
        TextView nick;
        TextView look;
        TextView title;
        TextView content;

        public DataViewHolder(View itemView) {
            super(itemView);
            nick = itemView.findViewById(R.id.nick);
            look = itemView.findViewById(R.id.btn_go);
            title = itemView.findViewById(R.id.title);
            content = itemView.findViewById(R.id.content);
        }
    }
}



