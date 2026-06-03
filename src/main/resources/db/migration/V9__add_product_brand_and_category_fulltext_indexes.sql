ALTER TABLE product
    ADD FULLTEXT KEY ft_product_brand (brand);

ALTER TABLE category
    ADD FULLTEXT KEY ft_category_name (name);
