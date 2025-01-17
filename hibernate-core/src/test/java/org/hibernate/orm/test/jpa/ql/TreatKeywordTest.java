/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.jpa.ql;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Arrays;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import org.junit.Test;

import org.hibernate.testing.FailureExpected;
import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.junit4.BaseCoreFunctionalTestCase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Steve Ebersole
 */
public class TreatKeywordTest extends BaseCoreFunctionalTestCase {
	@Override
	protected Class<?>[] getAnnotatedClasses() {
		return new Class[] {
				JoinedEntity.class, JoinedEntitySubclass.class, JoinedEntitySubSubclass.class,
				JoinedEntitySubclass2.class, JoinedEntitySubSubclass2.class,
				DiscriminatorEntity.class, DiscriminatorEntitySubclass.class, DiscriminatorEntitySubSubclass.class,
				Animal.class, Dog.class, Dachshund.class, Greyhound.class
		};
	}

	@Test
	public void testBasicUsageInJoin() {
		// todo : assert invalid naming of non-subclasses in TREAT statement
		Session s = openSession();

		s.createQuery( "from DiscriminatorEntity e join treat(e.other as DiscriminatorEntitySubclass) o" ).list();
		s.createQuery( "from DiscriminatorEntity e join treat(e.other as DiscriminatorEntitySubSubclass) o" ).list();
		s.createQuery( "from DiscriminatorEntitySubclass e join treat(e.other as DiscriminatorEntitySubSubclass) o" ).list();

		s.createQuery( "from JoinedEntity e join treat(e.other as JoinedEntitySubclass) o" ).list();
		s.createQuery( "from JoinedEntity e join treat(e.other as JoinedEntitySubSubclass) o" ).list();
		s.createQuery( "from JoinedEntitySubclass e join treat(e.other as JoinedEntitySubSubclass) o" ).list();

		s.close();
	}

	@Test
	@TestForIssue( jiraKey = "HHH-8637" )
	public void testFilteringDiscriminatorSubclasses() {
		Session s = openSession();
		s.beginTransaction();
		DiscriminatorEntity root = new DiscriminatorEntity( 1, "root" );
		s.save( root );
		DiscriminatorEntitySubclass child = new DiscriminatorEntitySubclass( 2, "child", root );
		s.save( child );
		s.getTransaction().commit();
		s.close();

		s = openSession();
		s.beginTransaction();

		// in select clause
		List result = s.createQuery( "select e from DiscriminatorEntity e" ).list();
		assertEquals( 2, result.size() );
		result = s.createQuery( "select treat (e as DiscriminatorEntitySubclass) from DiscriminatorEntity e" ).list();
		assertEquals( 1, result.size() );
		result = s.createQuery( "select treat (e as DiscriminatorEntitySubSubclass) from DiscriminatorEntity e" ).list();
		assertEquals( 0, result.size() );

		// in join
		result = s.createQuery( "from DiscriminatorEntity e inner join e.other" ).list();
		assertEquals( 1, result.size() );
		result = s.createQuery( "from DiscriminatorEntity e inner join treat (e.other as DiscriminatorEntitySubclass)" ).list();
		assertEquals( 0, result.size() );
		result = s.createQuery( "from DiscriminatorEntity e inner join treat (e.other as DiscriminatorEntitySubSubclass)" ).list();
		assertEquals( 0, result.size() );

		s.close();

		s = openSession();
		s.beginTransaction();
		s.delete( root );
		s.delete( child );
		s.getTransaction().commit();
		s.close();
	}

