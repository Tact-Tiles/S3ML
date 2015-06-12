/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3ml;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author gnome3
 */
public class S3ML {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        buildEditorWindow();
    }

    public static void buildEditorWindow() {
        final JFrame frame = new JFrame("S3ML - SEEPROM-Stored State Machine Library");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(800, 600));
        frame.setLocationRelativeTo(null);

        final JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.setTabPlacement(JTabbedPane.LEFT);
        tabbedPane.setFocusable(false);

        final ArrayList<Step> steps = new ArrayList<>();

        steps.add(new InputEditor());
        steps.add(new GestureEditor());
        steps.add(new AlphabetMapping());
        steps.add(new DFABuilder());
        steps.add(new UpdateDevice());

        if (new File("default.glove").exists()) {
            Settings.load("default.glove");
        }

        for (Step s : steps) {
            s.setSettings(Settings.getCurrentSettings());
            s.build();
            tabbedPane.add(s.getJPanel());
        }

        final boolean locktabs = true;

        if (locktabs) {
            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                if (i == tabbedPane.getSelectedIndex()) {
                    tabbedPane.setEnabledAt(i, true);
                } else {
                    tabbedPane.setEnabledAt(i, false);
                }
            }
        }

        frame.getContentPane().add(tabbedPane);

        final JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Project File", "glove", "txt");
        chooser.setCurrentDirectory(new File("."));
        chooser.setFileFilter(filter);

        //glass pane [http://stackoverflow.com/questions/5468522/how-to-place-components-beneath-tabs-in-right-oriented-jtabbedpane]
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 10));
        JButton newButton = new JButton(new AbstractAction("New") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                for (Step s : steps) {
                    s.setSettings(Settings.resetSettings());
                }
                for (Step s : steps) {
                    if (tabbedPane.getSelectedComponent() == s.getJPanel()) {
                        s.setUndone();
                        break;
                    }
                }
                tabbedPane.setSelectedIndex(0);
                if (locktabs) {
                    for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                        if (i == tabbedPane.getSelectedIndex()) {
                            tabbedPane.setEnabledAt(i, true);
                        } else {
                            tabbedPane.setEnabledAt(i, false);
                        }
                    }
                }
            }
        });
        newButton.setMargin(new Insets(0, 0, 0, 0));
        panel.add(newButton);
        JButton openButton = new JButton(new AbstractAction("Open") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                int returnVal = chooser.showOpenDialog(frame);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    Settings.load(chooser.getSelectedFile().toString());
                    for (Step s : steps) {
                        s.setSettings(Settings.getCurrentSettings());
                    }
                    System.out.println("You chose to open this file: " + chooser.getSelectedFile().toString());
                }

            }
        });
        openButton.setMargin(new Insets(0, 0, 0, 0));
        panel.add(openButton);
        JButton saveButton = new JButton(new AbstractAction("Save") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                int returnVal = chooser.showSaveDialog(frame);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    Settings.save(chooser.getSelectedFile().toString() + ".glove");
                    System.out.println("You chose to save this file: " + chooser.getSelectedFile().toString());
                }
            }
        });
        saveButton.setMargin(new Insets(0, 0, 0, 0));
        panel.add(saveButton);
        JButton defaultButton = new JButton(new AbstractAction("Set Default") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                int returnVal = chooser.showSaveDialog(frame);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    Settings.save(chooser.getSelectedFile().toString() + ".glove");
                    System.out.println("You chose to save this file: " + chooser.getSelectedFile().toString());
                    try {
                        File sourceFile = chooser.getSelectedFile();
                        File destFile = new File(sourceFile.getParentFile().getAbsolutePath() + File.separator + "default.glove");
                        System.out.println("" + sourceFile.getParentFile().getAbsolutePath() + File.separator + "default.glove");
                        if (!destFile.exists()) {
                            destFile.createNewFile();
                        }

                        FileChannel source = null;
                        FileChannel destination = null;

                        try {
                            source = new FileInputStream(sourceFile).getChannel();
                            destination = new FileOutputStream(destFile).getChannel();
                            destination.transferFrom(source, 0, source.size());
                        } finally {
                            if (source != null) {
                                source.close();
                            }
                            if (destination != null) {
                                destination.close();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        defaultButton.setMargin(new Insets(0, 0, 0, 0));
        panel.add(defaultButton);
        final JButton previousButton = new JButton(new AbstractAction("<- Previous") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                for (Step s : steps) {
                    if (tabbedPane.getSelectedComponent() == s.getJPanel()) {
                        s.setUndone();
                        break;
                    }
                }
                if (tabbedPane.getSelectedIndex() != 0) {
                    tabbedPane.setSelectedIndex(tabbedPane.getSelectedIndex() - 1);
                    if (locktabs) {
                        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                            if (i == tabbedPane.getSelectedIndex()) {
                                tabbedPane.setEnabledAt(i, true);
                            } else {
                                tabbedPane.setEnabledAt(i, false);
                            }
                        }
                    }
                }
            }
        });
        {
            InputMap inputMap = previousButton.getInputMap(JButton.WHEN_IN_FOCUSED_WINDOW);
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "1");
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "2");
            previousButton.getActionMap().put("1", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    previousButton.doClick();
                }
            });
            previousButton.getActionMap().put("2", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    previousButton.doClick();
                }
            });
        }
        previousButton.setMargin(new Insets(0, 0, 0, 0));
        panel.add(previousButton);
        final JButton nextButton = new JButton(new AbstractAction("Next ->") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                for (Step s : steps) {
                    if (tabbedPane.getSelectedComponent() == s.getJPanel()) {
                        if (!s.isDone()) {
                            s.doStep();
                        }
                        if (tabbedPane.getSelectedIndex() != tabbedPane.getTabCount() - 1) {
                            steps.get(tabbedPane.getSelectedIndex() + 1).setSettings(Settings.getCurrentSettings());
                            tabbedPane.setSelectedIndex(tabbedPane.getSelectedIndex() + 1);
                            if (locktabs) {
                                for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                                    if (i == tabbedPane.getSelectedIndex()) {
                                        tabbedPane.setEnabledAt(i, true);
                                    } else {
                                        tabbedPane.setEnabledAt(i, false);
                                    }
                                }
                            }
                        }
                        break;
                    }
                }

            }
        });
        {
            InputMap inputMap = nextButton.getInputMap(JButton.WHEN_IN_FOCUSED_WINDOW);
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "3");
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "4");
            nextButton.getActionMap().put("3", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    nextButton.doClick();
                }
            });
            nextButton.getActionMap().put("4", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    nextButton.doClick();
                }
            });
        }
        nextButton.setMargin(new Insets(0, 0, 0, 0));
        panel.add(nextButton);
        frame.add(tabbedPane);

        frame.pack();

        Rectangle tabBounds = tabbedPane.getBoundsAt(0);
        Container glassPane = (Container) frame.getGlassPane();
        glassPane.setVisible(true);
        glassPane.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.NONE;
        int margin = (tabBounds.x);
        System.out.println(margin);
        gbc.insets = new Insets(0, margin, 10, 0);
        gbc.anchor = GridBagConstraints.SOUTHWEST;

        panel.setPreferredSize(new Dimension((int) tabBounds.getWidth() - margin,
                panel.getPreferredSize().height));
        glassPane.add(panel, gbc);

        frame.setVisible(true);

    }

}
