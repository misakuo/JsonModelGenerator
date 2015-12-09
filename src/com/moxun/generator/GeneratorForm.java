package com.moxun.generator;

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
import net.sf.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
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

    private Project project;
    private List<JTextField> textFields = new ArrayList<>();
    private String mainClassName, basePath;
    private String[] implement;

    private JFrame frame;

    private JSONParser parser;


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

                    JSONObject response = HttpHelper.getResponse(urlText.getText());
                    JSONObject dist = null;

                    if (response == null) {
                        Messages.showErrorDialog(project, "Get JSONObject filed, see detail on Event Log", "Error");
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
    }

    private void showFileChoicer() {
        FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, false, false, false, false);
        VirtualFile virtualFile = FileChooser.chooseFile(descriptor, project, null);
        if (virtualFile != null) {
            PsiDirectory directory = PsiDirectoryFactory.getInstance(project).createDirectory(virtualFile);
            parser.reset(directory.getName(),project,directory);

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
