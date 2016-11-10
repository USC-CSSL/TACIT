package edu.usc.cssl.tacit.common.ui.corpusmanagement.internal;

import java.io.File;
import java.util.List;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.CorpusClass;
import edu.usc.cssl.tacit.common.ui.corpusmanagement.services.ManageCorpora;

public class CorpusMangementValidation {
	public static boolean isClassPathValid(String classPathText, String className, ScrolledForm corpusMgmtViewform) {
		if (classPathText.isEmpty()) {
			corpusMgmtViewform.getMessageManager().addMessage("classPath", "Class path must be a valid directory location for class \"" + className + "\"", null, IMessageProvider.ERROR);
			return false;
		}
		File tempFile = new File(classPathText);
		if (!tempFile.exists() || !tempFile.isDirectory()) {
			corpusMgmtViewform.getMessageManager().addMessage("classPath", "Class path must be a valid directory location for class \""+ className + "\"", null, IMessageProvider.ERROR);
			return false;
		} else {
			corpusMgmtViewform.getMessageManager().removeMessage("classPath");
			String message = validatePath(classPathText, className);
			if (null != message) {
				corpusMgmtViewform.getMessageManager().addMessage("classPath", message, null, IMessageProvider.ERROR);
				return false;
			}
		}
		corpusMgmtViewform.getMessageManager().removeMessage("classPath");
		return true;
	}
	
	private static String validatePath(String location, String className) {
		File locationFile = new File(location);
		if (locationFile.canRead()) {
			return null;
		} else {
			return className +"("+location+") does not have read permission";
		}
	}
	
	public static boolean isClassnameValid(String className, ICorpusClass selectedCorpusClass, ScrolledForm corpusMgmtViewform) {
		if(className.isEmpty()) {
			corpusMgmtViewform.getMessageManager().addMessage("classNameEmpty", "Class name cannot be empty", null, IMessageProvider.ERROR);
			return false;
		} else {
			corpusMgmtViewform.getMessageManager().removeMessage("classNameEmpty");
		}
		ICorpus parentCorpus = selectedCorpusClass.getParent();
		if(null == parentCorpus) return true; // newly created corpus
		for(ICorpusClass cc : parentCorpus.getClasses()) {
			if((CorpusClass)cc != selectedCorpusClass) {
				if(cc.getClassName().equals(className)) {
					corpusMgmtViewform.getMessageManager().addMessage("className", "Class name \""+ className +"\"already exists in corpus "+ parentCorpus.getCorpusName(), null, IMessageProvider.ERROR);
					return false;
				}
			}
		}
		corpusMgmtViewform.getMessageManager().removeMessage("className");
		return true;
	}	

	
	public static boolean validateClassData(ICorpusClass cc, ScrolledForm corpusMgmtViewform) {
		return isClassnameValid(cc.getClassName(), cc , corpusMgmtViewform) && isClassPathValid(cc.getClassPath(), cc.getClassName(), corpusMgmtViewform);
	}	
	
	public static boolean validateCorpus(ICorpus selectedCorpus, boolean saveCorpusAction, ScrolledForm corpusMgmtViewform, ManageCorpora corpusManagement) {
		
		if(isCorpusNameValid(selectedCorpus.getCorpusName(), selectedCorpus.getCorpusId(), corpusMgmtViewform, corpusManagement)) {
			List<ICorpusClass> classes = selectedCorpus.getClasses(); // validate the class details as well
			for(ICorpusClass cc : classes) {
				System.out.println(cc.getClassPath());
				if(saveCorpusAction && !CorpusMangementValidation.validateClassData(cc, corpusMgmtViewform)) return false;
			}
		} else 
			return false; // corpusName is not valid
		return true;
	}
	
	public static boolean corpusNameExists(String corpusName, String corpusId, ScrolledForm corpusMgmtViewform, ManageCorpora corpusManagement) {
		List<ICorpus> corpuses = corpusManagement.getAllCorpusDetails();
		for(ICorpus corpus : corpuses) {
			if(!corpus.getCorpusId().equals(corpusId) && corpus.getCorpusName().equals(corpusName)) return true;
		}
		return false;
	}
	
	public static boolean isCorpusNameValid(String corpusName, String corpusId, ScrolledForm corpusMgmtViewform, ManageCorpora corpusManagement) {
		if(corpusName.isEmpty()) {
			corpusMgmtViewform.getMessageManager().addMessage("corpusNameEmpty", "Provide valid corpus Name", null, IMessageProvider.ERROR);
			return false;
		} else 
			corpusMgmtViewform.getMessageManager().removeMessage("corpusNameEmpty");
		
		if(corpusNameExists(corpusName, corpusId, corpusMgmtViewform, corpusManagement)) {
			corpusMgmtViewform.getMessageManager().addMessage("corpusName", "Corpus Name \""+ corpusName +"\"already exists. Provide different ID", null, IMessageProvider.ERROR);
			return false;
		} else 
			corpusMgmtViewform.getMessageManager().removeMessage("corpusName");
		return true;	
	}	
	
}
