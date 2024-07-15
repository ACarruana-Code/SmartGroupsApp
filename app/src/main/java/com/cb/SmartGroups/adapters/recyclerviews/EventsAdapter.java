package com.cb.SmartGroups.adapters.recyclerviews;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cb.SmartGroups.R;
import com.cb.SmartGroups.utils.cards.EventCard;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventsCardViewHolder> {

    private ArrayList<EventCard> cardsList;
    private FirebaseAuth fAuth;


    public EventsAdapter(ArrayList<EventCard> cardsList) {
        this.cardsList = cardsList;
        fAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public EventsCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.utils_cards_eventcard, parent, false);
        return new EventsCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventsCardViewHolder holder, int position) {
        EventCard eventCard = cardsList.get(position);

        holder.eventTitle.setText(eventCard.getEventName());
        holder.eventMessage.setText(eventCard.getEventMessage());
        String eventPlace = "Venue of the event: " + eventCard.getEventPlace();

        SpannableStringBuilder str = new SpannableStringBuilder(eventPlace);
        str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, "Venue of the event: ".length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        holder.eventPlace.setText(str);

        String eventDate = "Date of the event: " + eventCard.getEventDate();

        SpannableStringBuilder str1 = new SpannableStringBuilder(eventDate);
        str1.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, "Date of the event: ".length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        holder.eventDate.setText(str1);

        if (!eventCard.getSenderID().equals(fAuth.getUid())) {
            holder.deleteEvent.setVisibility(View.GONE);
        }

        holder.deleteEvent.setOnClickListener(view -> {
            eventCard.getDocumentSnapshot().getReference().delete();
        });

    }

    @Override
    public int getItemCount() {
        return cardsList.size();
    }


    public static class EventsCardViewHolder extends RecyclerView.ViewHolder {

        TextView eventTitle;
        TextView eventMessage;
        TextView eventPlace;
        TextView eventDate;
        MaterialButton deleteEvent;

        public EventsCardViewHolder(@NonNull View itemView) {
            super(itemView);
            eventTitle = itemView.findViewById(R.id.eventTitle);
            eventMessage = itemView.findViewById(R.id.eventMessage);
            eventPlace = itemView.findViewById(R.id.eventPlace);
            eventDate = itemView.findViewById(R.id.eventDate);
            deleteEvent = itemView.findViewById(R.id.deleteEvent);
        }
    }

}