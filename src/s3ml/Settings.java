/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3ml;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author gnome3
 */
public class Settings implements Serializable {

    private static final long serialVersionUID = 7863262235394607247L;
    private static Settings currentSettings = null;

    public static Settings resetSettings() {
        currentSettings = new Settings();
        return currentSettings;
    }

    public static Settings getCurrentSettings() {
        if (currentSettings == null) {
            currentSettings = new Settings();
        }
        return currentSettings;
    }

    List<Integer> inputs = new ArrayList<>();
    Map<Integer, List<Double>> map = new HashMap<>();
    List<Gesture> gestures = new ArrayList<>();
    Map<List<Integer>, Integer> alphabetMap = new HashMap<>();
    List<Byte> data = new ArrayList<>();
    String path = "filename.txt";

    private Settings() {

    }

    public static void save(String file) {
        try {
            FileOutputStream fileStream = new FileOutputStream(file);
            ObjectOutputStream os = new ObjectOutputStream(fileStream);
            os.writeObject(currentSettings);
            os.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void load(String file) {
        try {
            FileInputStream fileInStream = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fileInStream);
            Settings obj = (Settings) ois.readObject();
            ois.close();
            currentSettings = obj;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void dumpMemory(PrintStream out) {
        for (int i = 0; i < data.size();) {
            out.print(String.format("%04d :  ", i));
            for (int j = 0; j < 10; j++, i++) {
                if (i >= data.size()) {
                    out.print("000.");
                } else {
                    int b = data.get(i);
                    if (b < 0) {
                        b = 256 + b;
                    }
                    out.print(String.format("%03d  ", b));
                }
            }
            out.println(" ");
        }
    }

//    public Settings() {
//        map.put(0, Arrays.asList(261.46536412078154, 152.33570159857905, 50., 50.));
//        map.put(1, Arrays.asList(302.38898756660745, 281.9271758436945, 50., 50.));
//        map.put(2, Arrays.asList(341.0390763765542, 395.6039076376554, 50., 50.));
//        map.put(3, Arrays.asList(509.28063943161635, 70.48845470692719, 50., 50.));
//        map.put(4, Arrays.asList(502.4600355239787, 215.99467140319717, 50., 50.));
//        map.put(5, Arrays.asList(502.4600355239787, 361.50088809946715, 50., 50.));
//
//        inputs.add(0);
//        inputs.add(1);
//        inputs.add(2);
//        inputs.add(3);
//        inputs.add(4);
//        inputs.add(5);
//
//        gestures.add(createGesture(11, 4));
//        gestures.add(createGesture(12, 3));
//        gestures.add(createGesture(13, 1));
//        gestures.add(createGesture(14, 2));
//        gestures.add(createGesture(15, 5));
//        gestures.add(createGesture(2, 1, new int[]{3, 4, 5}, 3));
//        gestures.add(createGesture(33, 1, new int[]{2, 3, 4}, 3));
//        gestures.add(createGesture(4, new int[]{2, 3}, 1));
//        gestures.add(createGesture(55, 1, new int[]{1, 5}, 1));
//        gestures.add(createGesture(32, 3, 4, 2));
//        gestures.add(createGesture(16, 1, 1, 1, 2, 3, 4));
//    }
    private Gesture createGesture(int s, Object... gs) {
        ArrayList<ArrayList<Integer>> a = new ArrayList<>();
        for (Object i : gs) {
            ArrayList<Integer> t = new ArrayList<>();
            if (i instanceof Integer) {
                t.add((Integer) i);
            } else if (i instanceof int[]) {
                for (int j : (int[]) i) {
                    t.add(j);
                }
            }
            if (!t.isEmpty()) {
                a.add(t);
            }
        }
        return new Gesture(a, s);
    }

    List<Integer> getInputs() {
        return inputs;
    }

    Map<Integer, List<Double>> getMap() {
        return map;
    }

    List<Gesture> getGestures() {
        return gestures;
    }

}
