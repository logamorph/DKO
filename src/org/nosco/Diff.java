package org.nosco;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * This class offers diff logic. &nbsp; Let's assume you have two databases with identical
 * schemas containing a {@code Question} table.  If you wanted to compare them (to perhaps sync
 * them), you could do the following:
 * <pre>  {@code Datasource dsA = [...]; // first database
 *  Datasource dsB = [...]; // second database
 *   
 *  Iterable<Question> allAs = Question.ALL.use(dsA).orderBy(Question.ID);
 *  Iterable<Question> allBs = Question.ALL.use(dsB).orderBy(Question.ID);
 *   
 *  Iterable<RowChange<Question>> changes = Diff.streamingDiff(allAs, allBs);
 *  for (RowChange<Question> change : changes) {
 *    // do something
 *  }}</pre>
 * 
 * Note: It's very important these are sorted in ascending order by their natural
 * ordering!  &nbsp; You will get nonsensical diffs otherwise.
 *  
 * @author Derek Anderson
 */
public class Diff {

	/**
	 * This will create a streaming diff of two input streams.  &nbsp; The diff is 
	 * calculated on the fly as you iterate over the returned object, avoiding having to 
	 * load either source input stream (or the resultant stream) into memory all at once.
	 * <p>
	 * Note: It's critically important that the two input lists are sorted by their
	 * natural ordering. &nbsp; This algorithm will not work otherwise.
	 * @param from a sorted {@code Iterable}
	 * @param to a sorted {@code Iterable}
	 * @return
	 */
	public static <T extends Table> Iterable<RowChange<T>> streamingDiff(
			final Iterable<T> from, final Iterable<T> to) {
		return new Iterable<RowChange<T>>() {
			@Override
			public Iterator<RowChange<T>> iterator() {
				return new ChangeIterator<T>(from.iterator(), to.iterator());
			}
		};
	}
	
	/**
	 * This is identical to {@code streamingDiff(from, to)}, but the resultant {@code Iterable}
	 * actualized into a {@code List} for you. &nbsp; Of note:
	 * <ul>
	 * <li>The incoming streams are still processed as a stream, meaning they're never loaded
	 * into memory in their entirety.
	 * <li>This will block until the entire diff is created.
	 * <li>In a worst-case scenario, the resultant {@code List} will be the size of both inputs
	 * combined.
	 * </ul>
	 * @param from a sorted Iterable
	 * @param to a sorted Iterable
	 * @return
	 */
	public static <T extends Table> List<RowChange<T>> streamingDiffActualized(
			final Iterable<T> from, final Iterable<T> to) {
		List<RowChange<T>> ret = new ArrayList<RowChange<T>>();
		for (RowChange<T> v : streamingDiff(from, to)) ret.add(v);
		return ret;
	}	
	

	private static enum CHANGE_TYPE {
		ADD, UPDATE, DELETE
	}

