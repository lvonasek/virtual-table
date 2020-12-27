DROP TABLE `cards`
DROP TABLE `players`
CREATE TABLE `cards` (`front` varchar(32) COLLATE utf8_unicode_ci NOT NULL,`back` varchar(32) COLLATE utf8_unicode_ci NOT NULL,`x` float NOT NULL,`y` float NOT NULL,`timestamp` bigint(20) NOT NULL);
CREATE TABLE `players` (`name` varchar(32) COLLATE utf8_unicode_ci NOT NULL,`password` varchar(32) COLLATE utf8_unicode_ci NOT NULL,`note` varchar(32) COLLATE utf8_unicode_ci NOT NULL,`x1` float NOT NULL,`y1` float NOT NULL,`x2` float NOT NULL,`y2` float NOT NULL);
