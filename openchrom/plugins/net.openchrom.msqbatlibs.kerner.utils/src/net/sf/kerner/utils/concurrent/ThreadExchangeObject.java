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
package net.sf.kerner.utils.concurrent;

/**
 *
 * TODO description
 *
 * <p>
 * <b>Example:</b><br>
 * </p>
 *
 * <p>
 *
 * <pre>
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * &#064;Override
 * public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
 * 
 * 	final ThreadExchangeObject&lt;Integer&gt; eo = new ThreadExchangeObject&lt;&gt;();
 * 	for(final RenamingStrategy s : strategies) {
 * 		final String result = s.offer(file.getFileName().toString());
 * 		Display.getDefault().asyncExec(new Runnable() {
 * 
 * 			&#064;Override
 * 			public void run() {
 * 
 * 				final MessageBox dialog = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
 * 				dialog.setText(&quot;My info&quot;);
 * 				dialog.setMessage(&quot;Do you really want to do this?&quot;);
 * 				// open dialog and await user selection
 * 				final int returnCode = dialog.open();
 * 				if(log.isDebugEnabled()) {
 * 					log.debug(&quot;returnval=&quot; + returnCode);
 * 				}
 * 				eo.set(returnCode);
 * 			}
 * 		});
 * 		try {
 * 			final int returnval = eo.get();
 * 			if(returnval == 1) {
 * 				return FileVisitResult.TERMINATE;
 * 			} else if(returnval == 0) {
 * 				try {
 * 					s.rename(file, attrs);
 * 				} catch(final IOException e) {
 * 					if(log.isErrorEnabled()) {
 * 						log.error(e.getLocalizedMessage(), e);
 * 					}
 * 					return FileVisitResult.TERMINATE;
 * 				}
 * 			}
 * 		} catch(final InterruptedException e1) {
 * 			if(log.isErrorEnabled()) {
 * 				log.error(e1.getLocalizedMessage(), e1);
 * 			}
 * 			return FileVisitResult.TERMINATE;
 * 		}
 * 	}
 * 	return FileVisitResult.CONTINUE;
 * }
 * </pre>
 *
 * </p>
 *
 * <p>
 * <b>Threading:</b> Fully thread safe.
 * </p>
 *
 * <p>
 * last reviewed: 2015-10-25
 * </p>
 *
 * @author <a href="mailto:alexander.kerner@silico-sciences.com">Alexander
 *         Kerner</a>
 *
 *
 * @param <T>
 *            type of object that is exchanged
 */
public class ThreadExchangeObject<T> {

	private final Object lock;
	private boolean released = false;
	private T t = null;

	public ThreadExchangeObject() {
		this(new Object());
	}

	public ThreadExchangeObject(final Object lock) {
		this.lock = lock;
	}

	/**
	 * Blocks until {@code t} becomes available.
	 *
	 * @return {@code t} or {@code null} if lock was released
	 * @see #releaseLock()
	 * @throws InterruptedException
	 */
	public T get() throws InterruptedException {

		synchronized(lock) {
			while(t == null && !released) {
				lock.wait();
			}
			return t;
		}
	}

	/**
	 * Does not block.
	 *
	 * @return {@code true} if {@code t} is not {@code null} or if block was
	 *         released; {@code false} otherwise
	 * @see #releaseLock()
	 */
	public boolean isAvailable() {

		// Must not block
		synchronized(lock) {
			return t != null || released;
		}
	}

	public void releaseLock() {

		synchronized(lock) {
			this.released = true;
			lock.notifyAll();
		}
	}

	/**
	 * Does not block.
	 */
	public void set(final T t) {

		if(t == null)
			throw new IllegalArgumentException();
		// Must not block
		synchronized(lock) {
			this.released = false;
			this.t = t;
			lock.notifyAll();
		}
	}
}
