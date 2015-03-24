source booksdb-schema.sql;

insert into users values('admin', MD5('admin'), 'Nacho', 'nachoadmin@acme.com');
insert into user_roles values ('admin', 'administrator');

insert into users values('test', MD5('test'), 'Test', 'test@acme.com');
insert into user_roles values ('test', 'registered');





insert into autor(nombre) values ('Lope de Vega');
insert into autor(nombre) values ('Lopez de Trigo');
insert into autor(nombre) values ('Lopez de Vaga');


select sleep(1);insert into libro(titulo, lengua, edicion, editorial) values ('Pata Negra', 'Castellano', '4', 'Santillana'); 
select sleep(1);insert into libro(titulo, lengua, edicion, editorial) values ('Pata Plata', 'Castellano', '5', 'Santillana');
select sleep(1);insert into libro(titulo, lengua, edicion, editorial) values ('Pata Verde', 'Ingles', '2', 'Santillana');
select sleep(1);insert into libro(titulo, lengua, edicion, editorial) values ('Pata Blanca', 'Catala', '3', 'Santillana');
select sleep(1);insert into libro(titulo, lengua, edicion, editorial) values ('Pata Gris', 'Frances', '1',  'Santillana');
select sleep(1);insert into libro(titulo, lengua, edicion, editorial) values ('Pata Roja', 'Italiano', '4', 'Santillana');
select sleep(1);insert into libro(titulo, lengua, edicion, editorial) values ('Pata Azul', 'Guarani', '4', 'Santillana');
select sleep(1);insert into libro(titulo, lengua, edicion, editorial) values ('Pata Amarilla', 'Arameo', '3', 'Santillana');
select sleep(1);insert into libro(titulo, lengua, edicion, editorial) values ('Pata Morada', 'Hebreo', '2', 'Santillana');
select sleep(1);insert into libro(titulo, lengua, edicion, editorial) values ('Pata Naranja', 'Latin', '2',  'Santillana');



insert into relLibroAutor(libroid, autorid) values (1,1);
insert into relLibroAutor(libroid, autorid) values (1,2);
insert into relLibroAutor(libroid, autorid) values (1,3);
insert into relLibroAutor(libroid, autorid) values (2,1);
insert into relLibroAutor(libroid, autorid) values (3,1);
insert into relLibroAutor(libroid, autorid) values (4,1);
insert into relLibroAutor(libroid, autorid) values (5,2);
insert into relLibroAutor(libroid, autorid) values (6,3);
insert into relLibroAutor(libroid, autorid) values (7,3);
insert into relLibroAutor(libroid, autorid) values (7,2);
insert into relLibroAutor(libroid, autorid) values (8,2);
insert into relLibroAutor(libroid, autorid) values (9,3);
insert into relLibroAutor(libroid, autorid) values (10,3);

select sleep(1);insert into reviews (libroid, username, name, texto) values (1,'admin','Nacho','No me ha gustado...');
select sleep(1);insert into reviews (libroid, username, name, texto) values (1,'test','Test','No me ha gustado...');
select sleep(1);insert into reviews (libroid, username, name, texto) values (3,'admin','Nacho','Muy guapo');
select sleep(1);insert into reviews (libroid, username, name, texto) values (4,'admin','Nacho', 'Muy largo');
select sleep(1);insert into reviews (libroid, username, name, texto) values (5,'test','Test','No me ha gustado...');
