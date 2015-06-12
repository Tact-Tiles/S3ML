/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3ml.util;

import java.io.OutputStream;
import java.io.PrintStream;
import javax.swing.JTextArea;

/**
 *
 * @author gnome3
 */
public class JTextAreaPrintStream extends PrintStream {

    private JTextArea jtextArea;

    public JTextAreaPrintStream(JTextArea area) {
        this(area, System.out);
    }

    public JTextAreaPrintStream(JTextArea area, OutputStream stdOut) {
        super(stdOut);
        jtextArea = area;
    }

    @Override
    public void println(String string) {
        jtextArea.append(string + "\n");
    }

    @Override
    public void print(String string) {
        jtextArea.append(string);
    }
}
