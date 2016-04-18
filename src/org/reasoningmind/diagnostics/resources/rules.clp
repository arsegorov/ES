;**************;
; Output rules ;
;**************;

;;; These rules fire at the end, when all the judgement is done.
;;; We need separate rules to keep the output in the desired order
;;; The saliences are as follows:
;;;   UNDEFINED 2: for debugging, change to 6
;;;   HIGH 5
;;;   SLIGHT 4
;;;   NO 3
;;;---------------------------------------------------------------

; When concern = HIGH
(defrule o--judgement-high
	""
	(declare (salience 5))
	
	(output (student-ID ?st))
	(judgement (student-ID ?st) (skill-ID ?sk) (concern HIGH) (reason $?r))
=>
	(print-to-java
	    "   " error "!! HIGH" reg " concern about " crlf
	    "      " info (upcase ?sk) crlf
	)
	(print-to-java
	    "    " $?r crlf
	    crlf
	)
)

; When concern = SLIGHT
(defrule o--judgement-slight
	""
	(declare (salience 4))
	
	(output (student-ID ?st))
	(judgement (student-ID ?st) (skill-ID ?sk) (concern SLIGHT) (reason $?r))
=>
	(print-to-java
	    "   " warn "?? SLIGHT" reg " concern about " crlf
        "      " info (upcase ?sk) crlf
    )
	(print-to-java
	    "    " $?r crlf
	    crlf
	)
)

; When concern = NO
(defrule o--judgement-no
	""
	(declare (salience 3))
	
	(output (student-ID ?st))
	(judgement (student-ID ?st) (skill-ID ?sk) (concern NO) (reason $?r))
=>
	(print-to-java
	    "   " nice "** NO" reg " concern about " crlf
        "      " info (upcase ?sk) crlf
    )
	(print-to-java
	    "    " $?r crlf
	    crlf
	)
)

; When concern = UNDEFINED
(defrule o--judgement-undefined
	""
	(declare (salience 2))
	
	(output (student-ID ?st))
	(judgement (student-ID ?st) (skill-ID ?sk) (concern UNDEFINED) (reason $?r))
=>
	(print-to-java
	    "   " info "~~ UNDEFINED" reg " concern about " crlf
        "      " info (upcase ?sk) crlf
    )
	(print-to-java
	    "    " $?r crlf
	    crlf
	)
)


;*****************;
; Judgement rules ;
;*****************;


;#####################;
; Some reused strings ;
;#####################;
(defglobal
    ; These pre-formatted lines are often used in explanations
	?*initially-undefined* = (create$ reg "  (*) Changed from " info "UNDEFINED")
	?*initially-no* = (create$ reg "  (*) Changed from " info "NO")
	?*initially-slight* = (create$ reg "  (*) Changed from " info "SLIGHT")
	?*initially-high* = (create$ reg "  (*) Changed from " info "HIGH")
)


;;;
;;; These rules apply when the skill level is increasing,
;;; and the skill is still being developed by the course.
;;;
;;; The rules fire when
;;;   trend = UP
;;; and the student is within the range of lessons,
;;; in which the skill is developed
;;;
;;; Judgement:
;;;   concern = NO
;;;-----------------------------------------------------
(defglobal ?*not-formed--trend-up--explanation* =
    (create$ reg "  The student's accuracy is going " info "up" reg ", while the skill is still being formed.")
)

; The version for the case when there is NO INITIAL JUDGEMENT
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
					?*not-formed--trend-up--explanation* crlf
				)
			)
		)
	)
	(assert (judged ?student ?skill not-formed--trend-up))
)

; The version for the case when there is already NO CONCERN
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
				?*not-formed--trend-up--explanation* crlf
				crlf
				$?r
			)
		)
	)
	(assert (judged ?student ?skill not-formed--trend-up))
)


;;;
;;; These rules apply when the skill level is high and
;;; not going down
;;;
;;; The rules fire when
;;;   level = A|B|C, trend = UP
;;; or
;;;   level = A|B, trend = EVEN
;;;
;;; Judgement:
;;;   concern = NO
;;;-----------------------------------------------------
(defglobal ?*level-medium-high--explanation* =
    (create$ reg "  The student's accuracy is at a " nice "high" reg " level and is " info "steady" reg ".")
)

; The version for the case when there is NO INITIAL JUDGEMENT
(defrule j--level-medium-high--no-j
	""
	(declare (salience 40))
	
	(diagnose ?student)
	(student-skill
		(student-ID ?student)
		(skill-ID ?skill)
		(level ?l & A|B|C)
		(trend ?t & UP|EVEN&:(neq (eq ?t EVEN) (eq ?l C)))
		(count ?c)
	)
	(skill
		(ID ?skill)
		(minimum-meaningful-count ?min & :(>= ?c ?min))
	)
	(not (judgement (student-ID ?student) (skill-ID ?skill)))
=>
	(assert
		(judgement
			(student-ID ?student)
			(skill-ID ?skill)
			(concern NO)
			(reason
				(create$
					(replace$ (replace$ ?*level-medium-high--explanation*
				                        3 4
										(if (neq ?l A B) then (create$ info "high") else (create$ nice "meduim"))
							  )
							  7 8
							  (if (eq ?t UP) then (create$ nice "going up") else (create$ bold "steady"))
					) crlf
				)
			)
		)
	)
	(assert (judged ?student ?skill level-medium-high))
)

