-- 1. Print the names of professors who work in departments that have fewer than 50 PhD students.
select pname
from prof as P, dept as D
where p.dname = D.dname 
	and numphds<50;

-- Print the names of the students with the lowest GPA.
select sname
from student as s
where gpa = (select min(gpa) from student);