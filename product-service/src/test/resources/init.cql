CREATE KEYSPACE IF NOT EXISTS product_catalog
    WITH replication = {
        'class': 'SimpleStrategy',
        'replication_factor': 1
        };