package com.example.hotel.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VrResponseDTO {

    private String startNodeId;

    private List<VrNodeDTO> nodes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VrNodeDTO {
        private String id;
        private String panorama;
        private String name;
        private List<VrLinkDTO> links;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VrLinkDTO {
        private String nodeId;
        private String label;
        private VrPositionDTO position;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VrPositionDTO {
        private String yaw;
        private String pitch;
    }
}