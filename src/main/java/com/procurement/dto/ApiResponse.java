package com.procurement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ApiResponse<T> {
    T response;
    private Integer status;
    private String message;
    private String salt;
    private boolean isProduction;
    private String key;
//new Added
    private String statuss;
    private Object data;

//    public ApiResponse(String status, String message, Object data) {
//        this.statuss = status;
//        this.message = message;
//        this.data = data;
//    }

    public ApiResponse(String status, String message, Object data) {
        this.statuss = status;
        this.message = message;
        this.data = data;
    }

    public ApiResponse() {

    }

    public ApiResponse(int status, String message){
        this.status = status;
        this.message = message;
    }
}
