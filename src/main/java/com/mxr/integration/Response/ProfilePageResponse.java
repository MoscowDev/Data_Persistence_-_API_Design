package com.mxr.integration.Response;
 
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
import java.util.List;
 
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfilePageResponse {
    private String status;
    private int page;
    private int limit;
    private long total;
    private List<?> data;
}