	@Test
	@TestForIssue( jiraKey = "HHH-8637" )
	public void testFilteringJoinedSubclasses() {
		Session s = openSession();
		s.beginTransaction();
		JoinedEntity root = new JoinedEntity( 1, "root" );
		s.save( root );
		JoinedEntitySubclass child = new JoinedEntitySubclass( 2, "child", root );
		s.save( child );
		s.getTransaction().commit();
		s.close();

		s = openSession();
		s.beginTransaction();

		// in the select clause which causes an implicit inclusion of subclass joins, the test here makes sure that
		// the TREAT-AS effects the join-type used.
		List result = s.createQuery( "select e from JoinedEntity e" ).list();
		assertEquals( 2, result.size() );
		result = s.createQuery( "select treat (e as JoinedEntitySubclass) from JoinedEntity e" ).list();
		assertEquals( 1, result.size() );
		result = s.createQuery( "select treat (e as JoinedEntitySubSubclass) from JoinedEntity e" ).list();
		assertEquals( 0, result.size() );

		// in join
		result = s.createQuery( "from JoinedEntity e inner join e.other" ).list();
		assertEquals( 1, result.size() );
		result = s.createQuery( "from JoinedEntity e inner join treat (e.other as JoinedEntitySubclass)" ).list();
		assertEquals( 0, result.size() );
		result = s.createQuery( "from JoinedEntity e inner join treat (e.other as JoinedEntitySubSubclass)" ).list();
		assertEquals( 0, result.size() );

		s.close();

		s = openSession();
		s.beginTransaction();
		s.delete( child );
		s.delete( root );
		s.getTransaction().commit();
		s.close();
	}

	@Test
	@TestForIssue( jiraKey = "HHH-9862" )
	public void testRestrictionsOnJoinedSubclasses() {
		Session s = openSession();
		s.beginTransaction();
		JoinedEntity root = new JoinedEntity( 1, "root" );
		s.save( root );
		JoinedEntitySubclass child1 = new JoinedEntitySubclass( 2, "child1", root );
		s.save( child1 );
		JoinedEntitySubclass2 child2 = new JoinedEntitySubclass2( 3, "child2", root );
		s.save( child2 );
		s.getTransaction().commit();
		s.close();

		s = openSession();
		s.beginTransaction();

		List result = s.createQuery( "select e from JoinedEntity e where treat (e as JoinedEntitySubclass ).name = 'child1'" ).list();
		assertEquals( 1, result.size() );
		assertTrue( JoinedEntitySubclass.class.isInstance( result.get( 0 ) ) );

		result = s.createQuery( "select e from JoinedEntity e where treat (e as JoinedEntitySubclass2 ).name = 'child1'" ).list();
		assertEquals( 0, result.size() );

		result = s.createQuery( "select e from JoinedEntity e where treat (e as JoinedEntitySubclass2 ).name = 'child2'" ).list();
		assertEquals( 1, result.size() );
		assertTrue( JoinedEntitySubclass2.class.isInstance( result.get( 0 ) ) );

		result = s.createQuery( "select e from JoinedEntity e where treat (e as JoinedEntitySubclass ).name = 'child1' or treat (e as JoinedEntitySubclass2 ).name = 'child2'" ).list();
		assertEquals( 2, result.size() );

		s.close();

		s = openSession();
		s.beginTransaction();
		s.delete( child1 );
		s.delete( child2 );
		s.delete( root );
		s.getTransaction().commit();
		s.close();
	}

	@Test
	@TestForIssue(jiraKey = "HHH-9411")
	public void testTreatWithRestrictionOnAbstractClass() {
		Session s = openSession();
		Transaction tx = s.beginTransaction();

		Greyhound greyhound = new Greyhound();
		Dachshund dachshund = new Dachshund();
		s.save( greyhound );
		s.save( dachshund );

		List results = s.createQuery( "select treat (a as Dog) from Animal a where a.fast = TRUE" ).list();

		assertEquals( Arrays.asList( greyhound ), results );

		tx.commit();
		s.close();
	}

	@Entity( name = "JoinedEntity" )
	@Table( name = "JoinedEntity" )
	@Inheritance( strategy = InheritanceType.JOINED )
	public static class JoinedEntity {
		@Id
		public Integer id;
		public String name;
		@ManyToOne( fetch = FetchType.LAZY )
		public JoinedEntity other;

		public JoinedEntity() {
		}

		public JoinedEntity(Integer id, String name) {
			this.id = id;
			this.name = name;
		}

		public JoinedEntity(Integer id, String name, JoinedEntity other) {
			this.id = id;
			this.name = name;
			this.other = other;
		}
	}

	@Entity( name = "JoinedEntitySubclass" )
	@Table( name = "JoinedEntitySubclass" )
	public static class JoinedEntitySubclass extends JoinedEntity {
		public JoinedEntitySubclass() {
		}

