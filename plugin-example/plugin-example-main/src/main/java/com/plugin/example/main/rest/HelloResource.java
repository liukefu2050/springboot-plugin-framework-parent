package com.plugin.example.main.rest;

import com.gitee.starblues.integration.PluginApplication;
import com.gitee.starblues.integration.user.PluginUser;
import com.plugin.example.main.plugin.ConsoleName;
import com.plugin.example.main.plugin.ConsoleNameFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 接口演示
 * @author zhangzhuo
 * @version 1.0
 */
@RestController
@RequestMapping(path = "/hello")
public class HelloResource {


    private final PluginUser pluginUser;
    private final ConsoleNameFactory consoleNameFactory;

    public HelloResource(PluginApplication pluginApplication,
                         ConsoleNameFactory consoleNameFactory) {
        this.pluginUser = pluginApplication.getPluginUser();
        this.consoleNameFactory = consoleNameFactory;
    }

    @GetMapping
    public String sya(){
        return "hello spring boot plugin example";
    }

    /**
     * 通过 PluginUser 获取实现类
     * 打印实现接口 com.plugin.example.main.plugin.ConsoleName 的实现类
     * @return 返回所有实现 com.plugin.example.main.plugin.ConsoleName 接口的实现类的 name() 方法的输出
     */
    @GetMapping("/consoleName")
    public String consoleName(){
        StringBuffer stringBuffer = new StringBuffer();
        // 获取到实现该接口的实现类
        List<ConsoleName> consoleNames = pluginUser.getBeans(ConsoleName.class);
        return getConsoleNames(stringBuffer, consoleNames);
    }

    /**
     * 通过 PluginUser 获取实现类
     * 打印实现接口 com.plugin.example.main.plugin.ConsoleName 接口的插件中的实现类
     * @return 返回所有实现 com.plugin.example.main.plugin.ConsoleName 接口的插件中实现类的 name() 方法的输出
     */
    @GetMapping("/pluginConsoleName")
    public String pluginConsoleName(){
        StringBuffer stringBuffer = new StringBuffer();
        // 获取到插件中实现该接口的实现类
        List<ConsoleName> consoleNames = pluginUser.getPluginBeans(ConsoleName.class);
        return getConsoleNames(stringBuffer, consoleNames);
    }

    /**
     * 通过 AbstractPluginSpringBeanRefresh 工厂获取实现类
     * 打印实现接口 com.plugin.example.main.plugin.ConsoleName 的实现类
     * @return 返回所有实现 com.plugin.example.main.plugin.ConsoleName 接口的实现类的 name() 方法的输出
     */
    @GetMapping("/pluginConsoleName2")
    public String pluginConsoleName2(){
        StringBuffer stringBuffer = new StringBuffer();
        List<ConsoleName> consoleNames = consoleNameFactory.getBeans();
        return getConsoleNames(stringBuffer, consoleNames);
    }

    /**
     * 调用接口 name() 方法，并拼接输出
     * @param stringBuffer stringBuffer
     * @param consoleNames 所有 ConsoleName 的实现类
     * @return 拼接的字符串
     */
    private String getConsoleNames(StringBuffer stringBuffer, List<ConsoleName> consoleNames) {
        for (ConsoleName consoleName : consoleNames) {
            stringBuffer.append(consoleName.name())
                    .append("<br/>");
        }
        return stringBuffer.toString();
    }

}
