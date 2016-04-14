package org.reasoningmind.diagnostics;

import org.ejml.simple.SimpleMatrix;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Keeps the history of a given student's responses.
 * <p>
 * The history is represented as a set of individual-skill histories. More specifically, the overall history stores
 * pairs {@link String}->{@link SkillHistory}.
 */
class StudentHistory extends HashMap<String, StudentHistory.SkillHistory>
{
	// TODO: javadoc
	///
	/// Methods
	///

	private void put(RecordKey key, String skill, int outcome, Set<String> otherSkills) {
		if (key == null || skill == null) {
			return;
		}

		SkillHistory history = get(skill);

		if (history == null) {
			history = new SkillHistory();
			put(skill, history);
		}

		history.put(key, outcome, otherSkills);
	}

	private void put(RecordKey key, Set<String> skills, boolean isCorrect) {
		if (key == null || skills == null || skills.size() == 0) {
			return;
		}

		int outcome = isCorrect
		              ?SkillHistory.PASS
		              :skills.size() == 1
		               ?SkillHistory.FAIL_A_SINGLE_SKILL
		               :SkillHistory.FAIL_MULTIPLE_SKILLS;

		for (String skill : skills) {
			Set<String> otherSkills = new HashSet<>(skills);
			otherSkills.remove(skill);

			put(key, skill, outcome, otherSkills);
		}
	}

	void put(long timestamp, String questionID, Set<String> skills, boolean isCorrect) {
		if (skills == null || skills.size() == 0) {
			return;
		}

		put(new RecordKey(timestamp, questionID), skills, isCorrect);
	}

	void buildWeights() {
		if (isEmpty()) {
			return;
		}

		Set<String> skills = keySet();
		for (String skill : skills) {
			get(skill).buildWeights();
		}
	}




	/**************/
	/* Record Key */
	/**************/

	/**
	 * {@code RecordKey} is used as an identifier for a {@link Record Record} in a {@link
	 * SkillHistory SkillHistory} of a
	 * {@link StudentHistory StudentHistory}.
	 * <p/>
	 * {@code RecordKey} is comparable to itself, lower keys representing more recent events.
	 */
	class RecordKey implements Comparable<RecordKey>
	{
		private long timestamp;
		private String questionID = "none";

		RecordKey(long timestamp, String questionID) {
			this.timestamp = timestamp;
			if (questionID != null) {
				this.questionID = questionID;
			}
		}

//		long getTimestamp() {
//			return timestamp;
//		}

		String getQuestionID() {
			return questionID;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			RecordKey that = (RecordKey) o;

			return timestamp == that.timestamp && questionID.equals(that.questionID);
		}

		/**
		 * Compares this key to the {@code other}.
		 * <p/>
		 * The key with the more recent {@code timestamp} is lower.
		 * If the {@code timestamp} of the two keys is the same, the {@code questionID}s are compared as strings.
		 * <p/>
		 * Null keys are considered greater than all.
		 *
		 * @param other
		 * 		the other key to compare to
		 *
		 * @return a negative number if this key represents a more recent event or, if the events are simultaneous, this
		 * key has
		 * a greater question ID
		 */
		@Override
		public int compareTo(RecordKey other) {
			// any non-null key is less than null
			if (other == null) {
				return -1;
			}

			if (equals(other)) {
				return 0;
			}

			if (other.timestamp == timestamp) {
				return questionID.compareTo(other.questionID);
			}
			else {
				return other.timestamp < timestamp ?-1 :1;
			}
		}
	}




	/**********/
	/* Record */
	/**********/

	/**
	 * Contains information about a single student response and the weight of the response for the purposes of skill
	 * level estimation.
	 */
	class Record
	{
		private int outcome;
		private double level = 0.5;
		private Set<String> otherSkills = new HashSet<>();

//		Set<String> getOtherSkills() {
//			return otherSkills;
//		}

//		void setOtherSkills(Set<String> otherSkills) {
//			this.otherSkills = otherSkills;
//		}

		Record(int outcome, Set<String> otherSkills) {
			this.outcome = outcome;
			this.otherSkills = otherSkills;
		}

		int getOutcome() {
			return outcome;
		}

		double getLevel() {
			return level;
		}

		void setLevel(double level) {
			this.level = level;
		}

		String printOtherSkills () {
			if (otherSkills.size() == 0)
			{
				return "\n";
			}

			String res = "";

			for(String skill : otherSkills) {
				res += skill + "\n                            ";
			}

			return res;
		}

