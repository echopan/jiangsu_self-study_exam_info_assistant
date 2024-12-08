package com.echo.examinfo;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author Echo Pan
 * @version 1.0
 * @date 2024/11/25 6:48
 * @description 数据加密
 */
class Encrypt4RSA {
    private static final String windowCreationScript = "var window = {" +
            "    location: { href: 'http://example.com' }," +
            "    document: {" +
            "        documentElement: { nodeName: 'HTML', nodeType: 1 }," +
            "        body: {" +
            "            childNodes: []," +
            "            appendChild: function(child) { this.childNodes.push(child); }," +
            "            getElementsByTagName: function(tagName) {" +
            "                return this.childNodes.filter(function(node) {" +
            "                    return node.nodeName === tagName.toUpperCase();" +
            "                });" +
            "            }," +
            "            getElementById: function(id) {" +
            "                return this.childNodes.find(function(node) {" +
            "                    return node.getAttribute('id') === id;" +
            "                });" +
            "            }" +
            "        }," +
            "        createElement: function(tagName) {" +
            "            var element = {" +
            "                nodeName: tagName.toUpperCase()," +
            "                childNodes: []," +
            "                attributes: {}," +
            "                appendChild: function(child) { this.childNodes.push(child); }," +
            "                setAttribute: function(name, value) { this.attributes[name] = value; }," +
            "                getAttribute: function(name) { return this.attributes[name]; }," +
            "                removeAttribute: function(name) { delete this.attributes[name]; }," +
            "                _innerHTML: ''," +
            "                getElementsByTagName: function(tagName) {" +
            "                    return this.childNodes.filter(function(node) {" +
            "                        return node.nodeName === tagName.toUpperCase();" +
            "                    });" +
            "                }," +
            "                createElement: function(tagName) {" +
            "                    return this.ownerDocument.createElement(tagName);" +
            "                }" +
            "            };" +
            "            Object.defineProperty(element, 'innerHTML', {" +
            "                get: function() { return this._innerHTML; }," +
            "                set: function(html) {" +
            "                    this._innerHTML = html;" +
            "                    var parser = function(html) {" +
            "                        var elements = [];" +
            "                        var regex = /<([a-zA-Z][^>]*)>(.*?)<\\/\\\\1>/g;" +
            "                        var match;" +
            "                        while ((match = regex.exec(html)) !== null) {" +
            "                            var el = this.createElement(match[1]);" +
            "                            el.innerHTML = match[2];" +
            "                            elements.push(el);" +
            "                        }" +
            "                        return elements;" +
            "                    }.bind(this);" +
            "                    this.childNodes = parser.call(this, html);" +
            "                }" +
            "            });" +
            "            return element;" +
            "        }," +
            "        createTextNode: function(text) { return { nodeValue: text }; }," +
            "        createDocumentFragment: function() {" +
            "            return {" +
            "                childNodes: []," +
            "                appendChild: function(child) { this.childNodes.push(child); }" +
            "            };" +
            "        }," +
            "        addEventListener: function(type, listener) {" +
            "            if (!this.eventListeners) this.eventListeners = {};" +
            "            if (!this.eventListeners[type]) this.eventListeners[type] = [];" +
            "            this.eventListeners[type].push(listener);" +
            "        }," +
            "        dispatchEvent: function(event) {" +
            "            if (this.eventListeners && this.eventListeners[event.type]) {" +
            "                this.eventListeners[event.type].forEach(function(listener) {" +
            "                    listener(event);" +
            "                });" +
            "            }" +
            "        }," +
            "        removeEventListener: function(type, listener) {" +
            "            if (this.eventListeners && this.eventListeners[type]) {" +
            "                this.eventListeners[type] = this.eventListeners[type].filter(function(l) {" +
            "                    return l !== listener;" +
            "                });" +
            "            }" +
            "        }," +
            "        nodeType: 9" +
            "    }," +
            "    defaultView: this," +
            "    ownerDocument: function() { return this.document; }," +
            "    navigator: {" +
            "        appName: 'Netscape'," +
            "        userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3'" +
            "    }," +
            "};" +
            "window.addEventListener = function(type, listener) {" +
            "    if (!this.eventListeners) this.eventListeners = {};" +
            "    if (!this.eventListeners[type]) this.eventListeners[type] = [];" +
            "    this.eventListeners[type].push(listener);" +
            "};" +
            "window.removeEventListener = function(type, listener) {" +
            "    if (this.eventListeners && this.eventListeners[type]) {" +
            "        this.eventListeners[type] = this.eventListeners[type].filter(function(l) {" +
            "            return l !== listener;" +
            "        });" +
            "    }" +
            "};" +
            "window.dispatchEvent = function(event) {" +
            "    if (this.eventListeners && this.eventListeners[event.type]) {" +
            "        this.eventListeners[event.type].forEach(function(listener) {" +
            "            listener(event);" +
            "        });" +
            "    }" +
            "};" +
            "window.setTimeout = function(callback, delay) {" +
            "    setTimeout(function() {" +
            "        callback.call(window);" +
            "    }, delay);" +
            "};" +
            "window.clearTimeout = function(timeoutId) {" +
            "    clearTimeout(timeoutId);" +
            "};" +
            "window.setInterval = function(callback, interval) {" +
            "    setInterval(function() {" +
            "        callback.call(window);" +
            "    }, interval);" +
            "};" +
            "window.clearInterval = function(intervalId) {" +
            "    clearInterval(intervalId);" +
            "};" +
            "window.requestAnimationFrame = function(callback) {" +
            "    requestAnimationFrame(function() {" +
            "        callback.call(window);" +
            "    });" +
            "};" +
            "window.cancelAnimationFrame = function(requestId) {" +
            "    cancelAnimationFrame(requestId);" +
            "};" +
            "window.localStorage = {" +
            "    data: {}," +
            "    getItem: function(key) {" +
            "        return this.data[key];" +
            "    }," +
            "    setItem: function(key, value) {" +
            "        this.data[key] = value;" +
            "    }," +
            "    removeItem: function(key) {" +
            "        delete this.data[key];" +
            "    }," +
            "    clear: function() {" +
            "        this.data = {};" +
            "    }" +
            "};" +
            "window.sessionStorage = {" +
            "    data: {}," +
            "    getItem: function(key) {" +
            "        return this.data[key];" +
            "    }," +
            "    setItem: function(key, value) {" +
            "        this.data[key] = value;" +
            "    }," +
            "    removeItem: function(key) {" +
            "        delete this.data[key];" +
            "    }," +
            "    clear: function() {" +
            "        this.data = {};" +
            "    }" +
            "};" +
            "window.atob=function(base64String) {\n" +
            "    var base64Chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=';\n" +
            "    var padding = base64String.length % 4;\n" +
            "    if (padding > 0) {\n" +
            "        base64String += new Array(5 - padding).join('=');\n" +
            "    }\n" +
            "\n" +
            "    var bytes = [];\n" +
            "    for (var i = 0; i < base64String.length; i += 4) {\n" +
            "        var charCode1 = base64Chars.indexOf(base64String.charAt(i));\n" +
            "        var charCode2 = base64Chars.indexOf(base64String.charAt(i + 1));\n" +
            "        var charCode3 = base64Chars.indexOf(base64String.charAt(i + 2));\n" +
            "        var charCode4 = base64Chars.indexOf(base64String.charAt(i + 3));\n" +
            "\n" +
            "        bytes.push((charCode1 << 2) | (charCode2 >> 4));\n" +
            "        if (charCode3 !== 64) {\n" +
            "            bytes.push(((charCode2 & 15) << 4) | (charCode3 >> 2));\n" +
            "        }\n" +
            "        if (charCode4 !== 64) {\n" +
            "            bytes.push(((charCode3 & 3) << 6) | charCode4);\n" +
            "        }\n" +
            "    }\n" +
            "\n" +
            "    return String.fromCharCode.apply(null, bytes);\n" +
            "};" +
            "var navigator = window.navigator;" +
            "var atob=window.atob;";

