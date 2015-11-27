package com.moxun.generator;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Stack;
import java.util.regex.Pattern;

/**
 * 生成器窗体
 * Created by moxun on 15/11/26.
 */
public class GeneratorForm {
    private JTextField authorText;
    private JTextField importText;
    private JTextField implementsText;
    private JTextField rootText;
    private JCheckBox generatorSampleCheckBox;
    private JButton generatorButton;
    private JTextField dirPath;
    private JButton selectButton;
    private JTextField urlText;
    private JTextField pkgText;
    private JPanel panel;
    private JProgressBar progressBar1;
    private JLabel status;

    private Project project;
    private java.util.List<JTextField> textFields = new ArrayList<>();
    private String mainClassName, basePath;
    private String[] imports, implement;

    private Stack<String> path = new Stack<>();

    private JFrame frame;


    public GeneratorForm(Project p_project) {
        project = p_project;
        textFields.clear();
        textFields.add(authorText);
        textFields.add(dirPath);
        textFields.add(urlText);
        textFields.add(pkgText);
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
        for (JTextField textField : textFields) {
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

                    processImportAndImplements();

                    path.clear();
                    path.push(mainClassName);
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
                    decodeJSONObject(dist);

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

    private void processImportAndImplements() {
        String importTxt = importText.getText();
        String implementsTxt = implementsText.getText();
        if (!importTxt.isEmpty()) {
            imports = importText.getText().split(",");
        }

        if (!implementsTxt.isEmpty()) {
            implement = implementsText.getText().split(",");
        }
    }

    private void preGen(String name) {
        File dist = new File(basePath + name + ".java");
        write(dist, "package " + pkgText.getText() + ";\n");

        if (imports != null) {
            append(dist, "\n");
            for (String s : imports) {
                append(dist, "import " + s + ";\n");
            }
        }

        append(dist, "\n");
        append(dist, "/**\n");
        append(dist, " * Author: " + authorText.getText() + "\n");
        append(dist, " * Created by: ModelGenerator on " + new Date().toString() + "\n");
        append(dist, " */\n\n");

        String imples = "";
        if (implement != null) {
            for (String s : implement) {
                imples = imples + s + ", ";
            }
            imples = imples.substring(0, imples.length() - 2);
            append(dist, "public class " + name + " implements " + imples + " {\n\n");
        } else {
            append(dist, "public class " + name + " {\n\n");
        }
    }

    private void write(File file, String s) {
        try {
            FileUtils.write(file, s, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void append(File file, String s) {
        try {
            FileUtils.write(file, s, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void append(String s) {
        String p = basePath + path.peek() + ".java";
        File dst = new File(p);
        append(dst, s);
    }

    public String suffixToUppercase(String s) {
        StringBuilder sb = new StringBuilder(s);
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        return sb.toString();
    }

    public boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    public void decodeJSONObject(JSONObject json) {
        Iterator<String> keys = json.keys();
        JSONObject current = null;
        Object value;
        String key;
        preGen(path.peek());
        while (keys.hasNext()) {
            key = keys.next();
            value = json.get(key);
            if (value instanceof JSONObject) {
                append("\tpublic " + suffixToUppercase(key) + " " + key + ";\n");
                path.push(suffixToUppercase(key));
                current = (JSONObject) value;
                if (current.keySet().size() > 0) {
                    decodeJSONObject(current);
                } else {
                    preGen(path.peek());
                    append("\t//TODO: complemented needed maybe\n");
                    append("\n}\n");
                    Logger.warn("file " + path.peek() + ".java generate success but maybe have some error");
                    path.pop();
                }
            } else if (value instanceof JSONArray) {
                JSONArray v = (JSONArray) value;
                if (v.size() > 0 && !(v.get(0) instanceof JSONObject)) {
                    //处理基本数据类型数组和String数组
                    if (isNumeric(v.get(0).toString())) {
                        if (v.get(0).toString().length() > 6 || key.contains("Id") || key.contains("id")) {
                            append("\tpublic long[] " + key + ";\n");
                        } else {
                            append("\tpublic int[] " + key + ";\n");
                        }
                    } else if (v.get(0).toString().equals("true") || value.toString().equals("false")) {
                        append("\tpublic boolean[] " + key + ";\n");
                    } else {
                        append("\tpublic String[] " + key + ";\n");
                    }
                } else {
                    //处理对象数组
                    append("\tpublic " + suffixToUppercase(key) + "Item[] " + key + ";\n");
                }
                path.push(suffixToUppercase(key));
                decodeJSONArray((JSONArray) value);
            } else {
                //处理基本数据类型和String
                if (isNumeric(value.toString())) {
                    if (value.toString().length() > 6 || key.contains("Id") || key.contains("id")) {
                        append("\tpublic long " + key + ";");
                    } else {
                        append("\tpublic int " + key + ";");
                    }
                } else if (value.toString().equals("true") || value.toString().equals("false")) {
                    append("\tpublic boolean " + key + ";");
                } else {
                    append("\tpublic String " + key + ";");
                }
                if (generatorSampleCheckBox.isSelected()) {
                    append("\t//eg: " + value);
                }
                append("\n");
            }
        }
        try {
            copyMethods();
        } catch (ClassNotFoundException e) {
            Logger.error("resolving implement failed. " + e.getMessage());
        }
        append("\n}\n");
        Logger.info("file " + path.peek() + ".java generate success");
        if (!path.isEmpty()) {
            path.pop();
        }
    }

    public void decodeJSONArray(JSONArray jsonArray) {

//        //是否需要遍历？
//        for (item in jsonArray) {
//            if (item instanceof JSONObject) {
//                path.push(path.peek() + "Item")
//                decodeJSONObject(item)
//            } else if (item instanceof JSONArray) {
//                path.push("array" + index + "->")
//                decodeJSONArray(item)
//            } else {
//
//            }
//        }

        //数组选择其中一个元素出来进行解析就OK
        Object item = jsonArray.get(0);
        if (item instanceof JSONObject) {
            path.push(path.peek() + "Item");
            decodeJSONObject((JSONObject) item);
        } else if (item instanceof JSONArray) {
            //多维数组我选择狗带
            path.push(path.peek() + "Item");
            decodeJSONArray((JSONArray) item);
        } else {

        }

        if (!path.isEmpty()) {
            path.pop();
        }
    }

    public void copyMethods() throws ClassNotFoundException {
        if (implement != null && implement.length > 0) {
            for (String name : imports) {
                for (String tmp : implement) {
                    if (name.contains(tmp)) {
                        Class clazz = Class.forName(name);
                        java.lang.reflect.Method[] methods = clazz.getMethods();
                        for (java.lang.reflect.Method m : methods) {
                            append("\n\t@Override\n");
                            Class<?> returnType = m.getReturnType();
                            int modify = m.getModifiers();
                            Class<?>[] params = m.getParameterTypes();
                            StringBuilder sb = new StringBuilder();
                            String mdf = Modifier.toString(modify).split(" ")[0];
                            sb.append("\t").append(mdf).append(" ")
                                    .append(returnType.getName()).append(" ").append(m.getName()).append(" (");
                            for (int i = 0; i < params.length; i++) {
                                sb.append(params[i].getName()).append(" arg").append(i);
                                if (i < params.length - 1) {
                                    sb.append(", ");
                                }
                            }
                            sb.append(") {\n\n");
                            switch (returnType.getName()) {
                                case "void":
                                    break;
                                case "byte":
                                case "short":
                                case "int":
                                case "long":
                                case "float":
                                case "double":
                                    sb.append("\t\treturn 0;\n");
                                    break;
                                case "boolean":
                                    sb.append("\t\treturn false;\n");
                                    break;
                                default:
                                    sb.append("\t\treturn null;\n");

                            }
                            sb.append("\t}\n");
                            String statement = sb.toString();
                            append(statement);
                        }
                        break;
                    }
                }
            }
        }
    }
}
