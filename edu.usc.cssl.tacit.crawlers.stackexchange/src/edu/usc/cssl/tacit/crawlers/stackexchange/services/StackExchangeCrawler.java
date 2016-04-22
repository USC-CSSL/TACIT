package edu.usc.cssl.tacit.crawlers.stackexchange.services;

import java.io.File;
import java.io.IOException;

import org.jsoup.Jsoup;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import edu.usc.cssl.tacit.common.ui.CommonUiActivator;
import edu.usc.cssl.tacit.crawlers.stackexchange.services.types.Answer;
import edu.usc.cssl.tacit.crawlers.stackexchange.services.types.AnswerItem;
import edu.usc.cssl.tacit.crawlers.stackexchange.services.types.Comment;
import edu.usc.cssl.tacit.crawlers.stackexchange.services.types.CommentItem;
import edu.usc.cssl.tacit.crawlers.stackexchange.services.types.Item;
import edu.usc.cssl.tacit.crawlers.stackexchange.services.types.Question;
import edu.usc.cssl.tacit.crawlers.stackexchange.services.types.QuestionItem;
import edu.usc.cssl.tacit.crawlers.stackexchange.services.types.User;
import retrofit2.Call;

public class StackExchangeCrawler {

	static StackExchangeCrawler cr, sr;
	StackExchangeSite s;
	String key = "6Xk6jRz2SrEBLRBnOhhSIw((";
	JsonGenerator jsonGenerator;
	JsonFactory jsonfactory;
	private boolean[] filter;

	public StackExchangeCrawler() {
		String k = CommonUiActivator.getDefault().getPreferenceStore().getString("ckey");
		if(k!=null && !k.equals("")){
			key = k;
		}
	}