    public static String encrypt4RSA2(String username, String password, Map<String, String> reqs) {
        String encryptedUsername = "", encryptedPassword = "";
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("nashorn");
        try {
            // 1. 下载 jQuery 和 JSEncrypt 文件
            String jqueryContent = MyHttpRequest.doGet("https://sdata.jseea.cn/resources/tpl_front/script/jquery-1.10.2.min.js", reqs, null).getContent();
            String jsEncryptContent = MyHttpRequest.doGet("https://sdata.jseea.cn/resources/tpl_front/script/jsencrypt.min.js", reqs, null).getContent();
            String checkJSContent = MyHttpRequest.doGet("https://sdata.jseea.cn/resources/tpl_front/script/checkJS.js", reqs, null).getContent();

            // 2. 创建一个模拟的 window 对象
            engine.eval(windowCreationScript);

            // 3. 读取 jQuery 文件
            engine.eval(jqueryContent);

            // 4. 读取 JSEncrypt 文件
            engine.eval(jsEncryptContent);
            engine.eval("var JSEncrypt = window.JSEncrypt;");// 确保 JSEncrypt 挂载到全局对象上

            // 5. 读取 checkJS.js 文件
            engine.eval(checkJSContent);

            // 6. 模拟 DOM 元素
            engine.eval("var simulatedDOM = {};");

            // 7. 重写 $ 函数
            engine.eval("function $(selector) { return simulatedDOM[selector]; }");

            // 8. 设置模拟的 DOM 元素值
            engine.eval("simulatedDOM['#u_username'] = { val: function(value) { if (arguments.length > 0) { this.value = value; } return this.value; }, value: '' };");
            engine.eval("simulatedDOM['#u_password'] = { val: function(value) { if (arguments.length > 0) { this.value = value; } return this.value; }, value: '' };");

            // 9. 设置模拟的 DOM 元素值
            engine.eval("$('#u_username').val('" + username + "');");
            engine.eval("$('#u_password').val('" + password + "');");

            // 10. 调用 encrypt4RSA 函数
            // 获取 Invocable 接口
            engine.eval("encrypt4RSA();");

            // 11. 获取加密后的值
            encryptedUsername = URLEncoder.encode(engine.eval("$('#u_username').val();").toString().replace(" ", "+"), StandardCharsets.UTF_8);
            encryptedPassword = URLEncoder.encode(engine.eval("$('#u_password').val();").toString().replace(" ", "+"), StandardCharsets.UTF_8);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return "u_username=" + encryptedUsername + "&u_password=" + encryptedPassword;

    }

    public static WebPage fakeLogin(String username, String password) {
        WebDriver driver;
        boolean flag = true;
        // 设置 ChromeDriver 路径
        System.setProperty("webdriver.chrome.driver", "./driver/chromedriver.exe");//版本要和浏览器匹配, 目前为129.0.6668.71

        // 配置 Chrome 选项
        ChromeOptions options = new ChromeOptions();
        if (flag) {
            options.addArguments("--headless=old"); // 启用无头模式
        }
        options.addArguments("--disable-gpu"); // 禁用 GPU 加速（某些系统需要）
        // options.addArguments("blink-settings=imagesEnabled=false");//禁用图片
        options.addArguments("--remote-allow-origins=*");  // 允许所有来源

        // 创建 WebDriver 实例
        driver = new ChromeDriver(options);
        WebPage site = new WebPage();
        try {
            // 获取项目目录中的 HTML 文件路径
            //String fileUrl = new File("./blank.html").toURI().toString();

            // 访问页面
            driver.get("https://sdata.jseea.cn/tpl_front/login.html");
            Thread.sleep(1000);
            //下载图片
            WebElement imageElement = driver.findElement(By.cssSelector("img.securitycodeImage"));
            String imageUrl = imageElement.getAttribute("src");
            System.out.println("Image URL: " + imageUrl);
            System.out.println("===开始下载图片");
            MyHttpRequest.doGet(imageUrl, new WebPage(null, set2List(driver.manage().getCookies())).getCooStrMap(), "file").saveFile("./temp/codeMsg.png");
            System.out.println("===图片下载完成");
            // 进行 OCR 识别
            String code = ImageAnalysis.recognizeImage("./temp/codeMsg.png", "./temp/denoise.png");
            // 5. 模拟创建输入元素
            String createElementScript = "$('#u_username').val('" + username + "');$('#u_password').val('" + password + "');$('.securitycode').val('" + code + "');$('.log-btn').click();";
            System.out.println("填写表单:" + createElementScript);
            ((JavascriptExecutor) driver).executeScript(createElementScript);
            Thread.sleep(2000);
            System.out.println("登录中,返回数据:" + driver.getCurrentUrl());
            // System.out.println(driver.getPageSource());
            Map<String, List<String>> header = new HashMap<>();
            header.put("Set-Cookie", set2List(driver.manage().getCookies()));
            site.setHeader(header);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 关闭浏览器
        if (flag) {
            driver.close();
            driver.quit();
        }
        return site;
    }

    public static List<String> set2List(Set<Cookie> cookies) {
        System.out.println("获取网页的cookiesSet:" + cookies);
        List<String> cookieValues = new ArrayList<>();
        for (Cookie cookie : cookies) {
            cookieValues.add(cookie.getName() + "=" + cookie.getValue());
        }
        return cookieValues;
    }

    public static String encrypt4RSA(String username, String password) {
        String encryptedUsername = "", encryptedPassword = "";
        WebDriver driver;

        // 设置 ChromeDriver 路径
        System.setProperty("webdriver.chrome.driver", "./driver/chromedriver.exe");//版本要和浏览器匹配, 目前为129.0.6668.71

        // 配置 Chrome 选项
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=old"); // 启用无头模式
        options.addArguments("--disable-gpu"); // 禁用 GPU 加速（某些系统需要）
        // options.addArguments("blink-settings=imagesEnabled=false");//禁用图片
        options.addArguments("--remote-allow-origins=*");  // 允许所有来源

        // 创建 WebDriver 实例
        driver = new ChromeDriver(options);
        try {
            // 获取项目目录中的 HTML 文件路径
            //String fileUrl = new File("./blank.html").toURI().toString();

            // 访问空白页面
            driver.get("about:blank");

            // 1. 下载 JSEncrypt 和 checkJS.js 文件
            String jQueryContent = MyHttpRequest.doGet("https://sdata.jseea.cn/resources/tpl_front/script/jquery-1.10.2.min.js", null, null).getContent();
            String jsEncryptContent = MyHttpRequest.doGet("https://sdata.jseea.cn/resources/tpl_front/script/jsencrypt.min.js", null, null).getContent();
            String checkJSContent = MyHttpRequest.doGet("https://sdata.jseea.cn/resources/tpl_front/script/checkJS.js", null, null).getContent();

            // 2. 执行 jQuery 文件
            System.out.println("let script=document.createElement('script');script.text=\"" + getPureString(jQueryContent) + "\";document.body.appendChild(script);");
            ((JavascriptExecutor) driver).executeScript("let script=document.createElement('script');script.text=\"" + getPureString(jQueryContent) + "\";document.body.appendChild(script);");

            // 3. 执行 JSEncrypt 文件
            ((JavascriptExecutor) driver).executeScript("let script=document.createElement('script');script.text=\"" + getPureString(jsEncryptContent) + "\";document.body.appendChild(script);");

            // 4. 执行 checkJS.js 文件
            ((JavascriptExecutor) driver).executeScript("let script=document.createElement('script');script.text=\"" + getPureString(checkJSContent) + "\";document.body.appendChild(script);");

            // 5. 模拟创建输入元素
            String createElementScript = "var input = document.createElement('input'); input.id = 'u_username'; input.value = '" + username + "'; document.body.appendChild(input);" +
                    "var input2 = document.createElement('input'); input2.id = 'u_password'; input2.value = '" + password + "'; document.body.appendChild(input2);";
            ((JavascriptExecutor) driver).executeScript(createElementScript);
            // 6. 调用 encrypt4RSA 函数
            System.out.println(((JavascriptExecutor) driver).executeScript("return 'other:'+$('#u_username').val();"));
            ((JavascriptExecutor) driver).executeScript("encrypt4RSA();");

            // 7. 获取加密后的值
            encryptedUsername = (String) ((JavascriptExecutor) driver).executeScript("return $('#u_username').val();");
            encryptedPassword = (String) ((JavascriptExecutor) driver).executeScript("return $('#u_password').val();");

        } catch (Exception e) {
            e.printStackTrace();
        }
        // 关闭浏览器
        driver.close();
        driver.quit();
        return "u_username=" + encryptedUsername + "&password=" + encryptedPassword;
    }

    public static String getPureString(String str) {
        return str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r\n", "\\n");
    }
}
