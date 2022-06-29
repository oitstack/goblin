# goblin
goblin是一款可以用于单元测试,接口测试以及端到端测试的Java开发框架，为自动化测试提供一站式解决方案。<br>

## goblin 产生的背景
### 测试阶段概述
根据测试分层金字塔，我们可以按阶段把测试分为单元测试、接口测试（自动化测试）、端到端测试。<br>
其中<br>
1、单元测试是指对软件中的最小可测试单元进行检查和验证。单元在质量保证中是非常重要的环节，根据测试金字塔原理，越往上层的测试，所需的测试投入比例越大，效果也越差，而单元测试的成本要小的多，也更容易发现问题。<br>
2、自动化测试作为提升测试效能的主要手段之一，在现代化测试工程中极为重要。<br>

然而目前业界在单元测试和自动化测试中都普遍存在一些相通的痛点，goblin框架针对这些痛点，提出了一套自己的解决方案。<br>

![](https://github.com/oitstack/goblin_material/blob/main/Layered-test.png)

### 单元测试痛点
* **所依赖中间件复杂多样**<br>
被测试应用往往依赖非常多的中间件，在测试过程中mock掉全部中间件依赖成本极高，需要对每种中间件进行内存化运行或者个性化mock。
* **外部系统依赖多，协议繁杂**<br>
外部系统依赖较多，接入协议包含http、dubbo、mq等，目前各个业务使用mock框架的选型、mock过程各式各样，新人接手需要研究各种不同的mock框架，学习成本高，工作量大。<br>
* **数据准备和清理花费时间多**<br>
对于测试数据的准备、清理，大家各自一套框架或逻辑，不统一，而且手写代码比较多，工作量大。<br>
### 自动化测试痛点
* **所依赖服务无法真正隔离**<br>
被测试应用往往依赖非常多的三方或者二方应用，这些应用都是公用的环境，同时提供给多套自动化环境甚至普通测试环境使用，多个环境产生的数据相互影响。
* **中心化配置数据无法隔离**
现在很多系统都使用了配置中心，与环境隔离类似，中心配置也无法隔离，这样基本上无法做到在自动化脚本中修改配置以达到测试的目的，因为一旦修改了配置，就会影响好其他环境的测试。
### goblin解决之道
注： 置灰表示该能力待开源<br>
![](https://github.com/oitstack/goblin_material/blob/main/tao-of-goblin.png)

#### goblin单测穿透之道
单元测试指导思想分为两种， 分别是穿透型和分层隔离型。
![](https://github.com/oitstack/goblin_material/blob/main/unit-test-guidelines.png)
##### 隔离方式实现单测
![](https://github.com/oitstack/goblin_material/blob/main/isolation-method.png)
优点<br>
1、测试代码极其轻量、运行速度快<br>
2、符合真正的单元测试原则，可以在断网的情况下运行<br>
3、质量更高<br>
缺点<br>
1、代码量大，依据实际经验开发测试占比预计需要1:3<br>
2、学习曲线较高，存在习惯问题<br>
3、对低复杂度的项目不友好<br>
4、要考虑代码可测性<br>


##### 穿透方式实现单测
![](https://github.com/oitstack/goblin_material/blob/main/Penetration.png)
优点<br>
1、代码量较少，测试占开发比重低
2、学习曲线低，偏向黑盒<br>
缺点<br>
1、整体较重，需启动Spring容器，运行预计需要分钟级别<br>

基于以上对比， goblin选择了穿透方式实现单测<br>

## goblin 快速开始
完整接入样例请参考 [goblin-demo](https://github.com/oitstack/goblin-demo)
### 接入步骤
![](https://github.com/oitstack/goblin_material/blob/main/Access-steps.png)
### 单元测试示例
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
//UserServiceTest#testGetUserById-mysql.xml
<?xml version='1.0' encoding='UTF-8'?>
<dataset>
    <User id="1" name="花花" age="11"/>
    <User id="2" name="草草" age="18"/>
</dataset>
```


## goblin 能力介绍
### 支持单元测试
![](https://github.com/oitstack/goblin_material/blob/main/unit-test.png)

### 支持接口测试
![](https://github.com/oitstack/goblin_material/blob/main/interface-test.png)

### 支持端到端测试
![](https://github.com/oitstack/goblin_material/blob/main/point-to-point-test.png)

### 支持数据的自动准备和断言
注： redis和mongodb后续开源<br>
![](https://github.com/oitstack/goblin_material/blob/main/data-prepare-assert.png)

### 常用中间件的支持
注： 当前仅开源了MySql, 其他的后续开源<br>
![](https://github.com/oitstack/goblin_material/blob/main/supported-middleware.png)


## goblin 实现原理
![](https://github.com/oitstack/goblin_material/blob/main/test-case-lifecycle.png)
