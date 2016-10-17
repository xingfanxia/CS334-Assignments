-- Problem 1
-- Write a query to produce a list of employees, one person per line, with one column 
-- for the land line phone number, and one column for the cell phone number. Use a NULL 
-- value if the employee is missing a phone number.
create view land_linePhones(empid, land_linePhones) as
	select P.empid, phonenumber
	from Phones P
	where phonetype = 'home';

create view cellPhones(empid, cellPhones) as
	select P.empid, phonenumber
	from Phones P
	where phonetype = 'cell';

select E.empid, L.land_linePhones, C.cellPhones
from land_linePhones L 
	right outer join Employees E on L.empid = E.empid
	left outer join cellPhones C on C.empid = E.empid;
	
-- Problem 2
-- Write a query to determine which salesperson had the highest total amount of sales for 
-- each promotion. The output of your query should have one row for each promotion, and list 
-- the name of the promo, the sales person name, and the amount. In the event of a tie, all 
-- winning salespeople should be shown. If no one has any sales during a promotion, show it 
-- on the list, but with null values for the salesperson and amount. Your results should be 
-- sorted by promo name.
create view sales_in_prmotions(promo, salesperson, sales) as
	select promo, salesperson, sum(amount)
	from Sales S 
		right outer join Promotions P 
		on saledate between startdate and enddate
	group by promo, salesperson;

create view max_sales (promo, sales) as
	select promo, max(sales) 
	from sales_in_prmotions 
	group by promo;

select P.promo, sip.salesperson, sip.sales
from max_sales as M 
	join sales_in_prmotions as sip
		using(promo, sales)
	right outer join Promotions as P
		using(promo)
order by P.promo asc;


-- Problem 3
-- Write a query that produces a list of baby names, and the maximum
-- number of overlapping sessions for the babysitter during that session.

-- create view baby_overlap_sessions as
-- 	select baby
-- 	from baby
create view multibabies(sitter, start_time, babycounts) as
	select s1.sitter, s1.start_time, count(s2.baby)
	from Sessions s1, Sessions s2
	where s1.sitter = s2.sitter and s1.start_time between s2.start_time and s2.end_time
	group by s1.sitter, s1.start_time, s1.baby;

select s.baby, max(babycounts)
from Sessions s, multibabies m
where s.sitter = m.sitter and m.start_time between s.start_time and s.end_time
group by s.baby;