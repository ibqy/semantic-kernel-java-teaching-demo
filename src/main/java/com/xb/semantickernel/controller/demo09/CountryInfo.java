package com.xb.semantickernel.controller.demo09;

public record CountryInfo(
        String name,
        String capital,
        long population,
        String currency) {

    public static CountryInfo empty() {
        return new CountryInfo("", "", 0L, "");
    }
}