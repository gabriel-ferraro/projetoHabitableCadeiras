package com.br.personniMoveis.service;

import com.br.personniMoveis.dto.ElementCmpDto.ElementCmpDto;
import com.br.personniMoveis.dto.SectionCmpDto.SectionCmpDto;
import com.br.personniMoveis.dto.SectionCmpDto.SectionCmpGetDto;
import com.br.personniMoveis.exception.BadRequestException;
import com.br.personniMoveis.mapper.SectionCmp.SectionCmpMapper;
import com.br.personniMoveis.model.category.Category;
import com.br.personniMoveis.model.productCmp.SectionCmp;
import com.br.personniMoveis.repository.CategoryRepository;
import com.br.personniMoveis.repository.SectionCmpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SectionCmpService {

    private final SectionCmpRepository sectionCmpRepository;
    private final CategoryRepository categoryRepository;

    private final ElementCmpService elementCmpService;

    @Autowired
    public SectionCmpService(SectionCmpRepository sectionCmpRepository, CategoryRepository categoryRepository, ElementCmpService elementCmpService)
    {
        this.sectionCmpRepository = sectionCmpRepository;
        this.categoryRepository = categoryRepository;
        this.elementCmpService = elementCmpService;
    }

    public List<SectionCmpGetDto> getAllSections() {
        List<SectionCmp> sections = sectionCmpRepository.findAll();
        List<SectionCmpGetDto> sectionDtos = sections.stream()
                .map(SectionCmpMapper.INSTANCE::SectionToSectionGetDto)
                .collect(Collectors.toList());
        return sectionDtos;
    }

    public SectionCmpGetDto findSectionByIdOrThrowBadRequestException(Long id, String exceptionMessage) {
        return SectionCmpMapper.INSTANCE.SectionToSectionGetDto(
                sectionCmpRepository.findById(id).orElseThrow(
                        () -> new BadRequestException(exceptionMessage)));
    }

    public void createSectionCmp(Set<SectionCmpDto> sectionCmpDtos, Long categoryId) {
        // Busca a categoria
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new BadRequestException("Category not found"));
        // Setando categorias para cada seção
        sectionCmpDtos.forEach(item -> item.setCategoryId(category.getCategoryId()));
        //Salvando seção
        Set<SectionCmp> newSection = SectionCmpMapper.INSTANCE.toSectionCmp(sectionCmpDtos);
        // Persiste a nova instância no banco de dados
        List<SectionCmp> newSectionList = sectionCmpRepository.saveAll(newSection);;

        Set<Long> sectionsIds = new HashSet<>();
        for (SectionCmp section : newSectionList) {
            sectionsIds.add(section.getSectionCmpId());
        }
        // Criando elementos relacionados, se necessário
        for (SectionCmpDto sectionCmpDto : sectionCmpDtos) {
            for (ElementCmpDto elementCmpDto : sectionCmpDto.getElementCmpDtos()) {
                if (!elementCmpDto.getName().isEmpty()) {
                    elementCmpService.createElementCmp(sectionCmpDto.getElementCmpDtos(), sectionsIds);
                }
            }
        }
    }

    public void updateSectionCmp(Set<SectionCmpDto> sectionCmpDtos, Long sectionCmpId) {
        // Faz alteracoes no produto.
        // Busca a categoria
        SectionCmp sectionCmp = sectionCmpRepository.findById(sectionCmpId).orElseThrow(() -> new BadRequestException("Section not found"));
        // Setando categorias para cada seção
        sectionCmpDtos.forEach(item -> item.setCategoryId(sectionCmp.getCategoryId()));
        Set<SectionCmp> SectionBeUpdated = SectionCmpMapper.INSTANCE.toSectionCmp(sectionCmpDtos);
        SectionBeUpdated.forEach(sectionCmpRepository::save);
    }


    public void deleteSectionCmpById(Long sectionCmpId) {
        // Econtra produto ou joga exceção.
        findSectionByIdOrThrowBadRequestException(sectionCmpId, "Category not found");
        // Deleta produto via id.
        sectionCmpRepository.deleteById(sectionCmpId);
    }
}
