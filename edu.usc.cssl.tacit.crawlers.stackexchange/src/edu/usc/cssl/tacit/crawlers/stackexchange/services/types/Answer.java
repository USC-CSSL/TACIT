package edu.usc.cssl.tacit.crawlers.stackexchange.services.types;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Answer {

    @SerializedName("answer_id")
    protected int id;

    protected boolean accepted;
    protected int awarded_bounty_amount;
    protected List<ShallowUser> awarded_bounty_users;
    protected String body;
    protected String body_markdown;
    protected boolean can_flag;
    protected int comment_count;
    protected List<Comment> comments;
    protected Long community_owned_date;
    protected Long creation_date;
    protected int down_vote_count;
    protected boolean downvoted;
    protected boolean is_accepted;
    protected Long last_activity_date;
    protected Long last_edit_date;
    protected ShallowUser last_editor;
    protected String link;
    protected Long locked_date;
    protected ShallowUser owner;
    protected int question_id;
    protected int score;
    protected String share_link;
    protected List<String> tags;
    protected String title;
    protected int up_vote_count;
    protected boolean upvoted;
    
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public boolean isAccepted() {
		return accepted;
	}
	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}
	public int getAwarded_bounty_amount() {
		return awarded_bounty_amount;
	}
	public void setAwarded_bounty_amount(int awarded_bounty_amount) {
		this.awarded_bounty_amount = awarded_bounty_amount;
	}
	public List<ShallowUser> getAwarded_bounty_users() {
		return awarded_bounty_users;
	}
	public void setAwarded_bounty_users(List<ShallowUser> awarded_bounty_users) {
		this.awarded_bounty_users = awarded_bounty_users;
	}
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
	public int getComment_count() {
		return comment_count;
	}
	public void setComment_count(int comment_count) {
		this.comment_count = comment_count;
	}
	public List<Comment> getComments() {
		return comments;
	}
	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}
	public Long getCommunity_owned_date() {
		return community_owned_date;
	}
	public void setCommunity_owned_date(Long community_owned_date) {
		this.community_owned_date = community_owned_date;
	}
	public Long getCreation_date() {
		return creation_date;
	}
	public void setCreation_date(Long creation_date) {
		this.creation_date = creation_date;
	}
	public int getDown_vote_count() {
		return down_vote_count;
	}
	public void setDown_vote_count(int down_vote_count) {
		this.down_vote_count = down_vote_count;
	}
	public boolean isDownvoted() {
		return downvoted;
	}
	public void setDownvoted(boolean downvoted) {
		this.downvoted = downvoted;
	}
	public boolean isIs_accepted() {
		return is_accepted;
	}
	public void setIs_accepted(boolean is_accepted) {
		this.is_accepted = is_accepted;
	}
	public Long getLast_activity_date() {
		return last_activity_date;
	}
	public void setLast_activity_date(Long last_activity_date) {
		this.last_activity_date = last_activity_date;
	}
	public Long getLast_edit_date() {
		return last_edit_date;
	}
	public void setLast_edit_date(Long last_edit_date) {
		this.last_edit_date = last_edit_date;
	}
	public ShallowUser getLast_editor() {
		return last_editor;
	}
	public void setLast_editor(ShallowUser last_editor) {
		this.last_editor = last_editor;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public Long getLocked_date() {
		return locked_date;
	}
	public void setLocked_date(Long locked_date) {
		this.locked_date = locked_date;
	}
	public ShallowUser getOwner() {
		return owner;
	}
	public void setOwner(ShallowUser owner) {
		this.owner = owner;
	}
	public int getQuestion_id() {
		return question_id;
	}
	public void setQuestion_id(int question_id) {
		this.question_id = question_id;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	public String getShare_link() {
		return share_link;
	}
	public void setShare_link(String share_link) {
		this.share_link = share_link;
	}
	public List<String> getTags() {
		return tags;
	}
	public void setTags(List<String> tags) {
		this.tags = tags;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public int getUp_vote_count() {
		return up_vote_count;
	}
	public void setUp_vote_count(int up_vote_count) {
		this.up_vote_count = up_vote_count;
	}
	public boolean isUpvoted() {
		return upvoted;
	}
	public void setUpvoted(boolean upvoted) {
		this.upvoted = upvoted;
	}

}
