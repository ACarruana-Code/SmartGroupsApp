package com.cb.SmartGroups.utils.cards.interactivity.studentcards;

import com.cb.SmartGroups.utils.customdatamodels.InteractivityCardType;

public class StandByCard extends InteractivityCard {

    private String spokerName;
    private int cardType;

    public StandByCard(String cardTitle, String studentID, String spokerName, int cardType) {
        super(cardTitle, studentID);
        this.spokerName = spokerName;
        this.cardType = cardType;
    }

    public String getSpokerName() {
        return spokerName;
    }

    public String getCardType() {
        String cardType;
        if (this.cardType == InteractivityCardType.TYPE_INPUTTEXT) {
            cardType = "Text input type activity";
        } else if (this.cardType == InteractivityCardType.TYPE_CHOICES) {
            cardType = "Survey-type activity";
        } else {
            cardType = "Reminder";
        }
        return cardType;
    }
}
