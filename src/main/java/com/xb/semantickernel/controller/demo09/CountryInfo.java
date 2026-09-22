package com.xb.semantickernel.controller.demo09;

/**
 * CountryInfo - 国家信息数据传输对象
 *
 * 作为 AI 模型输出的结构化 JSON 的目标解析类型。
 * 教学要点：用 record 定义不可变 DTO，配合 Jackson
 * 可轻松将模型输出解析为强类型 Java 对象。
 *
 * @author ibqy
 */
public record CountryInfo(
        String name,
        String capital,
        long population,
        String currency) {

    public static CountryInfo empty() {
        return new CountryInfo("", "", 0L, "");
    }
}