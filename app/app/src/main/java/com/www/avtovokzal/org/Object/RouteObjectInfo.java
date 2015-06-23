package com.www.avtovokzal.org.Object;

public class RouteObjectInfo {

    public String timePrib;
    public String timeWay;
    public String nameStation;
    public String noteStation;
    public long codeStation;
    public String priceBus;
    public String baggageBus;
    public String distanceData;

    public RouteObjectInfo(String timePrib, String timeWay, String nameStation, String noteStation, long codeStation, String priceBus, String baggageBus, String distanceData) {
        this.timePrib = timePrib;
        this.timeWay = timeWay;
        this.nameStation = nameStation;
        this.noteStation = noteStation;
        this.codeStation = codeStation;
        this.priceBus = priceBus;
        this.baggageBus = baggageBus;
        this.distanceData = distanceData;
    }

    public long getCodeStation() {
        return codeStation;
    }

    public String getDistanceData() {
        return distanceData;
    }

    public String getTimePrib() {
        return timePrib;
    }

    public String getTimeWay() {
        return timeWay;
    }

    public String getNameStation() {
        return nameStation;
    }

    public String getNoteStation() {
        return noteStation;
    }

    public String getPriceBus() {
        return priceBus;
    }

    public String getBaggageBus() {
        return baggageBus;
    }
}
