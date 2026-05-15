package searchservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import searchservice.document.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;
import java.util.UUID;

public interface ProductSearchRepository
        extends ElasticsearchRepository<ProductDocument, UUID> {

    List<ProductDocument> findByNameContainingOrDescriptionContaining(
            String name,
            String description
    );

    List<ProductDocument>
    findByNameContainingOrDescriptionContainingAndCategory(
            String name,
            String description,
            String category
    );

    List<ProductDocument>
    findByNameContainingOrDescriptionContainingAndCategoryAndBrand(
            String name,
            String description,
            String category,
            String brand
    );

    Page<ProductDocument>
    findByNameContainingOrDescriptionContaining(
            String name,
            String description,
            Pageable pageable
    );
}