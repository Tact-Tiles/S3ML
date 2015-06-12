/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3ml;

import javax.swing.JPanel;

/**
 *
 * @author gnome3
 */
public interface Step {

    void build();

    public void setSettings(Settings s);

    public JPanel getJPanel();

    public void doStep();

    public boolean isDone();

    public void setUndone();
}
