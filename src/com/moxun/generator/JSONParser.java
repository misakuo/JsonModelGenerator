package com.moxun.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

/**
 * Parsing json and generating code
 * Created by moxun on 15/12/9.
 */
public class JSONParser {
    private Stack<String> path = new Stack<String>();
    private List<String> allNodes = new ArrayList<String>();
    private boolean needGenSample = false;
    private GeneratorEngine engine;
    private boolean isArrayToList = false;
    private boolean genGetter;
    private boolean genSetter;

    public void reset(Project proj, PsiDirectory dir) {
        path.clear();
        allNodes.clear();
        engine = new GeneratorEngine(proj, dir);
    }

    public void init(String mainClassName, String pkg, String[] its, boolean isArrayToList) {
        push(suffixToUppercase(mainClassName));
        engine.init(pkg, its);
        this.isArrayToList = isArrayToList;
    }

    public void setGenSample(boolean has) {
        needGenSample = has;
    }

    public String decodeJSONObject(JSONObject json) {
        String className = null;
        Iterator<String> keys = json.keys();
        JSONObject current = null;
        Object value;
        String key;
        String last = "";
        if (path.size() > 1) {
            last = path.get(path.size() - 2);
        }
        className = engine.preGen(path.peek(), last);
        while (keys.hasNext()) {
            key = keys.next();
            value = json.get(key);
            key = ClassNameUtil.getName(key);
            if (value instanceof JSONObject) {
                String validName = ClassNameUtil.getName(suffixToUppercase(key));
                String modifier = getModifier();
                append(modifier + validName + " " + key + ";\n");
                push(validName);
                current = (JSONObject) value;
                if (current.keySet().size() > 0) {
                    decodeJSONObject(current);
                } else {
                    String last1 = "";
                    if (path.size() > 1) {
                        last1 = path.get(path.size() - 2);
                    }
                    engine.preGen(path.peek(), last1);
                    append("// TODO: complemented needed maybe.");
                    Logger.warn("Success to generating file " + path.peek() + ".java but it have no field");
                    path.pop();
                }
            } else if (value instanceof JSONArray) {
                JSONArray v = (JSONArray) value;
                if (v.size() > 0 && !(v.get(0) instanceof JSONObject)) {
                    Object firstValue = v.get(0);
                    //处理基本数据类型数组和String数组
                    String field = getModifier() + getArrayType(decisionValueType(key, firstValue, true), isArrayToList) + " " + key + ";\n";
                    append(field);
                } else {
                    //处理对象数组
                    if (isArrayToList) {
                        append(getModifier() + "List<" + suffixToUppercase(key) + "Item>" + key + ";\n");
                    } else {
                        append(getModifier() + suffixToUppercase(key) + "Item[] " + key + ";\n");
                    }
                }
                push(suffixToUppercase(key));
                decodeJSONArray((JSONArray) value);
            } else {
                //处理基本数据类型和String
                String field = getModifier();
                field += decisionValueType(key, value, false) + " " + key + ";";
                if (needGenSample) {
                    String v = String.valueOf(value);
                    v = v.replaceAll("\n", "");
                    if (v.length() > 15) {
                        v = v.substring(0, 15);
                    }
                    field = field + "\t// " + v;
                }
                append(field);
            }
        }

        Logger.info("Success to generating file " + path.peek() + ".java");
        if (!path.isEmpty()) {
            path.pop();
        }
        return className;
    }

    private String getModifier() {
        if (!genGetter && !genSetter) {
            return "public ";
        } else {
            return "private ";
        }
    }

    private String decisionValueType(/*not uesd*/String key, Object value, boolean formArray) {
        if (formArray) {
            return value.getClass().getSimpleName();
        } else {
            if (value instanceof Integer) {
                return "int";
            } else if (value instanceof Long) {
                return "long";
            } else if (value instanceof Double) {
                return "double";
            } else if (value instanceof Boolean) {
                return "boolean";
            }
        }
        return "String";
    }

    @Deprecated
    private String __inferValueType(String key, String value, boolean formArray) {
        String type = "String";
        if (isNumeric(value)) {
            if (isInteger(value)) {
                if (value.length() > 8 || key.contains("Id") || key.contains("id") || key.contains("ID")) {
                    if (formArray) {
                        return "Long";
                    }
                    return "long";
                } else {
                    if (formArray) {
                        return "Integer";
                    }
                    return "int";
                }
            } else {
                String[] tmp = value.split("\\.");
                int fLength = 0;
                if (tmp.length > 1) {
                    fLength = value.split("\\.")[1].length();
                } else {
                    Logger.error(value);
                }

                if (fLength > 8) {
                    if (formArray) {
                        return "Double";
                    } else {
                        return "double";
                    }
                } else {
                    if (formArray) {
                        return "Float";
                    } else {
                        return "float";
                    }
                }
            }
        } else if (value.equals("true") || value.equals("false")) {
            if (formArray) {
                return "Boolean";
            } else {
                return "boolean";
            }
        }
        return type;
    }

    private String getArrayType(String baseType, boolean isArrayToList) {
        if (isArrayToList) {
            return "List<" + baseType + ">";
        } else {
            return baseType + "[]";
        }
    }

    public void decodeJSONArray(JSONArray jsonArray) {

        //        //是否需要遍历？
        //        for (item in jsonArray) {
        //            if (item instanceof JSONObject) {
        //                push(path.peek() + "Item")
        //                decodeJSONObject(item)
        //            } else if (item instanceof JSONArray) {
        //                push("array" + index + "->")
        //                decodeJSONArray(item)
        //            } else {
        //
        //            }
        //        }

        //数组选择其中一个元素出来进行解析就OK
        Object item = jsonArray.get(0);
        if (item instanceof JSONObject) {
            push(path.peek() + "Item");
            decodeJSONObject((JSONObject) item);
        } else if (item instanceof JSONArray) {
            //多维数组我选择狗带
            push(path.peek() + "Item");
            decodeJSONArray((JSONArray) item);
        } else {

        }

        if (!path.isEmpty()) {
            path.pop();
        }
    }

    public String suffixToUppercase(String s) {
        StringBuilder sb = new StringBuilder(s);
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        return sb.toString();
    }

    //正负整数,浮点数
    public boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("-?[0-9]+\\.?[0-9]*");
        return pattern.matcher(str).matches();
    }

    private boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("-?[0-9]+");
        return pattern.matcher(str).matches();
    }

    public void append(String field) {
        engine.append(field, path.peek());
    }

    private void push(String name) {
        String uniqueName = ClassNameUtil.getName(name);
        if (allNodes.contains(name)) {
            uniqueName = path.peek() + name;
        }

        if (allNodes.contains(uniqueName)) {
            for (int i = 1; i <= 50; i++) {
                uniqueName = uniqueName + i;
                if (!allNodes.contains(uniqueName)) {
                    break;
                }
            }
        }

        allNodes.add(uniqueName);
        path.push(uniqueName);
    }

    void setGenGetter(boolean genGetter) {
        this.genGetter = genGetter;
        engine.setGenGetter(genGetter);
    }

    void setGenSetter(boolean genSetter) {
        this.genSetter = genSetter;
        engine.setGenSetter(genSetter);
    }
}
