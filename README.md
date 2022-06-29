# goblin
goblin是一款可以用于单元测试,接口测试以及端到端测试的Java开发框架，为自动化测试提供一站式解决方案。<br>

## goblin 产生的背景
   微服务架构下的测试一般都是采用分层测试，自低向上分别是单元测试，接口测试，和端到端测试。<br>
   goblin最初出现的动力是为了解决传统单元测试存在的种种问题。<br>
![](https://github.com/oitstack/goblin_material/blob/main/Layered-test.png)

### 单元测试指导思想
![](https://github.com/oitstack/goblin_material/blob/main/unit-test-guidelines.png)
#### 隔离方式实现单测
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


#### 穿透方式实现单测
![](https://github.com/oitstack/goblin_material/blob/main/Penetration.png)
优点<br>
1、代码量较少，开发测试占比预计可达1:1<br>
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

## goblin 实现原理
