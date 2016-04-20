package edu.usc.cssl.tacit.crawlers.stackexchange.services.types;

import com.google.gson.annotations.SerializedName;

public class User extends ShallowUser {

    @SerializedName("account_id")
	public int id;
    public String about_me;
    public int age;
    public int answer_count;
    public Long creation_date;
    public int down_vote_count;
    public boolean is_employee;
    public Long last_access_date;
    public Long last_modified_date;
    public String location;
    public int question_count;
    public int reputation_change_day;
    public int reputation_change_month;
    public int reputation_change_quarter;
    public int reputation_change_week;
    public int reputation_change_year;
    public Long timed_penalty_date;
    public int upvote_count;
    public int view_count;
    public String website_url;

}
