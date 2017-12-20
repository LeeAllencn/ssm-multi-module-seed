create database ssm-multi-module-seed default charset=utf8;
use ssm-multi-module-seed;
create table user(
    user_id int(4) not null auto_increment,
    username varchar(50) default null,
    password varchar(50) default null,
    sex varchar(4) default null,
    create_time datetime default null,
    update_time datetime default null,
    is_admin tinyint(1) default null,
    primary key(user_id)
)engine=innodb default charset=utf8;

insert into user values(1,"lee","123456","male","2017-12-15 13:51:05","2017-12-15 13:51:05",1);