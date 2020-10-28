## GC总结



### 串行GC(SerialGC)

单线程GC, 垃圾回收过程中会触发全线暂停（STW，防止过程中对象引用关系发生变化），停止所有的应用线程。CPU利用率高，暂停时间长。

#### 优点

少了多线程切换的开销，相较于其他收集器能够更加专注于垃圾回收，在单核场景下效率极高，并且在回收较小内存（几十或者一两百兆）时，停顿时间是毫秒级的。

#### 缺点

不能充分利用多核CPU。不管有多少CPU 内核，JVM在垃圾收集时都只能使用单个核心。

#### 适用场景

适用于单核、小内存（几十-几百M内存）应用适用。

#### YoungGC 和 FullGC

YoungGC(MinorGC), 对年轻代使用 mark-copy(标记复制)算法，释放年轻代内存中的 eden 和 s0/s1，同时s0/s1中部分对象晋升到老年代。
FullGC，对老年代使用 mark-sweep-compact(标记-清除-整理)算法，释放老年代，不触发YoungGC。



> java -Xms512m -Xmx512m -XX:+UseSerialGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis
>
> java -Xms1g -Xmx1g -XX:+UseSerialGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis
>
> java -Xms2g -Xmx2g -XX:+UseSerialGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis



### 并行GC(ParallelGC)

并行 GC 是对SerialGC的改进，支持多个线程同时进行GC，但是垃圾回收过程中依然会触发全线暂停（STW，防止过程中对象引用关系发生变化），停止所有的应用线程。
可以通过-XX:ParallelGCThreads=N参数设置 GC 线程数，默认值为 CPU核数。

#### 优点
多线程垃圾收集，充分利用多核性能，提高垃圾收集效率，从而提升系统吞吐量。

#### 缺点

相比并发 GC，每次 GC 导致的系统暂停时间长，增大了系统延迟。

#### 适用场景

适用于对吞吐量要求高，响应时间要求较低的系统。

#### YoungGC 和 FullGC

YoungGC, 对年轻代使用 mark-copy(标记复制)算法，释放年轻代内存中的 eden 和 s0/s1，同时s0/s1中部分对象晋升到老年代。
FullGC，对老年代使用 mark-sweep-compact(标记-清除-整理)算法，释放老年代，触发YoungGC。



> java -Xms512m -Xmx512m -XX:+UseParallelGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis
>
> java -Xms1g -Xmx1g -XX:+UseParallelGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis
>
> java -Xms2g -Xmx2g -XX:+UseParallelGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis



### 并发标记清除GC(CMS GC)

CMS GC 的设计目标是避免在老年代垃圾收集时出现长时间的卡顿，主要通过两种手段来达成此
目标：
1. 不对老年代进行整理，而是使用空闲列表（free-lists）来管理内存空间的回收。
2. 在mark-and-sweep （标记-清除） 阶段的大部分工作和应用线程一起并发执行。
但值得注意的是，它仍然和应用线程争抢 CPU 时间。默认情况下，CMS 使用的并发线程数等于CPU 核心数的1/4。

#### FullGC的六个阶段
1. 阶段1: Initial Mark（初始标记）,STW
2. 阶段2: Concurrent Mark（并发标记）
3. 阶段3: Concurrent Preclean（并发预清理）
4. 阶段4: Final Remark（最终标记）,STW
5. 阶段5: Concurrent Sweep（并发清除）
6. 阶段6: Concurrent Reset（并发重置）

#### 优点
多CPU情况下，CMS GC并没有明显的应用线程暂停，降低了GC停顿导致的系统延迟。

#### 缺点
没有对老年代进行整理，导致老年代内存碎片问题（因为不压缩），在某些情况下GC 会造成不可预测的暂停时间，特别是堆内存较大的情况下。

#### 适用场景

适用于对响应时间要求较高，吞吐量要求较低的系统。

#### YoungGC 和 FullGC

