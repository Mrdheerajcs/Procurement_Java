package com.procurement.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {

    private String userName;
    @jakarta.validation.constraints.NotBlank
    private String newPassword;
    @jakarta.validation.constraints.NotBlank
    private String oldPassword;


}
