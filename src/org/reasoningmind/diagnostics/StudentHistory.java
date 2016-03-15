package org.reasoningmind.diagnostics;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Keeps the history of a student's responses
 */
class StudentHistory extends HashMap<StudentHistory.StudentResponseRecordKey, StudentHistory.StudentResponseRecord> {
	class StudentResponseRecordKey {
		long timestamp;
		String questionID;

		StudentResponseRecordKey(long timestamp, String questionID) {
			this.timestamp = timestamp;
			this.questionID = questionID;
		}

		public String getQuestionID() {
			return questionID;
		}

		public long getTimestamp() {
			return timestamp;
		}

		@Override public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			StudentResponseRecordKey that = (StudentResponseRecordKey) o;

			if (timestamp != that.timestamp) {
				return false;
			}

			return questionID != null ?questionID.equals(that.questionID) :that.questionID == null;
		}
	}

	class StudentResponseRecord {
		/**
		 * The history this record belongs to.
		 */
		private StudentHistory studentHistory;

		private static final int PREVIOUS_RESPONSES_LOOKUP_SIZE = 20;
		private Map<Skills, CircularFifoQueue<Long>> skillStats;
		private boolean correct;

		StudentResponseRecord(StudentHistory studentHistory,
		                      Set<Skills> skillSet,
		                      boolean correct) {
			this.studentHistory = studentHistory;
			this.correct = correct;

			this.skillStats = new HashMap<>();

			for (Skills skill : skillSet) {
				StudentResponseRecordKey key = studentHistory.getLastRecordKeyForSkill(skill);

				if (key != null) {
					StudentResponseRecord lastRecord = studentHistory.get(key);
					CircularFifoQueue lastRecordQueue = lastRecord.skillStats.get(skill);
				}
				else {

				}
			}
		}

		public boolean isCorrect() {
			return correct;
		}

		public Set<Skills> getSkills() {
			return skillStats.keySet();
		}
	}

	private String studentID;
	private final HashMap<Skills, StudentResponseRecordKey> lastRecordsForSkills = new HashMap<>();

	StudentHistory(String studentID) {
		this.studentID = studentID;
	}

	public StudentResponseRecordKey getLastRecordKeyForSkill(Skills skill) {
		if (lastRecordsForSkills.containsKey(skill)) {
			return lastRecordsForSkills.get(skill);
		}
		else {
			return null;
		}
	}
}
