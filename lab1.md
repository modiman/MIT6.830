# 6.830/6.814 Lab 1: SimpleDB

**Assigned: Wed, Feb 24**

**Due: Wed, Mar 10 11:59 PM EDT**

<!--
**Bug Update:** We have a [page](bugs.html) to keep track
of SimpleDB bugs that you or we find. Fixes for bugs/annoyances will also be
posted there. Some bugs may have already been found, so do take a look at the page
to get the latest version/ patches for the lab code.
-->

在 6.830 的实验作业中你将编写一个叫做 SimpleDB 的基础数据库管理系统

本实验中，你将关注于如何实现访问硬盘上数据这一需求的核心模块

在后续实验中，还需要支持查询运算、事务、锁以及并发查询

SimpleDB 使用 Java 实现.

我们提供了一系列没有完全实现的类和接口

你需要写出这些类缺失的代码

然后需要通过一系列基于 Junit 编写的单元测试获得分数

我们还提供了一些虽然不计入分数但能验证你写的代码是否工作的单元测试

我们也鼓励你开发自己专属的单元测试

本文剩余部分描述了 SimpleDB 的基本架构，对如何着手编程给了一些建议
还讨论了如何提交作业

我们强烈建议你尽早开始实验，因为要写的代码确实不少

<!--

##  0.  发现漏洞，保持耐心，赚取糖果


SimpleDB 是一项相对复杂的编程任务
你很可能会发现错误、不一致和不好的地方，
过时或不正确的文件等。

因此，我们要求你以冒险的心态来做这个实验。

 如果有些事情不清楚，甚至是错误的，不要生气

相反，试着自己弄清楚或者给我们发一封友好的电子邮件。

我们承诺收到问题报告后会通过发布bug补丁、重新提交等来帮助解决问题。,