		int numberOfOtherSkills() {
			return otherSkills.size();
		}
	}




	/*****************/
	/* Skill History */
	/*****************/

	/**
	 * Contains the history of a single skill for a given student and the current estimated level.
	 */
	class SkillHistory extends ConcurrentSkipListMap<RecordKey, Record>
	{
		///
		/// Internal members
		///

		// How many history entries to take into account when calculating different stats
		private static final int HISTORY_LOOKUP_DEPTH = 10;
//		private static final double ONE_PLUS_THRESHOLD = 1 + 1.0/HISTORY_LOOKUP_DEPTH;

		// Store the most recent history (up to HISTORY_LOOKUP_DEPTH items) for faster processing of new items
		private Vector<Double> recentWeights;
		private Vector<Integer> recentOutcomes;

		// Latest (up to HISTORY_LOOKUP_DEPTH) stats on the skill
		private double totalRecentWeight = 0.0; // recentWeights' total
		private int totalRecentOutcomes = 0;    // recentOutcomes' total

		// Whether the history stores at least HISTORY_LOOKUP_DEPTH items
		private int recentHistorySize = 0;
		private RecordKey oldestInRecentHistory = null;


		///
		/// Constants
		///

		static final int FAIL_A_SINGLE_SKILL = 0;
		static final int PASS = 1;
		static final int FAIL_MULTIPLE_SKILLS = 2;


		///
		/// Constructors
		///

		SkillHistory() {
			recentWeights = new Vector<>(HISTORY_LOOKUP_DEPTH + 1);
			recentOutcomes = new Vector<>(HISTORY_LOOKUP_DEPTH + 1);
		}


		///
		/// Methods
		///

		/**
		 * Adds a record with given key and outcome to the history.
		 * <p/>
		 * The added record is placed into the history according to the ordering on {@link RecordKey
		 * RecordKey}.
		 * If a record with the same key already exists, it will be replaced.
		 *
		 * @param key
		 * 		specifies where in the history the record is to be inserted
		 * @param outcome
		 * 		indicates the status of the record:
		 * 		</ul>
		 * 		<li/>{@link #FAIL_A_SINGLE_SKILL} &mdash; a single-skill failure
		 * 		<li/>{@link #PASS} &mdash; a single- or a multiple-skill success
		 * 		<li/>{@link #FAIL_MULTIPLE_SKILLS} &mdash; a multiple-skill failure
		 * 		</ul>
		 */
		void put(RecordKey key, int outcome, Set<String> otherSkills) {
			if (key == null) {
				return;
			}

			Record rec = get(key);
			if (rec != null && rec.getOutcome() == outcome) {
				return;
			}

			put(key, new Record(outcome, otherSkills));
		}

		/**
		 * Estimates the skill level at the most recent recorded moment.
		 *
		 * @return the skill level estimate between 0.0 and 1.0, with lower value representing poorer skill
		 */
		double getSkillLevel() {
			return totalRecentWeight == 0
			       ?0.5
			       :((double) totalRecentOutcomes)/totalRecentWeight;
		}

		/**
		 * Estimates the skill level at the moment specified by the key.
		 *
		 * @param key
		 * 		the key to start looking up the history at; if the key is {@code null}, the most recent history is looked
		 * 		up
		 *
		 * @return the skill level estimate between 0.0 and 1.0, with lower value representing poorer skill
		 */
		double getSkillLevel(RecordKey key) {
			if (key == null ||
			    ceilingKey(key) == null) { // if the passed key is null, return the estimate for the most recent moment
				return getSkillLevel();
			}

			return get(ceilingKey(key)).getLevel();
		}

