package com.cb.SmartGroups.utils.cards.interactivity.studentcards;

import java.util.ArrayList;

public class GroupsInteractivityCardsContainer {

    private String name;
    private ArrayList<InteractivityCard> interactivityCardsList;

    public GroupsInteractivityCardsContainer(String name, ArrayList<InteractivityCard> interactivityCardsList) {
        this.name = name;
        this.interactivityCardsList = interactivityCardsList;
    }

    public String getName() {
        return name;
    }

    public ArrayList<InteractivityCard> getInteractivityCardsList() {
        return interactivityCardsList;
    }

}
