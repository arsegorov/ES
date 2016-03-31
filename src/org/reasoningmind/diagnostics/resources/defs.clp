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
	
	; This is the categorization of the skill in the skills' hierarchy
	(multislot depends-on
		(type STRING)
		(default "none")
	)
)

;;;
;;; A fact of this type stores the statistics about
;;; the level and trend of a given skill for a given student
;;;
(deftemplate skill-stats
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
		(allowed-symbols down even up)
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
(deftemplate not-concerned-about
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
	(slot reason
		(type STRING)
	)
)

;;;
;;; 
;;;
(deftemplate slightly-concerned-about
	"Reason for being slightly concerned about a student's skill"
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
	(slot reason
		(type STRING)
	)
)

;;;
;;; 
;;;
(deftemplate highly-concerned-about
	"Reason for being highly concerned about a student's skill"
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
	(slot reason
		(type STRING)
	)
)