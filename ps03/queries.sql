-- 1. Print the names of professors who work in departments that have fewer than 50 PhD students.
select pname
from prof as P, dept as D
where P.dname = D.dname 
	and numphds<50;

-- 2. Print the names of the students with the lowest GPA.
select sname
from student
where gpa = (select min(gpa) from student);

-- 3. For each Computer Sciences class, print the class number, section number, 
-- and the average gpa of the students enrolled in the class section.
select E.cno, E.sectno, avg(gpa)
from enroll E, student S
where E.dname = 'Computer Sciences' and E.sid = S.sid
group by E.cno, E.sectno;

-- 4. Print the names and section numbers of all sections with more than six students enrolled in them.
select C.cname, E.sectno
from enroll E, course C
where E.cno = C.cno and E.dname = C.dname
group by C.cname, E.sectno
having count(*)>6;

-- 5. Print the name(s) and sid(s) of the student(s) enrolled in the most sections.
with count_sid(sid, value) as
	(select sid, count(*)
	from enroll E
	group by E.sid)
select distinct S.sname, S.sid
from enroll E, Student S, count_sid
where count_sid.sid = S.sid 
	and S.sid = E.sid 
	and count_sid.value = 
	(select max(value) 
		from count_sid);

-- 6. Print the names of departments that have one or more majors who are under 18 years old.
select M.dname
from student S, major M
where S.age < 18 and S.sid = M.sid
group by M.dname
having count(*)>=1;
	
-- 7. Print the names and majors of students who are taking one of the College Geometry courses.
select S.sname, M.dname
from student S, major M
where S.sid = M.sid 
	and S.sid in 
	(select E.sid
	from course C, enroll E
	where E.cno = C.cno 
		and C.cname like 'College Geometry%'
	group by E.sid
	having count(*)=1);

-- 8.For those departments that have no major taking a College Geometry course print the department 
-- name and the number of PhD students in the department.
select D.dname, D.numphds
from dept D
where D.dname not in 
	(select distinct M.dname
	from course C, enroll E, major M
	where E.cno = C.cno 
		and C.dname = E.dname
		and M.sid = E.sid
		and C.cname like 'College Geometry%');

-- 9. Print the names of students who are taking both a Computer Sciences 
-- course and a Mathematics course.
select S.sname
from student S, enroll E1, enroll E2
where S.sid = E1.sid and S.sid = E2.sid 
	and E1.dname = 'Computer Sciences' and E2.dname = 'Mathematics';

-- 10. Print the age difference between the oldest and the youngest Computer Sciences major.
select max(age) - min(age) as age_diff
from major M, student S
where S.sid = M.sid;

-- 11. For each department that has one or more majors with a GPA under 1.0, 
-- print the name of the department and the average GPA of its majors.
select M.dname, avg(gpa)
from (select distinct M.dname
	from major M, student S
	where M.sid = S.sid
	and S.gpa < 1.0) as low_gpa, major M, student S
where S.sid = M.sid and M.dname = low_gpa.dname
group by M.dname;

-- 12.Print the ids, names and GPAs of the students who are currently taking all the 
-- Civil Engineering courses.
select S.sid, S.sname, S.gpa
from enroll E, student S
where S.sid = E.sid 
group by S.sid, S.sname, S.gpa
having count(E.dname = 'Civil Engineering') =
	(select count(*)
	from course C
	where C.dname = 'Civil Engineering');






