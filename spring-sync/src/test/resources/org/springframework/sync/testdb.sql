create table Todo (
	id identity,
	description varchar(2000),
	complete boolean
);

insert into Todo (description, complete) values ('A', false);
insert into Todo (description, complete) values ('B', false);
insert into Todo (description, complete) values ('C', false);
