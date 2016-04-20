package edu.usc.cssl.tacit.crawlers.stackexchange.services.types;

public class QuestionItem {
	public Question[] getItems() {
		return items;
	}
	public void setItems(Question[] items) {
		this.items = items;
	}
	public Question[] items;
	public boolean has_more;
	int quota_max;
	int quota_remaining;
}
