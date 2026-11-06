package com.example.hotel.DTO.response;



import com.example.hotel.DTO.command.DiscountCommandDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponseDTO
{
    private String id;
    private String name;
    private BigDecimal basicPrice;
    private List<DiscountCommandDTO> discounts;
    private List<String> picture;
    private String description;
}