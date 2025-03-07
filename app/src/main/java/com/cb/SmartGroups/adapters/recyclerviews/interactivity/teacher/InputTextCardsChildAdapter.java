package com.cb.SmartGroups.adapters.recyclerviews.interactivity.teacher;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cb.SmartGroups.R;
import com.cb.SmartGroups.utils.cards.interactivity.teachercards.InputTextCardParent;
import com.cb.SmartGroups.utils.firesoredatamodels.interactivitydocuments.InputTextCardDocument;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;

public class InputTextCardsChildAdapter extends RecyclerView.Adapter<InputTextCardsChildAdapter.InputTextCardsChildViewHolder> {

    private ArrayList<InputTextCardParent.InputTextCardChild> cardsList;
    private boolean isEvaluable;
    private final Context context;


    public InputTextCardsChildAdapter(ArrayList<InputTextCardParent.InputTextCardChild> cardsList, boolean isEvaluable, Context context) {
        this.cardsList = cardsList;
        this.isEvaluable = isEvaluable;
        this.context = context;
    }

    @NonNull
    @Override
    public InputTextCardsChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.utils_cards_interactivity_teachercards_inputextcardchild, parent, false);
        return new InputTextCardsChildViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InputTextCardsChildViewHolder holder, int position) {
        InputTextCardParent.InputTextCardChild inputTextCardChild = cardsList.get(position);

        holder.studentAnswer.setText(inputTextCardChild.getResponse());

        if (isEvaluable) {
            if (inputTextCardChild.getHasMarkSet()) {
                holder.markSlider.setVisibility(View.GONE);
                holder.addMark.setVisibility(View.GONE);
            } else {
                holder.markSlider.setVisibility(View.VISIBLE);
                holder.addMark.setVisibility(View.VISIBLE);
            }
        } else {
            holder.markSlider.setVisibility(View.GONE);
            holder.addMark.setVisibility(View.GONE);
        }

        holder.addMark.setOnClickListener(v -> {
            float mark = holder.markSlider.getValue();

            InputTextCardDocument inputTextCardDocument = inputTextCardChild.getInputTextCardDocument();

            DocumentReference documentReference = inputTextCardChild.getDocumentReference();

            ArrayList<InputTextCardDocument.InputTextCardStudentData> studentsData = new ArrayList<InputTextCardDocument.InputTextCardStudentData>();
            for (InputTextCardDocument.InputTextCardStudentData studentData : inputTextCardDocument.getStudentsData()) {
                if (studentData.getStudentID().equals(inputTextCardChild.getStudentID())) {
                    studentData.setMark(mark);
                    studentData.setHasMarkSet(true);
                }
                studentsData.add(studentData);
            }

            documentReference
                    .update("studentsData", studentsData)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(context, "Qualified question", Toast.LENGTH_SHORT).show();
                    });

        });

    }

    @Override
    public int getItemCount() {
        return cardsList.size();
    }

    public static class InputTextCardsChildViewHolder extends RecyclerView.ViewHolder {

        TextView studentAnswer;
        Slider markSlider;
        MaterialButton addMark;

        public InputTextCardsChildViewHolder(@NonNull View itemView) {
            super(itemView);
            studentAnswer = itemView.findViewById(R.id.studentAnswer);
            markSlider = itemView.findViewById(R.id.markSlider);
            addMark = itemView.findViewById(R.id.addMark);
        }
    }

}
