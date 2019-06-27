# springboot插件式开发框架

#### 介绍
该框架主要是集成于springboot项目，用于开发插件式应用的集成框架。

#### 核心功能
1. 配置式插拔于springboot项目。
2. 在springboot上可以插件式开发, 扩展性极强, 可以针对不同项目开发不同插件, 进行不同部署。
3. 在插件应用模块上可以任意使用spring注解式进行依赖注入、可以开发controller接口、也可以遵循主程序提供的插件接口开发任意扩展功能。
4. 插件可以自定义配置文件。目前只支持yml文件。
5. 支持上传插件和插件配置文件到服务器, 并且无需重启主程序, 动态部署插件、更新插件。
6. 支持查看插件运行状态, 查看插件安装位置。
7. 无需重启主程序, 动态的安装插件、卸载插件、启用插件、停止插件、备份插件、删除插件。

#### 软件架构
待完善
软件架构说明


#### 环境支持
1. jdk1.8+
2. apache maven 3.6

#### maven仓库地址

https://mvnrepository.com/artifact/com.gitee.starblues/springboot-plugin-framework

#### 主程序集成步骤
1. 在主程序中新增maven依赖包

```xml
<dependency>
    <groupId>com.gitee.starblues</groupId>
    <artifactId>springboot-plugin-framework</artifactId>
    <version>${springboot-plugin-framework.version}</version>
</dependency>
```

2. 定义配置

    实现 **com.plugin.development.integration.IntegrationConfiguration** 接口。

```java
import com.plugin.development.integration.*;
import org.pf4j.RuntimeMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "plugin")
public class PluginArgConfiguration implements IntegrationConfiguration {

    /**
     * 运行模式
     *  开发环境: development、dev
     *  生产/部署 环境: deployment、prod
     */
    @Value("${runMode:dev}")
    private String runMode;

    /**
     * 插件的路径
     */
    @Value("${pluginPath:plugins}")
    private String pluginPath;

    /**
     * 插件文件的路径
     */
    @Value("${pluginConfigFilePath:pluginConfigs}")
    private String pluginConfigFilePath;


    @Override
    public RuntimeMode environment() {
        return RuntimeMode.byName(runMode);
    }

    @Override
    public String pluginPath() {
        return pluginPath;
    }

    @Override
    public String pluginConfigFilePath() {
        return pluginConfigFilePath;
    }

    /**
     * 重写上传插件包的临时存储路径。只适用于生产环境
     * @return
     */
    @Override
    public String uploadTempPath() {
        return "temp";
    }

    /**
     * 重写插件备份路径。只适用于生产环境
     * @return
     */
    @Override
    public String backupPath() {
        return "backupPlugin";
    }
    
}
```

配置说明:

    runMode：运行项目时的模式。分为开发环境(dev)、生产环境(prod)
    pluginPath: 插件的路径。开发环境建议直接配置为插件模块的父级目录。例如: plugins。
    pluginConfigFilePath: 在生产环境下, 插件的配置文件路径。在生产环境下， 请将所有插件使用到的配置文件统一放到该路径下管理。
    uploadTempPath: 上传插件包时使用。上传插件包存储的临时路径。默认 temp(相对于主程序jar路径)
    backupPath: 备份插件包时使用。备份插件包的路径。默认: backupPlugin(相对于主程序jar路径)
    
    

3. 配置集成bean
  
```
import com.plugin.development.integration.*;
import com.plugin.development.integration.initialize.AutoPluginInitializer;
import com.plugin.development.integration.initialize.PluginInitializer;
import org.pf4j.PluginException;
import org.pf4j.PluginManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PluginBeanConfig {

    /**
     * 通过默认的集成工厂返回 PluginManager
     * @param integrationConfiguration 集成的配置文件
     * @return
     * @throws PluginException
     */
    @Bean
    public PluginManager pluginManager(IntegrationConfiguration integrationConfiguration) throws PluginException {
        IntegrationFactory integrationFactory = new DefaultIntegrationFactory();
        return integrationFactory.getPluginManager(integrationConfiguration);
    }

    /**
     * 定义默认的插件应用。使用可以注入它操作插件。
     * @return
     */
    @Bean
    public PluginApplication pluginApplication(){
        return new DefaultPluginApplication();
    }

    /**
     * 初始化插件。此处定义可以在系统启动时自动加载插件。
     *  如果想手动加载插件, 则可以使用 com.plugin.development.integration.initialize.ManualPluginInitializer 来初始化插件。
     * @param pluginApplication
     * @return
     */
    @Bean
    public PluginInitializer pluginInitializer(PluginApplication pluginApplication){
        return new AutoPluginInitializer(pluginApplication);
    }

}

```

