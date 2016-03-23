package org.reasoningmind.diagnostics;

/**
 * Labels for all the different skills
 */
class Skill
{
	static final String DEFAULT_ID = "other,(no subcategories),(no cases)";
//	static final Skill NN_MENTAL_ADD_1 = new Skill("1. Natural Numbers,(mental) +,1)with 1");

	private String id = DEFAULT_ID;


	Skill(String id) {
		if (id != null) {
			this.id = id;
		}
	}

	String getId() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Skill skill = (Skill) o;

		return id.equals(skill.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
}
