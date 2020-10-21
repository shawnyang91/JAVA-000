学习笔记

## 字节码

## 类加载器

## 内存模型

## JVM启动参数

-Xmx, 指定最大堆内存。如-Xmx4g. 这只是限制了Heap 部分的最大值为4g。
这个内存不包括栈内存，也不包括堆外使用的内存。

-Xms, 指定堆内存空间的初始大小。如-Xms4g。而且指定的内存大小，并
不是操作系统实际分配的初始值，而是GC先规划好，用到才分配。专用服务
器上需要保持–Xms 和–Xmx 一致，否则应用刚启动可能就有好几个FullGC。
当两者配置不一致时，堆内存扩容可能会导致性能抖动。

-Xmn, 等价于-XX:NewSize，使用G1 垃圾收集器不应该设置该选项，在其
他的某些业务场景下可以设置。官方建议设置为-Xmx 的1/2 ~ 1/4.

-Xss, 设置每个线程栈的字节数。例如-Xss1m 指定线程栈为1MB，与-
XX:ThreadStackSize=1m 等价.

-MaxMetaspaceSize=size, Java8 默认不限制Meta 空间, 一般不允许设
置该选项。

-MaxDirectMemorySize=size，系统可以使用的最大堆外内存，这个参
数跟-Dsun.nio.MaxDirectMemorySize 效果相同。


## JDK命令行工具

## JDK内置图形化工具
- jconsole
- VisualVM
- jmc

## GC策略