#### 插件包集成步骤

1. 插件包pom.xml配置说明


以 `<scope>provided</scope>` 方式引入springboot-plugin-framework包

```xml
<dependency>
    <groupId>com.gitee.starblues</groupId>
    <artifactId>springboot-plugin-framework</artifactId>
    <version>${springboot-plugin-framework.version}</version>
    <scope>provided</scope>
</dependency>
```

定义打包配置.主要用途是将 `Plugin-Id、Plugin-Version、Plugin-Provider、Plugin-Class、Plugin-Dependencies`的配置值定义到`META-INF\MANIFEST.MF`文件中
```xml
<properties>
    <plugin.id>springboot-plugin-example-plugin1</plugin.id>
    <plugin.class>com.plugin.example.plugin1.DefinePlugin</plugin.class>
    <plugin.version>${project.version}</plugin.version>
    <plugin.provider>StarBlues</plugin.provider>
    <plugin.dependencies></plugin.dependencies>

    <java.version>1.8</java.version>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>

    <maven-compiler-plugin.version>3.7.0</maven-compiler-plugin.version>
    <maven-assembly-plugin.version>3.1.1</maven-assembly-plugin.version>
    <springboot-plugin-framework.version>1.0-SNAPSHOT</springboot-plugin-framework.version>
</properties>
<build>
    <plugins>
        
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>${maven-compiler-plugin.version}</version>
            <configuration>
                <source>${java.version}</source>
                <target>${java.version}</target>
                <encoding>${project.build.sourceEncoding}</encoding>
            </configuration>
        </plugin>

        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-assembly-plugin</artifactId>
            <version>${maven-assembly-plugin.version}</version>
            <configuration>
                <descriptorRefs>
                    <descriptorRef>jar-with-dependencies</descriptorRef>
                </descriptorRefs>
                <archive>
                    <manifest>
                        <addDefaultImplementationEntries>true</addDefaultImplementationEntries>
                        <addDefaultSpecificationEntries>true</addDefaultSpecificationEntries>
                    </manifest>
                    <manifestEntries>
                        <Plugin-Id>${plugin.id}</Plugin-Id>
                        <Plugin-Version>${plugin.version}</Plugin-Version>
                        <Plugin-Provider>${plugin.provider}</Plugin-Provider>
                        <Plugin-Class>${plugin.class}</Plugin-Class>
                    </manifestEntries>
                </archive>
            </configuration>
            <executions>
                <execution>
                    <id>make-assembly</id>
                    <phase>package</phase>
                    <goals>
                        <goal>single</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

2. 在插件一级目录下新建plugin.properties文件(用于开发环境)
新增如下内容(属性值同步骤1中pom.xml定义的`manifestEntries`属性一致):
```
plugin.id=springboot-plugin-example-plugin2
plugin.class=com.plugin.example.plugin2.DefinePlugin
plugin.version=1.0-SNAPSHOT
plugin.provider=StarBlues
```

配置说明:
```
plugin.id: 插件id
plugin.class: 插件实现类。见步骤3说明
plugin.version: 插件版本
plugin.provider: 插件作者
```
    
3. 继承 `com.plugin.development.realize.BasePlugin` 包
``` java
import com.plugin.development.realize.BasePlugin;
import org.pf4j.PluginWrapper;

public class DefinePlugin extends BasePlugin {
    public DefinePlugin(PluginWrapper wrapper) {
        super(wrapper);
    }
    
    /**
     * 此项是重写的。也可以不用定义。则会默认当前类的包
     * @return
     */
    @Override
    protected String scanPackage() {
        return "com.plugin.example.plugin1";
    }
}
```

并且将该类全路径定义在步骤1和2的plugin.class属性中。


#### 使用说明
1. 插件中要使用主程序中Spring容器管理的bean

以`<scope>provided</scope>`作用范围在插件中引入主程序依赖
``` xml
<dependency>
    <groupId>com.gitee.starblues</groupId>
    <artifactId>plugin-example-start</artifactId>
    <version>1.0-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>

```

在要注入主程序bean的类上加入注解 `@Component、@ApplyMainBean`
注入bean时, 请使用`@Autowired(required = false)`

例如:
```java
import com.plugin.development.annotation.ApplyMainBean;
import com.plugin.example.start.config.PluginArgConfiguration;
import com.plugin.example.start.plugin.ConsoleName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ApplyMainBean
public class ConsoleNameImpl implements ConsoleName {

