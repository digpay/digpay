CREATE DATABASE notifierdb;
CREATE USER 'notifieruser'@'localhost' IDENTIFIED BY '1234567890A';
GRANT ALL PRIVILEGES ON notifierdb.* TO 'notifieruser'@'localhost';
FLUSH PRIVILEGES;

INSERT INTO `notifierdb`.`merchant`
(`mid`,
`apiKey`,
`requiredConfirmations`,
`url`)
VALUES
(
'1',
'ABCDEF',
'1',
'http://server.com'	
);


INSERT INTO `notifierdb`.`sale`
(`id`,
`currency`,
`status`,
`price`,
`description`,
`MERCHANT_ID`)
VALUES
(
'1',
'BTC',
'PENDING',
'123456',
'Item1',
'1'
);

INSERT INTO `notifierdb`.`sale`
(`id`,
`currency`,
`status`,
`price`,
`description`,
`MERCHANT_ID`)
VALUES
(
'2',
'BTC',
'PENDING',
'345678',
'Item1',
'1'
);


INSERT INTO `notifierdb`.`address`
(`btcAddress`,
`MERCHANT_ID`,
`sale_id`)
VALUES
(
'1dice8EMZmqKvrGE4Qc9bUFf9PX3xaYDp',
'1',
'1'
);

INSERT INTO `notifierdb`.`address`
(`btcAddress`,
`MERCHANT_ID`,
`sale_id`)
VALUES
(
'1dice97ECuByXAvqXpaYzSaQuPVvrtmz6',
'1',
'2'
);