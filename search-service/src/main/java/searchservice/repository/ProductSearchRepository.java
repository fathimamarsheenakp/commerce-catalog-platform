package searchservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import searchservice.document.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;
import java.util.UUID;

public interface ProductSearchRepository
        extends ElasticsearchRepository<ProductDocument, UUID> {

    List<ProductDocument> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String name,
            String description
    );

    List<ProductDocument> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndCategory(
            String name,
            String description,
            String category
    );

    List<ProductDocument> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndCategoryAndBrand(
            String name,
            String description,
            String category,
            String brand
    );

    Page<ProductDocument> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String name,
            String description,
            Pageable pageable
    );

    Page<ProductDocument>
    findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrBrandContainingIgnoreCaseOrCategoryContainingIgnoreCase(
            String name,
            String description,
            String brand,
            String category,
            Pageable pageable
    );

    Page<ProductDocument> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndCategory(
            String name,
            String description,
            String category,
            Pageable pageable
    );

    Page<ProductDocument> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndCategoryAndBrand(
            String name,
            String description,
            String category,
            String brand,
            Pageable pageable
    );

    Page<ProductDocument> findByCategory(String category, Pageable pageable);

    Page<ProductDocument> findByBrand(String brand, Pageable pageable);

    Page<ProductDocument> findByCategoryAndBrand(String category, String brand, Pageable pageable);
}
