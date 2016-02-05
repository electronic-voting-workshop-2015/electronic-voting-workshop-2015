SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

CREATE SCHEMA IF NOT EXISTS `mydb` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci ;
USE `mydb` ;

-- -----------------------------------------------------
-- Table `mydb`.`ballot_boxes`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`ballot_boxes` (
  `ballot_box_id` INT NOT NULL AUTO_INCREMENT,
  `public_key` BIGINT NULL,
  PRIMARY KEY (`ballot_box_id`),
  UNIQUE INDEX `ballot_box_id_UNIQUE` (`ballot_box_id` ASC, `public_key` ASC))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`votes`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`votes` (
  `idvotes` INT NOT NULL AUTO_INCREMENT,
  `vote_val` BIGINT NOT NULL,
  `time` DATETIME NOT NULL,
  `ballot_box_id` INT(11) NOT NULL,
  PRIMARY KEY (`idvotes`),
  UNIQUE INDEX `idvotes_UNIQUE` (`idvotes` ASC),
  UNIQUE INDEX `vote_val_UNIQUE` (`vote_val` ASC),
  INDEX `fk_votes_1_idx` (`ballot_box_id` ASC),
  CONSTRAINT `fk_ballotbox_id`
    FOREIGN KEY (`ballot_box_id`)
    REFERENCES `mydb`.`ballot_boxes` (`ballot_box_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`log_sources`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`log_sources` (
  `source_type` INT NOT NULL AUTO_INCREMENT,
  `description` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`source_type`),
  UNIQUE INDEX `source_type_UNIQUE` (`source_type` ASC))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `mydb`.`log`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `mydb`.`log` (
  `idLog` INT NOT NULL AUTO_INCREMENT,
  `time` DATETIME NOT NULL,
  `value` VARCHAR(4000) NOT NULL,
  `source_type` INT NOT NULL,
  `source_information` VARCHAR(45) NULL,
  PRIMARY KEY (`idLog`),
  INDEX `fk_log_sources_idx` (`source_type` ASC),
  UNIQUE INDEX `idLog_UNIQUE` (`idLog` ASC),
  CONSTRAINT `fk_log_sources`
    FOREIGN KEY (`source_type`)
    REFERENCES `mydb`.`log_sources` (`source_type`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- -----------------------------------------------------
-- Data for table `mydb`.`log_sources`
-- -----------------------------------------------------
START TRANSACTION;
USE `mydb`;
INSERT INTO `mydb`.`log_sources` (`source_type`, `description`) VALUES (1, 'BB');
INSERT INTO `mydb`.`log_sources` (`source_type`, `description`) VALUES (2, 'Ballot Committee');
INSERT INTO `mydb`.`log_sources` (`source_type`, `description`) VALUES (3, 'Mixnet');
INSERT INTO `mydb`.`log_sources` (`source_type`, `description`) VALUES (4, 'Verifier');
INSERT INTO `mydb`.`log_sources` (`source_type`, `description`) VALUES (5, 'Voter Registration');

COMMIT;

USE `mydb`;

DELIMITER $$
USE `mydb`$$
CREATE TRIGGER `votes_PREVENT_DEL` BEFORE DELETE ON `votes` FOR EACH ROW
begin
signal sqlstate '45000'
set message_text = 'Deleting values from this table is not allowed';
end $$

USE `mydb`$$
CREATE TRIGGER `log_PREVENT_DEL` BEFORE DELETE ON `log` FOR EACH ROW
begin
signal sqlstate '45000'
set message_text = 'Deleting values from this table is not allowed';
end $$


DELIMITER ;
