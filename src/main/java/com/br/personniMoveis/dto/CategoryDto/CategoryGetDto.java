package com.br.personniMoveis.dto.CategoryDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
@Data
@AllArgsConstructor
public class CategoryGetDto {

    private Long id;

    private String name;

    @Builder.Default
    private Boolean allow_creation = true; // Permitir criação do produto que se encaixa na categoria.

}
