package com.br.personniMoveis.service.product;

import com.br.personniMoveis.dto.product.*;
import com.br.personniMoveis.dto.product.get.ProductGetDto;
import com.br.personniMoveis.exception.AlreadyExistsException;
import com.br.personniMoveis.exception.ResourceNotFoundException;
import com.br.personniMoveis.mapper.SectionCmp.SectionCmpMapper;
import com.br.personniMoveis.mapper.product.*;
import com.br.personniMoveis.model.product.*;
import com.br.personniMoveis.repository.ProductRepository;
import com.br.personniMoveis.service.DetailService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final DetailService detailService;
    private final MaterialService materialService;
    private final TagService tagService;
    private final SectionService sectionService;

    @Autowired
    public ProductService(ProductRepository productRepository, DetailService detailService, TagService tagService,
                          MaterialService materialService, SectionService sectionService) {
        this.productRepository = productRepository;
        this.detailService = detailService;
        this.tagService = tagService;
        this.materialService = materialService;
        this.sectionService = sectionService;
    }

    public Product findProductOrThrowNotFoundException(Long id) {
        return productRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Produto não encontrado."));
    }

    private Detail findDetailInProductOrThrowNotfoundException(Product product, Long detailId) {
        return product.getDetails().stream().filter(detail -> detail.getDetailId().equals(detailId)).findAny().orElseThrow(
                () -> new ResourceNotFoundException("Detalhe não encontrada no produto."));
    }

    private Tag findTagInProductOrThrowNotFoundException(Product product, Long tagId) {
        return product.getTags().stream().filter(tag -> tag.getTagId().equals(tagId)).findAny().orElseThrow(
                () -> new ResourceNotFoundException("Tag não encontrada no produto."));
    }

    /**
     * Retorna todos produtos.
     * @return Lista de todos produtos.
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<ProductGetDto> getAllProductsWithTagId(Long tagId) {
        tagService.findTagOrThrowNotFoundException(tagId);
        return productRepository.findProductsInTag(tagId);
    }

    public List<Tag> getAllTagsFromProduct(Long productId) {
        this.findProductOrThrowNotFoundException(productId);
        return productRepository.findTagsFromProduct(productId);
    }

    @Transactional
    public void createProductMaterials(Set<Product> products) {
        products.forEach(product -> product.getMaterials().forEach(materialService::createMaterial));
    }

    @Transactional
    public void createProductTags(Set<Product> products) {
        products.forEach(product -> product.getTags().forEach(tagService::createTag));
    }

    @Transactional
    public void createProductMaterial(Set<Material> materials) {
        materials.forEach(materialService::createMaterial);
    }

    @Transactional
    public void createProductTag(Set<Tag> tags) {
        tags.forEach(tagService::createTag);
    }

    @Transactional
    public void createProduct(Product product) {
        productRepository.save(product);
    }

    public Product createProduct(ProductDto productDto) {
        return productRepository.save(ProductMapper.INSTANCE.productDtoToProduct(productDto));
    }

    public List<DetailDto> getAllDetailsFromProduct(Long productId) {
        Product product = findProductOrThrowNotFoundException(productId);
        return product.getDetails().stream().map(DetailMapper.INSTANCE::detailToDetailGetDto).toList();
    }

    /**
     * Persiste detalhe e insere no produto.
     *
     * @param productId id do produto.
     * @param detailDto Detail que se deseja criar e associar ao produto.
     */
    @Transactional
    public Detail assignDetailToProduct(Long productId, DetailDto detailDto) {
        Product product = findProductOrThrowNotFoundException(productId);
        // Adquire model de detail.
        Detail detail = DetailMapper.INSTANCE.detailDtoToDetail(detailDto);
        // Faz associação.
        detail.setProduct(product);
        product.getDetails().add(detail);
        // Retorna detail criado e associado.
        return detail;
    }

    public Detail createDetail(DetailDto detail) {
        return detailService.createDetail(DetailMapper.INSTANCE.detailDtoToDetail(detail));
    }

    public Material createMaterial(MaterialDto materialDto) {
        return materialService.createMaterial(MaterialMapper.INSTANCE.materialDtoToMaterial(materialDto));
    }

    public Tag createTag(TagDto tagDto) {
        return tagService.createTag(TagMapper.INSTANCE.tagDtoToTag(tagDto));
    }

    public Section createSection(SectionDto sectionDto) {
        return sectionService.createSection(SectionCmpMapper.INSTANCE.sectionDtoToSection(sectionDto));
    }

    public Option createOption(OptionDto option) {
        return sectionService.createOption(OptionMapper.INSTANCE.optionDtoToOption(option));
    }

    public void updateDetail(Long productId, Long detailId, DetailDto detailDto) {
        Product product = findProductOrThrowNotFoundException(productId);
        findDetailInProductOrThrowNotfoundException(product, detailId);
        Detail newDetail = DetailMapper.INSTANCE.detailDtoToDetail(detailDto);
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
        // Faz associação entre tag e produto no BD.
        product.getTags().add(tag);
        tag.getProducts().add(product);
    }

    public void updateProduct(ProductDto productDto, Long productId) {
        // Encontra produto existente para atualiza-lo ou joga exceção.
        this.findProductOrThrowNotFoundException(productId);
        // Faz alteracoes no produto.
        Product productToBeUpdated = ProductMapper.INSTANCE.productDtoToProduct(productDto);
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
