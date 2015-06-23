package com.www.avtovokzal.org.Object;

public class RouteObjectResult {

    public String timeOtpr;
    public String numberMarsh;
    public String numberMarshToSend;
    public String marshName;
    public String nameBus;
    public String countBus;
    public String freeBus;
    public String priceBus;
    public String baggageBus;
    public String timePrib;
    public int cancelBus;

    public RouteObjectResult(String timeOtpr, String numberMarsh, String numberMarshToSend, String marshName, String nameBus, String countBus, String freeBus, String priceBus, String baggageBus, String timePrib, int cancelBus) {
        this.timeOtpr = timeOtpr;
        this.numberMarsh = numberMarsh;
        this.numberMarshToSend = numberMarshToSend;
        this.marshName = marshName;
        this.nameBus = nameBus;
        this.countBus = countBus;
        this.freeBus = freeBus;
        this.priceBus = priceBus;
        this.baggageBus = baggageBus;
        this.timePrib = timePrib;
        this.cancelBus = cancelBus;
    }

    public String getNumberMarshToSend() {
        return numberMarshToSend;
    }

    public int getCancelBus() {
        return cancelBus;
    }

    public String getTimePrib() {
        return timePrib;
    }

    public String getNumberMarsh() {
        return numberMarsh;
    }

    public String getTimeOtpr() {
        return timeOtpr;
    }

    public String getMarshName() {
        return marshName;
    }

    public String getNameBus() {
        return nameBus;
    }

    public String getCountBus() {
        return countBus;
    }

    public String getFreeBus() {
        return freeBus;
    }

    public String getPriceBus() {
        return priceBus;
    }

    public String getBaggageBus() {
        return baggageBus;
    }
}
