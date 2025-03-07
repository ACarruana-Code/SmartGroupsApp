package com.cb.SmartGroups.adapters.recyclerviews.studentgroups;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import androidx.recyclerview.widget.RecyclerView;

import com.cb.SmartGroups.R;

import com.cb.SmartGroups.activities.ChatActivity;
import com.cb.SmartGroups.utils.cards.groups.GroupCard;

import com.cb.SmartGroups.utils.customdatamodels.UserType;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.ArrayList;

public class GroupStudentCardAdapter extends RecyclerView.Adapter<GroupStudentCardAdapter.GroupStudentCardViewHolder> {

    ArrayList<GroupCard> groupsList;
    Context context;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;

    public GroupStudentCardAdapter(ArrayList<GroupCard> groupsList, Context context) {
        this.groupsList = groupsList;
        this.context = context;

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public GroupStudentCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.utils_cards_groupcardstudent, parent, false);
        return new GroupStudentCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupStudentCardViewHolder holder, int position) {
        GroupCard groupCard = groupsList.get(position);
        holder.groupName.setText(groupCard.getGroupName());

        if(groupCard.getHasTeacher()){
            holder.image.setImageResource(R.drawable.ic_presentation);
        } else {
            holder.image.setImageResource(R.drawable.ic_groupchat);
        }

        holder.view.setOnClickListener(view -> {
            Intent intent = new Intent(context, ChatActivity.class);

            // Convert the GroupCard to JSON to send it to ChatActivity
            Gson gson = new Gson();
            String cardAsString = gson.toJson(groupCard);
            intent.putExtra("cardAsString", cardAsString);
            intent.putExtra("userType", UserType.TYPE_STUDENT);
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return groupsList.size();
    }

    static class GroupStudentCardViewHolder extends RecyclerView.ViewHolder {
        View view;
        ShapeableImageView image;
        TextView groupName;

        GroupStudentCardViewHolder(View view) {
            super(view);
            this.view = view;
            image = view.findViewById(R.id.image);
            groupName = view.findViewById(R.id.groupName);
        }
    }

}

