package edu.usc.cssl.tacit.crawlers.stackexchange.services.types;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Filter {

    @SerializedName("filter_type")
    protected Type type;

    @SerializedName("filter")
    protected String value;

    @SerializedName("included_fields")
    protected List<String> fields;

    public enum Type {
        @SerializedName("safe")     SAFE,
        @SerializedName("unsafe")   UNSAFE,
        @SerializedName("invalid")  INVALID,
    }


    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

}
