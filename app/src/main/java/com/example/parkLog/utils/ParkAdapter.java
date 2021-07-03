package com.example.parkLog.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkLog.R;
import com.example.parkLog.data.AppDataBase;
import com.example.parkLog.data.Park;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ParkAdapter extends RecyclerView.Adapter<ParkAdapter.ViewHolder> {
    private List<Park> parkList;
    private final Context context;
    private final ItemClickListener itemClickListener;
    private AppDataBase dataBase;

    public ParkAdapter(Context context, ItemClickListener itemClickListener) {
        this.context = context;
        this.itemClickListener =itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClickListener(int itemId);
    }

    public class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        public final TextView parkTimeTextView;
        public final TextView parkNameTextView;
        private final ImageView parkTrackingIcon;
        private final Button terminateParkButton;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            parkNameTextView = itemView.findViewById(R.id.parking_name);
            parkTimeTextView = itemView.findViewById(R.id.park_start_time);
            parkTrackingIcon = itemView.findViewById(R.id.tracking_status_icon);
            terminateParkButton = itemView.findViewById(R.id.status_edit_button);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int parkId = parkList.get(getAdapterPosition()).getId();
            itemClickListener.onItemClickListener(parkId);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.park_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Park currentPark = parkList.get(position);
        SimpleDateFormat parkTimeFormat = new SimpleDateFormat("MMM dd yyyy, HH:mm", Locale.getDefault());
        String parkTime = parkTimeFormat.format(currentPark.getParkTime());
        String parkTimeText = context.getString(R.string.parked_on_text) + " " + parkTime;
        holder.parkTimeTextView.setText(parkTimeText);
        holder.parkNameTextView.setText(currentPark.getParkLocationName());
        if (currentPark.getParkEndTime() != null) {
            holder.parkTrackingIcon.setImageResource(R.drawable.ic_car);
            holder.terminateParkButton.setText(R.string.park_finished);
            holder.terminateParkButton.setClickable(false);
        } else {
            holder.terminateParkButton.setOnClickListener(view -> AppExecutors.getInstance().diskIO().execute(() -> {
                currentPark.setParkEndTime(Calendar.getInstance().getTime());
                dataBase.parkDao().updatePark(currentPark);
            }));
        }
    }

    @Override
    public int getItemCount() {
        return parkList == null ? 0 : parkList.size();
    }

    public void setParks(List<Park> parkList) {
        this.parkList = parkList;
        notifyDataSetChanged();
    }
    public void setDataBase(AppDataBase dataBase) {
        this.dataBase = dataBase;
    }
}


