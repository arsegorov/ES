;*************************************************************************;
;  ;
;*************************************************************************;
(defrule find-first-singles
	""
	?counting <- (counting singles)
	?input <- (input (student-ID ?student) (skills ?single) (outcome ?correct) (accounted-for 0))
	(not (single-stats (student-ID ?student) (skill-ID ?single)))
=>
	(assert (single-stats (student-ID ?student) (skill-ID ?single) (count 1) (count-correct ?correct)))
	(modify ?input (accounted-for 1)))

;*****************************************************************;
;  ;
;*****************************************************************;
(defrule add-up-singles
	""
	?counting <- (counting singles)
	?input <- (input (student-ID ?student) (skills ?single) (outcome ?correct) (accounted-for 0))
	?stats <- (single-stats (student-ID ?student) (skill-ID ?single) (count ?a) (count-correct ?b))
=>
	(modify ?stats (count (+ ?a 1)) (count-correct (+ ?b ?correct)))
	(modify ?input (accounted-for 1)))

;*****************************************************************;
;  ;
;*****************************************************************;
(defrule end-adding-up-singles
	""
	?counting <- (counting singles)
	(not (input (skills ?single) (accounted-for 0)))
=>
	(retract ?counting))

;*****************************************************************;
;  ;
;*****************************************************************;
(defrule reset-singles
	""
	(not (counting singles))
	?input <- (input (skills ?single) (accounted-for 1))
=>
	(modify ?input (accounted-for 0)))