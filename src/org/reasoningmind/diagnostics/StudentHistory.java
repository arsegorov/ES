package org.reasoningmind.diagnostics;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Keeps the history of a given student's responses.
 * <p>
 * The history is represented as a set of individual-skill histories. More specifically, the overall history stores
 * pairs {@link Skill}->{@link SkillHistory}.
 */
class StudentHistory
		extends HashMap<Skill, StudentHistory.SkillHistory>
{ // TODO: javadoc
	///
	/// Fields
	///

	private String studentID;
	private HashMap<Skill, Double> skillLevels;
	private boolean changed = true;


	///
	/// Constructors
	///

	StudentHistory(String studentID) {
		this.studentID = studentID;
	}


	///
	/// Methods
	///

	public String getStudentID() {
		return studentID;
	}

	private void put(RecordKey key, Skill skill, int outcome) {
		if (key == null || skill == null) {
			return;
		}

		SkillHistory history = get(skill);

		if (history == null) {
			history = new SkillHistory();
			put(skill, history);
		}

		changed |= history.put(key, outcome);
	}

	public void put(RecordKey key, Set<Skill> skills, boolean isCorrect) {
		if (key == null || skills == null || skills.size() == 0) {
			return;
		}

		int outcome = isCorrect
		              ?SkillHistory.PASS
		              :skills.size() == 1
		               ?SkillHistory.FAIL_A_SINGLE_SKILL
		               :SkillHistory.FAIL_MULTIPLE_SKILLS;

		for (Skill skill : skills) {
			put(key, skill, outcome);
		}
	}

	public void put(long timestamp, String questionID, Set<Skill> skills, boolean isCorrect) {
		if(skills == null || skills.size() == 0) {
			return;
		}

		put(new RecordKey(timestamp, questionID), skills, isCorrect);
	}

	public HashMap<Skill, Double> getSkillLevels() {
		if(changed) {
			skillLevels = new HashMap<>(size());

			Set<Skill> skills = keySet();

			for (Skill skill : skills) {
				skillLevels.put(skill, get(skill).getSkillLevel());
			}

			changed = false;
		}

		return skillLevels;
	}


	/**
	 * {@code RecordKey} is used as an identifier for a {@link Record Record} in a {@link
	 * SkillHistory SkillHistory} of a
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
	class Record
	{
		private int outcome;
		private double weight;

		Record(int outcome, double weight) {
			this.outcome = outcome;
			this.weight = weight;
		}

		int getOutcome() {
			return outcome;
		}

		double getWeight() {
			return weight;
		}

		void setWeight(double weight) {
			this.weight = weight;
		}
	}


	/**
	 * Contains the history of a single skill for a given student and the current estimated level.
	 */
	class SkillHistory
			extends ConcurrentSkipListMap<RecordKey, Record>
	{
		///
		/// Internal members
		///

		// How many history entries to take into account when calculating different stats
		private int historyLookupDepth = 20;
		private double onePlusThreshold = 1.05;

		// Store the most recent history (up to historyLookupDepth items) for faster processing of new items
		private Vector<Double> recentWeights;
		private Vector<Integer> recentOutcomes;

		// Latest (up to historyLookupDepth) stats on the skill
		private double totalRecentWeight = 0.0; // recentWeights' total
		private int totalRecentOutcomes = 0; // recentOutcomes' total

		// Whether the history stores at least historyLookupDepth items
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
			recentWeights = new Vector<>(historyLookupDepth + 1);
			recentOutcomes = new Vector<>(historyLookupDepth + 1);
		}

		SkillHistory(int historyLookupDepth) {
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
		 * 		<li/>{@link #FAIL_A_SINGLE_SKILL} &mdash; a single-skill failure
		 * 		<li/>{@link #PASS} &mdash; a single- or a multiple-skill success
		 * 		<li/>{@link #FAIL_MULTIPLE_SKILLS} &mdash; a multiple-skill failure
		 * 		</ul>
		 */
		boolean put(RecordKey key, int outcome) {
			if (key == null) {
				return false;
			}

			Record rec = get(key);
			if (rec != null && rec.getOutcome() == outcome) {
				return false;
			}

			// the record's weight for the purposes of skill level estimation
			double weight = (outcome == FAIL_A_SINGLE_SKILL || outcome == PASS)
			                ?1.0
			                :(onePlusThreshold - getSkillLevel(key));

			// the value of the outcome used for skill level estimation
			int outcomeValue = outcome == PASS ?1 :0;

			RecordKey lower = lowerKey(key); // the oldest more recent key in the history

			if (lower == null) {
				if (rec == null) { // the key isn't in the history and there are no more recent keys in the history
					put(key, new Record(outcome, weight));
					totalRecentWeight += weight;
					totalRecentOutcomes += outcomeValue;

					recentWeights.add(0, weight);
					recentOutcomes.add(0, outcomeValue);

					if (recentHistorySize == 0) {
						oldestInRecentHistory = key;
					}

					if (recentHistorySize < historyLookupDepth) { // see if the recent history isn't full yet
						recentHistorySize++;
					}
					else {
						totalRecentWeight -= recentWeights.remove(recentHistorySize);
						totalRecentOutcomes -= recentOutcomes.remove(recentHistorySize);

						oldestInRecentHistory = lowerKey(oldestInRecentHistory);
					}
				}
				else { // the key is already in the history and is the most recent one
					replace(key, new Record(outcome, weight));
					totalRecentWeight += weight - recentWeights.remove(0);
					totalRecentOutcomes += outcomeValue - recentOutcomes.remove(0);

					recentWeights.add(0, weight);
					recentOutcomes.add(0, outcomeValue);
				}
			}
			else {
				put(key, new Record(outcome, weight));
				rebuildWeightsFrom(key);
			}

			return true;
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
				Record rec = get(k);

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

		/**
		 * Rebuild the weight values for the records starting from {@code fromKey}.
		 * <p>
		 * The method recalculates weights for the records with keys that are {@code <= fromKey}, but only those that
		 * might be affected by a change in the record at {@code fromKey}.
		 * <p>
		 * If {@code fromKey == null}, the method rebuilds the entire available history.
		 *
		 * @param fromKey
		 * 		the key to rebuild the weights from; if it is {@code null}, this method calls {@link #rebuildWeights()}
		 * 		instead
		 */
		private void rebuildWeightsFrom(RecordKey fromKey) {
			if (isEmpty()) {
				return;
			}
			if (fromKey == null) {
				rebuildWeights();
				return;
			}

			RecordKey k = fromKey;

			// back up for historyLookupDepth - 1 records,
			// or to the last record, whichever occurs first
			for (int i = 0; i < historyLookupDepth - 1 && higherKey(k) != null; i++) {
				k = higherKey(k);
			}

			// list all the keys for the records from that point down;
			// the list is tentative for a local rebuild
			Iterator<RecordKey> keys = headMap(k, true).keySet().descendingIterator();

			// if the list isn't empty
			if (keys.hasNext()) {
				// similar to #recentWeights
				Vector<Double> recWeights = new Vector<>(historyLookupDepth + 1);
				// similar to #recentOutcomes
				Vector<Integer> recOutcomes = new Vector<>(historyLookupDepth + 1);

				// similar to #totalRecentWeight
				double totRecWeight = 0.0;
				// similar to #totalRecentOutcomes
				int totRecOutcomes = 0;

				// similar to #recentHistorySize
				int recHistorySize = 0;
				// similar to #oldestInRecentHistory
				RecordKey oldestInRecHistory = null;

				// counts the length of history without records of failing multiple skills
				// (those are the only records that need their weights updated)
				int sinceLastMultipleFail = 0;

				// go through all the records on the tentative list;
				// only need to update history while multiple-skill failures are within #historyLookupDepth from each other
				while (keys.hasNext() && sinceLastMultipleFail < historyLookupDepth) {
					k = keys.next();

					// the record at the current key and the outcome recorded
					Record rec = get(k);
					int outcome = rec.getOutcome();

					// for the keys that are at least as recent as fromKey,
					// we see how long it has been since the last time multiple-skill fail occurred
					if (k.compareTo(fromKey) <= 0) {
						if (outcome == FAIL_MULTIPLE_SKILLS) {
							sinceLastMultipleFail = 0;
						}
						else {
							sinceLastMultipleFail++;
						}
					}

					// the record's weight for the purposes of skill level estimation
					double weight = getRecordWeight(totRecWeight, totRecOutcomes, outcome);
					int outcomeValue = outcome == PASS ?1 :0;

					// if the record represents a multiple-skill fail, its weight get updated
					if (outcome == FAIL_MULTIPLE_SKILLS) {
						rec.setWeight(weight);
					}

					totRecWeight += weight;
					totRecOutcomes += outcomeValue;

					recWeights.add(0, weight);
					recOutcomes.add(0, outcomeValue);

					if (recHistorySize == 0) {
						oldestInRecHistory = k;
					}

					if (recHistorySize < historyLookupDepth) {
						recHistorySize++;
					}
					else {
						totRecWeight -= recWeights.remove(recentHistorySize);
						totRecOutcomes -= recOutcomes.remove(recentHistorySize);

						oldestInRecHistory = lowerKey(oldestInRecHistory);
					}
				}

				// if the recent history got affected,
				// refresh it by continuing all the way down to the very bottom record,
				// but the weights don't need to be recalculated
				if (k.compareTo(oldestInRecentHistory) <= 0) {
					while (keys.hasNext()) {
						k = keys.next();
						Record rec = get(k);

						double weight = rec.getWeight(); // just use the previously calculated weight
						int outcomeValue = rec.getOutcome() == PASS ?1 :0;

						totRecWeight += weight;
						totRecOutcomes += outcomeValue;

						recWeights.add(0, weight);
						recOutcomes.add(0, outcomeValue);

						if (recHistorySize == 0) {
							oldestInRecHistory = k;
						}

						if (recHistorySize < historyLookupDepth) {
							recHistorySize++;
						}
						else {
							totRecWeight -= recWeights.remove(recentHistorySize);
							totRecOutcomes -= recOutcomes.remove(recentHistorySize);

							oldestInRecHistory = lowerKey(oldestInRecHistory);
						}
					}

					recentOutcomes = recOutcomes;
					recentWeights = recWeights;

					totalRecentOutcomes = totRecOutcomes;
					totalRecentWeight = totRecWeight;

					recentHistorySize = recHistorySize;
					oldestInRecentHistory = oldestInRecHistory;
				}
			}
		}

		/**
		 * Recalculate the weights for all the records.
		 */
		private void rebuildWeights() {
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
				double weight = getRecordWeight(totalRecentWeight, totalRecentOutcomes, outcome);
				int outcomeValue = outcome == PASS ?1 :0;

				// if the record represents failing multiple skills, its weight needs updating
				if (outcome == FAIL_MULTIPLE_SKILLS) {
					rec.setWeight(weight);
				}

				totalRecentWeight += weight;
				totalRecentOutcomes += outcomeValue;

				recentWeights.add(0, weight);
				recentOutcomes.add(0, outcomeValue);

				if (recentHistorySize == 0) {
					oldestInRecentHistory = k;
				}

				if (recentHistorySize < historyLookupDepth) {
					recentHistorySize++;
				}
				else {
					totalRecentWeight -= recentWeights.remove(recentHistorySize);
					totalRecentOutcomes -= recentOutcomes.remove(recentHistorySize);

					oldestInRecentHistory = lowerKey(oldestInRecentHistory);
				}
			}
		}

		/**
		 * Calculates the weight of a response given its outcome.
		 *
		 * @param totRecWeight
		 * 		the total weight of the previous #historyLookupDepth records
		 * @param totRecOutcomes
		 * 		the total of the outcomes of the previous #historyLookupDepth records
		 * @param outcome
		 * 		the outcome of this response; should be {@link #PASS}, {@link #FAIL_A_SINGLE_SKILL}, or {@link
		 * 		#FAIL_MULTIPLE_SKILLS}
		 *
		 * @return
		 */
		private double getRecordWeight(double totRecWeight, double totRecOutcomes, int outcome) {
			return (outcome == FAIL_A_SINGLE_SKILL || outcome == PASS)
			       ?1.0
			       :(onePlusThreshold - (totRecWeight == 0
			                             ?0.5
			                             :totRecOutcomes/totRecWeight));
		}
	}
}