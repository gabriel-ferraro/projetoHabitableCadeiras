package com.br.personniMoveis.service.product;

import com.br.personniMoveis.dto.product.DetailDto;
import com.br.personniMoveis.dto.product.DetailGetDto;
import com.br.personniMoveis.dto.product.ProductDto;
import com.br.personniMoveis.dto.product.ProductGetDto;
import com.br.personniMoveis.exception.AlreadyExistsException;
import com.br.personniMoveis.exception.ResourceNotFoundException;
import com.br.personniMoveis.mapper.product.DetailMapper;
import com.br.personniMoveis.mapper.product.ProductMapper;
import com.br.personniMoveis.model.category.Category;
import com.br.personniMoveis.model.product.Detail;
import com.br.personniMoveis.model.product.Product;
import com.br.personniMoveis.model.product.Tag;
import com.br.personniMoveis.repository.ProductRepository;
import com.br.personniMoveis.service.CategoryService;
import com.br.personniMoveis.service.DetailService;
import com.br.personniMoveis.utils.ValidationUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final DetailService detailService;
    private final TagService tagService;
    private final CategoryService categoryService;

    @Autowired
    public ProductService(ProductRepository productRepository, DetailService detailService, TagService tagService, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.detailService = detailService;
        this.tagService = tagService;
        this.categoryService = categoryService;
    }

    public Product findProductOrThrowNotFoundException(Long id) {
        return productRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Produto não encontrado."));
    }

    private Tag findTagInProductOrThrowNotFoundException(Product product, Long tagId) {
        return product.getTags().stream().filter(tag -> tag.getTagId().equals(tagId)).findAny().orElseThrow(
                () -> new ResourceNotFoundException("Tag não encontrada no produto."));
    }

    private Detail findDetailInProductOrThrowNotfoundException(Product product, Long detailId) {
        return product.getDetails().stream().filter(detail -> detail.getDetailId().equals(detailId)).findAny().orElseThrow(
                () -> new ResourceNotFoundException("Detalhe não encontrada no produto."));
    }

    /**
     * Retorna os produtos paginados.
     *
     * @param pageable
     * @return
     */
    public Page<ProductGetDto> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(
                ProductMapper.INSTANCE::productToProductGetDto);
    }

    public List<ProductGetDto> getAllProducts() {
        return productRepository.findAll().stream().map(
                ProductMapper.INSTANCE::productToProductGetDto).toList();
    }

    public List<ProductGetDto> getAllProductsWithTagId(Long tagId) {
        tagService.findTagOrThrowNotFoundException(tagId);
        return productRepository.findProductsTag(tagId);
    }

    public List<Tag> getAllTagsFromProduct(Long productId) {
        this.findProductOrThrowNotFoundException(productId);
        return productRepository.findTagsFromProduct(productId);
    }

    public ProductGetDto createProduct(ProductDto productDto) {
        Product newProduct = ProductMapper.INSTANCE.toProduct(productDto);
        // persiste no BD.
        Product product = productRepository.save(newProduct);
        // retorna ProductGetDto.
        return ProductMapper.INSTANCE.productToProductGetDto(product);
    }

    public List<DetailGetDto> getAllDetailsFromProduct(Long productId) {
        Product product = findProductOrThrowNotFoundException(productId);
        return product.getDetails().stream().map(DetailMapper.INSTANCE::detailToDetailGetDto).toList();
    }

    public void assignCategoryToProduct(Long productId, Long categoryId) {
        // Recupera product e category.
        Product product = this.findProductOrThrowNotFoundException(productId);
        Category category = categoryService.findCategoryOrThrowNotFoundException(categoryId);
        // Faz set da categoria no produto. O produto só deve conter uma categoria por vez.
        product.setCategory(category);
    }

    /**
     * Persiste detalhe e insere no produto.
     *
     * @param productId     id do produto.
     * @param detailDto Detail que se deseja criar e associar ao produto.
     */
    @Transactional
    public Detail assignDetailToProduct(Long productId, DetailDto detailDto) {
        Product product = findProductOrThrowNotFoundException(productId);
        // Adquire model de detail.
        Detail detail = DetailMapper.INSTANCE.toDetail(detailDto);
        detail.setProduct(product);
        // Faz associação.
        product.getDetails().add(detail);
        // Retorna detail criado e associado.
        return detail;
    }

    public void updateDetail(Long productId, Long detailId, DetailDto detailDto) {
        Product product = findProductOrThrowNotFoundException(productId);
        findDetailInProductOrThrowNotfoundException(product, detailId);
        Detail newDetail = DetailMapper.INSTANCE.toDetail(detailDto);
        newDetail.setDetailId(detailId);
        detailService.updateDetail(newDetail);
    }

    @Transactional
    public void removeDetailInProduct(Long productId, Long detailId) {
        Product product = findProductOrThrowNotFoundException(productId);
        Detail detail = findDetailInProductOrThrowNotfoundException(product, detailId);
        product.getDetails().remove(detail);
    }

    /**
     * Associa uma tag a um produto.
     *
     * @param productId Id do produto
     */
    @Transactional
    public void assignTagToProduct(Long productId, Long tagId) {
        // Recupera tag e product do BD.
        Product product = this.findProductOrThrowNotFoundException(productId);
        Tag tag = tagService.findTagOrThrowNotFoundException(tagId);
        // Se produto já tem a tag.
        if (product.getTags().contains(tag)) {
            throw new AlreadyExistsException("Produto já tem a tag.");
        }
        // Faz associação entre tag e produto no BD, depois persiste.
        product.getTags().add(tag);
        tag.getProducts().add(product);
    }

    public void updateProduct(ProductDto productDto, Long productId) {
        // Encontra produto existente para atualiza-lo ou joga exceção.
        this.findProductOrThrowNotFoundException(productId);
        // Faz alteracoes no produto.
        Product productToBeUpdated = ProductMapper.INSTANCE.toProduct(productDto);
        productToBeUpdated.setProductId(productId);
        // Persiste alteracoes.
        productRepository.save(productToBeUpdated);
    }

    @Transactional
    public void deleteProductById(Long productId) {
        // Remove todas as tags do produto.
        this.removeAllTagsInProduct(productId);
        productRepository.deleteById(productId);
    }

    @Transactional
    public void removeTagInProduct(Long productId, Long tagId) {
        // Encontra produto e tag.
        Product product = this.findProductOrThrowNotFoundException(productId);
        Tag tag = this.findTagInProductOrThrowNotFoundException(product, tagId);
        // Remove tag e atualiza produto.
        product.getTags().remove(tag);
    }

    @Transactional
    public void removeAllTagsInProduct(Long productId) {
        Product product = this.findProductOrThrowNotFoundException(productId);
        // Remove todas as tags do produto e salva alterações.
        product.getTags().clear();
    }
}
