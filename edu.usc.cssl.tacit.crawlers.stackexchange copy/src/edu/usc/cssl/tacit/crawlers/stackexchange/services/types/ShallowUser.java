package edu.usc.cssl.tacit.crawlers.stackexchange.services.types;

import com.google.gson.annotations.SerializedName;

public class ShallowUser {

    protected int acceptRate;
    protected BadgeCount badgeCounts;
    protected String displayName;
    protected String link;
    protected String profileImage;
    protected int reputation;
    protected int userId;
    protected Type userType;

    public enum Type {
        @SerializedName("unregistered")     UNREGISTERED,
        @SerializedName("registered")       REGISTERED,
        @SerializedName("moderator")        MODERATOR,
        @SerializedName("does_not_exist")   DOES_NOT_EXIST,
    }

}
