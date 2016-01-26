# JSON Model Generator  
[中文](https://github.com/misakuo/JsonModelGenerator/blob/master/readme.md)

## Introduction
An code tools plugin for Intellij IDEA platform, using it to transforming JSON string to Java class.
     
- Support get JSON String form URL;     
- Support field type inference;         
- Support generate import and implements statement, and generate implemented methods automatically;     
- Support parse JSONObject and JSONArray to separate class (rules: JSONObject -> Object   JSONArray -> Object[] )

## Install  
1. Click [here](https://github.com/misakuo/JsonModelGenerator/raw/master/ModelGenerator.zip) to downloading file **ModelGenerator.zip** or search plugin with name **JSON Model Generator** in your IDE  
2. Opening IDEA（Android Studio or others JetBrains IDE support Java can also be），open **Preferences -> Plugins -> Install plugin from disk...** select **ModelGenerator.zip** ，you can see plugin icon in toolbar after restart IDE  

## Useage  
#### Plugin's GUI  
![图1](https://raw.githubusercontent.com/misakuo/JsonModelGenerator/master/img/1.png)

#### Arguments  
- Path：Destination of generating .java file to, set by **Select** button  
- URL：Plugin will fetching this URL to get JSON string, only GET method supported.  sample: `http://news-at.zhihu.com/api/4/news/latest`
- Author：Author's name, using in class annotation, default is current login user's name,  you can set another name also.  
- Implements：The java interface's name it will be implemented by generated class, if you want to implement multiple interface, separate them by comma symbol.  such as `Runnable,Serializable` or others interface name you defined.  
- Root node：Define the root node of JSONObject you want to start parsing, parsing whole JSONObject if not setting it, for example, you got JSONObject from URL is `a`, if want to parsing `a.data`, only need to setting this filed to `data`  
- generate sample：If selected, plugin will generating the value as annotation after filed statement in java class, for example if selected it you will got filed like `public long userId;  // 27639372 ` in class  

**In all arguments Path、URL、Author and Package is necessary，Author and Package will be generating automatically by default. the remaining arguments could be empty if not require.**

## Todos  
1. supporting generating  getter and setter.  
2. supporting get JSON string from user input.  
3. using List<E> replace Array.  