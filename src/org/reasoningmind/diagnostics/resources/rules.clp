;;;
;;; Output rules
;;;
(defrule o--judgement-high
	""
	(declare (salience 5))
	
	(judgement (student-ID ?st) (skill-ID ?sk) (concern ?c&:(eq ?c HIGH)) (reason $?r))
=>
	(printout Java "  !! " ?c " concern about " (upcase ?sk) crlf
				crlf
				(print$ "         " $?r) crlf
	)
)

(defrule o--judgement-slight
	""
	(judgement (student-ID ?st) (skill-ID ?sk) (concern ?c&:(eq ?c SLIGHT)) (reason $?r))
=>
	(printout Java "  ?? " ?c " concern about " (upcase ?sk) crlf
				crlf
				(print$ "         " $?r) crlf
	)
)

;;;
;;; Judgement rules
;;;
(defrule j--not-formed--trend-up--no-j
	""
	(declare (salience 10))
	
	(diagnose ?student)
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
					"Initially NO concern:"
					"  The skill is still being formed and the student is making progress."
				)
			)
		)
	)
	(assert (judged ?student ?skill not-formed--trend-up))
)

(defrule j--not-formed--trend-up--concern-none
	""
	(declare (salience 10))
	
	(diagnose ?student)
	(student-skill (student-ID ?student) (skill-ID ?skill) (trend UP))
	(skill (ID ?skill) (formed-by ?formed))
	(student (ID ?student) (lesson ?l&:(< ?l ?formed)))
	(not (judged ?student ?skill not-formed--trend-up))
	?j <- (judgement (student-ID ?student) (skill-ID ?skill) (concern NO) (reason $?r))
=>
	(modify
		?j
		(reason
			(create$
				(first$ $?r)
				"  The skill is still being formed and the student is making progress."
				""
				(rest$ $?r)
			)
		)
	)
	(assert (judged ?student ?skill not-formed--trend-up))
)


;;;
;;; 
;;;
(defrule j--level-high--trend-down--no-j
	""
	(declare (salience 10))
	
	(diagnose ?student)
	(student-skill (student-ID ?student) (skill-ID ?skill) (level A|B) (trend DOWN))
	(not (judgement (student-ID ?student) (skill-ID ?skill)))
=>
	(assert
		(judgement
			(student-ID ?student)
			(skill-ID ?skill)
			(concern SLIGHT)
			(reason
				(create$
					"Initially SLIGHT concern:"
					"  The student appears to struggle with the increasing difficulty of the problems."
				)
			)
		)
	)
	(assert (judged ?student ?skill level-high--trend-down))
)

(defrule j--level-high--trend-down--concern-none
	""
	(declare (salience 10))
	
	(diagnose ?student)
	(student-skill (student-ID ?student) (skill-ID ?skill) (level A|B) (trend DOWN))
	?j <- (judgement (student-ID ?student) (skill-ID ?skill) (concern NO) (reason $?r))
=>
	(modify
		?j
		(concern SLIGHT)
		(reason
			(create$
				"Changed to SLIGHT concern:"
				"  The student appears to struggle with the increasing difficulty of the problems."
				""
				$?r
			)
		)
	)
	(assert (judged ?student ?skill level-high--trend-down))
)

(defrule j--level-high--trend-down--concern-slight
	""
	(declare (salience 10))
	
	(diagnose ?student)
	(student-skill (student-ID ?student) (skill-ID ?skill) (level A|B) (trend DOWN))
	(not (judged ?student ?skill level-high--trend-down))
	?j <- (judgement (student-ID ?student) (skill-ID ?skill) (concern SLIGHT) (reason $?r))
=>
	(modify
		?j
		(reason
			(create$
				(first$ $?r)
				"  The student appears to struggle with the increasing difficulty of the problems."
				""
				(rest$ $?r)
			)
		)
	)
	(assert (judged ?student ?skill level-high--trend-down))
)

;;;
;;; 
;;;
(defrule j--level-medium-low--trend-down--no-j
	""
	(declare (salience 10))
	
	(diagnose ?student)
	(student-skill (student-ID ?student) (skill-ID ?skill) (level C|D|F) (trend DOWN))
	(not (judgement (student-ID ?student) (skill-ID ?skill)))
=>
	(assert
		(judgement
			(student-ID ?student)
			(skill-ID ?skill)
			(concern HIGH)
			(reason
				(create$
					"Initially HIGH concern:"
					"  The student struggles with the increasing difficulty of the problems so the skill is no longer at a satisfactory level."
				)
			)
		)
	)
	(assert (judged ?student ?skill level-medium-low--trend-down))
)

