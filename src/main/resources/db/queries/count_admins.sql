SELECT count(u.email) AS count
	FROM USERS u
	JOIN USER_ROLE ur ON ur.user_email = u.email
	JOIN ROLES r ON r.id = ur.role_id WHERE r.name = 'ADMIN';