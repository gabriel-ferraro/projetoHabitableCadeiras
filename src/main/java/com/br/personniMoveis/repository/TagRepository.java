package com.br.personniMoveis.repository;

import com.br.personniMoveis.model.product.Tag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findTagsByProductsProductId(Long productId);
}
