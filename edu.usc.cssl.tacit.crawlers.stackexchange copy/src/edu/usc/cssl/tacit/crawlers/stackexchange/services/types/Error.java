package edu.usc.cssl.tacit.crawlers.stackexchange.services.types;

import com.google.gson.annotations.SerializedName;

public class Error {

    @SerializedName("error_id")
    protected int id;

    @SerializedName("error_name")
    protected String name;

    @SerializedName("error_message")
    protected String message;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
