create table enroll
	(sid	numeric(10),
	grade	numeric(2,1),
	dname	varchar(30),
	cno		numeric(3),
	sectno 	numeric(1),
	primary key (sid, dname, cno, sectno));

create table student
	(sid	numeric(10),
	sname	varchar(30),
	sex	varchar(1),
	age		numeric(2),
	year 	numeric(2),
	gpa		numeric(5,4),
	primary key (sid));

create table dept
	(dname	varchar(30),
	numphds	numeric(4),
	primary key (dname));

create table prof
	(pname	varchar(30),
	dname	varchar(30),
	primary key (pname));

create table course
	(cno	numeric(3),
	cname	varchar(30),
	dname	varchar(30),
	primary key (cno, dname));

create table major
	(dname	varchar(30),
	sid		numeric(10),	
	primary key (dname, sid));

create table section
	(dname	varchar(30),
	cno		numeric(3),
	sectno 	numeric(1),
	pname	varchar(30),
	primary key (dname, cno, sectno));


-- \copy enroll from enroll.data
-- \copy course from course.data
-- \copy dept from dept.data
-- \copy major from major.data
-- \copy prof from prof.data
-- \copy section from section.data
-- \copy student from student.data