create table Employees (
	empid integer primary key,
	firstname varchar(20) not null,
	lastname varchar(20) not null
);

create table Phones (
	empid integer not null references Employees,
	phonetype char(4) not null
		check (phonetype in ('home', 'cell')),
	phonenumber char(10) not null,
	primary key (empid, phonetype)
);

create table Promotions (
	promo varchar(35) not null primary key,
	startdate date not null,
	enddate date not null
);

create table Sales (
	salesperson varchar(25) not null,
	saledate date not null,
	amount numeric not null
);

INSERT INTO Promotions (promo,startdate,enddate) VALUES ('Aliquam','04/15/2016','05/14/2019'),('vulputate,','07/19/2016','04/22/2019'),('consequat','01/06/2016','12/08/2017'),('ut,','05/26/2016','05/09/2019'),('Praesent','04/30/2016','03/25/2019'),('nulla','04/05/2016','01/19/2019'),('lectus','01/14/2016','04/15/2019'),('sollicitudin','04/14/2016','05/31/2018'),('fermentum','03/15/2016','07/21/2019'),('enim.','05/06/2016','09/09/2019');
INSERT INTO Sales (salesperson,saledate,amount) VALUES ('Sasha','04/14/2016','76050'),('Lael','12/26/2015','60256'),('Ocean','05/01/2016','9896'),('Bevis','04/27/2019','49519'),('Carter','12/09/2018','58602'),('Walter','01/21/2018','58568'),('Amela','04/13/2019','88781'),('Nadine','05/08/2019','87519'),('Paki','03/25/2017','27409'),('Hadley','04/27/2019','69604');
INSERT INTO Sales (salesperson,saledate,amount) VALUES ('Sasha','04/20/2016','96050'),('Lael','12/20/2017','30256'),('Ocean','05/01/2019','2896'),('Bevis','11/27/2016','19519'),('Carter','12/09/2016','18602'),('Walter','11/21/2018','8568'),('Amela','04/13/2016','8781'),('Nadine','01/08/2019','47519'),('Paki','09/25/2016','2409'),('Hadley','04/27/2017','9604');
INSERT INTO Promotions (promo,startdate,enddate) VALUES ('NoOne','04/15/2022','05/14/2022')
INSERT INTO Sales (salesperson,saledate,amount) VALUES ('Sasha','04/21/2016','56050')

create table Sessions (
	baby varchar(25) not null primary key,
	sitter varchar(25) not null,
	start_time time not null,
	end_time time not null
);

INSERT INTO Sessions(baby, sitter, start_time, end_time) VALUES ('Alice', 'Rory', '08:00', '11:00'), ('Ben', 'Rory', '09:00', '13:00'), ('Cara', 'Amelia', '09:00', '15:30'),('Darren', 'Amelia', '08:00', '10:00'), ('Eustace', 'Amelia', '10:01', '11:30'), ('Merlin', 'Amelia', '09:30', '10:30')
INSERT INTO Sessions(baby, sitter, start_time, end_time) VALUES ('Soap', 'Rory', '9:00', '12:00'), ('dChen', 'Amelia', '11:09', '15:00'), ('dashi', 'Amelia', '10:29', '15:00')
