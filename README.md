

###基于百度uid-generator的全局uid微服务

---
####(1)引入依赖
排除mybatis和sl4j相关依赖，使用父项目com.lc.base中规定的依赖
```xml
        <dependency>
            <groupId>com.baidu.fsg</groupId>
            <artifactId>uid-generator</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <exclusions>
                <exclusion>
                    <groupId>org.slf4j</groupId>
                    <artifactId>log4j-over-slf4j</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>ch.qos.logback</groupId>
                    <artifactId>logback-classic</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>org.slf4j</groupId>
                    <artifactId>slf4j-api</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>org.mybatis</groupId>
                    <artifactId>mybatis-spring</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>org.mybatis</groupId>
                    <artifactId>mybatis</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
```
---
####(2)设置数据库连接，和mybatis中mapper映射位置
```properties
spring.datasource.url=jdbc:mysql://***:3306/uid-center?useSSL=false&useUnicode=true&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8
spring.datasource.username=***
spring.datasource.password=***
#mybatis
mybatis.mapper-locations=classpath:mapper/*.xml

```
---
####(3)用配置类LcUidGeneratorConfig创建Bean
DisposableWorkerIdAssigner、CachedUidGenerator
com.baidu.fsg.uid项目中使用xml配置文件的形式创建bean，根据其中的说明在配置类中对bean的属性进行设置
```java
@Configuration
public class LcUidGeneratorConfig {

    @Bean
    public DisposableWorkerIdAssigner disposableWorkerIdAssigner(){
        return new DisposableWorkerIdAssigner();
    }

    @Bean(value = "cachedUidGenerator")
    @ConditionalOnMissingBean
    public CachedUidGenerator cachedUidGenerator() {
        CachedUidGenerator cachedUidGenerator = new CachedUidGenerator();
        cachedUidGenerator.setWorkerIdAssigner(disposableWorkerIdAssigner());

        //以下为可选配置, 如未指定将采用默认值
        cachedUidGenerator.setTimeBits(29);
        cachedUidGenerator.setWorkerBits(21);
        cachedUidGenerator.setSeqBits(13);
        cachedUidGenerator.setEpochStr("2019-09-22");

        //RingBuffer size扩容参数, 可提高UID生成的吞吐量
        //默认:3, 原bufferSize=8192, 扩容后bufferSize= 8192 << 3 = 65536
        cachedUidGenerator.setBoostPower(3);
        // 指定何时向RingBuffer中填充UID, 取值为百分比(0, 100), 默认为50
        // 举例: bufferSize=1024, paddingFactor=50 -> threshold=1024 * 50 / 100 = 512
        // 当环上可用UID数量 < 512时, 将自动对RingBuffer进行填充补全
        //<property name="paddingFactor" value="50"></property>

        //另外一种RingBuffer填充时机, 在Schedule线程中, 周期性检查填充
        //默认:不配置此项, 即不实用Schedule线程. 如需使用, 请指定Schedule线程时间间隔, 单位:秒
        cachedUidGenerator.setScheduleInterval(60L);

        //拒绝策略: 当环已满, 无法继续填充时
        //默认无需指定, 将丢弃Put操作, 仅日志记录. 如有特殊需求, 请实现RejectedPutBufferHandler接口(支持Lambda表达式)
        //<property name="rejectedPutBufferHandler" ref="XxxxYourPutRejectPolicy"></property>
        //cachedUidGenerator.setRejectedPutBufferHandler();
        //拒绝策略: 当环已空, 无法继续获取时 -->
        //默认无需指定, 将记录日志, 并抛出UidGenerateException异常. 如有特殊需求, 请实现RejectedTakeBufferHandler接口(支持Lambda表达式) -->
        //<property name="rejectedTakeBufferHandler" ref="XxxxYourTakeRejectPolicy"></property>
        return cachedUidGenerator;
    }
}
```

每次项目启动会向数据库中新增一条记录，该方法通过WorkerIdAssigner$WorkerNodeDao中的方法实现。
复制com.baidu.fsg.uid中的WORKER_NODE.xml和WorkerNodeDao至本项目mapper目录中并重命名为WorkerNodeMapper。
修改WORKER_NODE.xml中namespace=com.lc.uid.provider.mapper.WorkerNodeMapper
```xml
<mapper namespace="com.lc.uid.provider.mapper.WorkerNodeMapper"></mapper>
```
```java
import com.baidu.fsg.uid.worker.entity.WorkerNodeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
@Mapper
public interface WorkerNodeMapper {
    /**
     * Get {@link WorkerNodeEntity} by node host
     * 
     * @param host
     * @param port
     * @return
     */
    WorkerNodeEntity getWorkerNodeByHostPort(@Param("host") String host, @Param("port") String port);
    /**
     * Add {@link WorkerNodeEntity}
     * 
     * @param workerNodeEntity
     */
    void addWorkerNode(WorkerNodeEntity workerNodeEntity);
}
```
---
####(4)创建service controller
这里可以考虑使用接口实现类。日志由framework-log项目托管（https://github.com/luc13-13/framework-log.git），
每个项目的docker挂载目录下会生成logs目录保存每天的日志

