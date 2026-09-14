package com.xb.semantickernel.controller.demo06;

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

    @GetMapping("/calc")
    public Map<String, Object> calc(
            @RequestParam int a,
            @RequestParam int b,
            @RequestParam(defaultValue = "add") String op) {
        KernelPlugin plugin = KernelPluginFactory.createFromObject(new MathPlugin(), "MathPlugin");
        KernelArguments args = KernelArguments.builder()
                .withVariable("a", a).withVariable("b", b).build();
        FunctionResult<Integer> result = kernel.invokeAsync("MathPlugin", op)
                .withArguments(args).withResultType(Integer.class).block();
        Integer value = result.getResult();
        List<String> functionNames = new java.util.ArrayList<>();
        for (var fn : plugin) functionNames.add(fn.getName());
        return Map.of("op", op, "a", a, "b", b, "result", value, "pluginFunctions", functionNames);
    }
}