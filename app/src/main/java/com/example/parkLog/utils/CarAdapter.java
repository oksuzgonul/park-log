package com.example.parkLog.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkLog.R;
import com.example.parkLog.data.Car;

import java.util.List;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.ViewHolder> {
    private List<Car> carList;
    private Context context;
    private final ItemClickListener itemClickListener;

    public CarAdapter(Context context, ItemClickListener itemClickListener) {
        this.context = context;
        this.itemClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClickListener(int itemId);
    }

    public class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        private final TextView carNameView;
        private final TextView trackedView;
        private final TextView ownerView;
        private final ImageView parkIconImage;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            carNameView = itemView.findViewById(R.id.list_car_name);
            trackedView = itemView.findViewById(R.id.tracking_status);
            ownerView = itemView.findViewById(R.id.list_car_owner);
            parkIconImage = itemView.findViewById(R.id.park_icon);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int carId = carList.get(getAdapterPosition()).getId();
            itemClickListener.onItemClickListener(carId);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.car_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.carNameView.setText(carList.get(position).getCarName());
        String ownerText;
        if (!carList.get(position).getCarOwner().isEmpty()) {
            ownerText = context.getString(R.string.belongs_to) + " " + carList.get(position).getCarOwner();
        } else {
            ownerText = context.getString(R.string.owner_not_specified);
        }
        holder.ownerView.setText(ownerText);

        if (carList.get(position).getParkedState()) {
            holder.parkIconImage.setImageResource(R.drawable.ic_green_park);
            holder.trackedView.setText(R.string.currently_tracked);
        } else {
            holder.parkIconImage.setImageResource(R.drawable.ic_red_park);
            holder.trackedView.setText(R.string.currently_not_tracked);
        }
    }

    @Override
    public int getItemCount() {
        return carList == null ? 0 : carList.size();
    }

    public void setCars(List<Car> carList)  {
        this.carList = carList;
        notifyDataSetChanged();
    }

}
