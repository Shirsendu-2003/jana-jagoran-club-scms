package com.janajagoran.scms.dto;

import lombok.Data;

import java.util.List;

@Data
public class MenuPermissionRequest {
    private List<String> menuKeys; // e.g. ["BUDGET_FINANCE", "EVENTS"]
}
