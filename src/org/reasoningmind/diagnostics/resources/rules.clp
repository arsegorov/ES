;;;
;;; Output rules
;;;
(defrule o--judgement-high
	""
	(declare (salience 5))
	
	(judgement (student-ID ?st) (skill-ID ?sk) (concern HIGH) (reason $?r))
=>
	(print-to-java
	    "  " error "!! HIGH" reg " concern about " info (upcase ?sk) crlf
		crlf
	)
	(print-to-java
	    "    " $?r crlf
	    crlf
	)
)

(defrule o--judgement-slight
	""
	(declare (salience 4))
	(judgement (student-ID ?st) (skill-ID ?sk) (concern SLIGHT) (reason $?r))
=>
	(print-to-java
	    "  " warn "?? SLIGHT" reg " concern about " info (upcase ?sk) crlf
		crlf
    )
	(print-to-java
	    "    " $?r crlf
	    crlf
	)
)

(defrule o--judgement-no
	""
	(declare (salience 3))
	(judgement (student-ID ?st) (skill-ID ?sk) (concern NO) (reason $?r))
=>
	(print-to-java
	    "  " info "** NO" reg " concern about " info (upcase ?sk) crlf
		crlf
    )
	(print-to-java
	    "    " $?r crlf
	    crlf
	)
)

(defrule o--judgement-undefined
	""
	(declare (salience 6))
	(judgement (student-ID ?st) (skill-ID ?sk) (concern UNDEFINED) (reason $?r))
=>
	(print-to-java
	    "  " info "~~ UNDEFINED" reg " concern about " info (upcase ?sk) crlf
		crlf
    )
	(print-to-java
	    "    " $?r crlf
	    crlf
	)
)

;;;
;;; Judgement rules
;;;

(defglobal

    ; These pre-formatted lines are often used in explanations

	?*initially-undefined* = (create$ reg "* Initially, " info "UNDEFINED" reg " concern *")
	?*initially-no* = (create$ reg "* Initially, " info "NO" reg " concern *")
	?*initially-slight* = (create$ reg "* Initially, " info "SLIGHT" reg " concern *")
	?*initially-high* = (create$ reg "* Initially, " info "HIGH" reg " concern *")

	?*changed-to-undefined* = (create$ reg "* Concern changed to " info "UNDEFINED" reg " *")
	?*changed-to-no* = (create$ reg "* Concern changed to " info "NO" reg " *")
	?*changed-to-slight* = (create$ reg "* Concern changed to " info "SLIGHT" reg " *")
	?*changed-to-high* = (create$ reg "* Concern changed to " info "HIGH" reg " *")
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
	(declare (salience 40))
	
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
	(declare (salience 40))
	
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
                 "  the student's accuracy is still at a " info "good level" reg ", but " info "going down" reg ".")
)

(defrule j--level-high--trend-down--no-j
	""
	(declare (salience 40))
	
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
	(declare (salience 40))
	
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
	(declare (salience 40))
	
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
                 "  the accuracy is " info "going down" reg " and the current level of skill is " info "low" reg ".")
)

(defrule j--level-medium-low--trend-down--no-j
	""
	(declare (salience 40))
	
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
	(declare (salience 40))
	
	(diagnose ?student)
	(student-skill (student-ID ?student) (skill-ID ?skill) (level C|D|F) (trend DOWN))
	(not (judged ?student ?skill decrease-concern--prerequisite-high))
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
	(declare (salience 40))
	
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
    (create$ reg "  The student's accuracy is " info "low" reg " and " info "isn't improving" reg ".")
)

(defrule j--level-low--trend-even-down--no-j
	""
	(declare (salience 40))
	
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
	(declare (salience 40))
	
	(diagnose ?student)
	(student-skill (student-ID ?student) (skill-ID ?skill) (level D|F) (trend EVEN|DOWN))
	(not (judged ?student ?skill decrease-concern--prerequisite-high))
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
	(declare (salience 40))
	
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
    (create$ reg "  The student's accuracy " info "isn't improving" reg " even though" crlf
                 "  the questions have " info "very few answer choices" reg "." crlf
                 "  The student might be " info "making random guesses" reg ".")
)

