-- Insert Payment Gateways
INSERT INTO payment_gateway (name, description) VALUES
('Gateway1', 'Description for Gateway1'),
('Gateway2', 'Description for Gateway2'),
('Gateway3', 'Description for Gateway3');

-- Insert AMCs
INSERT INTO amc (name) VALUES
('AMC1'),
('AMC2');

-- Insert Payment Methods
INSERT INTO payment_method (method) VALUES
('Credit Card'),
('Debit Card');

-- Insert Gateway Mappings
INSERT INTO gateway_mapping (gateway_id, amc_id, payment_method_id, weight) VALUES
(1, 1, 1, 10),
(1, 1, 2, 5),
(2, 1, 1, 15),
(2, 2, 2, 7),
(3, 2, 1, 8);
