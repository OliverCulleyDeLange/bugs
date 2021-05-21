#Issue 1
### A companion object's init block is called when mocking a class using mockk
We worked around this in Unit tests with the following function:
```
fun initRustBindings(vararg rustBindings: KClass<*>) {
    mockkStatic("java.lang.System") {
        every { System.loadLibrary("android") } returns Unit
        rustBindings.forEach {
            mockkClass(it)
            println("Mocked ${it.simpleName}")
        }
    }
}
```

This limits use to Android P/28/9.0 and above due to a mockk limitation.

Regarding the IOException on android 10 and 11 - there is a [workaround](https://github.com/mockk/mockk/issues/297#issuecomment-641361770) which appears to fix this issue as the tests pass. However, this looks like a false positive as the test is actually crashing, similar to how Android 9/28/P does. See Exception 3 below.


| Version           | Version   | Code          | Arch   | Works? | Error
|-------------------|-----------|---------------|--------|--------|--------------
| Android R         | 	11 	    | API level 30  | x86    | No     | Exception 1, and then Exception 3 with [workaround](https://github.com/mockk/mockk/issues/297#issuecomment-641361770)
| Android 10 (Q)    | 	10 	    | API level 29  | x86_64 | No     | Exception 1/3 - as above
| Pie               | 	9 	    | API level 28  | x86    | No     | Android studio says it passed, but Exception 3 happens.
| Oreo              | 	8.1.0 	| API level 27  | x86    | No     | Exception 2 - io.mockk.proxy.MockKAgentException: Mocking static is supported starting from Android P
| Oreo              | 	8.0.0 	| API level 26  | x86    | No     | Exception 2 - as above
| Nougat            | 	7.1 	| API level 25  | x86    | No     | Exception 2 - as above
| Nougat            | 	7.0 	| API level 24  | x86    | No     | Exception 2 - as above
| Marshmallow       | 	6.0 	| API level 23  | x86_64 | No     | Exception 2 - as above



# Exceptions:
## 1 [confirmed workaround](https://github.com/mockk/mockk/issues/297#issuecomment-641361770)
```
2021-05-21 11:22:40.688 5990-6014/uk.co.oliverdelange.bugs W/verdelange.bug: Agent attach failed (result=1) : Unable to dlopen libmockkjvmtiagent.so: dlopen failed: library "libmockkjvmtiagent.so" not found
2021-05-21 11:22:40.690 5990-6014/uk.co.oliverdelange.bugs E/TestRunner: failed: useAppContext(uk.co.oliverdelange.bugs.ExampleInstrumentedTest)
2021-05-21 11:22:40.690 5990-6014/uk.co.oliverdelange.bugs E/TestRunner: ----- begin exception -----
2021-05-21 11:22:40.693 5990-6014/uk.co.oliverdelange.bugs E/TestRunner: java.lang.ExceptionInInitializerError
        at uk.co.oliverdelange.bugs.ExampleInstrumentedTestKt.initRustBindings(ExampleInstrumentedTest.kt:89)
        at uk.co.oliverdelange.bugs.ExampleInstrumentedTest.useAppContext(ExampleInstrumentedTest.kt:53)
        at java.lang.reflect.Method.invoke(Native Method)
        at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:50)
        at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
        at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:47)
        at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
        at androidx.test.rule.ActivityTestRule$ActivityStatement.evaluate(ActivityTestRule.java:549)
        at org.junit.rules.RunRules.evaluate(RunRules.java:20)
        at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:325)
        at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:78)
        at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:57)
        at org.junit.runners.ParentRunner$3.run(ParentRunner.java:290)
        at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:71)
        at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:288)
        at org.junit.runners.ParentRunner.access$000(ParentRunner.java:58)
        at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:268)
        at org.junit.runners.ParentRunner.run(ParentRunner.java:363)
        at androidx.test.ext.junit.runners.AndroidJUnit4.run(AndroidJUnit4.java:154)
        at org.junit.runners.Suite.runChild(Suite.java:128)
        at org.junit.runners.Suite.runChild(Suite.java:27)
        at org.junit.runners.ParentRunner$3.run(ParentRunner.java:290)
        at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:71)
        at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:288)
        at org.junit.runners.ParentRunner.access$000(ParentRunner.java:58)
        at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:268)
        at org.junit.runners.ParentRunner.run(ParentRunner.java:363)
        at org.junit.runner.JUnitCore.run(JUnitCore.java:137)
        at org.junit.runner.JUnitCore.run(JUnitCore.java:115)
        at androidx.test.internal.runner.TestExecutor.execute(TestExecutor.java:56)
        at androidx.test.runner.AndroidJUnitRunner.onStart(AndroidJUnitRunner.java:444)
        at android.app.Instrumentation$InstrumentationThread.run(Instrumentation.java:2205)
     Caused by: io.mockk.proxy.MockKAgentException: MockK could not self-attach a jvmti agent to the current VM. This feature is required for inline mocking.
    This error occured due to an I/O error during the creation of this agent: java.io.IOException: Unable to dlopen libmockkjvmtiagent.so: dlopen failed: library "libmockkjvmtiagent.so" not found

    Potentially, the current VM does not support the jvmti API correctly
        at io.mockk.proxy.android.AndroidMockKAgentFactory.init(AndroidMockKAgentFactory.kt:65)
        at io.mockk.impl.JvmMockKGateway.<init>(JvmMockKGateway.kt:46)
        at io.mockk.impl.JvmMockKGateway.<clinit>(JvmMockKGateway.kt:172)
        	... 32 more
     Caused by: java.io.IOException: Unable to dlopen libmockkjvmtiagent.so: dlopen failed: library "libmockkjvmtiagent.so" not found
        at dalvik.system.VMDebug.nativeAttachAgent(Native Method)
        at dalvik.system.VMDebug.attachAgent(VMDebug.java:572)
        at android.os.Debug.attachJvmtiAgent(Debug.java:2563)
        at io.mockk.proxy.android.JvmtiAgent.<init>(JvmtiAgent.kt:48)
        at io.mockk.proxy.android.AndroidMockKAgentFactory.init(AndroidMockKAgentFactory.kt:38)
        	... 34 more
2021-05-21 11:22:40.693 5990-6014/uk.co.oliverdelange.bugs E/TestRunner: ----- end exception -----
```

## 2
```
05-21 11:32:37.703 3624-3643/uk.co.oliverdelange.bugs E/TestRunner: failed: useAppContext(uk.co.oliverdelange.bugs.ExampleInstrumentedTest)
05-21 11:32:37.703 3624-3643/uk.co.oliverdelange.bugs E/TestRunner: ----- begin exception -----
05-21 11:32:37.704 3624-3643/uk.co.oliverdelange.bugs E/TestRunner: io.mockk.MockKException: Failed to build static proxy
        at io.mockk.impl.instantiation.JvmStaticMockFactory.staticMockk(JvmStaticMockFactory.kt:41)
        at uk.co.oliverdelange.bugs.ExampleInstrumentedTestKt.initRustBindings(ExampleInstrumentedTest.kt:102)
        at uk.co.oliverdelange.bugs.ExampleInstrumentedTest.useAppContext(ExampleInstrumentedTest.kt:53)
        at java.lang.reflect.Method.invoke(Native Method)
        at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:50)
        at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
        at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:47)
        at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
        at androidx.test.rule.ActivityTestRule$ActivityStatement.evaluate(ActivityTestRule.java:549)
        at org.junit.rules.RunRules.evaluate(RunRules.java:20)
        at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:325)
        at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:78)
        at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:57)
        at org.junit.runners.ParentRunner$3.run(ParentRunner.java:290)
        at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:71)
        at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:288)
        at org.junit.runners.ParentRunner.access$000(ParentRunner.java:58)
        at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:268)
        at org.junit.runners.ParentRunner.run(ParentRunner.java:363)
        at androidx.test.ext.junit.runners.AndroidJUnit4.run(AndroidJUnit4.java:154)
        at org.junit.runners.Suite.runChild(Suite.java:128)
        at org.junit.runners.Suite.runChild(Suite.java:27)
        at org.junit.runners.ParentRunner$3.run(ParentRunner.java:290)
        at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:71)
        at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:288)
        at org.junit.runners.ParentRunner.access$000(ParentRunner.java:58)
        at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:268)
        at org.junit.runners.ParentRunner.run(ParentRunner.java:363)
        at org.junit.runner.JUnitCore.run(JUnitCore.java:137)
        at org.junit.runner.JUnitCore.run(JUnitCore.java:115)
        at androidx.test.internal.runner.TestExecutor.execute(TestExecutor.java:56)
        at androidx.test.runner.AndroidJUnitRunner.onStart(AndroidJUnitRunner.java:444)
        at android.app.Instrumentation$InstrumentationThread.run(Instrumentation.java:1879)
     Caused by: io.mockk.proxy.MockKAgentException: Mocking static is supported starting from Android P
        at io.mockk.proxy.android.StaticProxyMaker.staticProxy(StaticProxyMaker.kt:35)
        at io.mockk.impl.instantiation.JvmStaticMockFactory.staticMockk(JvmStaticMockFactory.kt:39)
        at uk.co.oliverdelange.bugs.ExampleInstrumentedTestKt.initRustBindings(ExampleInstrumentedTest.kt:102) 
        at uk.co.oliverdelange.bugs.ExampleInstrumentedTest.useAppContext(ExampleInstrumentedTest.kt:53) 
        at java.lang.reflect.Method.invoke(Native Method) 
        at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:50) 
        at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12) 
        at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:47) 
        at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17) 
        at androidx.test.rule.ActivityTestRule$ActivityStatement.evaluate(ActivityTestRule.java:549) 
        at org.junit.rules.RunRules.evaluate(RunRules.java:20) 
        at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:325) 
        at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:78) 
        at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:57) 
        at org.junit.runners.ParentRunner$3.run(ParentRunner.java:290) 
        at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:71) 
        at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:288) 
        at org.junit.runners.ParentRunner.access$000(ParentRunner.java:58) 
        at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:268) 
        at org.junit.runners.ParentRunner.run(ParentRunner.java:363) 
        at androidx.test.ext.junit.runners.AndroidJUnit4.run(AndroidJUnit4.java:154) 
        at org.junit.runners.Suite.runChild(Suite.java:128) 
        at org.junit.runners.Suite.runChild(Suite.java:27) 
        at org.junit.runners.ParentRunner$3.run(ParentRunner.java:290) 
        at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:71) 
        at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:288) 
        at org.junit.runners.ParentRunner.access$000(ParentRunner.java:58) 
        at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:268) 
        at org.junit.runners.ParentRunner.run(ParentRunner.java:363) 
        at org.junit.runner.JUnitCore.run(JUnitCore.java:137) 
        at org.junit.runner.JUnitCore.run(JUnitCore.java:115) 
        at androidx.test.internal.runner.TestExecutor.execute(TestExecutor.java:56) 
        at androidx.test.runner.AndroidJUnitRunner.onStart(AndroidJUnitRunner.java:444) 
        at android.app.Instrumentation$InstrumentationThread.run(Instrumentation.java:1879) 
05-21 11:32:37.704 3624-3643/uk.co.oliverdelange.bugs E/TestRunner: ----- end exception -----
```

## 3 - Android Studio 4.2 marks test as 'passed' when it actually crashes and fails
```
2021-05-21 11:44:40.349 7638-7669/uk.co.oliverdelange.bugs E/TestRunner: failed: useAppContext(uk.co.oliverdelange.bugs.ExampleInstrumentedTest)
2021-05-21 11:44:40.349 7638-7669/uk.co.oliverdelange.bugs E/TestRunner: ----- begin exception -----
2021-05-21 11:44:40.418 7638-7669/uk.co.oliverdelange.bugs I/TestRunner: finished: useAppContext(uk.co.oliverdelange.bugs.ExampleInstrumentedTest)
2021-05-21 11:44:40.459 7638-7669/uk.co.oliverdelange.bugs E/TestRunner: failed: uk.co.oliverdelange.bugs.ExampleInstrumentedTest
2021-05-21 11:44:40.459 7638-7669/uk.co.oliverdelange.bugs E/TestRunner: ----- begin exception -----
2021-05-21 11:44:40.538 7638-7669/uk.co.oliverdelange.bugs E/TestRunner: failed: null
2021-05-21 11:44:40.538 7638-7669/uk.co.oliverdelange.bugs E/TestRunner: ----- begin exception -----
2021-05-21 11:44:41.352 7638-7669/uk.co.oliverdelange.bugs I/Process: Sending signal. PID: 7638 SIG: 9
2021-05-21 11:44:41.394 2017-3057/system_process I/ActivityManager: Process uk.co.oliverdelange.bugs (pid 7638) has died: fore FGS
2021-05-21 11:44:41.397 2017-3057/system_process W/ActivityManager: Crash of app uk.co.oliverdelange.bugs running instrumentation ComponentInfo{uk.co.oliverdelange.bugs.test/androidx.test.runner.AndroidJUnitRunner}
```

``` from the debugger, caught while mockking System
java.lang.reflect.InvocationTargetException
java.lang.StackOverflowError: stack size 1042KB
1 = {Class@9824} "class java.lang.reflect.Method"
2 = {Class@11331} "class org.junit.runners.model.FrameworkMethod$1"
3 = {Class@11299} "class org.junit.internal.runners.model.ReflectiveCallable"
4 = {Class@11293} "class org.junit.runners.model.FrameworkMethod"
5 = {Class@11302} "class org.junit.internal.runners.statements.InvokeMethod"
6 = {Class@11423} "class androidx.test.rule.ActivityTestRule$ActivityStatement"
7 = {Class@11281} "class org.junit.rules.RunRules"
8 = {Class@11217} "class org.junit.runners.ParentRunner"
9 = {Class@11254} "class org.junit.runners.BlockJUnit4ClassRunner"
10 = {Class@11254} "class org.junit.runners.BlockJUnit4ClassRunner"
11 = {Class@11278} "class org.junit.runners.ParentRunner$3"
12 = {Class@11274} "class org.junit.runners.ParentRunner$1"
13 = {Class@11217} "class org.junit.runners.ParentRunner"
14 = {Class@11217} "class org.junit.runners.ParentRunner"
15 = {Class@11282} "class org.junit.runners.ParentRunner$2"
16 = {Class@11217} "class org.junit.runners.ParentRunner"
17 = {Class@11266} "class androidx.test.ext.junit.runners.AndroidJUnit4"
18 = {Class@11218} "class org.junit.runners.Suite"
19 = {Class@11218} "class org.junit.runners.Suite"
20 = {Class@11278} "class org.junit.runners.ParentRunner$3"
21 = {Class@11274} "class org.junit.runners.ParentRunner$1"
22 = {Class@11217} "class org.junit.runners.ParentRunner"
23 = {Class@11217} "class org.junit.runners.ParentRunner"
24 = {Class@11282} "class org.junit.runners.ParentRunner$2"
25 = {Class@11217} "class org.junit.runners.ParentRunner"
26 = {Class@11359} "class org.junit.runner.JUnitCore"
27 = {Class@11359} "class org.junit.runner.JUnitCore"
28 = {Class@10743} "class androidx.test.internal.runner.TestExecutor"
29 = {Class@10682} "class androidx.test.runner.AndroidJUnitRunner"
30 = {Class@10770} "class android.app.Instrumentation$InstrumentationThread"