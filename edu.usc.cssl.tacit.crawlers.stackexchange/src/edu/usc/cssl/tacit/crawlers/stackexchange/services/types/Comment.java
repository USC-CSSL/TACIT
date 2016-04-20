package edu.usc.cssl.tacit.crawlers.stackexchange.services.types;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Comment {

    protected String body;
    protected String body_markdown;
    protected boolean can_flag;
    protected int comment_id;
    protected Long creation_date;
    protected boolean edited;
    protected String link;
    protected ShallowUser owner;
    protected int post_id;
    protected Type post_type;
    protected ShallowUser reply_to_user;
    protected int score;
    protected boolean upvoted;
    


    public String getBody() {
		return body;
	}



	public void setBody(String body) {
		this.body = body;
	}



	public String getBody_markdown() {
		return body_markdown;
	}



	public void setBody_markdown(String body_markdown) {
		this.body_markdown = body_markdown;
	}



	public boolean isCan_flag() {
		return can_flag;
	}



	public void setCan_flag(boolean can_flag) {
		this.can_flag = can_flag;
	}



	public int getComment_id() {
		return comment_id;
	}



	public void setComment_id(int comment_id) {
		this.comment_id = comment_id;
	}



	public Long getCreation_date() {
		return creation_date;
	}



	public void setCreation_date(Long creation_date) {
		this.creation_date = creation_date;
	}



	public boolean isEdited() {
		return edited;
	}



	public void setEdited(boolean edited) {
		this.edited = edited;
	}



	public String getLink() {
		return link;
	}



	public void setLink(String link) {
		this.link = link;
	}



	public ShallowUser getOwner() {
		return owner;
	}



	public void setOwner(ShallowUser owner) {
		this.owner = owner;
	}



	public int getPost_id() {
		return post_id;
	}



	public void setPost_id(int post_id) {
		this.post_id = post_id;
	}



	public Type getPost_type() {
		return post_type;
	}



	public void setPost_type(Type post_type) {
		this.post_type = post_type;
	}



	public ShallowUser getReply_to_user() {
		return reply_to_user;
	}



	public void setReply_to_user(ShallowUser reply_to_user) {
		this.reply_to_user = reply_to_user;
	}



	public int getScore() {
		return score;
	}



	public void setScore(int score) {
		this.score = score;
	}



	public boolean isUpvoted() {
		return upvoted;
	}



	public void setUpvoted(boolean upvoted) {
		this.upvoted = upvoted;
	}



	public enum Type {
        @SerializedName("question") QUESTION,
        @SerializedName("answer")   ANSWER,
    }

}
