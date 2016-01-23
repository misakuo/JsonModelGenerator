package com.moxun.generator;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Frame to allow user input string and checking it is JSON or not
 * Created by moxun on 16/1/23.
 */
public class JSONStringInputer {
    private JTextArea textArea1;
    private JPanel panel1;
    private JButton commitButton;
    private JLabel status;
    private CommitCallback commitCallback;
    private JFrame frame;

    public JSONStringInputer(CommitCallback callback) {
        init();
        commitCallback = callback;
    }

    private void init() {
        textArea1.setLineWrap(true);
        textArea1.setWrapStyleWord(true);

        commitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = textArea1.getText();
                String src = replaceBlank(text);
                if (src.startsWith("{")) {
                    try {
                        JSONObject jsonObject = JSONObject.fromObject(src);
                        status.setVisible(false);
                        commitCallback.onCommit(src);
                        frame.dispose();
                    } catch (JSONException ex) {
                        status.setVisible(true);
                        status.setText("Exception:" + ex.getMessage());
                    }
                } else if (src.startsWith("[")) {
                    try {
                        JSONArray jsonArray = JSONArray.fromObject(src);
                        status.setVisible(false);
                        commitCallback.onCommit(src);
                        frame.dispose();
                    } catch (JSONException ex) {
                        status.setVisible(true);
                        status.setText("Exception:" + ex.getMessage());
                    }
                } else {
                    status.setText("Can't formatting input content to JSON");
                    status.setVisible(true);
                }
            }
        });
    }

    public String replaceBlank(String str) {
        String dest = "";
        if (str!=null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }

    public void show() {
        frame = new JFrame("Input your JSON string");
        frame.setContentPane(panel1);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(frame.getParent());
        frame.pack();
        frame.setVisible(true);
        frame.setAlwaysOnTop(true);
    }

    public interface CommitCallback {
        void onCommit(String src);
    }
}
