package com.enspy.syndicmanager.client.dto.response;

import lombok.Data;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
public class AgencyDto {
    private Instant created_at;
    private Instant updated_at;
    private Instant deleted_at;
    private String created_by;
    private String updated_by;
    private String organization_id;
    private String agency_id;
    private String owner_id;
    private String name;
    private String location;
    private String description;
    private boolean transferable;
    private List<String> business_domains;
    private boolean is_active;
    private String logo;
    private String short_name;
    private String long_name;
    private boolean is_individual_business;
    private boolean is_headquarter;
    private List<String> images;
    private String greeting_message;
    private Instant year_created;
    private String manager_name;
    private Instant registration_date;
    private double average_revenue;
    private double capital_share;
    private String registration_number;
    private String social_network;
    private String tax_number;
    private List<String> keywords;
    private boolean is_public;
    private boolean is_business;
    private Map<String, String> operation_time_plan;
    private int total_affiliated_customers;
}
