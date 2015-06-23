package com.www.avtovokzal.org.Object;

public class RouteObjectInfoArrival {

    public String timeOtpr;
    public String nameStation;
    public String noteStation;
    public String code;

    public RouteObjectInfoArrival(String timeOtpr, String nameStation, String noteStation, String code) {
        this.timeOtpr = timeOtpr;
        this.nameStation = nameStation;
        this.noteStation = noteStation;
        this.code = code;
    }

    public String getTimeOtpr() {
        return timeOtpr;
    }

    public String getNameStation() {
        return nameStation;
    }

    public String getNoteStation() {
        return noteStation;
    }

    public String getCode() {
        return code;
    }
}
