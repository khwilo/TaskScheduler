package com.pluralsight.datetime;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

public class Calendar {

	final private NavigableSet<WorkPeriod> workPeriods = new TreeSet<>(); // ordered by start time
	final private NavigableSet<Event> events = new TreeSet<>();		      // ordered by start time
	final private List<Task> tasks = new ArrayList<>();                   // ordered by priority

	public Schedule createSchedule(Clock clock) {
	 	//TODO (maybe) save overwritePeriodsByEvents from having to consider periods and events in the past
		NavigableSet<WorkPeriod> overwrittenPeriods = overwritePeriodsByEvents(workPeriods, events, clock.getZone());
		LocalDateTime ldt = LocalDateTime.now(clock);

		List<TaskPart> remainingTaskParts = tasks.stream().map(TaskPart::wholeOf).collect(toList());

		List<WorkPeriod> scheduledPeriods = new ArrayList<>();
		for (WorkPeriod p : overwrittenPeriods) {
			LocalDateTime effectiveStartTime = p.getStartTime().isAfter(ldt) ? p.getStartTime() : ldt;
			// TODO doesn't allow for DST changes during WorkPeriod
			if (WorkPeriod.MINIMUM_DURATION.minus(Duration.between(effectiveStartTime, p.getEndTime())).isNegative()) {
				p.setTaskParts(remainingTaskParts);
				scheduledPeriods.add(p.split(p.getEndTime()).orElseThrow(IllegalStateException::new));
				remainingTaskParts = p.getTaskParts();
			}
		}
		return new Schedule(clock.getZone(), scheduledPeriods, events, remainingTaskParts.isEmpty());
	}

	NavigableSet<WorkPeriod> overwritePeriodsByEvents(ZoneId zone) {
		return overwritePeriodsByEvents(workPeriods, events, zone);
	}

	private NavigableSet<WorkPeriod> overwritePeriodsByEvents(NavigableSet<WorkPeriod> workPeriods, NavigableSet<Event> events,
													  ZoneId zone) {
		NavigableSet<WorkPeriod> rawPeriods = workPeriods.stream()
				.map(WorkPeriod::copy)
				.collect(toCollection(TreeSet::new));
		NavigableSet<WorkPeriod> overwrittenPeriods = new TreeSet<>();
		WorkPeriod period = rawPeriods.pollFirst();
		Event event = events.isEmpty() ? null : events.first();
		while (period != null && event != null) {
			if (! period.getEndTime().isAfter(event.getLocalStartDateTime(zone))) {
				// non-overlapping, period first
				overwrittenPeriods.add(period);
				period = rawPeriods.higher(period);
			} else if (! period.getStartTime().isBefore(event.getLocalEndDateTime(zone))) {
				// non-overlapping, event first
				event = events.higher(event);
			} else if (period.getStartTime().isBefore(event.getLocalStartDateTime(zone))) {
				// overlapping, period starts first
				overwrittenPeriods.add(period.split(event.getLocalStartDateTime(zone)).get());
			} else if (period.getEndTime().isAfter(event.getLocalEndDateTime(zone))) {
				// overlapping, event starts first or at same time
				period.split(event.getLocalEndDateTime(zone)).get();
				event = events.higher(event);
			} else {
				// event encloses period
				period = workPeriods.higher(period);
			}
		}
		if (period != null) {
			overwrittenPeriods.add(period);
			overwrittenPeriods.addAll(rawPeriods.tailSet(period));
		}
		return overwrittenPeriods;
	}

	public Calendar addWorkPeriod(WorkPeriod p) {
		WorkPeriod preceding = workPeriods.floor(p);
		WorkPeriod following = workPeriods.ceiling(p);
		if (preceding != null && ! preceding.getEndTime().isBefore(p.getStartTime())) {
			throw new IllegalArgumentException("Work Periods cannot overlap: " + preceding + "," + p);
		} else if (following != null && ! following.getStartTime().isAfter(p.getEndTime())) {
			throw new IllegalArgumentException("Work Periods cannot overlap: " + p + "," + following);
		}
		workPeriods.add(p);
		return this;
	}

	public Calendar addWorkPeriods(List<WorkPeriod> periods) {
		for (WorkPeriod wp : periods) {
			addWorkPeriod(wp);
		}
		return this;
	}

	public Calendar addTask(int hours, int minutes, String description) {
		addTask(new Task(hours, minutes, description));
		return this;
	}

	public Calendar addTask(Task task) {
		tasks.add(task);
		return this;
	}

	public Calendar addEvent(Event e) {
		events.add(e);
		return this;
	}

	public Calendar addEvent(ZonedDateTime eventDateTime, Duration duration, String description) {
		addEvent(Event.of(eventDateTime, eventDateTime.plus(duration), description));
		return this;
	}
}
