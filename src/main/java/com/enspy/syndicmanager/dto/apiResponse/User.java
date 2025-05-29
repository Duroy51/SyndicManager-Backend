package com.enspy.syndicmanager.dto.apiResponse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {
    private String created_at;
    private String updated_at;
    private String deleted_at;
    private String created_by;
    private String updated_by;
    private String id;
    private String username;
    private String email;
    private String name;
    private String phone_number;
    private String session_id;
    private boolean active;

}

