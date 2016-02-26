package edu.usc.cssl.tacit.crawlers.stackexchange.services.types;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Comment {

    protected String body;
    protected String bodyMarkdown;
    protected boolean canFlag;
    protected int commentId;
    protected Date creationDate;
    protected boolean edited;
    protected String link;
    protected ShallowUser owner;
    protected int postId;
    protected Type postType;
    protected ShallowUser replyToUser;
    protected int score;
    protected boolean upvoted;

    public enum Type {
        @SerializedName("question") QUESTION,
        @SerializedName("answer")   ANSWER,
    }

}
