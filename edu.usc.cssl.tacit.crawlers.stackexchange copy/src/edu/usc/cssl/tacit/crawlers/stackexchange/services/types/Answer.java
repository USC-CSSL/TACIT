package edu.usc.cssl.tacit.crawlers.stackexchange.services.types;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

public class Answer {

    @SerializedName("answer_id")
    protected int id;

    protected boolean accepted;
    protected int awardedBountyAmount;
    protected List<ShallowUser> awardedBountyUsers;
    protected String body;
    protected String bodyMarkdown;
    protected boolean canFlag;
    protected int commentCount;
    protected List<Comment> comments;
    protected Date communityOwnedDate;
    protected Date creationDate;
    protected int downVoteCount;
    protected boolean downvoted;
    protected boolean isAccepted;
    protected Date lastActivityDate;
    protected Date lastEditDate;
    protected ShallowUser lastEditor;
    protected String link;
    protected Date lockedDate;
    protected ShallowUser owner;
    protected int questionId;
    protected int score;
    protected String shareLink;
    protected List<String> tags;
    protected String title;
    protected int upVoteCount;
    protected boolean upvoted;

}
