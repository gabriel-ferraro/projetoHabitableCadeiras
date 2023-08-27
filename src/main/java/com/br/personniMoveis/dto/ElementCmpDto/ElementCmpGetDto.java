package com.br.personniMoveis.dto.ElementCmpDto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ElementCmpGetDto {

    private Long elementCmpId;

    private String name;

    private String imgUrl;

    private Long sectionCmp;
}
