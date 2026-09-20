package com.janajagoran.scms.dto;

import lombok.Data;

@Data
public class SetActiveStatusRequest {
    private boolean active;
    private String reason; // required when deactivating -- the "modal reason -> save" step
}