	public void setDir(String fileName) {
		// TODO Auto-generated constructor stub
		// Instantiate JSON writer
		String output = fileName + File.separator + "stackexchange.json";
		File streamFile = new File(output);
		jsonfactory = new JsonFactory();
		try {
			jsonGenerator = jsonfactory.createGenerator(streamFile, JsonEncoding.UTF8);
			jsonGenerator.useDefaultPrettyPrinter();
			jsonGenerator.writeStartArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public StackExchangeSite stackoverflow(StackExchangeApi api, String site) {
		StackExchangeSite siteService = api.getSiteService(StackExchangeSite.STACK_OVERFLOW);
		return siteService;
	}

	public void search(String tags, int pages, boolean question, boolean answer, boolean comment, String corpusName,
			StackExchangeSite sc, String site, boolean[] jsonFilter) {
		filter = jsonFilter;
		if (tags.equals("") || tags == null) {
			if (question) {
				returnQuestions(sc, pages, site);
			}
			if (answer) {
				returnAnswers(sc, pages, site);
			}
			if (comment) {
				returnComments(sc, pages, site);
			}
		} else {
			getTaggedPost(sc, pages, tags, site);
		}
		try {
			jsonGenerator.writeEndArray();
			jsonGenerator.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void search(String tags, int pages, boolean question, boolean answer, boolean comment, String corpusName,
			StackExchangeSite sc, String site, Long from, Long to, boolean[] jsonFilter) {
		filter = jsonFilter;
		if (tags.equals("") || tags == null) {
			if (question) {
				returnQuestions(sc, pages, site, from, to);
			}
			if (answer) {
				returnAnswers(sc, pages, site, from, to);
			}
			if (comment) {
				returnComments(sc, pages, site, from, to);
			}
		} else {
			getTaggedPost(sc, pages, tags, site, from, to);
		}
		try {
			jsonGenerator.writeEndArray();
			jsonGenerator.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// to obtain all users
	public void returnUsers(StackExchangeSite siteService, int pages, String site) {
		try {
			int page = 1;
			while (page <= pages) {
				Call<Item> call = siteService.getUsers(page, key, site);
				Item i = call.execute().body();
				for (User user : i.items) {
					System.out.println(user.age);
				}
				page++;
				if (!i.has_more)
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// to obtain all answers
	public void returnAnswers(StackExchangeSite siteService, int pages, String site) {
		try {
			int page = 1;
			while (page <= pages) {
				Call<AnswerItem> call = siteService.getAnswers(page, key, site);
				AnswerItem i = call.execute().body();

				for (Answer answer : i.items) {
					jsonGenerator.writeStartObject();
					jsonGenerator.writeStringField("answer_id", Integer.toString(answer.getId()));
					if (filter[1])
						jsonGenerator.writeStringField("answer_body", Jsoup.parse(answer.getBody()).text());
					jsonGenerator.writeStringField("question_id", Integer.toString(answer.getQuestion_id()));
					if (filter[0]) {
						jsonGenerator.writeObjectFieldStart("user");
						jsonGenerator.writeStringField("user_id", Integer.toString(answer.getOwner().user_id));
						jsonGenerator.writeStringField("username", answer.getOwner().display_name);
						jsonGenerator.writeStringField("user_reputation",
								Integer.toString(answer.getOwner().reputation));
						jsonGenerator.writeStringField("user_type", answer.getOwner().user_type.toString());
						jsonGenerator.writeEndObject();
					}

					jsonGenerator.writeEndObject();
					System.out.println(Jsoup.parse(answer.getBody()).text());
					System.out.println("----------------------------");

				}
				page++;
				if (!i.has_more)
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// to obtain all answers by Date
	public void returnAnswers(StackExchangeSite siteService, int pages, String site, Long from, Long to) {
		try {
			int page = 1;
			while (page <= pages) {
				Call<AnswerItem> call = siteService.getAnswersByDate(page, key, site, from, to);
				AnswerItem i = call.execute().body();

				for (Answer answer : i.items) {
					jsonGenerator.writeStartObject();
					jsonGenerator.writeStringField("answer_id", Integer.toString(answer.getId()));
					if (filter[1])
						jsonGenerator.writeStringField("answer_body", Jsoup.parse(answer.getBody()).text());
					jsonGenerator.writeStringField("question_id", Integer.toString(answer.getQuestion_id()));
					if (filter[0]) {
						jsonGenerator.writeObjectFieldStart("user");
						jsonGenerator.writeStringField("user_id", Integer.toString(answer.getOwner().user_id));
						jsonGenerator.writeStringField("username", answer.getOwner().display_name);
						jsonGenerator.writeStringField("user_reputation",
								Integer.toString(answer.getOwner().reputation));
						jsonGenerator.writeStringField("user_type", answer.getOwner().user_type.toString());
						jsonGenerator.writeEndObject();
					}

					jsonGenerator.writeEndObject();
					System.out.println(Jsoup.parse(answer.getBody()).text());
					System.out.println("----------------------------");

				}
				page++;
				if (!i.has_more)
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// to obtain answer by Id
	public void getAnswerById(StackExchangeSite siteService, int answerId, String site) {
		try {
			Call<AnswerItem> call = siteService.getAnswerById(answerId, key, site);
			AnswerItem i = call.execute().body();
			for (Answer answer : i.items) {
				System.out.println(Jsoup.parse(answer.getBody()).text());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// to obtain all questions
	public void returnQuestions(StackExchangeSite siteService, int pages, String site) {
		try {
			int page = 1;
			while (page <= pages) {
				Call<QuestionItem> call = siteService.getQuestions(page, key, site);
				QuestionItem i = call.execute().body();

				for (Question question : i.items) {
					jsonGenerator.writeStartObject();
					jsonGenerator.writeStringField("question_id", Integer.toString(question.getQuestion_id()));
					if (filter[2])
						jsonGenerator.writeStringField("question_title", Jsoup.parse(question.getTitle()).text());
					if (filter[3])
						jsonGenerator.writeStringField("question_body", Jsoup.parse(question.getBody()).text());
					if (filter[5])
						jsonGenerator.writeStringField("is_answered", question.isIs_answered() + "");
					if (filter[4]) {
						jsonGenerator.writeObjectFieldStart("user");
						jsonGenerator.writeStringField("user_id", Integer.toString(question.getOwner().user_id));
						jsonGenerator.writeStringField("username", question.getOwner().display_name);
						jsonGenerator.writeStringField("user_reputation",
								Integer.toString(question.getOwner().reputation));
						jsonGenerator.writeStringField("user_type", question.getOwner().user_type.toString());
						jsonGenerator.writeEndObject();
					}
					jsonGenerator.writeEndObject();
					System.out.println(question.getTitle());
					if (question.getBody() != null)
						System.out.println(Jsoup.parse(question.getBody()).text());
					System.out.println("----------------------------");
				}
				page++;
				if (!i.has_more)
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// to obtain all questions in date range
	public void returnQuestions(StackExchangeSite siteService, int pages, String site, Long fromDate, Long toDate) {
		try {
			int page = 1;
			while (page <= pages) {
				Call<QuestionItem> call = siteService.getQuestionsByDate(page, key, site, fromDate, toDate);
				QuestionItem i = call.execute().body();

				for (Question question : i.items) {
					jsonGenerator.writeStartObject();
					jsonGenerator.writeStringField("question_id", Integer.toString(question.getQuestion_id()));
					if (filter[2])
						jsonGenerator.writeStringField("question_title", Jsoup.parse(question.getTitle()).text());
					if (filter[3])
						jsonGenerator.writeStringField("question_body", Jsoup.parse(question.getBody()).text());
					if (filter[5])
						jsonGenerator.writeStringField("is_answered", question.isIs_answered() + "");
					if (filter[4]) {
						jsonGenerator.writeObjectFieldStart("user");
						jsonGenerator.writeStringField("user_id", Integer.toString(question.getOwner().user_id));
						jsonGenerator.writeStringField("username", question.getOwner().display_name);
						jsonGenerator.writeStringField("user_reputation",
								Integer.toString(question.getOwner().reputation));
						jsonGenerator.writeStringField("user_type", question.getOwner().user_type.toString());
						jsonGenerator.writeEndObject();
					}

					jsonGenerator.writeEndObject();
					System.out.println(question.getTitle());
					if (question.getBody() != null)
						System.out.println(Jsoup.parse(question.getBody()).text());
					System.out.println("----------------------------");
				}
				page++;
				if (!i.has_more)
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// to obtain questions by id
	public void getQuestionById(StackExchangeSite siteService, int questionId, String site) {
		try {
			Call<QuestionItem> call = siteService.getQuestionsById(questionId, key, site);
			QuestionItem i = call.execute().body();
			for (Question question : i.items) {
				System.out.println(question.getTitle());
				if (question.getBody() != null)
					System.out.println(Jsoup.parse(question.getBody()).text());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// to obtain all comments
	public void returnComments(StackExchangeSite siteService, int pages, String site) {
		try {
			int page = 1;
			while (page <= pages) {
				Call<CommentItem> call = siteService.getComments(page, key, site);
				CommentItem i = call.execute().body();

				for (Comment comments : i.items) {
					jsonGenerator.writeStartObject();
					jsonGenerator.writeStringField("comment_id", Integer.toString(comments.getComment_id()));
					if (filter[6])
						jsonGenerator.writeStringField("comment_body", Jsoup.parse(comments.getBody()).text());
					if (filter[7]) {
						jsonGenerator.writeObjectFieldStart("user");
						jsonGenerator.writeStringField("user_id", Integer.toString(comments.getOwner().user_id));
						jsonGenerator.writeStringField("username", comments.getOwner().display_name);
						jsonGenerator.writeStringField("user_reputation",
								Integer.toString(comments.getOwner().reputation));
						jsonGenerator.writeStringField("user_type", comments.getOwner().user_type.toString());
						jsonGenerator.writeEndObject();
					}
					jsonGenerator.writeEndObject();
				}
				page++;
				if (!i.has_more)
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// to obtain all comments by date
	public void returnComments(StackExchangeSite siteService, int pages, String site, Long from, Long to) {
		try {
			int page = 1;
			while (page <= pages) {
				Call<CommentItem> call = siteService.getCommentsByDate(page, key, site, from, to);
				CommentItem i = call.execute().body();

				for (Comment comments : i.items) {
					jsonGenerator.writeStartObject();
					jsonGenerator.writeStringField("comment_id", Integer.toString(comments.getComment_id()));
					if (filter[6])
						jsonGenerator.writeStringField("comment_body", Jsoup.parse(comments.getBody()).text());
					if (filter[7]) {
						jsonGenerator.writeObjectFieldStart("user");
						jsonGenerator.writeStringField("user_id", Integer.toString(comments.getOwner().user_id));
						jsonGenerator.writeStringField("username", comments.getOwner().display_name);
						jsonGenerator.writeStringField("user_reputation",
								Integer.toString(comments.getOwner().reputation));
						jsonGenerator.writeStringField("user_type", comments.getOwner().user_type.toString());
						jsonGenerator.writeEndObject();
					}
					jsonGenerator.writeEndObject();
				}
				page++;
				if (!i.has_more)
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// to obtain comments by Id
	public void getCommentById(StackExchangeSite siteService, int commentId, String site) {
		try {
			Call<CommentItem> call = siteService.getCommentById(commentId, key, site);
			CommentItem i = call.execute().body();
			for (Comment comment : i.items) {
				if (comment.getBody() != null)
					System.out.println(Jsoup.parse(comment.getBody()).text());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void getTaggedPost(StackExchangeSite siteService, int pages, String tag, String site) {
		try {
			int page = 1;
			while (page <= pages) {
				Call<QuestionItem> call = siteService.getSearchTags(page, tag, key, site);
				System.out.println(call);
				QuestionItem i = call.execute().body();
				System.out.println(i);
				for (Question search : i.items) {
					int questionId = search.getQuestion_id();
					jsonGenerator.writeStartObject();
					jsonGenerator.writeObjectFieldStart("question");
					jsonGenerator.writeStringField("question_id", Integer.toString(search.getQuestion_id()));
					if (filter[2])
					jsonGenerator.writeStringField("question_title", Jsoup.parse(search.getTitle()).text());
					if (filter[3])
					jsonGenerator.writeStringField("question_body", Jsoup.parse(search.getBody()).text());
					if (filter[5])
					jsonGenerator.writeStringField("is_answered", search.isIs_answered() + "");
					// User details
					if (filter[4]) {
						jsonGenerator.writeObjectFieldStart("user");
						jsonGenerator.writeStringField("user_id", Integer.toString(search.getOwner().user_id));
						jsonGenerator.writeStringField("username", search.getOwner().display_name);
						jsonGenerator.writeStringField("user_reputation", Integer.toString(search.getOwner().reputation));
						jsonGenerator.writeStringField("user_type", search.getOwner().user_type.toString());
						jsonGenerator.writeEndObject();
					}
					// get Comments for question
					System.out.println("Comments++++++++++++++++");
					getCommentsByQuestionId(siteService, questionId, site);
					jsonGenerator.writeEndObject(); // End of question
					System.out.println("Answers++++++++++++++++");
					getAnswersByQuestionId(siteService, questionId, site);
					jsonGenerator.writeEndObject(); // End object
				}
				page++;
				if (!i.has_more)
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void getTaggedPost(StackExchangeSite siteService, int pages, String tag, String site, Long from, Long to) {
		try {
			int page = 1;
			while (page <= pages) {
				Call<QuestionItem> call = siteService.getSearchTagsByDate(page, tag, key, site, from, to);
				System.out.println(call);
				QuestionItem i = call.execute().body();
				System.out.println(i);
				for (Question search : i.items) {
					int questionId = search.getQuestion_id();
					jsonGenerator.writeStartObject();
					jsonGenerator.writeObjectFieldStart("Question");
					jsonGenerator.writeStringField("question_id", Integer.toString(search.getQuestion_id()));
					if (filter[2])
						jsonGenerator.writeStringField("question_title", Jsoup.parse(search.getTitle()).text());
					if (filter[3])
						jsonGenerator.writeStringField("question_body", Jsoup.parse(search.getBody()).text());
					if (filter[5])
						jsonGenerator.writeStringField("isAnswered", search.isIs_answered() + "");
					// User details
					if (filter[4]) {
						jsonGenerator.writeObjectFieldStart("user");
						jsonGenerator.writeStringField("user_id", Integer.toString(search.getOwner().user_id));
						jsonGenerator.writeStringField("username", search.getOwner().display_name);
						jsonGenerator.writeStringField("user_reputation",Integer.toString(search.getOwner().reputation));
						jsonGenerator.writeStringField("user_type", search.getOwner().user_type.toString());
						jsonGenerator.writeEndObject();
					}
					// get Comments for question
					System.out.println("Comments++++++++++++++++");
					getCommentsByQuestionId(siteService, questionId, site);
					jsonGenerator.writeEndObject(); // End of question
					System.out.println("Answers++++++++++++++++");
					getAnswersByQuestionId(siteService, questionId, site);
					jsonGenerator.writeEndObject(); // End object
				}
				page++;
				if (!i.has_more)
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void getCommentsByQuestionId(StackExchangeSite siteService, int questionId, String site) {
		try {
			while (true) {
				Call<CommentItem> call = siteService.getCommentsbyQuestionId(questionId, key, site);
				CommentItem i = call.execute().body();
				jsonGenerator.writeArrayFieldStart("q_comments");
				for (Comment comments : i.items) {
					jsonGenerator.writeStartObject();
					if(filter[6])
					jsonGenerator.writeStringField("comment_body", Jsoup.parse(comments.getBody()).text());
					if(filter[7]){
						jsonGenerator.writeObjectFieldStart("user");
						jsonGenerator.writeStringField("user_id", Integer.toString(comments.getOwner().user_id));
						jsonGenerator.writeStringField("username", comments.getOwner().display_name);
						jsonGenerator.writeStringField("user_reputation", Integer.toString(comments.getOwner().reputation));
						jsonGenerator.writeStringField("user_type", comments.getOwner().user_type.toString());
						jsonGenerator.writeEndObject();
					}
					jsonGenerator.writeEndObject();
				}
				jsonGenerator.writeEndArray();
				if (!i.has_more)
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void getAnswersByQuestionId(StackExchangeSite siteService, int questionId, String site) {
		try {
			while (true) {
				Call<AnswerItem> call = siteService.getAnswersbyQuestionId(questionId, key, site);
				AnswerItem i = call.execute().body();
				jsonGenerator.writeArrayFieldStart("answers_dets");
				for (Answer answer : i.items) {
					jsonGenerator.writeStartObject();
					jsonGenerator.writeStringField("answer_id", Integer.toString(answer.getQuestion_id()));
					if(filter[1])
					jsonGenerator.writeStringField("answer_body", Jsoup.parse(answer.getBody()).text());
					// user details
					if(filter[0]){
						jsonGenerator.writeObjectFieldStart("user");
						jsonGenerator.writeStringField("user_id", Integer.toString(answer.getOwner().user_id));
						jsonGenerator.writeStringField("username", answer.getOwner().display_name);
						jsonGenerator.writeStringField("user_reputation", Integer.toString(answer.getOwner().reputation));
						jsonGenerator.writeStringField("user_type", answer.getOwner().user_type.toString());
						jsonGenerator.writeEndObject();
					}

					getCommentsByAnswerId(siteService, answer.getId(), site);
					jsonGenerator.writeEndObject();
				}
				jsonGenerator.writeEndArray();
				if (!i.has_more)
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void getCommentsByAnswerId(StackExchangeSite siteService, int answerId, String site) {
		try {
			while (true) {
				Call<CommentItem> call = siteService.getCommentsbyAnswerId(answerId, key, site);
				CommentItem i = call.execute().body();
				jsonGenerator.writeArrayFieldStart("a_comments");
				for (Comment comment : i.items) {
					jsonGenerator.writeStartObject();
					if(filter[6])
					jsonGenerator.writeStringField("comment_body", Jsoup.parse(comment.getBody()).text());
					if(filter[7]){
						jsonGenerator.writeObjectFieldStart("user");
						jsonGenerator.writeStringField("user_id", Integer.toString(comment.getOwner().user_id));
						jsonGenerator.writeStringField("username", comment.getOwner().display_name);
						jsonGenerator.writeStringField("user_reputation", Integer.toString(comment.getOwner().reputation));
						jsonGenerator.writeStringField("user_type", comment.getOwner().user_type.toString());
						jsonGenerator.writeEndObject();
					}
					jsonGenerator.writeEndObject();
				}
				jsonGenerator.writeEndArray();
				if (!i.has_more)
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
