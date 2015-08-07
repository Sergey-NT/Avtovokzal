package com.www.avtovokzal.org.Object;

public class GgmObject {

    public String time;
    public String number;
    public String name;
    public String timeArrival;
    public String countBus;
    public String price;

    public GgmObject(String time, String number, String name, String timeArrival, String countBus, String price) {
        this.time = time;
        this.number = number;
        this.name = name;
        this.timeArrival = timeArrival;
        this.countBus = countBus;
        this.price = price;
    }

    public String getTime() {
        return time;
    }

    public String getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public String getTimeArrival() {
        return timeArrival;
    }

    public String getCountBus() {
        return countBus;
    }

    public String getPrice() {
        return price;
    }
}
