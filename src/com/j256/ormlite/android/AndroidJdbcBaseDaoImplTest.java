package com.j256.ormlite.android;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

import android.test.AndroidTestCase;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RawResults;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.db.SqliteAndroidDatabaseType;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;

@SuppressWarnings("deprecation")
public class AndroidJdbcBaseDaoImplTest extends AndroidTestCase {

	private final boolean CLOSE_IS_NOOP = true;
	private final boolean UPDATE_ROWS_RETURNS_ONE = true;

	private DatabaseType databaseType = new SqliteAndroidDatabaseType();
	private ConnectionSource connectionSource;
	private OrmDatabaseHelper helper;

	private Set<DatabaseTableConfig<?>> dropClassSet = new HashSet<DatabaseTableConfig<?>>();

	protected boolean isTableExistsWorks() {
		return false;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		helper = new OrmDatabaseHelper(getContext());
		connectionSource = helper.getConnectionSource();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		closeConnectionSource();
		if (helper != null) {
			helper.close();
		}
	}

	protected void closeConnection() throws Exception {
		if (connectionSource != null) {
			for (DatabaseTableConfig<?> tableConfig : dropClassSet) {
				dropTable(tableConfig, true);
			}
			connectionSource.close();
			connectionSource = null;
		}
		databaseType = null;
	}

	private <T, ID> Dao<T, ID> createDao(Class<T> clazz, boolean createTable) throws Exception {
		return createDao(DatabaseTableConfig.fromClass(connectionSource, clazz), createTable);
	}

	private <T, ID> Dao<T, ID> createDao(DatabaseTableConfig<T> tableConfig, boolean createTable) throws Exception {
		BaseDaoImpl<T, ID> dao = new BaseDaoImpl<T, ID>(connectionSource, tableConfig) {
		};
		return configDao(tableConfig, createTable, dao);
	}

	private <T> void createTable(Class<T> clazz, boolean dropAtEnd) throws Exception {
		DatabaseTableConfig<T> tableConfig = DatabaseTableConfig.fromClass(connectionSource, clazz);
		createTable(tableConfig, dropAtEnd);
	}

	private <T> void createTable(DatabaseTableConfig<T> tableConfig, boolean dropAtEnd) throws Exception {
		try {
			// first we drop it in case it existed before
			dropTable(tableConfig, true);
		} catch (SQLException ignored) {
			// ignore any errors about missing tables
		}
		TableUtils.createTable(connectionSource, tableConfig);
		if (dropAtEnd) {
			dropClassSet.add(tableConfig);
		}
	}

	private <T> void dropTable(DatabaseTableConfig<T> tableConfig, boolean ignoreErrors) throws Exception {
		// drop the table and ignore any errors along the way
		TableUtils.dropTable(connectionSource, tableConfig, ignoreErrors);
	}

	private <T, ID> Dao<T, ID> configDao(DatabaseTableConfig<T> tableConfig, boolean createTable, BaseDaoImpl<T, ID> dao)
			throws Exception {
		if (connectionSource == null) {
			throw new SQLException("no connection source configured");
		}
		dao.setConnectionSource(connectionSource);
		if (createTable) {
			createTable(tableConfig, true);
		}
		dao.initialize();
		return dao;
	}

	private void closeConnectionSource() throws Exception {
		if (connectionSource != null) {
			for (DatabaseTableConfig<?> tableConfig : dropClassSet) {
				dropTable(tableConfig, true);
			}
			connectionSource.close();
			connectionSource = null;
		}
		databaseType = null;
	}

	/*
	 * ==============================================================================================================
	 * Insert the JdbcBaseDaoImplTest below
	 * ==============================================================================================================
	 */

	private final static String DEFAULT_VALUE_STRING = "1314199";
	private final static int DEFAULT_VALUE = Integer.parseInt(DEFAULT_VALUE_STRING);
	private final static int ALL_TYPES_STRING_WIDTH = 4;
	private final static String FOO_TABLE_NAME = "footable";
	private final static String ENUM_TABLE_NAME = "enumtable";

	private final static String NULL_BOOLEAN_TABLE_NAME = "nullbooltable";
	private final static String NULL_INT_TABLE_NAME = "nullinttable";

	private final static String DEFAULT_BOOLEAN_VALUE = "true";
	private final static String DEFAULT_STRING_VALUE = "foo";
	// this can't have non-zero milliseconds
	private static DateFormat defaultDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
	private final static String DEFAULT_DATE_VALUE = "2010-07-16 01:31:17.000000";
	private final static String DEFAULT_DATE_LONG_VALUE = "1282768620000";
	private final static String DEFAULT_DATE_STRING_FORMAT = "MM/dd/yyyy HH-mm-ss-SSSSSS";
	private static DateFormat defaultDateStringFormat = new SimpleDateFormat(DEFAULT_DATE_STRING_FORMAT);
	private final static String DEFAULT_DATE_STRING_VALUE = "07/16/2010 01-31-17-000000";
	private final static String DEFAULT_BYTE_VALUE = "1";
	private final static String DEFAULT_SHORT_VALUE = "2";
	private final static String DEFAULT_INT_VALUE = "3";
	private final static String DEFAULT_LONG_VALUE = "4";
	private final static String DEFAULT_FLOAT_VALUE = "5";
	private final static String DEFAULT_DOUBLE_VALUE = "6";
	private final static String DEFAULT_ENUM_VALUE = "FIRST";
	private final static String DEFAULT_ENUM_NUMBER_VALUE = "1";

	public void testCreateDaoStatic() throws Exception {
		if (connectionSource == null) {
			return;
		}
		createTable(Foo.class, true);
		Dao<Foo, Integer> fooDao = BaseDaoImpl.createDao(connectionSource, Foo.class);
		String stuff = "stuff";
		Foo foo = new Foo();
		foo.stuff = stuff;
		assertEquals(1, fooDao.create(foo));

		// now we query for foo from the database to make sure it was persisted right
		Foo foo2 = fooDao.queryForId(foo.id);
		assertNotNull(foo2);
		assertEquals(foo.id, foo2.id);
		assertEquals(stuff, foo2.stuff);
	}

	public void testCreateUpdateDelete() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		String s1 = "stuff";
		Foo foo1 = new Foo();
		foo1.stuff = s1;
		assertEquals(0, foo1.id);

		// persist foo to db through the dao and sends the id on foo because it was auto-generated by the db
		assertEquals(1, fooDao.create(foo1));
		assertTrue(foo1.id != 0);
		assertEquals(s1, foo1.stuff);

		// now we query for foo from the database to make sure it was persisted right
		Foo foo2 = fooDao.queryForId(foo1.id);
		assertNotNull(foo2);
		assertEquals(foo1.id, foo2.id);
		assertEquals(s1, foo2.stuff);

		String s2 = "stuff2";
		foo2.stuff = s2;

		// now we update 1 row in a the database after changing foo
		assertEquals(1, fooDao.update(foo2));

		// now we get it from the db again to make sure it was updated correctly
		Foo foo3 = fooDao.queryForId(foo1.id);
		assertEquals(s2, foo3.stuff);
		assertEquals(1, fooDao.delete(foo2));