(defrule j--level-low--trend-even--very-limited-choices--no-j
	""
	(declare (salience 40))
	
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
	(declare (salience 40))
	
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
	(declare (salience 40))
	
	(diagnose ?student)
	(student-skill (student-ID ?student) (skill-ID ?skill) (level D|F) (trend EVEN))
	(skill (ID ?skill) (limited-choices VERY))
;	(not (judged ?student ?skill level-low--trend-even--very-limited-choices))
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
    (create$ reg "  The skill is essential for using the program," crlf
                 "  but student's accuracy is " info "low" reg ".")
)

(defrule j--missing-essential--no-j
	""
	(declare (salience 40))
	
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
	(declare (salience 40))
	
	(diagnose ?student)
	(student-skill (student-ID ?student) (skill-ID ?skill) (level D|F))
	(skill (ID ?skill) (essential TRUE))
	(not (judged ?student ?skill decrease-concern--prerequisite-high))
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
	(declare (salience 40))
	
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
(defrule decrease-concern--prerequisite-high--no-high-dependent
	""
	(declare (salience 30))
	
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
	(assert (judged ?student ?skill decrease-concern--prerequisite-high))
)

(defrule decrease-concern--prerequisite-high--no-dependent
	""
	(declare (salience 30))
	
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
	(assert (judged ?student ?skill decrease-concern--prerequisite-high))
)

;;;
;;;
;;;
(defglobal ?*exclude-concern--low-count--explanation* =
    (create$ reg "  There's " info "not enough data" reg " to draw conclusions yet," crlf
                 "  we had " info "too few opportunities" reg " to test this skill for this student.")
)

(defrule exclude-concern--low-count--no-j
    ""
    (declare (salience 20))

    (diagnose ?student)
    (skill (ID ?skill) (minimum-meaningful-count ?minc))
    (student-skill (student-ID ?student) (skill-ID ?skill) (count ?c & :(< ?c ?minc)))
    (not (judgement (student-ID ?student) (skill-ID ?skill)))
=>
    (assert
        (judgement
			(student-ID ?student)
			(skill-ID ?skill)
            (reason
                (create$
                    ?*initially-undefined* crlf
                    ?*exclude-concern--low-count--explanation* crlf
                )
            )
        )
    )
    (assert (judged ?student ?skill exclude-concern--low-count))
)

(defrule exclude-concern--low-count--concern-slight-high
    ""
    (declare (salience 20))

    (diagnose ?student)
    (skill (ID ?skill) (minimum-meaningful-count ?minc))
    (student-skill (student-ID ?student) (skill-ID ?skill) (count ?c & :(< ?c ?minc)))
    ?j <- (judgement (student-ID ?student) (skill-ID ?skill) (concern SLIGHT|HIGH) (reason $?r))
=>
    (modify ?j
        (concern UNDEFINED)
        (reason
            (create$
                ?*changed-to-undefined* crlf
				?*exclude-concern--low-count--explanation* crlf
				crlf
				$?r
            )
        )
    )
    (assert (judged ?student ?skill exclude-concern--low-count))
)


;;;
;;;
;;;
(defglobal ?*no-skill-description--explanation* =
    (create$ reg "  The requirements for this skill are unknown.")
)

(defrule j--no-skill-description
    ""
    (declare (salience 20))

    (diagnose ?student)
    (student-skill (student-ID ?student) (skill-ID ?skill))
    (not (skill (ID ?skill)))
=>
    (assert
        (judgement
            (student-ID ?student)
            (skill-ID ?skill)
            (reason
                (create$
                    ?*initially-undefined* crlf
                    ?*no-skill-description--explanation* crlf
                )
            )
        )
    )
    (assert (judged ?student ?skill no-skill-description))
)


;;;
;;;
;;;
(defglobal ?*no-other-applicable-judgement-rule--explanation* =
    (create$ reg "  No judgement rule applies to this skill.")
)

(defrule j--no-other-applicable-judgement-rule
    ""
    (declare (salience 10))

    (diagnose ?student)
    (student-skill (student-ID ?student) (skill-ID ?skill))
    (not (judgement (student-ID ?student) (skill-ID ?skill)))
=>
    (assert
        (judgement
            (student-ID ?student)
            (skill-ID ?skill)
            (reason
                (create$
                    ?*initially-undefined* crlf
                    ?*no-other-applicable-judgement-rule--explanation* crlf
                )
            )
        )
    )
    (assert (judged ?student ?skill no-other-applicable-judgement-rule))
)