    @Autowired(required = false)
    private PluginArgConfiguration pluginArgConfiguration;

    @Override
    public String name() {
        return "My name is Plugin1" + "; pluginArgConfiguration :" + pluginArgConfiguration.toString();
    }
}

```

2. 插件中定义Controller例子。同springboot一致。注意每个插件和主程序中的RequestMapping不要重复。
```java
import com.plugin.example.plugin1.config.PluginConfig;
import com.plugin.example.plugin1.service.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/plugin1")
public class HelloPlugin1 {

    @Autowired
    private HelloService helloService;

    @Autowired
    private PluginConfig pluginConfig;

    @GetMapping
    public String sya(){
        return "hello plugin1 example";
    }

    @GetMapping("config")
    public String getConfig(){
        return pluginConfig.toString();
    }


    @GetMapping("serviceConfig")
    public String getServiceConfig(){
        return helloService.getPluginConfig().toString();
    }

    @GetMapping("service")
    public String getService(){
        return helloService.sayService2();
    }

}
```

3. 插件中定义配置文件。

在配置文件类上加入注解`@Component、@ConfigDefinition("配置文件名")`

例如:
```java
import com.plugin.development.annotation.ConfigDefinition;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;


@Component
@ConfigDefinition("plugin1.yml")
public class PluginConfig {

    private String name;
    private String plugin;
    private Set<String> setString;
    private List<Integer> listInteger;

    private String defaultValue = "defaultValue";

    private SubConfig subConfig;

}

配置文件(plugin1.yml):

name: plugin1
plugin: examplePlugin1
setString:
  - set1
  - set2
listInteger:
  - 1
  - 2
  - 3
subConfig:
  subName: subConfigName

```

在配置文件映射的bean中, 必须加入get set方法, 此处为了展示, 没有引入get set方法。推荐使用lombok)

*注意:*

*在开发环境：配置文件必须放在resources目录下。并且@ConfigDefinition("plugin1.yml")中定义的文件名和resources下配置的文件名一致。*

*在生产环境: 该文件存放在`pluginConfigFilePath`配置的目录下。*

4. 在主程序中获取插件的bean。

通过使用 PluginApplication 获取 PluginUser实现类，然后操作。


通过接口获取插件中所有的实现类：

    调用PluginUser->getSpringDefineBeansOfType方法获取。例如：Map<String, ConsoleName> consoleNameMap = pluginUser.getSpringDefineBeansOfType(ConsoleName.class);

通过beanName获取插件中的类：

    调用PluginUser->getSpringDefineBean方法获取。例如ConsoleName consoleNameImpl consoleNameMap = pluginUser.getSpringDefineBeansOfType(
    'com.plugin.example.plugin1.service.ConsoleNameImpl');

5. 部署插件

windows环境下运行: package.bat

linux、mac 环境下运行: package.sh
   
#### 开发环境目录结构
见 `plugin-example` 案例

建议给每个插件定义个父级 pom.xml。

例如:
``` xml
<groupId>com.gitee.starblues</groupId>
<artifactId>plugin-example-plugin-parent</artifactId>
<version>1.0-SNAPSHOT</version>
<packaging>pom</packaging>

<modules>
    <module>plugin-example-plugin1</module>
    <module>plugin-example-plugin2</module>
</modules>

<properties>
    <!-- 子类覆盖该配置 -->
    <plugin.id/>
    <plugin.class/>
    <plugin.version/>
    <plugin.provider/>

    <java.version>1.8</java.version>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>

    <maven-compiler-plugin.version>3.7.0</maven-compiler-plugin.version>
    <maven-assembly-plugin.version>3.1.1</maven-assembly-plugin.version>
    <springboot-plugin-framework.version>1.0-SNAPSHOT</springboot-plugin-framework.version>
</properties>

<dependencies>
    <dependency>
        <groupId>com.gitee.starblues</groupId>
        <artifactId>springboot-plugin-framework</artifactId>
        <version>${springboot-plugin-framework.version}</version>
        <scope>provided</scope>
    </dependency>

    <dependency>
        <groupId>com.gitee.starblues</groupId>
        <artifactId>plugin-example-start</artifactId>
        <version>${project.version}</version>
        <scope>provided</scope>
    </dependency>

</dependencies>