YoungGC 使用 ParNewGC，使用 mark-copy（标记复制）算法，
FullGC 使用并发 mark-sweep（标记清除）算法。进行老年代的并发回收时，可能会伴随着多次年轻代的minor GC。



> java -Xms512m -Xmx512m -XX:+UseConcMarkSweepGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis
>
> java -Xms1g -Xmx1g -XX:+UseConcMarkSweepGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis
>
> java -Xms2g -Xmx2g -XX:+UseConcMarkSweepGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis



### G1 GC(Garbage-First GC)

G1 的全称是Garbage-First，意为垃圾优先，哪一块的垃圾最多就优先清理它。G1GC 最主要的设计目标是：将STW 停顿的时间和分布，变成可预期且可配置的。通过参数-XX:MaxGCPauseMillis=value进行设置。

首先，堆不再分成年轻代和老年代，而是划分为多个（通常是2048 个）可以存放对象的小块堆区域(smaller heap regions)。
每个小块，可能一会被定义成Eden 区，一会被指定为Survivor区或者Old 区。在逻辑上，所有的Eden 区和Survivor区合起来就是年轻代，所有的Old 区拼在一起那就是老年代。

#### GC三个步骤

1. 年轻代模式转移暂停（Evacuation Pause）
2. 并发标记（Concurrent Marking）
   - 阶段 1: Initial Mark(初始标记)
   - 阶段 2: Root Region Scan(Root区扫描)
   - 阶段 3: Concurrent Mark(并发标记)
   - 阶段 4: Remark(再次标记), STW
   - 阶段 5: Cleanup(清理)

3. 转移暂停: 混合模式（Evacuation Pause (mixed)）

#### FullGC

某些情况下G1 触发了Full GC，这时G1 会退化使用SerialGC 来完成垃圾的清理工作，它仅仅使用单线程来完成GC 工作，GC暂停时间将达到秒级别的.

##### 触发FullGC原因

1. 并发模式失败
   G1 启动标记周期，但在Mix GC 之前，老年代就被填满，这时候G1 会放弃标记周期。
   解决办法：增加堆大小，或者调整周期（例如增加线程数-XX:ConcGCThreads 等）。
2. 晋升失败
   没有足够的内存供存活对象或晋升对象使用，由此触发了Full GC(to-space exhausted/to-space overflow）。
   解决办法：
   	a)  增加–XX：G1ReservePercent 选项的值（并相应增加总的堆大小）增加预留内存量。
   	b)  通过减少–XX：InitiatingHeapOccupancyPercent 提前启动标记周期。
   	c)  通过增加–XX：ConcGCThreads 选项的值来增加并行标记线程的数目。
3. 巨型对象分配失败
   当巨型对象找不到合适的空间进行分配时，就会启动Full GC，来释放空间。
   解决办法：增加内存或者增大-XX：G1HeapRegionSize.

#### YoungGC 和 FullGC

一般不发生 FullGC，使得G1 不必每次都去收集整个堆空间，而是以增量的方式来进行处理: 每次只处理一部分内存块，称为此次 GC 的回收集(collection set)。每次GC暂停都会收集所有年轻代的内存块，但一般只包含部分老年代的内存块。

#### 适用场景

G1GC适用堆内存空间特别大（如:>6G）的场景。



> java -Xms512m -Xmx512m -XX:+UseG1GC -XX:+PrintGC -XX:+PrintGCDateStamps GCLogAnalysis
>
> java -Xms1g -Xmx1g -XX:+UseG1GC -XX:+PrintGC -XX:+PrintGCDateStamps GCLogAnalysis
>
> java -Xms2g -Xmx2g -XX:+UseG1GC -XX:+PrintGC -XX:+PrintGCDateStamps GCLogAnalysis



### 总结
理想情况下，内存越大，垃圾回收次数越少，吞吐量越高；
但内存不是越大越好，内存越大，相应的每次内存回收耗时就更多，系统延迟提高，延迟吞吐量可能更低。
需要结合具体应用场景指定GC策略。

