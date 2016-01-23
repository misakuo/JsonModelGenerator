package com.moxun.generator;

import com.intellij.ide.startup.StartupActionScriptManager;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.util.FileContentUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * 生成器窗体
 * Created by moxun on 15/11/26.
 */
public class GeneratorForm {
    private JTextField authorText;
    private JTextField implementsText;
    private JTextField rootText;
    private JCheckBox generatorSampleCheckBox;
    private JButton generatorButton;
    private JTextField dirPath;
    private JButton selectButton;
    private JTextField urlText;
    private JTextField pkgText;
    private JPanel panel;
    private JButton gitButton;
    private JComboBox source;

    private Project project;
    private List<JTextField> textFields = new ArrayList<JTextField>();
    private String mainClassName, basePath;
    private String[] implement;

    private JFrame frame;

    private JSONParser parser;

    private String jsonString;


    public GeneratorForm(Project p_project) {
        project = p_project;
        textFields.clear();
        textFields.add(authorText);
        textFields.add(dirPath);
        textFields.add(urlText);
        textFields.add(pkgText);

        parser = new JSONParser();
    }

    public void show() {
        setListener();
        authorText.setText(System.getProperty("user.name"));
        frame = new JFrame("JSON Model Generator");
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(frame.getParent());
        frame.setVisible(true);
    }

    private void setListener() {
        for (final JTextField textField : textFields) {
            textField.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    textField.setBackground(Color.YELLOW);
                }

                @Override
                public void focusLost(FocusEvent e) {
                    textField.setBackground(Color.WHITE);
                }
            });
        }

        urlText.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (source.getSelectedIndex() == 1) {
                    showInputer();
                }
            }
        });

        source.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (source.getSelectedItem().toString().equals("Input")) {
                    showInputer();
                } else {
                    urlText.setEditable(true);
                    urlText.setText("");
                }
            }
        });

        selectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setVisible(false);
                showFileChoicer();
            }
        });
        generatorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (check()) {
                    if (pkgText.getText().split("\\.").length > 0) {
                        String[] tmp = pkgText.getText().split("\\.");
                        mainClassName = tmp[tmp.length - 1];
                        StringBuilder sb = new StringBuilder(mainClassName);
                        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
                        mainClassName = sb.toString();
                    }

                    JSONObject response;
                    if (source.getSelectedIndex() == 0) {
                        response = parseString(HttpHelper.getResponse(urlText.getText()));
                    } else {
                        response = parseString(jsonString);
                    }
                    JSONObject dist = null;

                    if (response == null) {
                        Messages.showErrorDialog(project, "Parsing JSON failed, see detail on Event Log", "Error");
                        return;
                    }

                    if (!rootText.getText().isEmpty() && response != null) {
                        dist = response.getJSONObject(rootText.getText());
                    }

                    if (dist == null) {
                        dist = response;
                    }


                    processImplements();

                    parser.init(pkgText.getText(), implement);
                    parser.setGenSample(generatorSampleCheckBox.isSelected());
                    parser.decodeJSONObject(dist);

                    Messages.showInfoMessage(project, "Generating success!", "Success");
                    frame.dispose();
                    File file = new File(basePath + mainClassName + ".java");
                    if (file.exists()) {
                        VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
                        if (virtualFile != null) {
                            virtualFile.refresh(false, true);
                            FileEditorManager.getInstance(project).openTextEditor(new OpenFileDescriptor(project, virtualFile), true);
                        }
                    }
                }
            }
        });

        gitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    URI uri = new URI("https://github.com/misakuo/JsonModelGenerator");
                    Desktop.getDesktop().browse(uri);
                } catch (URISyntaxException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    private void showInputer() {
        frame.setAlwaysOnTop(false);
        new JSONStringInputer(new JSONStringInputer.CommitCallback() {
            @Override
            public void onCommit(String src) {
                jsonString = src;
                String snapshot;
                if (src.length() > 10) {
                    snapshot = src.substring(0,10);
                } else {
                    snapshot = src;
                }
                urlText.setText("[JSONString from user input] " + snapshot + " ……");
                urlText.setEditable(false);
                frame.setAlwaysOnTop(true);
            }
        }).show();
    }

    private JSONObject parseString(String src) {
        if (src != null) {
            if (src.startsWith("{")) {
                JSONObject ret = null;
                try {
                    ret = JSONObject.fromObject(src);
                } catch (JSONException e) {
                    Logger.error(e.getMessage());
                }
                return ret;
            } else if (src.startsWith("[")) {
                JSONArray ret = null;
                try {
                    ret = JSONArray.fromObject(src);
                    for (Object obj : ret) {
                        if (obj instanceof JSONObject) {
                            return (JSONObject) obj;
                        }
                    }
                    Logger.error("No JSONObject found on this JSONArray");
                    return null;
                } catch (JSONException e) {
                    Logger.error(e.getMessage());
                }
                return null;
            } else {
                if (src.length() > 50) {
                    src = src.substring(0, 50);
                }
                Logger.error(new StringBuilder().append("Parse failed, it maybe not a json string : ").append(src)
                        .append(" (").append(src.length()).append(" characters more) ……").toString());
                return null;
            }
        } else {
            Logger.error("Input string is null");
            return null;
        }
    }

    private void showFileChoicer() {
        FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, false, false, false, false);
        VirtualFile virtualFile = FileChooser.chooseFile(descriptor, project, null);
        if (virtualFile != null) {
            PsiDirectory directory = PsiDirectoryFactory.getInstance(project).createDirectory(virtualFile);
            parser.reset(directory.getName(), project, directory);
            String path = virtualFile.getPath();
            String pkg = "";
            dirPath.setText(path);
            if (path.contains("java")) {
                pkg = path.split("java/")[1].replaceAll("/", ".");
            } else if (path.contains("src")) {
                pkg = path.split("src/")[1].replaceAll("/", ".");
            }
            basePath = path + "/";
            pkgText.setText(pkg);

            Logger.info("got path " + path);
        } else {
            Logger.warn("got path null");
        }
        frame.setAlwaysOnTop(true);
        frame.setVisible(true);
    }

    private boolean check() {
        boolean pass = true;
        for (JTextField textField : textFields) {
            if (textField.getText().isEmpty()) {
                textField.setBackground(new Color(0xff, 0xae, 0xb9));
                pass = false;
            } else {
                textField.setBackground(new Color(0xff, 0xff, 0xff));
            }
        }
        return pass;
    }

    private void processImplements() {
        String implementsTxt = implementsText.getText();

        if (!implementsTxt.isEmpty()) {
            implement = implementsText.getText().split(",");
        }
    }
}