(defrule j--level-medium-low--trend-down--concern-slight-none
	""
	(declare (salience 10))
	
	(diagnose ?student)
	(student-skill (student-ID ?student) (skill-ID ?skill) (level C|D|F) (trend DOWN))
	(not (eliminated ?student ?skill))
	?j <- (judgement (student-ID ?student) (skill-ID ?skill) (concern SLIGHT|NO) (reason $?r))
=>
	(modify
		?j
		(concern HIGH)
		(reason
			(create$
				"Changed to HIGH concern:"
				"  The student struggles with the increasing difficulty of the problems so the skill is no longer at a satisfactory level."
				""
				$?r
			)
		)
	)
	(assert (judged ?student ?skill level-medium-low--trend-down))
)

(defrule j--level-medium-low--trend-down--concern-high
	""
	(declare (salience 10))
	
	(diagnose ?student)
	(student-skill (student-ID ?student) (skill-ID ?skill) (level C|D|F) (trend DOWN))
	(not (judged ?student ?skill level-medium-low--trend-down))
	?j <- (judgement (student-ID ?student) (skill-ID ?skill) (concern HIGH) (reason $?r))
=>
	(modify
		?j
		(reason
			(create$
				(first$ $?r)
				"  The student struggles with the increasing difficulty of the problems so the skill is no longer at a satisfactory level."
				""
				(rest$ $?r)
			)
		)
	)
	(assert (judged ?student ?skill level-medium-low--trend-down))
)

;;;
;;; 
;;;
(defrule j--level-low--trend-even-down--no-j
	""
	(declare (salience 10))
	
	(diagnose ?student)
	(student-skill (student-ID ?student) (skill-ID ?skill) (level D|F) (trend EVEN|DOWN))
	(not (judgement (student-ID ?student) (skill-ID ?skill)))
=>
	(assert
		(judgement
			(student-ID ?student)
			(skill-ID ?skill)
			(concern HIGH)
			(reason
				(create$
					"Initially HIGH concern:"
					"  The student has not formed this skill and isn't making progress."
				)
			)
		)
	)
	(assert (judged ?student ?skill level-low--trend-even-down))
)

(defrule j--level-low--trend-even-down--concern-slight-none
	""
	(declare (salience 10))
	
	(diagnose ?student)
	(student-skill (student-ID ?student) (skill-ID ?skill) (level D|F) (trend EVEN|DOWN))
	(not (eliminated ?student ?skill))
	?j <- (judgement (student-ID ?student) (skill-ID ?skill) (concern SLIGHT|NO) (reason $?r))
=>
	(modify
		?j
		(concern HIGH)
		(reason
			(create$
				"Changed to HIGH concern:"
				"  The student has not formed this skill and isn't making progress."
				""
				$?r
			)
		)
	)	
	(assert (judged ?student ?skill level-low--trend-even-down))
)

(defrule j--level-low--trend-even-down--concern-high
	""
	(declare (salience 10))
	
	(diagnose ?student)
	(student-skill (student-ID ?student) (skill-ID ?skill) (level D|F) (trend EVEN|DOWN))
	(not (judged ?student ?skill level-low--trend-even-down))
	?j <- (judgement (student-ID ?student) (skill-ID ?skill) (concern HIGH) (reason $?r))
=>
	(modify
		?j
		(reason
			(create$
				(first$ $?r)
				"  The student has not formed this skill and isn't making progress."
				""
				(rest$ $?r)
			)
		)
	)
	(assert (judged ?student ?skill level-low--trend-even-down))
)

;;;
;;; 
;;;
(defrule j--level-low--trend-even--very-limited-choices--no-j
	""
	(declare (salience 10))
	
	(diagnose ?student)
	(student-skill (student-ID ?student) (skill-ID ?skill) (level D|F) (trend EVEN))
	(skill (ID ?skill) (limited-choices VERY))
	(not (judgement (student-ID ?student) (skill-ID ?skill)))
=>	
	(assert
		(judgement
			(student-ID ?student)
			(skill-ID ?skill)
			(concern SLIGHT)
			(reason
				(create$
					"Initially SLIGHT concern:"
					"  The student isn't making progress on questions with very limited answer choices."
					"  The student might be making random guesses."
				)
			)
		)
	)
	(assert (judged ?student ?skill level-low--trend-even--very-limited-choices))
)

(defrule j--level-low--trend-even--very-limited-choices--concern-no
	""
	(declare (salience 10))
	
	(diagnose ?student)
	(student-skill (student-ID ?student) (skill-ID ?skill) (level D|F) (trend EVEN))
	(skill (ID ?skill) (limited-choices VERY))
	?j <- (judgement (student-ID ?student) (skill-ID ?skill) (concern NO) (reason $?r))
=>	
	(modify
		?j
		(concern SLIGHT)
		(reason
			(create$
				"Changed to SLIGHT concern:"
				"  The student isn't making progress on questions with very limited answer choices."
				"  The student might be making random guesses."
				""
				$?r
			)
		)
	)
	(assert (judged ?student ?skill level-low--trend-even--very-limited-choices))
)

