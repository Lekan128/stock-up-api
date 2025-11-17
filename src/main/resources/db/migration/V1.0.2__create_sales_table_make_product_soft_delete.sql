CREATE TABLE sales (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL,
    sold_price DOUBLE PRECISION NOT NULL,
    quantity INT NOT NULL,
    total_amount DOUBLE PRECISION NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_sales_product FOREIGN KEY (product_id)
        REFERENCES product (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

-- Optional index to speed up date and product filtering
CREATE INDEX idx_sales_created_at ON sales (created_at);
CREATE INDEX idx_sales_product_id ON sales (product_id);

ALTER TABLE product
    ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE;
