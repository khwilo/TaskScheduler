package com.pluralsight.datetime;

import java.time.Duration;

public class TaskPart {

	final private Task owner;
	final private int partSequenceNumber;
	private Duration duration;

	public TaskPart(Task owner, Duration duration, int partSequenceNumber) {
		this.owner = owner;
		this.duration = duration;
		this.partSequenceNumber = partSequenceNumber;
	}

	Task getOwner() {
		return owner;
	}

	public Duration getDuration() {
		return duration;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		TaskPart taskPart = (TaskPart) o;

		return partSequenceNumber == taskPart.partSequenceNumber && owner.equals(taskPart.owner) && duration.equals(taskPart.duration);
	}

	@Override
	public int hashCode() {
		int result = owner.hashCode();
		result = 31 * result + duration.hashCode();
		result = 31 * result + partSequenceNumber;
		return result;
	}

	@Override
	public String toString() {
		int taskPartCount = owner.getTaskPartCount();
		return owner.getDescription() +
				(taskPartCount != 1 ? "(" + partSequenceNumber + "/" + taskPartCount + ")" : "" ) +
				", " + Utils.formatDuration(duration);
	}

	public static TaskPart wholeOf(Task t) {
		return t.createTaskPart(t.getDuration());
	}

	public TaskPart split(Duration beforeSplitDuration) {
		TaskPart tp2 = getOwner().createTaskPart(getDuration().minus(beforeSplitDuration));
		duration = beforeSplitDuration;
		return tp2;
	}
}
