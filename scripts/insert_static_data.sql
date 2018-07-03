INSERT INTO public.question_state(question_state_id, state)
	VALUES
		(1, 'DRAFTED'),
		(2, 'SUBMITTED'),
		(3, 'ISSUED');

INSERT INTO public.answer_state(answer_state_id, state)
	VALUES
		(1, 'DRAFTED'),
		(2, 'answer_edited'),
		(3, 'SUBMITTED');

insert into jurisdiction(jurisdiction_id, jurisdiction_name, url) values (1, 'SSCS', 'http://localhost:8080/SSCS/notifications');
insert into jurisdiction(jurisdiction_id, jurisdiction_name, url) values (2, 'SSCSDown', 'http://localhost:8080/SSCS/downEndpoint');