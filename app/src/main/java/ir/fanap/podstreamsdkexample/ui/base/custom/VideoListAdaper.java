package ir.fanap.podstreamsdkexample.ui.base.custom;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.podstreamsdkexample.R;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import ir.fanap.podstreamsdkexample.data.VideoItem;

public class VideoListAdaper extends RecyclerView.Adapter<VideoListAdaper.ViewHolder> {

    ItemClickListener mClickListener;
    List<VideoItem> dataList;
    private LayoutInflater mInflater;

    public VideoListAdaper(List<VideoItem> dataList, Context context) {
        this.dataList = dataList;
        mInflater = LayoutInflater.from(context);
    }

    public void setDataList(List<VideoItem> dataList) {
        this.dataList = dataList;
        notifyDataSetChanged();
    }

    public void addVideo(VideoItem video) {
        this.dataList.add(0, video);
        notifyDataSetChanged();
    }


    public void setmClickListener(ItemClickListener mClickListener) {
        this.mClickListener = mClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.video_item_row, parent, false);
        return new ViewHolder(view);
    }

    private VideoItem getItem(int position) {
        return this.dataList.get(position);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.txt_hashcode.setText(getItem(position).getVideoHash());
        holder.txt_name.setText(getItem(position).getVideoName());
        holder.txt_quality.setText(getItem(position).getVideoQuality());
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txt_name;
        TextView txt_quality;
        TextView txt_hashcode;

        ViewHolder(View itemView) {
            super(itemView);
            txt_name = itemView.findViewById(R.id.txt_name);
            txt_quality = itemView.findViewById(R.id.txt_quality);
            txt_hashcode = itemView.findViewById(R.id.txt_hashcode);
            itemView.setOnClickListener(this::onClick);
        }

        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(getItem(getAdapterPosition()));
        }

        @NonNull
        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

    }

    public interface ItemClickListener {
        void onItemClick(VideoItem item);
    }
}
