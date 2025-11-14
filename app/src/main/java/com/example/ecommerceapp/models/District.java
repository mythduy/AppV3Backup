package com.example.ecommerceapp.models;

public class District {
    private String code;
    private String name;
    private String name_en;
    private String full_name;
    private String full_name_en;
    private String code_name;
    private String province_code;

    public District() {}

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFullName() { return full_name; }
    public void setFullName(String full_name) { this.full_name = full_name; }

    public String getProvinceCode() { return province_code; }
    public void setProvinceCode(String province_code) { this.province_code = province_code; }

    @Override
    public String toString() {
        return name;
    }
}
