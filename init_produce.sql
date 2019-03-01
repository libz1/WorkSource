/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50717
Source Host           : localhost:3306
Source Database       : init_produce

Target Server Type    : MYSQL
Target Server Version : 50717
File Encoding         : 65001

Date: 2018-09-30 17:35:59
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `param`
-- ----------------------------
DROP TABLE IF EXISTS `param`;
CREATE TABLE `param` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `computer` varchar(255) DEFAULT NULL,
  `keyname` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `note1` varchar(255) DEFAULT NULL,
  `note2` varchar(255) DEFAULT NULL,
  `serial` int(11) NOT NULL,
  `type` varchar(255) DEFAULT NULL,
  `value` varchar(255) DEFAULT NULL,
  `delaytime` int(11) NOT NULL,
  `waittime` int(11) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of param
-- ----------------------------
INSERT INTO `param` VALUES ('1', null, '29', '程序版本检测', null, null, '15', '验证', 'GW698.45', '35000', '2500');
INSERT INTO `param` VALUES ('2', null, '28', '集中器时钟检测', null, null, '20', '验证', '[误差<5分钟]', '100', '2500');
INSERT INTO `param` VALUES ('3', null, '2', '主站IP', null, null, '30', '设置', '172.017.017.057', '100', '2500');
INSERT INTO `param` VALUES ('4', null, '3', '主站端口', null, null, '40', '设置', '5060', '100', '2500');
INSERT INTO `param` VALUES ('5', null, '6', 'APN', null, null, '50', '设置', 'fjep.fj', '100', '2500');
INSERT INTO `param` VALUES ('6', null, '7', '终端地址', null, null, '60', '设置', '[SEQ=4]', '100', '2500');
INSERT INTO `param` VALUES ('7', null, '10', 'APN用户名', null, null, '70', '设置', '[APNNAME=6]', '100', '2500');
INSERT INTO `param` VALUES ('8', null, '11', 'APN密码', null, null, '80', '设置', '[APNPWD=6]', '100', '2500');
INSERT INTO `param` VALUES ('9', null, '12', '心跳周期', null, null, '90', '设置', '00060', '100', '2500');
INSERT INTO `param` VALUES ('10', null, '4', '备用主站IP', null, null, '100', '设置', '172.017.017.058', '100', '2500');
INSERT INTO `param` VALUES ('11', null, '5', '备用主站端口', null, null, '110', '设置', '4000', '100', '2500');
INSERT INTO `param` VALUES ('12', null, '19', '内部交采通信地址', null, null, '120', '设置', '000225001225', '100', '2500');
INSERT INTO `param` VALUES ('13', null, '20', '清交采', null, null, '130', '设置', '', '100', '2500');
INSERT INTO `param` VALUES ('15', null, 'cat /etc/macaddr', '检测MAC地址', null, null, '3', '验证', 'macaddr:02:%', '100', '2500');
INSERT INTO `param` VALUES ('16', null, 'dn 129.1.22.90', '更新程序', null, null, '5', '设置', '%成功', '100', '45000');
INSERT INTO `param` VALUES ('17', null, '99', '更新后重启集中器', null, null, '7', '设置', null, '100', '2500');

-- ----------------------------
-- Table structure for `teminaltestno`
-- ----------------------------
DROP TABLE IF EXISTS `teminaltestno`;
CREATE TABLE `teminaltestno` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `meterno` varchar(255) DEFAULT NULL,
  `stageno` varchar(255) DEFAULT NULL,
  `testno` int(11) NOT NULL,
  `yyyymm` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of teminaltestno