<p>...而且如果你找到了我们代码中的bug
我们将给你块糖(see [Section 3.3](#bugs))!

-->
<!--which you can find [here](bugs.html).</p>-->

## 0. 环境设置

**从下面的课程 GitHub 库下载 lab1 源码以及后续说明
[here](https://github.com/MIT-DB-Class/simple-db-hw-2021).**

这些说明是基于 Athena 和别的 Unix-based 平台 (e.g., Linux, MacOS, etc.)
不过由于代码有 Java 编写，在 windows 应该也能运行
尽管可能不太一样

[Section 1.2](#eclipse) 介绍了如何使用 eclipse 或 idea 编辑代码

## 1. 开始

SimpleDB 使用 [Ant build tool](http://ant.apache.org/) 编译和测试代码
Ant 与[make](http://www.gnu.org/software/make/manual/), 相似，但 build 文件由 XML
编写并且更加适配 Java 代码. 大多数版本的 Linux 自带 Ant. Under Athena
在 `sipb` 里, 你可以在 Athena prompt 输入 `add sipb` 查看.
注意在某些版本的 Athena 中必须同时执行`add -f java`
来设置正确的 Java 编程环境.详情参考 [Athena documentation on using Java](http://web.mit.edu/acs/www/languages.html#Java)

为了在开发过程中帮一手，我们提供了若干一对一的单元测试用来打分
这些测试未必全面，所以不要完全依赖它们验证你的程序是否正确
(6.170 学到的绝活该派上用场了).

为了运行单元测试，使用 `test` 构建目标 :

```
$ cd [project-directory]
$ # run all unit tests
$ ant test
$ # run a specific unit test
$ ant runtest -Dtest=TupleTest
```

你会看到类似下面这样的输出:

```
 build output...

test:
    [junit] Running simpledb.CatalogTest
    [junit] Testsuite: simpledb.CatalogTest
    [junit] Tests run: 2, Failures: 0, Errors: 2, Time elapsed: 0.037 sec
    [junit] Tests run: 2, Failures: 0, Errors: 2, Time elapsed: 0.037 sec

 ... stack traces and error reports ...
```
上面的输出表示编译出现了两处错误
这是因为我们给的代码部分不是完整的
如果你想写自己的单元测试，可以把他们添加到/test目录 


[manual](http://ant.apache.org/manual/). The [Running Ant](http://ant.apache.org/manual/running.html) 获取更多关于ant的操作

下面给出了一些本实验用到的指令

| Command                        | Description                                                    |
| ------------------------------ | -------------------------------------------------------------- |
| ant                            | Build the default target (for simpledb, this is dist).         |
| ant -projecthelp               | List all the targets in `build.xml` with descriptions.         |
| ant dist                       | Compile the code in src and package it in `dist/simpledb.jar`. |
| ant test                       | Compile and run all the unit tests.                            |
| ant runtest -Dtest=testname    | Run the unit test named `testname`.                            |
| ant systemtest                 | Compile and run all the system tests.                          |
| ant runsystest -Dtest=testname | Compile and run the system test named `testname`.              |

如果你正在使用windows系统并且不想从命令行执行测试
可以从eclipse运行
右键点击build.xml可以看到"runtest" "runsystest"等等


### 1.1. 运行一对一测试

我们提供了一系列端对端测试用来打分
这些测试放在test/simpledb/systemtest目录
使用 `systemtest` 构建:

```
$ ant systemtest

 ... build output ...

    [junit] Testcase: testSmall took 0.017 sec
    [junit]     Caused an ERROR
    [junit] expected to find the following tuples:
    [junit]     19128
    [junit]
    [junit] java.lang.AssertionError: expected to find the following tuples:
    [junit]     19128
    [junit]
    [junit]     at simpledb.systemtest.SystemTestUtil.matchTuples(SystemTestUtil.java:122)
    [junit]     at simpledb.systemtest.SystemTestUtil.matchTuples(SystemTestUtil.java:83)
    [junit]     at simpledb.systemtest.SystemTestUtil.matchTuples(SystemTestUtil.java:75)
    [junit]     at simpledb.systemtest.ScanTest.validateScan(ScanTest.java:30)
    [junit]     at simpledb.systemtest.ScanTest.testSmall(ScanTest.java:40)

 ... more error messages ...
```
这表示测试失败了，想要debug,找到错误出现的代码段检查
<p>
如果测试通过，输出如下内容

```
$ ant systemtest

 ... build output ...

    [junit] Testsuite: simpledb.systemtest.ScanTest
    [junit] Tests run: 3, Failures: 0, Errors: 0, Time elapsed: 7.278 sec
    [junit] Tests run: 3, Failures: 0, Errors: 0, Time elapsed: 7.278 sec
    [junit]
    [junit] Testcase: testSmall took 0.937 sec
    [junit] Testcase: testLarge took 5.276 sec
    [junit] Testcase: testRandom took 1.049 sec

BUILD SUCCESSFUL
Total time: 52 seconds
```


<!-- 
C A D D C
B A B D A -->


#### 1.1.1 创建笨蛋表

很可能你想创建自己的测试用例和数据表来测试你的数据库实现

你可以创建任意 <tt>.txt</tt> 文件并使用下面的指令
把它转换成SimpleDB中`HeapFile`格式的 <tt>.dat</tt>文件 


```
$ java -jar dist/simpledb.jar convert file.txt N
```
N是数据的列数
注意file.txt要组织成下面的格式:


```
int1,int2,...,intN
int1,int2,...,intN
int1,int2,...,intN
int1,int2,...,intN
```

... intN 是一个 非负整数


使用`print` 命令查看表中内容:

```
$ java -jar dist/simpledb.jar print file.dat N
```


<a name="eclipse"></a>

### 1.2. 使用IDE

IDEs (Integrated Development Environments) 是图形化软件开发环境，可以帮你管理更庞大的项目
我们提供了以下两种IDE的支持
[Eclipse](http://www.eclipse.org) and [IntelliJ](https://www.jetbrains.com/idea/). 
实验说明中提供的Eclipse基于Java 1.7.
对于IntelliJ, 我们使用Ultimate版,可以申请校园账户使用。我们强烈鼓励你为本项目部署一个IDE


**准备代码仓库**

运行下面的代码为IDE生成项目文件
```
ant eclipse
```

**基于Eclipse设置实验**

- Once Eclipse is installed, start it, and note that the first screen asks you to select a location for your workspace (
  we will refer to this directory as $W). Select the directory containing your simple-db-hw repository.
- In Eclipse, select File->New->Project->Java->Java Project, and push Next.
- Enter "simple-db-hw" as the project name.
- On the same screen that you entered the project name, select "Create project from existing source," and browse to
  $W/simple-db-hw.
- Click finish, and you should be able to see "simple-db-hw" as a new project in the Project Explorer tab on the
  left-hand side of your screen. Opening this project reveals the directory structure discussed above - implementation
  code can be found in "src," and unit tests and system tests found in "test."

**注意:** 本课程假定你使用的是Oracle官方发行的java版本
这是 MacOS和大多数Windows Eclipse 的默认版本;

不过很多版本的Linux默认使用别的替代版本，比如 OpenJDK
. 请从 [Oracle Website](http://www.oracle.com/technetwork/java/javase/downloads/index.html)下载jdk 8 

如果没有更换，后面的实验测试中你会遇到不少错误



**运行独立单元测试和系统测试**

为了运行单元测试和系统测试(都是Junit)
点击左侧的Package Explorer，在"simple-db-hw" 项目下,打开"test"目录. Unit tests
可以从 "simpledb" 包找到, system tests 可以从 "simpledb.systemtests" 包找到.

想要运行测试，选择test (通常被命名为 \*Test.java - 别选TestUtil.java 或 SystemTestUtil.java), 右键, 选择 "Run As," 选择 "JUnit Test."这会打开一个JUnit tab, 告诉你测试状态 

**Running Ant Build Targets**

想要运行诸如"ant test" 或 "ant systemtest"之类的命令,右键build.xml 

选择 "Run As," 然后"Ant Build..." (注意: 选择ellipsis (...), 否则不会出现一系列可以运行的 build targets). 接着, 在下一个屏幕中的"Targets" , 检出你想运行的targets (可能是 "dist" 和一个"test" 或 "systemtest").这些操作应该在控制台进行

**在IntelliJ中设置**

IntelliJ是一个更现代的Java IDE t，更流行也更容易操作。想使用 IntelliJ, 首先安装并打开. 和Eclipse相似, 在 Projects, 选择Open 并找到 项目根路径.双击.project 文件 (这个文件可能会被隐藏),
点击"open as project". IntelliJ 有支持ant的工具窗口。操作说明在[这](https://www.jetbrains.com/help/idea/ant.html),但这并不适合初学者
IntelliJ的更多特性在[这](https://www.jetbrains.com/help/idea/discover-intellij-idea.html)

### 1.3. 实施提示

开始编程之前我们强烈建议你读完整个文档，对高级数据库设计有一个大体的概念


<p>
你要补全还没实现的代码

要补全的地方很好找

你可能需要添加一些私有方法或帮助方法

也可以改变APIs,但确保我们用来[打分的](#grading) test可以运行，然后在writeup中解释清楚你的方案

<p>


除了当前实验需要实现的方法，类和接口里还有一些后续实现的方法，他们被标记成下面的样子
```java
// Not necessary for lab1.
public class Insert implements DbIterator {
```

或者

```Java
public boolean deleteTuple(Tuple t)throws DbException{
        // some code goes here
        // not necessary for lab1
        return false;
        }
```
你提交的代码暂时不用修改这样的方法

<p>
我们建议顺着文档指导你的实现，但你也可能探索出更适合你的实现顺序


**Here's a rough outline of one way you might proceed with your SimpleDB implementation:**

---

- 实现管理元组的相关类, 包括Tuple和TupleDesc. 我们已经为你实现了Field, IntField,
  StringField和 Type. 由于只用支持integer和 (fixed length) string fixed
  length tuples, 它们的实现简单直白
- 实现Catalog (这个很简单).
- 实现BufferPool 构造器 和getPage() 方法
- 实现存取方法，包括 HeapPage 、HeapFile 和相关的 ID类. 他们中很大一部分已经为你写出来了
- 实现运算符 SeqScan，然后通过ScanTest测试，这也是当前实验的最终目标

下面的第二章带你浏览一下每个实现步骤和单元测试的细节

### 1.4. 事务、锁和恢复

纵观所有接口，你会看到很多有关事务、锁和恢复的相关内容，当前实验还不用支持这些特性，但你需要在接口中保留这些参数

因为后续实验需要实现这些。我们提供的测试代码使用的是假的事务ID，该ID被传递给它所运行的查询的操作符；您应该将这个事务ID传递给其他操作符和缓冲池。

## 2. SimpleDB架构和实现知指导

SimpleDB 包含:

- 表示字段、元组和元组架构的类；
- 将谓词和条件应用于元组的类；
- 一个或多个访问方法(例如，堆文件),其将关系存储在盘上，并提供一种迭代通过那些关系的元组的方式；
- 处理元组的运算符类的集合(例如，选择、连接、插入、删除等)。)；
- 在内存中缓存活动元组和页面并处理并发控制和事务的缓冲池(当前实验无需考虑)；
- 存储有关可用表及其模式的信息的目录

SimpleDB不包括许多您可能认为是“数据库”一部分的东西。特别是，SimpleDB没有:

- (在本实验中允许您将查询直接键入SimpleDB)的SQL前端或解析器，。相反，查询是通过将一组操作符链接到一个手工构建的查询计划中来构建的(参见[第2.7节](#query_walkthrough))。我们将提供一个简单的解析器供后面的实验使用。
- 视图
- 除整数和固定长度字符串以外的数据类型。
- (在本实验中)查询优化器。
- (在本实验中)索引

<p>

 在本节的其余部分，我们将描述您需要在本实验中实现的SimpleDB的每个主要组件。您应该使用本次讨论中的`exercises `来指导您的实现。本文档当然不是SimpleDB的完整规范 您将需要决定如何设计和实现系统的各个部分。请注意，对于实验1，除了顺序扫描之外，您不需要实现任何操作符(例如，select、join、project)。您将在未来的实验中添加对其他操作员的支持。

<p>

### 2.1. Database Class

Database类提供对静态对象集合的访问，这些对象是数据库的全局状态。 特别是，这包括访问catalog(数据库中所有表的列表)、缓冲池(当前驻留在内存中的数据库文件页面的集合)和日志文件的方法 , . 在本实验中，您无需考虑日志文件。我们已经为您实现了数据库类。您应该看看这个文件，因为您将需要访问这些对象。

### 2.2. Fields 和 Tuples

SimpleDB中的Tuples非常基础 。  它们由`Field`对象的集合组成，在`元组`中每个字段一个对象. `Field` 是一个实现了同一接口但表示不同数据类型类（整数、定长字符长）`元组`对象由底层访问方法(例如，堆文件或B树)创建，如下一节所述。元组还具有类型(或模式)属性，称为_元组描述符_，由`TupleDesc`对象表示。该对象由`Type`对象的集合组成，元组中每个 `Field`一个对象，每个对象描述相应字段的类型。


### Exercise 1

****实现下列类中的方法**

---

- src/java/simpledb/storage/TupleDesc.java
- src/java/simpledb/storage/Tuple.java

---

此时，您的代码应该通过了单元测试TupleTest和TupleDescTest。但modifyRecordId()应该会失败，因为您还没有实现它。

### 2.3. Catalog

catalog (class `Catalog` in SimpleDB)包含了当前数据库里的表以及它们的schema, 您需要支持

* 添加新表
* 获取特定表的信息。

与每个表相关联的是一个`TupleDesc`对象，它允许操作员确定表中字段的类型和数量。

全局目录是为整个SimpleDB进程分配的`catalog`的单个实例。

可以通过`Database.getCatalog()`方法检索全局目录,与之相似的是buffer pool中的`Database.getBufferPool()`

### Exercise 2

**实现catalog类中的方法:**

---

- src/java/simpledb/common/Catalog.java

---

此时，您的代码应该通过了CatalogTest.

### 2.4. BufferPool

缓冲池(SimpleDB中的类`buffer pool`)负责在内存中缓存最近从磁盘读取的页面。所有操作符都通过缓冲池从磁盘上的各种文件中读写页面。它由固定数量的页面组成，由`BufferPool`构造函数的`numPages`参数定义。在后面的实验中，您将实现回收策略. 对于当前实验，您只需要实现SeqScan操作符使用的构造函数和`BufferPool.getPage()`方法. 缓冲池应该存储最多`numPages`个页面。对于当前实验，如果对页面发出了超过`numPages`个请求，那么您可以直接抛出一个DbException，回收策略的实现部分后面的实验才会涉及。

`Database` 类提供了静态方法 `Database.getBufferPool()`, 它会返回整个 SimpleDB进程拥有的的BufferPool唯一实例

### Exercise 3

**实现 `getPage()` 方法:**

---

- src/java/simpledb/storage/BufferPool.java

---

我们没有为BufferPool提供单元测试。您实现的功能将在后面的HeapFile实现中进行测试。您应该使用“DbFile.readPage”方法来访问DbFile的页面。

<!--
当缓冲池中的页面超过这个数量时，应该在加载下一个页面之前从缓冲池中清除一个页面。回收政策的选择由你决定；没有必要做复杂的事情。
-->

<!--
<p>
请注意,`buffer pool`要求您实现一个`flush _ all _ pages()`方法。这在缓冲池的实际实现中是不需要的。然而，我们需要这个方法来进行测试。你不应该在你的代码中的任何地方调用这个方法。
-->

### 2.5. HeapFile 存取方法

存取方法提供了一种从以特定方式排列的磁盘中读取或写入数据的方法。常见的存取方法包括堆文件(元组的未排序文件)和B树；对于这个任务，您将只实现一个堆文件访问方法，我们已经为您编写了一些代码。

一个`HeapFile`对象被安排成一组页面，每个页面由固定数量的字节组成，用于存储元组, (由常数`BufferPool.DEFAULT_PAGE_SIZE`定义),包括标题。在SimpleDB中，数据库中的每个表都有一个`HeapFile`对象。

`HeapFile`中的每个页面被安排为一组槽，每个槽可以容纳一个元组(SimpleDB中给定表的元组都是相同大小的)。

除了这些槽，每个页面都有一个由位图组成的头，每个元组槽一位。

如果对应于特定元组的位是1，则表明该元组是有效的; 如果为0，则元组无效 (例如已被删除或从未被初始化.) 

`HeapFile`对象的页面是实现`Page`接口的`HeapPage`类型。页面存储在缓冲池中，但由`HeapFile`类读写。



<p>
SimpleDB在磁盘上存储堆文件的格式与它们在内存中存储的格式大致相同。

每个文件由磁盘上连续排列的页面数据组成.

每个页面由一个或多个字节表示头部

后面紧跟真正的页面内容

没个元祖需要8bits数据部分和1bit头部

所以一个页面中的元祖数量计算为

`_tuples per page_ = floor((_page size_ * 8) / (_tuple size_ * 8 + 1))`





 _tuple size_ 是也页面中元组的 bytes数. 



这里的想法是，每个元组在头中需要一个额外的存储位。

我们计算一页中的位数(通过将页大小乘以8) , 并将该数量除以元组中的比特数(包括该额外的报头比特)以获得每页的元组数。 floor运算向下舍入到最接近的整数个元组(我们不想在一个页面上存储部分元组！)

<p>

一旦我们知道了每页的元组数，存储头所需的字节数就是:

<p>
`headerBytes = ceiling(tupsPerPage/8)`

<p>

ceiling运算向上舍入到最接近的整数字节数(我们从不存储少于一个完整字节的头信息。)

<p>

每个字节的低位(最低有效位)代表文件中较早的槽的状态。因此，第一个字节的最低位表示页面中的第一个槽是否在使用中。第一个字节的第二个最低位表示页面中的第二个槽是否正在使用，依此类推。另外，请注意 最后一个字节的高位可能不对应于文件中实际存在的槽，因为槽的数量可能不是8的倍数。还要注意，所有的Java虚拟机都是[big-endian](http://en . Wikipedia . org/wiki/Endianness)。

<p>

### Exercise 4

**实现下列方法:**

---

- src/java/simpledb/storage/HeapPageId.java
- src/java/simpledb/storage/RecordId.java
- src/java/simpledb/storage/HeapPage.java

---

虽然您不会在实验1中直接使用它们，但我们要求您实现< tt>getNumEmptySlots()</tt >和< tt > HeapPage中的isSlotUsed()</tt >。这些需要在页头中推送位。您可能会发现查看以下内容很有帮助 HeapPage或< TT > src/simple db/heapfileencoder . Java </TT >中提供的其他方法来理解 页面布局。

您还需要在页面中的元组上实现一个迭代器，这可能涉及到一个辅助类或数据结构。

此时，您的代码应该通过了HeapPageIdTest、RecordIDTest和HeapPageReadTest中的单元测试。

<p>

在您实现了<tt >堆分页</tt >之后，您将在本实验中为<tt>堆文件</tt >编写方法，以计算文件中的页数并从文件中读取一页。然后，您将能够从存储在磁盘上的文件中获取元组。

### Exercise 5

**实现下列方法:**

---

- src/java/simpledb/storage/HeapFile.java

---

要从磁盘中读取一页，首先需要计算文件中正确的偏移量。提示:您将需要随机访问该文件，以便以任意偏移量读写页面。从磁盘读取页面时，不应调用BufferPool方法。

<p> 
您还需要实现“HeapFile.iterator()”方法，该方法应该遍历HeapFile中每个页面的元组。迭代器必须使用“BufferPool.getPage()”方法来访问“HeapFile”中的页面。该方法将页面加载到缓冲池中，并最终用于(在后面的实验中)实现基于锁定的并发控制和恢复。不要在open()调用时将整个表加载到内存中——这将导致非常大的表出现内存不足的错误。

<p>
    此时，您的代码应该通过了HeapFileReadTest中的单元测试。



### 2.6. 操作符

操作符负责查询计划的实际执行。它们实现关系的操作 代数。在SimpleDB中，运算符是基于迭代器的；每个运算符都实现“DbIterator”接口。

<p>

通过将较低级别的操作符传递到较高级别的操作符的构造函数中，即通过“将它们链接在一起”，操作符被连接到一个计划中计划叶子上的特殊访问方法操作符负责从磁盘读取数据(因此它们下面没有任何操作符)。

<p>

在计划的顶部，与SimpleDB交互的程序简单地调用根操作符上的‘get next ’;然后，该操作符对其子操作符调用“getNext ”,依此类推，直到这些叶操作符被调用。它们从磁盘获取元组，并沿树向上传递它们(作为“getNext”的返回参数)；元组以这种方式在计划中向上传播，直到它们在根处被输出，或者被计划中的另一个操作符组合或拒绝。

<p>
<!--
对于实现“插入”和“删除”查询的计划，最顶层的操作符是一个特殊的“插入”或“删除”操作符，用于修改磁盘上的页面。这些操作符向用户级程序返回一个包含受影响元组的计数的元组。

<p>
-->

对于本实验，您只需要实现一个SimpleDB操作符。

### Exercise 6.

**实现下列方法:**

---

- src/java/simpledb/execution/SeqScan.java

---

This operator sequentially scans all of the tuples from the pages of the table specified by the `tableid` in the
constructor. This operator should access tuples through the `DbFile.iterator()` method.

<p>At this point, you should be able to complete the ScanTest system test. Good work!
您将在后续实验中填写其他操作符。<a name="query_walkthrough"></a>

### 2.7. A simple query

本节的目的是说明如何将这些不同的组件连接在一起以处理一个简单的 查询。

假设你有一个数据文件“some_data_file.txt”，内容如下: 

```
1,1,1
2,2,2
3,4,4
```

<p>
您可以将其转换为SimpleDB可以查询的二进制文件，如下所示:
<p>
```java -jar dist/simpledb.jar convert some_data_file.txt 3```
<p>
这里，参数“3”告诉conver输入有3列。
<p>
以下代码实现了对该文件的简单选择查询。这段代码相当于SQL语句`SELECT * FROM some_data_file`。


```java
package simpledb;
import java.io.*;

public class test {

    public static void main(String[] argv) {

        // construct a 3-column table schema
        Type types[] = new Type[]{ Type.INT_TYPE, Type.INT_TYPE, Type.INT_TYPE };
        String names[] = new String[]{ "field0", "field1", "field2" };
        TupleDesc descriptor = new TupleDesc(types, names);

        // create the table, associate it with some_data_file.dat
        // and tell the catalog about the schema of this table.
        HeapFile table1 = new HeapFile(new File("some_data_file.dat"), descriptor);
        Database.getCatalog().addTable(table1, "test");

        // construct the query: we use a simple SeqScan, which spoonfeeds
        // tuples via its iterator.
        TransactionId tid = new TransactionId();
        SeqScan f = new SeqScan(tid, table1.getId());

        try {
            // and run it
            f.open();
            while (f.hasNext()) {
                Tuple tup = f.next();
                System.out.println(tup);
            }
            f.close();
            Database.getBufferPool().transactionComplete(tid);
        } catch (Exception e) {
            System.out.println ("Exception : " + e);
        }
    }

}
```

这个表有三个整数列。

The table we create has three integer fields. To express this, we create a `TupleDesc` object and pass it an array
of `Type` objects, and optionally an array of `String` field names. Once we have created this `TupleDesc`, we initialize
a `HeapFile` object representing the table stored in `some_data_file.dat`. Once we have created the table, we add it to
the catalog. If this were a database server that was already running, we would have this catalog information loaded. We
need to load it explicitly to make this code self-contained.

Once we have finished initializing the database system, we create a query plan. Our plan consists only of the `SeqScan`
operator that scans the tuples from disk. In general, these operators are instantiated with references to the
appropriate table (in the case of `SeqScan`) or child operator (in the case of e.g. Filter). The test program then
repeatedly calls `hasNext` and `next` on the `SeqScan` operator. As tuples are output from the `SeqScan`, they are
printed out on the command line.

We **strongly recommend** you try this out as a fun end-to-end test that will help you get experience writing your own
test programs for simpledb. You should create the file "test.java" in the src/java/simpledb directory with the code above,
and you should add some "import" statement above the code,
and place the `some_data_file.dat` file in the top level directory. Then run:

```
ant
java -classpath dist/simpledb.jar simpledb.test
```

Note that `ant` compiles `test.java` and generates a new jarfile that contains it.

## 3. Logistics

你必须提交你的代码(见下文)以及一篇描述你的方法的短文(最多2页)。该记录应:

- 描述你的设计思路
- 讨论并证明您对API所做的更改。
- 描述代码中缺失或不完整的元素。
- -描述你在实验室花了多长时间，以及是否有你觉得特别困难或困惑的事情。

### 3.1. **合作**

这个实验室对一个人来说应该是可以完成的，但是如果你喜欢和一个伙伴一起工作，这也是可以的。不允许更大的组。请明确指出你在个人报告中与谁一起工作，如果有人的话。

### 3.2. 提交

<!--
提交之前创建一个 <tt>6.830-lab1.tar.gz</tt>压缩包 然后提交到 [6.830 Stellar Site](https://stellar.mit.edu/S/course/6/sp13/6.830/index.html). You can use the `ant handin` target to generate the tarball.
-->

我们将使用gradescope自动批改所有的程序作业。你们都应该被邀请到班级中；如果没有，请查看piazza 的邀请代码. 如果您仍然有问题，请让我们知道，我们可以 帮助您设置. 您可以在截止日期前多次提交您的代码；我们将使用最新版本作为 由gradescope确定。将报告与您提交的材料放在一个名为lab1-writeup.txt的文件中。您还需要显式添加您创建的任何其他文件，例如new \*。java文件。

向gradescope提交的最简单方法是使用包含您的代码的`. zip '文件。在Linux/MacOS上，您可以通过运行以下命令来实现:

```bash
$ zip -r submission.zip src/ lab1-writeup.txt
```

### 3.3. 提交bug

如果有bug,请把报告到 [6.830-staff@mit.edu](mailto:6.830-staff@mit.edu).报告应该包含以下内容

- bug描述
- 一个可以放在 test/simpledb 目录下编译和运行的.java file
- 一个存放了可以用来复现bug的数据的.txt文件,支持使用`HeapFileEncoder`转为.dat文件

bug的首位提出者将获得糖果

<!--最新的bug报告/补丁点击[here](bugs.html).-->

<a name="grading"></a>

### 3.4 Grading

<p>你的分数的75%将基于你的代码是否通过我们将运行的系统测试套件，这些测试是我们已经提供的测试的超集。

在提交您的代码之前，您应该确保它没有产生来自<tt>ant test</tt >和<tt>ant systemtest</tt >的错误(通过所有测试)。

**重要提示:** 在测试之前，gradescope将替换您的<tt>build.xml</tt >和<tt >测试</tt >的全部内容目录与我们的这些文件的版本。这意味着您不能更改<tt >的格式.dat</tt >文件！你应该慎重改变我们的API。您应该测试您的代码是否编译了未修改的测试。

之后，您将从gradescope获得失败测试的即时反馈和错误输出(如果有) 提交。给出的分数将作为你作业中亲笔签名部分的分数。额外的25% 分数将基于你的写作质量和我们对你的代码的主观评价。这部分也会在我们完成你的作业评分后发表在gradescope上。

我们在设计这个作业的过程中得到了很多乐趣，我们希望你能喜欢这个作业！
