/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package s3ml;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import s3ml.util.JTextAreaPrintStream;

/**
 *
 * @author gnome3
 */
public class AlphabetMapping extends JPanel implements Step {

    private Settings s;
    private JTextArea textArea = new JTextArea();
    boolean isDone = false;

    public AlphabetMapping() {
        super.setName("Alphabet Mapping");
        super.setLayout(new BorderLayout());

        textArea.setFont(new Font("monospaced", Font.PLAIN, 15));

        add(new JScrollPane(textArea), BorderLayout.CENTER);
        add(new JButton(new AbstractAction("Build Alphabet Map") {
            {
                setPreferredSize(new Dimension(200, 100));
            }

            @Override
            public void actionPerformed(ActionEvent ae) {
                buildInputMap();
            }
        }), BorderLayout.PAGE_START);
    }

    @Override
    public void build() {

    }

    public void buildInputMap() {

        HashMap<Integer, ArrayList<ArrayList<Integer>>> map = new HashMap();

        Comparator<ArrayList<Integer>> c = new Comparator<ArrayList<Integer>>() {
            @Override
            public int compare(ArrayList<Integer> t, ArrayList<Integer> t1) {
                for (int i = 0; i < t.size() && i < t1.size(); i++) {
                    int a = t.get(i);
                    int b = t1.get(i);
                    if (a < b) {
                        return -1;
                    } else if (a > b) {
                        return 1;
                    }
                }
                return 0;
            }
        };

        for (Gesture g : s.getGestures()) {
            for (ArrayList<Integer> key : g.msg) {
                int keySize = key.size();
                if (keySize > 0) {//TODO
                    if (map.containsKey(keySize)) {
                        ArrayList<ArrayList<Integer>> keys = map.get(keySize);
                        boolean equals = false;
                        for (ArrayList<Integer> anotherKey : keys) {
                            if (key.equals(anotherKey)) {
                                equals = true;
                            }
                        }
                        if (!equals) {
                            keys.add(key);
                        }
                    } else {
                        ArrayList<ArrayList<Integer>> newKeys = new ArrayList<>();
                        newKeys.add(key);
                        map.put(keySize, newKeys);
                    }
                }
            }
        }
        for (Map.Entry<Integer, ArrayList<ArrayList<Integer>>> e : map.entrySet()) {
            Collections.sort(e.getValue(), c);
        }

        PrintStream out = new JTextAreaPrintStream(textArea);
        ArrayList<Byte> data = new ArrayList<>();

        int headerSize = 10;

        data.add((byte) s.inputs.size());                 //number of keys
        data.add((byte) 0);                               //2byteoutput
        data.add((byte) map.size());                      //category size
        data.add((byte) headerSize);                      //alphabet address
        data.add((byte) 0);
        data.add((byte) 0);                               //dfa address (added latter)
        data.add((byte) 0);

        int usedHeaderSpace = data.size();
        for (int i = headerSize; i > usedHeaderSpace; i--) {
            data.add((byte) -1);
        }

        out.println("Alphabet size: " + map.size());
        int index = -1 + map.size() * 3;
        for (Map.Entry<Integer, ArrayList<ArrayList<Integer>>> e : map.entrySet()) {
            byte lowAddrs = (byte) ++index;
            byte highAddrs = (byte) (index += (e.getValue().get(0).size() + 1) * (e.getValue().size() - 1));
            index += e.getValue().get(0).size();
            out.println(e.getKey() + " (" + lowAddrs + ", " + highAddrs + ")");
            data.add(e.getKey().byteValue());
            data.add((byte) lowAddrs);
            data.add((byte) highAddrs);
        }
        int symbol = 1;
        int size = 0;
        Map<List<Integer>, Integer> aplhabetMap = new HashMap<>();
        for (Map.Entry<Integer, ArrayList<ArrayList<Integer>>> e : map.entrySet()) {
            for (ArrayList<Integer> k : e.getValue()) {
                size++;
                aplhabetMap.put(k, symbol);
                data.add((byte) symbol);
                out.print(symbol++ + "(" + e.getKey() + ") : [");
                for (Iterator<Integer> it = k.iterator(); it.hasNext();) {
                    int i = it.next();
                    out.print((i + 1) + "");
                    if (it.hasNext()) {
                        out.print(", ");
                    }
                }
                out.println("]");
                for (int i : k) {
                    data.add((byte) (i + 1));
                }
            }
        }

        data.set(0, (byte) (size));
        data.set(5, (byte) (data.size()));            //dfa address

        out.println("Map size: " + data.size() + " bytes");
        out.println("data: " + data);

        s.data = data;
        s.alphabetMap = aplhabetMap;

        s.dumpMemory(out);
        isDone = true;
    }

    @Override

    public void setSettings(Settings s) {
        this.s = s;
    }

    @Override
    public JPanel getJPanel() {
        return this;
    }

    @Override
    public void doStep() {
        buildInputMap();
    }

    @Override
    public boolean isDone() {
        return isDone;
    }

    @Override
    public void setUndone() {
        isDone = false;
    }
}
