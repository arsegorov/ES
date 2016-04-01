(deffacts arithmetic-skills
; properties of natural numbers
	(skill (ID "natural numbers: expanded form") (checked FALSE) (first-lesson 0) (formed-by 1) (proficient-by 1))
	(skill (ID "natural numbers: split <= 10") (checked FALSE) (first-lesson 0) (formed-by 1) (proficient-by 1))
	
; mental addition of natural numbers
	(skill (ID "addition: 1-digit and 1-digit, no carrying") (first-lesson 0) (formed-by 1) (proficient-by 1))
	(skill (ID "addition: 10 and 1-digit") (first-lesson 0) (formed-by 1) (proficient-by 1))
	(skill (ID "addition: 1-digit and 1-digit, with carrying") (first-lesson 0) (formed-by 1) (proficient-by 1)
		(depends-on "addition: 10 and 1-digit" "natural numbers: split <= 10")
	)
	(skill (ID "addition: 2-digit round and 1-digit") (first-lesson 0) (formed-by 1) (proficient-by 1))
	(skill (ID "addition: 2-digit round and 2-digit round") (first-lesson 0) (formed-by 1) (proficient-by 1)
		(depends-on "addition: 1-digit and 1-digit, no carrying")
	)
	(skill (ID "addition: 2-digit round and 2-digit") (first-lesson 0) (formed-by 1) (proficient-by 1)
		(depends-on "natural numbers: expanded form" "addition: 2-digit round and 2-digit round" "addition: 2-digit round and 1-digit")
	)
	(skill (ID "addition: 2-digit and 1-digit, no carrying") (first-lesson 0) (formed-by 1) (proficient-by 1)
		(depends-on "natural numbers: expanded form" "addition: 1-digit and 1-digit, no carrying" "addition: 2-digit round and 1-digit")
	)
	(skill (ID "addition: 2-digit and 1-digit, with carrying") (first-lesson 0) (formed-by 1) (proficient-by 1)
		(depends-on "natural numbers: expanded form" "addition: 1-digit and 1-digit, with carrying" "addition: 2-digit round and 2-digit")
	)
	(skill (ID "addition: 2-digit and 2-digit, no carrying") (first-lesson 0) (formed-by 1) (proficient-by 1)
		(depends-on "natural numbers: expanded form" "addition: 1-digit and 1-digit, no carrying" "addition: 2-digit round and 2-digit round")
	)
	(skill (ID "addition: 2-digit and 2-digit, with carrying") (first-lesson 0) (formed-by 1) (proficient-by 1)
		(depends-on "natural numbers: expanded form" "addition: 1-digit and 1-digit, with carrying" "addition: 2-digit round and 2-digit round" "addition: 2-digit round and 2-digit")
	)
	
; evaluation of expressions
	(skill (ID "addition: more than 2 1-digit") (first-lesson 0) (formed-by 1) (proficient-by 1)
		(depends-on "addition: 1-digit and 1-digit, with carrying")
	)
	(skill (ID "addition: more than 2 2-digit") (first-lesson 0) (formed-by 1) (proficient-by 1)
		(depends-on "addition: 2-digit and 2-digit, with carrying")
	)
	
; column addition of natural numbers
	(skill (ID "caddition: 3-digit and 3-digit, no carrying") (first-lesson 15) (formed-by 18) (proficient-by 20)
		(depends-on "addition: 1-digit and 1-digit, no carrying")
	)
	(skill (ID "caddition: 3-digit and 1-digit, with 1 carrying") (first-lesson 16) (formed-by 18) (proficient-by 20)
		(depends-on "addition: 1-digit and 1-digit, no carrying" "addition: 1-digit and 1-digit, with carrying"  "addition: more than 2 small summands")
	)

; solving word problems
	(skill (ID "wp: what is found") (first-lesson 1) (limited-choices VERY))

; working with equations
	(skill (ID "eqns: understand true/false equations")
		(first-lesson 1) (formed-by 10) (proficient-by 15) (limited-choices VERY)
	)

; translating natural language to math
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

; non-math skills
	(skill (ID "non-math: following direct instructions")
		(first-lesson 0) (formed-by 1) (proficient-by 1) (essential TRUE)
	)
)