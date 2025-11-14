package com.example.ecommerceapp.models;

public class Ward {
    private String code;
    private String name;
    private String name_en;
    private String full_name;
    private String full_name_en;
    private String code_name;
    private String district_code;

    public Ward() {}

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFullName() { return full_name; }
    public void setFullName(String full_name) { this.full_name = full_name; }

    public String getDistrictCode() { return district_code; }
    public void setDistrictCode(String district_code) { this.district_code = district_code; }

    @Override
    public String toString() {
        return name;
    }
}
