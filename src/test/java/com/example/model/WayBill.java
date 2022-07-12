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
public class WayBill {

    private String shopId;
    private Date createdAt;
    private Long customerId;
    private Long wayBillId;
}