	private static class ChangeIterator<T extends Table> implements
			Iterator<RowChange<T>> {
		private final Iterator<T> A;
		private final Iterator<T> B;
		private T a = null;
		private T b = null;
		RowChange<T> next = null;
		Map<Class<?>, Set<Field<?>>> fieldsForClass = new HashMap<Class<?>, Set<Field<?>>>();

		private ChangeIterator(Iterator<T> a, Iterator<T> b) {
			this.A = a;
			this.B = b;
		}

		@Override
		public boolean hasNext() {
			if (next != null)
				return true;
			while (true) {
				if (a == null && A.hasNext())
					a = A.next();
				if (b == null && B.hasNext())
					b = B.next();

				if (a == null && b == null)
					return false;
				if (a == null && b != null) {
					next = new RowChange<T>(CHANGE_TYPE.ADD, b, null);
					b = null;
					return true;
				}
				if (a != null && b == null) {
					next = new RowChange<T>(CHANGE_TYPE.DELETE, a, null);
					a = null;
					return true;
				}
				@SuppressWarnings("unchecked")
				int c = ((Comparable<T>)a).compareTo(b);
				if (c < 0) {
					next = new RowChange<T>(CHANGE_TYPE.DELETE, a, null);
					a = null;
					return true;
				} else if (c > 0) {
					next = new RowChange<T>(CHANGE_TYPE.ADD, b, null);
					b = null;
					return true;
				} else {
					Set<Field<?>> fields = null;
					if (a.getClass().equals(b.getClass())) {
						fields = fieldsForClass.get(a.getClass());
						if (fields == null) {
							fields = new LinkedHashSet<Field<?>>();
							for (Field<?> field : a.FIELDS()) {
								fields.add(field);
							}
							fieldsForClass.put(a.getClass(), fields);
						}

					} else {
						fields = new LinkedHashSet<Field<?>>();
						for (Field<?> field : a.FIELDS()) {
							fields.add(field);
						}
						for (Field<?> field : b.FIELDS()) {
							fields.add(field);
						}
					}
					Collection<FieldChange<T, ?>> diffs = new ArrayList<FieldChange<T, ?>>();
					for (Field<?> field : fields) {
						Object av = a.get(field);
						Object bv = b.get(field);
						if (av == null ? bv != null : !av.equals(bv)) {
							diffs.add(new FieldChange<T, Object>(
									(Field<Object>) field, av, bv));
						}
					}
					if (diffs.size() > 0) {
						next = new RowChange<T>(CHANGE_TYPE.UPDATE, a, diffs);
						a = null;
						b = null;
						return true;
					}
					a = null;
					b = null;
				}
			}
		}

		@Override
		public RowChange<T> next() {
			if (!hasNext())
				throw new NoSuchElementException();
			RowChange<T> tmp = next;
			next = null;
			return tmp;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	/**
	 * Represents a changed field between two versions of a row.
	 * 
	 * @author Derek Anderson
	 *
	 * @param <T> the type of the row that changed
	 * @param <S> the type of the field that changed
	 */
	public static class FieldChange<T extends Table, S> {
		/**
		 * The field that changed.
		 */
		public final Field<S> field;
		/**
		 * The value it changed from.
		 */
		public final S version1;
		/**
		 * The value it changed to.
		 */
		public final S version2;

		private FieldChange(Field<S> field, S v1, S v2) {
			this.field = field;
			this.version1 = v1;
			this.version2 = v2;
		}
	}

	/**
	 * Represents a change in a row. &nbsp; (either an ADD, UPDATE, or DELETE)
	 * 
	 * @author Derek Anderson
	 *
	 * @param <T> the type of the row that changed
	 */
	public static class RowChange<T extends Table> {

		private final CHANGE_TYPE type;
		private final T o;
		private final Collection<FieldChange<T, ?>> updates;

		private RowChange(CHANGE_TYPE type, T o,
				Collection<FieldChange<T, ?>> updates) {
			this.type = type;
			this.o = o;
			this.updates = updates;
		}

		/**
		 * The object representing the row that changed
		 * @return the object representing the row that changed
		 */
		public T getObject() {
			return o;
		}

		/**
		 * A collection of fields and values that changed (if it's an UPDATE)
		 * @return a collection of fields and values that changed (if it's an UPDATE)
		 */
		public Collection<FieldChange<T, ?>> getChanges() {
			return updates;
		}

		/**
		 * If row change was an UPDATE
		 * @return true if row change was an UPDATE
		 */
		public boolean isUpdate() {
			return type == CHANGE_TYPE.UPDATE;
		}

		/**
		 * If row change was an ADD
		 * @return true if row change was an ADD
		 */
		public boolean isAdd() {
			return type == CHANGE_TYPE.ADD;
		}

		/**
		 * If row change was a DELETE
		 * @return true if row change was a DELETE
		 */
		public boolean isDelete() {
			return type == CHANGE_TYPE.DELETE;
		}

		@Override
		public String toString() {
			return "[Change " + type + ", " + o + ", " + updates + "]";
		}

	}

}