; The version for the case when there is NO CONCERN
(defrule j--level-medium-high--concern-none
	""
	(declare (salience 40))
	
	(diagnose ?student)
	(student-skill
		(student-ID ?student)
		(skill-ID ?skill)
		(level ?l & A|B|C)
		(trend ?t & UP|EVEN&:(neq (eq ?t EVEN) (eq ?l C)))
		(count ?c)
	)
	(skill
		(ID ?skill)
		(minimum-meaningful-count ?min & :(>= ?c ?min))
	)
	(not (judged ?student ?skill level-medium-high))
	?j <- (judgement (student-ID ?student) (skill-ID ?skill) (concern NO) (reason $?r))
=>
	(modify
		?j
		(reason
			(create$
				(replace$ (replace$ ?*level-medium-high--explanation*
				                    3 4
									(if (neq ?l A B) then (create$ info "high") else (create$ nice "meduim"))
						  )
						  7 8
						  (if (eq ?t UP) then (create$ nice "going up") else (create$ bold "steady"))
				) crlf
				crlf
				$?r
			)
		)
	)
	(assert (judged ?student ?skill level-medium-high))
)


;;;
;;; These rules apply when the skill level is high, but
;;; is going down
;;;
;;; The rules fire when
;;;   level = A|B, trend = DOWN
;;;
;;; Judgement:
;;;   concern = SLIGHT
;;;
(defglobal ?*level-high--trend-down--explanation* =
    (create$ reg "  The student might be struggling with the increasing difficulty of the problems:" crlf
                 "  the student's accuracy is still at a " nice "good level" reg ", but is " warn "going down" reg ".")
)

; The version for the case when there is NO INITIAL JUDGEMENT
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
					?*level-high--trend-down--explanation* crlf
				)
			)
		)
	)
	(assert (judged ?student ?skill level-high--trend-down))
)

; The version for the case when there is already NO CONCERN
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
				?*level-high--trend-down--explanation* crlf
				crlf
				?*initially-no* crlf
                $?r
			)
		)
	)
	(assert (judged ?student ?skill level-high--trend-down))
)

; The version for the case when there is already a SLIGHT CONCERN
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
				?*level-high--trend-down--explanation* crlf
				crlf
				$?r
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
                 "  the student's accuracy is " error "low" reg " and is " error "going down" reg ".")
)

; The version for the case when there is NO INITIAL JUDGEMENT
(defrule j--level-medium-low--trend-down--no-j
	""
	(declare (salience 41)) ; this has a higher priority than the more generic level-low--trend-even-down
	
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
					?*level-medium-low--trend-down--explanation* crlf
				)
			)
		)
	)
	(assert (judged ?student ?skill level-medium-low--trend-down))
)

; The version for the case when there is already a SLIGHT or NO CONCERN
(defrule j--level-medium-low--trend-down--concern-slight-none
	""
	; this has a higher priority than the more generic level-low--trend-even-down, which is 40
	(declare (salience 41))
	
	(diagnose ?student)
	(student-skill (student-ID ?student) (skill-ID ?skill) (level C|D|F) (trend DOWN))
	(not (judged ?student ?skill decrease-concern--prerequisite-high))
	?j <- (judgement (student-ID ?student) (skill-ID ?skill) (concern ?c&SLIGHT|NO) (reason $?r))
=>
	(modify
		?j
		(concern HIGH)
		(reason
			(create$
				?*level-medium-low--trend-down--explanation* crlf
				crlf
				(if (eq ?c SLIGHT) then ?*initially-slight* else ?*initially-no*) crlf
				$?r
			)
		)
	)
	(assert (judged ?student ?skill level-medium-low--trend-down))
)

; The version for the case when there is already a HIGH CONCERN
(defrule j--level-medium-low--trend-down--concern-high
	""
	; this has a higher priority than the more generic level-low--trend-even-down, which is 40
	(declare (salience 41))
	
	(diagnose ?student)
	(student-skill (student-ID ?student) (skill-ID ?skill) (level C|D|F) (trend DOWN))
	(not (judged ?student ?skill level-medium-low--trend-down))
	?j <- (judgement (student-ID ?student) (skill-ID ?skill) (concern HIGH) (reason $?r))
=>
	(modify
		?j
		(reason
			(create$
				?*level-medium-low--trend-down--explanation* crlf
				crlf
				$?r
			)
		)
	)
	(assert (judged ?student ?skill level-medium-low--trend-down))
)