-- ----------------------------
INSERT INTO `teminaltestno` VALUES ('1', '0', '3', '1', '1809');
INSERT INTO `teminaltestno` VALUES ('2', '1', '3', '1', '1809');
INSERT INTO `teminaltestno` VALUES ('3', '2', '3', '1', '1809');
INSERT INTO `teminaltestno` VALUES ('4', '3', '3', '1', '1809');
INSERT INTO `teminaltestno` VALUES ('5', '4', '3', '1', '1809');
INSERT INTO `teminaltestno` VALUES ('6', '5', '3', '1', '1809');
INSERT INTO `teminaltestno` VALUES ('7', '6', '3', '1', '1809');
INSERT INTO `teminaltestno` VALUES ('8', '7', '3', '1', '1809');
INSERT INTO `teminaltestno` VALUES ('9', '8', '3', '1', '1809');
INSERT INTO `teminaltestno` VALUES ('10', '9', '3', '1', '1809');
INSERT INTO `teminaltestno` VALUES ('11', '10', '3', '1', '1809');
INSERT INTO `teminaltestno` VALUES ('12', '11', '3', '1', '1809');
INSERT INTO `teminaltestno` VALUES ('13', '12', '3', '1', '1809');
INSERT INTO `teminaltestno` VALUES ('14', '13', '3', '1', '1809');
INSERT INTO `teminaltestno` VALUES ('15', '14', '3', '1', '1809');
INSERT INTO `teminaltestno` VALUES ('16', '15', '3', '1', '1809');

-- ----------------------------
-- Table structure for `terminalinfo`
-- ----------------------------
DROP TABLE IF EXISTS `terminalinfo`;
CREATE TABLE `terminalinfo` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `addr` varchar(255) DEFAULT NULL,
  `barCode` varchar(255) DEFAULT NULL,
  `errcomputer` varchar(255) DEFAULT NULL,
  `errdatetime` varchar(255) DEFAULT NULL,
  `erroperater` varchar(255) DEFAULT NULL,
  `okcomputer` varchar(255) DEFAULT NULL,
  `okdatetime` varchar(255) DEFAULT NULL,
  `okoperater` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of terminalinfo
-- ----------------------------

-- ----------------------------
-- Table structure for `terminallog`
-- ----------------------------
DROP TABLE IF EXISTS `terminallog`;
CREATE TABLE `terminallog` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `addr` varchar(255) DEFAULT NULL,
  `errResult` varchar(255) DEFAULT NULL,
  `meterNo` varchar(255) DEFAULT NULL,
  `opBTime` varchar(255) DEFAULT NULL,
  `opETime` varchar(255) DEFAULT NULL,
  `opName` varchar(255) DEFAULT NULL,
  `result` varchar(255) DEFAULT NULL,
  `stageNo` varchar(255) DEFAULT NULL,
  `testNo` int(11) NOT NULL,
  `yyyymm` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of terminallog
-- ----------------------------
INSERT INTO `terminallog` VALUES ('1', '023018090001', '', '0', '2018-09-19 16:22:57:126', '', 'admin', '', '3', '1', '2018-09');
INSERT INTO `terminallog` VALUES ('2', '023118090001', '', '1', '2018-09-19 16:22:59:126', '', 'admin', '', '3', '1', '2018-09');
INSERT INTO `terminallog` VALUES ('3', '023218090001', '', '2', '2018-09-19 16:23:01:126', '', 'admin', '', '3', '1', '2018-09');
INSERT INTO `terminallog` VALUES ('4', '023318090001', '', '3', '2018-09-19 16:23:03:126', '', 'admin', '', '3', '1', '2018-09');
INSERT INTO `terminallog` VALUES ('5', '023418090001', '', '4', '2018-09-19 16:23:05:125', '', 'admin', '', '3', '1', '2018-09');
INSERT INTO `terminallog` VALUES ('6', '023518090001', '', '5', '2018-09-19 16:23:07:126', '', 'admin', '', '3', '1', '2018-09');
INSERT INTO `terminallog` VALUES ('7', '023618090001', '', '6', '2018-09-19 16:23:09:125', '', 'admin', '', '3', '1', '2018-09');
INSERT INTO `terminallog` VALUES ('8', '023718090001', '', '7', '2018-09-19 16:23:11:126', '', 'admin', '', '3', '1', '2018-09');
INSERT INTO `terminallog` VALUES ('9', '023818090001', '', '8', '2018-09-19 16:23:13:125', '', 'admin', '', '3', '1', '2018-09');
INSERT INTO `terminallog` VALUES ('10', '023918090001', '', '9', '2018-09-19 16:23:15:125', '', 'admin', '', '3', '1', '2018-09');
INSERT INTO `terminallog` VALUES ('11', '023A18090001', '', '10', '2018-09-19 16:23:17:126', '', 'admin', '', '3', '1', '2018-09');
INSERT INTO `terminallog` VALUES ('12', '023B18090001', '', '11', '2018-09-19 16:23:19:125', '', 'admin', '', '3', '1', '2018-09');
INSERT INTO `terminallog` VALUES ('13', '023C18090001', '', '12', '2018-09-19 16:23:21:126', '', 'admin', '', '3', '1', '2018-09');
INSERT INTO `terminallog` VALUES ('14', '023D18090001', '', '13', '2018-09-19 16:23:23:125', '', 'admin', '', '3', '1', '2018-09');
INSERT INTO `terminallog` VALUES ('15', '023E18090001', '', '14', '2018-09-19 16:23:25:126', '', 'admin', '', '3', '1', '2018-09');
INSERT INTO `terminallog` VALUES ('16', '023F18090001', '', '15', '2018-09-19 16:23:27:126', '', 'admin', '', '3', '1', '2018-09');

