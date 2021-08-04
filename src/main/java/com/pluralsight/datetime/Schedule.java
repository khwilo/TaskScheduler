package com.pluralsight.datetime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static java.util.stream.Collectors.*;

public class Schedule {

	private final ZoneId zoneId;
	private final List<WorkPeriod> scheduledPeriods;
	private final NavigableSet<Event> events;

	List<WorkPeriod> getScheduledPeriods() {
		return scheduledPeriods;
	}

	boolean isSuccessful() {
		return successful;
	}

	private final boolean successful;

	public Schedule(ZoneId zoneId, List<WorkPeriod> scheduledPeriods, NavigableSet<Event> events, boolean success) {
		this.zoneId = zoneId;
		this.scheduledPeriods = scheduledPeriods;
		this.events = events;
		this.successful = success;
	}

	@Override
	public String toString() {

		if (!successful) return "Schedule unsuccessful: insufficent time for tasks";

		List<WorkPeriod> printablePeriods = scheduledPeriods.stream().map(WorkPeriod::copy).collect(toList());
		List<WorkPeriod> periodSplitByMidnight = printablePeriods.stream()
				.map(WorkPeriod::split)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(toList());
		printablePeriods.addAll(periodSplitByMidnight);

		NavigableMap<LocalDateTime, String> dateTimeToPeriodOutput = printablePeriods.stream()
				.collect(groupingBy(WorkPeriod::getStartTime, TreeMap::new, mapping(WorkPeriod::toString, joining())));

		List<Event> printableEvents = events.stream().map(Event::copy).collect(toList());
		List<Event> eventsSplitByMidnight = printableEvents.stream()
				.map(e -> e.split(zoneId))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(toList());
		printableEvents.addAll(eventsSplitByMidnight);

		Map<LocalDateTime, String> dateTimeToEventOutput = printableEvents.stream()
				.collect(groupingBy(e -> e.getLocalStartDateTime(zoneId), mapping(e -> e.toString(zoneId), joining())));

		dateTimeToPeriodOutput.putAll(dateTimeToEventOutput);

		NavigableMap<LocalDate, String> dateToCalendarObjectOutput = dateTimeToPeriodOutput.entrySet().stream()
				.collect(groupingBy(e -> e.getKey().toLocalDate(), TreeMap::new, mapping(Map.Entry::getValue,joining())));

		StringBuilder sb = new StringBuilder();

		for (LocalDate date : dateToCalendarObjectOutput.keySet()) {
			sb.append("\n");
			sb.append(date);
			sb.append(dateToCalendarObjectOutput.get(date));
		}

		return sb.toString();
	}
}