		public JoinedEntitySubclass(Integer id, String name) {
			super( id, name );
		}

		public JoinedEntitySubclass(Integer id, String name, JoinedEntity other) {
			super( id, name, other );
		}
	}

	@Entity( name = "JoinedEntitySubSubclass" )
	@Table( name = "JoinedEntitySubSubclass" )
	public static class JoinedEntitySubSubclass extends JoinedEntitySubclass {
		public JoinedEntitySubSubclass() {
		}

		public JoinedEntitySubSubclass(Integer id, String name) {
			super( id, name );
		}

		public JoinedEntitySubSubclass(Integer id, String name, JoinedEntity other) {
			super( id, name, other );
		}
	}

	@Entity( name = "JoinedEntitySubclass2" )
	@Table( name = "JoinedEntitySubclass2" )
	public static class JoinedEntitySubclass2 extends JoinedEntity {
		public JoinedEntitySubclass2() {
		}

		public JoinedEntitySubclass2(Integer id, String name) {
			super( id, name );
		}

		public JoinedEntitySubclass2(Integer id, String name, JoinedEntity other) {
			super( id, name, other );
		}
	}

	@Entity( name = "JoinedEntitySubSubclass2" )
	@Table( name = "JoinedEntitySubSubclass2" )
	public static class JoinedEntitySubSubclass2 extends JoinedEntitySubclass2 {
		public JoinedEntitySubSubclass2() {
		}

		public JoinedEntitySubSubclass2(Integer id, String name) {
			super( id, name );
		}

		public JoinedEntitySubSubclass2(Integer id, String name, JoinedEntity other) {
			super( id, name, other );
		}
	}

	@Entity( name = "DiscriminatorEntity" )
	@Table( name = "DiscriminatorEntity" )
	@Inheritance( strategy = InheritanceType.SINGLE_TABLE )
	@DiscriminatorColumn( name = "e_type", discriminatorType = DiscriminatorType.STRING )
	@DiscriminatorValue( "B" )
	public static class DiscriminatorEntity {
		@Id
		public Integer id;
		public String name;
		@ManyToOne( fetch = FetchType.LAZY )
		public DiscriminatorEntity other;

		public DiscriminatorEntity() {
		}

		public DiscriminatorEntity(Integer id, String name) {
			this.id = id;
			this.name = name;
		}

		public DiscriminatorEntity(
				Integer id,
				String name,
				DiscriminatorEntity other) {
			this.id = id;
			this.name = name;
			this.other = other;
		}
	}

	@Entity( name = "DiscriminatorEntitySubclass" )
	@DiscriminatorValue( "S" )
	public static class DiscriminatorEntitySubclass extends DiscriminatorEntity {
		public DiscriminatorEntitySubclass() {
		}

		public DiscriminatorEntitySubclass(Integer id, String name) {
			super( id, name );
		}

		public DiscriminatorEntitySubclass(
				Integer id,
				String name,
				DiscriminatorEntity other) {
			super( id, name, other );
		}
	}

	@Entity( name = "DiscriminatorEntitySubSubclass" )
	@DiscriminatorValue( "SS" )
	public static class DiscriminatorEntitySubSubclass extends DiscriminatorEntitySubclass {
		public DiscriminatorEntitySubSubclass() {
		}

		public DiscriminatorEntitySubSubclass(Integer id, String name) {
			super( id, name );
		}

		public DiscriminatorEntitySubSubclass(
				Integer id,
				String name,
				DiscriminatorEntity other) {
			super( id, name, other );
		}
	}

	@Entity(name = "Animal")
	public static abstract class Animal {
		@Id
		@GeneratedValue
		private Long id;
	}

	@Entity(name = "Dog")
	public static abstract class Dog extends Animal {
		private boolean fast;

		protected Dog(boolean fast) {
			this.fast = fast;
		}

		public final boolean isFast() {
			return fast;
		}
	}

	@Entity(name = "Dachshund")
	public static class Dachshund extends Dog {
		public Dachshund() {
			super( false );
		}
	}

	@Entity(name = "Greyhound")
	public static class Greyhound extends Dog {
		public Greyhound() {
			super( true );
		}
	}
}
