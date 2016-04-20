package edu.usc.cssl.tacit.crawlers.stackexchange.services.types;

import com.google.gson.annotations.SerializedName;

public class ShallowUser {

	public int accept_rate;
    public BadgeCount badge_counts;
    public String display_name;
    public String link;
    public String profile_image;
    public int reputation;
    public int user_id;
    public Type user_type;

    public enum Type {
        @SerializedName("unregistered")     UNREGISTERED,
        @SerializedName("registered")       REGISTERED,
        @SerializedName("moderator")        MODERATOR,
        @SerializedName("does_not_exist")   DOES_NOT_EXIST,
    }

}
