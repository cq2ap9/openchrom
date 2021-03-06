/*******************************************************************************
 * Copyright (c) 2015, 2018 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Dr. Alexander Kerner - initial API and implementation
 *******************************************************************************/
package net.sf.kerner.utils.time;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 * A {@code TimePeriod} represents a period of time, which has a start point, a stop point and a duration.
 * </p>
 * <p>
 * This class is fully thread save.
 * </p>
 * <p>
 * TODO example
 * </p>
 * 
 * @author <a href="mailto:alex.kerner.24@googlemail.com">Alexander Kerner</a>
 * @version 2010-10-01
 * @see java.util.concurrent.TimeUnit
 */
public class TimePeriod {

	// Field //
	/**
	 * Default time unit for this time period.
	 */
	public final static TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MILLISECONDS;
	private volatile long start, stop;
	private final TimeUnit timeUnit;

	// Constructor //
	/**
	 * <p>
	 * Constructs a new {@code TimePeriod} with given start point, stop point and time unit.
	 * </p>
	 * 
	 * @param start
	 *            start point of this {@code TimePeriod}
	 * @param stop
	 *            stop point of this {@code TimePeriod}
	 * @param timeUnit
	 *            time unit of start and stop points
	 * @see java.util.concurrent.TimeUnit
	 */
	public TimePeriod(long start, long stop, TimeUnit timeUnit) {
		this.start = start;
		this.stop = stop;
		this.timeUnit = timeUnit;
	}

	/**
	 * <p>
	 * Constructs a new {@code TimePeriod} with given start point and stop point.
	 * </p>
	 * 
	 * @param start
	 *            start point of this {@code TimePeriod}
	 * @param stop
	 *            stop point of this {@code TimePeriod}
	 */
	public TimePeriod(long start, long stop) {
		this.start = start;
		this.stop = stop;
		this.timeUnit = DEFAULT_TIME_UNIT;
	}

	// Private //
	// Protected //
	// Public //
	// Override //
	public String toString() {

		return Long.toString(getDuration());
	}

	/**
	 * <p>
	 * Returns the start point of this time period. Time unit is {@link TimePeriod#DEFAULT_TIME_UNIT}.
	 * </p>
	 * 
	 * @return start point of this time period
	 */
	public long getStart() {

		return getStart(timeUnit);
	}

	/**
	 * <p>
	 * Returns the stop point of this time period. Time unit is {@link TimePeriod#DEFAULT_TIME_UNIT}.
	 * </p>
	 * 
	 * @return stop point of this time period
	 */
	public long getStop() {

		return getStop(timeUnit);
	}

	/**
	 * <p>
	 * Returns the start point of this time period in given time period.
	 * </p>
	 * 
	 * @param tu
	 *            {@link java.util.concurrent.TimeUnit} for start point
	 * @return start point of this time period
	 */
	public long getStart(TimeUnit tu) {

		return tu.convert(start, timeUnit);
	}

	/**
	 * <p>
	 * Returns the stop point of this time period in given time period.
	 * </p>
	 * 
	 * @param tu
	 *            {@link java.util.concurrent.TimeUnit} for stop point
	 * @return stop point of this time period
	 */
	public long getStop(TimeUnit tu) {

		return tu.convert(stop, timeUnit);
	}

	/**
	 * <p>
	 * Get duration of this {@code TimePeriod}.
	 * </p>
	 * 
	 * @return duration of this time period. Meaning stop point - start point
	 */
	public long getDuration() {

		return getDuration(timeUnit);
	}

	/**
	 * <p>
	 * Get duration of this {@code TimePeriod}, converted to given {@code TimeUnit}.
	 * </p>
	 * 
	 * @param tu
	 *            {@link java.util.concurrent.TimeUnit} in which duration is
	 *            measured
	 * @return duration of this time period. Meaning stop point - start point.
	 */
	public long getDuration(TimeUnit tu) {

		return tu.convert((stop - start), timeUnit);
	}
}