		assertNull(fooDao.queryForId(foo1.id));
	}

	public void testDoubleCreate() throws Exception {
		Dao<DoubleCreate, Object> doubleDao = createDao(DoubleCreate.class, true);
		int id = 313413123;
		DoubleCreate foo = new DoubleCreate();
		foo.id = id;
		assertEquals(1, doubleDao.create(foo));
		try {
			doubleDao.create(foo);
			fail("expected exception");
		} catch (SQLException e) {
			// expected
		}
	}

	public void testIterateRemove() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		List<Foo> acctList = fooDao.queryForAll();
		int initialSize = acctList.size();

		Foo foo1 = new Foo();
		foo1.stuff = "s1";
		Foo foo2 = new Foo();
		foo2.stuff = "s2";
		Foo foo3 = new Foo();
		foo3.stuff = "s3";
		fooDao.create(foo1);
		fooDao.create(foo2);
		fooDao.create(foo3);

		assertTrue(foo1.id != foo2.id);
		assertTrue(foo1.id != foo3.id);
		assertTrue(foo2.id != foo3.id);

		assertEquals(foo1, fooDao.queryForId(foo1.id));
		assertEquals(foo2, fooDao.queryForId(foo2.id));
		assertEquals(foo3, fooDao.queryForId(foo3.id));

		acctList = fooDao.queryForAll();
		assertEquals(initialSize + 3, acctList.size());
		assertEquals(foo1, acctList.get(acctList.size() - 3));
		assertEquals(foo2, acctList.get(acctList.size() - 2));
		assertEquals(foo3, acctList.get(acctList.size() - 1));
		int acctC = 0;
		Iterator<Foo> iterator = fooDao.iterator();
		while (iterator.hasNext()) {
			Foo foo = iterator.next();
			if (acctC == acctList.size() - 3) {
				assertEquals(foo1, foo);
			} else if (acctC == acctList.size() - 2) {
				iterator.remove();
				assertEquals(foo2, foo);
			} else if (acctC == acctList.size() - 1) {
				assertEquals(foo3, foo);
			}
			acctC++;
		}
		assertEquals(initialSize + 3, acctC);
	}

	public void testGeneratedField() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		foo1.stuff = "s1";
		assertEquals(0, foo1.id);
		assertEquals(1, fooDao.create(foo1));
		assertTrue(foo1.id != 0);
	}

	public void testGeneratedIdNotNullField() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		foo1.stuff = "s1";
		assertEquals(0, foo1.id);
		assertEquals(1, fooDao.create(foo1));
		assertTrue(foo1.id != 0);
	}

	public void testObjectToString() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		String stuff = "foo123231";
		Foo foo1 = new Foo();
		foo1.stuff = stuff;
		String objStr = fooDao.objectToString(foo1);
		assertTrue(objStr.contains(Integer.toString(foo1.id)));
		assertTrue(objStr.contains(stuff));
	}

	public void testCreateNull() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		assertEquals(0, fooDao.create(null));
	}

	public void testUpdateNull() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		assertEquals(0, fooDao.update((Foo) null));
	}

	public void testUpdateIdNull() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		assertEquals(0, fooDao.updateId(null, null));
	}

	public void testDeleteNull() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		assertEquals(0, fooDao.delete((Foo) null));
	}

	public void testCloseInIterator() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		foo1.stuff = "s1";
		fooDao.create(foo1);
		Iterator<Foo> iterator = fooDao.iterator();
		try {
			while (iterator.hasNext()) {
				iterator.next();
				closeConnectionSource();
			}
			if (!CLOSE_IS_NOOP) {
				fail("expected exception");
			}
		} catch (IllegalStateException e) {
			// expected
		}
	}

	public void testCloseIteratorFirst() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		foo1.stuff = "s1";
		fooDao.create(foo1);
		closeConnectionSource();
		try {
			fooDao.iterator();
			fail("expected exception");
		} catch (IllegalStateException e) {
			// expected
		}
	}

	public void testCloseIteratorBeforeNext() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		foo1.stuff = "s1";
		fooDao.create(foo1);
		Iterator<Foo> iterator = fooDao.iterator();
		try {
			while (iterator.hasNext()) {
				closeConnectionSource();
				iterator.next();
			}
			if (!CLOSE_IS_NOOP) {
				fail("expected exception");
			}
		} catch (IllegalStateException e) {
			// expected
		}
	}

	public void testCloseIteratorBeforeRemove() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		foo1.stuff = "s1";
		fooDao.create(foo1);
		Iterator<Foo> iterator = fooDao.iterator();
		try {
			while (iterator.hasNext()) {
				iterator.next();
				closeConnectionSource();
				iterator.remove();
			}
			fail("expected exception");
		} catch (Exception e) {
			// expected
		}
	}

	public void testNoNextBeforeRemove() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		foo1.stuff = "s1";
		fooDao.create(foo1);
		Iterator<Foo> iterator = fooDao.iterator();
		try {
			iterator.remove();
			fail("expected exception");
		} catch (IllegalStateException e) {
			// expected
		}
	}

	public void testIteratePageSize() throws Exception {
		final Dao<Foo, Integer> fooDao = createDao(Foo.class, true);

		int numItems = 1000;
		fooDao.callBatchTasks(new InsertCallable(numItems, fooDao));

		// now delete them with the iterator to test page-size
		Iterator<Foo> iterator = fooDao.iterator();
		while (iterator.hasNext()) {
			iterator.next();
			iterator.remove();
		}
	}

	public void testIteratorPreparedQuery() throws Exception {
		final Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		// do an insert of bunch of items
		final int numItems = 100;
		fooDao.callBatchTasks(new InsertCallable(numItems, fooDao));

		int lastX = 10;
		PreparedQuery<Foo> preparedQuery =
				fooDao.queryBuilder().where().ge(Foo.VAL_FIELD_NAME, numItems - lastX).prepare();

		// now delete them with the iterator to test page-size
		Iterator<Foo> iterator = fooDao.iterator(preparedQuery);
		int itemC = 0;
		while (iterator.hasNext()) {
			Foo foo = iterator.next();
			System.out.println("Foo = " + foo.val);
			itemC++;
		}
		assertEquals(lastX, itemC);
	}

	private class InsertCallable implements Callable<Void> {
		private int numItems;
		private Dao<Foo, Integer> fooDao;
		public InsertCallable(int numItems, Dao<Foo, Integer> fooDao) {
			this.numItems = numItems;
			this.fooDao = fooDao;
		}
		public Void call() throws Exception {
			for (int i = 0; i < numItems; i++) {
				Foo foo = new Foo();
				foo.val = i;
				assertEquals(1, fooDao.create(foo));
			}
			return null;
		}
	}

	public void testDeleteObjects() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		List<Foo> fooList = new ArrayList<Foo>();
		for (int i = 0; i < 100; i++) {
			Foo foo = new Foo();
			foo.stuff = Integer.toString(i);
			assertEquals(1, fooDao.create(foo));
			fooList.add(foo);
		}

		int deleted = fooDao.delete(fooList);
		if (UPDATE_ROWS_RETURNS_ONE) {
			assertEquals(1, deleted);
		} else {
			assertEquals(fooList.size(), deleted);
		}
		assertEquals(0, fooDao.queryForAll().size());
	}

	public void testDeleteObjectsNone() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		List<Foo> fooList = new ArrayList<Foo>();
		assertEquals(fooList.size(), fooDao.delete(fooList));
		assertEquals(0, fooDao.queryForAll().size());
	}

	public void testDeleteIds() throws Exception {
		final Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		final List<Integer> fooIdList = new ArrayList<Integer>();
		fooDao.callBatchTasks(new Callable<Void>() {
			public Void call() throws Exception {
				for (int i = 0; i < 100; i++) {
					Foo foo = new Foo();
					assertEquals(1, fooDao.create(foo));
					fooIdList.add(foo.id);
				}
				return null;
			}
		});

		int deleted = fooDao.deleteIds(fooIdList);
		if (UPDATE_ROWS_RETURNS_ONE) {
			assertEquals(1, deleted);
		} else {
			assertEquals(fooIdList.size(), deleted);
		}
		assertEquals(0, fooDao.queryForAll().size());
	}

	public void testDeleteIdsNone() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		List<Integer> fooIdList = new ArrayList<Integer>();
		assertEquals(fooIdList.size(), fooDao.deleteIds(fooIdList));
		assertEquals(0, fooDao.queryForAll().size());
	}

	public void testDeletePreparedStmtIn() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		List<Integer> fooIdList = new ArrayList<Integer>();
		for (int i = 0; i < 100; i++) {
			Foo foo = new Foo();
			assertEquals(1, fooDao.create(foo));
			fooIdList.add(foo.id);
		}

		DeleteBuilder<Foo, Integer> stmtBuilder = fooDao.deleteBuilder();
		stmtBuilder.where().in(Foo.ID_FIELD_NAME, fooIdList);

		int deleted = fooDao.delete(stmtBuilder.prepare());
		if (UPDATE_ROWS_RETURNS_ONE) {
			assertEquals(1, deleted);
		} else {
			assertEquals(fooIdList.size(), deleted);
		}
		assertEquals(0, fooDao.queryForAll().size());
	}

	public void testDeleteAllPreparedStmt() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		int fooN = 100;
		for (int i = 0; i < fooN; i++) {
			Foo foo = new Foo();
			assertEquals(1, fooDao.create(foo));
		}

		DeleteBuilder<Foo, Integer> stmtBuilder = fooDao.deleteBuilder();

		int deleted = fooDao.delete(stmtBuilder.prepare());
		if (UPDATE_ROWS_RETURNS_ONE) {
			assertEquals(1, deleted);
		} else {
			assertEquals(fooN, deleted);
		}
		assertEquals(0, fooDao.queryForAll().size());
	}

	public void testHasNextAfterDone() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		Iterator<Foo> iterator = fooDao.iterator();
		while (iterator.hasNext()) {
		}
		assertFalse(iterator.hasNext());
	}

	public void testNextWithoutHasNext() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		Iterator<Foo> iterator = fooDao.iterator();
		try {
			iterator.next();
			fail("expected exception");
		} catch (Exception e) {
			// expected
		}
	}

	public void testRemoveAfterDone() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		Iterator<Foo> iterator = fooDao.iterator();
		assertFalse(iterator.hasNext());
		try {
			iterator.remove();
			fail("expected exception");
		} catch (IllegalStateException e) {
			// expected
		}
	}

	public void testIteratorNoResults() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		Iterator<Foo> iterator = fooDao.iterator();
		assertFalse(iterator.hasNext());
		assertNull(iterator.next());
	}

	public void testCreateNoId() throws Exception {
		Dao<NoId, Object> whereDao = createDao(NoId.class, true);
		NoId noId = new NoId();
		assertEquals(0, whereDao.queryForAll().size());
		// this should work even though there is no id
		whereDao.create(noId);
		assertEquals(1, whereDao.queryForAll().size());
	}

	public void testJustIdCreateQueryDelete() throws Exception {
		Dao<JustId, Object> justIdDao = createDao(JustId.class, true);
		String id = "just-id";
		JustId justId = new JustId();
		justId.id = id;
		assertEquals(1, justIdDao.create(justId));
		JustId justId2 = justIdDao.queryForId(id);
		assertNotNull(justId2);
		assertEquals(id, justId2.id);
		assertEquals(1, justIdDao.delete(justId));
		assertNull(justIdDao.queryForId(id));
		// update should fail during construction
	}

	public void testJustIdUpdateId() throws Exception {
		Dao<JustId, Object> justIdDao = createDao(JustId.class, true);
		String id = "just-id-update-1";
		JustId justId = new JustId();
		justId.id = id;
		assertEquals(1, justIdDao.create(justId));
		JustId justId2 = justIdDao.queryForId(id);
		assertNotNull(justId2);
		assertEquals(id, justId2.id);
		String id2 = "just-id-update-2";
		// change the id
		assertEquals(1, justIdDao.updateId(justId2, id2));
		assertNull(justIdDao.queryForId(id));
		JustId justId3 = justIdDao.queryForId(id2);
		assertNotNull(justId3);
		assertEquals(id2, justId3.id);
		assertEquals(1, justIdDao.delete(justId3));
		assertNull(justIdDao.queryForId(id));
		assertNull(justIdDao.queryForId(id2));
	}

	public void testJustIdRefresh() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		String stuff1 = "just-id-refresh-1";
		Foo foo1 = new Foo();
		foo1.stuff = stuff1;
		assertEquals(1, fooDao.create(foo1));
		int id = foo1.id;
		Foo foo2 = fooDao.queryForId(id);
		assertNotNull(foo2);
		assertEquals(id, foo2.id);
		assertEquals(stuff1, foo2.stuff);
		String stuff2 = "just-id-refresh-2";
		foo2.stuff = stuff2;

		// change the id in the db
		assertEquals(1, fooDao.update(foo2));
		Foo foo3 = fooDao.queryForId(id);
		assertNotNull(foo3);
		assertEquals(id, foo3.id);
		assertEquals(stuff2, foo3.stuff);

		assertEquals(stuff1, foo1.stuff);
		assertEquals(1, fooDao.refresh(foo1));
		assertEquals(stuff2, foo1.stuff);
	}

	public void testSpringConstruction() throws Exception {
		if (connectionSource == null) {
			return;
		}
		createTable(Foo.class, true);
		BaseDaoImpl<Foo, Integer> fooDao = new BaseDaoImpl<Foo, Integer>(Foo.class) {
		};
		try {
			fooDao.create(new Foo());
			fail("expected exception");
		} catch (IllegalStateException e) {
			// expected
		}
		fooDao.setConnectionSource(connectionSource);
		fooDao.initialize();
		Foo foo = new Foo();
		assertEquals(1, fooDao.create(foo));
		assertEquals(1, fooDao.delete(foo));
	}

	public void testForeignCreation() throws Exception {
		Dao<ForeignWrapper, Integer> wrapperDao = createDao(ForeignWrapper.class, true);
		Dao<AllTypes, Integer> foreignDao = createDao(AllTypes.class, true);

		AllTypes foreign = new AllTypes();
		String stuff1 = "stuff1";
		foreign.stringField = stuff1;
		// this sets the foreign id
		assertEquals(1, foreignDao.create(foreign));

		ForeignWrapper wrapper = new ForeignWrapper();
		wrapper.foreign = foreign;
		// this sets the wrapper id
		assertEquals(1, wrapperDao.create(wrapper));

		ForeignWrapper wrapper2 = wrapperDao.queryForId(wrapper.id);
		assertEquals(wrapper.id, wrapper2.id);
		assertEquals(wrapper.foreign.id, wrapper2.foreign.id);
		assertTrue(wrapperDao.objectsEqual(wrapper, wrapper2));
		// this won't be true because wrapper2.foreign is a shell
		assertFalse(foreignDao.objectsEqual(foreign, wrapper2.foreign));
		assertNull(wrapper2.foreign.stringField);
		assertEquals(1, foreignDao.refresh(wrapper2.foreign));
		// now it should be true
		assertTrue(foreignDao.objectsEqual(foreign, wrapper2.foreign));
		assertEquals(stuff1, wrapper2.foreign.stringField);

		// create a new foreign
		foreign = new AllTypes();
		String stuff2 = "stuff2";
		foreign.stringField = stuff2;
		// this sets the foreign id
		assertEquals(1, foreignDao.create(foreign));

		// change the foreign object
		wrapper.foreign = foreign;
		// update it
		assertEquals(1, wrapperDao.update(wrapper));

		wrapper2 = wrapperDao.queryForId(wrapper.id);
		assertEquals(wrapper.id, wrapper2.id);
		assertEquals(wrapper.foreign.id, wrapper2.foreign.id);
		assertTrue(wrapperDao.objectsEqual(wrapper, wrapper2));
		// this won't be true because wrapper2.foreign is a shell
		assertFalse(foreignDao.objectsEqual(foreign, wrapper2.foreign));
		assertNull(wrapper2.foreign.stringField);
		assertEquals(1, foreignDao.refresh(wrapper2.foreign));
		// now it should be true
		assertTrue(foreignDao.objectsEqual(foreign, wrapper2.foreign));
		assertEquals(stuff2, wrapper2.foreign.stringField);
	}

	public void testForeignRefreshNoChange() throws Exception {
		Dao<ForeignWrapper, Integer> wrapperDao = createDao(ForeignWrapper.class, true);
		Dao<AllTypes, Integer> foreignDao = createDao(AllTypes.class, true);

		AllTypes foreign = new AllTypes();
		String stuff1 = "stuff1";
		foreign.stringField = stuff1;
		// this sets the foreign id
		assertEquals(1, foreignDao.create(foreign));

		ForeignWrapper wrapper = new ForeignWrapper();
		wrapper.foreign = foreign;
		// this sets the wrapper id
		assertEquals(1, wrapperDao.create(wrapper));

		ForeignWrapper wrapper2 = wrapperDao.queryForId(wrapper.id);
		assertEquals(1, foreignDao.refresh(wrapper2.foreign));
		AllTypes foreign2 = wrapper2.foreign;
		assertEquals(stuff1, foreign2.stringField);

		assertEquals(1, wrapperDao.refresh(wrapper2));
		assertSame(foreign2, wrapper2.foreign);
		assertEquals(stuff1, wrapper2.foreign.stringField);

		// now, in the background, we change the foreign
		ForeignWrapper wrapper3 = wrapperDao.queryForId(wrapper.id);
		AllTypes foreign3 = new AllTypes();
		String stuff3 = "stuff3";
		foreign3.stringField = stuff3;
		assertEquals(1, foreignDao.create(foreign3));
		wrapper3.foreign = foreign3;
		assertEquals(1, wrapperDao.update(wrapper3));

		assertEquals(1, wrapperDao.refresh(wrapper2));
		// now all of a sudden wrapper2 should not have the same foreign field
		assertNotSame(foreign2, wrapper2.foreign);
		assertNull(wrapper2.foreign.stringField);
	}

	public void testMultipleForeignWrapper() throws Exception {
		Dao<MultipleForeignWrapper, Integer> multipleWrapperDao = createDao(MultipleForeignWrapper.class, true);
		Dao<ForeignWrapper, Integer> wrapperDao = createDao(ForeignWrapper.class, true);
		Dao<AllTypes, Integer> foreignDao = createDao(AllTypes.class, true);

		AllTypes foreign = new AllTypes();
		String stuff1 = "stuff1";
		foreign.stringField = stuff1;
		// this sets the foreign id
		assertEquals(1, foreignDao.create(foreign));

		ForeignWrapper wrapper = new ForeignWrapper();
		wrapper.foreign = foreign;
		// this sets the wrapper id
		assertEquals(1, wrapperDao.create(wrapper));

		MultipleForeignWrapper multiWrapper = new MultipleForeignWrapper();
		multiWrapper.foreign = foreign;
		multiWrapper.foreignWrapper = wrapper;
		// this sets the wrapper id
		assertEquals(1, multipleWrapperDao.create(multiWrapper));

		MultipleForeignWrapper multiWrapper2 = multipleWrapperDao.queryForId(multiWrapper.id);
		assertEquals(foreign.id, multiWrapper2.foreign.id);
		assertNull(multiWrapper2.foreign.stringField);
		assertEquals(wrapper.id, multiWrapper2.foreignWrapper.id);
		assertNull(multiWrapper2.foreignWrapper.foreign);

		assertEquals(1, foreignDao.refresh(multiWrapper2.foreign));
		assertEquals(stuff1, multiWrapper2.foreign.stringField);
		assertNull(multiWrapper2.foreignWrapper.foreign);

		assertEquals(1, wrapperDao.refresh(multiWrapper2.foreignWrapper));
		assertEquals(foreign.id, multiWrapper2.foreignWrapper.foreign.id);
		assertNull(multiWrapper2.foreignWrapper.foreign.stringField);

		assertEquals(1, foreignDao.refresh(multiWrapper2.foreignWrapper.foreign));
		assertEquals(stuff1, multiWrapper2.foreignWrapper.foreign.stringField);
	}

	public void testRefreshNull() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		// this should be a noop
		assertEquals(0, fooDao.refresh(null));
	}

	public void testGetSet() throws Exception {
		Dao<GetSet, Integer> getSetDao = createDao(GetSet.class, true);
		GetSet getSet = new GetSet();
		String stuff = "ewfewfewfew343u42f";
		getSet.setStuff(stuff);
		assertEquals(1, getSetDao.create(getSet));
		GetSet getSet2 = getSetDao.queryForId(getSet.id);
		assertEquals(stuff, getSet2.stuff);
	}

	public void testQueryForFirst() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);

		String stuff = "ewf4334234u42f";

		QueryBuilder<Foo, Integer> qb = fooDao.queryBuilder();
		qb.where().eq(Foo.STUFF_FIELD_NAME, stuff);

		assertNull(fooDao.queryForFirst(qb.prepare()));

		Foo foo1 = new Foo();
		foo1.stuff = stuff;
		assertEquals(1, fooDao.create(foo1));

		// should still get foo1
		Foo foo2 = fooDao.queryForFirst(qb.prepare());
		assertEquals(foo1.id, foo2.id);
		assertEquals(stuff, foo2.stuff);

		// create another with same stuff
		Foo foo3 = new Foo();
		String stuff2 = "ewf43342wefwffwefwe34u42f";
		foo3.stuff = stuff2;
		assertEquals(1, fooDao.create(foo3));

		foo2 = fooDao.queryForFirst(qb.prepare());
		assertEquals(foo1.id, foo2.id);
		assertEquals(stuff, foo2.stuff);
	}

	public void testFieldConfig() throws Exception {
		List<DatabaseFieldConfig> fieldConfigs = new ArrayList<DatabaseFieldConfig>();
		fieldConfigs.add(new DatabaseFieldConfig("id", "id2", DataType.UNKNOWN, null, 0, false, false, true, null,
				false, null, false, null, false, null, false, null, null, false));
		fieldConfigs.add(new DatabaseFieldConfig("stuff", "stuffy", DataType.UNKNOWN, null, 0, false, false, false,
				null, false, null, false, null, false, null, false, null, null, false));
		DatabaseTableConfig<NoAnno> tableConfig = new DatabaseTableConfig<NoAnno>(NoAnno.class, "noanno", fieldConfigs);
		Dao<NoAnno, Integer> noAnnotaionDao = createDao(tableConfig, true);
		NoAnno noa = new NoAnno();
		String stuff = "qpoqwpjoqwp12";
		noa.stuff = stuff;
		assertEquals(1, noAnnotaionDao.create(noa));
		NoAnno noa2 = noAnnotaionDao.queryForId(noa.id);
		assertEquals(noa.id, noa2.id);
		assertEquals(stuff, noa2.stuff);
	}

	public void testFieldConfigForeign() throws Exception {
		List<DatabaseFieldConfig> noAnnotationsFieldConfigs = new ArrayList<DatabaseFieldConfig>();
		noAnnotationsFieldConfigs.add(new DatabaseFieldConfig("id", "idthingie", DataType.UNKNOWN, null, 0, false,
				false, true, null, false, null, false, null, false, null, false, null, null, false));
		noAnnotationsFieldConfigs.add(new DatabaseFieldConfig("stuff", "stuffy", DataType.UNKNOWN, null, 0, false,
				false, false, null, false, null, false, null, false, null, false, null, null, false));
		DatabaseTableConfig<NoAnno> noAnnotationsTableConfig =
				new DatabaseTableConfig<NoAnno>(NoAnno.class, noAnnotationsFieldConfigs);
		Dao<NoAnno, Integer> noAnnotationDao = createDao(noAnnotationsTableConfig, true);
		NoAnno noa = new NoAnno();
		String stuff = "qpoqwpjoqwp12";
		noa.stuff = stuff;
		assertEquals(1, noAnnotationDao.create(noa));
		assertNotNull(noAnnotationDao.queryForId(noa.id));

		List<DatabaseFieldConfig> noAnnotationsForiegnFieldConfigs = new ArrayList<DatabaseFieldConfig>();
		noAnnotationsForiegnFieldConfigs.add(new DatabaseFieldConfig("id", "anotherid", DataType.UNKNOWN, null, 0,
				false, false, true, null, false, null, false, null, false, null, false, null, null, false));
		noAnnotationsForiegnFieldConfigs.add(new DatabaseFieldConfig("foreign", "foreignThingie", DataType.UNKNOWN,
				null, 0, false, false, false, null, true, noAnnotationsTableConfig, false, null, false, null, false,
				null, null, false));
		DatabaseTableConfig<NoAnnoFor> noAnnotationsForiegnTableConfig =
				new DatabaseTableConfig<NoAnnoFor>(NoAnnoFor.class, noAnnotationsForiegnFieldConfigs);

		Dao<NoAnnoFor, Integer> noAnnotaionForeignDao = createDao(noAnnotationsForiegnTableConfig, true);
		NoAnnoFor noaf = new NoAnnoFor();
		noaf.foreign = noa;
		assertEquals(1, noAnnotaionForeignDao.create(noaf));

		NoAnnoFor noaf2 = noAnnotaionForeignDao.queryForId(noaf.id);
		assertNotNull(noaf2);
		assertEquals(noaf.id, noaf2.id);
		assertEquals(noa.id, noaf2.foreign.id);
		assertNull(noaf2.foreign.stuff);
		assertEquals(1, noAnnotationDao.refresh(noaf2.foreign));
		assertEquals(stuff, noaf2.foreign.stuff);
	}

	public void testGeneratedIdNotNull() throws Exception {
		// we saw an error with the not null before the generated id stuff under hsqldb
		Dao<GeneratedIdNotNull, Integer> dao = createDao(GeneratedIdNotNull.class, true);
		assertEquals(1, dao.create(new GeneratedIdNotNull()));
	}

	public void testBasicStuff() throws Exception {
		Dao<Basic, String> fooDao = createDao(Basic.class, true);

		String string = "s1";
		Basic foo1 = new Basic();
		foo1.id = string;
		assertEquals(1, fooDao.create(foo1));

		Basic foo2 = fooDao.queryForId(string);
		assertTrue(fooDao.objectsEqual(foo1, foo2));

		List<Basic> fooList = fooDao.queryForAll();
		assertEquals(1, fooList.size());
		assertTrue(fooDao.objectsEqual(foo1, fooList.get(0)));
		int i = 0;
		for (Basic foo3 : fooDao) {
			assertTrue(fooDao.objectsEqual(foo1, foo3));
			i++;
		}
		assertEquals(1, i);

		assertEquals(1, fooDao.delete(foo2));
		assertNull(fooDao.queryForId(string));
		fooList = fooDao.queryForAll();
		assertEquals(0, fooList.size());
		i = 0;
		for (Basic foo3 : fooDao) {
			assertTrue(fooDao.objectsEqual(foo1, foo3));
			i++;
		}
		assertEquals(0, i);
	}

	public void testMultiplePrimaryKey() throws Exception {
		Dao<Basic, String> fooDao = createDao(Basic.class, true);
		Basic foo1 = new Basic();
		foo1.id = "dup";
		assertEquals(1, fooDao.create(foo1));
		try {
			fooDao.create(foo1);
			fail("expected exception");
		} catch (SQLException e) {
			// expected
		}
	}

	public void testDefaultValue() throws Exception {
		Dao<DefaultValue, Object> defValDao = createDao(DefaultValue.class, true);
		DefaultValue defVal1 = new DefaultValue();
		assertEquals(1, defValDao.create(defVal1));
		List<DefaultValue> defValList = defValDao.queryForAll();
		assertEquals(1, defValList.size());
		DefaultValue defVal2 = defValList.get(0);
		assertEquals(DEFAULT_VALUE, (int) defVal2.intField);
	}

	public void testNotNull() throws Exception {
		Dao<NotNull, Object> defValDao = createDao(NotNull.class, true);
		NotNull notNull = new NotNull();
		try {
			defValDao.create(notNull);
			fail("expected exception");
		} catch (SQLException e) {
			// expected
		}
	}

	public void testNotNullOkay() throws Exception {
		Dao<NotNull, Object> defValDao = createDao(NotNull.class, true);
		NotNull notNull = new NotNull();
		notNull.notNull = "not null";
		assertEquals(1, defValDao.create(notNull));
	}

	public void testGeneratedId() throws Exception {
		Dao<GeneratedId, Object> genIdDao = createDao(GeneratedId.class, true);
		GeneratedId genId = new GeneratedId();
		assertEquals(0, genId.id);
		assertEquals(1, genIdDao.create(genId));
		long id = genId.id;
		assertEquals(1, id);
		GeneratedId genId2 = genIdDao.queryForId(id);
		assertNotNull(genId2);
		assertEquals(id, genId2.id);

		genId = new GeneratedId();
		assertEquals(0, genId.id);
		assertEquals(1, genIdDao.create(genId));
		id = genId.id;
		assertEquals(2, id);
		genId2 = genIdDao.queryForId(id);
		assertNotNull(genId2);
		assertEquals(id, genId2.id);
	}

	public void testAllTypes() throws Exception {
		Dao<AllTypes, Integer> allDao = createDao(AllTypes.class, true);
		AllTypes allTypes = new AllTypes();
		String stringVal = "some string";
		boolean boolVal = true;
		// we have to round this because the db may not be storing millis
		long millis = (System.currentTimeMillis() / 1000) * 1000;
		Date dateVal = new Date(millis);
		Date dateLongVal = new Date(millis);
		Date dateStringVal = new Date(millis);
		byte byteVal = 117;
		short shortVal = 15217;
		int intVal = 1023213;
		long longVal = 1231231231231L;
		float floatVal = 123.13F;
		double doubleVal = 1413312.1231233;
		OurEnum enumVal = OurEnum.FIRST;
		allTypes.stringField = stringVal;
		allTypes.booleanField = boolVal;
		allTypes.dateField = dateVal;
		allTypes.dateLongField = dateLongVal;
		allTypes.dateStringField = dateStringVal;
		allTypes.byteField = byteVal;
		allTypes.shortField = shortVal;
		allTypes.intField = intVal;
		allTypes.longField = longVal;
		allTypes.floatField = floatVal;
		allTypes.doubleField = doubleVal;
		allTypes.enumField = enumVal;
		allTypes.enumStringField = enumVal;
		allTypes.enumIntegerField = enumVal;
		SerialData obj = new SerialData();
		String key = "key";
		String value = "value";
		obj.addEntry(key, value);
		allTypes.serialField = obj;
		assertEquals(1, allDao.create(allTypes));
		List<AllTypes> allTypesList = allDao.queryForAll();
		assertEquals(1, allTypesList.size());
		assertTrue(allDao.objectsEqual(allTypes, allTypesList.get(0)));
		assertEquals(value, allTypesList.get(0).serialField.map.get(key));
		assertEquals(1, allDao.refresh(allTypes));
		// queries on all fields
		QueryBuilder<AllTypes, Integer> qb = allDao.queryBuilder();
		checkQueryResult(allDao, qb, allTypes, AllTypes.STRING_FIELD_NAME, stringVal, true);
		checkQueryResult(allDao, qb, allTypes, AllTypes.BOOLEAN_FIELD_NAME, boolVal, true);
		checkQueryResult(allDao, qb, allTypes, AllTypes.DATE_FIELD_NAME, dateVal, true);
		checkQueryResult(allDao, qb, allTypes, AllTypes.DATE_LONG_FIELD_NAME, dateLongVal, true);
		checkQueryResult(allDao, qb, allTypes, AllTypes.DATE_STRING_FIELD_NAME, dateStringVal, true);
		checkQueryResult(allDao, qb, allTypes, AllTypes.BYTE_FIELD_NAME, byteVal, true);
		checkQueryResult(allDao, qb, allTypes, AllTypes.SHORT_FIELD_NAME, shortVal, true);
		checkQueryResult(allDao, qb, allTypes, AllTypes.INT_FIELD_NAME, intVal, true);
		checkQueryResult(allDao, qb, allTypes, AllTypes.LONG_FIELD_NAME, longVal, true);
		// float tested below
		checkQueryResult(allDao, qb, allTypes, AllTypes.DOUBLE_FIELD_NAME, doubleVal, true);
		checkQueryResult(allDao, qb, allTypes, AllTypes.ENUM_FIELD_NAME, enumVal, true);
		checkQueryResult(allDao, qb, allTypes, AllTypes.ENUM_STRING_FIELD_NAME, enumVal, true);
		checkQueryResult(allDao, qb, allTypes, AllTypes.ENUM_INTEGER_FIELD_NAME, enumVal, true);
	}

	/**
	 * This is special because comparing floats may not work as expected.
	 */

	public void testAllTypesFloat() throws Exception {
		Dao<AllTypes, Integer> allDao = createDao(AllTypes.class, true);
		AllTypes allTypes = new AllTypes();
		float floatVal = 123.13F;
		float floatLowVal = floatVal * 0.9F;
		float floatHighVal = floatVal * 1.1F;
		allTypes.floatField = floatVal;
		assertEquals(1, allDao.create(allTypes));
		List<AllTypes> allTypesList = allDao.queryForAll();
		assertEquals(1, allTypesList.size());
		assertTrue(allDao.objectsEqual(allTypes, allTypesList.get(0)));
		assertEquals(1, allDao.refresh(allTypes));
		// queries on all fields
		QueryBuilder<AllTypes, Integer> qb = allDao.queryBuilder();

		// float comparisons are not exactly right so we switch to a low -> high query instead
		if (!checkQueryResult(allDao, qb, allTypes, AllTypes.FLOAT_FIELD_NAME, floatVal, false)) {
			qb.where().gt(AllTypes.FLOAT_FIELD_NAME, floatLowVal).and().lt(AllTypes.FLOAT_FIELD_NAME, floatHighVal);
			List<AllTypes> results = allDao.query(qb.prepare());
			assertEquals(1, results.size());
			assertTrue(allDao.objectsEqual(allTypes, results.get(0)));
		}
	}

	public void testAllTypesDefault() throws Exception {
		Dao<AllTypes, Integer> allDao = createDao(AllTypes.class, true);
		AllTypes allTypes = new AllTypes();
		assertEquals(1, allDao.create(allTypes));
		List<AllTypes> allTypesList = allDao.queryForAll();
		assertEquals(1, allTypesList.size());
		assertTrue(allDao.objectsEqual(allTypes, allTypesList.get(0)));
	}

	public void testNumberTypes() throws Exception {
		Dao<NumberTypes, Integer> numberDao = createDao(NumberTypes.class, true);

		NumberTypes numberMins = new NumberTypes();
		numberMins.byteField = Byte.MIN_VALUE;
		numberMins.shortField = Short.MIN_VALUE;
		numberMins.intField = Integer.MIN_VALUE;
		numberMins.longField = Long.MIN_VALUE;
		numberMins.floatField = -1.0e+37F;
		numberMins.doubleField = -1.0e+307;
		assertEquals(1, numberDao.create(numberMins));

		NumberTypes numberMins2 = new NumberTypes();
		numberMins2.byteField = Byte.MIN_VALUE;
		numberMins2.shortField = Short.MIN_VALUE;
		numberMins2.intField = Integer.MIN_VALUE;
		numberMins2.longField = Long.MIN_VALUE;
		numberMins2.floatField = 1.0e-37F;
		// derby couldn't take 1.0e-307 for some reason
		numberMins2.doubleField = 1.0e-306;
		assertEquals(1, numberDao.create(numberMins2));

		NumberTypes numberMaxs = new NumberTypes();
		numberMaxs.byteField = Byte.MAX_VALUE;
		numberMaxs.shortField = Short.MAX_VALUE;
		numberMaxs.intField = Integer.MAX_VALUE;
		numberMaxs.longField = Long.MAX_VALUE;
		numberMaxs.floatField = 1.0e+37F;
		numberMaxs.doubleField = 1.0e+307;
		assertEquals(1, numberDao.create(numberMaxs));
		assertEquals(1, numberDao.refresh(numberMaxs));

		List<NumberTypes> allTypesList = numberDao.queryForAll();
		assertEquals(3, allTypesList.size());
		assertTrue(numberDao.objectsEqual(numberMins, allTypesList.get(0)));
		assertTrue(numberDao.objectsEqual(numberMins2, allTypesList.get(1)));
		assertTrue(numberDao.objectsEqual(numberMaxs, allTypesList.get(2)));
	}

	public void testStringWidthTooLong() throws Exception {
		if (connectionSource == null) {
			return;
		}
		if (!connectionSource.getDatabaseType().isVarcharFieldWidthSupported()) {
			return;
		}
		Dao<StringWidth, Object> stringWidthDao = createDao(StringWidth.class, true);
		StringWidth stringWidth = new StringWidth();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < ALL_TYPES_STRING_WIDTH + 1; i++) {
			sb.append("c");
		}
		String string = sb.toString();
		assertTrue(string.length() > ALL_TYPES_STRING_WIDTH);
		stringWidth.stringField = string;
		try {
			stringWidthDao.create(stringWidth);
			fail("expected exception");
		} catch (SQLException e) {
			// expected
		}
	}

	public void testStringWidthOkay() throws Exception {
		Dao<StringWidth, Object> stringWidthDao = createDao(StringWidth.class, true);
		StringWidth stringWidth = new StringWidth();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < ALL_TYPES_STRING_WIDTH; i++) {
			sb.append("c");
		}
		String string = sb.toString();
		assertTrue(string.length() == ALL_TYPES_STRING_WIDTH);
		stringWidth.stringField = string;
		assertEquals(1, stringWidthDao.create(stringWidth));
		List<StringWidth> stringWidthList = stringWidthDao.queryForAll();
		assertEquals(1, stringWidthList.size());
		assertTrue(stringWidthDao.objectsEqual(stringWidth, stringWidthList.get(0)));
	}

	public void testCreateReserverdTable() throws Exception {
		Dao<Where, String> whereDao = createDao(Where.class, true);
		String id = "from-string";
		Where where = new Where();
		where.id = id;
		assertEquals(1, whereDao.create(where));
		Where where2 = whereDao.queryForId(id);
		assertEquals(id, where2.id);
		assertEquals(1, whereDao.delete(where2));
		assertNull(whereDao.queryForId(id));
	}

	public void testCreateReserverdFields() throws Exception {
		Dao<ReservedField, Object> reservedDao = createDao(ReservedField.class, true);
		String from = "from-string";
		ReservedField res = new ReservedField();
		res.from = from;
		assertEquals(1, reservedDao.create(res));
		int id = res.select;
		ReservedField res2 = reservedDao.queryForId(id);
		assertNotNull(res2);
		assertEquals(id, res2.select);
		String group = "group-string";
		for (ReservedField reserved : reservedDao) {
			assertEquals(from, reserved.from);
			reserved.group = group;
			reservedDao.update(reserved);
		}
		Iterator<ReservedField> reservedIterator = reservedDao.iterator();
		while (reservedIterator.hasNext()) {
			ReservedField reserved = reservedIterator.next();
			assertEquals(from, reserved.from);
			assertEquals(group, reserved.group);
			reservedIterator.remove();
		}
		assertEquals(0, reservedDao.queryForAll().size());
	}

	public void testEscapeCharInField() throws Exception {
		if (connectionSource == null) {
			return;
		}
		StringBuilder sb = new StringBuilder();
		String word = "foo";
		connectionSource.getDatabaseType().appendEscapedWord(sb, word);
		String escaped = sb.toString();
		int index = escaped.indexOf(word);
		String escapeString = escaped.substring(0, index);

		Dao<Basic, String> fooDao = createDao(Basic.class, true);
		Basic foo1 = new Basic();
		String id = word + escapeString + word;
		foo1.id = id;
		assertEquals(1, fooDao.create(foo1));
		Basic foo2 = fooDao.queryForId(id);
		assertNotNull(foo2);
		assertEquals(id, foo2.id);
	}

	public void testGeneratedIdCapital() throws Exception {
		createDao(GeneratedColumnCapital.class, true);
	}

	public void testObject() throws Exception {
		Dao<ObjectHolder, Integer> objDao = createDao(ObjectHolder.class, true);

		ObjectHolder foo1 = new ObjectHolder();
		foo1.obj = new SerialData();
		String key = "key2";
		String value = "val2";
		foo1.obj.addEntry(key, value);
		String strObj = "fjpwefefwpjoefwjpojopfew";
		foo1.strObj = strObj;
		assertEquals(1, objDao.create(foo1));

		ObjectHolder foo2 = objDao.queryForId(foo1.id);
		assertTrue(objDao.objectsEqual(foo1, foo2));
	}

	public void testNotSerializable() throws Exception {
		try {
			createDao(NotSerializable.class, true);
			fail("expected exception");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	public void testStringEnum() throws Exception {
		Dao<LocalEnumString, Object> fooDao = createDao(LocalEnumString.class, true);
		OurEnum ourEnum = OurEnum.SECOND;
		LocalEnumString foo = new LocalEnumString();
		foo.ourEnum = ourEnum;
		assertEquals(1, fooDao.create(foo));

		List<LocalEnumString> fooList = fooDao.queryForAll();
		assertEquals(1, fooList.size());
		assertEquals(ourEnum, fooList.get(0).ourEnum);
	}

	public void testUnknownStringEnum() throws Exception {
		Dao<LocalEnumString, Object> fooDao = createDao(LocalEnumString.class, true);
		OurEnum ourEnum = OurEnum.SECOND;
		LocalEnumString foo = new LocalEnumString();
		foo.ourEnum = ourEnum;
		assertEquals(1, fooDao.create(foo));

		Dao<LocalEnumString2, Object> foo2Dao = createDao(LocalEnumString2.class, false);
		try {
			foo2Dao.queryForAll();
			fail("expected exception");
		} catch (SQLException e) {
			// expected
		}
	}

	public void testIntEnum() throws Exception {
		Dao<LocalEnumInt, Object> fooDao = createDao(LocalEnumInt.class, true);
		OurEnum ourEnum = OurEnum.SECOND;
		LocalEnumInt foo = new LocalEnumInt();
		foo.ourEnum = ourEnum;
		assertEquals(1, fooDao.create(foo));

		List<LocalEnumInt> fooList = fooDao.queryForAll();
		assertEquals(1, fooList.size());
		assertEquals(ourEnum, fooList.get(0).ourEnum);
	}

	public void testUnknownIntEnum() throws Exception {
		Dao<LocalEnumInt, Object> fooDao = createDao(LocalEnumInt.class, true);
		OurEnum ourEnum = OurEnum.SECOND;
		LocalEnumInt foo = new LocalEnumInt();
		foo.ourEnum = ourEnum;
		assertEquals(1, fooDao.create(foo));

		Dao<LocalEnumInt2, Object> foo2Dao = createDao(LocalEnumInt2.class, false);
		try {
			foo2Dao.queryForAll();
			fail("expected exception");
		} catch (SQLException e) {
			// expected
		}
	}

	public void testUnknownIntUnknownValEnum() throws Exception {
		Dao<LocalEnumInt, Object> fooDao = createDao(LocalEnumInt.class, true);
		OurEnum ourEnum = OurEnum.SECOND;
		LocalEnumInt foo = new LocalEnumInt();
		foo.ourEnum = ourEnum;
		assertEquals(1, fooDao.create(foo));

		Dao<LocalEnumInt3, Object> foo2Dao = createDao(LocalEnumInt3.class, false);
		List<LocalEnumInt3> fooList = foo2Dao.queryForAll();
		assertEquals(1, fooList.size());
		assertEquals(OurEnum2.FIRST, fooList.get(0).ourEnum);
	}

	public void testNullHandling() throws Exception {
		Dao<AllObjectTypes, Object> allDao = createDao(AllObjectTypes.class, true);
		AllObjectTypes all = new AllObjectTypes();
		assertEquals(1, allDao.create(all));
		List<AllObjectTypes> allList = allDao.queryForAll();
		assertEquals(1, allList.size());
		assertTrue(allDao.objectsEqual(all, allList.get(0)));
	}

	public void testObjectNotNullHandling() throws Exception {
		Dao<AllObjectTypes, Object> allDao = createDao(AllObjectTypes.class, true);
		AllObjectTypes all = new AllObjectTypes();
		all.stringField = "foo";
		all.booleanField = false;
		Date dateValue = new Date(1279649192000L);
		all.dateField = dateValue;
		all.byteField = 0;
		all.shortField = 0;
		all.intField = 0;
		all.longField = 0L;
		all.floatField = 0F;
		all.doubleField = 0D;
		all.objectField = new SerialData();
		all.ourEnum = OurEnum.FIRST;
		assertEquals(1, allDao.create(all));
		assertEquals(1, allDao.refresh(all));
		List<AllObjectTypes> allList = allDao.queryForAll();
		assertEquals(1, allList.size());
		assertTrue(allDao.objectsEqual(all, allList.get(0)));
	}

	public void testDefaultValueHandling() throws Exception {
		Dao<AllTypesDefault, Object> allDao = createDao(AllTypesDefault.class, true);
		AllTypesDefault all = new AllTypesDefault();
		assertEquals(1, allDao.create(all));
		assertEquals(1, allDao.refresh(all));
		List<AllTypesDefault> allList = allDao.queryForAll();
		assertEquals(1, allList.size());
		all.stringField = DEFAULT_STRING_VALUE;
		all.dateField = defaultDateFormat.parse(DEFAULT_DATE_VALUE);
		all.dateLongField = new Date(Long.parseLong(DEFAULT_DATE_LONG_VALUE));
		all.dateStringField = defaultDateStringFormat.parse(DEFAULT_DATE_STRING_VALUE);
		all.booleanField = Boolean.parseBoolean(DEFAULT_BOOLEAN_VALUE);
		all.booleanObj = Boolean.parseBoolean(DEFAULT_BOOLEAN_VALUE);
		all.byteField = Byte.parseByte(DEFAULT_BYTE_VALUE);
		all.byteObj = Byte.parseByte(DEFAULT_BYTE_VALUE);
		all.shortField = Short.parseShort(DEFAULT_SHORT_VALUE);
		all.shortObj = Short.parseShort(DEFAULT_SHORT_VALUE);
		all.intField = Integer.parseInt(DEFAULT_INT_VALUE);
		all.intObj = Integer.parseInt(DEFAULT_INT_VALUE);
		all.longField = Long.parseLong(DEFAULT_LONG_VALUE);
		all.longObj = Long.parseLong(DEFAULT_LONG_VALUE);
		all.floatField = Float.parseFloat(DEFAULT_FLOAT_VALUE);
		all.floatObj = Float.parseFloat(DEFAULT_FLOAT_VALUE);
		all.doubleField = Double.parseDouble(DEFAULT_DOUBLE_VALUE);
		all.doubleObj = Double.parseDouble(DEFAULT_DOUBLE_VALUE);
		all.ourEnum = OurEnum.valueOf(DEFAULT_ENUM_VALUE);
		assertFalse(allDao.objectsEqual(all, allList.get(0)));
	}

	public void testBooleanDefaultValueHandling() throws Exception {
		Dao<BooleanDefault, Object> allDao = createDao(BooleanDefault.class, true);
		BooleanDefault all = new BooleanDefault();
		assertEquals(1, allDao.create(all));
		List<BooleanDefault> allList = allDao.queryForAll();
		assertEquals(1, allList.size());
		all.booleanField = Boolean.parseBoolean(DEFAULT_BOOLEAN_VALUE);
		all.booleanObj = Boolean.parseBoolean(DEFAULT_BOOLEAN_VALUE);
		assertFalse(allDao.objectsEqual(all, allList.get(0)));
	}

	public void testNullUnPersistToBooleanPrimitive() throws Exception {
		Dao<NullBoolean1, Object> null1Dao = createDao(NullBoolean1.class, true);
		NullBoolean1 nullThing = new NullBoolean1();
		assertEquals(1, null1Dao.create(nullThing));
		Dao<NullBoolean2, Object> null2Dao = createDao(NullBoolean2.class, false);
		try {
			null2Dao.queryForAll();
			fail("expected exception");
		} catch (SQLException e) {
			// expected
		}
	}

	public void testNullUnPersistToIntPrimitive() throws Exception {
		Dao<NullInt1, Object> null1Dao = createDao(NullInt1.class, true);
		NullInt1 nullThing = new NullInt1();
		assertEquals(1, null1Dao.create(nullThing));
		Dao<NullInt2, Object> null2Dao = createDao(NullInt2.class, false);
		try {
			null2Dao.queryForAll();
			fail("expected exception");
		} catch (SQLException e) {
			// expected
		}
	}

	public void testQueryRawStrings() throws Exception {
		Dao<Foo, Object> fooDao = createDao(Foo.class, true);
		Foo foo = new Foo();
		String stuff = "eprjpejrre";
		foo.stuff = stuff;

		String queryString = buildFooQueryAllString(fooDao);
		GenericRawResults<String[]> results = fooDao.queryRaw(queryString);
		assertEquals(0, results.getResults().size());
		assertEquals(1, fooDao.create(foo));

		results = fooDao.queryRaw(queryString);
		int colN = results.getNumberColumns();
		String[] colNames = results.getColumnNames();
		assertEquals(3, colNames.length);
		boolean gotId = false;
		boolean gotStuff = false;
		boolean gotVal = false;
		// all this crap is here because of android column order
		for (int colC = 0; colC < 3; colC++) {
			if (colNames[colC].equalsIgnoreCase(Foo.ID_FIELD_NAME)) {
				gotId = true;
			} else if (colNames[colC].equalsIgnoreCase(Foo.STUFF_FIELD_NAME)) {
				gotStuff = true;
			} else if (colNames[colC].equalsIgnoreCase(Foo.VAL_FIELD_NAME)) {
				gotVal = true;
			}
		}
		assertTrue(gotId);
		assertTrue(gotStuff);
		assertTrue(gotVal);
		List<String[]> resultList = results.getResults();
		assertEquals(1, resultList.size());
		String[] result = resultList.get(0);
		assertEquals(colN, result.length);
		for (int colC = 0; colC < results.getNumberColumns(); colC++) {
			if (results.getColumnNames()[colC] == "id") {
				assertEquals(Integer.toString(foo.id), result[colC]);
			}
			if (results.getColumnNames()[colC] == "stuff") {
				assertEquals(stuff, result[1]);
			}
		}
	}

	public void testQueryRawStringsIterator() throws Exception {
		Dao<Foo, Object> fooDao = createDao(Foo.class, true);
		Foo foo = new Foo();
		String stuff = "eprjpejrre";
		int val = 12321411;
		foo.stuff = stuff;
		foo.val = val;

		String queryString = buildFooQueryAllString(fooDao);
		GenericRawResults<String[]> results = fooDao.queryRaw(queryString);
		CloseableIterator<String[]> iterator = results.iterator();
		try {
			assertFalse(iterator.hasNext());
		} finally {
			iterator.close();
		}
		assertEquals(1, fooDao.create(foo));

		results = fooDao.queryRaw(queryString);
		int colN = results.getNumberColumns();
		String[] colNames = results.getColumnNames();
		assertEquals(3, colNames.length);
		iterator = results.iterator();
		try {
			assertTrue(iterator.hasNext());
			String[] result = iterator.next();
			assertEquals(colN, result.length);
			boolean foundId = false;
			boolean foundStuff = false;
			boolean foundVal = false;
			for (int colC = 0; colC < results.getNumberColumns(); colC++) {
				if (results.getColumnNames()[colC].equalsIgnoreCase(Foo.ID_FIELD_NAME)) {
					assertEquals(Integer.toString(foo.id), result[colC]);
					foundId = true;
				}
				if (results.getColumnNames()[colC].equalsIgnoreCase(Foo.STUFF_FIELD_NAME)) {
					assertEquals(stuff, result[colC]);
					foundStuff = true;
				}
				if (results.getColumnNames()[colC].equalsIgnoreCase(Foo.VAL_FIELD_NAME)) {
					assertEquals(Integer.toString(val), result[colC]);
					foundVal = true;
				}
			}
			assertTrue(foundId);
			assertTrue(foundStuff);
			assertTrue(foundVal);
			assertFalse(iterator.hasNext());
		} finally {
			iterator.close();
		}
	}

	public void testQueryRawMappedIterator() throws Exception {
		Dao<Foo, Object> fooDao = createDao(Foo.class, true);
		final Foo foo = new Foo();
		String stuff = "eprjpejrre";
		foo.stuff = stuff;

		String queryString = buildFooQueryAllString(fooDao);
		Mapper mapper = new Mapper();
		GenericRawResults<Foo> rawResults = fooDao.queryRaw(queryString, mapper);
		assertEquals(0, rawResults.getResults().size());
		assertEquals(1, fooDao.create(foo));
		rawResults = fooDao.queryRaw(queryString, mapper);
		Iterator<Foo> iterator = rawResults.iterator();
		assertTrue(iterator.hasNext());
		Foo foo2 = iterator.next();
		assertEquals(foo.id, foo2.id);
		assertEquals(foo.stuff, foo2.stuff);
		assertEquals(foo.val, foo2.val);
		assertFalse(iterator.hasNext());
	}

	public void testQueryRawObjectsIterator() throws Exception {
		Dao<Foo, Object> fooDao = createDao(Foo.class, true);
		Foo foo = new Foo();
		String stuff = "eprjpejrre";
		int val = 213123;
		foo.stuff = stuff;
		foo.val = val;

		String queryString = buildFooQueryAllString(fooDao);
		GenericRawResults<Object[]> results =
				fooDao.queryRaw(queryString, new DataType[] { DataType.INTEGER, DataType.STRING, DataType.INTEGER });
		CloseableIterator<Object[]> iterator = results.iterator();
		try {
			assertFalse(iterator.hasNext());
		} finally {
			iterator.close();
		}
		assertEquals(1, fooDao.create(foo));

		results = fooDao.queryRaw(queryString, new DataType[] { DataType.INTEGER, DataType.STRING, DataType.INTEGER });
		int colN = results.getNumberColumns();
		String[] colNames = results.getColumnNames();
		assertEquals(3, colNames.length);
		iterator = results.iterator();
		try {
			assertTrue(iterator.hasNext());
			Object[] result = iterator.next();
			assertEquals(colN, result.length);
			String[] columnNames = results.getColumnNames();
			boolean foundId = false;
			boolean foundStuff = false;
			boolean foundVal = false;
			for (int colC = 0; colC < results.getNumberColumns(); colC++) {
				if (columnNames[colC].equalsIgnoreCase(Foo.ID_FIELD_NAME)) {
					assertEquals(foo.id, result[colC]);
					foundId = true;
				}
				if (columnNames[colC].equalsIgnoreCase(Foo.STUFF_FIELD_NAME)) {
					assertEquals(stuff, result[colC]);
					foundStuff = true;
				}
				if (columnNames[colC].equalsIgnoreCase(Foo.VAL_FIELD_NAME)) {
					assertEquals(val, result[colC]);
					foundVal = true;
				}
			}
			assertTrue(foundId);
			assertTrue(foundStuff);
			assertTrue(foundVal);
			assertFalse(iterator.hasNext());
		} finally {
			iterator.close();
		}
	}

	@SuppressWarnings("deprecation")
	public void testRawResults() throws Exception {
		Dao<Foo, Object> fooDao = createDao(Foo.class, true);
		Foo foo = new Foo();
		String stuff = "eprjpejrre";
		foo.stuff = stuff;

		String queryString = buildFooQueryAllString(fooDao);
		RawResults results = fooDao.queryForAllRaw(queryString);
		assertEquals(0, results.getResults().size());
		assertEquals(1, fooDao.create(foo));

		results = fooDao.queryForAllRaw(queryString);
		int colN = results.getNumberColumns();
		String[] colNames = results.getColumnNames();
		assertEquals(3, colNames.length);
		boolean gotId = false;
		boolean gotStuff = false;
		boolean gotVal = false;
		// all this crap is here because of android column order
		for (int colC = 0; colC < 3; colC++) {
			if (colNames[colC].equalsIgnoreCase(Foo.ID_FIELD_NAME)) {
				gotId = true;
			} else if (colNames[colC].equalsIgnoreCase(Foo.STUFF_FIELD_NAME)) {
				gotStuff = true;
			} else if (colNames[colC].equalsIgnoreCase(Foo.VAL_FIELD_NAME)) {
				gotVal = true;
			}
		}
		assertTrue(gotId);
		assertTrue(gotStuff);
		assertTrue(gotVal);
		List<String[]> resultList = results.getResults();
		assertEquals(1, resultList.size());
		String[] result = resultList.get(0);
		assertEquals(colN, result.length);
		String[] columnNames = results.getColumnNames();
		for (int colC = 0; colC < results.getNumberColumns(); colC++) {
			if (columnNames[colC].equalsIgnoreCase(Foo.ID_FIELD_NAME)) {
				assertEquals(Integer.toString(foo.id), result[colC]);
			} else if (columnNames[colC].equalsIgnoreCase(Foo.STUFF_FIELD_NAME)) {
				assertEquals(stuff, result[colC]);
			} else if (columnNames[colC].equalsIgnoreCase(Foo.VAL_FIELD_NAME)) {
				assertEquals(Integer.toString(foo.val), result[colC]);
			} else {
				fail("Unknown column: " + columnNames[colC]);
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void testRawResultsIterator() throws Exception {
		Dao<Foo, Object> fooDao = createDao(Foo.class, true);
		Foo foo = new Foo();
		String stuff = "eprjpejrre";
		foo.stuff = stuff;

		String queryString = buildFooQueryAllString(fooDao);
		RawResults results = fooDao.iteratorRaw(queryString);
		CloseableIterator<String[]> iterator = results.iterator();
		try {
			assertFalse(iterator.hasNext());
		} finally {
			iterator.close();
		}
		assertEquals(1, fooDao.create(foo));

		results = fooDao.queryForAllRaw(queryString);
		int colN = results.getNumberColumns();
		String[] colNames = results.getColumnNames();
		assertEquals(3, colNames.length);
		iterator = results.iterator();
		try {
			assertTrue(iterator.hasNext());
			String[] result = iterator.next();
			assertEquals(colN, result.length);
			for (int colC = 0; colC < results.getNumberColumns(); colC++) {
				if (results.getColumnNames()[colC] == "id") {
					assertEquals(Integer.toString(foo.id), result[colC]);
				}
				if (results.getColumnNames()[colC] == "stuff") {
					assertEquals(stuff, result[1]);
				}
			}
			assertFalse(iterator.hasNext());
		} finally {
			iterator.close();
		}
	}

	@SuppressWarnings("deprecation")
	public void testRawResultsMappedList() throws Exception {
		Dao<Foo, Object> fooDao = createDao(Foo.class, true);
		final Foo foo = new Foo();
		String stuff = "eprjpejrre";
		foo.stuff = stuff;

		String queryString = buildFooQueryAllString(fooDao);
		RawResults rawResults = fooDao.queryForAllRaw(queryString);
		assertEquals(0, rawResults.getResults().size());
		assertEquals(1, fooDao.create(foo));
		rawResults = fooDao.queryForAllRaw(queryString);
		List<Foo> results = rawResults.getMappedResults(new Mapper());
		assertEquals(1, results.size());
		Foo foo2 = results.get(0);
		assertEquals(foo.id, foo2.id);
		assertEquals(foo.stuff, foo2.stuff);
		assertEquals(foo.val, foo2.val);
	}

	@SuppressWarnings("deprecation")
	public void testRawResultsMappedIterator() throws Exception {
		Dao<Foo, Object> fooDao = createDao(Foo.class, true);
		final Foo foo = new Foo();
		String stuff = "eprjpejrre";
		foo.stuff = stuff;

		String queryString = buildFooQueryAllString(fooDao);
		RawResults rawResults = fooDao.queryForAllRaw(queryString);
		assertEquals(0, rawResults.getResults().size());
		assertEquals(1, fooDao.create(foo));
		rawResults = fooDao.queryForAllRaw(queryString);
		Iterator<Foo> iterator = rawResults.iterator(new Mapper());
		assertTrue(iterator.hasNext());
		Foo foo2 = iterator.next();
		assertEquals(foo.id, foo2.id);
		assertEquals(foo.stuff, foo2.stuff);
		assertEquals(foo.val, foo2.val);
		assertFalse(iterator.hasNext());
	}

	public void testNotNullDefault() throws Exception {
		Dao<NotNullDefault, Object> dao = createDao(NotNullDefault.class, true);
		NotNullDefault notNullDefault = new NotNullDefault();
		assertEquals(1, dao.create(notNullDefault));
	}

	public void testStringDefault() throws Exception {
		Dao<StringDefalt, Object> dao = createDao(StringDefalt.class, true);
		StringDefalt foo = new StringDefalt();
		assertEquals(1, dao.create(foo));
	}

	public void testDateUpdate() throws Exception {
		Dao<LocalDate, Object> dao = createDao(LocalDate.class, true);
		LocalDate localDate = new LocalDate();
		// note: this does not have milliseconds
		Date date = new Date(2131232000);
		localDate.date = date;
		assertEquals(1, dao.create(localDate));
		List<LocalDate> allDates = dao.queryForAll();
		assertEquals(1, allDates.size());
		assertEquals(date, allDates.get(0).date);

		// now we update it
		assertEquals(1, dao.update(localDate));
		allDates = dao.queryForAll();
		assertEquals(1, allDates.size());
		assertEquals(date, allDates.get(0).date);

		// now we set it to null
		localDate.date = null;
		// now we update it
		assertEquals(1, dao.update(localDate));
		allDates = dao.queryForAll();
		assertEquals(1, allDates.size());
		// we should get null back and not some auto generated field
		assertNull(allDates.get(0).date);
	}

	public void testDateRefresh() throws Exception {
		Dao<LocalDate, Object> dao = createDao(LocalDate.class, true);
		LocalDate localDate = new LocalDate();
		// note: this does not have milliseconds
		Date date = new Date(2131232000);
		localDate.date = date;
		assertEquals(1, dao.create(localDate));
		assertEquals(1, dao.refresh(localDate));
	}

	public void testSpringBadWiring() throws Exception {
		BaseDaoImpl<String, String> daoSupport = new BaseDaoImpl<String, String>(String.class) {
		};
		try {
			daoSupport.initialize();
			fail("expected exception");
		} catch (IllegalStateException e) {
			// expected
		}
	}

	public void testUnique() throws Exception {
		Dao<Unique, Long> dao = createDao(Unique.class, true);
		String stuff = "this doesn't need to be unique";
		String uniqueStuff = "this needs to be unique";
		Unique unique = new Unique();
		unique.stuff = stuff;
		unique.uniqueStuff = uniqueStuff;
		assertEquals(1, dao.create(unique));
		// can't create it twice with the same stuff which needs to be unique
		unique = new Unique();
		unique.stuff = stuff;
		assertEquals(1, dao.create(unique));
		unique = new Unique();
		unique.uniqueStuff = uniqueStuff;
		try {
			dao.create(unique);
			fail("Should have thrown");
		} catch (SQLException e) {
			// expected
			return;
		}
	}

	public void testForeignQuery() throws Exception {
		Dao<ForeignWrapper, Integer> wrapperDao = createDao(ForeignWrapper.class, true);
		Dao<AllTypes, Integer> foreignDao = createDao(AllTypes.class, true);

		AllTypes foreign = new AllTypes();
		String stuff1 = "stuff1";
		foreign.stringField = stuff1;
		Date date = new Date();
		foreign.dateField = date;
		// this sets the foreign id
		assertEquals(1, foreignDao.create(foreign));

		ForeignWrapper wrapper = new ForeignWrapper();
		wrapper.foreign = foreign;
		// this sets the wrapper id
		assertEquals(1, wrapperDao.create(wrapper));

		QueryBuilder<ForeignWrapper, Integer> qb = wrapperDao.queryBuilder();
		qb.where().eq(ForeignWrapper.FOREIGN_FIELD_NAME, foreign.id);
		List<ForeignWrapper> results = wrapperDao.query(qb.prepare());
		assertEquals(1, results.size());
		assertNotNull(results.get(0).foreign);
		assertEquals(foreign.id, results.get(0).foreign.id);

		/*
		 * now look it up not by foreign.id but by foreign which should extract the id automagically
		 */
		qb.where().eq(ForeignWrapper.FOREIGN_FIELD_NAME, foreign);
		results = wrapperDao.query(qb.prepare());
		assertEquals(1, results.size());
		assertNotNull(results.get(0).foreign);
		assertEquals(foreign.id, results.get(0).foreign.id);

		/*
		 * Now let's try the same thing but with a SelectArg
		 */
		SelectArg selectArg = new SelectArg();
		qb.where().eq(ForeignWrapper.FOREIGN_FIELD_NAME, selectArg);
		selectArg.setValue(foreign.id);
		results = wrapperDao.query(qb.prepare());
		assertEquals(1, results.size());
		assertNotNull(results.get(0).foreign);
		assertEquals(foreign.id, results.get(0).foreign.id);

		/*
		 * Now let's try the same thing but with a SelectArg with foreign value, not foreign.id
		 */
		selectArg = new SelectArg();
		qb.where().eq(ForeignWrapper.FOREIGN_FIELD_NAME, selectArg);
		selectArg.setValue(foreign);
		results = wrapperDao.query(qb.prepare());
		assertEquals(1, results.size());
		assertNotNull(results.get(0).foreign);
		assertEquals(foreign.id, results.get(0).foreign.id);
	}

	public void testPrepareStatementUpdateValueString() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		Foo foo = new Foo();
		String stuff = "dqedqdq";
		foo.stuff = stuff;
		assertEquals(1, fooDao.create(foo));

		QueryBuilder<Foo, Integer> stmtb = fooDao.queryBuilder();
		stmtb.where().eq(Foo.STUFF_FIELD_NAME, stuff);
		List<Foo> results = fooDao.query(stmtb.prepare());
		assertEquals(1, results.size());

		UpdateBuilder<Foo, Integer> updateb = fooDao.updateBuilder();
		String newStuff = "fepojefpjo";
		updateb.updateColumnValue(Foo.STUFF_FIELD_NAME, newStuff);
		assertEquals(1, fooDao.update(updateb.prepare()));

		results = fooDao.query(stmtb.prepare());
		assertEquals(0, results.size());
	}

	public void testInSubQuery() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		Dao<Basic, String> basicDao = createDao(Basic.class, true);

		Basic basic1 = new Basic();
		String string1 = "ewpofjewgprgrg";
		basic1.id = string1;
		assertEquals(1, basicDao.create(basic1));
		Basic basic2 = new Basic();
		String string2 = "e2432423432wpofjewgprgrg";
		basic2.id = string2;
		assertEquals(1, basicDao.create(basic2));

		Foo foo1 = new Foo();
		foo1.stuff = basic1.id;
		Foo foo2 = new Foo();
		foo2.stuff = basic2.id;
		Foo foo3 = new Foo();
		String string3 = "neither of the others";
		foo3.stuff = string3;

		int num1 = 7;
		for (int i = 0; i < num1; i++) {
			assertEquals(1, fooDao.create(foo1));
		}
		int num2 = 17;
		for (int i = 0; i < num2; i++) {
			assertEquals(1, fooDao.create(foo2));
		}
		int num3 = 29;
		long maxId = 0;
		for (int i = 0; i < num3; i++) {
			assertEquals(1, fooDao.create(foo3));
			if (foo3.id > maxId) {
				maxId = foo3.id;
			}
		}

		QueryBuilder<Basic, String> bqb = basicDao.queryBuilder();
		bqb.selectColumns(Basic.ID_FIELD);

		// string1
		bqb.where().eq(Basic.ID_FIELD, string1);
		List<Foo> results = fooDao.query(fooDao.queryBuilder().where().in(Foo.STUFF_FIELD_NAME, bqb).prepare());
		assertEquals(num1, results.size());

		// string2
		bqb.where().eq(Basic.ID_FIELD, string2);
		results = fooDao.query(fooDao.queryBuilder().where().in(Foo.STUFF_FIELD_NAME, bqb).prepare());
		assertEquals(num2, results.size());

		// ! string2 with not().in(...)
		bqb.where().eq(Basic.ID_FIELD, string2);
		results = fooDao.query(fooDao.queryBuilder().where().not().in(Foo.STUFF_FIELD_NAME, bqb).prepare());
		assertEquals(num1 + num3, results.size());

		// string3 which there should be none
		bqb.where().eq(Basic.ID_FIELD, string3);
		results = fooDao.query(fooDao.queryBuilder().where().in(Foo.STUFF_FIELD_NAME, bqb).prepare());
		assertEquals(0, results.size());

		// string1 OR string2
		bqb.where().eq(Basic.ID_FIELD, string1).or().eq(Basic.ID_FIELD, string2);
		results = fooDao.query(fooDao.queryBuilder().where().in(Foo.STUFF_FIELD_NAME, bqb).prepare());
		assertEquals(num1 + num2, results.size());

		// all strings IN
		bqb.where().in(Basic.ID_FIELD, string1, string2, string3);
		results = fooDao.query(fooDao.queryBuilder().where().in(Foo.STUFF_FIELD_NAME, bqb).prepare());
		assertEquals(num1 + num2, results.size());

		// string1 AND string2 which there should be none
		bqb.where().eq(Basic.ID_FIELD, string1).and().eq(Basic.ID_FIELD, string2);
		results = fooDao.query(fooDao.queryBuilder().where().in(Foo.STUFF_FIELD_NAME, bqb).prepare());
		assertEquals(0, results.size());
	}

	public void testInSubQuerySelectArgs() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		Dao<Basic, String> basicDao = createDao(Basic.class, true);

		Basic basic1 = new Basic();
		String string1 = "ewpofjewgprgrg";
		basic1.id = string1;
		assertEquals(1, basicDao.create(basic1));
		Basic basic2 = new Basic();
		String string2 = "e2432423432wpofjewgprgrg";
		basic2.id = string2;
		assertEquals(1, basicDao.create(basic2));

		Foo foo1 = new Foo();
		foo1.stuff = basic1.id;
		Foo foo2 = new Foo();
		foo2.stuff = basic2.id;

		int num1 = 7;
		for (int i = 0; i < num1; i++) {
			assertEquals(1, fooDao.create(foo1));
		}
		int num2 = 17;
		long maxId = 0;
		for (int i = 0; i < num2; i++) {
			assertEquals(1, fooDao.create(foo2));
			if (foo2.id > maxId) {
				maxId = foo2.id;
			}
		}
		// using seletArgs
		SelectArg arg1 = new SelectArg();
		SelectArg arg2 = new SelectArg();
		QueryBuilder<Basic, String> bqb = basicDao.queryBuilder();
		bqb.selectColumns(Basic.ID_FIELD);
		bqb.where().eq(Basic.ID_FIELD, arg1);
		PreparedQuery<Foo> preparedQuery =
				fooDao.queryBuilder().where().in(Foo.STUFF_FIELD_NAME, bqb).and().lt(Foo.ID_FIELD_NAME, arg2).prepare();
		arg1.setValue(string1);
		// this should get none
		arg2.setValue(0);
		List<Foo> results = fooDao.query(preparedQuery);
		assertEquals(0, results.size());
	}

	public void testPrepareStatementUpdateValueNumber() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		Foo foo = new Foo();
		foo.val = 123213;
		assertEquals(1, fooDao.create(foo));

		QueryBuilder<Foo, Integer> stmtb = fooDao.queryBuilder();
		stmtb.where().eq(Foo.ID_FIELD_NAME, foo.id);
		List<Foo> results = fooDao.query(stmtb.prepare());
		assertEquals(1, results.size());

		UpdateBuilder<Foo, Integer> updateb = fooDao.updateBuilder();
		updateb.updateColumnValue(Foo.VAL_FIELD_NAME, foo.val + 1);
		assertEquals(1, fooDao.update(updateb.prepare()));

		results = fooDao.query(stmtb.prepare());
		assertEquals(1, results.size());
		assertEquals(foo.val + 1, results.get(0).val);
	}

	public void testPrepareStatementUpdateValueExpression() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		Foo foo = new Foo();
		foo.val = 123213;
		assertEquals(1, fooDao.create(foo));

		QueryBuilder<Foo, Integer> stmtb = fooDao.queryBuilder();
		stmtb.where().eq(Foo.ID_FIELD_NAME, foo.id);
		List<Foo> results = fooDao.query(stmtb.prepare());
		assertEquals(1, results.size());

		UpdateBuilder<Foo, Integer> updateb = fooDao.updateBuilder();
		String stuff = "deopdjq";
		updateb.updateColumnValue(Foo.STUFF_FIELD_NAME, stuff);
		StringBuilder sb = new StringBuilder();
		updateb.escapeColumnName(sb, Foo.VAL_FIELD_NAME);
		sb.append("+ 1");
		updateb.updateColumnExpression(Foo.VAL_FIELD_NAME, sb.toString());
		assertEquals(1, fooDao.update(updateb.prepare()));

		results = fooDao.queryForAll();
		assertEquals(1, results.size());
		assertEquals(stuff, results.get(0).stuff);
		assertEquals(foo.val + 1, results.get(0).val);
	}

	public void testPrepareStatementUpdateValueWhere() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		foo1.val = 78582351;
		assertEquals(1, fooDao.create(foo1));
		Foo foo2 = new Foo();
		String stuff = "eopqjdepodje";
		foo2.stuff = stuff;
		foo2.val = 123344131;
		assertEquals(1, fooDao.create(foo2));

		UpdateBuilder<Foo, Integer> updateb = fooDao.updateBuilder();
		String newStuff = "deopdjq";
		updateb.updateColumnValue(Foo.STUFF_FIELD_NAME, newStuff);
		StringBuilder sb = new StringBuilder();
		updateb.escapeColumnName(sb, Foo.VAL_FIELD_NAME);
		sb.append("+ 1");
		updateb.updateColumnExpression(Foo.VAL_FIELD_NAME, sb.toString());
		updateb.where().eq(Foo.ID_FIELD_NAME, foo2.id);
		assertEquals(1, fooDao.update(updateb.prepare()));

		List<Foo> results = fooDao.queryForAll();
		assertEquals(2, results.size());
		Foo foo = results.get(0);
		assertEquals(foo1.id, foo.id);
		assertEquals(foo1.val, foo.val);
		assertNull(foo.stuff);
		foo = results.get(1);
		assertEquals(foo2.id, foo.id);
		assertEquals(foo2.val + 1, foo.val);
		assertEquals(newStuff, foo.stuff);
	}

	public void testStringAsId() throws Exception {
		checkTypeAsId(StringId.class, "foo", "bar");
	}

	public void testLongStringAsId() throws Exception {
		try {
			createDao(LongStringId.class, true);
			fail("expected exception");
		} catch (SQLException e) {
			// expected
		}
	}

	public void testBooleanAsId() throws Exception {
		try {
			createDao(BooleanId.class, true);
			fail("expected exception");
		} catch (SQLException e) {
			// expected
		}
	}

	public void testBooleanObjAsId() throws Exception {
		try {
			createDao(BooleanObjId.class, true);
			fail("expected exception");
		} catch (SQLException e) {
			// expected
		}
	}

	public void testDateAsId() throws Exception {
		// no milliseconds
		checkTypeAsId(DateId.class, new Date(1232312313000L), new Date(1232312783000L));
	}

	public void testDateLongAsId() throws Exception {
		// no milliseconds
		checkTypeAsId(DateLongId.class, new Date(1232312313000L), new Date(1232312783000L));
	}

	public void testDateStringAsId() throws Exception {
		// no milliseconds
		checkTypeAsId(DateStringId.class, new Date(1232312313000L), new Date(1232312783000L));
	}

	public void testByteAsId() throws Exception {
		checkTypeAsId(ByteId.class, (byte) 1, (byte) 2);
	}

	public void testByteObjAsId() throws Exception {
		checkTypeAsId(ByteObjId.class, (byte) 1, (byte) 2);
	}

	public void testShortAsId() throws Exception {
		checkTypeAsId(ShortId.class, (short) 1, (short) 2);
	}

	public void testShortObjAsId() throws Exception {
		checkTypeAsId(ShortObjId.class, (short) 1, (short) 2);
	}

	public void testIntAsId() throws Exception {
		checkTypeAsId(IntId.class, (int) 1, (int) 2);
	}

	public void testIntObjAsId() throws Exception {
		checkTypeAsId(IntObjId.class, (int) 1, (int) 2);
	}

	public void testLongAsId() throws Exception {
		checkTypeAsId(LongId.class, (long) 1, (long) 2);
	}

	public void testLongObjAsId() throws Exception {
		checkTypeAsId(LongObjId.class, (long) 1, (long) 2);
	}

	public void testFloatAsId() throws Exception {
		checkTypeAsId(FloatId.class, (float) 1, (float) 2);
	}

	public void testFloatObjAsId() throws Exception {
		checkTypeAsId(FloatObjId.class, (float) 1, (float) 2);
	}

	public void testDoubleAsId() throws Exception {
		checkTypeAsId(DoubleId.class, (double) 1, (double) 2);
	}

	public void testDoubleObjAsId() throws Exception {
		checkTypeAsId(DoubleObjId.class, (double) 1, (double) 2);
	}

	public void testEnumAsId() throws Exception {
		checkTypeAsId(EnumId.class, OurEnum.SECOND, OurEnum.FIRST);
	}

	public void testEnumStringAsId() throws Exception {
		checkTypeAsId(EnumStringId.class, OurEnum.SECOND, OurEnum.FIRST);
	}

	public void testEnumIntegerAsId() throws Exception {
		checkTypeAsId(EnumIntegerId.class, OurEnum.SECOND, OurEnum.FIRST);
	}

	public void testSerializableAsId() throws Exception {
		try {
			createDao(SerializableId.class, true);
			fail("expected exception");
		} catch (SQLException e) {
			// expected
		}
	}

	public void testRecursiveForeign() throws Exception {
		Dao<Recursive, Integer> recursiveDao = createDao(Recursive.class, true);
		Recursive recursive1 = new Recursive();
		Recursive recursive2 = new Recursive();
		recursive2.foreign = recursive1;
		assertEquals(recursiveDao.create(recursive1), 1);
		assertEquals(recursiveDao.create(recursive2), 1);
		Recursive recursive3 = recursiveDao.queryForId(recursive2.id);
		assertNotNull(recursive3);
		assertEquals(recursive1.id, recursive3.foreign.id);
	}

	public void testSerializableWhere() throws Exception {
		Dao<AllTypes, Object> allDao = createDao(AllTypes.class, true);
		try {
			// can't query for a serial field
			allDao.queryBuilder().where().eq(AllTypes.SERIAL_FIELD_NAME, new SelectArg());
			fail("expected exception");
		} catch (SQLException e) {
			// expected
		}
	}

	@SuppressWarnings("deprecation")
	public void testInteratorForAllRaw() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		int valSum = 0;
		int fooN = 20;
		for (int i = 0; i < fooN; i++) {
			Foo foo = new Foo();
			foo.val = i / 2;
			assertEquals(1, fooDao.create(foo));
			valSum += foo.val;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("select ");
		databaseType.appendEscapedEntityName(sb, Foo.VAL_FIELD_NAME);
		sb.append(" from ").append(FOO_TABLE_NAME);
		sb.append(" group by ");
		databaseType.appendEscapedEntityName(sb, Foo.VAL_FIELD_NAME);
		sb.append(" order by ");
		databaseType.appendEscapedEntityName(sb, Foo.VAL_FIELD_NAME);
		RawResults rawResults = fooDao.iteratorRaw(sb.toString());
		String[] cols = rawResults.getColumnNames();
		assertEquals(1, cols.length);
		// on android, the quotes are exposed
		if (cols[0].compareToIgnoreCase(Foo.VAL_FIELD_NAME) != 0) {
			assertTrue(cols[0].contains(Foo.VAL_FIELD_NAME));
		}
		int i = 0;
		for (String[] resultArray : rawResults) {
			assertEquals(1, resultArray.length);
			assertEquals(Integer.toString(i), resultArray[0]);
			i++;
		}
		assertEquals(i, fooN / 2);
	}

	public void testSerializedBytes() throws Exception {
		Dao<SerializedBytes, Integer> dao = createDao(SerializedBytes.class, true);
		SerializedBytes serial = new SerializedBytes();
		serial.bytes = new byte[] { 1, 25, 3, 124, 10 };
		assertEquals(1, dao.create(serial));
		SerializedBytes raw2 = dao.queryForId(serial.id);
		assertNotNull(raw2);
		assertEquals(serial.id, raw2.id);
		assertTrue(Arrays.equals(serial.bytes, raw2.bytes));
	}

	public void testByteArray() throws Exception {
		Dao<ByteArray, Integer> dao = createDao(ByteArray.class, true);
		ByteArray foo = new ByteArray();
		foo.bytes = new byte[] { 17, 25, 3, 124, 0, 127, 10 };
		assertEquals(1, dao.create(foo));
		ByteArray raw2 = dao.queryForId(foo.id);
		assertNotNull(raw2);
		assertEquals(foo.id, raw2.id);
		assertTrue(Arrays.equals(foo.bytes, raw2.bytes));
	}

	public void testSuperClassAnnotations() throws Exception {
		Dao<Sub, Integer> dao = createDao(Sub.class, true);
		Sub sub1 = new Sub();
		String stuff = "doepqjdpqdq";
		sub1.stuff = stuff;
		assertEquals(1, dao.create(sub1));
		Sub sub2 = dao.queryForId(sub1.id);
		assertNotNull(sub2);
		assertEquals(sub1.id, sub2.id);
		assertEquals(sub1.stuff, sub2.stuff);
	}

	public void testFieldIndex() throws Exception {
		Dao<Index, Integer> dao = createDao(Index.class, true);
		Index index1 = new Index();
		String stuff = "doepqjdpqdq";
		index1.stuff = stuff;
		assertEquals(1, dao.create(index1));
		Index index2 = dao.queryForId(index1.id);
		assertNotNull(index2);
		assertEquals(index1.id, index2.id);
		assertEquals(stuff, index2.stuff);
		// this should work
		assertEquals(1, dao.create(index1));

		PreparedQuery<Index> query = dao.queryBuilder().where().eq("stuff", index1.stuff).prepare();
		List<Index> results = dao.query(query);
		assertNotNull(results);
		assertEquals(2, results.size());
		assertEquals(stuff, results.get(0).stuff);
		assertEquals(stuff, results.get(1).stuff);
	}

	public void testFieldIndexColumnName() throws Exception {
		Dao<IndexColumnName, Integer> dao = createDao(IndexColumnName.class, true);
		IndexColumnName index1 = new IndexColumnName();
		String stuff = "doepqjdpqdq";
		index1.stuff = stuff;
		assertEquals(1, dao.create(index1));
		IndexColumnName index2 = dao.queryForId(index1.id);
		assertNotNull(index2);
		assertEquals(index1.id, index2.id);
		assertEquals(stuff, index2.stuff);
		// this should work
		assertEquals(1, dao.create(index1));

		PreparedQuery<IndexColumnName> query =
				dao.queryBuilder().where().eq(IndexColumnName.STUFF_COLUMN_NAME, index1.stuff).prepare();
		List<IndexColumnName> results = dao.query(query);
		assertNotNull(results);
		assertEquals(2, results.size());
		assertEquals(stuff, results.get(0).stuff);
		assertEquals(stuff, results.get(1).stuff);
	}

	public void testFieldUniqueIndex() throws Exception {
		Dao<UniqueIndex, Integer> dao = createDao(UniqueIndex.class, true);
		UniqueIndex index1 = new UniqueIndex();
		String stuff1 = "doepqjdpqdq";
		index1.stuff = stuff1;
		assertEquals(1, dao.create(index1));
		UniqueIndex index2 = dao.queryForId(index1.id);
		assertNotNull(index2);
		assertEquals(index1.id, index2.id);
		assertEquals(stuff1, index2.stuff);
		try {
			dao.create(index1);
			fail("This should have thrown");
		} catch (SQLException e) {
			// expected
		}
		String stuff2 = "fewofwgwgwee";
		index1.stuff = stuff2;
		assertEquals(1, dao.create(index1));

		PreparedQuery<UniqueIndex> query = dao.queryBuilder().where().eq("stuff", stuff1).prepare();
		List<UniqueIndex> results = dao.query(query);
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(stuff1, results.get(0).stuff);

		query = dao.queryBuilder().where().eq("stuff", stuff2).prepare();
		results = dao.query(query);
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(stuff2, results.get(0).stuff);
	}

	public void testComboFieldIndex() throws Exception {
		Dao<ComboIndex, Integer> dao = createDao(ComboIndex.class, true);
		ComboIndex index1 = new ComboIndex();
		String stuff = "doepqjdpqdq";
		long junk1 = 131234124213213L;
		index1.stuff = stuff;
		index1.junk = junk1;
		assertEquals(1, dao.create(index1));
		ComboIndex index2 = dao.queryForId(index1.id);
		assertNotNull(index2);
		assertEquals(index1.id, index2.id);
		assertEquals(stuff, index2.stuff);
		assertEquals(junk1, index2.junk);
		assertEquals(1, dao.create(index1));

		PreparedQuery<ComboIndex> query = dao.queryBuilder().where().eq("stuff", index1.stuff).prepare();
		List<ComboIndex> results = dao.query(query);
		assertNotNull(results);
		assertEquals(2, results.size());
		assertEquals(stuff, results.get(0).stuff);
		assertEquals(junk1, results.get(0).junk);
		assertEquals(stuff, results.get(1).stuff);
		assertEquals(junk1, results.get(1).junk);
	}

	public void testComboUniqueFieldIndex() throws Exception {
		Dao<ComboUniqueIndex, Integer> dao = createDao(ComboUniqueIndex.class, true);
		ComboUniqueIndex index1 = new ComboUniqueIndex();
		String stuff1 = "doepqjdpqdq";
		long junk = 131234124213213L;
		index1.stuff = stuff1;
		index1.junk = junk;
		assertEquals(1, dao.create(index1));
		ComboUniqueIndex index2 = dao.queryForId(index1.id);
		assertNotNull(index2);
		assertEquals(index1.id, index2.id);
		assertEquals(index1.stuff, index2.stuff);
		assertEquals(index1.junk, index2.junk);
		try {
			dao.create(index1);
			fail("This should have thrown");
		} catch (SQLException e) {
			// expected
		}
		String stuff2 = "fpeowjfewpf";
		index1.stuff = stuff2;
		// same junk
		assertEquals(1, dao.create(index1));

		PreparedQuery<ComboUniqueIndex> query = dao.queryBuilder().where().eq("stuff", stuff1).prepare();
		List<ComboUniqueIndex> results = dao.query(query);
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(stuff1, results.get(0).stuff);
		assertEquals(junk, results.get(0).junk);

		query = dao.queryBuilder().where().eq("stuff", stuff2).prepare();
		results = dao.query(query);
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(stuff2, results.get(0).stuff);
		assertEquals(junk, results.get(0).junk);
	}

	public void testLongVarChar() throws Exception {
		Dao<LongVarChar, Integer> dao = createDao(LongVarChar.class, true);
		LongVarChar lvc = new LongVarChar();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 10240; i++) {
			sb.append(".");
		}
		String stuff = sb.toString();
		lvc.stuff = stuff;
		assertEquals(1, dao.create(lvc));

		LongVarChar lvc2 = dao.queryForId(lvc.id);
		assertNotNull(lvc2);
		assertEquals(stuff, lvc2.stuff);
	}

	public void testTableExists() throws Exception {
		if (!isTableExistsWorks()) {
			return;
		}
		Dao<Foo, Integer> dao = createDao(Foo.class, false);
		assertFalse(dao.isTableExists());
		TableUtils.createTable(connectionSource, Foo.class);
		assertTrue(dao.isTableExists());

		TableUtils.dropTable(connectionSource, Foo.class, false);
		assertFalse(dao.isTableExists());
	}

	public void testRaw() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		int val = 131265312;
		foo.val = val;
		assertEquals(1, dao.create(foo));
		StringBuilder sb = new StringBuilder();
		databaseType.appendEscapedEntityName(sb, Foo.VAL_FIELD_NAME);
		String fieldName = sb.toString();
		QueryBuilder<Foo, Integer> qb = dao.queryBuilder();
		qb.where().eq(Foo.ID_FIELD_NAME, foo.id).and().raw(fieldName + " = " + val);
		assertEquals(1, dao.query(qb.prepare()).size());
		qb.where().eq(Foo.ID_FIELD_NAME, foo.id).and().raw(fieldName + " != " + val);
		assertEquals(0, dao.query(qb.prepare()).size());
	}

	public void testUuidInsertQuery() throws Exception {
		Dao<UuidGeneratedId, UUID> dao = createDao(UuidGeneratedId.class, true);
		UuidGeneratedId uuid1 = new UuidGeneratedId();
		String stuff1 = "fopewfjefjwgw";
		uuid1.stuff = stuff1;
		assertNull(uuid1.id);
		assertEquals(1, dao.create(uuid1));
		assertNotNull(uuid1.id);

		UuidGeneratedId uuid2 = new UuidGeneratedId();
		String stuff2 = "fopefewjfepowfjefjwgw";
		uuid2.stuff = stuff2;
		assertNull(uuid2.id);
		assertEquals(1, dao.create(uuid2));
		assertNotNull(uuid2.id);
		assertFalse(uuid1.id.equals(uuid2.id));

		List<UuidGeneratedId> uuids = dao.queryForAll();
		assertEquals(2, uuids.size());
		UuidGeneratedId uuid3 = dao.queryForId(uuid1.id);
		assertEquals(stuff1, uuid3.stuff);
		uuid3 = dao.queryForId(uuid2.id);
		assertEquals(stuff2, uuid3.stuff);
	}

	public void testBaseDaoEnabled() throws Exception {
		Dao<One, Integer> dao = createDao(One.class, true);
		One one = new One();
		String stuff = "fewpfjewfew";
		one.stuff = stuff;
		one.setDao(dao);
		assertEquals(1, one.create());
	}

	public void testBaseDaoEnabledForeign() throws Exception {
		Dao<One, Integer> oneDao = createDao(One.class, true);
		Dao<ForeignDaoEnabled, Integer> foreignDao = createDao(ForeignDaoEnabled.class, true);

		One one = new One();
		String stuff = "fewpfjewfew";
		one.stuff = stuff;
		one.setDao(oneDao);
		assertEquals(1, one.create());

		ForeignDaoEnabled foreign = new ForeignDaoEnabled();
		foreign.one = one;
		foreign.setDao(foreignDao);
		assertEquals(1, foreign.create());

		ForeignDaoEnabled foreign2 = foreignDao.queryForId(foreign.id);
		assertNotNull(foreign2);
		assertEquals(one.id, foreign2.one.id);
		assertNull(foreign2.one.stuff);
		assertEquals(1, foreign2.one.refresh());
		assertEquals(stuff, foreign2.one.stuff);
	}

	public void testBasicEagerCollection() throws Exception {
		Dao<EagerAccount, Integer> accountDao = createDao(EagerAccount.class, true);
		Dao<EagerOrder, Integer> orderDao = createDao(EagerOrder.class, true);

		EagerAccount account = new EagerAccount();
		String name = "fwepfjewfew";
		account.name = name;
		assertEquals(1, accountDao.create(account));

		EagerOrder order1 = new EagerOrder();
		int val1 = 13123441;
		order1.val = val1;
		order1.account = account;
		assertEquals(1, orderDao.create(order1));

		EagerOrder order2 = new EagerOrder();
		int val2 = 113787097;
		order2.val = val2;
		order2.account = account;
		assertEquals(1, orderDao.create(order2));

		EagerAccount account2 = accountDao.queryForId(account.id);
		assertEquals(name, account2.name);
		assertNotNull(account2.orders);
		int orderC = 0;
		for (EagerOrder order : account2.orders) {
			orderC++;
			switch (orderC) {
				case 1 :
					assertEquals(val1, order.val);
					break;
				case 2 :
					assertEquals(val2, order.val);
					break;
			}
		}
		assertEquals(2, orderC);

		// insert it via the collection
		EagerOrder order3 = new EagerOrder();
		int val3 = 76557654;
		order3.val = val3;
		order3.account = account;
		account2.orders.add(order3);
		// the size should change immediately
		assertEquals(3, account2.orders.size());

		// now insert it behind the collections back
		EagerOrder order4 = new EagerOrder();
		int val4 = 1123587097;
		order4.val = val4;
		order4.account = account;
		assertEquals(1, orderDao.create(order4));
		// account2's collection should not have changed
		assertEquals(3, account2.orders.size());

		// now we refresh the collection
		assertEquals(1, accountDao.refresh(account2));
		assertEquals(name, account2.name);
		assertNotNull(account2.orders);
		orderC = 0;
		for (EagerOrder order : account2.orders) {
			orderC++;
			switch (orderC) {
				case 1 :
					assertEquals(val1, order.val);
					break;
				case 2 :
					assertEquals(val2, order.val);
					break;
				case 3 :
					assertEquals(val3, order.val);
					break;
				case 4 :
					assertEquals(val4, order.val);
					break;
			}
		}
		assertEquals(4, orderC);
	}

	public void testBasicLazyCollection() throws Exception {
		Dao<LazyAccount, Integer> accountDao = createDao(LazyAccount.class, true);
		Dao<LazyOrder, Integer> orderDao = createDao(LazyOrder.class, true);

		LazyAccount account = new LazyAccount();
		String name = "fwepfjewfew";
		account.name = name;
		assertEquals(1, accountDao.create(account));

		LazyOrder order1 = new LazyOrder();
		int val1 = 13123441;
		order1.val = val1;
		order1.account = account;
		assertEquals(1, orderDao.create(order1));

		LazyOrder order2 = new LazyOrder();
		int val2 = 113787097;
		order2.val = val2;
		order2.account = account;
		assertEquals(1, orderDao.create(order2));

		LazyAccount account2 = accountDao.queryForId(account.id);
		assertEquals(name, account2.name);
		assertNotNull(account2.orders);
		int orderC = 0;
		for (LazyOrder order : account2.orders) {
			orderC++;
			switch (orderC) {
				case 1 :
					assertEquals(val1, order.val);
					break;
				case 2 :
					assertEquals(val2, order.val);
					break;
			}
		}
		assertEquals(2, orderC);

		// insert it via the collection
		LazyOrder order3 = new LazyOrder();
		int val3 = 76557654;
		order3.val = val3;
		order3.account = account;
		account2.orders.add(order3);
		orderC = 0;
		for (LazyOrder order : account2.orders) {
			orderC++;
			switch (orderC) {
				case 1 :
					assertEquals(val1, order.val);
					break;
				case 2 :
					assertEquals(val2, order.val);
					break;
				case 3 :
					assertEquals(val3, order.val);
					break;
			}
		}
		assertEquals(3, orderC);

		// now insert it behind the collections back
		LazyOrder order4 = new LazyOrder();
		int val4 = 1123587097;
		order4.val = val4;
		order4.account = account;
		assertEquals(1, orderDao.create(order4));

		// without refreshing we should see the new order
		orderC = 0;
		for (LazyOrder order : account2.orders) {
			orderC++;
			switch (orderC) {
				case 1 :
					assertEquals(val1, order.val);
					break;
				case 2 :
					assertEquals(val2, order.val);
					break;
				case 3 :
					assertEquals(val3, order.val);
					break;
				case 4 :
					assertEquals(val4, order.val);
					break;
			}
		}
		assertEquals(4, orderC);
	}

	/* ==================================================================================== */

	private <T extends TestableType<ID>, ID> void checkTypeAsId(Class<T> clazz, ID id1, ID id2) throws Exception {
		Constructor<T> constructor = clazz.getDeclaredConstructor();
		Dao<T, ID> dao = createDao(clazz, true);

		String s1 = "stuff";
		T data1 = constructor.newInstance();
		data1.setId(id1);
		data1.setStuff(s1);

		// create it
		assertEquals(1, dao.create(data1));
		// refresh it
		assertEquals(1, dao.refresh(data1));

		// now we query for foo from the database to make sure it was persisted right
		T data2 = dao.queryForId(id1);
		assertNotNull(data2);
		assertEquals(id1, data2.getId());
		assertEquals(s1, data2.getStuff());

		// now we update 1 row in a the database after changing stuff
		String s2 = "stuff2";
		data2.setStuff(s2);
		assertEquals(1, dao.update(data2));

		// now we get it from the db again to make sure it was updated correctly
		T data3 = dao.queryForId(id1);
		assertEquals(s2, data3.getStuff());

		// change its id
		assertEquals(1, dao.updateId(data2, id2));
		// the old id should not exist
		assertNull(dao.queryForId(id1));
		T data4 = dao.queryForId(id2);
		assertNotNull(data4);
		assertEquals(s2, data4.getStuff());

		// delete it
		assertEquals(1, dao.delete(data2));
		// should not find it
		assertNull(dao.queryForId(id1));
		assertNull(dao.queryForId(id2));

		// create data1 and data2
		data1.setId(id1);
		assertEquals(1, dao.create(data1));
		data2 = constructor.newInstance();
		data2.setId(id2);
		assertEquals(1, dao.create(data2));

		data3 = dao.queryForId(id1);
		assertNotNull(data3);
		assertEquals(id1, data3.getId());
		data4 = dao.queryForId(id2);
		assertNotNull(data4);
		assertEquals(id2, data4.getId());

		// delete a collection of ids
		List<ID> idList = new ArrayList<ID>();
		idList.add(id1);
		idList.add(id2);
		if (UPDATE_ROWS_RETURNS_ONE) {
			assertEquals(1, dao.deleteIds(idList));
		} else {
			assertEquals(2, dao.deleteIds(idList));
		}
		assertNull(dao.queryForId(id1));
		assertNull(dao.queryForId(id2));

		// delete a collection of objects
		assertEquals(1, dao.create(data1));
		assertEquals(1, dao.create(data2));
		List<T> dataList = new ArrayList<T>();
		dataList.add(data1);
		dataList.add(data2);
		if (UPDATE_ROWS_RETURNS_ONE) {
			assertEquals(1, dao.delete(dataList));
		} else {
			assertEquals(2, dao.delete(dataList));
		}

		assertNull(dao.queryForId(id1));
		assertNull(dao.queryForId(id2));
	}

	/**
	 * Returns the object if the query failed or null otherwise.
	 */
	private boolean checkQueryResult(Dao<AllTypes, Integer> allDao, QueryBuilder<AllTypes, Integer> qb,
			AllTypes allTypes, String fieldName, Object value, boolean required) throws SQLException {
		qb.where().eq(fieldName, value);
		List<AllTypes> results = allDao.query(qb.prepare());
		if (required) {
			assertEquals(1, results.size());
			assertTrue(allDao.objectsEqual(allTypes, results.get(0)));
		} else if (results.size() == 1) {
			assertTrue(allDao.objectsEqual(allTypes, results.get(0)));
		} else {
			return false;
		}

		SelectArg selectArg = new SelectArg();
		qb.where().eq(fieldName, selectArg);
		selectArg.setValue(value);
		results = allDao.query(qb.prepare());
		assertEquals(1, results.size());
		assertTrue(allDao.objectsEqual(allTypes, results.get(0)));
		return true;
	}

	private String buildFooQueryAllString(Dao<Foo, Object> fooDao) throws SQLException {
		String queryString =
				fooDao.queryBuilder()
						.selectColumns(Foo.ID_FIELD_NAME, Foo.STUFF_FIELD_NAME, Foo.VAL_FIELD_NAME)
						.prepareStatementString();
		return queryString;
	}

	private interface TestableType<ID> {
		String getStuff();
		void setStuff(String stuff);
		ID getId();
		void setId(ID id);
	}

	private static class Mapper implements RawRowMapper<Foo> {
		public Foo mapRow(String[] columnNames, String[] resultColumns) {
			Foo foo = new Foo();
			for (int i = 0; i < columnNames.length; i++) {
				if (columnNames[i].equalsIgnoreCase(Foo.ID_FIELD_NAME)) {
					foo.id = Integer.parseInt(resultColumns[i]);
				} else if (columnNames[i].equalsIgnoreCase(Foo.STUFF_FIELD_NAME)) {
					foo.stuff = resultColumns[i];
				} else if (columnNames[i].equalsIgnoreCase(Foo.VAL_FIELD_NAME)) {
					foo.val = Integer.parseInt(resultColumns[i]);
				}
			}
			return foo;
		}
	}

	/* ==================================================================================== */

	@DatabaseTable(tableName = FOO_TABLE_NAME)
	protected static class Foo {
		public final static String ID_FIELD_NAME = "id";
		public final static String STUFF_FIELD_NAME = "stuff";
		public final static String VAL_FIELD_NAME = "val";

		@DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
		public int id;
		@DatabaseField(columnName = STUFF_FIELD_NAME)
		public String stuff;
		@DatabaseField(columnName = VAL_FIELD_NAME)
		public int val;
		public Foo() {
		}
		@Override
		public boolean equals(Object other) {
			if (other == null || other.getClass() != getClass())
				return false;
			return id == ((Foo) other).id;
		}
		@Override
		public String toString() {
			return "Foo.id=" + id;
		}
	}

	protected static class DoubleCreate {
		@DatabaseField(id = true)
		int id;
	}

	protected static class NoId {
		@DatabaseField
		public String notId;
	}

	private static class JustId {
		@DatabaseField(id = true)
		public String id;
	}

	private static class ForeignWrapper {
		private final static String FOREIGN_FIELD_NAME = "foreign";
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(foreign = true, columnName = FOREIGN_FIELD_NAME)
		AllTypes foreign;
	}

	private static class MultipleForeignWrapper {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(foreign = true)
		AllTypes foreign;
		@DatabaseField(foreign = true)
		ForeignWrapper foreignWrapper;
	}

	protected static class GetSet {
		@DatabaseField(generatedId = true, useGetSet = true)
		private int id;
		@DatabaseField(useGetSet = true)
		private String stuff;

		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public String getStuff() {
			return stuff;
		}
		public void setStuff(String stuff) {
			this.stuff = stuff;
		}
	}

	protected static class NoAnno {
		public int id;
		public String stuff;
		public NoAnno() {
		}
	}

	protected static class NoAnnoFor {
		public int id;
		public NoAnno foreign;
		public NoAnnoFor() {
		}
	}

	protected static class GeneratedIdNotNull {
		@DatabaseField(generatedId = true, canBeNull = false)
		public int id;
		@DatabaseField
		public String stuff;
		public GeneratedIdNotNull() {
		}
	}

	protected static class Basic {
		public final static String ID_FIELD = "id";
		@DatabaseField(id = true, columnName = ID_FIELD)
		String id;
	}

	private static class DefaultValue {
		@DatabaseField(defaultValue = DEFAULT_VALUE_STRING)
		Integer intField;
		DefaultValue() {
		}
	}

	protected static class NotNull {
		@DatabaseField(canBeNull = false)
		String notNull;
		NotNull() {
		}
	}

	protected static class GeneratedId {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		String other;
		public GeneratedId() {
		}
	}

	protected static class AllTypes {
		public static final String STRING_FIELD_NAME = "stringField";
		public static final String BOOLEAN_FIELD_NAME = "booleanField";
		public static final String DATE_FIELD_NAME = "dateField";
		public static final String DATE_LONG_FIELD_NAME = "dateLongField";
		public static final String DATE_STRING_FIELD_NAME = "dateStringField";
		public static final String SERIAL_FIELD_NAME = "serialField";
		public static final String BYTE_FIELD_NAME = "byteField";
		public static final String SHORT_FIELD_NAME = "shortField";
		public static final String INT_FIELD_NAME = "intField";
		public static final String LONG_FIELD_NAME = "longField";
		public static final String FLOAT_FIELD_NAME = "floatField";
		public static final String DOUBLE_FIELD_NAME = "doubleField";
		public static final String ENUM_FIELD_NAME = "enumField";
		public static final String ENUM_STRING_FIELD_NAME = "enumStringField";
		public static final String ENUM_INTEGER_FIELD_NAME = "enumIntegerField";
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(columnName = STRING_FIELD_NAME)
		String stringField;
		@DatabaseField(columnName = BOOLEAN_FIELD_NAME)
		boolean booleanField;
		@DatabaseField(columnName = DATE_FIELD_NAME)
		Date dateField;
		@DatabaseField(columnName = DATE_LONG_FIELD_NAME, dataType = DataType.DATE_LONG)
		Date dateLongField;
		@DatabaseField(columnName = DATE_STRING_FIELD_NAME, dataType = DataType.DATE_STRING, format = DEFAULT_DATE_STRING_FORMAT)
		Date dateStringField;
		@DatabaseField(columnName = BYTE_FIELD_NAME)
		byte byteField;
		@DatabaseField(columnName = SHORT_FIELD_NAME)
		short shortField;
		@DatabaseField(columnName = INT_FIELD_NAME)
		int intField;
		@DatabaseField(columnName = LONG_FIELD_NAME)
		long longField;
		@DatabaseField(columnName = FLOAT_FIELD_NAME)
		float floatField;
		@DatabaseField(columnName = DOUBLE_FIELD_NAME)
		double doubleField;
		@DatabaseField(columnName = SERIAL_FIELD_NAME, dataType = DataType.SERIALIZABLE)
		SerialData serialField;
		@DatabaseField(columnName = ENUM_FIELD_NAME)
		OurEnum enumField;
		@DatabaseField(columnName = ENUM_STRING_FIELD_NAME, dataType = DataType.ENUM_STRING)
		OurEnum enumStringField;
		@DatabaseField(columnName = ENUM_INTEGER_FIELD_NAME, dataType = DataType.ENUM_INTEGER)
		OurEnum enumIntegerField;
		AllTypes() {
		}
	}

	protected static class AllTypesDefault {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(defaultValue = DEFAULT_STRING_VALUE)
		String stringField;
		@DatabaseField(defaultValue = DEFAULT_DATE_VALUE)
		Date dateField;
		@DatabaseField(dataType = DataType.DATE_LONG, defaultValue = DEFAULT_DATE_LONG_VALUE)
		Date dateLongField;
		@DatabaseField(dataType = DataType.DATE_STRING, defaultValue = DEFAULT_DATE_STRING_VALUE, format = DEFAULT_DATE_STRING_FORMAT)
		Date dateStringField;
		@DatabaseField(defaultValue = DEFAULT_BOOLEAN_VALUE)
		boolean booleanField;
		@DatabaseField(defaultValue = DEFAULT_BOOLEAN_VALUE)
		Boolean booleanObj;
		@DatabaseField(defaultValue = DEFAULT_BYTE_VALUE)
		byte byteField;
		@DatabaseField(defaultValue = DEFAULT_BYTE_VALUE)
		Byte byteObj;
		@DatabaseField(defaultValue = DEFAULT_SHORT_VALUE)
		short shortField;
		@DatabaseField(defaultValue = DEFAULT_SHORT_VALUE)
		Short shortObj;
		@DatabaseField(defaultValue = DEFAULT_INT_VALUE)
		int intField;
		@DatabaseField(defaultValue = DEFAULT_INT_VALUE)
		Integer intObj;
		@DatabaseField(defaultValue = DEFAULT_LONG_VALUE)
		long longField;
		@DatabaseField(defaultValue = DEFAULT_LONG_VALUE)
		Long longObj;
		@DatabaseField(defaultValue = DEFAULT_FLOAT_VALUE)
		float floatField;
		@DatabaseField(defaultValue = DEFAULT_FLOAT_VALUE)
		Float floatObj;
		@DatabaseField(defaultValue = DEFAULT_DOUBLE_VALUE)
		double doubleField;
		@DatabaseField(defaultValue = DEFAULT_DOUBLE_VALUE)
		Double doubleObj;
		@DatabaseField(dataType = DataType.SERIALIZABLE)
		SerialData objectField;
		@DatabaseField(defaultValue = DEFAULT_ENUM_VALUE)
		OurEnum ourEnum;
		@DatabaseField(defaultValue = DEFAULT_ENUM_NUMBER_VALUE, dataType = DataType.ENUM_INTEGER)
		OurEnum ourEnumNumber;
		AllTypesDefault() {
		}
	}

	protected static class BooleanDefault {
		@DatabaseField(defaultValue = DEFAULT_BOOLEAN_VALUE)
		boolean booleanField;
		@DatabaseField(defaultValue = DEFAULT_BOOLEAN_VALUE)
		Boolean booleanObj;
		BooleanDefault() {
		}
	}

	protected static class AllObjectTypes {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		String stringField;
		@DatabaseField
		Boolean booleanField;
		@DatabaseField
		Date dateField;
		@DatabaseField
		Byte byteField;
		@DatabaseField
		Short shortField;
		@DatabaseField
		Integer intField;
		@DatabaseField
		Long longField;
		@DatabaseField
		Float floatField;
		@DatabaseField
		Double doubleField;
		@DatabaseField(dataType = DataType.SERIALIZABLE)
		SerialData objectField;
		@DatabaseField
		OurEnum ourEnum;
		AllObjectTypes() {
		}
	}

	protected static class NumberTypes {
		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField
		public byte byteField;
		@DatabaseField
		public short shortField;
		@DatabaseField
		public int intField;
		@DatabaseField
		public long longField;
		@DatabaseField
		public float floatField;
		@DatabaseField
		public double doubleField;
		public NumberTypes() {
		}
	}

	protected static class StringWidth {
		@DatabaseField(width = ALL_TYPES_STRING_WIDTH)
		String stringField;
		StringWidth() {
		}
	}

	// for testing reserved table names as fields
	private static class Where {
		@DatabaseField(id = true)
		public String id;
	}

	// for testing reserved words as field names
	protected static class ReservedField {
		@DatabaseField(id = true)
		public int select;
		@DatabaseField
		public String from;
		@DatabaseField
		public String table;
		@DatabaseField
		public String where;
		@DatabaseField
		public String group;
		@DatabaseField
		public String order;
		@DatabaseField
		public String values;
		public ReservedField() {
		}
	}

	// test the field name that has a capital letter in it
	protected static class GeneratedColumnCapital {
		@DatabaseField(generatedId = true, columnName = "idCap")
		int id;
		@DatabaseField
		String other;
		public GeneratedColumnCapital() {
		}
	}

	protected static class ObjectHolder {
		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField(dataType = DataType.SERIALIZABLE)
		public SerialData obj;
		@DatabaseField(dataType = DataType.SERIALIZABLE)
		public String strObj;
		public ObjectHolder() {
		}
	}

	protected static class NotSerializable {
		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField(dataType = DataType.SERIALIZABLE)
		public ObjectHolder obj;
		public NotSerializable() {
		}
	}

	protected static class SerialData implements Serializable {
		private static final long serialVersionUID = -3883857119616908868L;
		public Map<String, String> map;
		public SerialData() {
		}
		public void addEntry(String key, String value) {
			if (map == null) {
				map = new HashMap<String, String>();
			}
			map.put(key, value);
		}
		@Override
		public int hashCode() {
			return map.hashCode();
		}
		@Override
		public boolean equals(Object obj) {
			if (obj == null || obj.getClass() != getClass()) {
				return false;
			}
			SerialData other = (SerialData) obj;
			if (map == null) {
				return other.map == null;
			} else {
				return map.equals(other.map);
			}
		}
	}

	@DatabaseTable(tableName = ENUM_TABLE_NAME)
	protected static class LocalEnumString {
		@DatabaseField
		OurEnum ourEnum;
	}

	@DatabaseTable(tableName = ENUM_TABLE_NAME)
	protected static class LocalEnumString2 {
		@DatabaseField
		OurEnum2 ourEnum;
	}

	@DatabaseTable(tableName = ENUM_TABLE_NAME)
	protected static class LocalEnumInt {
		@DatabaseField(dataType = DataType.ENUM_INTEGER)
		OurEnum ourEnum;
	}

	@DatabaseTable(tableName = ENUM_TABLE_NAME)
	protected static class LocalEnumInt2 {
		@DatabaseField(dataType = DataType.ENUM_INTEGER)
		OurEnum2 ourEnum;
	}

	@DatabaseTable(tableName = ENUM_TABLE_NAME)
	protected static class LocalEnumInt3 {
		@DatabaseField(dataType = DataType.ENUM_INTEGER, unknownEnumName = "FIRST")
		OurEnum2 ourEnum;
	}

	private enum OurEnum {
		FIRST,
		SECOND, ;
	}

	private enum OurEnum2 {
		FIRST, ;
	}

	@DatabaseTable(tableName = NULL_BOOLEAN_TABLE_NAME)
	protected static class NullBoolean1 {
		@DatabaseField
		Boolean val;
	}

	@DatabaseTable(tableName = NULL_BOOLEAN_TABLE_NAME)
	protected static class NullBoolean2 {
		@DatabaseField(throwIfNull = true)
		boolean val;
	}

	@DatabaseTable(tableName = NULL_INT_TABLE_NAME)
	protected static class NullInt1 {
		@DatabaseField
		Integer val;
	}

	@DatabaseTable(tableName = NULL_INT_TABLE_NAME)
	protected static class NullInt2 {
		@DatabaseField(throwIfNull = true)
		int val;
	}

	@DatabaseTable
	protected static class NotNullDefault {
		@DatabaseField(canBeNull = false, defaultValue = "3")
		String stuff;
	}

	@DatabaseTable
	protected static class Unique {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		String stuff;
		@DatabaseField(unique = true)
		String uniqueStuff;
	}

	@DatabaseTable
	protected static class StringDefalt {
		@DatabaseField(defaultValue = "3")
		String stuff;
	}

	@DatabaseTable
	protected static class LocalDate {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		Date date;
	}

	@DatabaseTable
	protected static class StringId implements TestableType<String> {
		@DatabaseField(id = true)
		String id;
		@DatabaseField
		String stuff;
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getStuff() {
			return stuff;
		}
		public void setStuff(String stuff) {
			this.stuff = stuff;
		}
	}

	@DatabaseTable
	protected static class LongStringId implements TestableType<String> {
		@DatabaseField(id = true, dataType = DataType.LONG_STRING)
		String id;
		@DatabaseField
		String stuff;
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getStuff() {
			return stuff;
		}
		public void setStuff(String stuff) {
			this.stuff = stuff;
		}
	}

	@DatabaseTable
	protected static class BooleanId implements TestableType<Boolean> {
		@DatabaseField(id = true)
		boolean id;
		@DatabaseField
		String stuff;
		public Boolean getId() {
			return id;
		}
		public void setId(Boolean bool) {
			this.id = bool;
		}
		public String getStuff() {
			return stuff;
		}
		public void setStuff(String stuff) {
			this.stuff = stuff;
		}
	}

	@DatabaseTable
	protected static class BooleanObjId implements TestableType<Boolean> {
		@DatabaseField(id = true)
		Boolean id;
		@DatabaseField
		String stuff;
		public Boolean getId() {
			return id;
		}
		public void setId(Boolean bool) {
			this.id = bool;
		}
		public String getStuff() {
			return stuff;
		}
		public void setStuff(String stuff) {
			this.stuff = stuff;
		}
	}

	@DatabaseTable
	protected static class DateId implements TestableType<Date> {
		@DatabaseField(id = true)
		Date id;
		@DatabaseField
		String stuff;
		public Date getId() {
			return id;
		}
		public void setId(Date id) {
			this.id = id;
		}
		public String getStuff() {
			return stuff;
		}
		public void setStuff(String stuff) {
			this.stuff = stuff;
		}
	}

	@DatabaseTable
	protected static class DateLongId implements TestableType<Date> {
		@DatabaseField(id = true, dataType = DataType.DATE_LONG)
		Date id;
		@DatabaseField
		String stuff;
		public Date getId() {
			return id;
		}
		public void setId(Date id) {
			this.id = id;
		}
		public String getStuff() {
			return stuff;
		}
		public void setStuff(String stuff) {
			this.stuff = stuff;
		}
	}

	@DatabaseTable
	protected static class DateStringId implements TestableType<Date> {
		@DatabaseField(id = true, dataType = DataType.DATE_STRING)
		Date id;
		@DatabaseField
		String stuff;
		public Date getId() {
			return id;
		}
		public void setId(Date id) {
			this.id = id;
		}
		public String getStuff() {
			return stuff;
		}
		public void setStuff(String stuff) {
			this.stuff = stuff;
		}
	}

	@DatabaseTable
	protected static class ByteId implements TestableType<Byte> {
		@DatabaseField(id = true)
		byte id;
		@DatabaseField
		String stuff;
		public Byte getId() {
			return id;
		}
		public void setId(Byte id) {
			this.id = id;
		}
		public String getStuff() {
			return stuff;
		}
		public void setStuff(String stuff) {
			this.stuff = stuff;
		}
	}

	@DatabaseTable
	protected static class ByteObjId implements TestableType<Byte> {
		@DatabaseField(id = true)
		Byte id;
		@DatabaseField
		String stuff;
		public Byte getId() {
			return id;
		}
		public void setId(Byte id) {
			this.id = id;
		}
		public String getStuff() {
			return stuff;
		}
		public void setStuff(String stuff) {
			this.stuff = stuff;
		}
	}

	@DatabaseTable
	protected static class ShortId implements TestableType<Short> {
		@DatabaseField(id = true)
		short id;
		@DatabaseField
		String stuff;
		public Short getId() {
			return id;
		}
		public void setId(Short id) {
			this.id = id;
		}
		public String getStuff() {
			return stuff;
		}
		public void setStuff(String stuff) {
			this.stuff = stuff;
		}
	}

	@DatabaseTable
	protected static class ShortObjId implements TestableType<Short> {
		@DatabaseField(id = true)
		Short id;
		@DatabaseField
		String stuff;
		public Short getId() {
			return id;
		}
		public void setId(Short id) {
			this.id = id;
		}
		public String getStuff() {
			return stuff;
		}
		public void setStuff(String stuff) {
			this.stuff = stuff;
		}
	}

	@DatabaseTable
	protected static class IntId implements TestableType<Integer> {
		@DatabaseField(id = true)
		int id;
		@DatabaseField
		String stuff;
		public Integer getId() {
			return id;
		}
		public void setId(Integer id) {
			this.id = id;
		}
		public String getStuff() {
			return stuff;
		}
		public void setStuff(String stuff) {
			this.stuff = stuff;
		}
	}

	@DatabaseTable
	protected static class IntObjId implements TestableType<Integer> {
		@DatabaseField(id = true)
		Integer id;
		@DatabaseField
		String stuff;
		public Integer getId() {
			return id;
		}
		public void setId(Integer id) {
			this.id = id;
		}
		public String getStuff() {
			return stuff;
		}
		public void setStuff(String stuff) {
			this.stuff = stuff;
		}
	}

	@DatabaseTable
	protected static class LongId implements TestableType<Long> {
		@DatabaseField(id = true)
		long id;
		@DatabaseField
		String stuff;
		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		public String getStuff() {
			return stuff;
		}
		public void setStuff(String stuff) {
			this.stuff = stuff;
		}
	}

	@DatabaseTable
	protected static class LongObjId implements TestableType<Long> {
		@DatabaseField(id = true)
		Long id;
		@DatabaseField
		String stuff;
		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		public String getStuff() {
			return stuff;
		}
		public void setStuff(String stuff) {
			this.stuff = stuff;
		}
	}

	@DatabaseTable
	protected static class FloatId implements TestableType<Float> {
		@DatabaseField(id = true)
		float id;
		@DatabaseField
		String stuff;
		public Float getId() {
			return id;
		}
		public void setId(Float id) {
			this.id = id;
		}
		public String getStuff() {
			return stuff;
		}
		public void setStuff(String stuff) {
			this.stuff = stuff;
		}
	}

	@DatabaseTable
	protected static class FloatObjId implements TestableType<Float> {
		@DatabaseField(id = true)
		Float id;
		@DatabaseField
		String stuff;
		public Float getId() {
			return id;
		}
		public void setId(Float id) {
			this.id = id;
		}
		public String getStuff() {
			return stuff;
		}
		public void setStuff(String stuff) {
			this.stuff = stuff;
		}
	}

	@DatabaseTable
	protected static class DoubleId implements TestableType<Double> {
		@DatabaseField(id = true)
		double id;
		@DatabaseField
		String stuff;
		public Double getId() {
			return id;
		}
		public void setId(Double id) {
			this.id = id;
		}
		public String getStuff() {
			return stuff;
		}
		public void setStuff(String stuff) {
			this.stuff = stuff;
		}
	}

	@DatabaseTable
	protected static class DoubleObjId implements TestableType<Double> {
		@DatabaseField(id = true)
		Double id;
		@DatabaseField
		String stuff;
		public Double getId() {
			return id;
		}
		public void setId(Double id) {
			this.id = id;
		}
		public String getStuff() {
			return stuff;
		}
		public void setStuff(String stuff) {
			this.stuff = stuff;
		}
	}

	@DatabaseTable
	protected static class EnumId implements TestableType<OurEnum> {
		@DatabaseField(id = true)
		OurEnum ourEnum;
		@DatabaseField
		String stuff;
		public OurEnum getId() {
			return ourEnum;
		}
		public void setId(OurEnum id) {
			this.ourEnum = id;
		}
		public String getStuff() {
			return stuff;
		}
		public void setStuff(String stuff) {
			this.stuff = stuff;
		}
	}

	@DatabaseTable
	protected static class EnumStringId implements TestableType<OurEnum> {
		@DatabaseField(id = true, dataType = DataType.ENUM_STRING)
		OurEnum ourEnum;
		@DatabaseField
		String stuff;
		public OurEnum getId() {
			return ourEnum;
		}
		public void setId(OurEnum id) {
			this.ourEnum = id;
		}
		public String getStuff() {
			return stuff;
		}
		public void setStuff(String stuff) {
			this.stuff = stuff;
		}
	}

	@DatabaseTable
	protected static class EnumIntegerId implements TestableType<OurEnum> {
		@DatabaseField(id = true, dataType = DataType.ENUM_INTEGER)
		OurEnum ourEnum;
		@DatabaseField
		String stuff;
		public OurEnum getId() {
			return ourEnum;
		}
		public void setId(OurEnum id) {
			this.ourEnum = id;
		}
		public String getStuff() {
			return stuff;
		}
		public void setStuff(String stuff) {
			this.stuff = stuff;
		}
	}

	@DatabaseTable
	protected static class SerializableId implements TestableType<SerialData> {
		@DatabaseField(id = true, dataType = DataType.SERIALIZABLE)
		SerialData serial;
		@DatabaseField
		String stuff;
		public SerialData getId() {
			return serial;
		}
		public void setId(SerialData id) {
			this.serial = id;
		}
		public String getStuff() {
			return stuff;
		}
		public void setStuff(String stuff) {
			this.stuff = stuff;
		}
	}

	@DatabaseTable
	protected static class Recursive {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(foreign = true)
		Recursive foreign;
		public Recursive() {
		}
	}

	@DatabaseTable
	protected static class SerializedBytes {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(dataType = DataType.SERIALIZABLE)
		byte[] bytes;
		public SerializedBytes() {
		}
	}

	@DatabaseTable
	protected static class ByteArray {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(dataType = DataType.BYTE_ARRAY)
		byte[] bytes;
		public ByteArray() {
		}
	}

	@DatabaseTable
	protected static class Index {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(index = true)
		String stuff;
		public Index() {
		}
	}

	@DatabaseTable
	protected static class IndexColumnName {
		public static final String STUFF_COLUMN_NAME = "notStuff";
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(index = true, columnName = STUFF_COLUMN_NAME)
		String stuff;
		public IndexColumnName() {
		}
	}

	@DatabaseTable
	protected static class UniqueIndex {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(uniqueIndex = true)
		String stuff;
		public UniqueIndex() {
		}
	}

	@DatabaseTable
	protected static class ComboIndex {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(indexName = "stuffjunk")
		String stuff;
		@DatabaseField(indexName = "stuffjunk")
		long junk;
		public ComboIndex() {
		}
	}

	@DatabaseTable
	protected static class ComboUniqueIndex {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(uniqueIndexName = "stuffjunk")
		String stuff;
		@DatabaseField(uniqueIndexName = "stuffjunk")
		long junk;
		public ComboUniqueIndex() {
		}
	}

	@DatabaseTable
	protected static class LongVarChar {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(dataType = DataType.LONG_STRING)
		String stuff;
		public LongVarChar() {
		}
	}

	protected static class Base {
		@DatabaseField(id = true)
		int id;
		public Base() {
			// for ormlite
		}
	}

	protected static class Sub extends Base {
		@DatabaseField
		String stuff;
		public Sub() {
			// for ormlite
		}
	}

	protected static class UuidGeneratedId {
		@DatabaseField(generatedId = true)
		public UUID id;
		@DatabaseField
		public String stuff;
		public UuidGeneratedId() {
		}
	}

	protected static class One extends BaseDaoEnabled<One, Integer> {
		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField
		public String stuff;
		public One() {
		}
	}

	protected static class ForeignDaoEnabled extends BaseDaoEnabled<ForeignDaoEnabled, Integer> {
		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField(foreign = true)
		public One one;
		public ForeignDaoEnabled() {
		}
	}

	protected static class EagerAccount {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		String name;
		@ForeignCollectionField(eager = true)
		Collection<EagerOrder> orders;
		protected EagerAccount() {
		}
	}

	protected static class EagerOrder {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		int val;
		@DatabaseField(foreign = true)
		EagerAccount account;
		protected EagerOrder() {
		}
	}

	protected static class LazyAccount {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		String name;
		@ForeignCollectionField
		Collection<LazyOrder> orders;
		protected LazyAccount() {
		}
	}

	protected static class LazyOrder {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		int val;
		@DatabaseField(foreign = true)
		LazyAccount account;
		protected LazyOrder() {
		}
	}
}
