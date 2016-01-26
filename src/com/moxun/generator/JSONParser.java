package com.moxun.generator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.Iterator;
import java.util.Stack;
import java.util.regex.Pattern;

/**
 * Parsing json and generating code
 * Created by moxun on 15/12/9.
 */
public class JSONParser {
    private Stack<String> path = new Stack<String>();
    private boolean needGenSample = false;
    private GeneratorEnginer enginer;

    public void reset(String mainClsName,Project proj,PsiDirectory dir) {
        path.clear();
        path.push(suffixToUppercase(mainClsName));
        enginer = new GeneratorEnginer(proj,dir);
    }

    public void init(String pkg,String[] its) {
        enginer.init(pkg,its);
    }

    public void setGenSample(boolean has) {
        needGenSample = has;
    }

    public void decodeJSONObject(JSONObject json) {
        Iterator<String> keys = json.keys();
        JSONObject current = null;
        Object value;
        String key;
        enginer.preGen(path.peek());
        while (keys.hasNext()) {
            key = keys.next();
            value = json.get(key);
            if (value instanceof JSONObject) {
                append("public " + suffixToUppercase(key) + " " + key + ";\n");
                path.push(suffixToUppercase(key));
                current = (JSONObject) value;
                if (current.keySet().size() > 0) {
                    decodeJSONObject(current);
                } else {
                    enginer.preGen(path.peek());
                    append("//TODO: complemented needed maybe\n");
                    Logger.warn("file " + path.peek() + ".java generate success but maybe have some error");
                    path.pop();
                }
            } else if (value instanceof JSONArray) {
                JSONArray v = (JSONArray) value;
                if (v.size() > 0 && !(v.get(0) instanceof JSONObject)) {
                    //处理基本数据类型数组和String数组
                    if (isNumeric(v.get(0).toString())) {
                        if (v.get(0).toString().length() > 6 || key.contains("Id") || key.contains("id")) {
                            append("public long[] " + key + ";\n");
                        } else {
                            append("public int[] " + key + ";\n");
                        }
                    } else if (v.get(0).toString().equals("true") || value.toString().equals("false")) {
                        append("public boolean[] " + key + ";\n");
                    } else {
                        append("public String[] " + key + ";\n");
                    }
                } else {
                    //处理对象数组
                    append("public " + suffixToUppercase(key) + "Item[] " + key + ";\n");
                }
                path.push(suffixToUppercase(key));
                decodeJSONArray((JSONArray) value);
            } else {
                //处理基本数据类型和String
                String field = null;
                if (isNumeric(value.toString())) {
                    if (value.toString().length() > 6 || key.contains("Id") || key.contains("id")) {
                        field = "public long " + key + ";";
                    } else {
                        field = "public int " + key + ";";
                    }
                } else if (value.toString().equals("true") || value.toString().equals("false")) {
                    field = "public boolean " + key + ";";
                } else {
                    field = "public String " + key + ";";
                }
                if (needGenSample) {
                    field = field + "\t// " + value;
                }
                append(field);
            }
        }

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

    public String suffixToUppercase(String s) {
        StringBuilder sb = new StringBuilder(s);
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        return sb.toString();
    }

    public boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    public void append(String field) {
        enginer.append(field,path.peek());
    }
}
