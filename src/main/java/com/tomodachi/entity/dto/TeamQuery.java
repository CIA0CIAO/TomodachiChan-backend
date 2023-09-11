package com.tomodachi.entity.dto;

import lombok.Data;

@Data
public class TeamQuery {
    private Integer currentPage;
    private String searchText;
    private Boolean onlyNoPassword;
}