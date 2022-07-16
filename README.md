## Goblin
[![Maven Central](https://img.shields.io/maven-central/v/io.github.oitstack/goblin-core)](https://search.maven.org/search?q=goblin)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

**Goblin是一款用于单元测试、自动化测试，解决测试环境隔离问题、提升测试代码编写效率的套件。**

它具有以下能力：
* 依托docker容器化技术，构建单元测试过程中所依赖的中间件环境，减轻单元测试开发人员编写mock的工作量
* 自研了一套轻量级容器编排技术，解决了接口自动化测试过程中，依赖的服务树整体环境统一拉起的问题
* 提供了一套数据自动准备和自动断言工具，方便单元测试开发人员以声明式的方式构造、清理数据和对单测进行断言
* 内置Mock Server，为三方接口mock提供统一编程界面
* 提供了rocketMq、redis cluster的单docker容器解决方案，在节省资源的同时，加速了启动速度
* 提供丰富的扩展点，支持用户自定义中间件扩展、自定义数据集合断言规则等
---
## Quick Start
完整接入样例请参考 [goblin-demo](https://github.com/oitstack/goblin-demo)
### 接入整体流程
![](https://github.com/oitstack/goblin_material/blob/main/Access-steps.png)
### 引入依赖
引入主依赖
```xml
    <dependency>
        <groupId>io.github.oitstack</groupId>
        <artifactId>goblin-all</artifactId>
        <version>${goblin.version}</version>
    </dependency>
```
引入扩展依赖： 例如若项目中依赖到mysql，则引入mysql容器包，其他中间件类似
```xml
    <dependency>
        <groupId>io.github.oitstack</groupId>
        <artifactId>goblin-container-mysql</artifactId>
        <version>${goblin.version}</version>
    </dependency>
```
引入扩展依赖：例如需要使用mysql自动数据构建和断言工具，则引入mysql-unit包，其他类似
```xml
    <dependency>
        <groupId>io.github.oitstack</groupId>
        <artifactId>goblin-mysql-unit</artifactId>
        <version>${goblin.version}</version>
    </dependency>
```
### 编写单元测试
单元测试代码
```Java
@ExtendWith(SpringExtension.class)
@SpringBootTest
@GoblinTest //该注解用于启动Goblin运行环境
public class UserServiceTest {
    @Autowired
    private UserService userService;
 
    @Test
    @UsingDataSet //该注解用于给数据库预置数据，在用例执行之前会读取用于定义的对应用例的数据并插入到数据库.
    public void testGetUserById() {
        Long id = 1L;
        Integer age = 11;
        String name = "花花";
        User user = userService.getById(id);
 
        assertEquals(id, user.getId());
    }
}
```

数据断言工具配置

UserServiceTest#testGetUserById-mysql.xml
```xml
<?xml version='1.0' encoding='UTF-8'?>
<dataset>
    <User id="1" name="花花" age="11"/>
    <User id="2" name="草草" age="18"/>
</dataset>
```
---
## Learn it & Contact us
* 使用文档: <https://github.com/oitstack/goblin/wiki/Goblin%E4%BD%BF%E7%94%A8%E6%96%87%E6%A1%A3>
* 实现原理: <https://github.com/oitstack/goblin/wiki/Goblin%E5%AE%9E%E7%8E%B0%E5%8E%9F%E7%90%86>
* Issues: <https://github.com/oitstack/goblin/issues>
* Gips: <https://github.com/oitstack/goblin/wiki/Goblin-Improvement-Proposal>
* Wechat Group:![20220716131608.png](https://s2.loli.net/2022/07/16/m2oQyp8IxdfX9vE.png)
---
