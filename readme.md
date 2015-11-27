# JSON Model Generator  

## 简介  
一个Intellij IDEA平台插件，可以快速执行从JSON字符串到Java实体类的转换。
  
- 支持从URL获取JSON字符串； 
- 支持字段值类型推断；    
- 支持默认import和默认接口实现；  
- 支持将类型为JSONObject和JSONArray的子元素拆分为单独的实体类；

## 安装  
1. [从这里](http://gitlab.alibaba-inc.com/moxun.ljf/JsonModelGenerator/blob/master/ModelGenerator.zip)下载ModelGenerator.zip文件  
2. 打开IDEA（Android Studio或其他JetBrains系的IDE也可以），打开Preferences -> Plugins -> Install plugin from disk... 选择ModelGenerator.zip，添加后重启IDE即可在工具栏上看到插件图标

## 使用  
这个插件干了也没有什么别的，大概三件事：  

1. 从URL获得JSON字符串，并解析成JSONObject  
2. 根据用户指定的根节点（如果没有指定则以整个JSONObject的最外层作为根节点），递归解析出每一个Key-Value对，推断Value类型并生成Java成员变量  
3. 解析用户指定的需要实现的接口，通过反射得到方法列表、方法修饰符、返回值和参数列表，生成默认方法实现和默认返回值

如果说还有其他的就是生成默认的package字段（根据文件路径判断），生成默认的import区块，生成默认的类注释，实体类生成完之后刷新IDE的虚拟文件系统并在编辑器中打开生成的文件。但这些都是次要的，主要的就是三件事情，很惭愧，就做了一点微小的工作。

## 图解  
#### 插件图形界面  
![图1](http://gitlab.alibaba-inc.com/moxun.ljf/JsonModelGenerator/raw/master/img/1.png)

#### 参数说明  
- Path：生成的实体类的 .java 文件存放的路径，通过右侧 Select 按钮选择
- URL：可获取 JSON 字符串的 URL ，只支持 GET 请求，例如 `http://news-at.zhihu.com/api/4/news/latest`
- Author：作者名称，用在类注释中，默认为当前本机登录的用户名，可自定义
- package：实体类中的 package 部分，默认会根据 Path 值自动解析，如果解析错误可自定义，形如 `com.example.ui.view`
- import：实体类中的类 import 部分，多个值用英文逗号隔开，例如 `java.util.List,java.util.TreeMap,com.example.inter.A`
- implements：实体类中要实现的接口，该接口必须在 import 字段中已被引入，例如要实现 import 字段中引入的 `com.example.inter.A` 接口，此处只需填入 `A` 即可（事实上如果在此处填入完整类路径会导致解析错误）
- root node：指定开始解析 JSONObject 的根节点，如果不填则解析整个从 URL 返回的 JSONObject ，例如从 URL 返回的 JSONObject 为 a ，而我们需要的数据位于 a 的 data 元素内，则可以在该字段内填入 `data`
- generator：勾选此项后会在 java 实体类中将每个字段对应的返回值以注释的形式附在字段后，例如勾选了此项后生成的实体类中字段声明会是这样 `public long userId;  //eg: 27639372 `

**以上参数中 Path、URL、Author 和 package 为必填参数，其中 Author 和 package 默认会自动生成。剩余三项如果不需要可以留空。**

#### 示例  
现在我们需要访问接口并生成一系列实体类在项目的 com.moxun.generator.gen 目录下，生成的每一个类都需要实现 com.moxun.generator.inter.A 接口  

1. 点击插件图标 ![ICON](http://gitlab.alibaba-inc.com/moxun.ljf/JsonModelGenerator/raw/master/src/icons/icon.gif) 启动插件  
2. 填入参数如下  
![图2](http://gitlab.alibaba-inc.com/moxun.ljf/JsonModelGenerator/raw/master/img/2.png)  
  
3. 点击 Generator   
4. 生成成功，插件会更新 IDE 的虚拟文件系统，让 IDE 发现生成的文件并在编辑器中打开文件，生成的其中一个文件如下  
![图3](http://gitlab.alibaba-inc.com/moxun.ljf/JsonModelGenerator/raw/master/img/3.png)  
**Excited!**

## 一些不足  
1. 不能自动生成 getter 和 setter （这个要实现其实很简单，只是目前用不到所以没有加上）
2. 不能将接口实现方法的返回值加入 import 部分（例如此处返回值 String 被声明成了 java.lang.String ），因为需要实现接口的场景较少所以没做  

## 结语  
你还在对着 json 串一个个抠字段写 model 么？ 不要再让变量名写错一个字母导致出现灵异错误的事情发生，快装上这个插件吧！毕竟它的 ICON 也是这么提神醒脑，蛤蛤