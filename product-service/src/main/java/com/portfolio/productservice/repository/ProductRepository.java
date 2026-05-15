package com.portfolio.productservice.repository;

import com.portfolio.productservice.model.Product;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.UUID;

public interface ProductRepository extends CassandraRepository<Product, UUID> {
}