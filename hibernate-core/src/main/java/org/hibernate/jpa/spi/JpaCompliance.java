/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.jpa.spi;

import org.hibernate.Transaction;

/**
 * Encapsulates settings controlling whether Hibernate complies strictly
 * with certain debatable strictures of the JPA specification.
 *
 * @author Steve Ebersole
 */
public interface JpaCompliance {
	/**
	 * Controls whether Hibernate's handling of JPA's
	 * {@link jakarta.persistence.Query} (JPQL, Criteria and native-query)
	 * should strictly follow the JPA spec. This includes parsing and
	 * translating a query as JPQL instead of HQL, as well as whether calls
	 * to the {@link jakarta.persistence.Query} methods always throw the
	 * exceptions defined by the specification.
	 *
	 * Deviations result in an exception if enabled
	 *
	 * @return {@code true} indicates to behave in the spec-defined way
	 *
	 * @see org.hibernate.cfg.AvailableSettings#JPA_QUERY_COMPLIANCE
	 */
	boolean isJpaQueryComplianceEnabled();

	/**
	 * Indicates that Hibernate's {@link Transaction} should behave as
	 * defined by the specification for JPA's
	 * {@link jakarta.persistence.EntityTransaction} since it extends it.
	 *
	 * @return {@code true} indicates to behave in the spec-defined way
	 *
	 * @see org.hibernate.cfg.AvailableSettings#JPA_TRANSACTION_COMPLIANCE
	 */
	boolean isJpaTransactionComplianceEnabled();

	/**
	 * Controls how Hibernate interprets a mapped {@link java.util.List}
	 * without no {@linkplain jakarta.persistence.OrderColumn order column}
	 * specified. Historically Hibernate treats this as a "bag", which is a
	 * concept JPA does not have.
	 * <ul>
	 * <li>If enabled, Hibernate will recognize this case as a
	 *     {@linkplain org.hibernate.collection.spi.PersistentList list}.
	 * <li>Otherwise, if disabled, Hibernate will treat it as a
	 *     {@linkplain org.hibernate.collection.spi.PersistentBag "bag"}.
	 * </ul>
	 *
	 * @return {@code true} indicates to behave in the spec-defined way,
	 * interpreting the mapping as a "list", rather than a "bag"
	 *
	 * @see org.hibernate.cfg.AvailableSettings#JPA_LIST_COMPLIANCE
	 *
	 * @deprecated Use {@link org.hibernate.cfg.AvailableSettings#DEFAULT_LIST_SEMANTICS} instead
	 */
	@Deprecated( since = "6.0" )
	boolean isJpaListComplianceEnabled();

	/**
	 * JPA defines specific exceptions on specific methods when called on
	 * {@link jakarta.persistence.EntityManager} and
	 * {@link jakarta.persistence.EntityManagerFactory} when those objects
	 * have been closed. This setting controls whether the spec defined
	 * behavior or Hibernate's behavior will be used.
	 * <p>
	 * If enabled Hibernate will operate in the JPA specified way throwing
	 * exceptions when the spec says it should with regard to close checking
	 *
	 * @return {@code true} indicates to behave in the spec-defined way
	 *
	 * @see org.hibernate.cfg.AvailableSettings#JPA_CLOSED_COMPLIANCE
	 */
	boolean isJpaClosedComplianceEnabled();

	/**
	 * JPA spec says that an {@link jakarta.persistence.EntityNotFoundException}
	 * should be thrown when accessing an entity proxy which does not have
	 * an associated table row in the database.
	 * <p>
	 * Traditionally, Hibernate does not initialize an entity Proxy when
	 * accessing its identifier since we already know the identifier value,
	 * hence we can save a database round trip.
	 * <p>
	 * If enabled Hibernate will initialize the entity proxy even when
	 * accessing its identifier.
	 *
	 * @return {@code true} indicates to behave in the spec-defined way
	 *
	 * @see org.hibernate.cfg.AvailableSettings#JPA_PROXY_COMPLIANCE
	 */
	boolean isJpaProxyComplianceEnabled();

	/**
	 * Should Hibernate comply with all aspects of caching as defined by JPA?
	 * Or can it deviate to perform things it believes will be "better"?
	 *
	 * @implNote Effects include marking all secondary tables as non-optional.
	 * The reason being that optional secondary tables can lead to entity cache
	 * being invalidated rather than updated.
	 *
	 * @return {@code true} indicates to behave in the spec-defined way
	 *
	 * @see org.hibernate.cfg.AvailableSettings#JPA_CACHING_COMPLIANCE
	 * @see org.hibernate.persister.entity.AbstractEntityPersister#isCacheInvalidationRequired()
	 */
	boolean isJpaCacheComplianceEnabled();

	/**
	 * Should the scope of {@link jakarta.persistence.TableGenerator#name()}
	 * and {@link jakarta.persistence.SequenceGenerator#name()} be considered
	 * globally or locally defined?
	 *
	 * @return {@code true} if the generator name scope is considered global
	 *
	 * @see org.hibernate.cfg.AvailableSettings#JPA_ID_GENERATOR_GLOBAL_SCOPE_COMPLIANCE
	 */
	boolean isGlobalGeneratorScopeEnabled();

	/**
	 * Should we strictly handle {@link jakarta.persistence.OrderBy} expressions?
	 * <p>
	 * JPA says the order-items can only be attribute references whereas
	 * Hibernate supports a wide range of items.  With this enabled, Hibernate
	 * will throw a compliance error when a non-attribute-reference is used.
	 *
	 * @see org.hibernate.cfg.AvailableSettings#JPA_ORDER_BY_MAPPING_COMPLIANCE
	 */
	boolean isJpaOrderByMappingComplianceEnabled();

	/**
	 * JPA says that the id passed to
	 * {@link jakarta.persistence.EntityManager#getReference} and
	 * {@link jakarta.persistence.EntityManager#find} should be exactly the
	 * expected type, allowing no type coercion.
	 * <p>
	 * Historically, Hibernate behaved the same way. Since 6.0 however,
	 * Hibernate has the ability to coerce the passed type to the expected
	 * type. For example, an {@link Integer} may be widened to {@link Long}.
	 * Coercion is performed by calling
	 * {@link org.hibernate.type.descriptor.java.JavaType#coerce}.
	 * <p>
	 * This setting controls whether such coercion should be allowed.
	 *
	 * @see org.hibernate.cfg.AvailableSettings#JPA_LOAD_BY_ID_COMPLIANCE
	 *
	 * @since 6.0
	 */
	boolean isLoadByIdComplianceEnabled();

	/**
	 * JPA says that mutations done to {@link jakarta.persistence.criteria.CriteriaQuery},
	 * {@link jakarta.persistence.criteria.CriteriaUpdate} and {@link jakarta.persistence.criteria.CriteriaDelete}
	 * after such objects were used to create a {@link jakarta.persistence.Query} may not affect that query.
	 * This requirement makes it necessary to copy these objects because the APIs allow mutations.
	 *
	 * If disabled, it is assumed that users do not mutate the criteria query afterwards
	 * and due to that, no copy will be created, which will improve performance.
	 *
	 * By default, no copies are created to not hurt performance. When enabled,
	 * criteria query objects are copied, as required by the JPA specification.
	 *
	 * @see org.hibernate.cfg.AvailableSettings#JPA_CRITERIA_COPY_COMPLIANCE
	 *
	 * @since 6.0
	 */
	boolean isJpaCriteriaCopyComplianceEnabled();
}
