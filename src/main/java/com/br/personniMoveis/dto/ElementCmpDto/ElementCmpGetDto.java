package com.br.personniMoveis.dto.ElementCmpDto;

import com.br.personniMoveis.model.productCmp.SectionCmp;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ElementCmpGetDto {

    private Long elementCmpId;

    private String name;

    private String imgUrl;

    private SectionCmp sectionCmp;
}
