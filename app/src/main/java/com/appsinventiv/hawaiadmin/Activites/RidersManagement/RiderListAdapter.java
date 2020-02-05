package com.appsinventiv.hawaiadmin.Activites.RidersManagement;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.appsinventiv.hawaiadmin.Models.RiderModel;
import com.appsinventiv.hawaiadmin.R;
import com.appsinventiv.hawaiadmin.TrackingManagement.MapsActivity;
import com.appsinventiv.hawaiadmin.Utils.CommonUtils;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class RiderListAdapter extends RecyclerView.Adapter<RiderListAdapter.ViewHolder> {
    Context context;
    ArrayList<RiderModel> itemList;
    ArrayList<RiderModel> arrayList;
    RiderAdapterCallbacks callbacks;


    public RiderListAdapter(Context context, ArrayList<RiderModel> itemList, RiderAdapterCallbacks callbacks) {
        this.context = context;
        this.itemList = itemList;
        this.callbacks = callbacks;
        this.arrayList = new ArrayList<>(itemList);

    }

    public void updateList(ArrayList<RiderModel> list) {
        this.itemList = list;
        arrayList.clear();
        arrayList.addAll(list);
        notifyDataSetChanged();
    }

    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        itemList.clear();
        if (charText.length() == 0) {
            itemList.addAll(arrayList);
        } else {
            for (RiderModel text : arrayList) {
                if (text.getName().toLowerCase().contains(charText.toLowerCase()) || text.getPhone().contains(charText)) {
                    itemList.add(text);
                }
            }
        }
        notifyDataSetChanged();

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.rider_item_layout, parent, false);
        RiderListAdapter.ViewHolder viewHolder = new RiderListAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final RiderModel model = itemList.get(position);
        holder.name.setText(model.getName());
        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callbacks.onEditRider(model);
            }
        });
        holder.track.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                callbacks.onTrackRider(model);
                if (model.getLatitude() > 0) {
                    if (model.isActive()) {
                        Intent i = new Intent(context, MapsActivity.class);
                        i.putExtra("riderId", model.getId());
                        context.startActivity(i);
                    } else {
                        CommonUtils.showToast("Tracking is off");
                    }
                } else {
                    CommonUtils.showToast("No coordinates available");
                }
            }
        });
        if (model.getPicUrl() != null) {
            Glide.with(context).load(model.getPicUrl()).into(holder.image);
        } else {
            Glide.with(context).load(R.drawable.add_picture).into(holder.image);

        }

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        Button edit, track;
        CircleImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.name);
            edit = itemView.findViewById(R.id.edit);
            track = itemView.findViewById(R.id.track);
        }
    }

    public interface RiderAdapterCallbacks {
        public void onEditRider(RiderModel model);

        public void onTrackRider(RiderModel model);
    }
}
