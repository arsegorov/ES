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
	(print-to-java "    " $?r crlf
	               crlf)
)

(defrule o--judgement-slight
	""
	(judgement (student-ID ?st) (skill-ID ?sk) (concern SLIGHT) (reason $?r))
=>
	(print-to-java "  " warn "?? SLIGHT" reg " concern about " info (upcase ?sk) crlf 
						crlf)
	(print-to-java "    " $?r crlf
	               crlf)
)

;;;
;;; Judgement rules
;;;

; These pre-formatted lines are often used in explanations
(defglobal
	?*initially-no* = (create$ reg "Initially, " info "NO" reg " concern:")
	?*initially-slight* = (create$ reg "Initially, " info "SLIGHT" reg " concern:")
	?*initially-high* = (create$ reg "Initially, " info "HIGH" reg " concern:")

	?*changed-to-no* = (create$ reg "Changed to " info "NO" reg " concern:")
	?*changed-to-slight* = (create$ reg "Changed to " info "SLIGHT" reg " concern:")
	?*changed-to-high* = (create$ reg "Changed to " info "HIGH" reg " concern:")
)

;;;
;;; These rules apply when the skill level is increasing,
;;; and the skill is still being developed by the course
;;;
(defglobal ?*not-formed--trend-up--explanation* =
    (create$ reg "  The student's accuracy is going " info "UP" reg ", while the skill is still being formed.")
)

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
					?*not-formed--trend-up--explanation* crlf
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
				(subseq$ $?r 1 (member$ crlf $?r))
				?*not-formed--trend-up--explanation* crlf
				crlf
				(subseq$ $?r (+ (member$ crlf $?r) 1) (length$ $?r))
			)
		)
	)
	(assert (judged ?student ?skill not-formed--trend-up))
)


;;;
;;; 
;;;
(defglobal ?*level-high--trend-down--explanation* =
    (create$ reg "  The student might be struggling with the increasing difficulty of the problems:" crlf
                 "  the student's accuracy is still at a good level, but going down.")
)

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
					?*level-high--trend-down--explanation* crlf
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
				?*level-high--trend-down--explanation* crlf
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
				(subseq$ $?r 1 (member$ crlf $?r))
				?*level-high--trend-down--explanation* crlf
				crlf
				(subseq$ $?r (+ (member$ crlf $?r) 1) (length$ $?r))
			)
		)
	)
	(assert (judged ?student ?skill level-high--trend-down))
)

;;;
;;; 
;;;
(defglobal ?*level-medium-low--trend-down--explanation* =
    (create$ reg "  The student might be struggling with the increasing difficulty of the problems:" crlf
                 "  the accuracy is going down and the current level of skill is low.")
)

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
					?*level-medium-low--trend-down--explanation* crlf
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
				?*level-medium-low--trend-down--explanation* crlf
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
				(subseq$ $?r 1 (member$ crlf $?r))
				?*level-medium-low--trend-down--explanation* crlf
				crlf
				(subseq$ $?r (+ (member$ crlf $?r) 1) (length$ $?r))
			)
		)
	)
	(assert (judged ?student ?skill level-medium-low--trend-down))
)

;;;
;;; 
;;;
(defglobal ?*level-low--trend-even-down--explanation* =
    (create$ reg "  The student's accuracy is low and isn't improving.")
)

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
					?*level-low--trend-even-down--explanation* crlf
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
				?*level-low--trend-even-down--explanation* crlf
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
				(subseq$ $?r 1 (member$ crlf $?r))
				?*level-low--trend-even-down--explanation* crlf
				crlf
				(subseq$ $?r (+ (member$ crlf $?r) 1) (length$ $?r))
			)
		)
	)
	(assert (judged ?student ?skill level-low--trend-even-down))
)

;;;
;;; 
;;;
(defglobal ?*level-low--trend-even--very-limited-choices--explanation* =
    (create$ reg "  The student isn't making progress on questions with very limited answer choices." crlf
                 "  The student might be making random guesses.")
)

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
					?*level-low--trend-even--very-limited-choices--explanation* crlf
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
				?*level-low--trend-even--very-limited-choices--explanation* crlf
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
				(subseq$ $?r 1 (member$ crlf $?r))
				?*level-low--trend-even--very-limited-choices--explanation* crlf
				crlf
				(subseq$ $?r (+ (member$ crlf $?r) 1) (length$ $?r))
			)
		)
	)
	(assert (judged ?student ?skill level-low--trend-even--very-limited-choices))
)

;;;
;;; 
;;;
(defglobal ?*missing-essential--explanation* =
    (create$ reg "  The skill is essential for using the program.")
)

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
					?*missing-essential--explanation* crlf
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
				?*missing-essential--explanation* crlf
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
				(subseq$ $?r 1 (member$ crlf $?r))
				?*missing-essential--explanation* crlf
				crlf
				(subseq$ $?r (+ (member$ crlf $?r) 1) (length$ $?r))
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