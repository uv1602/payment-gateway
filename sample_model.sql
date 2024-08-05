INSERT INTO payment_gateways (name, description) VALUES
('Gateway A', 'Description for Gateway A'),
('Gateway B', 'Description for Gateway B'),
('Gateway C', 'Description for Gateway C');


INSERT INTO banks (name) VALUES
('Bank X'),
('Bank Y'),
('Bank Z');


INSERT INTO amcs (name) VALUES
('AMC Alpha'),
('AMC Beta'),
('AMC Gamma');


INSERT INTO payment_methods (method) VALUES
('Credit Card'),
('Debit Card'),
('Bank Transfer');


INSERT INTO gateway_banks (gateway_id, bank_id) VALUES
(1, 1),  -- Gateway A supports Bank X
(1, 2),  -- Gateway A supports Bank Y
(2, 2),  -- Gateway B supports Bank Y
(2, 3),  -- Gateway B supports Bank Z
(3, 1);  -- Gateway C supports Bank X


INSERT INTO gateway_amcs (gateway_id, amc_id, payment_method_id) VALUES
(1, 1, 1),  -- Gateway A supports AMC Alpha with Credit Card
(1, 2, 2),  -- Gateway A supports AMC Beta with Debit Card
(2, 2, 3),  -- Gateway B supports AMC Beta with Bank Transfer
(3, 1, 1),  -- Gateway C supports AMC Alpha with Credit Card
(3, 3, 2);  -- Gateway C supports AMC Gamma with Debit Card


INSERT INTO gateway_weights (gateway_id, weight) VALUES
(1, 10),  -- Gateway A weight 10
(2, 20),  -- Gateway B weight 20
(3, 15);  -- Gateway C weight 15
