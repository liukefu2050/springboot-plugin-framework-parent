package com.gitee.starblues.factory;

import com.gitee.starblues.integration.PluginListenerContext;
import org.pf4j.PluginWrapper;


/**
 * 插件注册者接口
 *
 * @author starBlues
 * @version 2.4.0
 */
public interface PluginFactory extends PluginListenerContext {

    /**
     * 工厂初始化
     * @throws Exception 初始化异常
     */
    void initialize() throws Exception;


    /**
     * 注册插件。
     * @param pluginRegistryInfo 插件注册信息
     * @return 插件工厂
     * @throws Exception 插件工厂异常
     */
    PluginFactory registry(PluginRegistryInfo pluginRegistryInfo) throws Exception;


    /**
     * 注销插件。
     * @param pluginId 插件id
     * @return 插件工厂
     * @throws Exception 插件工厂异常
     */
    PluginFactory unRegistry(String pluginId) throws Exception;


    /**
     * 注册或者注销后的构建调用
     * @throws Exception 插件工厂异常
     */
    void build() throws Exception;
}
