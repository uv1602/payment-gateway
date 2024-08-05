CREATE TABLE payment_gateways (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT
);

CREATE TABLE banks (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);


CREATE TABLE amcs (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE payment_methods (
    id SERIAL PRIMARY KEY,
    method VARCHAR(255) NOT NULL
);

CREATE TABLE gateway_weights (
    gateway_id INT REFERENCES payment_gateways(id),
    weight INT NOT NULL,
    PRIMARY KEY (gateway_id)
);

CREATE TABLE gateway_banks (
    id SERIAL PRIMARY KEY,
    gateway_id INT REFERENCES payment_gateways(id),
    bank_id INT REFERENCES banks(id)
);

CREATE TABLE gateway_amcs (
    id SERIAL PRIMARY KEY,
    gateway_id INT REFERENCES payment_gateways(id),
    amc_id INT REFERENCES amcs(id),
    payment_method_id INT REFERENCES payment_methods(id)
);

