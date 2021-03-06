;;;
;;; A fact of this type stores the information about a single skill
;;;
(deftemplate skill
	"A skill's properties"
	; This is the skill's ID
	(slot ID
		(type STRING)
		(default ?NONE)
	)
	
	; Whether the skill is checked in the course.
	; Some skills might not be checked even though they are prerequisites for other skills
	(slot checked
		(type SYMBOL)
		(allowed-symbols TRUE FALSE)
		(default TRUE)
	)
	
	; When the skill appears for the first time in the course.
	; 0 means that the skill is expected to be introduced by the beginning of the course
	(slot first-lesson
		(type INTEGER)
		(default ?NONE)
	)
	
	; The number of the lesson by which the skill is expected to be formed
	; (not necessarily in full proficiency)
	(slot formed-by
		(type INTEGER)
		(default 1000)
	)
	
	; The number of the lesson by which the skill is expected to be developed to full proficiency
	(slot proficient-by
		(type INTEGER)
		(default 1000)
	)
	
	; The other skills this skill directly depends on
	(multislot depends-on
		(type STRING)
		(default "none")
	)
	
	; Whether the answer choices are typically limited to a number of options 
	(slot limited-choices
		(type SYMBOL)
		(allowed-symbols VERY YES NO)
		(default NO)
	)
	
	; Whether the skill is essential for the student's success
	; (for example, being able to follow written instructions)
	(slot essential
		(type SYMBOL)
		(allowed-symbols TRUE FALSE)
		(default FALSE)
	)

	; The minimum number of answers for meaningful reasoning
	(slot minimum-meaningful-count
	    (type INTEGER)
	    (default 3)
	)
)

;;;
;;; A fact of this type stores the statistics about
;;; the level and trend of a given skill for a given student
;;;
(deftemplate student-skill
	"A given student's stats for a given skill"
	; ID of the student for whom the stats are stored
	(slot student-ID
		(type STRING)
		(default ?NONE)
	)
		
	; ID of the skill for which the stats are stored
	(slot skill-ID
		(type STRING)
		(default ?NONE)
	)
		
	; The estimated level of proficiency
	(slot level
		(type SYMBOL)
		(default ?NONE)
		(allowed-symbols F D C B A)
	)
		
	; The recent dynamic of the skill
	(slot trend
		(type SYMBOL)
		(default ?NONE)
		(allowed-symbols DOWN EVEN UP)
	)

	; The number of questions in the student's history
	(slot count
	    (type INTEGER)
	)
)

;;;
;;; 
;;;
(deftemplate student
	"A student's ID and place in the course"
	; 
	(slot ID
		(type STRING)
		(default ?NONE)
	)
	
	; 
	(slot lesson
		(type INTEGER)
		(default ?NONE)
	)
)

;;;
;;; 
;;;
(deftemplate judgement
	"Reason for not being concerned about a student's skill"
	; 
	(slot student-ID
		(type STRING)
		(default ?NONE)
	)
	
	; 
	(slot skill-ID
		(type STRING)
		(default ?NONE)
	)
	
	;
	(slot concern
		(type SYMBOL)
		(allowed-symbols NO SLIGHT HIGH UNDEFINED)
		(default ?NONE)
	)
	
	; 
	(multislot reason
		(type STRING)
	)
)

;;;
;;; 
;;;
(deffunction print-to-java (?indent-string $?multi)
	(bind ?router reg)
	(bind ?first crlf)
	
	(while (> (length$ $?multi) 0) do
		(if (eq ?first crlf)
		then
			(printout java-reg ?indent-string)
		)
		
		(bind ?first (nth$ 1 $?multi))
		
		(if (neq ?first reg bold nice info warn error)
		then
			(printout (sym-cat java- ?router) ?first)
		else
			(bind ?router ?first)
		)
		
		(bind $?multi (rest$ $?multi))
	)
)


;;;
;;; 
;;;
(deftemplate output
	""
	(slot student-ID
		(type STRING)
		(default ?NONE)
	)
)

;(defgeneric xor)
;
;(defmethod xor (
;			(?a SYMBOL (not (neq ?a TRUE FALSE)))
;			(?b SYMBOL (not (neq ?b TRUE FALSE)))
;		)
;	
;	(if (eq ?a ?b)
;	 then
;		(return FALSE)
;	 else
;		(return TRUE)
;	)
;)