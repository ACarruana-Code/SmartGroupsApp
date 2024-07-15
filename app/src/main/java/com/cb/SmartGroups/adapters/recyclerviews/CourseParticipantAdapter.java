package com.cb.SmartGroups.adapters.recyclerviews;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.cb.SmartGroups.R;
import com.cb.SmartGroups.utils.cards.CourseParticipantCard;
import com.cb.SmartGroups.utils.customdatamodels.UserType;
import com.cb.SmartGroups.utils.dialogs.teacherdialogs.IndividualStatisticsDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CourseParticipantAdapter extends RecyclerView.Adapter<CourseParticipantAdapter.CourseParticipantViewHolder> {

    private List<CourseParticipantCard> courseParticipantList;
    private int userType;
    private Context context;

    private String selectedCourse;
    private String selectedSubject;

    private HashMap<String, HashMap<String, HashMap<String, Double>>> allStudentsStatistics;


    public CourseParticipantAdapter(String selectedCourse, String selectedSubject, List<CourseParticipantCard> courseParticipantList, int userType, Context context, HashMap<String, HashMap<String, HashMap<String, Double>>> allStudentsStatistics) {
        this.courseParticipantList = courseParticipantList;
        this.userType = userType;
        this.context = context;
        this.selectedCourse = selectedCourse;
        this.selectedSubject = selectedSubject;
        this.allStudentsStatistics = allStudentsStatistics;
    }

    @NonNull
    @Override
    public CourseParticipantViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.utils_cards_courseparticipant, viewGroup, false);

        return new CourseParticipantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseParticipantViewHolder viewHolder, int position) {
        CourseParticipantCard courseParticipant = courseParticipantList.get(position);

        viewHolder.courseparticipant_avatar.setImageResource(courseParticipant.getImage());
        viewHolder.participantRole.setText(courseParticipant.getParticipantRole());
        viewHolder.participantName.setText(courseParticipant.getParticipantName());
        viewHolder.participantEmail.setText(courseParticipant.getParticipantEmail());


        if (userType == UserType.TYPE_TEACHER) {
            viewHolder.view.setOnClickListener(v -> {
                HashMap<String, HashMap<String, Double>> studentsStatistics = allStudentsStatistics.get(courseParticipant.getParticipantID());
                if (studentsStatistics.keySet().size() == 0) {
                        Toast.makeText(context, "This student is not in any group", Toast.LENGTH_SHORT).show();
                } else {
                    IndividualStatisticsDialog dialog = new IndividualStatisticsDialog(selectedCourse, selectedSubject, courseParticipant.getParticipantName(), courseParticipant.getParticipantID(), allStudentsStatistics.get(courseParticipant.getParticipantID()));
                    dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "dialog");
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return courseParticipantList.size();
    }

    static class CourseParticipantViewHolder extends RecyclerView.ViewHolder {
        View view;
        ImageView courseparticipant_avatar;
        TextView participantRole;
        TextView participantName;
        TextView participantEmail;

        CourseParticipantViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            courseparticipant_avatar = itemView.findViewById(R.id.courseparticipant_avatar);
            participantRole = itemView.findViewById(R.id.courseparticipant_role);
            participantName = itemView.findViewById(R.id.courseparticipant_name);
            participantEmail = itemView.findViewById(R.id.courseparticipant_email);
        }
    }


    public void filteredList(ArrayList<CourseParticipantCard> filteredList) {
        courseParticipantList = filteredList;
        notifyDataSetChanged();
    }


}