<build>
    <plugins>
        
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>${maven-compiler-plugin.version}</version>
            <configuration>
                <source>${java.version}</source>
                <target>${java.version}</target>
                <encoding>${project.build.sourceEncoding}</encoding>
            </configuration>
        </plugin>

        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-assembly-plugin</artifactId>
            <version>${maven-assembly-plugin.version}</version>
            <configuration>
                <descriptorRefs>
                    <descriptorRef>jar-with-dependencies</descriptorRef>
                </descriptorRefs>
                <archive>
                    <manifest>
                        <addDefaultImplementationEntries>true</addDefaultImplementationEntries>
                        <addDefaultSpecificationEntries>true</addDefaultSpecificationEntries>
                    </manifest>
                    <manifestEntries>
                        <Plugin-Id>${plugin.id}</Plugin-Id>
                        <Plugin-Version>${plugin.version}</Plugin-Version>
                        <Plugin-Provider>${plugin.provider}</Plugin-Provider>
                        <Plugin-Class>${plugin.class}</Plugin-Class>
                    </manifestEntries>
                </archive>
            </configuration>
            <executions>
                <execution>
                    <id>make-assembly</id>
                    <phase>package</phase>
                    <goals>
                        <goal>single</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

插件继承该父类pom.xml

例如:
```xml
<modelVersion>4.0.0</modelVersion>

<parent>
    <groupId>com.gitee.starblues</groupId>
    <artifactId>plugin-example-plugin-parent</artifactId>
    <version>1.0-SNAPSHOT</version>
    <relativePath>../pom.xml</relativePath>
</parent>

<artifactId>plugin-example-plugin1</artifactId>
<version>1.0-SNAPSHOT</version>
<packaging>jar</packaging>

<properties>
    <plugin.id>springboot-plugin-example-plugin1</plugin.id>
    <plugin.class>com.plugin.example.plugin1.DefinePlugin</plugin.class>
    <plugin.version>${project.version}</plugin.version>
    <plugin.provider>StarBlues</plugin.provider>

    <gson.version>2.8.2</gson.version>
</properties>

<dependencies>

    <dependency>
        <groupId>com.google.code.gson</groupId>
        <artifactId>gson</artifactId>
        <version>${gson.version}</version>
    </dependency>

</dependencies>
```

#### 生产环境目录结构

```
-main.jar

-main.yml

-plugins
  -plugin1.jar
  -plugin2.jar
  
-pluginFile
  -plugin1.yml
  -plugin2.yml
```

#### 开发环境建议配置

建议定义一个用于启动的pom.xml。**这样既可以解决在开发环境下可以加载插件中的依赖包、也可以解决在启动时无法自动编译插件包的问题。**

例如:
```xml
<modelVersion>4.0.0</modelVersion>

<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.0.3.RELEASE</version>
    <relativePath/>
</parent>

<groupId>com.gitee.starblues</groupId>
<artifactId>plugin-example-runner</artifactId>
<version>1.0-SNAPSHOT</version>
<packaging>pom</packaging>

<dependencies>

    <dependency>
        <groupId>com.gitee.starblues</groupId>
        <artifactId>plugin-example-start</artifactId>
        <version>${project.version}</version>
    </dependency>

    <dependency>
        <groupId>com.gitee.starblues</groupId>
        <artifactId>plugin-example-plugin1</artifactId>
        <version>${project.version}</version>
        <scope>compile</scope>
    </dependency>

    <dependency>
        <groupId>com.gitee.starblues</groupId>
        <artifactId>plugin-example-plugin2</artifactId>
        <version>${project.version}</version>
        <scope>compile</scope>
    </dependency>

</dependencies>

```

该启动的pom.xml依赖主程序、插件程序(以`<scope>compile</scope>`方式引入)。

运行配置(idea):

Working directory : D:\xx\xx\springboot-plugin-framework-parent\plugin-example

Use classpath of module: plugin-exampe-runner

勾选: Include dependencies with "Provided" scope

#### 注意事项

**1. 如果没有按照开发环境建议配置, 则在插件中代码编写完后, 请保证在class文件下的类都是最新编译的, 再运行主程序, 否则会导致运行的插件代码不是最新的。**

**2.如果启动时插件没有加载。请检查配置文件中的 pluginPath**

    如果pluginPath 配置为相当路径，请检查是否是相对于当前工作环境的目录。

    如果pluginPath配置为绝对路径，请检查路径是否正确。
    
#### 版本更新

##### 1.1 版本
**1. 新增插件注册、卸载监听器。

**2. 新增可通过 PluginUser 获取插件中实现主程序中定义的接口的实现类。

**3. 新增插件注册、卸载时监听时, 可手动刷新接口定义的实现Bean的机制。继承com.plugin.development.context.refresh.AbstractPluginSpringBeanRefresh 或者 com.plugin.development.context.refresh.AbstractSpringBeanRefresh 即可实现。 

