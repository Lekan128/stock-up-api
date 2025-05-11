CREATE TABLE category (
    id UUID PRIMARY KEY,
    name VARCHAR(50)
);

CREATE TABLE product (
    id UUID PRIMARY KEY,
    name VARCHAR(100),
    category_id UUID NOT NULL REFERENCES category,
    tags VARCHAR(10) ARRAY,
    number_available integer,
    price NUMERIC(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE product_tags (
    product_id UUID REFERENCES product NOT NULL,
    tag VARCHAR(15) NOT NULL
)