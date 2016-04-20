package edu.usc.cssl.tacit.crawlers.stackexchange.services.types;

public class Question {
	protected boolean is_answered;
    protected int view_count;
    protected int answer_count;
    protected int score;
    protected Long last_activity_date;
    protected Long creation_date;
    protected int question_id;
    protected String link;
    protected String body;
    protected String title;
    protected ShallowUser owner;
    protected String []tags;
    
    
	public boolean isIs_answered() {
		return is_answered;
	}
	public void setIs_answered(boolean is_answered) {
		this.is_answered = is_answered;
	}
	public int getView_count() {
		return view_count;
	}
	public void setView_count(int view_count) {
		this.view_count = view_count;
	}
	public int getAnswer_count() {
		return answer_count;
	}
	public void setAnswer_count(int answer_count) {
		this.answer_count = answer_count;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	public Long getLast_activity_date() {
		return last_activity_date;
	}
	public void setLast_activity_date(Long last_activity_date) {
		this.last_activity_date = last_activity_date;
	}
	public Long getCreation_date() {
		return creation_date;
	}
	public void setCreation_date(Long creation_date) {
		this.creation_date = creation_date;
	}
	public int getQuestion_id() {
		return question_id;
	}
	public void setQuestion_id(int question_id) {
		this.question_id = question_id;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public ShallowUser getOwner() {
		return owner;
	}
	public void setOwner(ShallowUser owner) {
		this.owner = owner;
	}
	public String[] getTags() {
		return tags;
	}
	public void setTags(String[] tags) {
		this.tags = tags;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
}
