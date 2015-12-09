-- MySQL Script generated by MySQL Workbench
-- Пн. 07 дек. 2015 19:02:41
-- Model: New Model    Version: 1.0
-- MySQL Workbench Forward Engineering

-- -----------------------------------------------------
-- Table `User`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `User` (
  `id` INTEGER,
  `googleToken` STRING NOT NULL UNIQUE,
  `userType` INT NOT NULL,
  `nicknameStr` STRING NOT NULL,
  `nicknameId` STRING NOT NULL,
  `color` INT UNSIGNED NOT NULL,
  `invitationTarget`  NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`invitationTarget`)
  REFERENCES `User` (`id`)
  ON DELETE CASCADE
  ON UPDATE CASCADE
);

-- -----------------------------------------------------
-- Table `Contacts`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `Contacts` (
  `user` INT UNSIGNED NOT NULL,
  `friend` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`user`, `friend`),
  FOREIGN KEY (`user` , `friend`)
  REFERENCES `User` (`id` , `id`)
  ON DELETE CASCADE
  ON UPDATE CASCADE
);


-- -----------------------------------------------------
-- Table `Game`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `Game` (
  `id` INTEGER,
  `playerCrosses` INT UNSIGNED NOT NULL,
  `playerZeroes` INT UNSIGNED NOT NULL,
  `status` INT NOT NULL,
  `gameDate` TEXT NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`playerCrosses` , `playerZeroes`)
  REFERENCES `User` (`id` , `id`)
  ON DELETE CASCADE
  ON UPDATE CASCADE
);


-- -----------------------------------------------------
-- Table `Turn`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `Turn` (
  `game` INT UNSIGNED NOT NULL,
  `turnNo` INT UNSIGNED NOT NULL,
  `type` INT NOT NULL,
  `x` INT NULL,
  `y` INT NULL,
  PRIMARY KEY (`game`, `turnNo`),
  FOREIGN KEY (`game`)
  REFERENCES `Game` (`id`)
  ON DELETE CASCADE
  ON UPDATE CASCADE
);

