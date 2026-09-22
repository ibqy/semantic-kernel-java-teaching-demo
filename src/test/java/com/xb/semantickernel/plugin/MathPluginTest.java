package com.xb.semantickernel.plugin;

/**
 * MathPlugin 单元测试 - 验证加减乘除四则运算的正确性和边界处理
 */
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MathPlugin 数学插件测试")
class MathPluginTest {

    private MathPlugin plugin;

    @BeforeEach
    void setUp() {
        plugin = new MathPlugin();
    }

    @Nested
    @DisplayName("add 加法")
    class AddTests {
        @Test
        @DisplayName("正数相加")
        void add_positiveNumbers() {
            assertEquals(8, plugin.add(3, 5));
        }

        @Test
        @DisplayName("含负数相加")
        void add_negativeNumbers() {
            assertEquals(-1, plugin.add(2, -3));
        }

        @Test
        @DisplayName("零相加")
        void add_zero() {
            assertEquals(5, plugin.add(5, 0));
        }
    }

    @Nested
    @DisplayName("subtract 减法")
    class SubtractTests {
        @Test
        @DisplayName("正数相减")
        void subtract_positiveResult() {
            assertEquals(7, plugin.subtract(10, 3));
        }

        @Test
        @DisplayName("结果为负数")
        void subtract_negativeResult() {
            assertEquals(-3, plugin.subtract(2, 5));
        }
    }

    @Nested
    @DisplayName("multiply 乘法")
    class MultiplyTests {
        @Test
        @DisplayName("正数相乘")
        void multiply_positive() {
            assertEquals(15, plugin.multiply(3, 5));
        }

        @Test
        @DisplayName("乘以零")
        void multiply_byZero() {
            assertEquals(0, plugin.multiply(100, 0));
        }

        @Test
        @DisplayName("负数相乘")
        void multiply_negative() {
            assertEquals(6, plugin.multiply(-2, -3));
        }
    }

    @Nested
    @DisplayName("divide 除法")
    class DivideTests {
        @Test
        @DisplayName("整除")
        void divide_exact() {
            assertEquals(5, plugin.divide(10, 2));
        }

        @Test
        @DisplayName("整数除法截断")
        void divide_truncates() {
            assertEquals(3, plugin.divide(7, 2));
        }

        @Test
        @DisplayName("除数为零抛异常")
        void divide_byZero_throws() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> plugin.divide(10, 0));
            assertEquals("除数不能为 0", ex.getMessage());
        }
    }
}
