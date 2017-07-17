package com.duy.terminal.compat;

public interface ClipboardManagerCompat {
	CharSequence getText();

	boolean hasText();

    void setText(CharSequence text);
}
