package com.gitee.starblues.integration.listener;

import com.gitee.starblues.utils.SpringBeanUtils;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.support.GenericApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 插件监听工厂
 *
 * @author starBlues
 * @version 2.4.4
 */
public class PluginListenerFactory implements PluginListener {

    private final List<PluginListener> listeners = new ArrayList<>();
    private final List<Class> listenerClasses = new ArrayList<>();
    private boolean isBuildListenerClass = false;

    @Override
    public void registry(String pluginId, boolean isInitialize) {
        for (PluginListener listener : listeners) {
            try {
                listener.registry(pluginId, isInitialize);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void unRegistry(String pluginId) {
        for (PluginListener listener : listeners) {
            try {
                listener.unRegistry(pluginId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void registryFailure(String pluginId, Throwable throwable) {
        for (PluginListener listener : listeners) {
            try {
                listener.registryFailure(pluginId, throwable);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void unRegistryFailure(String pluginId, Throwable throwable) {
        for (PluginListener listener : listeners) {
            try {
                listener.unRegistryFailure(pluginId, throwable);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 添加监听者
     *
     * @param pluginListener 插件监听者
     */
    public void addPluginListener(PluginListener pluginListener) {
        if (pluginListener != null) {
            listeners.add(pluginListener);
        }
    }

    /**
     * 添加监听者
     *
     * @param pluginListenerClass 插件监听者Class类
     * @param <T>                 插件监听者类。继承 PluginListener
     */
    public <T extends PluginListener> void addPluginListener(Class<T> pluginListenerClass) {
        if (pluginListenerClass != null) {
            synchronized (listenerClasses) {
                listenerClasses.add(pluginListenerClass);
            }
        }
    }

    public <T extends PluginListener> void buildListenerClass(GenericApplicationContext applicationContext) {
        if (applicationContext == null) {
            return;
        }
        synchronized (listenerClasses) {
            if(isBuildListenerClass){
                return;
            }
            // 搜索Spring容器中的监听器
            List<PluginListener> pluginListeners = SpringBeanUtils.getBeans(applicationContext, PluginListener.class);
            if(pluginListeners.isEmpty()){
                pluginListeners = new ArrayList<>();
            }
            for (Class<T> listenerClass : listenerClasses) {
                // 兼容 spring 4.x
                applicationContext.registerBeanDefinition(listenerClass.getName(),
                        BeanDefinitionBuilder.genericBeanDefinition(listenerClass).getBeanDefinition());
                T bean = applicationContext.getBean(listenerClass);
                pluginListeners.add(bean);
            }
            for (PluginListener pluginListener : pluginListeners) {
                boolean find = false;
                for (PluginListener listener : listeners) {
                    if(Objects.equals(listener, pluginListener)){
                        find = true;
                        break;
                    }
                }
                // 防止监听器重复注册
                if(!find){
                    listeners.add(pluginListener);
                }
            }
            listenerClasses.clear();
            isBuildListenerClass = true;
        }
    }

    /**
     * 得到监听者
     *
     * @return 监听者集合
     */
    public List<PluginListener> getListeners() {
        return listeners;
    }

    /**
     * 得到监听者class
     *
     * @return 监听者class集合
     */
    public List<Class> getListenerClasses() {
        return listenerClasses;
    }
}
