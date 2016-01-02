package com.github.sdmcraft.meetme;

public class Extension {

    private final String number;
    private final String callerId;

    public Extension(String number, String callerId) {
        super();
        this.number = number;
        this.callerId = callerId;
    }

    /**
     * @return the number
     */
    public String getNumber() {
        return number;
    }

    public String getCallerId() {
        return callerId;
    }

}
