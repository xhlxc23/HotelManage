package com.example.hotel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.hotel.DTO.response.VrResponseDTO;
import com.example.hotel.entity.PanoramaLink;
import com.example.hotel.entity.PanoramaNode;
import com.example.hotel.exception.BusionessException;
import com.example.hotel.mapper.CategoryMapper;
import com.example.hotel.mapper.PanoramaLinkMapper;
import com.example.hotel.mapper.PanoramaNodeMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VrService {

    @Resource
    private PanoramaNodeMapper panoramaNodeMapper;

    @Resource
    private PanoramaLinkMapper panoramaLinkMapper;

    @Resource
    private CategoryMapper categoryMapper;

    public VrResponseDTO getVrByCategoryId(String categoryId) {
        if (categoryMapper.selectById(categoryId) == null) {
            throw new BusionessException("房型不存在");
        }

        List<PanoramaNode> nodes = panoramaNodeMapper.selectList(
                new LambdaQueryWrapper<PanoramaNode>()
                        .eq(PanoramaNode::getCategoryId, categoryId));

        if (nodes == null || nodes.isEmpty()) {
            return null;
        }

        PanoramaNode startNode = nodes.stream()
                .filter(n -> n.getIsStart() != null && n.getIsStart() == 1)
                .findFirst()
                .orElse(nodes.get(0));

        List<String> nodeIds = nodes.stream()
                .map(PanoramaNode::getId)
                .collect(Collectors.toList());

        List<PanoramaLink> allLinks = panoramaLinkMapper.selectList(
                new LambdaQueryWrapper<PanoramaLink>()
                        .in(PanoramaLink::getSourceNodeId, nodeIds));

        List<VrResponseDTO.VrNodeDTO> nodeDTOs = nodes.stream()
                .map(node -> {
                    List<VrResponseDTO.VrLinkDTO> linkDTOs = allLinks.stream()
                            .filter(link -> link.getSourceNodeId().equals(node.getId()))
                            .map(link -> VrResponseDTO.VrLinkDTO.builder()
                                    .nodeId(link.getTargetNodeId())
                                    .label(link.getLabel())
                                    .position(VrResponseDTO.VrPositionDTO.builder()
                                            .yaw(link.getYaw() != null ? link.getYaw().stripTrailingZeros().toPlainString() + "deg" : null)
                                            .pitch(link.getPitch() != null ? link.getPitch().stripTrailingZeros().toPlainString() + "deg" : null)
                                            .build())
                                    .build())
                            .collect(Collectors.toList());

                    return VrResponseDTO.VrNodeDTO.builder()
                            .id(node.getId())
                            .panorama(node.getImageUrl())
                            .name(node.getName())
                            .links(linkDTOs)
                            .build();
                })
                .collect(Collectors.toList());

        return VrResponseDTO.builder()
                .startNodeId(startNode.getId())
                .nodes(nodeDTOs)
                .build();
    }
}