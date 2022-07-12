package com.example.model;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Jerry Will
 * @version 2022-07-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveRoom {

    private String tenantId;
    private String roomId;
    private Date recordDate;
    private Date createdAt;
    private Long seqId;
}
