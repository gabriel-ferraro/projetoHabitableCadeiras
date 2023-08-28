package com.br.personniMoveis.model.product;

import com.br.personniMoveis.model.category.Category;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Mapeamento ORM para produto.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "product_id")
    private Long productId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double value;

    @Column(nullable = false)
    private Long quantity;

    @Column(nullable = false)
    @Builder.Default
    private Boolean editable = false; // Produto não é editável por padrão.

    @Column(name = "img_url")
    private String imgUrl;

    private String description;

    /**
     * Details são campos descritivos do produto, exemplo: peso do produto - A cadeira X é leve e tem só x kg.
     */
    @OneToMany(orphanRemoval = true, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "product_id")
    private final Set<Detail> details = new HashSet<>();

    /**
     * Um móvel pode ter somente um material (material é o que compõe a maior parte do produto, como madeira de algum tipo, metal, etc).
     */
    @ManyToMany
    @JoinTable(name = "product_material", joinColumns = @JoinColumn(name = "product_id"), inverseJoinColumns = @JoinColumn(name = "material_id"))
    private final Set<Material> materials = new HashSet<>();

    @OneToMany(orphanRemoval = true, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "product_id")
    private final Set<Section> sections = new HashSet<>();

    /**
     * As tags podem estar em produtos diferentes, não pertencem a um produto específico, exemplos de tags: escritório, cozinha, sala de estar, etc.
     */
    @ManyToMany
    @JoinTable(name = "product_tag", joinColumns = @JoinColumn(name = "product_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private final Set<Tag> tags = new HashSet<>();

    /**
     * Produto pode ser de SOMENTE UMA categoria, ou de nehuma, como: caderias, armários, mesas, etc...
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Override
    public int hashCode() {
        return Objects.hash(productId);
    }
}