;;;
;;; 
;;;
(defglobal ?*level-low--trend-even-down--explanation* =
    (create$ reg "  The student's accuracy is " error "low" reg " and is " warn "not improving" reg ".")
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
	?j <- (judgement (student-ID ?student) (skill-ID ?skill) (concern ?c & SLIGHT|NO) (reason $?r))
=>
	(modify
		?j
		(concern HIGH)
		(reason
			(create$
				?*level-low--trend-even-down--explanation* crlf
				crlf
				(if (eq ?c SLIGHT) then ?*initially-slight* else ?*initially-no*) crlf
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
	(not (judged ?student ?skill level-medium-low--trend-down))
	?j <- (judgement (student-ID ?student) (skill-ID ?skill) (concern HIGH) (reason $?r))
=>
	(modify
		?j
		(reason
			(create$
				?*level-low--trend-even-down--explanation* crlf
				crlf
				$?r
			)
		)
	)
	(assert (judged ?student ?skill level-low--trend-even-down))
)

;;;
;;; 
;;;
(defglobal ?*level-low--trend-even--very-limited-choices--explanation* =
    (create$ reg "  The student's accuracy is " error "low" reg " and is " warn "not improving" reg ", even though" crlf
                 "  the questions have " info "very few answer choices" reg "." crlf
                 "  The student " info "might be making random guesses" reg ".")
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
					?*level-low--trend-even--very-limited-choices--explanation* crlf
				)
			)
		)
	)
	(assert (judged ?student ?skill level-low--trend-even--very-limited-choices))
)

(defrule j--level-low--trend-even--very-limited-choices--concern-none
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
				?*level-low--trend-even--very-limited-choices--explanation* crlf
				crlf
				?*initially-no*
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
	(not (judged ?student ?skill level-low--trend-even--very-limited-choices))
	?j <- (judgement (student-ID ?student) (skill-ID ?skill) (concern SLIGHT) (reason $?r))
=>	
	(modify
		?j
		(reason
			(create$
				?*level-low--trend-even--very-limited-choices--explanation* crlf
				crlf
				$?r
			)
		)
	)
	(assert (judged ?student ?skill level-low--trend-even--very-limited-choices))
)

;;;
;;; 
;;;
(defglobal ?*missing-essential--explanation* =
    (create$ reg "  The skill is " warn "essential" reg " for using the program," crlf
                 "  but student's accuracy is " error "low" reg ".")
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
	?j <- (judgement (student-ID ?student) (skill-ID ?skill) (concern ?c&SLIGHT|NO) (reason $?r))
=>
	(modify
		?j
		(concern HIGH)
		(reason
			(create$
				?*missing-essential--explanation* crlf
				crlf
				(if (eq ?c SLIGHT) then ?*initially-slight* else ?*initially-no*) crlf
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
				?*missing-essential--explanation* crlf
				crlf
                $?r
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
				reg "  There is a higher concern about a prerequisite, " crlf
					"    " info (upcase ?skill2) crlf
				reg "  that requires attention first." crlf
				crlf
				?*initially-high*
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
				reg "  There is a higher concern about a prerequisite, " crlf
					"    " info (upcase ?skill2) crlf
				reg "  that requires attention first." crlf
				crlf
				?*initially-high*
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
    (create$ reg "  There's " warn "not enough data" reg " to draw conclusions yet." crlf
                 "  We didn't have enough opportunities to test this skill for this student.")
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
			(concern UNDEFINED)
            (reason
                (create$
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
    ?j <- (judgement (student-ID ?student) (skill-ID ?skill) (concern ?con & SLIGHT|HIGH) (reason $?r))
=>
    (assert (judged ?student ?skill exclude-concern--low-count))
    (modify ?j
        (concern UNDEFINED)
        (reason
            (create$
				?*exclude-concern--low-count--explanation* crlf
				crlf
				(if (eq ?con HIGH) then ?*initially-high* else ?*initially-slight*) crlf
				$?r
            )
        )
    )
)


;;;
;;;
;;;
(defglobal ?*no-skill-description--explanation* =
    (create$ reg "  The requirements for this skill are " warn "unknown" reg ".")
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
			(concern UNDEFINED)
            (reason
                (create$
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
			(concern UNDEFINED)
            (reason
                (create$
                    ?*no-other-applicable-judgement-rule--explanation* crlf
                )
            )
        )
    )
    (assert (judged ?student ?skill no-other-applicable-judgement-rule))
)

(defrule no-skill-definition
    ""
    (declare (salience 50))

    (student-skill (skill-ID ?s))
    (not (skill (ID ?s)))
=>
    (assert (skill (ID ?s) (first-lesson 0)))
)


;;;
;;; Diagnostics is finished, and it's time to print the results
;;;
(defrule begin-output
	""
	(diagnose ?student)
	(not
		(and
			(student-skill (student-ID ?student) (skill-ID ?skill))
			(not (judgement (student-ID ?student) (skill-ID ?skill)))
		)
	)
=>
	(assert (output (student-ID ?student)))
	;(print-to-java "" info "Diagnostics complete." crlf)
)