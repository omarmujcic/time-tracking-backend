package com.omarmujcic.timetracking.core.timetracking.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TimeEntryPageDTO {

    private List<TimeEntryResponseDTO> entries;
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrevious;
    private String nextCursor;
    private String previousCursor;
}
