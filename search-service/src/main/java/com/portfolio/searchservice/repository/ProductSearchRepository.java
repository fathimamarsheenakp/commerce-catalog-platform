package com.portfolio.searchservice.repository;

import com.portfolio.searchservice.document.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.UUID;

public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, UUID> {
}