package edu.usc.cssl.tacit.crawlers.stackexchange.services.types;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class User extends ShallowUser {

    @SerializedName("account_id")
	public int id;

    protected String aboutMe;
    protected int age;
    protected int answer_ount;
    protected Long creation_date;
    protected int down_vote_count;
    protected boolean is_employee;
    protected Long last_access_date;
    protected Long last_modified_date;
    protected String location;
    protected int question_count;
    protected int reputation_change_day;
    protected int reputation_change_month;
    protected int reputation_change_quarter;
    protected int reputation_change_week;
    protected int reputation_change_year;
    protected Long timed_penalty_date;
    protected int upvote_count;
    protected int view_count;
    protected String website_url;

}
