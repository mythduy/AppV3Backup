package com.example.ecommerceapp.api;

import com.example.ecommerceapp.models.District;
import com.example.ecommerceapp.models.Province;
import com.example.ecommerceapp.models.Ward;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ProvinceApiService {
    
    // Get all provinces
    @GET("p/")
    Call<List<Province>> getProvinces();
    
    // Get districts by province code
    @GET("p/{province_code}?depth=2")
    Call<ProvinceWithDistricts> getDistricts(@Path("province_code") String provinceCode);
    
    // Get wards by district code
    @GET("d/{district_code}?depth=2")
    Call<DistrictWithWards> getWards(@Path("district_code") String districtCode);
    
    // Response wrapper classes
    class ProvinceWithDistricts {
        public String code;
        public String name;
        public List<District> districts;
    }
    
    class DistrictWithWards {
        public String code;
        public String name;
        public List<Ward> wards;
    }
}
