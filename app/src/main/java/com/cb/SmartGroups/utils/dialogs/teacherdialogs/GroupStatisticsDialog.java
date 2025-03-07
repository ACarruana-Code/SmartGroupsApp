package com.cb.SmartGroups.utils.dialogs.teacherdialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.cb.SmartGroups.R;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class GroupStatisticsDialog extends DialogFragment {

    TextView averageGroupMarkInputText;
    TextView averageIndividualMarkInputText;

    TextView groupalPerc;
    TextView individualPerc;

    HashMap<String, Double> statistics;

    Context context;

    public GroupStatisticsDialog(HashMap<String, Double> statistics) {
        this.statistics = statistics;
    }

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @NonNull
    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View view = getActivity().getLayoutInflater().inflate(R.layout.utils_dialogs_groupstatisticsdialog, null);

        averageGroupMarkInputText = view.findViewById(R.id.averageGroupMarkInputText);
        averageIndividualMarkInputText = view.findViewById(R.id.averageIndividualMarkInputText);
        groupalPerc = view.findViewById(R.id.groupalPerc);
        individualPerc = view.findViewById(R.id.individualPerc);

        // InputText Groupal
        Double groupalMarkInputText = statistics.get("Groupal InputText Mark");
        Double evaluableGroupalInputCards = statistics.get("Groupal InputText Cards");

        if (groupalMarkInputText != null && evaluableGroupalInputCards != null) {
            if (evaluableGroupalInputCards != 0) {
                setAverageInputTextString(groupalMarkInputText, evaluableGroupalInputCards, averageGroupMarkInputText);
            } else {
                String text = "No se ha evaluado ninguna actividad grupal";
                averageGroupMarkInputText.setTextColor(ContextCompat.getColor(context, R.color.orange));
                averageGroupMarkInputText.setText(text);
            }
        }

        // InputText Individual
        Double individualMarkInputText = statistics.get("Individual InputText Mark");
        Double evaluableIndividualStudents = statistics.get("Individual Evaluable Students");

        if (individualMarkInputText != null && evaluableIndividualStudents != null) {
            if (evaluableIndividualStudents != 0) {
                setAverageInputTextString(individualMarkInputText, evaluableIndividualStudents, averageIndividualMarkInputText);
            } else {
                String text = "No se ha evaluado ninguna actividad individual";
                averageIndividualMarkInputText.setTextColor(ContextCompat.getColor(context, R.color.orange));
                averageIndividualMarkInputText.setText(text);
            }
        }

        // Multichoice Groupal
        Double evaluableGroupalMultichoiceCards = statistics.get("Groupal Multichoice Cards");
        Double totalGroupalPoints = statistics.get("Groupal Multichoice Mark");

        if (evaluableGroupalMultichoiceCards != null && totalGroupalPoints != null) {
            if (evaluableGroupalMultichoiceCards != 0) {
                setMultichoiceTextString(totalGroupalPoints, evaluableGroupalMultichoiceCards, groupalPerc);
            } else {
                String text = "No se ha evaluado ninguna actividad grupal";
                groupalPerc.setTextColor(ContextCompat.getColor(context, R.color.orange));
                groupalPerc.setText(text);
            }
        }

        // Multichoice Individual
        Double evaluableIndividuals = statistics.get("Individial Multichoice Evaluable");
        Double evaluableIndividualMarks = statistics.get("Individual Mulichoice Mark");

        if (evaluableIndividuals != null && evaluableIndividualMarks != null) {
            if (evaluableIndividuals != 0) {
                setMultichoiceTextString(evaluableIndividualMarks, evaluableIndividuals, individualPerc);
            } else {
                String text = "No se ha evaluado ninguna actividad individual";
                individualPerc.setTextColor(ContextCompat.getColor(context, R.color.orange));
                individualPerc.setText(text);
            }
        }

        builder.setTitle("Estadísticas del actividades evaluadas")
                .setView(view)
                .setNegativeButton("Ocultar", (dialog, i) -> {
                    // Just closes the dialog
                });

        return builder.create();
    }

    private void setAverageInputTextString(double mark1, double mark2, TextView textView) {
        double average = mark1 / mark2;

        if (average < 5) {
            textView.setTextColor(ContextCompat.getColor(context, R.color.red)); // Red
        } else if (average >= 5 && average < 7) {
            textView.setTextColor(ContextCompat.getColor(context, R.color.yellow)); // Yellow
        } else if (average >= 7 && average < 9) {
            textView.setTextColor(ContextCompat.getColor(context, R.color.green1)); // Green 1
        } else {
            textView.setTextColor(ContextCompat.getColor(context, R.color.green2)); // Green 2
        }

        String averageMarkText = "" + average;

        if (averageMarkText.endsWith(".0")) {
            averageMarkText = averageMarkText.replace(".0", "");
        } else if (averageMarkText.length() > 4) {
            averageMarkText = averageMarkText.substring(0, 4);
        }

        String text =  averageMarkText + "/10";
        textView.setText(text);
    }

    private void setMultichoiceTextString(double mark1, double mark2, TextView textView) {
        double rate = mark1 / mark2;

        if (rate < 0.5) {
            textView.setTextColor(ContextCompat.getColor(context, R.color.red)); // Red
        } else if (rate >= 0.5 && rate < 0.7) {
            textView.setTextColor(ContextCompat.getColor(context, R.color.yellow)); // Yellow
        } else if (rate >= 0.7 && rate < 0.9) {
            textView.setTextColor(ContextCompat.getColor(context, R.color.green1)); // Green 1
        } else {
            textView.setTextColor(ContextCompat.getColor(context, R.color.green2)); // Green 2
        }
        double ratePerc = rate * 100;
        String ratePercText = "" + ratePerc;

        if (ratePercText.endsWith(".0")) {
            ratePercText = ratePercText.replace(".0", "");
        } else if (ratePercText.length() > 4) {
            ratePercText = ratePercText.substring(0, 4);
        }

        String text =  ratePercText + "%";
        textView.setText(text);
    }

}
