;*************************************************************************;
; A fact of this type stores the outcome of a single input from a student ;
;*************************************************************************;
(deftemplate input
	"an input from a student"
	; This stores the student's ID
	(slot student-ID
		(type SYMBOL)
		(default ?DERIVE))
	; This stores the timestamp of the student's answer
	(slot timestamp
		(type INTEGER)
		(default ?DERIVE))
	; This stores the list of all the skill IDs associated with the question that was asked of the student
	(multislot skills
		(type SYMBOL)
		(default ?DERIVE))
	; This stores the whether the student's answer was correct or incorrect
	(slot outcome
		(type INTEGER)
		(allowed-integers 0 1))
	; This is an auxiliary slot used to check if the slot was accounted for in the current counting
	(slot accounted-for
		(type INTEGER)
		(allowed-integers 0 1)
		(default ?DERIVE)))

;*****************************************************************;
; A fact of this type stores the information about a single skill ;
;*****************************************************************;
(deftemplate skill
	"a skill's properties"
	; This is the skill's ID
	(slot name
		(type SYMBOL)
		(default ?DERIVE))
	; This is the categorization of the skill in the skills' hierarchy
	(multislot categories
		(type SYMBOL)
		(default ?DERIVE)))
		
;*****************************************************************;
;  ;
;*****************************************************************;
(deftemplate single-stats
	""
	; 
	(slot student-ID
		(type SYMBOL)
		(default ?DERIVE))
	; 
	(slot skill-ID
		(type SYMBOL)
		(default ?DERIVE))
	; 
	(slot count
		(type INTEGER)
		(default ?DERIVE))
	;
	(slot count-correct
		(type INTEGER)
		(default ?DERIVE)
		(allowed-integers 0 1)))