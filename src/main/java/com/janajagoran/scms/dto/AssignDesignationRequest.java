package com.janajagoran.scms.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignDesignationRequest {
    @NotNull
    private Long memberId;

    @NotNull
    private Long designationId;
}