```java
import com.baidu.fsg.uid.UidGenerator;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
/**
 * @author: lucheng
 * @data: 2022/4/27 15:38
 * @version: 1.0
 */
@Service
@Slf4j
public class UidGeneratorService {
    @Resource
    private  UidGenerator uidGenerator;

    public long nextId() {
        long id = this.uidGenerator.getUID();
        log.info(uidGenerator.parseUID(id));
         return id;
    }
}
```
其它服务调用http://ip:port/uid/nextId就可以获得uid
```java
@RestController
@RequestMapping("/uid")
public class ServiceController {
    private final UidGeneratorService uidGenerator;

    public ServiceController(UidGeneratorService uidGenerator) {
        this.uidGenerator = uidGenerator;
    }
    @GetMapping("/nextId")
    public long nextId() {
        return uidGenerator.nextId();
    }
}
```
---
####最后，完整的pom.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>com.lc</groupId>
		<artifactId>base</artifactId>
		<version>1.0-SNAPSHOT</version>
	</parent>
	<groupId>com.lc</groupId>
	<artifactId>uid-provider</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<packaging>jar</packaging>
	<properties>
		<java.version>1.8</java.version>
		<maven.compiler.source>8</maven.compiler.source>
		<maven.compiler.target>8</maven.compiler.target>
	</properties>
	<distributionManagement>
		<repository>
			<id>nexus-snapshot</id>
			<url>http://192.168.223.128:8081/repository/maven-snapshots/</url>
		</repository>
	</distributionManagement>

	<dependencies>
		<dependency>
			<groupId>com.baidu.fsg</groupId>
			<artifactId>uid-generator</artifactId>
			<version>1.0.0-SNAPSHOT</version>
			<exclusions>
				<exclusion>
					<groupId>org.slf4j</groupId>
					<artifactId>log4j-over-slf4j</artifactId>
				</exclusion>
				<exclusion>
					<groupId>ch.qos.logback</groupId>
					<artifactId>logback-classic</artifactId>
				</exclusion>
				<exclusion>
					<groupId>org.slf4j</groupId>
					<artifactId>slf4j-api</artifactId>
				</exclusion>
				<exclusion>
					<groupId>org.mybatis</groupId>
					<artifactId>mybatis-spring</artifactId>
				</exclusion>
				<exclusion>
					<groupId>org.mybatis</groupId>
					<artifactId>mybatis</artifactId>
				</exclusion>
			</exclusions>
		</dependency>

		<dependency>
			<groupId>com.lc</groupId>
			<artifactId>framework-log</artifactId>
			<version>1.0-SNAPSHOT</version>
		</dependency>
	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-war-plugin</artifactId>
				<version>3.2.2</version>
				<configuration>
					<webXml>web\WEB-INF\web.xml</webXml>
				</configuration>
			</plugin>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
				<configuration>
					<excludes>
						<exclude>
							<groupId>org.projectlombok</groupId>
							<artifactId>lombok</artifactId>
						</exclude>
					</excludes>
				</configuration>
			</plugin>
			<plugin>
				<groupId>org.mybatis.generator</groupId>
				<artifactId>mybatis-generator-maven-plugin</artifactId>
				<version>1.3.5</version>
				<configuration>
					<verbose>true</verbose>
					<overwrite>true</overwrite>
				</configuration>
				<dependencies>
					<dependency>
						<groupId>mysql</groupId>
						<artifactId>mysql-connector-java</artifactId>
						<version>5.1.46</version>
					</dependency>
					<dependency>
						<groupId>com.dt</groupId>
						<artifactId>mybatis-generator-config</artifactId>
						<version>1.0.0-SNAPSHOT</version>
					</dependency>
				</dependencies>
			</plugin>
		</plugins>
		<resources>
			<!-- 配置将哪些资源文件(静态文件/模板文件/mapper文件)加载到tomcat输出目录里 -->
			<resource>
				<directory>src/main/java</directory><!--java文件的路径-->
				<includes>
					<include>**/*.*</include>
				</includes>
				<!-- <filtering>false</filtering>-->
			</resource>
			<resource>
				<directory>src/main/resources</directory><!--资源文件的路径-->
				<includes>
					<include>**/*.*</include>
				</includes>
				<!-- <filtering>false</filtering>-->
			</resource>
		</resources>
	</build>
</project>
```
