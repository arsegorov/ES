;*************************************************************************;
;  ;
;*************************************************************************;
;(defrule find-first-singles
;	""
;	?counting <- (counting singles)
;	?input <- (input (student-ID ?student) (skills ?single) (outcome ?correct) (accounted-for 0))
;	(not (single-stats (student-ID ?student) (skill-ID ?single)))
;=>
;	(assert (single-stats (student-ID ?student) (skill-ID ?single) (count 1) (count-correct ?correct)))
;	(modify ?input (accounted-for 1)))

;*****************************************************************;
;  ;
;*****************************************************************;
;(defrule add-up-singles
;	""
;	?counting <- (counting singles)
;	?input <- (input (student-ID ?student) (skills ?single) (outcome ?correct) (accounted-for 0))
;	?stats <- (single-stats (student-ID ?student) (skill-ID ?single) (count ?a) (count-correct ?b))
;=>
;	(modify ?stats (count (+ ?a 1)) (count-correct (+ ?b ?correct)))
;	(modify ?input (accounted-for 1)))

;*****************************************************************;
;  ;
;*****************************************************************;
;(defrule end-adding-up-singles
;	""
;	?counting <- (counting singles)
;	(not (input (skills ?single) (accounted-for 0)))
;=>
;	(retract ?counting))

;*****************************************************************;
;  ;
;*****************************************************************;
;(defrule reset-singles
;	""
;	(not (counting singles))
;	?input <- (input (skills ?single) (accounted-for 1))
;=>
;	(modify ?input (accounted-for 0)))

;;;
;;; 
;;;
(defrule trend-is-up-before-formed
	""
	(skill-stats (student-ID ?student) (skill-ID ?skill) (trend up))
	(skill (ID ?skill) (formed-by ?formed))
	(student (ID ?student) (lesson ?lesson&:(< ?lesson ?formed)))
	(not (slightly-concerned-about (student-ID ?student) (skill-ID ?skill)))
	(not (highly-concerned-about (student-ID ?student) (skill-ID ?skill)))
=>
	(assert
		(not-concerned-about
			(student-ID ?student)
			(skill-ID ?skill)
			(reason "The skill is still being formed. The trend is up, which is good.")
		)
	)
)