

this is a project template used for creating new project with
packages and configurations files
/pom.xml/application.properties/mybatisGeneratorConfig.xml/


```
project pacakge struture:
├─src
│  ├─main/java/com/lc/project/name
│  │  │                  ├─annotation          
│  │  │                  ├─aop
│  │  │                  ├─async
│  │  │                  │  ├─event
│  │  │                  │  ├─listener
│  │  │                  │  ├─model
│  │  │                  │  └─publishier
│  │  │                  ├─binding
│  │  │                  │  └─dto
│  │  │                  ├─bo                      `**业务对象抽象类，将数据库查询结果DO转为BO后再返回给controller**`
│  │  │                  ├─config                  **配置类，返回其他项目的beans**；
│  │  │                  ├─dto                     **数据传输对象，controller将request转为dto至service层**
│  │  │                  ├─enums                   **枚举类**
│  │  │                  ├─exception               **额外的异常类**
│  │  │                  ├─executor                **执行器类**
│  │  │                  ├─convertor               **用于model vo bo dto之间的转换**
│  │  │                  ├─interceptor             **拦截器**
│  │  │                  ├─listener                **监听类**
│  │  │                  ├─mapper                  **数据访问层**
│  │  │                  ├─model                   **数据库访问对象**
│  │  │                  ├─service                 **业务逻辑实现层**
│  │  │                  ├─task                    **提交至线程池的任务**
│  │  │                  ├─utils                   **工具类**
│  │  │                  └─web                     **controller层**
│  │  │                      ├─request             **前端请求**
│  │  │                      └─vo                  **返回给前端的视图对象**
│  │  └─resources
│  │      ├─db
│  │      │  └─migration                           **数据库表结构、内容变更SQL记录**
│  │      └─mapper                                 **mapper.xml**
│  └─test/java/com/lc/peoject/name
```