		/**
		 * Calculates the weights for all the records.
		 */
		private void buildWeights() {
			if (isEmpty()) {
				return;
			}

			// list all the keys for the records from that point down;
			// the list is tentative for a local rebuild
			Iterator<RecordKey> keys = keySet().descendingIterator();

			recentWeights.clear();
			recentOutcomes.clear();

			totalRecentWeight = 0.0;
			totalRecentOutcomes = 0;

			recentHistorySize = 0;
			oldestInRecentHistory = null;

			RecordKey k;

			// go through all the records on the tentative list;
			// if updating locally, only need to update history with multiple-skill failures within lookup depth
			while (keys.hasNext()) {
				k = keys.next();

				// the record at the current key and the outcome recorded
				Record rec = get(k);
				int outcome = rec.getOutcome();

				// the record's weight for the purposes of skill level estimation
				double weight = (outcome == FAIL_A_SINGLE_SKILL || outcome == PASS)
				?1.0
				:0; // getRecordWeight(totalRecentWeight, totalRecentOutcomes, outcome);
				int outcomeValue = outcome == PASS ?1 :0;

				totalRecentWeight += weight;
				totalRecentOutcomes += outcomeValue;

				rec.setLevel(getSkillLevel());

				recentWeights.add(0, weight);
				recentOutcomes.add(0, outcomeValue);

				if (recentHistorySize == 0) {
					oldestInRecentHistory = k;
				}

				if (recentHistorySize < HISTORY_LOOKUP_DEPTH) {
					recentHistorySize++;
				}
				else {
					totalRecentWeight -= recentWeights.remove(recentHistorySize);
					totalRecentOutcomes -= recentOutcomes.remove(recentHistorySize);

					oldestInRecentHistory = lowerKey(oldestInRecentHistory);
				}
			}
		}

//		/**
//		 * Calculates the weight of a response given its outcome.
//		 *
//		 * @param totRecWeight
//		 * 		the total weight of the previous {@link #HISTORY_LOOKUP_DEPTH} records
//		 * @param totRecOutcomes
//		 * 		the total of the outcomes of the previous {@link #HISTORY_LOOKUP_DEPTH} records
//		 * @param outcome
//		 * 		the outcome of this response; should be {@link #PASS}, {@link #FAIL_A_SINGLE_SKILL}, or {@link
//		 * 		#FAIL_MULTIPLE_SKILLS}
//		 *
//		 * @return the weight of the response
//		 */
//		private double getRecordWeight(double totRecWeight, double totRecOutcomes, int outcome) {
//			return (outcome == FAIL_A_SINGLE_SKILL || outcome == PASS)
//			       ?1.0
//			       :(ONE_PLUS_THRESHOLD - (totRecWeight == 0
//			                               ?0.5
//			                               :totRecOutcomes/totRecWeight));
//		}

		int recentPureOutcomesSize() {
			int count = 0;
			Iterator<RecordKey> records = keySet().iterator();

			while(records.hasNext() && count < TREND_LOOKUP_DEPTH){
				RecordKey record = records.next();

				if (get(record).getOutcome() != FAIL_MULTIPLE_SKILLS) {
					count++;
				}
			}

			return count;
		}

		private RecordKey ceilingPureOutcome(RecordKey key) {
			if(key == null) {
				return null;
			}

			for (RecordKey record : tailMap(key).keySet()) {
				if (get(record).getOutcome() != FAIL_MULTIPLE_SKILLS) {
					return record;
				}
			}

			return null;
		}

		private static final int TREND_LOOKUP_DEPTH = 10;

		/**
		 * Calculates the trend in the skill level using a linear regression.
		 *
		 * @return skill level trend
		 */
		double trend() {
			if (isEmpty() || totalRecentWeight == 0) {
				return 0;
			}

			// Using the Normal Equation for linear regressions

			int outcomesCount = recentPureOutcomesSize();
			SimpleMatrix
					// The matrix of columns, (1, i)
//					X = new SimpleMatrix(2, size() > TREND_LOOKUP_DEPTH ?TREND_LOOKUP_DEPTH :size()),
					X = new SimpleMatrix(2, outcomesCount > TREND_LOOKUP_DEPTH ?TREND_LOOKUP_DEPTH :outcomesCount),

					// The vector of values, skillLevel(i)
					y = new SimpleMatrix(X.numCols(), 1);

			RecordKey k = ceilingPureOutcome(firstKey());

			// Initializing the matrices using the last #TREND_LOOKUP_DEPTH records
			for (int i = y.numRows() - 1; i >= 0; i--, k = ceilingPureOutcome(higherKey(k))) {
				X.set(0, i, 1);
				X.set(1, i, i);
				y.set(i, 0, get(k).getLevel());
			}

			// Applying the Normal Equation to get the coefficients:
			// theta_0 is the y-intercept, theta_1 is the slope
			SimpleMatrix theta = X.mult(X.transpose()).pseudoInverse().mult(X).mult(y);

			// Returning theta_1
			return theta.get(1, 0);
		}
	}
}