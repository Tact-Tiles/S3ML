/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3ml;

import audiobl.wavCreator.WavCodeGenerator;
import audiobl.waveFile.AePlayWave;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 *
 * @author gnome3
 */
public class UpdateDevice extends JPanel implements Step {

    private Settings s;
    private final JProgressBar progressbar;
    Thread player = null;
    JButton stopbutton = null;

    public UpdateDevice() {
        super.setName("Update Device");
        this.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        add(new JButton(new AbstractAction("Generate & Play") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                play();
            }
        }));
        progressbar = new JProgressBar();

        stopbutton = new JButton(new AbstractAction("Stop") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                stopbutton.setEnabled(false);
                player.stop();
            }
        });
        stopbutton.setEnabled(false);
        add(stopbutton);
        add(progressbar);

    }

    @Override

    public void build() {

    }

    @Override

    public void setSettings(Settings s) {
        this.s = s;
    }

    @Override
    public JPanel getJPanel() {
        return this;
    }

    void play() {
        if (player == null || !player.isAlive()) {
            stopbutton.setEnabled(true);
            int size = s.data.size();
            byte data[] = new byte[size];
            for (int i = 0; i < size; i++) {
                data[i] = s.data.get(i);
            }

            File f = WavCodeGenerator.createWavFile(data, 64, 1);
            player = new AePlayWave(f.toString(), progressbar);
            player.start();
        }
    }

    @Override
    public void doStep() {
        play();
    }

    @Override
    public boolean isDone() {
        return false;
    }
    
    @Override
    public void setUndone(){
        
    }
}
