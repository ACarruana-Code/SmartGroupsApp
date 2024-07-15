package com.cb.SmartGroups.utils.firesoredatamodels;

import java.util.ArrayList;

public class CollectiveGroupDocument {

    private String name;
    private ArrayList<String> allParticipantsIDs;
    private ArrayList<Group> groups;
    private String spokerID;
    private String spokerName;
    private String pattern;

    public CollectiveGroupDocument(){

    }

    public CollectiveGroupDocument(String name, ArrayList<String> allParticipantsIDs, ArrayList<Group> groups, String spokerID, String spokerName, String pattern){
        this.name = name;
        this.allParticipantsIDs = allParticipantsIDs;
        this.groups = groups;
        this.spokerID = spokerID;
        this.spokerName = spokerName;
        this.pattern = pattern;
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getAllParticipantsIDs() {
        return allParticipantsIDs;
    }

    public ArrayList<Group> getGroups() {
        return groups;
    }

    public String getSpokerID() {
        return spokerID;
    }

    public void setSpokerID(String spokerID) {
        this.spokerID = spokerID;
    }

    public String getSpokerName() {
        return spokerName;
    }

    public void setSpokerName(String spokerName) {
        this.spokerName = spokerName;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
}
