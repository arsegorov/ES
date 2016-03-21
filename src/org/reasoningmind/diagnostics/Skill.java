package org.reasoningmind.diagnostics;

/**
 * Labels for all the different skills
 */
class Skill {
	private String id;

	public static final Skill NN_MENTAL_ADD_1 = new Skill("1. Natural Numbers,(mental) +,1)with 1");

	Skill (String id) {
		this.id = id;
	}

	String getId() {
		return id;
	}
}
