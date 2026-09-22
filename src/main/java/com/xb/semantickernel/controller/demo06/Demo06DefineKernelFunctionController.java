package com.xb.semantickernel.controller.demo06;

/**
 * Demo06DefineKernelFunctionController - 函数显式调用演示
 *
 * 演示不经过 AI 模型，而是由程序直接调用 Kernel 中注册的函数。
 * 教学要点：Kernel.invokeAsync 是"编排模式"的核心 API，
 * 开发者可以像调用本地方法一样精确控制 AI 工具的执行。
 *
 * @author ibqy
 */
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.orchestration.FunctionResult;
import com.microsoft.semantickernel.plugin.KernelPlugin;
import com.microsoft.semantickernel.plugin.KernelPluginFactory;
import com.microsoft.semantickernel.semanticfunctions.KernelArguments;
import com.xb.semantickernel.plugin.MathPlugin;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo06")
public class Demo06DefineKernelFunctionController {

    private final Kernel kernel;

    public Demo06DefineKernelFunctionController(Kernel kernel) {
        this.kernel = kernel;
    }

    /**
     * 显式调用 MathPlugin 中的数学函数
     *
     * 绕过 AI 模型，直接由程序编排调用指定函数并获取结果。
     *
     * @param a  第一个操作数
     * @param b  第二个操作数
     * @param op 运算名称（add/subtract/multiply/divide）
     * @return 运算结果及插件中包含的所有函数名
     */
    @GetMapping("/calc")
    public Map<String, Object> calc(
            @RequestParam int a,
            @RequestParam int b,
            @RequestParam(defaultValue = "add") String op) {
        // 运行时动态创建插件实例（实际项目中通常从 Kernel 获取）
        KernelPlugin plugin = KernelPluginFactory.createFromObject(new MathPlugin(), "MathPlugin");
        // 将参数封装为 KernelArguments，模拟函数调用时的入参
        KernelArguments args = KernelArguments.builder()
                .withVariable("a", a).withVariable("b", b).build();
        // 通过 plugin 名 + function 名定位函数，链式传入参数和返回类型
        FunctionResult<Integer> result = kernel.invokeAsync("MathPlugin", op)
                .withArguments(args).withResultType(Integer.class).block();
        Integer value = result.getResult();
        List<String> functionNames = new java.util.ArrayList<>();
        for (var fn : plugin) functionNames.add(fn.getName());
        return Map.of("op", op, "a", a, "b", b, "result", value, "pluginFunctions", functionNames);
    }
}