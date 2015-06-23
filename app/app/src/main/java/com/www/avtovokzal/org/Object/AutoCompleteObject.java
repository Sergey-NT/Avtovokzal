package com.www.avtovokzal.org.Object;

public class AutoCompleteObject {

    public String objectName;
    public long objectSum;
    public long objectCode;

    public AutoCompleteObject(String objectName, long objectSum, long objectCode) {
        this.objectName = objectName;
        this.objectSum = objectSum;
        this.objectCode = objectCode;
    }

    public String getObjectName() {
        return objectName;
    }

    @Override
    public String toString() {
        return getObjectName();
    }
}
