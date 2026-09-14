package com.xb.semantickernel.plugin;

import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;

public class MathPlugin {

    @DefineKernelFunction(name = "add", description = "计算两个整数的和")
    public int add(
            @KernelFunctionParameter(name = "a", description = "第一个加数") int a,
            @KernelFunctionParameter(name = "b", description = "第二个加数") int b) {
        return a + b;
    }

    @DefineKernelFunction(name = "subtract", description = "计算两个整数的差")
    public int subtract(
            @KernelFunctionParameter(name = "a", description = "被减数") int a,
            @KernelFunctionParameter(name = "b", description = "减数") int b) {
        return a - b;
    }

    @DefineKernelFunction(name = "multiply", description = "计算两个整数的乘积")
    public int multiply(
            @KernelFunctionParameter(name = "a", description = "乘数") int a,
            @KernelFunctionParameter(name = "b", description = "乘数") int b) {
        return a * b;
    }

    @DefineKernelFunction(name = "divide", description = "计算两个整数的商")
    public int divide(
            @KernelFunctionParameter(name = "a", description = "被除数") int a,
            @KernelFunctionParameter(name = "b", description = "除数") int b) {
        if (b == 0) throw new IllegalArgumentException("除数不能为 0");
        return a / b;
    }
}