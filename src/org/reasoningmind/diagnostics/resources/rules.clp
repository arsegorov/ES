;;;
;;; Output rules
;;;
(defrule o--judgement-high
	""
	(declare (salience 5))
	
	(judgement (student-ID ?st) (skill-ID ?sk) (concern HIGH) (reason $?r))
=>
	(print-to-java "  " error "!! HIGH" reg " concern about " info (upcase ?sk) crlf 
						crlf)
	(print-to-java "    " $?r crlf crlf)
)

(defrule o--judgement-slight
	""
	(judgement (student-ID ?st) (skill-ID ?sk) (concern SLIGHT) (reason $?r))
=>
	(print-to-java "  " warn "?? SLIGHT" reg " concern about " info (upcase ?sk) crlf 
						crlf)
	(print-to-java "    " $?r crlf crlf)
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
					?*initially-no* crlf
					"  The skill is still being formed and the student is making progress." crlf
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
				reg "  The skill is still being formed and the student is making progress." crlf
				crlf
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
					?*initially-slight* crlf
					"  The student appears to struggle with the increasing difficulty of the problems." crlf
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
				?*changed-to-slight* crlf
				"  The student appears to struggle with the increasing difficulty of the problems." crlf
				crlf
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
				reg "  The student appears to struggle with the increasing difficulty of the problems." crlf
				crlf
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
					?*initially-high* crlf
					"  The student struggles with the increasing difficulty of the problems," crlf
					"  so the skill is no longer at a satisfactory level." crlf
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
				?*changed-to-high* crlf
				"  The student struggles with the increasing difficulty of the problems," crlf
				"  so the skill is no longer at a satisfactory level." crlf
				crlf
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
				reg "  The student struggles with the increasing difficulty of the problems," crlf
				"  so the skill is no longer at a satisfactory level." crlf
				crlf
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
					?*initially-high* crlf
					"  The student has not formed this skill and isn't making progress." crlf
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
				?*changed-to-high* crlf
				"  The student has not formed this skill and isn't making progress." crlf
				crlf
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
				reg "  The student has not formed this skill and isn't making progress." crlf
				crlf
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
					?*initially-slight* crlf
					"  The student isn't making progress on questions with very limited answer choices." crlf
					"  The student might be making random guesses." crlf
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
				?*changed-to-slight* crlf
				"  The student isn't making progress on questions with very limited answer choices." crlf
				"  The student might be making random guesses." crlf
				crlf
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
				reg "  The student isn't making progress on questions with very limited answer choices." crlf
				"  The student might be making random guesses." crlf
				crlf
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
					?*initially-high* crlf
					"  The skill is essential for using the program." crlf
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
				?*changed-to-high* crlf
				"  The skill is essential for using the program." crlf
				crlf
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
				reg "  The skill is essential for using the program."
				crlf
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
	(skill (ID ?skill) (depends-on $?skills & :(subsetp (create$ ?skill2) $?skills)))
	(skill (ID ?skill3) (depends-on $?skills3 & :(subsetp (create$ ?skill) $?skills3)))
	(not (judgement (student-ID ?student) (skill-ID ?skill3) (concern HIGH)))
=>
	(modify
		?j
		(concern SLIGHT)
		(reason
			(create$
				?*changed-to-slight* crlf
				reg "  There is a higher concern about a prerequisite, " crlf
					"    " info (upcase ?skill2) crlf
				reg "  that requires attention first." crlf
				crlf
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
	(skill (ID ?skill) (depends-on $?skills & :(subsetp (create$ ?skill2) $?skills)))
	(not (skill (ID ?skill3) (depends-on $?skills3 & :(subsetp (create$ ?skill) $?skills3))))
=>
	(modify
		?j
		(concern SLIGHT)
		(reason
			(create$
				?*changed-to-slight* crlf
				reg "  There is a higher concern about a prerequisite, " crlf
					"    " info (upcase ?skill2) crlf
				reg "  that requires attention first." crlf
				crlf
				$?r
			)
		)
	)
	(assert (eliminated ?student ?skill))
)