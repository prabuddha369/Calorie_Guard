package com.example.calorieguard;

public class DataModel {
    private String calorie;
    private String weight;
    private String macros;
    private String name;

    public DataModel(String calorie, String weight, String macros, String name) {
        this.calorie = calorie;
        this.weight = weight;
        this.macros = macros;
        this.name = name;
    }

    public String getCalorieScan() {
        return calorie;
    }

    public void setCalorieScan(String calorie) {
        this.calorie = calorie;
    }

    public String getWeightScan() {
        return weight;
    }

    public void setWeightScan(String weight) {
        this.weight = weight;
    }

    public String getMacrosScan() {
        return macros;
    }

    public void setMacros(String macros) {
        this.macros = macros;
    }

    public String getNameScan() {
        return name;
    }
}
