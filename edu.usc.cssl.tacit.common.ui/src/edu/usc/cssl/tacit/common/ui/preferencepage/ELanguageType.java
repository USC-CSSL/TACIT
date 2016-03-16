package edu.usc.cssl.tacit.common.ui.preferencepage;

public enum ELanguageType {
	
	//AUTODETECT,
	EN,
	DE,
	FR,
	IT,
	DA,
	NL,
	FI,
	HU,
	NO,
	TR,
	LATIN
	;
	private String text;

	ELanguageType(String langName){
		this.setText(langName);
	}
	ELanguageType(){
		this.setText("");
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	

}
