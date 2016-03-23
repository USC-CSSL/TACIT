package edu.usc.cssl.tacit.crawlers.stackexchange.services.types;

import com.google.gson.annotations.SerializedName;

public class ShallowUser {

    protected int accept_rate;
    protected BadgeCount badge_counts;
    protected String display_name;
    protected String link;
    protected String profile_image;
    protected int reputation;
    protected int user_id;
    protected Type user_type;

    public enum Type {
        @SerializedName("unregistered")     UNREGISTERED,
        @SerializedName("registered")       REGISTERED,
        @SerializedName("moderator")        MODERATOR,
        @SerializedName("does_not_exist")   DOES_NOT_EXIST,
    }

}
