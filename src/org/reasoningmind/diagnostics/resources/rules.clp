;;;
;;; Output rules
;;;
(defrule o--judgement
	""
	(judgement (student-ID ?st) (skill-ID ?sk) (concern ?c&:(eq ?c SLIGHT)|:(eq ?c HIGH)) (reason $?r))
=>
	(printout t "  There is a " ?c " concern about" crlf
				"  " (upcase ?st) "'s level of" crlf
				crlf
				"    " (upcase ?sk) crlf
				crlf
				       (print$ $?r) crlf)
)

;;;
;;; Judgement rules
;;;
(defrule j--not-formed--trend-up
	""
	(student-skill (student-ID ?student) (skill-ID ?skill) (trend UP))
	(skill (ID ?skill) (formed-by ?formed))
	(student (ID ?student) (lesson ?l&:(< ?l ?formed)))
	(not (judgement (student-ID ?student) (skill-ID ?skill)))
=>
	(assert
		(judgement
			(student-ID ?student)
			(skill-ID ?skill)
			(concern NO)
			(reason
				(create$
					"Initially NO concern."
					"The skill is still being formed and the student is making progress."
				)
			)
		)
	)
)

;;;
;;; 
;;;
(defrule j--level-high--trend-down
	""
	(student-skill (student-ID ?student) (skill-ID ?skill) (level A|B) (trend DOWN))
	(not (judgement (student-ID ?student) (skill-ID ?skill) (concern SLIGHT|HIGH)))
=>
	(assert
		(judgement
			(student-ID ?student)
			(skill-ID ?skill)
			(concern SLIGHT)
			(reason
				(create$
					"Initially SLIGHT concern."
					"The student appears to struggle with the increasing difficulty of the problems."
				)
			)
		)
	)
)

;;;
;;; 
;;;
(defrule j--level-medium-low--trend-down
	""
	(student-skill (student-ID ?student) (skill-ID ?skill) (level C|D|F) (trend DOWN))
=>
	(assert
		(judgement
			(student-ID ?student)
			(skill-ID ?skill)
			(concern HIGH)
			(reason
				(create$
					"Initially HIGH concern."
					"The student struggles with the increasing difficulty of the problems so the skill is no longer at a satisfactory level."
				)
			)
		)
	)
)

;;;
;;; 
;;;
(defrule j--level-low--trend-even-down
	""
	(student-skill (student-ID ?student) (skill-ID ?skill) (level D|F) (trend EVEN|DOWN))
=>
	(assert
		(judgement
			(student-ID ?student)
			(skill-ID ?skill)
			(concern HIGH)
			(reason
				(create$
					"Initially HIGH concern."
					"The student has not formed this skill and isn't making progress."
				)
			)
		)
	)
)

;;;
;;; 
;;;
(defrule j--level-low--trend-even--very-limited-choices
	""
	(student-skill (student-ID ?student) (skill-ID ?skill) (level D|F) (trend EVEN))
	(skill (ID ?skill) (limited-choices VERY))
=>
	
	(assert
		(judgement
			(student-ID ?student)
			(skill-ID ?skill)
			(concern SLIGHT)
			(reason
				(create$
					"Initially SLIGHT concern."
					"The student isn't making progress on questions with very limited answer choices."
					"The student might be making random guesses."
				)
			)
		)
	)
)

;;;
;;; 
;;;
(defrule j--missing-essential
	""
	(student-skill (student-ID ?student) (skill-ID ?skill) (level D|F))
	(skill (ID ?skill) (essential TRUE))
=>
	(assert
		(judgement
			(student-ID ?student)
			(skill-ID ?skill)
			(concern HIGH)
			(reason
				(create$
					"Initially HIGH concern."
					"The skill is essential for using the program."
				)
			)
		)
	)
)

;;;
;;; 
;;;
(defrule eliminate--dependent
	""
	?j <- (judgement (student-ID ?student) (skill-ID ?skill) (concern HIGH) (reason $?r))
	(judgement (student-ID ?student) (skill-ID ?skill2) (concern HIGH))
	(skill (ID ?skill) (depends-on $?skills&:(subsetp (create$ ?skill2) $?skills)))
=>
	(modify
		?j
		(concern SLIGHT)
		(reason
			(create$
				"Changed to SLIGHT concern."
				(str-cat "There is a HIGH concern about a prerequisite, " (upcase ?skill2) ", that requires attention first.")
				""
				$?r
			)
		)
	)
)