(defrule j--level-low--trend-even--very-limited-choices--concern-slight
	""
	(declare (salience 10))
	
	(diagnose ?student)
	(student-skill (student-ID ?student) (skill-ID ?skill) (level D|F) (trend EVEN))
	(skill (ID ?skill) (limited-choices VERY))
	(not (judged ?student ?skill level-low--trend-even--very-limited-choices))
	?j <- (judgement (student-ID ?student) (skill-ID ?skill) (concern SLIGHT) (reason $?r))
=>	
	(modify
		?j
		(reason
			(create$
				(first$ $?r)
				"  The student isn't making progress on questions with very limited answer choices."
				"  The student might be making random guesses."
				""
				(rest$ $?r)
			)
		)
	)
	(assert (judged ?student ?skill level-low--trend-even--very-limited-choices))
)

;;;
;;; 
;;;
(defrule j--missing-essential--no-j
	""
	(declare (salience 10))
	
	(diagnose ?student)
	(student-skill (student-ID ?student) (skill-ID ?skill) (level D|F))
	(skill (ID ?skill) (essential TRUE))
	(not (judgement (student-ID ?student) (skill-ID ?skill)))
=>
	(assert
		(judgement
			(student-ID ?student)
			(skill-ID ?skill)
			(concern HIGH)
			(reason
				(create$
					"Initially HIGH concern:"
					"  The skill is essential for using the program."
				)
			)
		)
	)
	(assert (judged ?student ?skill missing-essential))
)

(defrule j--missing-essential--concern-slight-none
	""
	(declare (salience 10))
	
	(diagnose ?student)
	(student-skill (student-ID ?student) (skill-ID ?skill) (level D|F))
	(skill (ID ?skill) (essential TRUE))
	(not (eliminated ?student ?skill))
	?j <- (judgement (student-ID ?student) (skill-ID ?skill) (concern SLIGHT|NO) (reason $?r))
=>
	(modify
		?j
		(concern HIGH)
		(reason
			(create$
				"Changed to HIGH concern:"
				"  The skill is essential for using the program."
				""
				$?r
			)
		)
	)
	(assert (judged ?student ?skill missing-essential))
)

(defrule j--missing-essential--concern-high
	""
	(declare (salience 10))
	
	(diagnose ?student)
	(student-skill (student-ID ?student) (skill-ID ?skill) (level D|F))
	(skill (ID ?skill) (essential TRUE))
	(not (judged ?student ?skill missing-essential))
	?j <- (judgement (student-ID ?student) (skill-ID ?skill) (concern HIGH) (reason $?r))
=>
	(modify
		?j
		(reason
			(create$
				(first$ $?r)
				"  The skill is essential for using the program."
				""
				(rest$ $?r)
			)
		)
	)
	(assert (judged ?student ?skill missing-essential))
)

;;;
;;; 
;;;
(defrule eliminate--prerequisite--no-high-dependent
	""
	(declare (salience 8))
	
	(diagnose ?student)
	?j <- (judgement (student-ID ?student) (skill-ID ?skill) (concern HIGH) (reason $?r))
	(judgement (student-ID ?student) (skill-ID ?skill2) (concern HIGH))
	(skill (ID ?skill) (depends-on $?skills&:(subsetp (create$ ?skill2) $?skills)))
	(skill (ID ?skill3) (depends-on $?skills3&:(subsetp (create$ ?skill) $?skills3)))
	(not (judgement (student-ID ?student) (skill-ID ?skill3) (concern HIGH)))
=>
	(modify
		?j
		(concern SLIGHT)
		(reason
			(create$
				"Changed to SLIGHT concern:"
				(str-cat "  There is a HIGH concern about a prerequisite, " (upcase ?skill2) ", that requires attention first.")
				""
				$?r
			)
		)
	)
	(assert (eliminated ?student ?skill))
)

(defrule eliminate--prerequisite--no-dependent
	""
	(declare (salience 8))
	
	(diagnose ?student)
	?j <- (judgement (student-ID ?student) (skill-ID ?skill) (concern HIGH) (reason $?r))
	(judgement (student-ID ?student) (skill-ID ?skill2) (concern HIGH))
	(skill (ID ?skill) (depends-on $?skills&:(subsetp (create$ ?skill2) $?skills)))
	(not (skill (ID ?skill3) (depends-on $?skills3&:(subsetp (create$ ?skill) $?skills3))))
=>
	(modify
		?j
		(concern SLIGHT)
		(reason
			(create$
				"Changed to SLIGHT concern:"
				(str-cat "  There is a HIGH concern about a prerequisite, " (upcase ?skill2) ", that requires attention first.")
				""
				$?r
			)
		)
	)
	(assert (eliminated ?student ?skill))
)