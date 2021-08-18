// #condition MIDP
package it.yup.ui.wrappers;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;

// #ifndef RIM

import javax.microedition.lcdui.TextBox;

// #endif

public class UITextbox {

	private CommandListener innerCommandListener;

// #ifndef RIM
	TextBox textBox = null;

	// #endif

	public UITextbox(String title, String text, int maxSize, int constraints) {
// #ifndef RIM
		textBox = new TextBox(title, text, maxSize, constraints);
		// #endif
	}

	public void addCommand(Command command) {
// #ifndef RIM
		textBox.addCommand(command);
		// #endif
	}

	public void setCommandListener(CommandListener commandlistener) {
		innerCommandListener = commandlistener;
		// #ifndef RIM
		textBox.setCommandListener(innerCommandListener);
		// #endif
	}

	public String getString() {
// #ifndef RIM
		return textBox.getString();
		// #endif
	}

	public void setTitle(String s) {
		textBox.setTitle(s);
	}

	public void removeCommand(Command command) {
// #ifndef RIM
		textBox.removeCommand(command);
		// #endif
	}

// #ifndef RIM
	public TextBox getTextBox() {
		// #endif
		return textBox;
	}
}
