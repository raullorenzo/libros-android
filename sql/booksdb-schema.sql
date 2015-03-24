 drop database if exists booksdb;
create database booksdb;
 
use booksdb;
 
create table users (
username varchar(20) not null primary key,
password char(32) not null,
name varchar(70) not null,
email varchar(255) not null
);
 
create table user_roles (
username varchar(20) not null,
rolename varchar(20) not null,
foreign key(username) references users(username) on delete cascade,
primary key (username, rolename)
);
 
create table libro (
id int not null auto_increment primary key,
titulo varchar(100) not null,
lengua varchar(20) not null,
edicion varchar(50) not null,
fecha_edicion datetime not null default current_timestamp,
fecha_impresion timestamp default current_timestamp ON UPDATE CURRENT_TIMESTAMP,
editorial varchar(20) not null
); 

create table autor (
aid int not null auto_increment primary key,
nombre varchar(40) not null
);

create table relLibroAutor (
libroid int not null,
autorid int not null,
foreign key(libroid) references libro(id) on delete cascade,
foreign key(autorid) references autor(aid)
);

create table reviews (
reseñaid int not null auto_increment primary key,
libroid int not null,
username varchar(20) not null,
name varchar(70) not null,
ultima_fecha_hora timestamp default current_timestamp ON UPDATE CURRENT_TIMESTAMP,
texto varchar(500) not null,
foreign key(libroid) references libro(id) on delete cascade

);






 