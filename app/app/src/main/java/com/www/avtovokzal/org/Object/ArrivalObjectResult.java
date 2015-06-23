package com.www.avtovokzal.org.Object;

public class ArrivalObjectResult {

    public String timeOtpr;
    public String timePrib;
    public String timeFromStation;
    public String numberMarsh;
    public String nameMarsh;
    public String scheduleMarsh;

    public ArrivalObjectResult(String timeOtpr, String timePrib, String timeFromStation, String numberMarsh, String nameMarsh, String scheduleMarsh) {
        this.timeOtpr = timeOtpr;
        this.timePrib = timePrib;
        this.timeFromStation = timeFromStation;
        this.numberMarsh = numberMarsh;
        this.nameMarsh = nameMarsh;
        this.scheduleMarsh = scheduleMarsh;
    }

    public String getTimeOtpr() {
        return timeOtpr;
    }

    public String getTimePrib() {
        return timePrib;
    }

    public String getTimeFromStation() {
        return timeFromStation;
    }

    public String getNumberMarsh() {
        return numberMarsh;
    }

    public String getNameMarsh() {
        return nameMarsh;
    }

    public String getScheduleMarsh() {
        return scheduleMarsh;
    }
}
