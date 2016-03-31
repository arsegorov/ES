(deffacts students
	(student (ID "Alice") (lesson 18))
	(student (ID "Bob") (lesson 12))
	(student (ID "Charlie") (lesson 4)))

(deffacts skills-stats
	(skill-stats (student-ID "Alice") (skill-ID "+: 1d and 1d NC") (level B) (trend even))
	(skill-stats (student-ID "Alice") (skill-ID "+: 10 and 1d") (level A) (trend even))
	(skill-stats (student-ID "Alice") (skill-ID "+: 1d and 1d C") (level D) (trend up))
	(skill-stats (student-ID "Alice") (skill-ID "c+: 3d and 3d NC") (level D) (trend up))
	(skill-stats (student-ID "Bob") (skill-ID "+: 1d and 1d NC") (level A) (trend down))
	(skill-stats (student-ID "Bob") (skill-ID "+: 10 and 1d") (level A) (trend even))
	(skill-stats (student-ID "Charlie") (skill-ID "+: 1d and 1d NC") (level C) (trend even))
	(skill-stats (student-ID "Charlie") (skill-ID "+: 1d and 1d C") (level D) (trend even)))