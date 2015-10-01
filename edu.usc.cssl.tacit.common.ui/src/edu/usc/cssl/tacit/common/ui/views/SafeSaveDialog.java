package edu.usc.cssl.tacit.common.ui.views;

import java.io.File;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;


public class SafeSaveDialog {
private FileDialog dlg;

public SafeSaveDialog(Shell shell) {
  dlg = new FileDialog(shell, SWT.SAVE);
}

public String open() {
  String fileName = null;

  boolean isSaved = false;

  while (!isSaved) {
    fileName = dlg.open();
    if (fileName == null) {
      isSaved = true;
    } else {
      File file = new File(fileName);
      if (file.exists()) {
        MessageBox mb = new MessageBox(dlg.getParent(), SWT.ICON_WARNING
            | SWT.YES | SWT.NO);
        mb.setMessage(fileName + " already exists. Do you want to replace it?");

        isSaved = mb.open() == SWT.YES;
      } else {
        isSaved = true;
      }
    }
  }
  return fileName;
}

public String getFileName() {
  return dlg.getFileName();
}

public String[] getFileNames() {
  return dlg.getFileNames();
}

public String[] getFilterExtensions() {
  return dlg.getFilterExtensions();
}

public String[] getFilterNames() {
  return dlg.getFilterNames();
}

public String getFilterPath() {
  return dlg.getFilterPath();
}

public void setFileName(String string) {
  dlg.setFileName(string);
}

public void setFilterExtensions(String[] extensions) {
  dlg.setFilterExtensions(extensions);
}

public void setFilterNames(String[] names) {
  dlg.setFilterNames(names);
}

public void setFilterPath(String string) {
  dlg.setFilterPath(string);
}

public Shell getParent() {
  return dlg.getParent();
}

public int getStyle() {
  return dlg.getStyle();
}

public String getText() {
  return dlg.getText();
}

public void setText(String string) {
  dlg.setText(string);
}
}
