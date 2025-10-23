package com.ke.stacktrace;

public interface RunTimeErrorExplainPrompt {

	String basic =
			"给你一段 {lang} 代码的运行时错误信息和引发异常的代码的上下文，" +
					"你需要给出错误原因和可能的解决方案。\n" +
					"请使用简洁精炼的语言给出解决方案或建议，如果可以给出修复后的代码请务必给出。请注意异常可能是某个第三方库抛出的。\n" +
					"代码：{code}\n" +
					"堆栈：\n{traceback}\n" +
					"原因和解决方案:";

	String noCodePrompt =
			"给你一段代码的运行时错误信息" +
					"你需要给出错误原因和可能的解决方案。\n" +
					"请使用简洁精炼的语言给出解决方案或建议。请注意异常可能是某个第三方库抛出的。\n" +

					"堆栈：\n{traceback}\n" +
					"原因和解决方案:";

}




