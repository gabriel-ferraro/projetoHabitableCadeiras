package com.br.personniMoveis.controller;

import com.br.personniMoveis.dto.CategoryDto.CategoryCmpDto;
import com.br.personniMoveis.dto.CategoryDto.CategoryGetDto;
import com.br.personniMoveis.dto.product.CategoryDto;
import com.br.personniMoveis.dto.product.get.ProductGetDto;
import com.br.personniMoveis.model.category.Category;
import com.br.personniMoveis.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("category")
@CrossOrigin(origins = "*")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "Categoria", description = "Adquire a categoria CMP do id informado")
    @GetMapping(path = "/{categoryId}")
    public ResponseEntity<CategoryGetDto> getCategoryCmpById(@PathVariable("categoryId") Long categoryId) {
        return ResponseEntity.ok(categoryService.findCategoryCmpByIdOrThrowBadRequestException(categoryId));
    }

    @Operation(summary = "Categorias", description = "Lista todas as categorias")
    @GetMapping
    public ResponseEntity<List<CategoryGetDto>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @Operation(summary = "Adquire produtos na categoria (CMP ou não)", description = "Lista todos os produtos da categoria de id informado")
    @GetMapping(path = "/products-in-category/{categoryId}")
    public ResponseEntity<List<ProductGetDto>> getAllProductsInCategory(@PathVariable("categoryId") Long categoryId) {
        return ResponseEntity.ok(categoryService.getAllProductsInCategory(categoryId));
    }

    /**
     * Retorna uma categoria editada ou criada. Faz o processo de persist�ncia de toda a categoria e objetos que ela cont�m.
     *
     * @param categoryId Id da categoria. Deve ser enviado para fazer a edi��o, n�o enviado para fazer a cria��o.
     * @param dto        Dto com os dados necess�rios para fazer a cria��o das entidades em somente uma requisi��o.
     * @return Uma categoria editada.
     */
    @Operation(summary = "Cria/edita categoria do produto convencional", description = "Endpoint que recebe todo payload para criação ou edição do produto convencional e seus subitens")
    @PutMapping(path = "/create-full-product")
    public ResponseEntity<Category> createFullProduct(
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestBody @Valid CategoryDto dto) {
        return ResponseEntity.ok(categoryService.createOrUpdateRegularProduct(categoryId, dto));
    }

    @Operation(summary = "Cria categoria CMP", description = "recebe payload da categoria CMP e CRIA itens")
    @PostMapping(path = "/create-full-cmp")
    public ResponseEntity<HttpStatus> createCategoryCmp(@RequestBody @Valid CategoryCmpDto categoryCmpDto) {
        categoryService.createCategoryCmp(categoryCmpDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(summary = "Atualiza categoria CMP", description = "recebe payload da categoria CMP e ATUALIZA itens")
    @PutMapping(path = "/{cateogoryId}")
    public ResponseEntity<HttpStatus> updateProduct(@RequestBody @Valid CategoryCmpDto categorycmpDto, @PathVariable("cateogoryId") Long cateogoryId) {
        categoryService.updateCategoryCmp(categorycmpDto, cateogoryId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "deleta produto de uma categoria", description = "recebe o id de uma categoria e de um produto, faz a exclusao do produto nessa categoria")
    @DeleteMapping(path = "/{categoryId}")
    public ResponseEntity<HttpStatus> deleteProductById(@PathVariable("categoryId") Long categoryId) {
        categoryService.deleteCategoryById(categoryId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
