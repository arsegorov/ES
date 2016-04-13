(deffacts arithmetic-skills
;
; knowledge of properties of natural numbers
;-------------------------------------------
	(skill (ID "nat num: expanded form") (checked FALSE)
	    (first-lesson 0) (formed-by 1) (proficient-by 1)
	)
	(skill (ID "nat num: split <= 10") (checked FALSE)
	    (first-lesson 0) (formed-by 1) (proficient-by 1)
	)

;
; mental addition of natural numbers
;-----------------------------------
	(skill (ID "add: 1-digit and 1-digit, no carrying")
	    (first-lesson 0) (formed-by 1) (proficient-by 1)
	)
	(skill (ID "add: 2R and 1-digit")
	    (first-lesson 0) (formed-by 1) (proficient-by 1)
	)
	(skill (ID "add: 1-digit and 1-digit, with carrying")
	    (first-lesson 0) (formed-by 1) (proficient-by 1)
		(depends-on
		    "add: 2R and 1-digit"
		    "nat num: split <= 10"
		)
	)
	(skill (ID "add: 2-digit round and 1-digit")
	    (first-lesson 0) (formed-by 1) (proficient-by 1)
	)
	(skill (ID "add: 2-digit round and 2-digit round")
	    (first-lesson 0) (formed-by 1) (proficient-by 1)
		(depends-on
			"add: 1-digit and 1-digit, no carrying"
		)
	)
	(skill (ID "add: 2-digit round and 2-digit")
	    (first-lesson 0) (formed-by 1) (proficient-by 1)
		(depends-on
			"nat num: expanded form" 
			"add: 2-digit round and 2-digit round" 
			"add: 2-digit round and 1-digit"
		)
	)
	(skill (ID "add: 2-digit and 1-digit, no carrying")
	    (first-lesson 0) (formed-by 1) (proficient-by 1)
		(depends-on
		    "nat num: expanded form"
		    "add: 1-digit and 1-digit, no carrying"
		    "add: 2-digit round and 1-digit"
		)
	)
	(skill (ID "add: 2-digit and 1-digit, with carrying")
	    (first-lesson 0) (formed-by 1) (proficient-by 1)
		(depends-on
		    "nat num: expanded form"
		    "add: 1-digit and 1-digit, with carrying"
		    "add: 2-digit round and 2-digit"
		)
	)
	(skill (ID "+: 2- and 2-, NC")
	    (first-lesson 0) (formed-by 1) (proficient-by 1)
		(depends-on
		    "nat num: expanded form"
		    "add: 1-digit and 1-digit, no carrying"
		    "add: 2-digit round and 2-digit round"
		)
	)
	(skill (ID "add: 2-digit and 2-digit, with carrying")
	    (first-lesson 0) (formed-by 1) (proficient-by 1)
		(depends-on
		    "nat num: expanded form"
		    "add: 1-digit and 1-digit, with carrying"
		    "add: 2-digit round and 2-digit round"
		    "add: 2-digit round and 2-digit"
		)
	)

;
; evaluation
;-----------
	(skill (ID "add: more than 2 1-digit")
	    (first-lesson 0) (formed-by 1) (proficient-by 1)
		(depends-on
		    "add: 1-digit and 1-digit, with carrying"
		)
	)
	(skill (ID "add: more than 2 2-digit")
	    (first-lesson 0) (formed-by 1) (proficient-by 1)
		(depends-on
		    "add: 2-digit and 2-digit, with carrying"
		)
	)

;
; working with expressions
;-------------------------
	(skill (ID "expr: substitute given number for unknown")
	    (first-lesson 0) (formed-by 1) (proficient-by 3)
	)

;
; column addition of natural numbers
;-----------------------------------
	(skill (ID "col add: 3-digit and 3-digit, no carrying")
	    (first-lesson 15) (formed-by 18) (proficient-by 20)
		(depends-on
		    "add: 1-digit and 1-digit, no carrying"
		)
	)
	(skill (ID "col add: 3-digit and 1-digit, with 1 carrying")
	    (first-lesson 16) (formed-by 18) (proficient-by 20)
		(depends-on
		    "add: 1-digit and 1-digit, no carrying"
		    "add: 1-digit and 1-digit, with carrying"
		    "add: more than 2 small summands"
		)
	)

;
; solving word problems
;----------------------
	(skill (ID "wp: what is found")
	    (first-lesson 1) (limited-choices VERY)
	)
	(skill (ID "wp: solve \"less than given\"")
	    (first-lesson 1) (formed-by 1) (proficient-by 5)
	)

;
; working with equations
;-----------------------
	(skill (ID "eqns: understand true/false equations")
		(first-lesson 1) (formed-by 10) (proficient-by 15) (limited-choices VERY)
	)

;
; translating natural language to math
;-------------------------------------
	(skill (ID "lang->math: greater than ... by ...")
		(first-lesson 2) (formed-by 1) (proficient-by 34) (limited-choices YES)
	)
	(skill (ID "lang->math: less than ... by ...")
		(first-lesson 2) (formed-by 1) (proficient-by 34) (limited-choices YES)
	)
	(skill (ID "lang->math: by how much is ... greater than ...")
		(first-lesson 2) (formed-by 1) (proficient-by 1) (limited-choices YES)
	)
	(skill (ID "lang->math: by how much is ... less than ...")
		(first-lesson 2) (formed-by 1) (proficient-by 1) (limited-choices YES)
	)

;
; non-math skills
;----------------
	(skill (ID "non-math: following direct instructions") (essential TRUE)
		(first-lesson 0) (formed-by 1) (proficient-by 1)
	)
)