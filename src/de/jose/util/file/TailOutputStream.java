package de.jose.util.file;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;

public class TailOutputStream
        extends OutputStream
{
    private JTextArea textArea;

    public TailOutputStream(JTextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public void write(int b) throws IOException {
        textArea.append(String.valueOf((char) b));
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        textArea.append(new String(b, off, len));
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }
}
