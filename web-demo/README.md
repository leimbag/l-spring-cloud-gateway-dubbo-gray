# 测试json的log日志里面有自定义跟踪id

1. 启动 dubbo-user-service
2. 启动 web-demo

访问地址：http://localhost:19830/user/getUserNameByShutdown?uid=3

service.log 内容
```
2022-04-02 16:09:32,367 [] [INFO] [http-nio-19830-exec-1raceId] (http-nio-19830-exec-1) o.s.w.s.DispatcherServlet.initServletBean:547 - Completed initialization in 1 ms
2022-04-02 16:09:42,423 [136a92c8f6d041d8a0e414139f3f71b9] [INFO] [http-nio-19830-exec-1raceId] (http-nio-19830-exec-1) c.l.w.d.c.UserController.getUserNameByShutdown:41 - 查询uid=3的用户名, result=TestUser:3
```

service.log.json 内容
```
{"instant":{"epochSecond":1648886972,"nanoOfSecond":367114000},"thread":"http-nio-19830-exec-1","level":"INFO","loggerName":"org.springframework.web.servlet.DispatcherServlet","message":"Completed initialization in 1 ms","endOfBatch":false,"loggerFqcn":"org.apache.commons.logging.LogAdapter$Log4jLog","threadId":1025,"threadPriority":5,"timestamp":"2022-04-02-16:09:32.00367","project":"web-demo","mytid":"${ctx:MYTID}"}
{"instant":{"epochSecond":1648886982,"nanoOfSecond":423931000},"thread":"http-nio-19830-exec-1","level":"INFO","loggerName":"com.leimbag.web.demo.controller.UserController","message":"查询uid=3的用户名, result=TestUser:3","endOfBatch":false,"loggerFqcn":"org.apache.logging.slf4j.Log4jLogger","threadId":1025,"threadPriority":5,"timestamp":"2022-04-02-16:09:42.00423","project":"web-demo","mytid":"136a92c8f6d041d8a0e414139f3f71b9"}
```

log4j2.yml  json日志格式配置
```
      - name: APP_JSON
        ignoreExceptions: false
        fileName: ${LOG_FILE_PATH}/service.log.json
        filePattern: ${LOG_FILE_PATH}/service.log.json.%d{yyyyMMdd}
        JsonLayout:
          compact: true
          eventEol: true
          stacktraceAsString: true
          objectMessageAsJsonObject: true
          KeyValuePair:
            - key: timestamp
              value: $${date:yyyy-MM-dd-HH:mm:ss.SSSSS}
            - key: project
              value: ${PROJECT}
            - key: mytid
              value: $${ctx:MYTID}
        Policies:
          TimeBasedTriggeringPolicy:
            interval: 1
            modulate: true
        DefaultRolloverStrategy:
          Delete:
            basePath: ${LOG_FILE_PATH}
            maxDepth: 1
            IfFileName:
              glob: service.log.json.*
            IfLastModified:
              age: 3d
```

$${ctx:MYTID}  如果没有数据写入到MDC的上下文中，则原文输出，如有有值，输出上下文中的MYTID变量值

参考官方JSON Layout配置

参考地址 https://logging.apache.org/log4j/2.x/manual/layouts.html