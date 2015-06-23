package com.www.avtovokzal.org.Object;

public class StationsObject {

    public String timeOtpr;
    public String numberMarsh;
    public String numberMarshToSend;
    public String marshName;
    public String nameBus;
    public String countBus;
    public String freeBus;
    public int cancelBus;

    public StationsObject(String timeOtpr, String numberMarsh, String numberMarshToSend, String marshName, String nameBus, String countBus, String freeBus, int cancelBus) {
        this.numberMarsh = numberMarsh;
        this.numberMarshToSend = numberMarshToSend;
        this.timeOtpr = timeOtpr;
        this.marshName = marshName;
        this.nameBus = nameBus;
        this.countBus = countBus;
        this.freeBus = freeBus;
        this.cancelBus = cancelBus;
    }

    public String getNumberMarshToSend() {
        return numberMarshToSend;
    }

    public int getCancelBus() {
        return cancelBus;
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

}
