package com.cb.SmartGroups.adapters.recyclerviews.teachergroups;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.cb.SmartGroups.R;
import com.cb.SmartGroups.activities.ChatActivity;
import com.cb.SmartGroups.utils.cards.groups.GroupCard;
import com.cb.SmartGroups.utils.customdatamodels.UserType;
import com.cb.SmartGroups.utils.dialogs.teacherdialogs.ChangeSpokerDialog;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.ArrayList;

public class GroupTeacherCardAdapter extends RecyclerView.Adapter<GroupTeacherCardAdapter.GroupCardViewHolder> {

    ArrayList<GroupCard> groupsList;
    Context context;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;

    public GroupTeacherCardAdapter(ArrayList<GroupCard> groupsList, Context context) {
        this.groupsList = groupsList;
        this.context = context;

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public GroupCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.utils_cards_groupcardteacher, parent, false);
        return new GroupCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupCardViewHolder holder, int position) {
        GroupCard groupCard = groupsList.get(position);

        holder.groupName.setText(groupCard.getGroupName());

        if (groupCard.getSpokerName() == null) {
            holder.spokerName.setVisibility(View.GONE);
            holder.changeSpoker.setVisibility(View.GONE);
        } else {
            String spokerNameTitle = "Group spokesperson: " + groupCard.getSpokerName();
            SpannableStringBuilder strb = new SpannableStringBuilder(spokerNameTitle);
            strb.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, "Group spokesperson: ".length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.spokerName.setText(strb);
        }

        if (groupCard.getCollectionId().equals("IndividualGroups")) {
            holder.sentStudentsGroupMessages.setVisibility(View.GONE);
        }

        String text;
        if (groupCard.getNumMessages() == 0) {
            text = "Students have not yet spoken to each other";
        } else {
            text = "Messages sent between students: " + groupCard.getNumMessages();
        }
        holder.sentStudentsGroupMessages.setText(text);

        if (groupCard.getCollectionId().equals("IndividualGroups")) {
            holder.showParticipants.setVisibility(View.GONE);
            holder.deleteGroup.setIconResource(R.drawable.ic_baseline_visibility_off_24);
            holder.deleteGroup.setIconTint(ContextCompat.getColorStateList(holder.deleteGroup.getContext(), R.color.black));
            String hide = "Hide";
            holder.deleteGroup.setText(hide);
            holder.deleteGroup.setTextColor(ContextCompat.getColorStateList(holder.deleteGroup.getContext(), R.color.black));
            holder.deleteGroup.setBackgroundTintList(ContextCompat.getColorStateList(holder.deleteGroup.getContext(), R.color.white));
        }

        holder.showParticipants.setOnClickListener(v -> {
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(context);
            builderSingle.setTitle("Participants");

            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context, R.layout.utils_participantname, R.id.participantName, groupCard.getParticipantNames()) {
                @Override
                public boolean isEnabled(int position1) {
                    return false;
                }
            };

            builderSingle.setNegativeButton("Ok", (dialog, which) -> dialog.dismiss());
            builderSingle.setAdapter(arrayAdapter, null);
            builderSingle.show();
        });

        if (groupCard.getCollectionId().equals("IndividualGroups")) {
            holder.deleteGroup.setOnClickListener(v -> {
                DocumentReference groupRef = fStore
                        .collection("CoursesOrganization")
                        .document(groupCard.getCourseName())
                        .collection("Subjects")
                        .document(groupCard.getSubjectName())
                        .collection(groupCard.getCollectionId())
                        .document(groupCard.getGroupId());
                groupRef.update("hasVisibility", false);
            });
        } else {

            holder.deleteGroup.setOnClickListener(v -> {

                DocumentReference groupRef = fStore
                        .collection("CoursesOrganization")
                        .document(groupCard.getCourseName())
                        .collection("Subjects")
                        .document(groupCard.getSubjectName())
                        .collection(groupCard.getCollectionId())
                        .document(groupCard.getGroupId());

                if (groupCard.getCollectionId().equals("IndividualGroups")) {
                    groupRef.update("hasVisibility", false);
                } else {
                    deleteDocuments(groupRef);
                }
            });

        }

        holder.changeSpoker.setOnClickListener(v -> {
            ChangeSpokerDialog dialog = new ChangeSpokerDialog(groupCard.getCourseName(), groupCard.getSubjectName(), groupCard.getGroupId(), groupCard.getSpokerID());
            dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "dialog");
        });

        holder.view.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);

            // Convert the GroupCard to JSON to send it to ChatActivity
            Gson gson = new Gson();
            String cardAsString = gson.toJson(groupCard);
            intent.putExtra("cardAsString", cardAsString);
            intent.putExtra("userType", UserType.TYPE_TEACHER);
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return groupsList.size();
    }

    static class GroupCardViewHolder extends RecyclerView.ViewHolder {
        View view;
        TextView groupName;
        TextView spokerName;
        TextView sentStudentsGroupMessages;

        MaterialButton showParticipants;
        MaterialButton deleteGroup;
        MaterialButton changeSpoker;

        GroupCardViewHolder(View view) {
            super(view);
            this.view = view;
            groupName = view.findViewById(R.id.groupName);
            showParticipants = view.findViewById(R.id.showParticipants);
            spokerName = view.findViewById(R.id.spokerName);
            sentStudentsGroupMessages = view.findViewById(R.id.sentStudentsGroupMessages);
            deleteGroup = view.findViewById(R.id.deleteGroup);
            changeSpoker = view.findViewById(R.id.changeSpoker);
        }
    }

    private void deleteDocuments(DocumentReference groupRef) {
        groupRef.collection("ChatRoomWithTeacher").get().addOnSuccessListener(queryDocumentSnapshots1 -> {
            for (DocumentSnapshot document : queryDocumentSnapshots1) {
                document.getReference().delete();
            }

            groupRef.collection("StorageWithTeacher").get().addOnSuccessListener(queryDocumentSnapshots2 -> {
                for (DocumentSnapshot document : queryDocumentSnapshots2) {
                    document.getReference().delete();
                }
                groupRef.collection("ChatRoomWithoutTeacher").get().addOnSuccessListener(queryDocumentSnapshots3 -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots3) {
                        document.getReference().delete();
                    }

                    groupRef.collection("StorageWithoutTeacher").get().addOnSuccessListener(queryDocumentSnapshots4 -> {
                        for (DocumentSnapshot document : queryDocumentSnapshots4) {
                            document.getReference().delete();
                        }
                        groupRef.collection("InteractivityCards").get().addOnSuccessListener(queryDocumentSnapshots5 -> {
                            for (DocumentSnapshot document : queryDocumentSnapshots5) {
                                document.getReference().delete();
                            }
                            groupRef.delete();
                        });
                    });
                });
            });
        });
    }

    public void filteredList(ArrayList<GroupCard> filteredList) {
        groupsList = filteredList;
        notifyDataSetChanged();
    }

}

