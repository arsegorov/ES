package org.reasoningmind.diagnostics;

import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Keeps the history of a student's responses.
 */
class StudentHistory
		extends HashMap<Skill, StudentHistory.SingleSkillHistory>
{
	/**
	 * {@code RecordKey} is used as an identifier for a {@link SingleSkillRecord SingleSkillRecord} in a {@link
	 * SingleSkillHistory SingleSkillHistory} of a
	 * {@link StudentHistory StudentHistory}.
	 * <p/>
	 * {@code RecordKey} is comparable to itself, lower keys representing more recent events.
	 */
	class RecordKey
			implements Comparable<RecordKey>
	{
		long timestamp;
		String questionID;

		RecordKey(long timestamp, String questionID) {
			this.timestamp = timestamp;
			this.questionID = questionID;
		}

		String getQuestionID() {
			return questionID;
		}

		long getTimestamp() {
			return timestamp;
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

			if (timestamp != that.timestamp) {
				return false;
			}

			return questionID != null ?questionID.equals(that.questionID) :that.questionID == null;
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
				return other.timestamp < timestamp ?1 :-1;
			}
		}
	}


	/**
	 * Contains information about a single student response and the weight of the response for the purposes of skill
	 * level estimation.
	 */
	class SingleSkillRecord
	{
		private int outcome;
		private double weight;

		SingleSkillRecord(int outcome, double weight) {
			this.outcome = outcome;
			this.weight = weight;
		}

		int getOutcome() {
			return outcome;
		}

		double getWeight() {
			return weight;
		}

		public void setWeight(double weight) {
			this.weight = weight;
		}
	}

	/**
	 * Contains the history of a single skill for a given student and the current estimated level.
	 */
	class SingleSkillHistory
			extends ConcurrentSkipListMap<RecordKey, SingleSkillRecord>
	{
		///
		/// Internal members
		///

		// How many history entries to take into account when calculating different stats
		private int historyLookupDepth = 20;
		private double onePlusThreshold = 1.05;

		// Latest (up to historyLookupDepth) stats on the skill
		private double totalRecentWeight = 0.0; // recentWeights' total
		private int totalRecentOutcomes = 0; // recentOutcomes' total

		// Store the most recent history (up to historyLookupDepth items) for faster processing of new items
		private Vector<Double> recentWeights;
		private Vector<Integer> recentOutcomes;

		// Whether the history stores at least historyLookupDepth items
		private int recentHistorySize = 0;


		///
		/// Constants
		///

		static final int FAIL_SINGLE_SKILL = 0;
		static final int PASS = 1;
		static final int FAIL_MULTIPLE_SKILLS = 2;


		///
		/// Constructors
		///
		SingleSkillHistory() {
			recentWeights = new Vector<>(historyLookupDepth + 1);
			recentOutcomes = new Vector<>(historyLookupDepth + 1);
		}

		SingleSkillHistory(int historyLookupDepth) {
			if (historyLookupDepth < 1) {
				historyLookupDepth = 1;
			}
			this.historyLookupDepth = historyLookupDepth;
			onePlusThreshold = 1 + 1.0/historyLookupDepth;

			recentWeights = new Vector<>(historyLookupDepth + 1);
			recentOutcomes = new Vector<>(historyLookupDepth + 1);
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
		 * 		<li/>{@link #FAIL_SINGLE_SKILL} &mdash; a single-skill failure
		 * 		<li/>{@link #PASS} &mdash; a single- or a multiple-skill success
		 * 		<li/>{@link #FAIL_MULTIPLE_SKILLS} &mdash; a multiple-skill failure
		 * 		</ul>
		 */
		void put(RecordKey key, int outcome) {
			if (key == null) {
				return;
			}

			// the record's weight for the purposes of skill level estimation
			double weight = (outcome == FAIL_SINGLE_SKILL || outcome == PASS)
			                ?1.0
			                :(onePlusThreshold - getSkillLevel(key));

			// the value of the outcome used for skill level estimation
			int outcomeValue = outcome == PASS ?1 :0;

			RecordKey floor = floorKey(key), lower =
					lowerKey(key); // the oldest more recent key in the history

			if (floor == null) { // the key isn't in the history and there are no more recent keys in the history
				put(key, new SingleSkillRecord(outcome, weight));
				totalRecentWeight += weight;
				totalRecentOutcomes += outcomeValue;

				recentWeights.add(0, weight);
				recentOutcomes.add(0, outcomeValue);

				if (recentHistorySize < historyLookupDepth) { // see if the "head" isn't full yet
					recentHistorySize++;
				}
				else {
					totalRecentWeight -= recentWeights.remove(recentHistorySize);
					totalRecentOutcomes -= recentOutcomes.remove(recentHistorySize);
				}
			}
			else if (floor.equals(key) && // the key is already in the history
			         lower == null) { // and is the most recent one
				replace(key, new SingleSkillRecord(outcome, weight));
				totalRecentWeight += weight - recentWeights.remove(0);
				totalRecentOutcomes += outcomeValue - recentOutcomes.remove(0);

				recentWeights.add(0, weight);
				recentOutcomes.add(0, outcomeValue);
			}
			else {
				// todo
			}
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
			if (key == null) { // if the passed key is null, return the estimate for the most recent moment
				return getSkillLevel();
			}

			RecordKey k = floorKey(key);

			// if there are no more recent recorded moments in the history, return the estimate for the most recent moment
			if (k == null || k.equals(key)) {
				return getSkillLevel();
			}

			double totRecWeight = 0.0;
			int totRecOutcomes = 0;

			k = key;
			for (int i = 0; i < historyLookupDepth && k != null; i++, k = higherKey(k)) {
				SingleSkillRecord rec = get(k);

				if (rec == null) {
					i--;
				}
				else {
					totRecWeight += rec.getWeight();
					totRecOutcomes += rec.getOutcome() == 1 ?1 :0;
				}
			}

			return totRecWeight == 0
			       ?0.5
			       :((double) totRecOutcomes)/totRecWeight;
		}

		void rebuildWeightsFrom(RecordKey key) {
			RecordKey k;

			if (key == null) {
				try {
					k = lastKey();                      // if the key is null, start from the very top of the history
				}
				catch (NoSuchElementException nsee) {
					return;                             // if the history is empty, do nothing
				}
			}
			else {
				k = key;                                // otherwise, start from the key
			}

			// back up for historyLookupDepth records, if possible; otherwise, as far up as possible
			for (int i = 0; i < historyLookupDepth - 1 && higherKey(k) != null; i++) {
				k = higherKey(k);
			}

			// list all the key from that point down
			Iterator<RecordKey> keys = headMap(k, true).keySet().descendingIterator();

			// if there are any such keys
			if (keys.hasNext()) {

				// start rebuilding the weights
				int recHistorySize = 0;
				int sinceLastMultipleFail = 0;

				Vector<Double> recWeights = new Vector<>(historyLookupDepth + 1);
				Vector<Integer> recOutcomes = new Vector<>(historyLookupDepth + 1);

				double totRecWeight = 0.0;
				int totRecOutcomes = 0;

				// go through all the records
				for (k = keys.next(); keys.hasNext() && sinceLastMultipleFail < historyLookupDepth;
				     k = keys.next()) {

					SingleSkillRecord rec = get(k);
					int outcome = rec.getOutcome();

					if(k.compareTo(key) < 0) {
						if (outcome == FAIL_MULTIPLE_SKILLS) {
							sinceLastMultipleFail = 0;
						}
						else {
							sinceLastMultipleFail++;
						}
					}

					// the record's weight for the purposes of skill level estimation
					double weight = (outcome == FAIL_SINGLE_SKILL || outcome == PASS)
					                ?1.0
					                :(onePlusThreshold - (totRecWeight == 0
					                                      ?0.5
					                                      :((double) totRecOutcomes)/totRecWeight));
					int outcomeValue = outcome == PASS ?1 :0;

					rec.setWeight(weight);

					totRecWeight += weight;
					totRecOutcomes += outcomeValue;

					recWeights.add(0, weight);
					recOutcomes.add(0, outcomeValue);

					if (recHistorySize < historyLookupDepth) { // see if the "head" isn't full yet
						recHistorySize++;
					}
					else {
						totRecWeight -= recWeights.remove(recentHistorySize);
						totRecOutcomes -= recOutcomes.remove(recentHistorySize);
					}
				}
			}
		}
	}


	///
	/// Fields
	///
	private String studentID;

	///
	/// Methods
	///
	StudentHistory(String studentID) {
		this.studentID = studentID;
	}

	public String getStudentID() {
		return studentID;
	}
}