-- ----------------------------
-- Table structure for `terminallogdetail`
-- ----------------------------
DROP TABLE IF EXISTS `terminallogdetail`;
CREATE TABLE `terminallogdetail` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `addr` varchar(255) DEFAULT NULL,
  `analys` varchar(255) DEFAULT NULL,
  `caseno` int(11) NOT NULL,
  `computer` varchar(255) DEFAULT NULL,
  `delaytime` int(11) NOT NULL,
  `expect` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  `port` varchar(255) DEFAULT NULL,
  `protocol` varchar(255) DEFAULT NULL,
  `recv` varchar(255) DEFAULT NULL,
  `recvtime` varchar(255) DEFAULT NULL,
  `result` varchar(255) DEFAULT NULL,
  `retrys` int(11) NOT NULL,
  `runID` int(11) NOT NULL,
  `send` varchar(255) DEFAULT NULL,
  `sendtime` varchar(255) DEFAULT NULL,
  `sendtime0` varchar(255) DEFAULT NULL,
  `sendtimes` int(11) NOT NULL,
  `subid` varchar(255) DEFAULT NULL,
  `waittime` int(11) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of terminallogdetail
-- ----------------------------
INSERT INTO `terminallogdetail` VALUES ('1', '023018090001', null, '1', null, '0', '84969601', null, null, null, null, '', null, '', '0', '1', '6800008101160B68141204969601023018090001180919162257E716', '2018-09-19 16:22:57:126', '2018-09-19 16:22:57:126', '0', null, '0');
INSERT INTO `terminallogdetail` VALUES ('2', '023118090001', null, '1', null, '0', '84969601', null, null, null, null, '', null, '', '0', '2', '6800008101160C68141204969601023118090001180919162259EB16', '2018-09-19 16:22:59:126', '2018-09-19 16:22:59:126', '0', null, '0');
INSERT INTO `terminallogdetail` VALUES ('3', '023218090001', null, '1', null, '0', '84969601', null, null, null, null, '', null, '', '0', '3', '6800008101160D681412049696010232180900011809191623019616', '2018-09-19 16:23:01:126', '2018-09-19 16:23:01:126', '0', null, '0');
INSERT INTO `terminallogdetail` VALUES ('4', '023318090001', null, '1', null, '0', '84969601', null, null, null, null, '', null, '', '0', '4', '6800008101160E681412049696010233180900011809191623039A16', '2018-09-19 16:23:03:126', '2018-09-19 16:23:03:126', '0', null, '0');
INSERT INTO `terminallogdetail` VALUES ('5', '023418090001', null, '1', null, '0', '84969601', null, null, null, null, '', null, '', '0', '5', '6800008101160F681412049696010234180900011809191623059E16', '2018-09-19 16:23:05:125', '2018-09-19 16:23:05:125', '0', null, '0');
INSERT INTO `terminallogdetail` VALUES ('6', '023518090001', null, '1', null, '0', '84969601', null, null, null, null, '', null, '', '0', '6', '6800008101161068141204969601023518090001180919162307A216', '2018-09-19 16:23:07:126', '2018-09-19 16:23:07:126', '0', null, '0');
INSERT INTO `terminallogdetail` VALUES ('7', '023618090001', null, '1', null, '0', '84969601', null, null, null, null, '', null, '', '0', '7', '6800008101161168141204969601023618090001180919162309A616', '2018-09-19 16:23:09:125', '2018-09-19 16:23:09:125', '0', null, '0');
INSERT INTO `terminallogdetail` VALUES ('8', '023718090001', null, '1', null, '0', '84969601', null, null, null, null, '', null, '', '0', '8', '6800008101161268141204969601023718090001180919162311B016', '2018-09-19 16:23:11:126', '2018-09-19 16:23:11:126', '0', null, '0');
INSERT INTO `terminallogdetail` VALUES ('9', '023818090001', null, '1', null, '0', '84969601', null, null, null, null, '', null, '', '0', '9', '6800008101161368141204969601023818090001180919162313B416', '2018-09-19 16:23:13:125', '2018-09-19 16:23:13:125', '0', null, '0');
INSERT INTO `terminallogdetail` VALUES ('10', '023918090001', null, '1', null, '0', '84969601', null, null, null, null, '', null, '', '0', '10', '6800008101161468141204969601023918090001180919162315B816', '2018-09-19 16:23:15:125', '2018-09-19 16:23:15:125', '0', null, '0');
INSERT INTO `terminallogdetail` VALUES ('11', '023A18090001', null, '1', null, '0', '84969601', null, null, null, null, '', null, '', '0', '11', '6800008101161568141204969601023A18090001180919162317BC16', '2018-09-19 16:23:17:126', '2018-09-19 16:23:17:126', '0', null, '0');
INSERT INTO `terminallogdetail` VALUES ('12', '023B18090001', null, '1', null, '0', '84969601', null, null, null, null, '', null, '', '0', '12', '6800008101161668141204969601023B18090001180919162319C016', '2018-09-19 16:23:19:125', '2018-09-19 16:23:19:125', '0', null, '0');
INSERT INTO `terminallogdetail` VALUES ('13', '023C18090001', null, '1', null, '0', '84969601', null, null, null, null, '', null, '', '0', '13', '6800008101161768141204969601023C18090001180919162321CA16', '2018-09-19 16:23:21:126', '2018-09-19 16:23:21:126', '0', null, '0');
INSERT INTO `terminallogdetail` VALUES ('14', '023D18090001', null, '1', null, '0', '84969601', null, null, null, null, '', null, '', '0', '14', '6800008101161868141204969601023D18090001180919162323CE16', '2018-09-19 16:23:23:125', '2018-09-19 16:23:23:125', '0', null, '0');
INSERT INTO `terminallogdetail` VALUES ('15', '023E18090001', null, '1', null, '0', '84969601', null, null, null, null, '', null, '', '0', '15', '6800008101161968141204969601023E18090001180919162325D216', '2018-09-19 16:23:25:126', '2018-09-19 16:23:25:126', '0', null, '0');
INSERT INTO `terminallogdetail` VALUES ('16', '023F18090001', null, '1', null, '0', '84969601', null, null, null, null, '', null, '', '0', '16', '6800008101161A68141204969601023F18090001180919162327D616', '2018-09-19 16:23:27:126', '2018-09-19 16:23:27:126', '0', null, '0');

-- ----------------------------
-- Table structure for `usermanager`
-- ----------------------------
DROP TABLE IF EXISTS `usermanager`;
CREATE TABLE `usermanager` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `userPriority` int(11) NOT NULL,
  `userid` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  `userpwd` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of usermanager
-- ----------------------------
INSERT INTO `usermanager` VALUES ('1', '1', 'admin', 'admin', '6867');
