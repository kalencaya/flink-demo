# README

因为 JDK 采用 17，在**本地**需在启动时设置 `VM参数`（不是环境变量），支持反射访问。部署到阿里云上不需要，因为阿里云在启动脚本中会自己指定。

```shell
--add-opens java.base/java.lang=ALL-UNNAMED
--add-opens java.base/java.lang.reflect=ALL-UNNAMED
```

同时因为部分 flink maven pom.xml 依赖设置为 `provided`，启动时需在 idea 中设置一下包含 `provided`，或者临时修改 pom.xml 中设置为 `compile`

