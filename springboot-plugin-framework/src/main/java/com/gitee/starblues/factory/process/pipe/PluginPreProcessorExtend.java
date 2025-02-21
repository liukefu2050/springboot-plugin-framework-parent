package com.gitee.starblues.factory.process.pipe;

import com.gitee.starblues.utils.OrderPriority;

/**
 * 单插件处理者扩展接口
 *
 * @author starBlues
 * @version 2.2.5
 */
public interface PluginPreProcessorExtend extends PluginPipeProcessor {

    /**
     * 扩展key
     * @return String
     */
    String key();

    /**
     * 执行顺序
     * @return OrderPriority
     */
    OrderPriority order();

}
