(deffacts students
	(student (ID "Alice") (lesson 16))
	(student (ID "Bob") (lesson 12))
	(student (ID "Charlie") (lesson 4))
)

(deffacts skills-stats
	;
	; Alice
	;
	(student-skill
		(student-ID "Alice")
		(skill-ID "addition: 1-digit and 1-digit, no carrying")
		(level B) (trend EVEN)
	)
	(student-skill
		(student-ID "Alice")
		(skill-ID "addition: 10 and 1-digit")
		(level A) (trend EVEN)
	)
	(student-skill
		(student-ID "Alice")
		(skill-ID "addition: 1-digit and 1-digit, with carrying")
		(level C) (trend UP)
	)
	(student-skill
		(student-ID "Alice")
		(skill-ID "caddition: 3-digit and 3-digit, no carrying")
		(level D) (trend UP)
	)
	(student-skill
		(student-ID "Alice")
		(skill-ID "caddition: 3-digit and 1-digit, with 1 carrying")
		(level D) (trend UP)
	)
	(student-skill
		(student-ID "Alice")
		(skill-ID "non-math: following direct instructions")
		(level B) (trend EVEN)
	)

	;
	; Bob
	;
	(student-skill
		(student-ID "Bob")
		(skill-ID "addition: 1-digit and 1-digit, no carrying")
		(level A) (trend DOWN)
	)
	(student-skill
		(student-ID "Bob")
		(skill-ID "addition: 10 and 1-digit")
		(level A) (trend EVEN)
	)
	(student-skill
		(student-ID "Bob")
		(skill-ID "non-math: following direct instructions")
		(level B) (trend EVEN)
	)

	;
	; Charlie
	;
	(student-skill
		(student-ID "Charlie")
		(skill-ID "addition: 1-digit and 1-digit, no carrying")
		(level C) (trend UP)
	)
	(student-skill
		(student-ID "Charlie")
		(skill-ID "addition: 10 and 1-digit")
		(level D) (trend EVEN)
	)
	(student-skill
		(student-ID "Charlie")
		(skill-ID "addition: 1-digit and 1-digit, with carrying")
		(level D) (trend EVEN)
	)
	(student-skill
		(student-ID "Charlie")
		(skill-ID "wp: what is found")
		(level D) (trend EVEN)
	)
	(student-skill
		(student-ID "Charlie")
		(skill-ID "non-math: following direct instructions")
		(level B) (trend EVEN)
	)
)